package com.ruoyi.monitor.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.monitor.domain.MonitorConfig;
import com.ruoyi.monitor.service.IMonitorConfigService;
import com.ruoyi.monitor.service.IMonitorDataService;
import com.ruoyi.monitor.service.ISqlMergeService;
import com.ruoyi.monitor.utils.*;
import com.ruoyi.monitor.utils.XmlParser;
import com.ruoyi.monitor.cache.XmlDataCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 监控数据获取Service实现类
 */
@Service
public class MonitorDataServiceImpl implements IMonitorDataService 
{
    private static final Logger log = LoggerFactory.getLogger(MonitorDataServiceImpl.class);

    @Autowired
    private IMonitorConfigService monitorConfigService;

    @Autowired
    private ISqlMergeService sqlMergeService;

    @Autowired
    private XmlDataCache xmlDataCache;

    @Override
    public Map<String, Object> getMonitorData(String configKey) 
    {
        return getMonitorData(configKey, null);
    }

    @Override
    public Map<String, Object> getMonitorData(String configKey, Map<String, Object> params)
    {
        Map<String, Object> resultData = new HashMap<>();

        try {
            MonitorConfig config = monitorConfigService.selectMonitorConfigByConfigKey(configKey);
            if (config == null) {
                log.error("监控配置不存在: {}", configKey);
                return resultData;
            }
            
            JSONObject configJson = JSON.parseObject(config.getConfigJson());
            Map<String, Object> dbData = MonitorDataProcessor.getDatabaseData(configJson, params, sqlMergeService);
            Map<String, Object> apiData = MonitorDataProcessor.getApiData(configKey, configJson, params, xmlDataCache);
            
            Map<String, Object> allData = new HashMap<>();
            allData.putAll(dbData);
            allData.putAll(apiData);
            
            MonitorDataProcessor.processComputedData(configJson, allData);
            log.info("开始构建前端需要的数据类型");
            resultData = MonitorDataProcessor.buildFrontendData(configJson, allData);
            
        } catch (Exception e) {
            log.error("获取监控数据失败", e);
        }

        return resultData;
    }

    /**
     * 获取下拉框选项
     */
    @Override
    public Map<String, Object> getSelectOptions(String configKey, Map<String, Object> params)
    {
        Map<String, Object> result = new HashMap<>();

        log.info("==================== 开始获取下拉框选项 ====================");
        log.info("配置KEY: {}", configKey);
        log.info("请求参数: {}", params);

        try
        {
            // 1. 查询配置
            MonitorConfig config = monitorConfigService.selectMonitorConfigByConfigKey(configKey);
            if (config == null)
            {
                log.error("监控配置不存在: {}", configKey);
                return result;
            }

            // 2. 解析配置JSON
            JSONObject configJson = JSON.parseObject(config.getConfigJson());

            // 3. 获取基础配置
            String group = configJson.getString("procConditionGroup");
            String majorCd = configJson.getString("majorClassCd");
            String minorCd = configJson.getString("minorClassCd");
            MonitorConfigUtil.ProjectConfig projectConfig = new MonitorConfigUtil.ProjectConfig(group);
            boolean isGpProject = projectConfig.isGp();
            int cdIndex = projectConfig.getCdIndex();

            log.info("基础配置: group={}, majorCd={}, minorCd={}, isGpProject={}, cdIndex={}",
                     group, majorCd, minorCd, isGpProject, cdIndex);

            // 4. 构建XML请求体
            params.put("majorClassCd", majorCd);
            params.put("minorClassCd", minorCd);

            String xmlRequest;
            if (isGpProject)
            {
                // GP项目
                xmlRequest = XmlParser.buildGpXmlRequest(params);
            }
            else
            {
                // 非GP项目
                xmlRequest = XmlParser.buildNonGpXmlRequest(params);
            }

            log.info("XML请求体: {}", xmlRequest);

            // 5. 调用API
            String apiUrl = configJson.getString("apiUrl");
            if (StringUtils.isEmpty(apiUrl))
            {
                log.warn("未配置API地址");
                return result;
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> entity = new HttpEntity<>(xmlRequest, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful())
            {
                log.error("API调用失败，状态码: {}", response.getStatusCode());
                return result;
            }

            String xmlResponse = response.getBody();
            log.info("XML响应: {}", xmlResponse);

            // 6. 解析XML响应
            List<Map<String, String>> xmlRows = XmlParser.parseXmlResponse(xmlResponse);
            log.info("解析XML，共 {} 个row", xmlRows.size());

            // 6.5 清理XML rows（非GP项目按固定位置移除分隔符）
            List<Map<String, String>> cleanedXmlRows = new ArrayList<>();
            for (Map<String, String> row : xmlRows)
            {
                cleanedXmlRows.add(MonitorDataProcessor.cleanXmlRowIfNeeded(row, isGpProject));
            }

            // 7. 缓存XML数据（用于后续查询时直接使用，避免重复调用API）
            String cacheKey = xmlDataCache.generateCacheKey(configKey, params);
            xmlDataCache.put(cacheKey, cleanedXmlRows);
            log.info("已缓存XML数据，cacheKey: {}", cacheKey);

        // 8. 处理下拉框配置 - 使用简化算法
        if (configJson.containsKey("formItems"))
        {
            // 使用之前创建的项目配置对象
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> formItems = (List<Map<String, Object>>) configJson.get("formItems");

            for (Map<String, Object> item : formItems)
            {
                if ("select".equals(item.get("type")) && "xml".equals(item.get("dataSource")))
                {
                    String prop = (String) item.get("prop");
                    String expression = (String) item.get("expression");

                    log.info("处理下拉框: prop={}, expression={}", prop, expression);

                    Set<String> options = MonitorDataProcessor.extractSelectOptions(expression, cleanedXmlRows, projectConfig);
                    
                    log.info("下拉框 {} 的选项: {}", prop, options);
                    result.put(prop, new ArrayList<>(options));
                }
            }
        }
        } catch (Exception e) {
            log.error("==================== 获取下拉框选项失败 ====================", e);
            log.error("配置KEY: {}", configKey);
            log.error("请求参数: {}", params);
        }
        return result;
    }
}


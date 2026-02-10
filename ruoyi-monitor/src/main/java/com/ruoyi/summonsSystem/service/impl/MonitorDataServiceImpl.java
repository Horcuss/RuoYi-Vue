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
            
            // 处理指示书信息表格数据
            log.info("开始处理指示书信息表格数据");
            List<Map<String, Object>> instructionTableData = processInstructionTableData(configJson);
            resultData.put("instructionTableData", instructionTableData);
            
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

    /**
     * 处理指示书信息表格数据
     * @param configJson 监控配置JSON
     * @return 指示书信息表格数据列表
     */
    private List<Map<String, Object>> processInstructionTableData(JSONObject configJson) {
        List<Map<String, Object>> tableData = new ArrayList<>();
        
        try {
            // 生成模拟XML字符串（后续替换为实际API调用）
            String xmlResponse = generateMockInstructionXml();
            
            // 解析XML
            List<List<Map<String, String>>> patterns = parseInstructionXml(xmlResponse);
            
            if (patterns.size() < 5) {
                log.warn("XML格式不正确，pattern数量不足");
                return tableData;
            }
            
            // Pattern[2]: 指示书主要信息
            List<Map<String, String>> pattern2 = patterns.get(2);
            // Pattern[3]: 追加情报
            List<Map<String, String>> pattern3 = patterns.get(3);
            // Pattern[4]: Lot信息
            List<Map<String, String>> pattern4 = patterns.get(4);
            
            // 判断是否GP项目
            String procConditionGroup = configJson.getString("procConditionGroup");
            boolean isGpProject = "02".equals(procConditionGroup);
            
            // 按指示书NO合并数据
            Map<String, Map<String, Object>> instructionMap = new HashMap<>();
            
            // 处理Pattern[2] - 主要信息
            for (Map<String, String> row : pattern2) {
                String instructionNo = getItemValue(row, 0);
                if (StringUtils.isEmpty(instructionNo)) continue;
                
                Map<String, Object> instruction = new HashMap<>();
                instruction.put("instructionNo", instructionNo);
                instruction.put("issueType", getItemValue(row, 4)); // 发行区分
                instruction.put("revisionRecord", getItemValue(row, 1)); // 改订记号
                instruction.put("documentType", getItemValue(row, 2)); // 文书类别名
                instruction.put("instructionName", getItemValue(row, 3)); // 指示书名
                instruction.put("mainTextTitle", getItemValue(row, 5)); // 本文标题
                instruction.put("mainTextBlobId", getItemValue(row, 6)); // 本文文件blobid
                instruction.put("processingConditionTitle", getItemValue(row, 7)); // 加工条件标题
                instruction.put("processingConditionBlobId", getItemValue(row, 8)); // 加工条件文件blobid
                
                instructionMap.put(instructionNo, instruction);
            }
            
            // 处理Pattern[3] - 追加情报
            for (Map<String, String> row : pattern3) {
                String instructionNo = getItemValue(row, 0);
                if (StringUtils.isEmpty(instructionNo)) continue;
                
                Map<String, Object> instruction = instructionMap.get(instructionNo);
                if (instruction != null) {
                    instruction.put("additionalInfoTitle", getItemValue(row, 2)); // 追加情报标题
                    instruction.put("additionalInfoBlobId", getItemValue(row, 3)); // 追加情报文件blobid
                }
            }
            
            // 处理Pattern[4] - Lot信息
            for (Map<String, String> row : pattern4) {
                String instructionNo = getItemValue(row, 0);
                if (StringUtils.isEmpty(instructionNo)) continue;
                
                Map<String, Object> instruction = instructionMap.get(instructionNo);
                if (instruction != null) {
                    String lotNo = getItemValue(row, 3);
                    String productNo = getItemValue(row, 4);
                    String processSeries = getItemValue(row, 5);
                    
                    // 根据项目类型拼接lotInfo
                    String lotInfo;
                    if (isGpProject) {
                        // GP项目：显示 品番・工程系列
                        lotInfo = (StringUtils.isNotEmpty(productNo) ? productNo : "") + 
                                  "・" + 
                                  (StringUtils.isNotEmpty(processSeries) ? processSeries : "");
                    } else {
                        // 非GP项目：显示 lot no
                        lotInfo = StringUtils.isNotEmpty(lotNo) ? lotNo : "";
                    }
                    
                    instruction.put("lotInfo", lotInfo);
                    instruction.put("middleClassName", getItemValue(row, 6)); // 中分类名
                    instruction.put("comment", getItemValue(row, 7)); // comment
                }
            }
            
            // 转换为列表
            tableData.addAll(instructionMap.values());
            
            log.info("指示书信息表格数据处理完成，共{}条", tableData.size());
            
        } catch (Exception e) {
            log.error("处理指示书信息表格数据失败", e);
        }
        
        return tableData;
    }
    
    /**
     * 生成模拟的指示书XML数据
     */
    private String generateMockInstructionXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>\n" +
               "<trafficParam>\n" +
               "  <pattern><row><item>02</item></row></pattern>\n" +
               "  <pattern><row><item>02</item></row></pattern>\n" +
               "  <pattern>\n" +
               "    <row>\n" +
               "      <item>INS-001</item>\n" +
               "      <item>Rev1</item>\n" +
               "      <item>加工指示书</item>\n" +
               "      <item>GP加工指示</item>\n" +
               "      <item>1</item>\n" +
               "      <item>本文标题1</item>\n" +
               "      <item>blob_main_001</item>\n" +
               "      <item>加工条件标题1</item>\n" +
               "      <item>blob_proc_001</item>\n" +
               "    </row>\n" +
               "  </pattern>\n" +
               "  <pattern>\n" +
               "    <row>\n" +
               "      <item>INS-001</item>\n" +
               "      <item></item>\n" +
               "      <item>追加情报标题1</item>\n" +
               "      <item>blob_add_001</item>\n" +
               "    </row>\n" +
               "  </pattern>\n" +
               "  <pattern>\n" +
               "    <row>\n" +
               "      <item>INS-001</item>\n" +
               "      <item>001</item>\n" +
               "      <item>N01</item>\n" +
               "      <item>LOT-12345</item>\n" +
               "      <item>PROD-A</item>\n" +
               "      <item>Series-X</item>\n" +
               "      <item>GP中分类</item>\n" +
               "      <item></item>\n" +
               "    </row>\n" +
               "  </pattern>\n" +
               "  <result>1</result>\n" +
               "  <resultMessage/>\n" +
               "  <resultMessageId/>\n" +
               "</trafficParam>";
    }
    
    /**
     * 解析指示书XML
     * @param xmlString XML字符串
     * @return Pattern列表，每个Pattern包含多个row
     */
    private List<List<Map<String, String>>> parseInstructionXml(String xmlString) {
        List<List<Map<String, String>>> patterns = new ArrayList<>();
        if(StringUtils.isEmpty(xmlString)){
            return patterns;
        }
        
        try {
            // 简单的XML解析（使用正则表达式）
            String[] patternBlocks = xmlString.split("<pattern>");
            
            for (String block : patternBlocks) {
                if (!block.contains("<row>")) continue;
                
                List<Map<String, String>> patternRows = new ArrayList<>();
                String[] rows = block.split("<row>");
                
                for (String row : rows) {
                    if (!row.contains("<item>")) continue;
                    
                    Map<String, String> rowData = new HashMap<>();
                    String[] items = row.split("<item>");
                    
                    int itemIndex = 0;
                    for (String item : items) {
                        if (!item.contains("</item>")) continue;
                        
                        String value = item.substring(0, item.indexOf("</item>")).trim();
                        rowData.put("item_" + itemIndex, value);
                        itemIndex++;
                    }
                    
                    if (!rowData.isEmpty()) {
                        patternRows.add(rowData);
                    }
                }
                
                patterns.add(patternRows);
            }
            
        } catch (Exception e) {
            log.error("解析指示书XML失败", e);
        }
        
        return patterns;
    }
    
    /**
     * 获取item值
     */
    private String getItemValue(Map<String, String> row, int index) {
        String value = row.get("item_" + index);
        return value != null ? value.trim() : "";
    }
}


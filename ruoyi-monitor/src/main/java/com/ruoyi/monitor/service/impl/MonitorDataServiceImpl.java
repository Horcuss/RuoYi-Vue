package com.ruoyi.monitor.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.monitor.domain.MonitorConfig;
import com.ruoyi.monitor.service.IMonitorConfigService;
import com.ruoyi.monitor.service.IMonitorDataService;
import com.ruoyi.monitor.service.ISqlMergeService;
import com.ruoyi.monitor.utils.ExpressionCalculator;
import com.ruoyi.monitor.utils.JsonPathExtractor;
import com.ruoyi.monitor.utils.SymbolNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 监控数据获取Service实现类
 * 
 * @author ruoyi
 * @date 2025-01-15
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
    private com.ruoyi.monitor.mapper.ProcConditionMappingMapper procConditionMappingMapper;

    @Autowired
    private com.ruoyi.monitor.cache.XmlDataCache xmlDataCache;

    /**
     * 根据配置KEY获取监控数据
     */
    @Override
    public Map<String, Object> getMonitorData(String configKey) 
    {
        return getMonitorData(configKey, null);
    }

    /**
     * 根据配置KEY和参数获取监控数据
     */
    @Override
    public Map<String, Object> getMonitorData(String configKey, Map<String, Object> params)
    {
        Map<String, Object> resultData = new HashMap<>();

        log.info("==================== 开始获取监控数据 ====================");
        log.info("配置KEY: {}", configKey);
        log.info("请求参数: {}", params);

        try
        {
            // 1. 查询配置
            log.info("---------- 步骤1: 查询配置 ----------");
            MonitorConfig config = monitorConfigService.selectMonitorConfigByConfigKey(configKey);
            if (config == null)
            {
                log.error("监控配置不存在: {}", configKey);
                return resultData;
            }
            log.info("配置查询成功: {}", config.getConfigName());

            // 2. 解析配置JSON
            log.info("---------- 步骤2: 解析配置JSON ----------");
            JSONObject configJson = JSON.parseObject(config.getConfigJson());
            log.info("配置JSON解析成功");
            log.info("  formItems: {}", configJson.containsKey("formItems") ? "存在" : "不存在");
            log.info("  descItems: {}", configJson.containsKey("descItems") ? "存在" : "不存在");
            log.info("  remarkItems: {}", configJson.containsKey("remarkItems") ? "存在" : "不存在");
            log.info("  tableConfigs: {}", configJson.containsKey("tableConfigs") ? "存在" : "不存在");

            // 3. 获取数据库数据源的数据（原始数据）
            log.info("---------- 步骤3: 获取数据库数据 ----------");
            Map<String, Object> dbData = getDatabaseData(configJson, params);
            log.info("数据库数据获取完成，共 {} 条数据", dbData.size());

            // 4. 获取API数据源的数据
            log.info("---------- 步骤4: 获取API数据 ----------");
            Map<String, Object> apiData = getApiData(configKey, configJson, params);
            log.info("API数据获取完成，共 {} 条数据", apiData.size());

            // 5. 合并数据
            Map<String, Object> allData = new HashMap<>();
            allData.putAll(dbData);
            allData.putAll(apiData);

            // 6. 处理API数据源的计算类型（只处理API数据源）
            log.info("---------- 步骤5: 处理API数据的计算类型 ----------");
            processComputedData(configJson, allData);
            log.info("计算类型数据处理完成");

            // 7. 构建前端期望的数据格式
            log.info("---------- 步骤6: 构建前端数据格式 ----------");
            resultData = buildFrontendData(configJson, allData);
            log.info("前端数据格式构建完成");
            log.info("  descItems: {}", resultData.containsKey("descItems") ? "已构建" : "未构建");
            log.info("  remarkItems: {}", resultData.containsKey("remarkItems") ? "已构建" : "未构建");
            log.info("  tableConfigs: {}", resultData.containsKey("tableConfigs") ? "已构建" : "未构建");

            log.info("==================== 监控数据获取完成 ====================");
        }
        catch (Exception e)
        {
            log.error("==================== 获取监控数据失败 ====================", e);
            log.error("配置KEY: {}", configKey);
            log.error("请求参数: {}", params);
        }

        return resultData;
    }

    /**
     * 构建前端期望的数据格式
     * rawData 的格式: {SQL语句: 查询结果值, ...}
     */
    private Map<String, Object> buildFrontendData(JSONObject configJson, Map<String, Object> rawData)
    {
        Map<String, Object> result = new HashMap<>();

        log.info("开始构建前端数据格式，原始数据包含 {} 条", rawData.size());

        // 构建 descItems
        if (configJson.containsKey("descItems"))
        {
            log.info(">>> 构建 descItems");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> descItems = (List<Map<String, Object>>) configJson.get("descItems");
            List<Map<String, Object>> descResult = new ArrayList<>();

            for (Map<String, Object> item : descItems)
            {
                Map<String, Object> descItem = new HashMap<>();
                String label = (String) item.get("label");
                descItem.put("label", label);

                // 从原始数据中获取值
                String dataSource = (String) item.get("dataSource");
                String displayType = (String) item.get("displayType");
                Object value = "";

                if ("database".equals(dataSource))
                {
                    if ("computed".equals(displayType))
                    {
                        // 计算类型：优先使用valueKey，否则使用标准化后的表达式
                        String valueKey = (String) item.get("valueKey");
                        if (StringUtils.isNotEmpty(valueKey))
                        {
                            value = rawData.get(valueKey);
                            log.info("  {} -> {} (来源: database-computed, 变量名: {})", label, value, valueKey);
                        }
                        else
                        {
                            String expression = (String) item.get("expression");
                            String normalizedExpression = SymbolNormalizer.normalize(expression);
                            value = rawData.get(normalizedExpression);
                            log.info("  {} -> {} (来源: database-computed, 表达式: {})", label, value, normalizedExpression);
                        }
                    }
                    else
                    {
                        // 直接显示：使用SQL获取值
                        String sql = (String) item.get("expression");
                        value = rawData.get(sql);
                        log.info("  {} -> {} (来源: database-direct)", label, value);
                    }
                }
                else if ("api".equals(dataSource))
                {
                    // 优先使用valueKey（用于XML数据源），否则使用标准化表达式（用于JSON数据源）
                    String valueKey = (String) item.get("valueKey");
                    if (StringUtils.isNotEmpty(valueKey))
                    {
                        value = rawData.get(valueKey);
                        log.info("  {} -> {} (来源: api, 变量名: {})", label, value, valueKey);
                    }
                    else
                    {
                        String expression = (String) item.get("expression");
                        // 使用标准化后的表达式（如果存在）
                        String normalizedExpression = (String) item.get("normalizedExpression");
                        if (normalizedExpression == null)
                        {
                            normalizedExpression = SymbolNormalizer.normalize(expression);
                        }
                        value = rawData.get(normalizedExpression);
                        log.info("  {} -> {} (来源: api, 表达式: {})", label, value, normalizedExpression);
                    }
                }

                descItem.put("value", value != null ? value : "");

                descResult.add(descItem);
            }

            result.put("descItems", descResult);
            log.info("descItems 构建完成，共 {} 项", descResult.size());
        }

        // 构建 remarkItems
        if (configJson.containsKey("remarkItems"))
        {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> remarkItems = (List<Map<String, Object>>) configJson.get("remarkItems");
            List<Map<String, Object>> remarkResult = new ArrayList<>();

            for (Map<String, Object> item : remarkItems)
            {
                Map<String, Object> remarkItem = new HashMap<>();
                remarkItem.put("title", item.get("title"));

                // 从原始数据中获取值
                String dataSource = (String) item.get("dataSource");
                String displayType = (String) item.get("displayType");
                Object value = "";

                if ("database".equals(dataSource))
                {
                    if ("computed".equals(displayType))
                    {
                        // 计算类型：优先使用valueKey，否则使用标准化后的表达式
                        String valueKey = (String) item.get("valueKey");
                        if (StringUtils.isNotEmpty(valueKey))
                        {
                            value = rawData.get(valueKey);
                        }
                        else
                        {
                            String expression = (String) item.get("expression");
                            String normalizedExpression = SymbolNormalizer.normalize(expression);
                            value = rawData.get(normalizedExpression);
                        }
                    }
                    else
                    {
                        // 直接显示：使用SQL获取值
                        String sql = (String) item.get("expression");
                        value = rawData.get(sql);
                    }
                }
                else if ("api".equals(dataSource))
                {
                    // 优先使用valueKey（用于XML数据源），否则使用标准化表达式（用于JSON数据源）
                    String valueKey = (String) item.get("valueKey");
                    if (StringUtils.isNotEmpty(valueKey))
                    {
                        value = rawData.get(valueKey);
                    }
                    else
                    {
                        String expression = (String) item.get("expression");
                        // 使用标准化后的表达式（如果存在）
                        String normalizedExpression = (String) item.get("normalizedExpression");
                        if (normalizedExpression == null)
                        {
                            normalizedExpression = SymbolNormalizer.normalize(expression);
                        }
                        value = rawData.get(normalizedExpression);
                    }
                }

                remarkItem.put("content", value != null ? value : "");

                remarkResult.add(remarkItem);
            }

            result.put("remarkItems", remarkResult);
        }

        // 构建 tableConfigs
        if (configJson.containsKey("tableConfigs"))
        {
            log.info(">>> 构建 tableConfigs");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tableConfigs = (List<Map<String, Object>>) configJson.get("tableConfigs");
            List<Map<String, Object>> tableResult = new ArrayList<>();

            int tableIndex = 0;
            for (Map<String, Object> tableConfig : tableConfigs)
            {
                log.info("  处理表格 {}: rowHeader={}", tableIndex + 1, tableConfig.get("rowHeader"));

                Map<String, Object> table = new HashMap<>();
                // 保留 rowHeader
                table.put("rowHeader", tableConfig.get("rowHeader"));

                // 处理 rows 数组
                if (tableConfig.containsKey("rows"))
                {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> rows = (List<Map<String, Object>>) tableConfig.get("rows");
                    List<Map<String, Object>> rowsResult = new ArrayList<>();

                    log.info("    该表格包含 {} 行配置", rows.size());

                    int rowIndex = 0;
                    for (Map<String, Object> row : rows)
                    {
                        // 复制整个 row 配置（保留 rowType, projectName, unit, subRows 等）
                        Map<String, Object> rowResult = new HashMap<>(row);

                        // 获取行的数据源和表达式
                        String rowType = (String) row.get("rowType");
                        String projectName = (String) row.get("projectName");

                        if ("simple".equals(rowType))
                        {
                            // 简单行：获取值
                            String dataSource = (String) row.get("dataSource");
                            String displayType = (String) row.get("displayType");
                            Object value = "";

                            if ("database".equals(dataSource))
                            {
                                if ("computed".equals(displayType))
                                {
                                    // 计算类型：优先使用valueKey，否则使用标准化后的表达式
                                    String valueKey = (String) row.get("valueKey");
                                    if (StringUtils.isNotEmpty(valueKey))
                                    {
                                        value = rawData.get(valueKey);
                                        log.info("    行[{}] {} (简单行) -> {} (database-computed, 变量名: {})",
                                                rowIndex + 1, projectName, value, valueKey);
                                    }
                                    else
                                    {
                                        String expression = (String) row.get("expression");
                                        String normalizedExpression = SymbolNormalizer.normalize(expression);
                                        value = rawData.get(normalizedExpression);
                                        log.info("    行[{}] {} (简单行) -> {} (database-computed, 表达式: {})",
                                                rowIndex + 1, projectName, value, normalizedExpression);
                                    }
                                }
                                else
                                {
                                    // 直接显示：使用SQL获取值
                                    String sql = (String) row.get("expression");
                                    value = rawData.get(sql);
                                    log.info("    行[{}] {} (简单行) -> {} (database-direct)",
                                            rowIndex + 1, projectName, value);
                                }
                            }
                            else if ("api".equals(dataSource))
                            {
                                // 优先使用valueKey（用于XML数据源），否则使用标准化表达式（用于JSON数据源）
                                String valueKey = (String) row.get("valueKey");
                                if (StringUtils.isNotEmpty(valueKey))
                                {
                                    value = rawData.get(valueKey);
                                    log.info("    行[{}] {} (简单行) -> {} (API, 变量名: {})",
                                            rowIndex + 1, projectName, value, valueKey);
                                }
                                else
                                {
                                    String expression = (String) row.get("expression");
                                    // 使用标准化后的表达式（如果存在）
                                    String normalizedExpression = (String) row.get("normalizedExpression");
                                    if (normalizedExpression == null)
                                    {
                                        normalizedExpression = SymbolNormalizer.normalize(expression);
                                    }
                                    value = rawData.get(normalizedExpression);
                                    log.info("    行[{}] {} (简单行) -> {} (API, 表达式: {})",
                                            rowIndex + 1, projectName, value, normalizedExpression);
                                }
                            }

                            rowResult.put("value", value != null ? value : "");
                        }
                        else if ("complex".equals(rowType))
                        {
                            log.info("    行[{}] {} (复杂行)", rowIndex + 1, projectName);

                            // 复杂行：处理 subRows
                            if (row.containsKey("subRows"))
                            {
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> subRows = (List<Map<String, Object>>) row.get("subRows");
                                List<Map<String, Object>> subRowsResult = new ArrayList<>();

                                log.info("      包含 {} 个子行", subRows.size());

                                int subRowIndex = 0;
                                for (Map<String, Object> subRow : subRows)
                                {
                                    // 复制整个 subRow 配置
                                    Map<String, Object> subRowResult = new HashMap<>(subRow);

                                    // 获取子行的值
                                    String dataSource = (String) subRow.get("dataSource");
                                    String displayType = (String) subRow.get("displayType");
                                    String subName = (String) subRow.get("subName");
                                    Object value = "";

                                    if ("database".equals(dataSource))
                                    {
                                        if ("computed".equals(displayType))
                                        {
                                            // 计算类型：优先使用valueKey，否则使用标准化后的表达式
                                            String valueKey = (String) subRow.get("valueKey");
                                            if (StringUtils.isNotEmpty(valueKey))
                                            {
                                                value = rawData.get(valueKey);
                                                log.info("      子行[{}] {} -> {} (database-computed, 变量名: {})",
                                                        subRowIndex + 1, subName, value, valueKey);
                                            }
                                            else
                                            {
                                                String expression = (String) subRow.get("expression");
                                                String normalizedExpression = SymbolNormalizer.normalize(expression);
                                                value = rawData.get(normalizedExpression);
                                                log.info("      子行[{}] {} -> {} (database-computed, 表达式: {})",
                                                        subRowIndex + 1, subName, value, normalizedExpression);
                                            }
                                        }
                                        else
                                        {
                                            // 直接显示：使用SQL获取值
                                            String sql = (String) subRow.get("expression");
                                            value = rawData.get(sql);
                                            log.info("      子行[{}] {} -> {} (database-direct)",
                                                    subRowIndex + 1, subName, value);
                                        }
                                    }
                                    else if ("api".equals(dataSource))
                                    {
                                        // 优先使用valueKey（用于XML数据源），否则使用标准化表达式（用于JSON数据源）
                                        String valueKey = (String) subRow.get("valueKey");
                                        if (StringUtils.isNotEmpty(valueKey))
                                        {
                                            value = rawData.get(valueKey);
                                            log.info("      子行[{}] {} -> {} (API, 变量名: {})",
                                                    subRowIndex + 1, subName, value, valueKey);
                                        }
                                        else
                                        {
                                            String expression = (String) subRow.get("expression");
                                            // 使用标准化后的表达式（如果存在）
                                            String normalizedExpression = (String) subRow.get("normalizedExpression");
                                            if (normalizedExpression == null)
                                            {
                                                normalizedExpression = SymbolNormalizer.normalize(expression);
                                            }
                                            value = rawData.get(normalizedExpression);
                                            log.info("      子行[{}] {} -> {} (API, 表达式: {})",
                                                    subRowIndex + 1, subName, value, normalizedExpression);
                                        }
                                    }

                                    subRowResult.put("value", value != null ? value : "");
                                    subRowsResult.add(subRowResult);
                                    subRowIndex++;
                                }

                                rowResult.put("subRows", subRowsResult);
                            }
                        }

                        rowsResult.add(rowResult);
                        rowIndex++;
                    }

                    table.put("rows", rowsResult);
                }

                tableResult.add(table);
                tableIndex++;
            }

            result.put("tableConfigs", tableResult);
            log.info("tableConfigs 构建完成，共 {} 个表格", tableResult.size());
        }

        return result;
    }

    /**
     * 从API获取数据
     * 调用外部API并根据配置的expression提取数据
     * 支持JSON和XML两种响应格式
     *
     * @param configKey 配置KEY（用于缓存）
     * @param configJson 配置JSON
     * @param params 请求参数
     * @return API数据
     */
    private Map<String, Object> getApiData(String configKey, JSONObject configJson, Map<String, Object> params)
    {
        Map<String, Object> apiData = new HashMap<>();

        try
        {
            // 检查配置中是否有API数据源
            boolean hasApiDataSource = hasApiDataSource(configJson);

            if (!hasApiDataSource)
            {
                // 配置中没有API数据源，直接返回空数据
                log.debug("配置中没有API数据源，跳过API数据获取");
                return apiData;
            }

            // 1. 获取API URL
            String apiUrl = configJson.getString("apiUrl");
            if (StringUtils.isEmpty(apiUrl))
            {
                log.warn("配置中有API数据源，但未配置apiUrl");
                return apiData;
            }

            log.info("准备调用外部API: {}", apiUrl);

            // 2. 检查是否为XML API（通过procConditionGroup判断）
            boolean isXmlApi = StringUtils.isNotEmpty(configJson.getString("procConditionGroup"));

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response;

            if (isXmlApi)
            {
                // XML API - 使用POST请求
                String group = configJson.getString("procConditionGroup");
                String majorCd = configJson.getString("majorClassCd");
                String minorCd = configJson.getString("minorClassCd");

                params.put("majorClassCd", majorCd);
                params.put("minorClassCd", minorCd);

                // 先检查缓存
                String cacheKey = xmlDataCache.generateCacheKey(configKey, params);
                List<Map<String, String>> cachedXmlRows = xmlDataCache.get(cacheKey);

                if (cachedXmlRows != null)
                {
                    // 缓存命中，直接使用缓存数据
                    log.info("使用缓存的XML数据，跳过API调用");
                    apiData = extractXmlData(configJson, cachedXmlRows, params);
                    log.info("从缓存的XML数据中提取到 {} 个字段", apiData.size());
                    return apiData;
                }

                // 缓存未命中，调用API
                log.info("缓存未命中，调用XML API");

                String xmlRequest;
                if ("02".equals(group))
                {
                    xmlRequest = com.ruoyi.monitor.utils.XmlParser.buildGpXmlRequest(params);
                }
                else
                {
                    xmlRequest = com.ruoyi.monitor.utils.XmlParser.buildNonGpXmlRequest(params);
                }

                log.info("XML请求体: {}", xmlRequest);

                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_XML);
                org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(xmlRequest, headers);

                response = restTemplate.postForEntity(apiUrl, entity, String.class);

                // 解析并缓存结果
                String xmlResponse = response.getBody();
                if (xmlResponse != null && xmlResponse.trim().startsWith("<?xml"))
                {
                    List<Map<String, String>> xmlRows = com.ruoyi.monitor.utils.XmlParser.parseXmlResponse(xmlResponse);
                    xmlDataCache.put(cacheKey, xmlRows);
                    log.info("已缓存XML数据，cacheKey: {}", cacheKey);
                }
            }
            else
            {
                // JSON API - 使用GET请求（保持原有逻辑）
                response = restTemplate.getForEntity(apiUrl, String.class);
            }

            if (!response.getStatusCode().is2xxSuccessful())
            {
                log.error("API调用失败，状态码: {}", response.getStatusCode());
                return apiData;
            }

            String responseBody = response.getBody();
            log.info("API返回数据: {}", responseBody);

            // 3. 根据响应类型解析数据
            if (responseBody.trim().startsWith("<?xml"))
            {
                // XML响应
                log.info("检测到XML响应，使用XML解析逻辑");
                apiData = extractXmlData(configJson, responseBody, params);
            }
            else
            {
                // JSON响应
                log.info("检测到JSON响应，使用JSON解析逻辑");
                JSONObject apiResponse = JSON.parseObject(responseBody);
                apiData = extractApiData(configJson, apiResponse);
            }

            log.info("从API提取到 {} 个字段的数据", apiData.size());
        }
        catch (Exception e)
        {
            log.error("从API获取数据失败", e);
        }

        return apiData;
    }

    /**
     * 从API响应中提取配置的字段数据
     */
    private Map<String, Object> extractApiData(JSONObject configJson, JSONObject apiResponse)
    {
        Map<String, Object> apiData = new HashMap<>();

        // 提取descItems中的API字段
        if (configJson.containsKey("descItems"))
        {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> descItems = (List<Map<String, Object>>) configJson.get("descItems");
            for (Map<String, Object> item : descItems)
            {
                if ("api".equals(item.get("dataSource")))
                {
                    extractAndStoreApiField(item, apiResponse, apiData);
                }
            }
        }

        // 提取remarkItems中的API字段
        if (configJson.containsKey("remarkItems"))
        {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> remarkItems = (List<Map<String, Object>>) configJson.get("remarkItems");
            for (Map<String, Object> item : remarkItems)
            {
                if ("api".equals(item.get("dataSource")))
                {
                    extractAndStoreApiField(item, apiResponse, apiData);
                }
            }
        }

        // 提取tableConfigs中的API字段
        if (configJson.containsKey("tableConfigs"))
        {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tableConfigs = (List<Map<String, Object>>) configJson.get("tableConfigs");
            for (Map<String, Object> tableConfig : tableConfigs)
            {
                if (tableConfig.containsKey("rows"))
                {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> rows = (List<Map<String, Object>>) tableConfig.get("rows");
                    for (Map<String, Object> row : rows)
                    {
                        String rowType = (String) row.get("rowType");

                        if ("simple".equals(rowType))
                        {
                            if ("api".equals(row.get("dataSource")))
                            {
                                extractAndStoreApiField(row, apiResponse, apiData);
                            }
                        }
                        else if ("complex".equals(rowType))
                        {
                            if (row.containsKey("subRows"))
                            {
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> subRows = (List<Map<String, Object>>) row.get("subRows");
                                for (Map<String, Object> subRow : subRows)
                                {
                                    if ("api".equals(subRow.get("dataSource")))
                                    {
                                        extractAndStoreApiField(subRow, apiResponse, apiData);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return apiData;
    }

    /**
     * 从API响应中提取单个字段并存储
     *
     * @param item 配置项
     * @param apiResponse API响应JSON
     * @param apiData 存储提取结果的Map
     */
    private void extractAndStoreApiField(Map<String, Object> item, JSONObject apiResponse, Map<String, Object> apiData)
    {
        String expression = (String) item.get("expression");
        String displayType = (String) item.get("displayType");

        if (expression == null || expression.trim().isEmpty())
        {
            log.warn("  表达式为空，跳过");
            return;
        }

        // 标准化表达式中的符号（将中文、日文等符号转换为英文符号）
        String normalizedExpression = SymbolNormalizer.normalize(expression);

        // 如果是运算类型，需要提取表达式中的所有变量
        if ("computed".equals(displayType))
        {
            // 提取表达式中的所有变量名
            java.util.Set<String> variables = extractVariablesFromExpression(normalizedExpression);
            log.info("  运算表达式: {} 包含变量: {}", normalizedExpression, variables);

            // 提取每个变量的值
            for (String variable : variables)
            {
                if (!apiData.containsKey(variable))
                {
                    Object value = JsonPathExtractor.extractValue(apiResponse, variable);
                    apiData.put(variable, value);
                    log.info("    提取变量: {} = {}", variable, value);
                }
            }

            // 存储标准化后的表达式（用于后续计算）
            item.put("normalizedExpression", normalizedExpression);
        }
        else
        {
            // 直接显示类型，直接提取字段值
            Object value = JsonPathExtractor.extractValue(apiResponse, normalizedExpression);
            apiData.put(normalizedExpression, value);
            log.info("  提取字段: {} = {}", normalizedExpression, value);
        }
    }

    /**
     * 从表达式中提取变量名
     * 例如: "id * 2" -> ["id"]
     *      "(lat + lng) * 2" -> ["lat", "lng"]
     *      "address.geo.lat * 2" -> ["address.geo.lat"]
     *
     * @param expression 表达式
     * @return 变量名集合
     */
    private java.util.Set<String> extractVariablesFromExpression(String expression)
    {
        java.util.Set<String> variables = new java.util.HashSet<>();

        // 移除空格
        expression = expression.replaceAll("\\s+", "");

        // 分割运算符: +, -, *, /, (, )
        String[] tokens = expression.split("[+\\-*/()]");

        for (String token : tokens)
        {
            token = token.trim();
            // 如果不是空字符串，且不是数字，则认为是变量名
            if (!token.isEmpty() && !token.matches("\\d+(\\.\\d+)?"))
            {
                variables.add(token);
            }
        }

        return variables;
    }

    /**
     * 检查配置中是否有API数据源
     */
    private boolean hasApiDataSource(JSONObject configJson)
    {
        // 检查 descItems
        if (configJson.containsKey("descItems"))
        {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> descItems = (List<Map<String, Object>>) configJson.get("descItems");
            for (Map<String, Object> item : descItems)
            {
                if ("api".equals(item.get("dataSource")))
                {
                    return true;
                }
            }
        }

        // 检查 remarkItems
        if (configJson.containsKey("remarkItems"))
        {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> remarkItems = (List<Map<String, Object>>) configJson.get("remarkItems");
            for (Map<String, Object> item : remarkItems)
            {
                if ("api".equals(item.get("dataSource")))
                {
                    return true;
                }
            }
        }

        // 检查 tableConfigs
        if (configJson.containsKey("tableConfigs"))
        {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tableConfigs = (List<Map<String, Object>>) configJson.get("tableConfigs");
            for (Map<String, Object> tableConfig : tableConfigs)
            {
                if (tableConfig.containsKey("rows"))
                {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> rows = (List<Map<String, Object>>) tableConfig.get("rows");
                    for (Map<String, Object> row : rows)
                    {
                        String rowType = (String) row.get("rowType");

                        if ("simple".equals(rowType))
                        {
                            // 简单行：检查数据源
                            if ("api".equals(row.get("dataSource")))
                            {
                                return true;
                            }
                        }
                        else if ("complex".equals(rowType))
                        {
                            // 复杂行：检查 subRows
                            if (row.containsKey("subRows"))
                            {
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> subRows = (List<Map<String, Object>>) row.get("subRows");
                                for (Map<String, Object> subRow : subRows)
                                {
                                    if ("api".equals(subRow.get("dataSource")))
                                    {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * 从数据库获取数据
     */
    private Map<String, Object> getDatabaseData(JSONObject configJson, Map<String, Object> params) 
    {
        Map<String, Object> dbData = new HashMap<>();
        List<String> sqlList = new ArrayList<>();
        // 用于建立 SQL -> valueKey 的映射关系
        Map<String, String> sqlToValueKeyMap = new HashMap<>();

        try 
        {
            // 收集所有数据库数据源的SQL，并建立SQL到valueKey的映射
            collectDatabaseSqlsWithValueKey(configJson, sqlList, sqlToValueKeyMap);

            // 如果有SQL需要执行
            if (!sqlList.isEmpty()) 
            {
                // 使用SQL合并服务执行
                Map<String, Object> sqlResults = sqlMergeService.mergeAndExecute(sqlList, params);
                
                // 存储结果：处理SQL执行返回的Map（包含列名/别名）
                for (Map.Entry<String, Object> entry : sqlResults.entrySet())
                {
                    String sql = entry.getKey();
                    Object result = entry.getValue();
                    
                    log.info("  [SQL结果处理] SQL: {}", sql);
                    log.info("  [SQL结果处理] 返回类型: {}", result != null ? result.getClass().getSimpleName() : "null");
                    
                    // 如果结果是Map（包含列名），提取列名和值
                    if (result instanceof Map)
                    {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> columnMap = (Map<String, Object>) result;
                        
                        log.info("  [SQL结果处理] 包含 {} 个列: {}", columnMap.size(), columnMap.keySet());
                        
                        // 1. 存储每个列名（别名）和对应的值
                        for (Map.Entry<String, Object> columnEntry : columnMap.entrySet())
                        {
                            String columnName = columnEntry.getKey();
                            Object columnValue = columnEntry.getValue();
                            
                            dbData.put(columnName, columnValue);
                            log.info("    [列名映射] {} -> {}", columnName, columnValue);
                        }
                        
                        // 2. 如果只有一列，也用SQL本身作为key存储值（保持向后兼容）
                        if (columnMap.size() == 1)
                        {
                            Object singleValue = columnMap.values().iterator().next();
                            dbData.put(sql, singleValue);
                            log.info("    [SQL映射] SQL -> {} (向后兼容)", singleValue);
                        }
                        else
                        {
                            // 多列结果，用SQL作为key存储整个Map
                            dbData.put(sql, columnMap);
                            log.info("    [SQL映射] SQL -> Map({}列)", columnMap.size());
                        }
                        
                        // 3. 如果有对应的valueKey，用valueKey作为key存储
                        if (sqlToValueKeyMap.containsKey(sql))
                        {
                            String valueKey = sqlToValueKeyMap.get(sql);
                            if (StringUtils.isNotEmpty(valueKey))
                            {
                                // 检查列名中是否有与valueKey匹配的列
                                if (columnMap.containsKey(valueKey))
                                {
                                    // 如果SQL结果中有同名列，使用该列的值
                                    Object aliasValue = columnMap.get(valueKey);
                                    dbData.put(valueKey, aliasValue);
                                    log.info("    [变量映射-别名] {} -> {} (来自列名)", valueKey, aliasValue);
                                }
                                else if (columnMap.size() == 1)
                                {
                                    // 如果只有一列且没有匹配的列名，使用该列的值
                                    Object singleValue = columnMap.values().iterator().next();
                                    dbData.put(valueKey, singleValue);
                                    log.info("    [变量映射-单列] {} -> {}", valueKey, singleValue);
                                }
                            }
                        }
                    }
                    else
                    {
                        // 兼容旧版本：如果返回的不是Map，直接存储值
                        dbData.put(sql, result);
                        
                        if (sqlToValueKeyMap.containsKey(sql))
                        {
                            String valueKey = sqlToValueKeyMap.get(sql);
                            if (StringUtils.isNotEmpty(valueKey))
                            {
                                dbData.put(valueKey, result);
                                log.info("  [变量映射-兼容] {} -> {}", valueKey, result);
                            }
                        }
                    }
                }
            }
        } 
        catch (Exception e) 
        {
            log.error("从数据库获取数据失败", e);
        }

        return dbData;
    }

    /**
     * 收集配置中所有数据库数据源的SQL，并建立SQL到valueKey的映射
     */
    private void collectDatabaseSqlsWithValueKey(JSONObject configJson, List<String> sqlList, Map<String, String> sqlToValueKeyMap)
    {
        // 处理descItems
        if (configJson.containsKey("descItems"))
        {
            List<Map<String, Object>> descItems = (List<Map<String, Object>>) configJson.get("descItems");
            for (Map<String, Object> item : descItems)
            {
                if ("database".equals(item.get("dataSource")) && "direct".equals(item.get("displayType")))
                {
                    String sql = (String) item.get("expression");
                    String valueKey = (String) item.get("valueKey");
                    if (StringUtils.isNotEmpty(sql))
                    {
                        sqlList.add(sql);
                        if (StringUtils.isNotEmpty(valueKey))
                        {
                            sqlToValueKeyMap.put(sql, valueKey);
                        }
                    }
                }
            }
        }

        // 处理remarkItems
        if (configJson.containsKey("remarkItems"))
        {
            List<Map<String, Object>> remarkItems = (List<Map<String, Object>>) configJson.get("remarkItems");
            for (Map<String, Object> item : remarkItems)
            {
                if ("database".equals(item.get("dataSource")) && "direct".equals(item.get("displayType")))
                {
                    String sql = (String) item.get("expression");
                    String valueKey = (String) item.get("valueKey");
                    if (StringUtils.isNotEmpty(sql))
                    {
                        sqlList.add(sql);
                        if (StringUtils.isNotEmpty(valueKey))
                        {
                            sqlToValueKeyMap.put(sql, valueKey);
                        }
                    }
                }
            }
        }

        // 处理tableConfigs
        if (configJson.containsKey("tableConfigs"))
        {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tableConfigs = (List<Map<String, Object>>) configJson.get("tableConfigs");
            for (Map<String, Object> tableConfig : tableConfigs)
            {
                if (tableConfig.containsKey("rows"))
                {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> rows = (List<Map<String, Object>>) tableConfig.get("rows");
                    for (Map<String, Object> row : rows)
                    {
                        String rowType = (String) row.get("rowType");

                        if ("simple".equals(rowType))
                        {
                            // 简单行：直接获取SQL
                            if ("database".equals(row.get("dataSource")) && "direct".equals(row.get("displayType")))
                            {
                                String sql = (String) row.get("expression");
                                String valueKey = (String) row.get("valueKey");
                                if (StringUtils.isNotEmpty(sql))
                                {
                                    sqlList.add(sql);
                                    if (StringUtils.isNotEmpty(valueKey))
                                    {
                                        sqlToValueKeyMap.put(sql, valueKey);
                                    }
                                }
                            }
                        }
                        else if ("complex".equals(rowType))
                        {
                            // 复杂行：遍历 subRows
                            if (row.containsKey("subRows"))
                            {
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> subRows = (List<Map<String, Object>>) row.get("subRows");
                                for (Map<String, Object> subRow : subRows)
                                {
                                    if ("database".equals(subRow.get("dataSource")) && "direct".equals(subRow.get("displayType")))
                                    {
                                        String sql = (String) subRow.get("expression");
                                        String valueKey = (String) subRow.get("valueKey");
                                        if (StringUtils.isNotEmpty(sql))
                                        {
                                            sqlList.add(sql);
                                            if (StringUtils.isNotEmpty(valueKey))
                                            {
                                                sqlToValueKeyMap.put(sql, valueKey);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 收集配置中所有数据库数据源的SQL（旧方法，保留兼容性）
     */
    private void collectDatabaseSqls(JSONObject configJson, List<String> sqlList)
    {
        // 处理descItems
        if (configJson.containsKey("descItems"))
        {
            List<Map<String, Object>> descItems = (List<Map<String, Object>>) configJson.get("descItems");
            for (Map<String, Object> item : descItems)
            {
                if ("database".equals(item.get("dataSource")))
                {
                    String sql = (String) item.get("expression");
                    if (StringUtils.isNotEmpty(sql))
                    {
                        sqlList.add(sql);
                    }
                }
            }
        }

        // 处理remarkItems
        if (configJson.containsKey("remarkItems"))
        {
            List<Map<String, Object>> remarkItems = (List<Map<String, Object>>) configJson.get("remarkItems");
            for (Map<String, Object> item : remarkItems)
            {
                if ("database".equals(item.get("dataSource")))
                {
                    String sql = (String) item.get("expression");
                    if (StringUtils.isNotEmpty(sql))
                    {
                        sqlList.add(sql);
                    }
                }
            }
        }

        // 处理tableConfigs
        if (configJson.containsKey("tableConfigs"))
        {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tableConfigs = (List<Map<String, Object>>) configJson.get("tableConfigs");
            for (Map<String, Object> tableConfig : tableConfigs)
            {
                if (tableConfig.containsKey("rows"))
                {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> rows = (List<Map<String, Object>>) tableConfig.get("rows");
                    for (Map<String, Object> row : rows)
                    {
                        String rowType = (String) row.get("rowType");

                        if ("simple".equals(rowType))
                        {
                            // 简单行：直接获取SQL
                            if ("database".equals(row.get("dataSource")))
                            {
                                String sql = (String) row.get("expression");
                                if (StringUtils.isNotEmpty(sql))
                                {
                                    sqlList.add(sql);
                                }
                            }
                        }
                        else if ("complex".equals(rowType))
                        {
                            // 复杂行：遍历 subRows
                            if (row.containsKey("subRows"))
                            {
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> subRows = (List<Map<String, Object>>) row.get("subRows");
                                for (Map<String, Object> subRow : subRows)
                                {
                                    if ("database".equals(subRow.get("dataSource")))
                                    {
                                        String sql = (String) subRow.get("expression");
                                        if (StringUtils.isNotEmpty(sql))
                                        {
                                            sqlList.add(sql);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理计算类型的数据（支持API和数据库数据源）
     * 对于数据库数据源，通过valueKey引用其他字段的值进行计算
     */
    private void processComputedData(JSONObject configJson, Map<String, Object> resultData)
    {
        log.info("开始处理计算类型数据（支持API和数据库数据源）");
        int computedCount = 0;

        // 处理descItems中的computed类型（支持API和数据库数据源）
        if (configJson.containsKey("descItems"))
        {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> descItems = (List<Map<String, Object>>) configJson.get("descItems");
            for (Map<String, Object> item : descItems)
            {
                String dataSource = (String) item.get("dataSource");
                String displayType = (String) item.get("displayType");

                // 处理所有数据源的computed类型
                if ("computed".equals(displayType))
                {
                    String expression = (String) item.get("expression");
                    // 使用标准化后的表达式（如果存在）
                    String normalizedExpression = (String) item.get("normalizedExpression");
                    if (normalizedExpression == null)
                    {
                        normalizedExpression = SymbolNormalizer.normalize(expression);
                    }

                    Double computed = calculateExpression(normalizedExpression, resultData);

                    if (computed != null)
                    {
                        // 使用标准化后的表达式作为key存储计算结果
                        resultData.put(normalizedExpression, computed);
                        
                        // 如果有valueKey，也用valueKey作为key存储（便于链式计算引用）
                        String valueKey = (String) item.get("valueKey");
                        if (StringUtils.isNotEmpty(valueKey))
                        {
                            resultData.put(valueKey, computed);
                            log.info("  计算 ({}): {} = {} (表达式: {}, 变量名: {})", dataSource, normalizedExpression, computed, normalizedExpression, valueKey);
                        }
                        else
                        {
                            log.info("  计算 ({}): {} = {} (表达式: {})", dataSource, normalizedExpression, computed, normalizedExpression);
                        }
                        computedCount++;
                    }
                }
            }
        }

        // 处理remarkItems中的computed类型（支持API和数据库数据源）
        if (configJson.containsKey("remarkItems"))
        {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> remarkItems = (List<Map<String, Object>>) configJson.get("remarkItems");
            for (Map<String, Object> item : remarkItems)
            {
                String dataSource = (String) item.get("dataSource");
                String displayType = (String) item.get("displayType");

                if ("computed".equals(displayType))
                {
                    String expression = (String) item.get("expression");
                    // 使用标准化后的表达式（如果存在）
                    String normalizedExpression = (String) item.get("normalizedExpression");
                    if (normalizedExpression == null)
                    {
                        normalizedExpression = SymbolNormalizer.normalize(expression);
                    }

                    Double computed = calculateExpression(normalizedExpression, resultData);

                    if (computed != null)
                    {
                        resultData.put(normalizedExpression, computed);
                        
                        // 如果有valueKey，也用valueKey作为key存储（便于链式计算引用）
                        String valueKey = (String) item.get("valueKey");
                        if (StringUtils.isNotEmpty(valueKey))
                        {
                            resultData.put(valueKey, computed);
                            log.info("  计算 ({}): {} = {} (表达式: {}, 变量名: {})", dataSource, normalizedExpression, computed, normalizedExpression, valueKey);
                        }
                        else
                        {
                            log.info("  计算 ({}): {} = {} (表达式: {})", dataSource, normalizedExpression, computed, normalizedExpression);
                        }
                        computedCount++;
                    }
                }
            }
        }

        // 处理tableConfigs中的computed类型（支持API和数据库数据源）
        if (configJson.containsKey("tableConfigs"))
        {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tableConfigs = (List<Map<String, Object>>) configJson.get("tableConfigs");
            for (Map<String, Object> tableConfig : tableConfigs)
            {
                if (tableConfig.containsKey("rows"))
                {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> rows = (List<Map<String, Object>>) tableConfig.get("rows");
                    for (Map<String, Object> row : rows)
                    {
                        String rowType = (String) row.get("rowType");

                        if ("simple".equals(rowType))
                        {
                            String dataSource = (String) row.get("dataSource");
                            String displayType = (String) row.get("displayType");

                            if ("computed".equals(displayType))
                            {
                                String expression = (String) row.get("expression");
                                // 使用标准化后的表达式（如果存在）
                                String normalizedExpression = (String) row.get("normalizedExpression");
                                if (normalizedExpression == null)
                                {
                                    normalizedExpression = SymbolNormalizer.normalize(expression);
                                }

                                Double computed = calculateExpression(normalizedExpression, resultData);

                                if (computed != null)
                                {
                                    resultData.put(normalizedExpression, computed);
                                    
                                    // 如果有valueKey，也用valueKey作为key存储（便于链式计算引用）
                                    String valueKey = (String) row.get("valueKey");
                                    if (StringUtils.isNotEmpty(valueKey))
                                    {
                                        resultData.put(valueKey, computed);
                                        log.info("  计算 ({}): {} = {} (表达式: {}, 变量名: {})", dataSource, normalizedExpression, computed, normalizedExpression, valueKey);
                                    }
                                    else
                                    {
                                        log.info("  计算 ({}): {} = {} (表达式: {})", dataSource, normalizedExpression, computed, normalizedExpression);
                                    }
                                    computedCount++;
                                }
                            }
                        }
                        else if ("complex".equals(rowType))
                        {
                            if (row.containsKey("subRows"))
                            {
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> subRows = (List<Map<String, Object>>) row.get("subRows");
                                for (Map<String, Object> subRow : subRows)
                                {
                                    String dataSource = (String) subRow.get("dataSource");
                                    String displayType = (String) subRow.get("displayType");

                                    if ("computed".equals(displayType))
                                    {
                                        String expression = (String) subRow.get("expression");
                                        // 使用标准化后的表达式（如果存在）
                                        String normalizedExpression = (String) subRow.get("normalizedExpression");
                                        if (normalizedExpression == null)
                                        {
                                            normalizedExpression = SymbolNormalizer.normalize(expression);
                                        }

                                        Double computed = calculateExpression(normalizedExpression, resultData);

                                        if (computed != null)
                                        {
                                            resultData.put(normalizedExpression, computed);
                                            
                                            // 如果有valueKey，也用valueKey作为key存储（便于链式计算引用）
                                            String valueKey = (String) subRow.get("valueKey");
                                            if (StringUtils.isNotEmpty(valueKey))
                                            {
                                                resultData.put(valueKey, computed);
                                                log.info("  计算 ({}): {} = {} (表达式: {}, 变量名: {})", dataSource, normalizedExpression, computed, normalizedExpression, valueKey);
                                            }
                                            else
                                            {
                                                log.info("  计算 ({}): {} = {} (表达式: {})", dataSource, normalizedExpression, computed, normalizedExpression);
                                            }
                                            computedCount++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        log.info("计算类型数据处理完成，共处理 {} 个计算字段", computedCount);
    }

    /**
     * 计算表达式的值
     * 从resultData中获取变量值，然后计算表达式
     *
     * @param expression 表达式，如 "id * 2", "(lat + lng) * 2"
     * @param resultData 包含变量值的数据Map
     * @return 计算结果
     */
    private Double calculateExpression(String expression, Map<String, Object> resultData)
    {
        try
        {
            // 提取表达式中的所有变量
            java.util.Set<String> variables = extractVariablesFromExpression(expression);
            
            // 按变量名长度降序排序，避免短变量名先被替换导致长变量名无法匹配
            // 例如：先替换 maxAge，再替换 age
            List<String> sortedVariables = new ArrayList<>(variables);
            sortedVariables.sort((a, b) -> Integer.compare(b.length(), a.length()));

            // 替换表达式中的变量为实际值
            String replacedExpression = expression;
            for (String variable : sortedVariables)
            {
                if (resultData.containsKey(variable))
                {
                    Object value = resultData.get(variable);
                    Double numValue = convertToDouble(value);
                    if (numValue != null)
                    {
                        // 替换变量名为数值
                        // 对于带点号的变量名（如 address.geo.lat），不能使用 \b 边界匹配
                        // 因为 \b 会在点号处中断，导致匹配失败
                        // 解决方案：使用前后文检查，确保是完整变量名
                        String escapedVariable = variable.replace(".", "\\.").replace("$", "\\$");
                        
                        // 使用环视断言：前面是运算符/括号/开头，后面是运算符/括号/结尾
                        // (?<![\\w.]) 表示前面不是字母/数字/下划线/点号
                        // (?![\\w.]) 表示后面不是字母/数字/下划线/点号
                        replacedExpression = replacedExpression.replaceAll(
                            "(?<![\\w])"+  escapedVariable + "(?![\\w])", 
                            numValue.toString()
                        );
                        log.debug("    替换变量: {} = {}", variable, numValue);
                    }
                    else
                    {
                        log.warn("    变量 {} 的值 {} 无法转换为数字", variable, value);
                        return null;
                    }
                }
                else
                {
                    log.warn("    变量 {} 在数据中不存在", variable);
                    return null;
                }
            }

            log.debug("    替换后的表达式: {}", replacedExpression);

            // 使用ExpressionCalculator计算
            return ExpressionCalculator.calculate(replacedExpression);
        }
        catch (Exception e)
        {
            log.error("    计算表达式失败: {}", expression, e);
            return null;
        }
    }

    /**
     * 将对象转换为Double
     */
    private Double convertToDouble(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String)
        {
            try
            {
                return Double.parseDouble((String) value);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取数据映射的key
     * 优先使用valueKey，其次使用expression
     *
     * @param item 配置项
     * @return 数据key
     */
    private String getDataKey(Map<String, Object> item)
    {
        // 优先使用valueKey（用于复杂label场景，如label是数组的情况）
        if (item.containsKey("valueKey") && StringUtils.isNotEmpty((String) item.get("valueKey")))
        {
            return (String) item.get("valueKey");
        }
        // 其次使用expression
        return (String) item.get("expression");
    }

    /**
     * 从XML响应中提取数据
     *
     * @param configJson 配置JSON
     * @param xmlResponse XML响应字符串
     * @param params 请求参数（包含下拉框选择值）
     * @return 提取的数据Map
     */
    /**
     * 从XML响应中提取数据
     */
    private Map<String, Object> extractXmlData(JSONObject configJson, String xmlResponse, Map<String, Object> params) throws Exception
    {
        log.info("开始从XML响应中提取数据");

        // 1. 解析XML
        List<Map<String, String>> xmlRows = com.ruoyi.monitor.utils.XmlParser.parseXmlResponse(xmlResponse);
        log.info("解析XML，共 {} 个row", xmlRows.size());

        // 2. 调用重载方法处理
        return extractXmlData(configJson, xmlRows, params);
    }

    /**
     * 从XML行数据中提取数据（重载方法，用于缓存优化）
     */
    private Map<String, Object> extractXmlData(JSONObject configJson, List<Map<String, String>> xmlRows, Map<String, Object> params) throws Exception
    {
        Map<String, Object> result = new HashMap<>();

        log.info("从XML行数据中提取配置项，共 {} 个row", xmlRows.size());

        // 1. 获取基础配置
        String group = configJson.getString("procConditionGroup");
        String majorCd = configJson.getString("majorClassCd");
        String minorCd = configJson.getString("minorClassCd");
        boolean isGpProject = "02".equals(group);

        log.info("基础配置: group={}, majorCd={}, minorCd={}, isGpProject={}", group, majorCd, minorCd, isGpProject);

        // 0. 清理XML rows（非GP项目需要移除分隔符）
        List<Map<String, String>> cleanedXmlRows = new ArrayList<>();
        for (Map<String, String> row : xmlRows)
        {
            cleanedXmlRows.add(cleanXmlRowIfNeeded(row, isGpProject));
        }

        // 2. 根据下拉框选择值确定对应的rows（按cd分组）
        Map<String, Map<String, String>> selectedRows = findSelectedRows(cleanedXmlRows, params, configJson, group);

        if (selectedRows.isEmpty())
        {
            log.warn("未找到匹配的XML rows");
            return result;
        }

        log.info("找到匹配的rows，共 {} 个cd", selectedRows.size());

        // 4. 从选中的rows中提取所有配置项的值
        if (configJson.containsKey("descItems"))
        {
            extractXmlItems((List<Map<String, Object>>) configJson.get("descItems"),
                    selectedRows, group, majorCd, minorCd, result);
        }

        if (configJson.containsKey("remarkItems"))
        {
            extractXmlItems((List<Map<String, Object>>) configJson.get("remarkItems"),
                    selectedRows, group, majorCd, minorCd, result);
        }

        if (configJson.containsKey("tableConfigs"))
        {
            extractXmlTableItems(configJson, selectedRows, group, majorCd, minorCd, result);
        }

        log.info("从XML提取数据完成，共 {} 个字段", result.size());

        return result;
    }

    /**
     * 根据下拉框选择值找到对应的XML rows（按加工条件種cd分组）
     *
     * @param xmlRows 所有XML行
     * @param params 请求参数（包含用户输入和下拉框选择值）
     * @param configJson 配置JSON
     * @param group 加工条件group
     * @return 匹配的rows，key为加工条件種cd，value为匹配的row
     */
    private Map<String, Map<String, String>> findSelectedRows(
            List<Map<String, String>> xmlRows,
            Map<String, Object> params,
            JSONObject configJson,
            String group)
    {
        Map<String, Map<String, String>> result = new HashMap<>();
        int cdIndex = getCdIndex(group);
        int dataStartIndex = getDataStartIndex(group);

        log.info("findSelectedRows: group={}, cdIndex={}, dataStartIndex={}", group, cdIndex, dataStartIndex);

        try
        {
            // 1. 收集所有需要的cd（包括下拉框的cd和descItems/remarkItems/tableConfigs中的cd）
            java.util.Set<String> allRequiredCds = new java.util.HashSet<>();
            collectAllRequiredCds(configJson, allRequiredCds);

            log.info("配置中需要的所有cd: {}", allRequiredCds);

            // 2. 获取下拉框配置信息
            @SuppressWarnings("unchecked")
            Map<String, Object> selectConfigs = (Map<String, Object>) params.get("selectConfigs");

            if (selectConfigs == null || selectConfigs.isEmpty())
            {
                log.warn("未找到下拉框配置信息");
                // 没有下拉框，为每个需要的cd找第一个匹配的row
                for (String cd : allRequiredCds)
                {
                    Map<String, String> firstRow = findFirstRowByTypeCode(xmlRows, cd, group);
                    if (firstRow != null)
                    {
                        result.put(cd, firstRow);
                        log.info("cd={} 没有下拉框，使用第一个匹配的row", cd);
                    }
                }
                return result;
            }

            // 3. 按加工条件種cd分组下拉框
            Map<String, Map<String, Object>> groupedByTypeCode = new HashMap<>();
            for (Map.Entry<String, Object> entry : selectConfigs.entrySet())
            {
                String selectProp = entry.getKey();
                Object selectValue = params.get(selectProp);

                if (selectValue == null)
                {
                    continue;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> selectConfig = (Map<String, Object>) entry.getValue();
                String expression = (String) selectConfig.get("expression");
                String[] parts = expression.split(";");
                String typeCode = parts[0];  // 加工条件種cd

                if (!groupedByTypeCode.containsKey(typeCode))
                {
                    groupedByTypeCode.put(typeCode, new HashMap<>());
                }
                groupedByTypeCode.get(typeCode).put(selectProp, selectConfig);

                log.info("下拉框分组: cd={}, prop={}, value={}", typeCode, selectProp, selectValue);
            }

            log.info("下拉框按cd分组完成，共 {} 个cd", groupedByTypeCode.size());

            // 记录有下拉框的cd（用于后续区分是"没找到"还是"没下拉框"）
            java.util.Set<String> cdsWithSelect = new java.util.HashSet<>(groupedByTypeCode.keySet());

            // 4. 对每个cd，找到同时满足所有下拉框的row
            for (Map.Entry<String, Map<String, Object>> cdEntry : groupedByTypeCode.entrySet())
            {
                String targetTypeCode = cdEntry.getKey();
                Map<String, Object> cdSelectConfigs = cdEntry.getValue();

                log.info("开始匹配cd={}, 该cd下有 {} 个下拉框", targetTypeCode, cdSelectConfigs.size());

                // 遍历所有row，找到同时满足这个cd下所有下拉框的row
                for (Map<String, String> row : xmlRows)
                {
                    String rowTypeCode = row.get("item_" + cdIndex);

                    if (!targetTypeCode.equals(rowTypeCode))
                    {
                        continue;  // cd不匹配，跳过
                    }

                    // 检查这个row是否满足当前cd的所有下拉框
                    boolean allMatch = true;
                    StringBuilder matchLog = new StringBuilder();

                    for (Map.Entry<String, Object> selectEntry : cdSelectConfigs.entrySet())
                    {
                        String selectProp = selectEntry.getKey();
                        Object selectValue = params.get(selectProp);

                        @SuppressWarnings("unchecked")
                        Map<String, Object> selectConfig = (Map<String, Object>) selectEntry.getValue();
                        String expression = (String) selectConfig.get("expression");
                        String[] parts = expression.split(";");
                        int selectSeq = Integer.parseInt(parts[2]);

                        String rowValue = row.get("item_" + (cdIndex + selectSeq));

                        matchLog.append(String.format(" [%s: 期望=%s, 实际=%s]",
                            selectProp, selectValue, rowValue));

                        if (!selectValue.toString().equals(rowValue))
                        {
                            allMatch = false;
                            break;
                        }
                    }

                    if (allMatch)
                    {
                        log.info("找到匹配的row: cd={}, 匹配条件:{}", targetTypeCode, matchLog);
                        result.put(targetTypeCode, row);
                        break;  // 找到了，处理下一个cd
                    }
                }

                if (!result.containsKey(targetTypeCode))
                {
                    log.warn("未找到匹配的row: cd={}, 用户选择的下拉框组合不存在", targetTypeCode);
                }
            }

            log.info("根据下拉框匹配到 {} 个cd的row", result.size());

            // 5. 补充没有下拉框但配置中需要的cd（注意：有下拉框但没匹配到的cd不在这里处理）
            for (String cd : allRequiredCds)
            {
                if (!result.containsKey(cd))
                {
                    // 检查这个cd是否有下拉框
                    if (cdsWithSelect.contains(cd))
                    {
                        // 有下拉框但没匹配到，不应该fallback，跳过
                        log.warn("cd={} 有下拉框但未找到匹配的row，跳过数据提取", cd);
                    }
                    else
                    {
                        // 没有下拉框，使用第一个匹配的row
                        log.info("cd={} 在配置中需要，但没有对应的下拉框，查找第一个匹配的row", cd);
                        Map<String, String> firstRow = findFirstRowByTypeCode(xmlRows, cd, group);
                        if (firstRow != null)
                        {
                            result.put(cd, firstRow);
                            log.info("cd={} 使用第一个匹配的row", cd);
                        }
                        else
                        {
                            log.warn("cd={} 在XML中找不到匹配的row", cd);
                        }
                    }
                }
            }

            log.info("共找到 {} 个cd的匹配row", result.size());
        }
        catch (Exception e)
        {
            log.error("匹配XML rows失败", e);
        }

        return result;
    }

    /**
     * 收集配置中所有需要的加工条件種cd
     */
    private void collectAllRequiredCds(JSONObject configJson, java.util.Set<String> cds)
    {
        // 从descItems收集
        if (configJson.containsKey("descItems"))
        {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> descItems = (List<Map<String, Object>>) configJson.get("descItems");
            for (Map<String, Object> item : descItems)
            {
                if ("api".equals(item.get("dataSource")))
                {
                    String expression = (String) item.get("expression");
                    if (expression != null && expression.contains(";"))
                    {
                        String cd = expression.split(";")[0];
                        cds.add(cd);
                    }
                }
            }
        }

        // 从remarkItems收集
        if (configJson.containsKey("remarkItems"))
        {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> remarkItems = (List<Map<String, Object>>) configJson.get("remarkItems");
            for (Map<String, Object> item : remarkItems)
            {
                if ("api".equals(item.get("dataSource")))
                {
                    String expression = (String) item.get("expression");
                    if (expression != null && expression.contains(";"))
                    {
                        String cd = expression.split(";")[0];
                        cds.add(cd);
                    }
                }
            }
        }

        // 从tableConfigs收集
        if (configJson.containsKey("tableConfigs"))
        {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tableConfigs = (List<Map<String, Object>>) configJson.get("tableConfigs");
            for (Map<String, Object> tableConfig : tableConfigs)
            {
                if (tableConfig.containsKey("rows"))
                {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> rows = (List<Map<String, Object>>) tableConfig.get("rows");
                    for (Map<String, Object> row : rows)
                    {
                        if ("simple".equals(row.get("rowType")) && "api".equals(row.get("dataSource")))
                        {
                            String expression = (String) row.get("expression");
                            if (expression != null && expression.contains(";"))
                            {
                                String cd = expression.split(";")[0];
                                cds.add(cd);
                            }
                        }
                        else if ("complex".equals(row.get("rowType")) && row.containsKey("subRows"))
                        {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> subRows = (List<Map<String, Object>>) row.get("subRows");
                            for (Map<String, Object> subRow : subRows)
                            {
                                if ("api".equals(subRow.get("dataSource")))
                                {
                                    String expression = (String) subRow.get("expression");
                                    if (expression != null && expression.contains(";"))
                                    {
                                        String cd = expression.split(";")[0];
                                        cds.add(cd);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 根据加工条件種cd找到第一个匹配的row
     */
    private Map<String, String> findFirstRowByTypeCode(List<Map<String, String>> xmlRows, String typeCode, String group)
    {
        int cdIndex = getCdIndex(group);
        for (Map<String, String> row : xmlRows)
        {
            String rowTypeCode = row.get("item_" + cdIndex);
            if (typeCode.equals(rowTypeCode))
            {
                return row;
            }
        }
        return null;
    }

    /**
     * 从XML rows中提取配置项的值（支持多个不同cd的row）
     *
     * @param items 配置项列表
     * @param xmlRows XML行数据（按cd分组）
     * @param group 加工条件group
     * @param majorCd 大分类cd
     * @param minorCd 中分类cd
     * @param result 结果Map
     */
    private void extractXmlItems(
            List<Map<String, Object>> items,
            Map<String, Map<String, String>> xmlRows,
            String group, String majorCd, String minorCd,
            Map<String, Object> result)
    {
        int cdIndex = getCdIndex(group);

        for (Map<String, Object> item : items)
        {
            if (!"api".equals(item.get("dataSource")))
            {
                continue;
            }

            String expression = (String) item.get("expression");
            String valueKey = (String) item.get("valueKey");

            // 解析expression: "006;1;1"
            String[] parts = expression.split(";");
            if (parts.length != 3)
            {
                log.warn("表达式格式错误: {}", expression);
                continue;
            }

            String typeCd = parts[0];  // 加工条件種cd
            String multiKey = parts[1];
            Integer seq = Integer.parseInt(parts[2]);

            // 1. 根据typeCd获取对应的row
            Map<String, String> xmlRow = xmlRows.get(typeCd);
            if (xmlRow == null)
            {
                // 尝试使用DEFAULT（用于无下拉框的情况）
                xmlRow = xmlRows.get("DEFAULT");
                if (xmlRow == null)
                {
                    log.warn("未找到cd={}的匹配row，跳过字段: {}", typeCd, valueKey);
                    continue;
                }
            }

            // 2. 查询映射表获取加工条件名称
            String conditionName = procConditionMappingMapper.findConditionName(
                    group, majorCd, minorCd, typeCd, multiKey, seq
            );

            // 3. 从XML row中提取值
            // GP项目: cdIndex=5, 数据从item_6开始; 非GP项目: cdIndex=3, 数据从item_4开始
            String value = xmlRow.get("item_" + (cdIndex + seq));

            // 处理空值和NoData
            value = normalizeXmlValue(value);

            log.info("提取字段: cd={}, {} -> {} (加工条件名称: {})", typeCd, valueKey, value, conditionName);

            // 4. 存储（使用valueKey）
            if (StringUtils.isNotEmpty(valueKey))
            {
                result.put(valueKey, value);
            }

            // 同时存储加工条件名称（用于前端显示label）
            if (StringUtils.isNotEmpty(conditionName))
            {
                result.put(valueKey + "_name", conditionName);
            }
        }
    }

    /**
     * 从XML rows中提取表格项的值（支持多个不同cd的row）
     */
    private void extractXmlTableItems(
            JSONObject configJson,
            Map<String, Map<String, String>> xmlRows,
            String group, String majorCd, String minorCd,
            Map<String, Object> result)
    {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tableConfigs = (List<Map<String, Object>>) configJson.get("tableConfigs");

        for (Map<String, Object> tableConfig : tableConfigs)
        {
            if (!tableConfig.containsKey("rows"))
            {
                continue;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) tableConfig.get("rows");

            for (Map<String, Object> row : rows)
            {
                String rowType = (String) row.get("rowType");

                if ("simple".equals(rowType))
                {
                    // 简单行
                    if ("api".equals(row.get("dataSource")))
                    {
                        extractSingleXmlItem(row, xmlRows, group, majorCd, minorCd, result);
                    }
                }
                else if ("complex".equals(rowType))
                {
                    // 复杂行：处理 subRows
                    if (row.containsKey("subRows"))
                    {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> subRows = (List<Map<String, Object>>) row.get("subRows");

                        for (Map<String, Object> subRow : subRows)
                        {
                            if ("api".equals(subRow.get("dataSource")))
                            {
                                extractSingleXmlItem(subRow, xmlRows, group, majorCd, minorCd, result);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 提取单个XML项（支持多个不同cd的row）
     */
    private void extractSingleXmlItem(
            Map<String, Object> item,
            Map<String, Map<String, String>> xmlRows,
            String group, String majorCd, String minorCd,
            Map<String, Object> result)
    {
        int cdIndex = getCdIndex(group);

        String expression = (String) item.get("expression");
        String valueKey = (String) item.get("valueKey");

        String[] parts = expression.split(";");
        if (parts.length != 3)
        {
            log.warn("表达式格式错误: {}", expression);
            return;
        }

        String typeCd = parts[0];  // 加工条件種cd
        String multiKey = parts[1];
        Integer seq = Integer.parseInt(parts[2]);

        // 1. 根据typeCd获取对应的row
        Map<String, String> xmlRow = xmlRows.get(typeCd);
        if (xmlRow == null)
        {
            // 尝试使用DEFAULT（用于无下拉框的情况）
            xmlRow = xmlRows.get("DEFAULT");
            if (xmlRow == null)
            {
                log.warn("未找到cd={}的匹配row，跳过表格字段: {}", typeCd, valueKey);
                return;
            }
        }

        // 2. 查询映射表
        String conditionName = procConditionMappingMapper.findConditionName(
                group, majorCd, minorCd, typeCd, multiKey, seq
        );

        // 3. 从XML row中提取值
        // GP项目: cdIndex=5, 数据从item_6开始; 非GP项目: cdIndex=3, 数据从item_4开始
        String value = xmlRow.get("item_" + (cdIndex + seq));

        // 处理空值和NoData
        value = normalizeXmlValue(value);

        log.info("提取表格字段: cd={}, {} -> {} (加工条件名称: {})", typeCd, valueKey, value, conditionName);

        // 4. 存储
        if (StringUtils.isNotEmpty(valueKey))
        {
            result.put(valueKey, value);
        }

        if (StringUtils.isNotEmpty(conditionName))
        {
            result.put(valueKey + "_name", conditionName);
        }
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
            boolean isGpProject = "02".equals(group);
            int cdIndex = getCdIndex(group);

            log.info("基础配置: group={}, majorCd={}, minorCd={}, isGpProject={}, cdIndex={}",
                     group, majorCd, minorCd, isGpProject, cdIndex);

            // 4. 构建XML请求体
            params.put("majorClassCd", majorCd);
            params.put("minorClassCd", minorCd);

            String xmlRequest;
            if (isGpProject)
            {
                // GP项目
                xmlRequest = com.ruoyi.monitor.utils.XmlParser.buildGpXmlRequest(params);
            }
            else
            {
                // 非GP项目
                xmlRequest = com.ruoyi.monitor.utils.XmlParser.buildNonGpXmlRequest(params);
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
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_XML);
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(xmlRequest, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful())
            {
                log.error("API调用失败，状态码: {}", response.getStatusCode());
                return result;
            }

            String xmlResponse = response.getBody();
            log.info("XML响应: {}", xmlResponse);

            // 6. 解析XML响应
            List<Map<String, String>> xmlRows = com.ruoyi.monitor.utils.XmlParser.parseXmlResponse(xmlResponse);
            log.info("解析XML，共 {} 个row", xmlRows.size());

            // 6.5 清理XML rows（非GP项目按固定位置移除分隔符）
            List<Map<String, String>> cleanedXmlRows = new ArrayList<>();
            for (Map<String, String> row : xmlRows)
            {
                cleanedXmlRows.add(cleanXmlRowIfNeeded(row, isGpProject));
            }

            // 7. 缓存XML数据（用于后续查询时直接使用，避免重复调用API）
            String cacheKey = xmlDataCache.generateCacheKey(configKey, params);
            xmlDataCache.put(cacheKey, cleanedXmlRows);
            log.info("已缓存XML数据，cacheKey: {}", cacheKey);

            // 8. 处理下拉框配置
            if (configJson.containsKey("formItems"))
            {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> formItems = (List<Map<String, Object>>) configJson.get("formItems");

                for (Map<String, Object> item : formItems)
                {
                    if ("select".equals(item.get("type")) && "xml".equals(item.get("dataSource")))
                    {
                        String prop = (String) item.get("prop");
                        String expression = (String) item.get("expression");

                        log.info("处理下拉框: prop={}, expression={}", prop, expression);

                        // 解析expression: "001;2;2"
                        String[] parts = expression.split(";");
                        if (parts.length != 3)
                        {
                            log.warn("下拉框表达式格式错误: {}", expression);
                            continue;
                        }

                        String targetTypeCode = parts[0];  // 加工条件種cd
                        int itemSeq = Integer.parseInt(parts[2]); // 序号

                        // 提取选项值
                        java.util.Set<String> options = new java.util.LinkedHashSet<>();
                        for (Map<String, String> row : cleanedXmlRows)
                        {
                            // GP项目: cd在item_5; 非GP项目: cd在item_3
                            String rowTypeCode = row.get("item_" + cdIndex);
                            if (targetTypeCode.equals(rowTypeCode))
                            {
                                // 计算序号值的位置：cdIndex + 序号
                                String optionValue = row.get("item_" + (cdIndex + itemSeq));
                                if (StringUtils.isNotEmpty(optionValue))
                                {
                                    options.add(optionValue);
                                }
                            }
                        }

                        log.info("下拉框 {} 的选项: {}", prop, options);
                        result.put(prop, new ArrayList<>(options));
                    }
                }
            }

            log.info("==================== 下拉框选项获取完成 ====================");
        }
        catch (Exception e)
        {
            log.error("==================== 获取下拉框选项失败 ====================", e);
            log.error("配置KEY: {}", configKey);
            log.error("请求参数: {}", params);
        }

        return result;
    }

    /**
     * 获取加工条件种cd的索引位置
     * GP项目：item_5，非GP项目：item_3
     */
    private int getCdIndex(String group)
    {
        return "02".equals(group) ? 5 : 3;
    }

    /**
     * 获取数据起始索引位置
     * GP项目：item_6，非GP项目：item_4
     */
    private int getDataStartIndex(String group)
    {
        return "02".equals(group) ? 6 : 4;
    }

    /**
     * 清理非GP项目XML的固定位置分隔符
     *
     * <p>非GP项目XML格式固定：
     * <ul>
     *   <li>item_0 ~ item_3: 固定字段（4个）</li>
     *   <li>item_4, item_6, item_8...: 真实值（偶数索引）</li>
     *   <li>item_5, item_7, item_9...: 分隔符（奇数索引，值不固定，可能是9、;、,等）</li>
     * </ul>
     *
     * <p>清理策略：直接跳过奇数索引位置，不关心分隔符的具体值
     *
     * @param row 原始XML row
     * @param isGpProject 是否是GP项目（GP项目无分隔符，直接返回原row）
     * @return 清理后的row
     */
    private Map<String, String> cleanXmlRowIfNeeded(Map<String, String> row, boolean isGpProject)
    {
        if (row == null || row.isEmpty())
        {
            return row;
        }

        // GP项目没有分隔符，直接返回
        if (isGpProject)
        {
            return row;
        }

        // 非GP项目：分隔符位置固定在奇数索引（item_5, item_7, item_9...），直接跳过
        Map<String, String> cleanedRow = new HashMap<>();

        // 1. 复制固定字段（item_0 到 item_3）
        for (int i = 0; i <= 3; i++)
        {
            String value = row.get("item_" + i);
            if (value != null)
            {
                cleanedRow.put("item_" + i, value);
            }
        }

        // 2. 只复制偶数索引的数据（跳过奇数索引的分隔符）
        //    原始：item_4=值1, item_5=分隔符, item_6=值2, item_7=分隔符...
        //    结果：item_4=值1, item_5=值2, item_6=值3...
        int targetIndex = 4;
        for (int i = 4; i < 100; i += 2)  // i += 2 只遍历偶数索引
        {
            String value = row.get("item_" + i);
            if (value == null)
            {
                break;
            }
            cleanedRow.put("item_" + targetIndex, value);
            targetIndex++;
        }

        log.debug("清理非GP项目XML分隔符: {}个字段 → {}个字段", row.size(), cleanedRow.size());

        return cleanedRow;
    }

    /**
     * 标准化XML值，处理空值和NoData
     *
     * @param value XML中提取的原始值
     * @return 标准化后的值，如果是空或NoData则返回null
     */
    private String normalizeXmlValue(String value)
    {
        // null或空字符串
        if (value == null || value.trim().isEmpty())
        {
            return null;
        }

        // 去除前后空格
        String trimmedValue = value.trim();

        // 如果是"NoData"（不区分大小写），返回null
        if ("NoData".equalsIgnoreCase(trimmedValue))
        {
            return null;
        }

        return trimmedValue;
    }
}


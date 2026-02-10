package com.ruoyi.monitor.utils;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 监控数据处理工具类 - 统一处理XML和前端数据
 */
public class MonitorDataProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(MonitorDataProcessor.class);
    
    // ==================== XML数据处理 ====================
    
    /**
     * XML行索引器 - 按cd建立索引，O(1)查找
     */
    public static class XmlRowIndexer {
        private final Map<String, List<Map<String, String>>> cdToRowsMap;
        
        public XmlRowIndexer(List<Map<String, String>> xmlRows, MonitorConfigUtil.ProjectConfig config) {
            this.cdToRowsMap = buildIndex(xmlRows, config);
        }
        
        private Map<String, List<Map<String, String>>> buildIndex(List<Map<String, String>> xmlRows, MonitorConfigUtil.ProjectConfig config) {
            Map<String, List<Map<String, String>>> index = new LinkedHashMap<>();
            
            for (Map<String, String> row : xmlRows) {
                if (row == null) continue;
                
                String cd = row.get("item_" + config.getCdIndex());
                if (StringUtils.isNotEmpty(cd)) {
                    index.computeIfAbsent(cd, k -> new ArrayList<>()).add(row);
                }
            }
            
            log.debug("构建XML索引完成，共 {} 个cd，总计 {} 行数据", index.size(), xmlRows.size());
            return index;
        }
        
        public List<Map<String, String>> getRowsByCd(String cd) {
            return cdToRowsMap.getOrDefault(cd, Collections.emptyList());
        }
    }
    
    /**
     * 清理非GP项目XML的分隔符
     */
    public static Map<String, String> cleanXmlRowIfNeeded(Map<String, String> row, boolean isGpProject) {
        if (row == null || row.isEmpty()) {
            return row;
        }

        if (isGpProject) {
            return row;
        }

        Map<String, String> cleanedRow = new HashMap<>();
        
        for (int i = 0; i <= 3; i++) {
            String value = row.get("item_" + i);
            if (value != null) {
                cleanedRow.put("item_" + i, value);
            }
        }

        int targetIndex = 4;
        for (int i = 4; i < 100; i += 2) {
            String value = row.get("item_" + i);
            if (value == null) {
                break;
            }
            cleanedRow.put("item_" + targetIndex, value);
            targetIndex++;
        }

        return cleanedRow;
    }
    
    
    /**
     * 标准化XML值，处理空值和NoData
     */
    public static String normalizeXmlValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String trimmedValue = value.trim();

        if ("NoData".equalsIgnoreCase(trimmedValue)) {
            return null;
        }

        return trimmedValue;
    }
    
    // ==================== 字段提取 ====================
    
    /**
     * 简化的字段提取方法 - 使用索引器优化性能
     */
    public static String extractFieldValue(String expression, 
                                         List<Map<String, String>> xmlRows, 
                                         MonitorConfigUtil.ProjectConfig config,
                                         Map<String, Object> userSelections) {
        try {
            MonitorConfigUtil.FieldExpression fieldExpr = new MonitorConfigUtil.FieldExpression(expression);
            XmlRowIndexer indexer = new XmlRowIndexer(xmlRows, config);
            
            List<Map<String, String>> candidateRows = indexer.getRowsByCd(fieldExpr.getCd());
            if (candidateRows.isEmpty()) {
                return null;
            }
            
            Map<String, String> targetRow = findMatchingRow(candidateRows, fieldExpr.getCd(), userSelections, config);
            if (targetRow == null) {
                // 找不到匹配的行时，返回null而不是使用默认值
                log.debug("未找到匹配的XML行: cd={}, userSelections={}", fieldExpr.getCd(), userSelections);
                return null;
            }
            
            int fieldIndex = fieldExpr.calculateIndex(config);
            String value = targetRow.get("item_" + fieldIndex);
            
            return normalizeXmlValue(value);
        } catch (Exception e) {
            log.error("提取字段值失败: expression={}", expression, e);
            return null;
        }
    }
    
    /**
     * 简化的下拉框选项提取方法
     */
    public static Set<String> extractSelectOptions(String expression, 
                                                  List<Map<String, String>> xmlRows, 
                                                  MonitorConfigUtil.ProjectConfig config) {
        try {
            MonitorConfigUtil.FieldExpression fieldExpr = new MonitorConfigUtil.FieldExpression(expression);
            XmlRowIndexer indexer = new XmlRowIndexer(xmlRows, config);
            
            List<Map<String, String>> rows = indexer.getRowsByCd(fieldExpr.getCd());
            int fieldIndex = fieldExpr.calculateIndex(config);
            
            Set<String> options = new LinkedHashSet<>();
            for (Map<String, String> row : rows) {
                String value = row.get("item_" + fieldIndex);
                String normalizedValue = normalizeXmlValue(value);
                if (normalizedValue != null) {
                    options.add(normalizedValue);
                }
            }
            
            return options;
        } catch (Exception e) {
            log.error("提取下拉框选项失败: expression={}", expression, e);
            return Collections.emptySet();
        }
    }
    
    /**
     * 简化的行匹配方法
     */
    @SuppressWarnings("unchecked")
    private static Map<String, String> findMatchingRow(List<Map<String, String>> candidateRows,
                                                      String cd,
                                                      Map<String, Object> userSelections,
                                                      MonitorConfigUtil.ProjectConfig config) {
        if (userSelections == null || userSelections.isEmpty()) {
            return candidateRows.isEmpty() ? null : candidateRows.get(0);
        }
        
        Map<String, Object> selectConfigs = (Map<String, Object>) userSelections.get("selectConfigs");
        if (selectConfigs == null) {
            return candidateRows.isEmpty() ? null : candidateRows.get(0);
        }
        
        Map<String, Object> cdSelectConfigs = new HashMap<>();
        for (Map.Entry<String, Object> entry : selectConfigs.entrySet()) {
            String selectProp = entry.getKey();
            Map<String, Object> selectConfig = (Map<String, Object>) entry.getValue();
            String expression = (String) selectConfig.get("expression");
            
            if (expression != null && expression.startsWith(cd + ";")) {
                cdSelectConfigs.put(selectProp, selectConfig);
            }
        }
        
        if (cdSelectConfigs.isEmpty()) {
            return candidateRows.isEmpty() ? null : candidateRows.get(0);
        }
        
        for (Map<String, String> row : candidateRows) {
            boolean allMatch = true;
            
            for (Map.Entry<String, Object> configEntry : cdSelectConfigs.entrySet()) {
                String selectProp = configEntry.getKey();
                Object userValue = userSelections.get(selectProp);
                
                if (userValue == null) continue;
                
                Map<String, Object> selectConfig = (Map<String, Object>) configEntry.getValue();
                String expression = (String) selectConfig.get("expression");
                int sequence = Integer.parseInt(expression.split(";")[2]);
                
                String rowValue = row.get("item_" + (config.getCdIndex() + sequence));
                
                if (!userValue.toString().equals(rowValue)) {
                    allMatch = false;
                    break;
                }
            }
            
            if (allMatch) {
                return row;
            }
        }
        
        return null;
    }
    
    // ==================== 前端数据构建 ====================
    
    /**
     * 构建前端期望的数据格式
     */
    public static Map<String, Object> buildFrontendData(JSONObject configJson, Map<String, Object> rawData) {
        Map<String, Object> result = new HashMap<>();
        
        // 构建 descItems
        if (configJson.containsKey("descItems")) {
            List<Map<String, Object>> descResult = buildDescItems(configJson, rawData);
            result.put("descItems", descResult);
        }
        
        // 构建 remarkItems
        if (configJson.containsKey("remarkItems")) {
            List<Map<String, Object>> remarkResult = buildRemarkItems(configJson, rawData);
            result.put("remarkItems", remarkResult);
        }
        
        // 构建 tableConfigs
        if (configJson.containsKey("tableConfigs")) {
            List<Map<String, Object>> tableResult = buildTableConfigs(configJson, rawData);
            result.put("tableConfigs", tableResult);
        }
        
        return result;
    }
    
    /**
     * 构建描述项数据
     */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> buildDescItems(JSONObject configJson, Map<String, Object> rawData) {
        List<Map<String, Object>> descItems = (List<Map<String, Object>>) configJson.get("descItems");
        List<Map<String, Object>> descResult = new ArrayList<>();
        
        for (Map<String, Object> item : descItems) {
            Map<String, Object> descItem = new HashMap<>();
            String label = (String) item.get("label");
            descItem.put("label", label);
            
            Object value = extractValueFromRawData(item, rawData);
            descItem.put("value", value != null ? value : "");
            descResult.add(descItem);
        }
        
        return descResult;
    }
    
    /**
     * 构建备注项数据
     */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> buildRemarkItems(JSONObject configJson, Map<String, Object> rawData) {
        List<Map<String, Object>> remarkItems = (List<Map<String, Object>>) configJson.get("remarkItems");
        List<Map<String, Object>> remarkResult = new ArrayList<>();
        
        for (Map<String, Object> item : remarkItems) {
            Map<String, Object> remarkItem = new HashMap<>();
            remarkItem.put("title", item.get("title"));
            
            Object value = extractValueFromRawData(item, rawData);
            remarkItem.put("content", value != null ? value : "");
            remarkResult.add(remarkItem);
        }
        
        return remarkResult;
    }
    
    /**
     * 构建表格配置数据
     */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> buildTableConfigs(JSONObject configJson, Map<String, Object> rawData) {
        List<Map<String, Object>> tableConfigs = (List<Map<String, Object>>) configJson.get("tableConfigs");
        List<Map<String, Object>> tableResult = new ArrayList<>();
        
        for (Map<String, Object> tableConfig : tableConfigs) {
            Map<String, Object> table = new HashMap<>();
            table.put("title", tableConfig.get("title"));
            
            if (tableConfig.containsKey("rows")) {
                List<Map<String, Object>> rows = (List<Map<String, Object>>) tableConfig.get("rows");
                List<Map<String, Object>> rowResults = new ArrayList<>();
                
                for (Map<String, Object> row : rows) {
                    Map<String, Object> rowResult = buildTableRow(row, rawData);
                    if (rowResult != null) {
                        rowResults.add(rowResult);
                    }
                }
                
                table.put("rows", rowResults);
            }
            
            tableResult.add(table);
        }
        
        return tableResult;
    }
    
    /**
     * 构建表格行数据
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> buildTableRow(Map<String, Object> row, Map<String, Object> rawData) {
        String rowType = (String) row.get("rowType");
        Map<String, Object> rowResult = new HashMap<>();
        rowResult.put("rowType", rowType);
        
        if ("simple".equals(rowType)) {
            Object value = extractValueFromRawData(row, rawData);
            rowResult.put("projectName", row.get("projectName"));  // 前端期望 projectName
            rowResult.put("unit", row.get("unit"));               // 前端期望 unit
            rowResult.put("value", value != null ? value : "");
        } else if ("complex".equals(rowType)) {
            rowResult.put("projectName", row.get("projectName"));  // 前端期望 projectName
            
            if (row.containsKey("subRows")) {
                List<Map<String, Object>> subRows = (List<Map<String, Object>>) row.get("subRows");
                List<Map<String, Object>> subResults = new ArrayList<>();
                
                for (Map<String, Object> subRow : subRows) {
                    Map<String, Object> subResult = new HashMap<>();
                    subResult.put("subName", subRow.get("subName"));   // 前端期望 subName
                    subResult.put("unit", subRow.get("unit"));         // 前端期望 unit
                    
                    Object value = extractValueFromRawData(subRow, rawData);
                    subResult.put("value", value != null ? value : "");
                    subResults.add(subResult);
                }
                
                rowResult.put("subRows", subResults);
            }
        }
        
        return rowResult;
    }
    
    /**
     * 从原始数据中提取值
     */
    private static Object extractValueFromRawData(Map<String, Object> item, Map<String, Object> rawData) {
        String dataSource = (String) item.get("dataSource");
        String displayType = (String) item.get("displayType");
        
        if ("database".equals(dataSource)) {
            if ("computed".equals(displayType)) {
                String valueKey = (String) item.get("valueKey");
                if (StringUtils.isNotEmpty(valueKey)) {
                    return rawData.get(valueKey);
                } else {
                    String expression = (String) item.get("expression");
                    String normalizedExpression = SymbolNormalizer.normalize(expression);
                    return rawData.get(normalizedExpression);
                }
            } else {
                String sql = (String) item.get("expression");
                return rawData.get(sql);
            }
        } else if ("api".equals(dataSource)) {
            String valueKey = (String) item.get("valueKey");
            if (StringUtils.isNotEmpty(valueKey)) {
                return rawData.get(valueKey);
            } else {
                String expression = (String) item.get("expression");
                String normalizedExpression = (String) item.get("normalizedExpression");
                if (normalizedExpression == null) {
                    normalizedExpression = SymbolNormalizer.normalize(expression);
                }
                return rawData.get(normalizedExpression);
            }
        }
        
        return "";
    }
    
    // ==================== API数据获取 ====================
    
    /**
     * 从API获取数据
     */
    public static Map<String, Object> getApiData(String configKey, JSONObject configJson, Map<String, Object> params,
                                                com.ruoyi.monitor.cache.XmlDataCache xmlDataCache) {
        Map<String, Object> apiData = new HashMap<>();

        try {
            // 检查配置中是否有API数据源
            boolean hasApiDataSource = MonitorConfigUtil.hasApiDataSource(configJson);

            if (!hasApiDataSource) {
                log.debug("配置中没有API数据源，跳过API数据获取");
                return apiData;
            }

            // 1. 获取API URL
            String apiUrl = configJson.getString("apiUrl");
            if (StringUtils.isEmpty(apiUrl)) {
                log.warn("配置中有API数据源，但未配置apiUrl");
                return apiData;
            }

            log.info("准备调用外部API: {}", apiUrl);

            // 2. 检查是否为XML API（通过procConditionGroup判断）
            boolean isXmlApi = StringUtils.isNotEmpty(configJson.getString("procConditionGroup"));

            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            org.springframework.http.ResponseEntity<String> response;

            if (isXmlApi) {
                // XML API - 使用POST请求
                String group = configJson.getString("procConditionGroup");
                String majorCd = configJson.getString("majorClassCd");
                String minorCd = configJson.getString("minorClassCd");

                params.put("majorClassCd", majorCd);
                params.put("minorClassCd", minorCd);

                // 先检查缓存
                String cacheKey = xmlDataCache.generateCacheKey(configKey, params);
                List<Map<String, String>> cachedXmlRows = xmlDataCache.get(cacheKey);

                if (cachedXmlRows != null) {
                    // 缓存命中，直接使用缓存数据
                    log.info("使用缓存的XML数据，跳过API调用");
                    apiData = extractXmlData(configJson, cachedXmlRows, params);
                    log.info("从缓存的XML数据中提取到 {} 个字段", apiData.size());
                    return apiData;
                }

                // 缓存未命中，调用API
                log.info("缓存未命中，调用XML API");

                String xmlRequest;
                if ("02".equals(group)) {
                    xmlRequest = com.ruoyi.monitor.utils.XmlParser.buildGpXmlRequest(params);
                } else {
                    xmlRequest = com.ruoyi.monitor.utils.XmlParser.buildNonGpXmlRequest(params);
                }

                log.info("XML请求体: {}", xmlRequest);

                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_XML);
                org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(xmlRequest, headers);

                response = restTemplate.postForEntity(apiUrl, entity, String.class);

                // 解析并缓存结果（重要：缓存清理后的数据）
                String xmlResponse = response.getBody();
                if (xmlResponse != null && xmlResponse.trim().startsWith("<?xml")) {
                    List<Map<String, String>> xmlRows = com.ruoyi.monitor.utils.XmlParser.parseXmlResponse(xmlResponse);
                    
                    // 清理XML数据后再缓存（避免缓存原始数据覆盖已清理的数据）
                    MonitorConfigUtil.ProjectConfig projectConfig = new MonitorConfigUtil.ProjectConfig(group);
                    List<Map<String, String>> cleanedXmlRows = new ArrayList<>();
                    for (Map<String, String> row : xmlRows) {
                        cleanedXmlRows.add(cleanXmlRowIfNeeded(row, projectConfig.isGp()));
                    }
                    
                    // 调试日志：输出清理后的第一行数据
                    if (!cleanedXmlRows.isEmpty()) {
                        log.info("清理后的第一行数据: {}", cleanedXmlRows.get(0));
                    }
                    
                    xmlDataCache.put(cacheKey, cleanedXmlRows);
                    log.info("已缓存清理后的XML数据，cacheKey: {}", cacheKey);
                }
            } else {
                // JSON API - 使用GET请求（保持原有逻辑）
                response = restTemplate.getForEntity(apiUrl, String.class);
            }

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("API调用失败，状态码: {}", response.getStatusCode());
                return apiData;
            }

            String responseBody = response.getBody();
            log.info("API返回数据: {}", responseBody);

            if (isXmlApi) {
                // XML响应处理
                apiData = extractXmlData(configJson, responseBody, params);
            } else {
                // JSON响应处理
                processJsonApiResponse(configJson, responseBody, apiData);
            }

        } catch (Exception e) {
            log.error("从API获取数据失败", e);
        }

        return apiData;
    }
    
    /**
     * 处理JSON API响应
     */
    private static void processJsonApiResponse(JSONObject configJson, String apiResponse, Map<String, Object> apiData) {
        // 处理descItems
        if (configJson.containsKey("descItems")) {
            processJsonApiItems(configJson, "descItems", apiResponse, apiData);
        }

        // 处理remarkItems
        if (configJson.containsKey("remarkItems")) {
            processJsonApiItems(configJson, "remarkItems", apiResponse, apiData);
        }

        // 处理tableConfigs
        if (configJson.containsKey("tableConfigs")) {
            processJsonApiTableConfigs(configJson, apiResponse, apiData);
        }
    }
    
    /**
     * 处理JSON API配置项
     */
    @SuppressWarnings("unchecked")
    private static void processJsonApiItems(JSONObject configJson, String itemsKey, String apiResponse, Map<String, Object> apiData) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) configJson.get(itemsKey);
        
        for (Map<String, Object> item : items) {
            if (!"api".equals(item.get("dataSource"))) {
                continue;
            }

            String expression = (String) item.get("expression");
            String displayType = (String) item.get("displayType");

            if (StringUtils.isEmpty(expression)) {
                return;
            }

            // 标准化表达式中的符号
            String normalizedExpression = SymbolNormalizer.normalize(expression);

            // 如果是运算类型，需要提取表达式中的所有变量
            if ("computed".equals(displayType)) {
                // 提取表达式中的所有变量名
                Set<String> variables = MonitorConfigUtil.extractVariablesFromExpression(normalizedExpression);
                log.info("  运算表达式: {} 包含变量: {}", normalizedExpression, variables);

                // 提取每个变量的值
                JSONObject responseJson = com.alibaba.fastjson2.JSON.parseObject(apiResponse);
                for (String variable : variables) {
                    if (!apiData.containsKey(variable)) {
                        Object value = JsonPathExtractor.extractValue(responseJson, variable);
                        apiData.put(variable, value);
                        log.info("    提取变量: {} = {}", variable, value);
                    }
                }

                // 存储标准化后的表达式
                apiData.put(normalizedExpression, normalizedExpression);
                log.info("  存储表达式: {}", normalizedExpression);
            } else {
                // 直接显示类型，直接提取字段值
                JSONObject responseJson = com.alibaba.fastjson2.JSON.parseObject(apiResponse);
                Object value = JsonPathExtractor.extractValue(responseJson, normalizedExpression);
                apiData.put(normalizedExpression, value);
                log.info("  提取字段: {} = {}", normalizedExpression, value);
            }
        }
    }
    
    /**
     * 处理JSON API表格配置
     */
    @SuppressWarnings("unchecked")
    private static void processJsonApiTableConfigs(JSONObject configJson, String apiResponse, Map<String, Object> apiData) {
        List<Map<String, Object>> tableConfigs = (List<Map<String, Object>>) configJson.get("tableConfigs");
        
        for (Map<String, Object> tableConfig : tableConfigs) {
            if (tableConfig.containsKey("rows")) {
                List<Map<String, Object>> rows = (List<Map<String, Object>>) tableConfig.get("rows");
                
                for (Map<String, Object> row : rows) {
                    String rowType = (String) row.get("rowType");
                    
                    if ("simple".equals(rowType)) {
                        processJsonApiSingleItem(row, apiResponse, apiData);
                    } else if ("complex".equals(rowType)) {
                        if (row.containsKey("subRows")) {
                            List<Map<String, Object>> subRows = (List<Map<String, Object>>) row.get("subRows");
                            for (Map<String, Object> subRow : subRows) {
                                processJsonApiSingleItem(subRow, apiResponse, apiData);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 处理JSON API单个配置项
     */
    private static void processJsonApiSingleItem(Map<String, Object> item, String apiResponse, Map<String, Object> apiData) {
        if (!"api".equals(item.get("dataSource"))) {
            return;
        }

        String expression = (String) item.get("expression");
        String displayType = (String) item.get("displayType");

        if (StringUtils.isEmpty(expression)) {
            return;
        }

        String normalizedExpression = SymbolNormalizer.normalize(expression);

        if ("computed".equals(displayType)) {
            Set<String> variables = MonitorConfigUtil.extractVariablesFromExpression(normalizedExpression);
            JSONObject responseJson = com.alibaba.fastjson2.JSON.parseObject(apiResponse);
            
            for (String variable : variables) {
                if (!apiData.containsKey(variable)) {
                    Object value = JsonPathExtractor.extractValue(responseJson, variable);
                    apiData.put(variable, value);
                }
            }
            
            apiData.put(normalizedExpression, normalizedExpression);
        } else {
            JSONObject responseJson = com.alibaba.fastjson2.JSON.parseObject(apiResponse);
            Object value = JsonPathExtractor.extractValue(responseJson, normalizedExpression);
            apiData.put(normalizedExpression, value);
        }
    }
    
    /**
     * 从XML响应中提取数据
     */
    public static Map<String, Object> extractXmlData(JSONObject configJson, String xmlResponse, Map<String, Object> params) throws Exception {
        log.info("开始从XML响应中提取数据");

        // 1. 解析XML
        List<Map<String, String>> xmlRows = com.ruoyi.monitor.utils.XmlParser.parseXmlResponse(xmlResponse);
        log.info("解析XML，共 {} 个row", xmlRows.size());

        // 2. 清理XML数据（非GP项目需要移除分隔符）
        String group = configJson.getString("procConditionGroup");
        MonitorConfigUtil.ProjectConfig projectConfig = new MonitorConfigUtil.ProjectConfig(group);
        List<Map<String, String>> cleanedXmlRows = new ArrayList<>();
        for (Map<String, String> row : xmlRows) {
            cleanedXmlRows.add(cleanXmlRowIfNeeded(row, projectConfig.isGp()));
        }
        
        // 调试日志：输出清理后的第一行数据
        if (!cleanedXmlRows.isEmpty()) {
            log.info("清理后的第一行数据: {}", cleanedXmlRows.get(0));
        }

        // 3. 调用重载方法处理清理后的数据
        return extractXmlData(configJson, cleanedXmlRows, params);
    }

    /**
     * 从XML行数据中提取数据（重载方法，用于缓存优化）- 使用简化算法
     */
    public static Map<String, Object> extractXmlData(JSONObject configJson, List<Map<String, String>> xmlRows, Map<String, Object> params) throws Exception {
        Map<String, Object> result = new HashMap<>();

        log.info("从XML行数据中提取配置项，共 {} 个row", xmlRows.size());

        // 1. 获取基础配置并创建项目配置对象
        String group = configJson.getString("procConditionGroup");
        MonitorConfigUtil.ProjectConfig projectConfig = new MonitorConfigUtil.ProjectConfig(group);

        log.info("基础配置: group={}, isGpProject={}", group, projectConfig.isGp());

        // 2. 直接使用已清理的XML数据（避免重复清理bug）
        // 3. 使用简化算法提取所有配置项的值
        extractAllConfiguredFields(configJson, xmlRows, projectConfig, params, result);

        log.info("从XML提取数据完成，共 {} 个字段", result.size());

        return result;
    }
    
    /**
     * 使用简化算法提取所有配置的字段
     */
    @SuppressWarnings("unchecked")
    private static void extractAllConfiguredFields(JSONObject configJson, 
                                          List<Map<String, String>> xmlRows, 
                                          MonitorConfigUtil.ProjectConfig config, 
                                          Map<String, Object> params, 
                                          Map<String, Object> result) {
        
        // 处理 descItems
        if (configJson.containsKey("descItems")) {
            List<Map<String, Object>> descItems = (List<Map<String, Object>>) configJson.get("descItems");
            
            for (Map<String, Object> item : descItems) {
                if ("api".equals(item.get("dataSource"))) {
                    String expression = (String) item.get("expression");
                    String valueKey = (String) item.get("valueKey");
                    String displayType = (String) item.get("displayType");
                    
                    if (expression != null && valueKey != null) {
                        // 只处理direct类型的API字段表达式，computed类型由processComputedData处理
                        if ("direct".equals(displayType) && isFieldExpression(expression)) {
                            String value = extractFieldValue(expression, xmlRows, config, params);
                            result.put(valueKey, value);
                            log.debug("提取descItem: {} = {}", valueKey, value);
                        }
                    }
                }
            }
        }
        
        // 处理 remarkItems
        if (configJson.containsKey("remarkItems")) {
            List<Map<String, Object>> remarkItems = (List<Map<String, Object>>) configJson.get("remarkItems");
            
            for (Map<String, Object> item : remarkItems) {
                if ("api".equals(item.get("dataSource"))) {
                    String expression = (String) item.get("expression");
                    String valueKey = (String) item.get("valueKey");
                    String displayType = (String) item.get("displayType");
                    
                    if (expression != null && valueKey != null) {
                        // 只处理direct类型的API字段表达式，computed类型由processComputedData处理
                        if ("direct".equals(displayType) && isFieldExpression(expression)) {
                            String value = extractFieldValue(expression, xmlRows, config, params);
                            result.put(valueKey, value);
                            log.debug("提取remarkItem: {} = {}", valueKey, value);
                        }
                    }
                }
            }
        }
        
        // 处理 tableConfigs
        if (configJson.containsKey("tableConfigs")) {
            List<Map<String, Object>> tableConfigs = (List<Map<String, Object>>) configJson.get("tableConfigs");
            
            for (Map<String, Object> tableConfig : tableConfigs) {
                if (tableConfig.containsKey("rows")) {
                    List<Map<String, Object>> rows = (List<Map<String, Object>>) tableConfig.get("rows");
                    
                    for (Map<String, Object> row : rows) {
                        extractTableRowFields(row, xmlRows, config, params, result);
                    }
                }
            }
        }
    }
    
    /**
     * 提取表格行字段
     */
    @SuppressWarnings("unchecked")
    private static void extractTableRowFields(Map<String, Object> row, 
                                     List<Map<String, String>> xmlRows, 
                                     MonitorConfigUtil.ProjectConfig config, 
                                     Map<String, Object> params, 
                                     Map<String, Object> result) {
        
        String rowType = (String) row.get("rowType");
        
        if ("simple".equals(rowType)) {
            if ("api".equals(row.get("dataSource"))) {
                String expression = (String) row.get("expression");
                String valueKey = (String) row.get("valueKey");
                String displayType = (String) row.get("displayType");
                
                if (expression != null && valueKey != null) {
                    // 只处理direct类型的API字段表达式，computed类型由processComputedData处理
                    if ("direct".equals(displayType) && isFieldExpression(expression)) {
                        String value = extractFieldValue(expression, xmlRows, config, params);
                        result.put(valueKey, value);
                        log.debug("提取tableItem: {} = {}", valueKey, value);
                    }
                }
            }
        } else if ("complex".equals(rowType)) {
            if (row.containsKey("subRows")) {
                List<Map<String, Object>> subRows = (List<Map<String, Object>>) row.get("subRows");
                
                for (Map<String, Object> subRow : subRows) {
                    if ("api".equals(subRow.get("dataSource"))) {
                        String expression = (String) subRow.get("expression");
                        String valueKey = (String) subRow.get("valueKey");
                        String displayType = (String) subRow.get("displayType");
                        
                        if (expression != null && valueKey != null) {
                            // 只处理direct类型的API字段表达式，computed类型由processComputedData处理
                            if ("direct".equals(displayType) && isFieldExpression(expression)) {
                                String value = extractFieldValue(expression, xmlRows, config, params);
                                result.put(valueKey, value);
                                log.debug("提取subTableItem: {} = {}", valueKey, value);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查表达式是否为字段表达式格式 (cd;type;sequence)
     */
    private static boolean isFieldExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        
        try {
            String[] parts = expression.split(";");
            if (parts.length != 3) {
                return false;
            }
            
            // 检查第二部分是否为数字（type）
            Integer.parseInt(parts[1]);
            
            // 检查第三部分是否为数字（sequence）
            Integer.parseInt(parts[2]);
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // ==================== 数据库数据获取 ====================
    
    /**
     * 从数据库获取数据
     */
    public static Map<String, Object> getDatabaseData(JSONObject configJson, Map<String, Object> params,
                                                     com.ruoyi.monitor.service.ISqlMergeService sqlMergeService) {
        Map<String, Object> dbData = new HashMap<>();
        List<String> sqlList = new ArrayList<>();
        // 用于建立 SQL -> valueKey 的映射关系
        Map<String, String> sqlToValueKeyMap = new HashMap<>();

        try {
            // 收集所有数据库数据源的SQL，并建立SQL到valueKey的映射
            MonitorConfigUtil.collectDatabaseSqls(configJson, sqlList, sqlToValueKeyMap);

            // 如果有SQL需要执行
            if (!sqlList.isEmpty()) {
                // 使用SQL合并服务执行
                Map<String, Object> sqlResults = sqlMergeService.mergeAndExecute(sqlList, params);
                
                // 存储结果：处理SQL执行返回的Map（包含列名/别名）
                for (Map.Entry<String, Object> entry : sqlResults.entrySet()) {
                    String sql = entry.getKey();
                    Object result = entry.getValue();
                    
                    log.info("  [SQL结果处理] SQL: {}", sql);
                    log.info("  [SQL结果处理] 返回类型: {}", result != null ? result.getClass().getSimpleName() : "null");
                    
                    // 如果结果是Map（包含列名），提取列名和值
                    if (result instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> columnMap = (Map<String, Object>) result;
                        
                        log.info("  [SQL结果处理] 包含 {} 个列: {}", columnMap.size(), columnMap.keySet());
                        
                        // 1. 存储每个列名（别名）和对应的值
                        for (Map.Entry<String, Object> columnEntry : columnMap.entrySet()) {
                            String columnName = columnEntry.getKey();
                            Object columnValue = columnEntry.getValue();
                            
                            dbData.put(columnName, columnValue);
                            log.info("    [列名映射] {} -> {}", columnName, columnValue);
                        }
                        
                        // 2. 如果只有一列，也用SQL本身作为key存储值（保持向后兼容）
                        if (columnMap.size() == 1) {
                            Object singleValue = columnMap.values().iterator().next();
                            dbData.put(sql, singleValue);
                            log.info("    [SQL映射] SQL -> {} (向后兼容)", singleValue);
                        } else {
                            // 多列结果，用SQL作为key存储整个Map
                            dbData.put(sql, columnMap);
                            log.info("    [SQL映射] SQL -> Map({}列)", columnMap.size());
                        }
                        
                        // 3. 如果有对应的valueKey，用valueKey作为key存储
                        if (sqlToValueKeyMap.containsKey(sql)) {
                            String valueKey = sqlToValueKeyMap.get(sql);
                            if (StringUtils.isNotEmpty(valueKey)) {
                                // 检查列名中是否有与valueKey匹配的列
                                if (columnMap.containsKey(valueKey)) {
                                    // 如果SQL结果中有同名列，使用该列的值
                                    Object aliasValue = columnMap.get(valueKey);
                                    dbData.put(valueKey, aliasValue);
                                    log.info("    [变量映射-别名] {} -> {} (来自列名)", valueKey, aliasValue);
                                } else if (columnMap.size() == 1) {
                                    // 如果只有一列且没有匹配的列名，使用该列的值
                                    Object singleValue = columnMap.values().iterator().next();
                                    dbData.put(valueKey, singleValue);
                                    log.info("    [变量映射-单列] {} -> {}", valueKey, singleValue);
                                }
                            }
                        }
                    } else {
                        // 兼容旧版本：如果返回的不是Map，直接存储值
                        dbData.put(sql, result);
                        
                        if (sqlToValueKeyMap.containsKey(sql)) {
                            String valueKey = sqlToValueKeyMap.get(sql);
                            if (StringUtils.isNotEmpty(valueKey)) {
                                dbData.put(valueKey, result);
                                log.info("  [变量映射-兼容] {} -> {}", valueKey, result);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("从数据库获取数据失败", e);
        }

        return dbData;
    }

    // ==================== 计算数据处理 ====================
    
    /**
     * 处理计算类型的数据（支持API和数据库数据源）
     */
    public static void processComputedData(JSONObject configJson, Map<String, Object> resultData) {
        log.info("开始处理计算类型数据（支持API和数据库数据源）");
        int computedCount = 0;

        // 处理descItems中的计算类型
        if (configJson.containsKey("descItems")) {
            computedCount += processComputedItems(configJson, "descItems", resultData);
        }

        // 处理remarkItems中的计算类型
        if (configJson.containsKey("remarkItems")) {
            computedCount += processComputedItems(configJson, "remarkItems", resultData);
        }

        // 处理tableConfigs中的计算类型
        if (configJson.containsKey("tableConfigs")) {
            computedCount += processComputedTableConfigs(configJson, resultData);
        }

        log.info("计算类型数据处理完成，共处理 {} 个字段", computedCount);
    }
    
    /**
     * 处理配置项中的计算类型数据
     */
    @SuppressWarnings("unchecked")
    private static int processComputedItems(JSONObject configJson, String itemsKey, Map<String, Object> resultData) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) configJson.get(itemsKey);
        int count = 0;
        
        for (Map<String, Object> item : items) {
            if ("computed".equals(item.get("displayType"))) {
                if (processComputedItem(item, resultData)) {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    /**
     * 处理表格配置中的计算类型数据
     */
    @SuppressWarnings("unchecked")
    private static int processComputedTableConfigs(JSONObject configJson, Map<String, Object> resultData) {
        List<Map<String, Object>> tableConfigs = (List<Map<String, Object>>) configJson.get("tableConfigs");
        int count = 0;
        
        for (Map<String, Object> tableConfig : tableConfigs) {
            if (tableConfig.containsKey("rows")) {
                List<Map<String, Object>> rows = (List<Map<String, Object>>) tableConfig.get("rows");
                
                for (Map<String, Object> row : rows) {
                    String rowType = (String) row.get("rowType");
                    
                    if ("simple".equals(rowType)) {
                        if ("computed".equals(row.get("displayType"))) {
                            if (processComputedItem(row, resultData)) {
                                count++;
                            }
                        }
                    } else if ("complex".equals(rowType)) {
                        if (row.containsKey("subRows")) {
                            List<Map<String, Object>> subRows = (List<Map<String, Object>>) row.get("subRows");
                            for (Map<String, Object> subRow : subRows) {
                                if ("computed".equals(subRow.get("displayType"))) {
                                    if (processComputedItem(subRow, resultData)) {
                                        count++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return count;
    }
    
    /**
     * 处理单个计算项
     */
    private static boolean processComputedItem(Map<String, Object> item, Map<String, Object> resultData) {
        String expression = (String) item.get("expression");
        String valueKey = (String) item.get("valueKey");
        
        if (StringUtils.isEmpty(expression)) {
            return false;
        }
        
        try {
            // 提取表达式中的变量名
            Set<String> variables = MonitorConfigUtil.extractVariablesFromExpression(expression);
            
            // 替换表达式中的变量为实际数值
            String calculableExpression = expression;
            for (String variable : variables) {
                Object value = resultData.get(variable);
                if (value != null) {
                    // 将变量名替换为数值
                    calculableExpression = calculableExpression.replaceAll("\\b" + variable + "\\b", value.toString());
                } else {
                    log.warn("计算表达式中缺少变量值: variable={}, expression={}", variable, expression);
                    return false;
                }
            }
            
            log.debug("计算表达式: {} → {}", expression, calculableExpression);
            
            // 使用表达式计算器计算结果
            Double result = ExpressionCalculator.calculate(calculableExpression);
            
            if (result != null) {
                // 存储结果
                String key = StringUtils.isNotEmpty(valueKey) ? valueKey : SymbolNormalizer.normalize(expression);
                resultData.put(key, result);
                
                log.debug("计算完成: {} = {} (表达式: {})", key, result, expression);
                return true;
            } else {
                log.error("计算结果为null: 表达式={}", calculableExpression);
                return false;
            }
            
        } catch (Exception e) {
            log.error("计算失败: 表达式={}, 错误={}", expression, e.getMessage());
            return false;
        }
    }
}

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
            Map<String, Object> apiData = getApiData(configJson, params);
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
                    String expression = (String) item.get("expression");
                    // 使用标准化后的表达式（如果存在）
                    String normalizedExpression = (String) item.get("normalizedExpression");
                    if (normalizedExpression == null)
                    {
                        normalizedExpression = SymbolNormalizer.normalize(expression);
                    }
                    value = rawData.get(normalizedExpression);
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
     */
    private Map<String, Object> getApiData(JSONObject configJson, Map<String, Object> params)
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

            // 2. 调用外部API
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);

            if (!response.getStatusCode().is2xxSuccessful())
            {
                log.error("API调用失败，状态码: {}", response.getStatusCode());
                return apiData;
            }

            String responseBody = response.getBody();
            log.info("API返回数据: {}", responseBody);

            // 3. 解析API返回的JSON
            JSONObject apiResponse = JSON.parseObject(responseBody);

            // 4. 根据配置中的expression提取数据
            apiData = extractApiData(configJson, apiResponse);

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
}


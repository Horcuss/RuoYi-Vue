package com.ruoyi.monitor.utils;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;

import java.util.*;

/**
 * 监控配置工具类 - 统一处理配置相关的操作
 */
public class MonitorConfigUtil {
    
    /**
     * 项目配置类 - 统一管理GP和非GP的差异
     */
    public static class ProjectConfig {
        private final boolean isGp;
        private final int cdIndex;
        
        public ProjectConfig(String group) {
            this.isGp = "02".equals(group);
            this.cdIndex = isGp ? 5 : 3;
        }
        
        public boolean isGp() { return isGp; }
        public int getCdIndex() { return cdIndex; }
    }
    
    /**
     * 字段表达式解析器
     */
    public static class FieldExpression {
        private final String cd;
        private final int sequence;
        
        public FieldExpression(String expression) {
            String[] parts = expression.split(";");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid expression: " + expression);
            }
            this.cd = parts[0];
            this.sequence = Integer.parseInt(parts[2]);
        }
        
        public String getCd() { return cd; }
        
        public int calculateIndex(ProjectConfig config) {
            return config.getCdIndex() + sequence;
        }
    }
    
    
    /**
     * 检查配置中是否有API数据源
     */
    @SuppressWarnings("unchecked")
    public static boolean hasApiDataSource(JSONObject configJson) {
        // 检查 descItems
        if (configJson.containsKey("descItems")) {
            List<Map<String, Object>> descItems = (List<Map<String, Object>>) configJson.get("descItems");
            for (Map<String, Object> item : descItems) {
                if ("api".equals(item.get("dataSource"))) {
                    return true;
                }
            }
        }

        // 检查 remarkItems
        if (configJson.containsKey("remarkItems")) {
            List<Map<String, Object>> remarkItems = (List<Map<String, Object>>) configJson.get("remarkItems");
            for (Map<String, Object> item : remarkItems) {
                if ("api".equals(item.get("dataSource"))) {
                    return true;
                }
            }
        }

        // 检查 tableConfigs
        if (configJson.containsKey("tableConfigs")) {
            List<Map<String, Object>> tableConfigs = (List<Map<String, Object>>) configJson.get("tableConfigs");
            for (Map<String, Object> tableConfig : tableConfigs) {
                if (tableConfig.containsKey("rows")) {
                    List<Map<String, Object>> rows = (List<Map<String, Object>>) tableConfig.get("rows");
                    for (Map<String, Object> row : rows) {
                        String rowType = (String) row.get("rowType");

                        if ("simple".equals(rowType)) {
                            if ("api".equals(row.get("dataSource"))) {
                                return true;
                            }
                        } else if ("complex".equals(rowType)) {
                            if (row.containsKey("subRows")) {
                                List<Map<String, Object>> subRows = (List<Map<String, Object>>) row.get("subRows");
                                for (Map<String, Object> subRow : subRows) {
                                    if ("api".equals(subRow.get("dataSource"))) {
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
     * 从表达式中提取变量名
     */
    public static Set<String> extractVariablesFromExpression(String expression) {
        Set<String> variables = new HashSet<>();

        // 移除空格
        expression = expression.replaceAll("\\s+", "");

        // 分割运算符: +, -, *, /, (, )
        String[] tokens = expression.split("[+\\-*/()]");

        for (String token : tokens) {
            token = token.trim();
            // 如果不是空字符串，且不是数字，则认为是变量名
            if (!token.isEmpty() && !token.matches("\\d+(\\.\\d+)?")) {
                variables.add(token);
            }
        }

        return variables;
    }
    
    /**
     * 收集配置中所有数据库数据源的SQL，并建立SQL到valueKey的映射
     */
    @SuppressWarnings("unchecked")
    public static void collectDatabaseSqls(JSONObject configJson, 
                                          List<String> sqlList, 
                                          Map<String, String> sqlToValueKeyMap) {
        // 处理descItems
        if (configJson.containsKey("descItems")) {
            List<Map<String, Object>> descItems = (List<Map<String, Object>>) configJson.get("descItems");
            collectFromItems(descItems, sqlList, sqlToValueKeyMap);
        }

        // 处理remarkItems
        if (configJson.containsKey("remarkItems")) {
            List<Map<String, Object>> remarkItems = (List<Map<String, Object>>) configJson.get("remarkItems");
            collectFromItems(remarkItems, sqlList, sqlToValueKeyMap);
        }

        // 处理tableConfigs
        if (configJson.containsKey("tableConfigs")) {
            List<Map<String, Object>> tableConfigs = (List<Map<String, Object>>) configJson.get("tableConfigs");
            collectFromTableConfigs(tableConfigs, sqlList, sqlToValueKeyMap);
        }
    }
    
    /**
     * 从配置项中收集SQL
     */
    private static void collectFromItems(List<Map<String, Object>> items, 
                                       List<String> sqlList, 
                                       Map<String, String> sqlToValueKeyMap) {
        for (Map<String, Object> item : items) {
            if ("database".equals(item.get("dataSource"))) {
                String sql = (String) item.get("expression");
                String valueKey = (String) item.get("valueKey");
                
                if (StringUtils.isNotEmpty(sql)) {
                    sqlList.add(sql);
                    if (StringUtils.isNotEmpty(valueKey)) {
                        sqlToValueKeyMap.put(sql, valueKey);
                    }
                }
            }
        }
    }
    
    /**
     * 从表格配置中收集SQL
     */
    @SuppressWarnings("unchecked")
    private static void collectFromTableConfigs(List<Map<String, Object>> tableConfigs, 
                                              List<String> sqlList, 
                                              Map<String, String> sqlToValueKeyMap) {
        for (Map<String, Object> tableConfig : tableConfigs) {
            if (tableConfig.containsKey("rows")) {
                List<Map<String, Object>> rows = (List<Map<String, Object>>) tableConfig.get("rows");
                
                for (Map<String, Object> row : rows) {
                    String rowType = (String) row.get("rowType");
                    
                    if ("simple".equals(rowType)) {
                        collectFromSingleItem(row, sqlList, sqlToValueKeyMap);
                    } else if ("complex".equals(rowType)) {
                        if (row.containsKey("subRows")) {
                            List<Map<String, Object>> subRows = (List<Map<String, Object>>) row.get("subRows");
                            for (Map<String, Object> subRow : subRows) {
                                collectFromSingleItem(subRow, sqlList, sqlToValueKeyMap);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 从单个配置项中收集SQL
     */
    private static void collectFromSingleItem(Map<String, Object> item, 
                                            List<String> sqlList, 
                                            Map<String, String> sqlToValueKeyMap) {
        if ("database".equals(item.get("dataSource"))) {
            String sql = (String) item.get("expression");
            String valueKey = (String) item.get("valueKey");
            
            if (StringUtils.isNotEmpty(sql)) {
                sqlList.add(sql);
                if (StringUtils.isNotEmpty(valueKey)) {
                    sqlToValueKeyMap.put(sql, valueKey);
                }
            }
        }
    }
}

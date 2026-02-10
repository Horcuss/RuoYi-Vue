package com.ruoyi.monitor.service.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.monitor.service.ISqlMergeService;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SQL合并服务实现类
 * 
 * @author ruoyi
 * @date 2025-01-15
 */
@Service
public class SqlMergeServiceImpl implements ISqlMergeService 
{
    private static final Logger log = LoggerFactory.getLogger(SqlMergeServiceImpl.class);

    @Autowired
    @Qualifier("userDataSource") // 注入user数据源
    private DataSource userDataSource;

    /**
     * 合并执行多个SQL语句
     */
    @Override
    public Map<String, Object> mergeAndExecute(List<String> sqlList) 
    {
        return mergeAndExecute(sqlList, null);
    }

    /**
     * 合并执行多个SQL语句（带参数）
     */
    @Override
    public Map<String, Object> mergeAndExecute(List<String> sqlList, Map<String, Object> params)
    {
        Map<String, Object> resultMap = new HashMap<>();

        if (sqlList == null || sqlList.isEmpty())
        {
            log.info("========== SQL合并服务 ==========");
            log.info("SQL列表为空，无需执行");
            return resultMap;
        }

        log.info("========== SQL合并服务开始 ==========");
        log.info("收到 {} 条SQL需要执行", sqlList.size());
        log.info("请求参数: {}", params);

        // 打印所有原始SQL
        for (int i = 0; i < sqlList.size(); i++)
        {
            log.info("原始SQL[{}]: {}", i + 1, sqlList.get(i));
        }

        try
        {
            // 解析SQL并分组
            log.info("---------- 开始解析和分组SQL ----------");
            Map<String, List<SqlInfo>> groupedSqls = groupSqlsByTableAndWhere(sqlList);
            log.info("分组完成，共 {} 个分组", groupedSqls.size());

            int groupIndex = 1;
            // 合并执行
            for (Map.Entry<String, List<SqlInfo>> entry : groupedSqls.entrySet())
            {
                log.info("---------- 处理分组 {} ----------", groupIndex);
                log.info("分组Key: {}", entry.getKey());

                List<SqlInfo> sqlInfos = entry.getValue();
                log.info("该分组包含 {} 条SQL", sqlInfos.size());

                if (sqlInfos.size() == 1)
                {
                    // 只有一条SQL，直接执行
                    SqlInfo sqlInfo = sqlInfos.get(0);
                    log.info("单条SQL，直接执行");
                    log.info("  表名: {}", sqlInfo.tableName);
                    log.info("  WHERE条件: {}", sqlInfo.whereClause);
                    log.info("  SELECT列: {}", sqlInfo.selectColumn);

                    Map<String, Object> sqlResult = executeSql(sqlInfo.originalSql, params);
                    log.info("执行结果（包含列名）: {}", sqlResult);

                    // 提取实际的列名（处理AS别名的情况）
                    String columnKey = extractActualColumnName(sqlInfo.selectColumn, sqlResult);
                    Object value = sqlResult.get(columnKey);
                    
                    // 将提取的值作为结果存储
                    resultMap.put(sqlInfo.originalSql, value);
                    log.info("  提取结果: {} -> {} (使用列名: {})", sqlInfo.selectColumn, value, columnKey);
                }
                else
                {
                    // 多条SQL，合并执行
                    log.info("多条SQL，进行合并");
                    for (int i = 0; i < sqlInfos.size(); i++)
                    {
                        SqlInfo info = sqlInfos.get(i);
                        log.info("  SQL[{}] - 表名: {}, WHERE: {}, SELECT: {}",
                                i + 1, info.tableName, info.whereClause, info.selectColumn);
                    }

                    String mergedSql = mergeSqls(sqlInfos);
                    log.info("合并后的SQL: {}", mergedSql);

                    Map<String, Object> mergedResult = executeMergedSql(mergedSql, params);
                    log.info("合并SQL执行结果: {}", mergedResult);

                    // 将合并结果分配到各个原始SQL
                    for (SqlInfo sqlInfo : sqlInfos)
                    {
                        // 提取实际的列名（处理AS别名的情况）
                        String columnKey = extractActualColumnName(sqlInfo.selectColumn, mergedResult);
                        Object value = mergedResult.get(columnKey);
                        resultMap.put(sqlInfo.originalSql, value);
                        log.info("  分配结果: {} -> {} (使用列名: {})", sqlInfo.selectColumn, value, columnKey);
                    }
                }

                groupIndex++;
            }

            log.info("========== SQL合并服务完成 ==========");
            log.info("最终结果Map包含 {} 个条目", resultMap.size());
            for (Map.Entry<String, Object> entry : resultMap.entrySet())
            {
                log.info("结果: {} = {}", entry.getKey(), entry.getValue());
            }
        }
        catch (Exception e)
        {
            log.error("========== SQL合并执行失败 ==========", e);
        }

        return resultMap;
    }

    /**
     * 将SQL按表名和WHERE条件分组
     */
    private Map<String, List<SqlInfo>> groupSqlsByTableAndWhere(List<String> sqlList) 
    {
        Map<String, List<SqlInfo>> groupedSqls = new HashMap<>();

        for (String sql : sqlList) 
        {
            try 
            {
                SqlInfo sqlInfo = parseSql(sql);
                if (sqlInfo != null) 
                {
                    String groupKey = sqlInfo.tableName + "||" + sqlInfo.whereClause;
                    groupedSqls.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(sqlInfo);
                }
            } 
            catch (Exception e) 
            {
                log.error("SQL解析失败: {}", sql, e);
            }
        }

        return groupedSqls;
    }

    /**
     * 解析SQL语句
     */
    private SqlInfo parseSql(String sql) throws JSQLParserException 
    {
        Statement statement = CCJSqlParserUtil.parse(sql);
        
        if (statement instanceof Select) 
        {
            Select selectStatement = (Select) statement;
            SelectBody selectBody = selectStatement.getSelectBody();
            
            if (selectBody instanceof PlainSelect) 
            {
                PlainSelect plainSelect = (PlainSelect) selectBody;
                
                SqlInfo sqlInfo = new SqlInfo();
                sqlInfo.originalSql = sql;
                
                // 获取表名
                FromItem fromItem = plainSelect.getFromItem();
                if (fromItem != null) 
                {
                    sqlInfo.tableName = fromItem.toString();
                }
                
                // 获取WHERE条件
                if (plainSelect.getWhere() != null) 
                {
                    sqlInfo.whereClause = plainSelect.getWhere().toString();
                } 
                else 
                {
                    sqlInfo.whereClause = "";
                }
                
                // 获取SELECT列（只取第一列）
                List<SelectItem> selectItems = plainSelect.getSelectItems();
                if (selectItems != null && !selectItems.isEmpty()) 
                {
                    sqlInfo.selectColumn = selectItems.get(0).toString();
                }
                
                return sqlInfo;
            }
        }
        
        return null;
    }

    /**
     * 合并多条SQL为一条
     */
    private String mergeSqls(List<SqlInfo> sqlInfos) 
    {
        if (sqlInfos.isEmpty()) 
        {
            return "";
        }

        // 收集所有SELECT列
        String columns = sqlInfos.stream()
                .map(info -> info.selectColumn)
                .distinct()
                .collect(Collectors.joining(", "));

        SqlInfo first = sqlInfos.get(0);
        
        StringBuilder mergedSql = new StringBuilder();
        mergedSql.append("SELECT ").append(columns);
        mergedSql.append(" FROM ").append(first.tableName);
        
        if (StringUtils.isNotEmpty(first.whereClause)) 
        {
            mergedSql.append(" WHERE ").append(first.whereClause);
        }

        return mergedSql.toString();
    }

    /**
     * 执行单条SQL
     * 返回Map，包含列名和值的映射关系
     */
    private Map<String, Object> executeSql(String sql, Map<String, Object> params)
    {
        Map<String, Object> resultMap = new HashMap<>();
        
        try
        {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(userDataSource);

            String originalSql = sql;
            // 替换SQL中的参数占位符
            if (params != null && !params.isEmpty())
            {
                for (Map.Entry<String, Object> entry : params.entrySet())
                {
                    sql = sql.replace(":" + entry.getKey(), "'" + entry.getValue() + "'");
                }
            }

            log.info("==> 准备执行SQL: {}", sql);
            if (!sql.equals(originalSql))
            {
                log.info("    (参数替换前: {})", originalSql);
            }

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            log.info("<== 查询返回 {} 行", results != null ? results.size() : 0);

            if (results != null && !results.isEmpty())
            {
                Map<String, Object> firstRow = results.get(0);
                log.info("    第一行数据（包含列名）: {}", firstRow);

                // 返回包含列名的完整Map
                resultMap.putAll(firstRow);
                log.info("    返回Map包含 {} 个列: {}", resultMap.size(), resultMap.keySet());
            }
        }
        catch (Exception e)
        {
            log.error("SQL执行失败: {}", sql, e);
        }

        return resultMap;
    }

    /**
     * 执行合并后的SQL
     */
    private Map<String, Object> executeMergedSql(String sql, Map<String, Object> params)
    {
        Map<String, Object> resultMap = new HashMap<>();

        try
        {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(userDataSource);

            String originalSql = sql;
            // 替换SQL中的参数占位符
            if (params != null && !params.isEmpty())
            {
                for (Map.Entry<String, Object> entry : params.entrySet())
                {
                    sql = sql.replace(":" + entry.getKey(), "'" + entry.getValue() + "'");
                }
            }

            log.info("==> 准备执行合并SQL: {}", sql);
            if (!sql.equals(originalSql))
            {
                log.info("    (参数替换前: {})", originalSql);
            }

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            log.info("<== 查询返回 {} 行", results != null ? results.size() : 0);

            if (results != null && !results.isEmpty())
            {
                Map<String, Object> firstRow = results.get(0);
                log.info("    第一行数据: {}", firstRow);
                resultMap.putAll(firstRow);
            }
        }
        catch (Exception e)
        {
            log.error("合并SQL执行失败: {}", sql, e);
        }

        return resultMap;
    }

    /**
     * 提取实际的列名（处理AS别名）
     * 支持不区分大小写的AS关键字
     * 
     * @param selectColumn SELECT列表达式，可能包含AS别名
     * @param resultMap 查询结果Map
     * @return 实际的列名
     */
    private String extractActualColumnName(String selectColumn, Map<String, Object> resultMap)
    {
        if (selectColumn == null || selectColumn.trim().isEmpty())
        {
            return selectColumn;
        }

        // 检查是否包含AS关键字（不区分大小写）
        // 使用正则表达式匹配 " AS " 或 " as " 或 " As " 等
        String trimmedColumn = selectColumn.trim();
        String pattern = "(?i)\\s+AS\\s+";  // (?i) 表示不区分大小写
        String[] parts = trimmedColumn.split(pattern);
        
        if (parts.length >= 2)
        {
            // 提取AS后面的别名（最后一部分）
            String alias = parts[parts.length - 1].trim();
            log.debug("  检测到AS别名: {} (原始表达式: {})", alias, selectColumn);
            
            // 检查别名是否在结果Map中存在
            if (resultMap.containsKey(alias))
            {
                log.debug("  使用别名作为列名: {}", alias);
                return alias;
            }
            else
            {
                log.warn("  别名 {} 在结果Map中不存在，可用的列名: {}", alias, resultMap.keySet());
            }
        }
        
        // 如果没有AS或者别名不存在，尝试直接用selectColumn
        if (resultMap.containsKey(selectColumn))
        {
            log.debug("  使用完整列表达式作为列名: {}", selectColumn);
            return selectColumn;
        }
        
        // 都不存在，返回原值（会导致取值为null，但在日志中会显示）
        log.warn("  列名 {} 在结果Map中不存在，可用的列名: {}", selectColumn, resultMap.keySet());
        return selectColumn;
    }

    /**
     * SQL信息内部类
     */
    private static class SqlInfo 
    {
        String originalSql;      // 原始SQL
        String tableName;        // 表名
        String whereClause;      // WHERE条件
        String selectColumn;     // SELECT列
    }
}


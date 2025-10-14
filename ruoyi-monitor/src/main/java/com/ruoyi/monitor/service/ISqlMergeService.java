package com.ruoyi.monitor.service;

import java.util.List;
import java.util.Map;

/**
 * SQL合并服务接口
 * 
 * @author ruoyi
 * @date 2025-01-15
 */
public interface ISqlMergeService 
{
    /**
     * 合并执行多个SQL语句
     * 将相同表、相同WHERE条件的SQL合并为一条执行
     * 
     * @param sqlList SQL语句列表
     * @return 执行结果Map，key为原始SQL，value为查询结果（单列结果）或Map（多列结果，包含列名）
     */
    public Map<String, Object> mergeAndExecute(List<String> sqlList);

    /**
     * 合并执行多个SQL语句（带参数）
     * 
     * @param sqlList SQL语句列表
     * @param params 参数Map
     * @return 执行结果Map，key为原始SQL，value为查询结果Map（包含列名和值）
     */
    public Map<String, Object> mergeAndExecute(List<String> sqlList, Map<String, Object> params);
}


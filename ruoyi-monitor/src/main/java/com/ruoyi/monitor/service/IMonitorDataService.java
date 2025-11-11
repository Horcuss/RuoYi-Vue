package com.ruoyi.monitor.service;

import java.util.Map;

/**
 * 监控数据获取Service接口
 * 
 * @author ruoyi
 * @date 2025-01-15
 */
public interface IMonitorDataService 
{
    /**
     * 根据配置KEY获取监控数据
     * 
     * @param configKey 监控配置KEY
     * @return 监控数据Map
     */
    public Map<String, Object> getMonitorData(String configKey);

    /**
     * 根据配置KEY和参数获取监控数据
     *
     * @param configKey 监控配置KEY
     * @param params 查询参数
     * @return 监控数据Map
     */
    public Map<String, Object> getMonitorData(String configKey, Map<String, Object> params);

    /**
     * 获取下拉框选项
     *
     * @param configKey 监控配置KEY
     * @param params 查询参数（包含用户输入的品名/lotno等）
     * @return 下拉框选项Map
     */
    public Map<String, Object> getSelectOptions(String configKey, Map<String, Object> params);
}


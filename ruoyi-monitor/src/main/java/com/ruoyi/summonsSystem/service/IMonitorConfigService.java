package com.ruoyi.monitor.service;

import java.util.List;
import com.ruoyi.monitor.domain.MonitorConfig;

/**
 * 监控配置Service接口
 * 
 * @author ruoyi
 * @date 2025-01-15
 */
public interface IMonitorConfigService 
{
    /**
     * 查询监控配置
     * 
     * @param configId 监控配置主键
     * @return 监控配置
     */
    public MonitorConfig selectMonitorConfigByConfigId(Long configId);

    /**
     * 根据配置KEY查询监控配置
     * 
     * @param configKey 监控配置KEY
     * @return 监控配置
     */
    public MonitorConfig selectMonitorConfigByConfigKey(String configKey);

    /**
     * 查询监控配置列表
     * 
     * @param monitorConfig 监控配置
     * @return 监控配置集合
     */
    public List<MonitorConfig> selectMonitorConfigList(MonitorConfig monitorConfig);

    /**
     * 新增监控配置
     * 
     * @param monitorConfig 监控配置
     * @return 结果
     */
    public int insertMonitorConfig(MonitorConfig monitorConfig);

    /**
     * 修改监控配置
     * 
     * @param monitorConfig 监控配置
     * @return 结果
     */
    public int updateMonitorConfig(MonitorConfig monitorConfig);

    /**
     * 批量删除监控配置
     * 
     * @param configIds 需要删除的监控配置主键集合
     * @return 结果
     */
    public int deleteMonitorConfigByConfigIds(Long[] configIds);

    /**
     * 删除监控配置信息
     * 
     * @param configId 监控配置主键
     * @return 结果
     */
    public int deleteMonitorConfigByConfigId(Long configId);

    /**
     * 校验配置KEY是否唯一
     * 
     * @param monitorConfig 监控配置信息
     * @return 结果
     */
    public boolean checkConfigKeyUnique(MonitorConfig monitorConfig);
}


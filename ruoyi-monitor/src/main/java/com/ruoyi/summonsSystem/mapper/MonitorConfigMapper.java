package com.ruoyi.monitor.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.monitor.domain.MonitorConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 监控配置Mapper接口
 * 使用MyBatis标准映射
 * 使用@DataSource注解指定monitor数据源
 *
 * @author ruoyi
 * @date 2025-01-15
 */
@DataSource(DataSourceType.MONITOR)  // 指定使用monitor数据源
public interface MonitorConfigMapper
{
    /**
     * 根据ID查询监控配置
     *
     * @param configId 监控配置ID
     * @return 监控配置
     */
    MonitorConfig selectById(@Param("configId") Long configId);

    /**
     * 根据配置KEY查询监控配置
     *
     * @param configKey 监控配置KEY
     * @return 监控配置
     */
    MonitorConfig selectMonitorConfigByConfigKey(@Param("configKey") String configKey);

    /**
     * 查询监控配置列表
     *
     * @param monitorConfig 监控配置
     * @return 监控配置集合
     */
    List<MonitorConfig> selectList(MonitorConfig monitorConfig);

    /**
     * 新增监控配置
     *
     * @param monitorConfig 监控配置
     * @return 结果
     */
    int insert(MonitorConfig monitorConfig);

    /**
     * 修改监控配置
     *
     * @param monitorConfig 监控配置
     * @return 结果
     */
    int updateById(MonitorConfig monitorConfig);

    /**
     * 删除监控配置
     *
     * @param configId 监控配置ID
     * @return 结果
     */
    int deleteById(@Param("configId") Long configId);

    /**
     * 批量删除监控配置
     *
     * @param configIds 需要删除的数据ID
     * @return 结果
     */
    int deleteBatchIds(@Param("configIds") List<Long> configIds);

    /**
     * 校验配置KEY是否唯一
     *
     * @param configKey 配置KEY
     * @param configId 配置ID（排除自己）
     * @return 结果
     */
    MonitorConfig checkConfigKeyUnique(@Param("configKey") String configKey, @Param("configId") Long configId);
}


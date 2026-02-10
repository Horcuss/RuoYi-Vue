package com.ruoyi.monitor.service.impl;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.monitor.mapper.MonitorConfigMapper;
import com.ruoyi.monitor.domain.MonitorConfig;
import com.ruoyi.monitor.service.IMonitorConfigService;

/**
 * 监控配置Service业务层处理
 * 使用MyBatis标准映射进行数据操作
 *
 * @author ruoyi
 * @date 2025-01-15
 */
@Service
public class MonitorConfigServiceImpl implements IMonitorConfigService
{
    @Autowired
    private MonitorConfigMapper monitorConfigMapper;

    /**
     * 查询监控配置
     *
     * @param configId 监控配置主键
     * @return 监控配置
     */
    @Override
    public MonitorConfig selectMonitorConfigByConfigId(Long configId)
    {
        return monitorConfigMapper.selectById(configId);
    }

    /**
     * 根据配置KEY查询监控配置
     *
     * @param configKey 配置KEY
     * @return 监控配置
     */
    @Override
    public MonitorConfig selectMonitorConfigByConfigKey(String configKey)
    {
        return monitorConfigMapper.selectMonitorConfigByConfigKey(configKey);
    }

    /**
     * 查询监控配置列表
     *
     * @param monitorConfig 监控配置
     * @return 监控配置
     */
    @Override
    public List<MonitorConfig> selectMonitorConfigList(MonitorConfig monitorConfig)
    {
        // 使用MyBatis标准方式，通过XML中的动态SQL处理查询条件
        return monitorConfigMapper.selectList(monitorConfig);
    }

    /**
     * 新增监控配置
     *
     * @param monitorConfig 监控配置
     * @return 结果
     */
    @Override
    public int insertMonitorConfig(MonitorConfig monitorConfig)
    {
        monitorConfig.setCreateTime(DateUtils.getNowDate());
        monitorConfig.setDelFlag("0");
        return monitorConfigMapper.insert(monitorConfig);
    }

    /**
     * 修改监控配置
     *
     * @param monitorConfig 监控配置
     * @return 结果
     */
    @Override
    public int updateMonitorConfig(MonitorConfig monitorConfig)
    {
        monitorConfig.setUpdateTime(DateUtils.getNowDate());
        return monitorConfigMapper.updateById(monitorConfig);
    }

    /**
     * 批量删除监控配置
     *
     * @param configIds 需要删除的监控配置主键
     * @return 结果
     */
    @Override
    public int deleteMonitorConfigByConfigIds(Long[] configIds)
    {
        // 逻辑删除：更新del_flag为2
        return monitorConfigMapper.deleteBatchIds(Arrays.asList(configIds));
    }

    /**
     * 删除监控配置信息
     *
     * @param configId 监控配置主键
     * @return 结果
     */
    @Override
    public int deleteMonitorConfigByConfigId(Long configId)
    {
        // 逻辑删除：更新del_flag为2
        return monitorConfigMapper.deleteById(configId);
    }

    /**
     * 校验配置KEY是否唯一
     *
     * @param monitorConfig 监控配置
     * @return 结果
     */
    @Override
    public boolean checkConfigKeyUnique(MonitorConfig monitorConfig)
    {
        Long configId = StringUtils.isNull(monitorConfig.getConfigId()) ? -1L : monitorConfig.getConfigId();
        MonitorConfig info = monitorConfigMapper.checkConfigKeyUnique(
            monitorConfig.getConfigKey(),
            configId
        );
        if (StringUtils.isNotNull(info) && info.getConfigId().longValue() != configId.longValue())
        {
            return false;
        }
        return true;
    }
}


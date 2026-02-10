package com.ruoyi.summonsSystem.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.summonsSystem.vo.SensorEventLog;

/**
 * 感应器事件日志Mapper接口
 */
@DataSource(DataSourceType.USER)
public interface SensorEventLogMapper
{
    int insert(SensorEventLog log);

    int updateProcessed(SensorEventLog log);
}

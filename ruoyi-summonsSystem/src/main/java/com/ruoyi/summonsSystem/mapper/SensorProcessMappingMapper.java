package com.ruoyi.summonsSystem.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.summonsSystem.vo.SensorProcessMapping;
import org.apache.ibatis.annotations.Param;

/**
 * 感应器工序映射Mapper接口
 */
@DataSource(DataSourceType.USER)
public interface SensorProcessMappingMapper
{
    SensorProcessMapping selectByIpAndPort(@Param("sensorIp") String sensorIp, @Param("sensorPort") String sensorPort);
}

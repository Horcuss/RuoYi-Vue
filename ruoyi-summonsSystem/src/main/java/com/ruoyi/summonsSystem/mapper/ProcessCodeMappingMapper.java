package com.ruoyi.summonsSystem.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.summonsSystem.vo.ProcessCodeMapping;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 工序code映射Mapper接口
 */
@DataSource(DataSourceType.USER)
public interface ProcessCodeMappingMapper
{
    ProcessCodeMapping selectByCode(@Param("processCode") String processCode);

    List<ProcessCodeMapping> selectAll();
}

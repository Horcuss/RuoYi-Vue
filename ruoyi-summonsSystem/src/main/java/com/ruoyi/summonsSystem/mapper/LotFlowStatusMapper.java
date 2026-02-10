package com.ruoyi.summonsSystem.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.summonsSystem.vo.LotFlowStatus;
import org.apache.ibatis.annotations.Param;

/**
 * lot流转状态Mapper接口
 */
@DataSource(DataSourceType.USER)
public interface LotFlowStatusMapper
{
    LotFlowStatus selectByLotNo(@Param("lotNo") String lotNo);

    int insert(LotFlowStatus status);

    int update(LotFlowStatus status);

    int deleteByLotNo(@Param("lotNo") String lotNo);
}

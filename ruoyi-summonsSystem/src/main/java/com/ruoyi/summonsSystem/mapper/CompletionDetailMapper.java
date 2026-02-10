package com.ruoyi.summonsSystem.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.summonsSystem.vo.CompletionDetail;
import org.apache.ibatis.annotations.Param;

/**
 * 完了输机明细Mapper接口
 */
@DataSource(DataSourceType.USER)
public interface CompletionDetailMapper
{
    CompletionDetail selectByLotAndProcess(@Param("lotNo") String lotNo, @Param("processCode") String processCode);

    int insert(CompletionDetail detail);

    int update(CompletionDetail detail);
}

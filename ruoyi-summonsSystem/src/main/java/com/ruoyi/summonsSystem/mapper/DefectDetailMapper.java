package com.ruoyi.summonsSystem.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.summonsSystem.vo.DefectDetail;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 不良入力明细Mapper接口
 */
@DataSource(DataSourceType.USER)
public interface DefectDetailMapper
{
    List<DefectDetail> selectByLotNo(@Param("lotNo") String lotNo);

    int insert(DefectDetail detail);
}

package com.ruoyi.summonsSystem.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.summonsSystem.vo.LotProcessSequence;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * lot最终工序序列Mapper接口
 */
@DataSource(DataSourceType.USER)
public interface LotProcessSequenceMapper
{
    List<LotProcessSequence> selectByLotNo(@Param("lotNo") String lotNo);

    LotProcessSequence selectByLotNoAndSeq(@Param("lotNo") String lotNo, @Param("seq") int seq);

    LotProcessSequence selectByLotNoAndProcessCode(@Param("lotNo") String lotNo, @Param("processCode") String processCode);

    int deleteByLotNo(@Param("lotNo") String lotNo);

    int batchInsert(@Param("list") List<LotProcessSequence> list);
}

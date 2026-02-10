package com.ruoyi.summonsSystem.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.summonsSystem.vo.EinkDisplayContent;
import org.apache.ibatis.annotations.Param;

/**
 * 水墨屏显示内容Mapper接口
 */
@DataSource(DataSourceType.USER)
public interface EinkDisplayContentMapper
{
    EinkDisplayContent selectByRfid(@Param("rfid") String rfid);

    EinkDisplayContent selectByLotNo(@Param("lotNo") String lotNo);

    int insert(EinkDisplayContent content);

    int updateByRfid(EinkDisplayContent content);

    int deleteByRfid(@Param("rfid") String rfid);
}

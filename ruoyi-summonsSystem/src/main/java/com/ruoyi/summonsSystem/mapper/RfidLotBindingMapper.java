package com.ruoyi.summonsSystem.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.summonsSystem.vo.RfidLotBinding;
import org.apache.ibatis.annotations.Param;

/**
 * RFID与lot绑定关系Mapper接口
 */
@DataSource(DataSourceType.USER)
public interface RfidLotBindingMapper
{
    RfidLotBinding selectActiveByRfid(@Param("rfid") String rfid);

    RfidLotBinding selectActiveByLotNo(@Param("lotNo") String lotNo);

    int unbindByRfid(@Param("rfid") String rfid);

    int insert(RfidLotBinding binding);
}

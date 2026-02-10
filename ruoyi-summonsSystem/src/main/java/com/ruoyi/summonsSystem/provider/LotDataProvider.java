package com.ruoyi.summonsSystem.provider;

import com.ruoyi.summonsSystem.vo.LotBasicInfo;

/**
 * lot基础数据接口
 * 数据来源：甲方的表，结构待定
 * 等甲方给了表结构后，写一个新的实现类替换MockLotDataProvider即可
 */
public interface LotDataProvider
{
    /**
     * 读取lot基础数据（品名、品番、客户等）
     *
     * @param lotNo lot编号
     * @return lot基础数据，查不到返回null
     */
    LotBasicInfo getLotInfo(String lotNo);
}

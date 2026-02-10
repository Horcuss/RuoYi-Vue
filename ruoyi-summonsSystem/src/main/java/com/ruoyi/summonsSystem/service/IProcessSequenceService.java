package com.ruoyi.summonsSystem.service;

import com.ruoyi.summonsSystem.vo.LotProcessSequence;
import java.util.List;

/**
 * 工序排列服务接口
 */
public interface IProcessSequenceService
{
    /**
     * 计算并保存lot的最终工序序列
     * 合并固定工序 + 额外工序
     *
     * @param lotNo lot编号
     * @param productName 品名（用于查额外工序）
     * @return 排好序的工序列表
     */
    List<LotProcessSequence> buildAndSave(String lotNo, String productName);

    /**
     * 查询lot的工序序列
     */
    List<LotProcessSequence> getByLotNo(String lotNo);
}

package com.ruoyi.summonsSystem.provider;

import com.ruoyi.summonsSystem.vo.ProcessStep;
import java.util.List;

/**
 * 工序数据接口
 * 数据来源：甲方提供的表，结构待定
 * 等甲方给了表结构后，写一个新的实现类替换MockProcessDataProvider即可
 */
public interface ProcessDataProvider
{
    /**
     * 读取某lot的固定工序列表（带顺序）
     *
     * @param lotNo lot编号
     * @return 固定工序列表，按顺序排列
     */
    List<ProcessStep> getFixedProcesses(String lotNo);

    /**
     * 读取某品名的额外工序列表
     *
     * @param productName 品名
     * @return 额外工序列表
     */
    List<ProcessStep> getExtraProcesses(String productName);
}

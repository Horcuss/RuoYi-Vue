package com.ruoyi.summonsSystem.service;

import com.ruoyi.summonsSystem.vo.DefectDetail;
import java.util.List;
import java.util.Map;

/**
 * 不良入力服务接口
 */
public interface IDefectService
{
    /**
     * 获取lot当前工序
     */
    Map<String, String> getCurrentProcess(String lotNo);

    /**
     * 提交不良数据
     */
    int submit(DefectDetail detail);

    /**
     * 查询lot的不良明细列表（含合计）
     */
    Map<String, Object> getDefectList(String lotNo);
}

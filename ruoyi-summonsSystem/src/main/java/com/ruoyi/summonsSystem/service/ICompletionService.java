package com.ruoyi.summonsSystem.service;

import com.ruoyi.summonsSystem.vo.CompletionDetail;

/**
 * 完了输机服务接口
 */
public interface ICompletionService
{
    /**
     * 提交完了输机数据（存在则更新，不存在则新增）
     *
     * @param detail 完了输机数据
     * @return 影响行数
     */
    int submit(CompletionDetail detail);
}

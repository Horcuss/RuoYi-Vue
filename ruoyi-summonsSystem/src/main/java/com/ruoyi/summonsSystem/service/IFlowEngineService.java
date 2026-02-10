package com.ruoyi.summonsSystem.service;

import com.ruoyi.summonsSystem.flow.FlowResult;
import com.ruoyi.summonsSystem.flow.UnifiedFlowEvent;

/**
 * 流转引擎服务接口
 */
public interface IFlowEngineService
{
    /**
     * 处理统一流转事件
     *
     * @param event 统一流转事件
     * @return 流转结果
     */
    FlowResult processEvent(UnifiedFlowEvent event);
}

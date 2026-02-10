package com.ruoyi.summonsSystem.flow.adapter;

import com.ruoyi.summonsSystem.flow.UnifiedFlowEvent;

/**
 * 流转事件适配器接口
 * 不同事件源（WS感应器、扫码枪、手动录入等）实现此接口，
 * 将原始数据转为统一的 UnifiedFlowEvent
 */
public interface FlowEventAdapter
{
    /**
     * 适配器名称
     */
    String getName();

    /**
     * 将原始数据转为统一流转事件
     *
     * @param rawData 原始数据（JSON字符串）
     * @return 统一流转事件，如果数据不属于该适配器或无法解析则返回null
     */
    UnifiedFlowEvent adapt(String rawData);
}

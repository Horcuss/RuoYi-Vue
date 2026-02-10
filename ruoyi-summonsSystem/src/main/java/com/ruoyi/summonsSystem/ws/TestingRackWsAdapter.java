package com.ruoyi.summonsSystem.ws;

import com.ruoyi.summonsSystem.flow.FlowResult;
import com.ruoyi.summonsSystem.flow.UnifiedFlowEvent;
import com.ruoyi.summonsSystem.mapper.LotFlowStatusMapper;
import com.ruoyi.summonsSystem.service.IFlowEngineService;
import com.ruoyi.summonsSystem.vo.LotFlowStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 测定棚架WS适配器
 *
 * 职责：将测定棚架发来的原始WS消息转换为UnifiedFlowEvent，交给流转引擎处理。
 *
 * 目前为占位实现，等甲方提供具体WS格式后，只需修改 parseRawMessage 方法的解析逻辑。
 * 其他方法（onMessage、resolveProcessCode）不需要改。
 *
 * 可能的消息来源（甲方还没确定）：
 * 1. 棚架网关IP发来的消息
 * 2. 棚架内部感应器IP发来的消息
 * 3. 两者都有
 *
 * 不管哪种，最终都通过此适配器统一转换为UnifiedFlowEvent。
 */
@Component
public class TestingRackWsAdapter
{
    private static final Logger log = LoggerFactory.getLogger(TestingRackWsAdapter.class);

    @Autowired
    private IFlowEngineService flowEngineService;

    @Autowired
    private LotFlowStatusMapper lotFlowStatusMapper;

    /**
     * WS收到测定棚架消息时调用此方法
     * 将来WS Handler里直接调: testingRackWsAdapter.onMessage(rawMessage)
     *
     * @param rawMessage 原始WS消息
     * @return 流转结果
     */
    public FlowResult onMessage(String rawMessage)
    {
        log.info("测定棚架WS收到消息: {}", rawMessage);

        UnifiedFlowEvent event = parseRawMessage(rawMessage);
        if (event == null)
        {
            log.warn("无法解析测定棚架消息: {}", rawMessage);
            return FlowResult.ignored("无法解析测定棚架消息");
        }

        return flowEngineService.processEvent(event);
    }

    /**
     * 解析原始WS消息 → UnifiedFlowEvent
     *
     * ★ 等甲方提供具体格式后，只改这个方法 ★
     *
     * 目前假设格式: lotNo|eventType
     * 例: "LOT20260210001|IN" 或 "LOT20260210001|OUT"
     *
     * 实际可能是JSON、二进制、或其他格式，到时候替换解析逻辑即可。
     */
    private UnifiedFlowEvent parseRawMessage(String rawMessage)
    {
        // TODO: 等甲方提供具体格式后替换解析逻辑
        if (rawMessage == null || rawMessage.isEmpty())
        {
            return null;
        }

        String[] parts = rawMessage.split("\\|");
        if (parts.length < 2)
        {
            return null;
        }

        String lotNo = parts[0].trim();
        String eventType = parts[1].trim().toUpperCase();

        if (!"IN".equals(eventType) && !"OUT".equals(eventType))
        {
            return null;
        }

        // 获取测定工序code
        String processCode = resolveTestingProcessCode(lotNo);
        if (processCode == null)
        {
            log.warn("无法确定lot={}的测定工序code，请先通过正常流转进入测定工序", lotNo);
            return null;
        }

        UnifiedFlowEvent event = new UnifiedFlowEvent();
        event.setLotNo(lotNo);
        event.setProcessCode(processCode);
        event.setEventType(eventType);
        event.setTestingEvent(true);
        event.setTimestamp(new Date());
        event.setSource("TESTING_RACK_WS");
        event.setRawData(rawMessage);

        return event;
    }

    /**
     * 获取该lot的测定工序code
     * 优先从flow_status中读取（之前已记录过），如果没有则需要人工先走一次流转
     */
    private String resolveTestingProcessCode(String lotNo)
    {
        LotFlowStatus status = lotFlowStatusMapper.selectByLotNo(lotNo);
        if (status != null && status.getTestingProcessCode() != null)
        {
            return status.getTestingProcessCode();
        }
        // 还没有记录过测定工序code，需要先通过测试页面或正常流转触发一次
        return null;
    }
}

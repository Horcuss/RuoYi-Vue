package com.ruoyi.summonsSystem.service.impl;

import com.ruoyi.summonsSystem.flow.FlowResult;
import com.ruoyi.summonsSystem.flow.UnifiedFlowEvent;
import com.ruoyi.summonsSystem.mapper.LotFlowStatusMapper;
import com.ruoyi.summonsSystem.mapper.LotProcessSequenceMapper;
import com.ruoyi.summonsSystem.mapper.ProcessCodeMappingMapper;
import com.ruoyi.summonsSystem.service.IDisplayService;
import com.ruoyi.summonsSystem.service.IFlowEngineService;
import com.ruoyi.summonsSystem.vo.LotFlowStatus;
import com.ruoyi.summonsSystem.vo.LotProcessSequence;
import com.ruoyi.summonsSystem.vo.ProcessCodeMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 流转引擎服务实现
 * 核心状态机逻辑，处理工序流转判定
 *
 * 流转规则：
 * 1. 普通工序：只能走序列中的下一工序(seq+1)
 * 2. 第1回测定：按序列走(seq+1)，即 测定工序→...→3500(真空热处理)
 * 3. 真空热处理(3500)：除了seq+1，还允许回到测定工序(第2回)
 * 4. 第2回以降测定：OUT再IN=新一回(自循环)，或直接走3500之后的工序(退出测定)
 * 5. 测定回数 = 进入测定棚的次数，最大10回
 * 6. 测定工序由事件的 testingEvent 标志识别，不依赖工序code格式
 */
@Service
@DataSource(DataSourceType.USER)
public class FlowEngineServiceImpl implements IFlowEngineService
{
    private static final Logger log = LoggerFactory.getLogger(FlowEngineServiceImpl.class);

    /** 真空热处理工序code（测定循环的回退点，有测定就必走真空） */
    private static final String PROCESS_VACUUM = "3500";
    /** 测定工序code（后续可改为从配置表/数据库读取） */
    private static final String PROCESS_TESTING = "3300";
    /** 最大测定回数 */
    private static final int MAX_TESTING_ROUND = 10;

    @Autowired
    private LotFlowStatusMapper lotFlowStatusMapper;

    @Autowired
    private LotProcessSequenceMapper lotProcessSequenceMapper;

    @Autowired
    private ProcessCodeMappingMapper processCodeMappingMapper;

    @Autowired
    private IDisplayService displayService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlowResult processEvent(UnifiedFlowEvent event)
    {
        log.info("流转引擎收到事件: {}", event);

        String lotNo = event.getLotNo();
        String eventProcessCode = event.getProcessCode();
        String eventType = event.getEventType();
        boolean isTestingEvent = event.isTestingEvent();

        // 获取当前流转状态
        LotFlowStatus flowStatus = lotFlowStatusMapper.selectByLotNo(lotNo);
        if (flowStatus == null)
        {
            log.warn("lot未初始化流转状态，忽略: lotNo={}", lotNo);
            return FlowResult.ignored("lot未初始化流转状态");
        }

        String currentProcessCode = flowStatus.getCurrentProcessCode();
        boolean hasExited = flowStatus.getHasExited() == 1;
        boolean isAbnormal = "ABNORMAL".equals(flowStatus.getStatus());
        String testingProcessCode = flowStatus.getTestingProcessCode();

        // 测定事件且还没记录过测定工序code → 提前记录，确保后续validNext计算能包含loopback
        if (isTestingEvent && testingProcessCode == null)
        {
            flowStatus.setTestingProcessCode(eventProcessCode);
            testingProcessCode = eventProcessCode;
            log.info("lot={} 提前记录测定工序code: {}", lotNo, eventProcessCode);
        }

        // 获取合法的下一工序集合
        Set<String> validNextCodes = getValidNextProcessCodes(flowStatus);

        // 异常状态下的恢复逻辑
        if (isAbnormal)
        {
            if (UnifiedFlowEvent.EVENT_OUT.equals(eventType))
            {
                return FlowResult.ignored("lot处于异常状态，等待回到正确工序");
            }

            if (eventProcessCode.equals(currentProcessCode))
            {
                // 回到了异常前的工序 → 恢复正常
                flowStatus.setStatus("NORMAL");
                flowStatus.setAbnormalMsg(null);
                flowStatus.setHasExited(0);
                lotFlowStatusMapper.update(flowStatus);
                displayService.refreshDisplay(lotNo, currentProcessCode,
                    getProcessName(currentProcessCode));
                log.info("lot={} 异常恢复: 回到工序 {}", lotNo, currentProcessCode);
                return FlowResult.recovered("异常恢复: 回到工序 " + currentProcessCode);
            }
            else if (validNextCodes.contains(eventProcessCode))
            {
                // 进入了合法的下一工序 → 恢复并流转
                flowStatus.setStatus("NORMAL");
                flowStatus.setAbnormalMsg(null);
                flowStatus.setHasExited(1);
                FlowResult result = doTransition(flowStatus, eventProcessCode, isTestingEvent);
                flowStatus.setLastEventType(eventType);
                flowStatus.setLastEventTime(event.getTimestamp());
                lotFlowStatusMapper.update(flowStatus);
                log.info("lot={} 异常恢复并流转到 {}", lotNo, eventProcessCode);
                return result;
            }
            else
            {
                log.info("lot={} 异常状态，等待回到正确工序，忽略: {}", lotNo, eventProcessCode);
                return FlowResult.ignored("lot处于异常状态，等待回到正确工序(当前应在" + currentProcessCode + ")");
            }
        }

        FlowResult result;

        if (UnifiedFlowEvent.EVENT_OUT.equals(eventType))
        {
            result = handleOutEvent(flowStatus, eventProcessCode, currentProcessCode, hasExited);
        }
        else
        {
            result = handleInEvent(flowStatus, eventProcessCode, currentProcessCode, hasExited,
                                   validNextCodes, isTestingEvent, testingProcessCode);
        }

        // 更新最后事件信息
        flowStatus.setLastEventType(eventType);
        flowStatus.setLastEventTime(event.getTimestamp());
        lotFlowStatusMapper.update(flowStatus);

        return result;
    }

    /**
     * 处理OUT事件
     */
    private FlowResult handleOutEvent(LotFlowStatus flowStatus, String eventProcessCode,
                                       String currentProcessCode, boolean hasExited)
    {
        if (eventProcessCode.equals(currentProcessCode))
        {
            if (!hasExited)
            {
                flowStatus.setHasExited(1);
                log.info("lot={} 离开工序 {}", flowStatus.getLotNo(), currentProcessCode);
                return FlowResult.exited("离开工序: " + currentProcessCode);
            }
            else
            {
                return FlowResult.ignored("重复OUT信号，忽略");
            }
        }
        else
        {
            return FlowResult.ignored("非当前工序OUT，忽略");
        }
    }

    /**
     * 处理IN事件
     */
    private FlowResult handleInEvent(LotFlowStatus flowStatus, String eventProcessCode,
                                      String currentProcessCode, boolean hasExited,
                                      Set<String> validNextCodes, boolean isTestingEvent,
                                      String testingProcessCode)
    {
        String lotNo = flowStatus.getLotNo();

        // 测定棚自循环：2回目以降，已出测定棚，再次进入同一测定工序 → 新的一回
        if (isTestingEvent && testingProcessCode != null
            && eventProcessCode.equals(testingProcessCode)
            && eventProcessCode.equals(currentProcessCode)
            && hasExited && flowStatus.getTestingRound() >= 2)
        {
            return doTransition(flowStatus, eventProcessCode, true);
        }

        if (eventProcessCode.equals(currentProcessCode))
        {
            if (hasExited)
            {
                flowStatus.setHasExited(0);
                log.info("lot={} 回到工序 {} (恢复为未离开)", lotNo, currentProcessCode);
                return FlowResult.ignored("回到当前工序，恢复为未离开");
            }
            else
            {
                return FlowResult.ignored("重复IN信号，忽略");
            }
        }

        // 事件的工序不是当前工序
        if (validNextCodes.contains(eventProcessCode))
        {
            if (!hasExited)
            {
                String abnormalMsg = "未出" + currentProcessCode + "就进入" + eventProcessCode;
                flowStatus.setStatus("ABNORMAL");
                flowStatus.setAbnormalMsg(abnormalMsg);
                displayService.showAbnormal(lotNo, abnormalMsg);
                log.error("lot={} 异常: {}", lotNo, abnormalMsg);
                return FlowResult.abnormal(abnormalMsg);
            }

            // 正常流转
            return doTransition(flowStatus, eventProcessCode, isTestingEvent);
        }
        else
        {
            if (hasExited)
            {
                String abnormalMsg = "跳工序: 从" + currentProcessCode + "到" + eventProcessCode;
                flowStatus.setStatus("ABNORMAL");
                flowStatus.setAbnormalMsg(abnormalMsg);
                displayService.showAbnormal(lotNo, abnormalMsg);
                log.error("lot={} 异常: {}", lotNo, abnormalMsg);
                return FlowResult.abnormal(abnormalMsg);
            }
            else
            {
                return FlowResult.ignored("非相关工序IN，忽略");
            }
        }
    }

    /**
     * 执行工序流转
     */
    private FlowResult doTransition(LotFlowStatus flowStatus, String eventProcessCode,
                                     boolean isTestingEvent)
    {
        String lotNo = flowStatus.getLotNo();
        String actualProcessCode = eventProcessCode;
        String processName;

        // 判断是否为测定工序流转（测定棚事件 或 正常流转首次进入测定工序）
        boolean isTestingTransition = isTestingEvent;
        if (!isTestingTransition && flowStatus.getTestingProcessCode() == null
            && PROCESS_TESTING.equals(actualProcessCode))
        {
            isTestingTransition = true;
        }

        if (isTestingTransition)
        {
            // 记录测定工序code
            if (flowStatus.getTestingProcessCode() == null)
            {
                flowStatus.setTestingProcessCode(eventProcessCode);
            }

            // 测定回数+1
            int newRound = determineTestingRound(flowStatus);
            flowStatus.setTestingRound(newRound);

            ProcessCodeMapping mapping = processCodeMappingMapper.selectByCode(actualProcessCode);
            processName = (mapping != null) ? mapping.getProcessName() : actualProcessCode;
            // 显示名附加回数
            processName = processName + "(" + newRound + "回)";
        }
        else
        {
            ProcessCodeMapping mapping = processCodeMappingMapper.selectByCode(actualProcessCode);
            processName = (mapping != null) ? mapping.getProcessName() : actualProcessCode;
        }

        // 查找新工序在序列中的位置
        LotProcessSequence seqRecord = lotProcessSequenceMapper.selectByLotNoAndProcessCode(lotNo, actualProcessCode);
        int newSeq = (seqRecord != null) ? seqRecord.getSeq() : flowStatus.getCurrentSeq();

        // 更新流转状态
        flowStatus.setCurrentProcessCode(actualProcessCode);
        flowStatus.setCurrentSeq(newSeq);
        flowStatus.setHasExited(0);
        flowStatus.setAbnormalMsg(null);

        // 刷新水墨屏
        displayService.refreshDisplay(lotNo, actualProcessCode, processName);

        log.info("lot={} 流转到工序 {}({})", lotNo, processName, actualProcessCode);
        return FlowResult.transitioned(actualProcessCode, processName);
    }

    /**
     * 确定测定回数（每次进入测定棚+1，最大10回）
     */
    private int determineTestingRound(LotFlowStatus flowStatus)
    {
        int currentRound = flowStatus.getTestingRound();
        int newRound = currentRound + 1;
        if (newRound > MAX_TESTING_ROUND)
        {
            log.warn("lot={} 测定回数超过最大值{}，保持{}", flowStatus.getLotNo(), MAX_TESTING_ROUND, MAX_TESTING_ROUND);
            return MAX_TESTING_ROUND;
        }
        return newRound;
    }

    /**
     * 获取合法的下一工序code集合
     *
     * 规则：
     * 1. 第1回测定：按序列走seq+1(如 测定→3410→3500)
     * 2. 第2回以降测定：退出测定→3500之后的工序（自循环在handleInEvent中处理）
     * 3. 真空热处理(3500)：seq+1(退出测定) 或 回到测定工序(循环)
     * 4. 其他工序：seq+1
     */
    private Set<String> getValidNextProcessCodes(LotFlowStatus flowStatus)
    {
        Set<String> validCodes = new HashSet<>();
        String lotNo = flowStatus.getLotNo();
        String currentCode = flowStatus.getCurrentProcessCode();
        int currentSeq = flowStatus.getCurrentSeq();
        String testingProcessCode = flowStatus.getTestingProcessCode();

        // 第2回以降的测定：退出测定循环→3500之后的工序
        if (testingProcessCode != null && currentCode.equals(testingProcessCode)
            && flowStatus.getTestingRound() >= 2)
        {
            LotProcessSequence vacuumSeq = lotProcessSequenceMapper.selectByLotNoAndProcessCode(lotNo, PROCESS_VACUUM);
            if (vacuumSeq != null)
            {
                LotProcessSequence afterVacuum = lotProcessSequenceMapper.selectByLotNoAndSeq(lotNo, vacuumSeq.getSeq() + 1);
                if (afterVacuum != null)
                {
                    validCodes.add(afterVacuum.getProcessCode());
                }
            }
            return validCodes;
        }

        // 通用规则：序列中的下一工序
        LotProcessSequence next = lotProcessSequenceMapper.selectByLotNoAndSeq(lotNo, currentSeq + 1);
        if (next != null)
        {
            validCodes.add(next.getProcessCode());
        }

        // 真空热处理：额外允许回到测定工序（形成测定循环）
        if (PROCESS_VACUUM.equals(currentCode) && testingProcessCode != null)
        {
            validCodes.add(testingProcessCode);
        }

        return validCodes;
    }

    /**
     * 查询工序中文名
     */
    private String getProcessName(String processCode)
    {
        ProcessCodeMapping mapping = processCodeMappingMapper.selectByCode(processCode);
        return (mapping != null) ? mapping.getProcessName() : processCode;
    }
}

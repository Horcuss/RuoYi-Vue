package com.ruoyi.summonsSystem.provider.impl;

import com.ruoyi.summonsSystem.provider.ProcessDataProvider;
import com.ruoyi.summonsSystem.vo.ProcessStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 工序数据 - Mock实现（测试用）
 * 等甲方给了表结构后，写一个新的实现类替换即可
 */
@Component
public class MockProcessDataProvider implements ProcessDataProvider
{
    private static final Logger log = LoggerFactory.getLogger(MockProcessDataProvider.class);

    /**
     * 模拟固定工序（所有lot共用一套默认固定工序，实际可按lot区分）
     */
    private static final List<ProcessStep> DEFAULT_FIXED_PROCESSES = new ArrayList<>();

    /**
     * 模拟额外工序（特殊品名才有额外工序）
     */
    private static final Map<String, List<ProcessStep>> EXTRA_PROCESSES_MAP = new HashMap<>();

    static
    {
        // 默认固定工序（注意3410在3500后面，不是按数字排序的）
        DEFAULT_FIXED_PROCESSES.add(new ProcessStep("2350", "dh处理", 1));
        DEFAULT_FIXED_PROCESSES.add(new ProcessStep("2400", "电镀", 2));
        DEFAULT_FIXED_PROCESSES.add(new ProcessStep("2500", "电镀后热处理", 3));
        DEFAULT_FIXED_PROCESSES.add(new ProcessStep("3300", "一回测定", 4));
        DEFAULT_FIXED_PROCESSES.add(new ProcessStep("3410", "后始末", 5));
        DEFAULT_FIXED_PROCESSES.add(new ProcessStep("3500", "真空热处理", 6));
        DEFAULT_FIXED_PROCESSES.add(new ProcessStep("3550", "mips", 7));
        DEFAULT_FIXED_PROCESSES.add(new ProcessStep("3600", "g2外选", 8));
        DEFAULT_FIXED_PROCESSES.add(new ProcessStep("4100", "gp", 9));
        DEFAULT_FIXED_PROCESSES.add(new ProcessStep("4400", "磁石吸取", 10));
        DEFAULT_FIXED_PROCESSES.add(new ProcessStep("4500", "清空", 11));

        // 特殊品名"特殊产品X"的额外工序
        List<ProcessStep> extraForX = new ArrayList<>();
        extraForX.add(new ProcessStep("2450", "电镀后外选", null));
        extraForX.add(new ProcessStep("3650", "g2外选后检查", null));
        EXTRA_PROCESSES_MAP.put("特殊产品X", extraForX);
    }

    @Override
    public List<ProcessStep> getFixedProcesses(String lotNo)
    {
        log.info("[Mock] 查询固定工序: lotNo={}", lotNo);
        // 返回副本避免被修改
        List<ProcessStep> result = new ArrayList<>();
        for (ProcessStep step : DEFAULT_FIXED_PROCESSES)
        {
            result.add(new ProcessStep(step.getProcessCode(), step.getProcessName(), step.getSeq()));
        }
        return result;
    }

    @Override
    public List<ProcessStep> getExtraProcesses(String productName)
    {
        log.info("[Mock] 查询额外工序: productName={}", productName);
        List<ProcessStep> extras = EXTRA_PROCESSES_MAP.get(productName);
        if (extras == null)
        {
            return new ArrayList<>();
        }
        // 返回副本
        List<ProcessStep> result = new ArrayList<>();
        for (ProcessStep step : extras)
        {
            result.add(new ProcessStep(step.getProcessCode(), step.getProcessName(), step.getSeq()));
        }
        return result;
    }
}

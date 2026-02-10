package com.ruoyi.summonsSystem.service.impl;

import com.ruoyi.summonsSystem.mapper.LotProcessSequenceMapper;
import com.ruoyi.summonsSystem.provider.ProcessDataProvider;
import com.ruoyi.summonsSystem.service.IProcessSequenceService;
import com.ruoyi.summonsSystem.vo.LotProcessSequence;
import com.ruoyi.summonsSystem.vo.ProcessStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 工序排列服务实现
 * 核心算法：将额外工序插入到固定工序中（不是排序，是插入）
 */
@Service
@DataSource(DataSourceType.USER)
public class ProcessSequenceServiceImpl implements IProcessSequenceService
{
    private static final Logger log = LoggerFactory.getLogger(ProcessSequenceServiceImpl.class);

    @Autowired
    private ProcessDataProvider processDataProvider;

    @Autowired
    private LotProcessSequenceMapper lotProcessSequenceMapper;

    @Override
    public List<LotProcessSequence> buildAndSave(String lotNo, String productName)
    {
        // 1. 获取固定工序
        List<ProcessStep> fixedSteps = processDataProvider.getFixedProcesses(lotNo);
        log.info("lot={} 固定工序数量: {}", lotNo, fixedSteps.size());

        // 2. 获取额外工序
        List<ProcessStep> extraSteps = processDataProvider.getExtraProcesses(productName);
        log.info("lot={} 额外工序数量: {} (品名={})", lotNo, extraSteps.size(), productName);

        // 3. 合并工序
        List<ProcessStep> merged = mergeProcesses(fixedSteps, extraSteps);
        log.info("lot={} 合并后工序数量: {}", lotNo, merged.size());

        // 4. 删除旧数据
        lotProcessSequenceMapper.deleteByLotNo(lotNo);

        // 5. 构建实体并批量插入
        List<LotProcessSequence> sequences = new ArrayList<>();
        for (int i = 0; i < merged.size(); i++)
        {
            ProcessStep step = merged.get(i);
            LotProcessSequence seq = new LotProcessSequence();
            seq.setLotNo(lotNo);
            seq.setProcessCode(step.getProcessCode());
            seq.setProcessName(step.getProcessName());
            seq.setSeq(i + 1);
            seq.setProcessSource(step.getSeq() != null ? "fixed" : "extra");
            sequences.add(seq);
        }

        if (!sequences.isEmpty())
        {
            lotProcessSequenceMapper.batchInsert(sequences);
        }

        return sequences;
    }

    @Override
    public List<LotProcessSequence> getByLotNo(String lotNo)
    {
        return lotProcessSequenceMapper.selectByLotNo(lotNo);
    }

    /**
     * 合并算法：将额外工序插入固定工序中
     * 固定工序顺序不可变，额外工序根据code数值插入到相邻固定工序之间
     */
    private List<ProcessStep> mergeProcesses(List<ProcessStep> fixed, List<ProcessStep> extra)
    {
        if (extra == null || extra.isEmpty())
        {
            return new ArrayList<>(fixed);
        }

        // 用LinkedList方便插入，这里用ArrayList模拟
        List<ProcessStep> result = new ArrayList<>(fixed);

        for (ProcessStep extraStep : extra)
        {
            int extraCode = parseCode(extraStep.getProcessCode());
            if (extraCode < 0)
            {
                // 非数字code，追加到末尾
                result.add(extraStep);
                continue;
            }

            boolean inserted = false;

            // 从前往后遍历，找到合适的插入位置
            for (int i = 0; i < result.size() - 1; i++)
            {
                int curCode = parseCode(result.get(i).getProcessCode());
                int nextCode = parseCode(result.get(i + 1).getProcessCode());

                if (curCode >= 0 && nextCode >= 0 && curCode < extraCode && extraCode < nextCode)
                {
                    result.add(i + 1, extraStep);
                    inserted = true;
                    break;
                }
            }

            if (!inserted)
            {
                // 检查是否小于第一个
                int firstCode = parseCode(result.get(0).getProcessCode());
                if (firstCode >= 0 && extraCode < firstCode)
                {
                    result.add(0, extraStep);
                }
                else
                {
                    // 大于最后一个或无法插入，追加到末尾
                    result.add(extraStep);
                }
            }
        }

        return result;
    }

    /**
     * 尝试将工序code解析为数字，非数字返回-1
     */
    private int parseCode(String processCode)
    {
        if (processCode == null)
        {
            return -1;
        }
        try
        {
            return Integer.parseInt(processCode);
        }
        catch (NumberFormatException e)
        {
            return -1;
        }
    }
}

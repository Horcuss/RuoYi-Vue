package com.ruoyi.summonsSystem.service.impl;

import com.ruoyi.summonsSystem.mapper.*;
import com.ruoyi.summonsSystem.provider.LotDataProvider;
import com.ruoyi.summonsSystem.service.IDisplayService;
import com.ruoyi.summonsSystem.service.IEinkBindService;
import com.ruoyi.summonsSystem.service.IProcessSequenceService;
import com.ruoyi.summonsSystem.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 水墨屏绑定服务实现
 */
@Service
@DataSource(DataSourceType.USER)
public class EinkBindServiceImpl implements IEinkBindService
{
    private static final Logger log = LoggerFactory.getLogger(EinkBindServiceImpl.class);

    /** gp工序code */
    private static final String GP_PROCESS_CODE = "4100";

    @Autowired
    private LotDataProvider lotDataProvider;

    @Autowired
    private IProcessSequenceService processSequenceService;

    @Autowired
    private IDisplayService displayService;

    @Autowired
    private ProcessCodeMappingMapper processCodeMappingMapper;

    @Autowired
    private RfidLotBindingMapper rfidLotBindingMapper;

    @Autowired
    private EinkDisplayContentMapper einkDisplayContentMapper;

    @Autowired
    private CompletionDetailMapper completionDetailMapper;

    @Autowired
    private LotFlowStatusMapper lotFlowStatusMapper;

    @Autowired
    private LotProcessSequenceMapper lotProcessSequenceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> bind(String rfid, String lotNo, String processCode)
    {
        log.info("开始绑定: rfid={}, lotNo={}, processCode={}", rfid, lotNo, processCode);

        // 1. 解绑该rfid的旧绑定
        rfidLotBindingMapper.unbindByRfid(rfid);

        // 2. 获取lot基础数据
        LotBasicInfo lotInfo = lotDataProvider.getLotInfo(lotNo);
        if (lotInfo == null)
        {
            throw new RuntimeException("未找到lot基础数据: " + lotNo);
        }

        // 3. 查询工序中文名
        ProcessCodeMapping mapping = processCodeMappingMapper.selectByCode(processCode);
        String processName = (mapping != null) ? mapping.getProcessName() : processCode;

        // 4. 工序排列服务 - 生成lot最终工序序列
        List<LotProcessSequence> sequences = processSequenceService.buildAndSave(lotNo, lotInfo.getProductName());

        // 5. 查询gp不良指示
        String gpDefectInfo = null;
        CompletionDetail gpCompletion = completionDetailMapper.selectByLotAndProcess(lotNo, GP_PROCESS_CODE);
        if (gpCompletion != null)
        {
            gpDefectInfo = gpCompletion.getDefectInfo();
        }

        // 6. 删除旧的显示内容，插入新的
        einkDisplayContentMapper.deleteByRfid(rfid);

        EinkDisplayContent content = new EinkDisplayContent();
        content.setRfid(rfid);
        content.setLotNo(lotNo);
        content.setProductName(lotInfo.getProductName());
        content.setProductModel(lotInfo.getProductModel());
        content.setCustomer(lotInfo.getCustomer());
        content.setCurrentProcessCode(processCode);
        content.setCurrentProcessName(processName);
        content.setGpDefectInfo(gpDefectInfo);
        content.setStatus("0");
        einkDisplayContentMapper.insert(content);

        // 7. 创建rfid绑定关系
        RfidLotBinding binding = new RfidLotBinding();
        binding.setRfid(rfid);
        binding.setLotNo(lotNo);
        rfidLotBindingMapper.insert(binding);

        // 8. 初始化/更新lot流转状态
        int seq = 1;
        LotProcessSequence currentSeqRecord = lotProcessSequenceMapper.selectByLotNoAndProcessCode(lotNo, processCode);
        if (currentSeqRecord != null)
        {
            seq = currentSeqRecord.getSeq();
        }

        LotFlowStatus flowStatus = new LotFlowStatus();
        flowStatus.setLotNo(lotNo);
        flowStatus.setCurrentProcessCode(processCode);
        flowStatus.setCurrentSeq(seq);
        flowStatus.setHasExited(0);
        flowStatus.setTestingRound(0);
        flowStatus.setStatus("NORMAL");
        lotFlowStatusMapper.insert(flowStatus);

        // 9. 调用水墨屏API推送（预留接口）
        displayService.pushToEinkScreen(rfid, null);

        // 10. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("lotNo", lotNo);
        result.put("processName", processName);
        result.put("productName", lotInfo.getProductName());
        result.put("sequenceCount", sequences.size());

        log.info("绑定成功: rfid={}, lotNo={}, 工序={}", rfid, lotNo, processName);
        return result;
    }
}

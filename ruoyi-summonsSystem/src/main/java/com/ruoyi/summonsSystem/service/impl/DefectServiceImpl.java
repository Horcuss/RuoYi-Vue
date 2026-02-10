package com.ruoyi.summonsSystem.service.impl;

import com.ruoyi.summonsSystem.mapper.DefectDetailMapper;
import com.ruoyi.summonsSystem.mapper.EinkDisplayContentMapper;
import com.ruoyi.summonsSystem.service.IDefectService;
import com.ruoyi.summonsSystem.vo.DefectDetail;
import com.ruoyi.summonsSystem.vo.EinkDisplayContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 不良入力服务实现
 */
@Service
@DataSource(DataSourceType.USER)
public class DefectServiceImpl implements IDefectService
{
    private static final Logger log = LoggerFactory.getLogger(DefectServiceImpl.class);

    @Autowired
    private DefectDetailMapper defectDetailMapper;

    @Autowired
    private EinkDisplayContentMapper einkDisplayContentMapper;

    @Override
    public Map<String, String> getCurrentProcess(String lotNo)
    {
        Map<String, String> result = new HashMap<>();
        EinkDisplayContent content = einkDisplayContentMapper.selectByLotNo(lotNo);
        if (content != null)
        {
            result.put("processCode", content.getCurrentProcessCode());
            result.put("processName", content.getCurrentProcessName());
        }
        return result;
    }

    @Override
    public int submit(DefectDetail detail)
    {
        log.info("提交不良数据: lotNo={}, defectItem={}, sampleCount={}",
                detail.getLotNo(), detail.getDefectItem(), detail.getSampleCount());
        return defectDetailMapper.insert(detail);
    }

    @Override
    public Map<String, Object> getDefectList(String lotNo)
    {
        List<DefectDetail> details = defectDetailMapper.selectByLotNo(lotNo);

        // 计算合计
        int total = 0;
        for (DefectDetail d : details)
        {
            if (d.getSampleCount() != null)
            {
                total += d.getSampleCount();
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("details", details);
        result.put("total", total);
        return result;
    }
}

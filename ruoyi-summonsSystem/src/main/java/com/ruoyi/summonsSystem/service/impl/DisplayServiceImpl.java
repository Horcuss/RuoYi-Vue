package com.ruoyi.summonsSystem.service.impl;

import com.ruoyi.summonsSystem.mapper.CompletionDetailMapper;
import com.ruoyi.summonsSystem.mapper.EinkDisplayContentMapper;
import com.ruoyi.summonsSystem.mapper.RfidLotBindingMapper;
import com.ruoyi.summonsSystem.provider.LotDataProvider;
import com.ruoyi.summonsSystem.service.IDisplayService;
import com.ruoyi.summonsSystem.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 显示服务实现（水墨屏数据管理 + API预留）
 */
@Service
@DataSource(DataSourceType.USER)
public class DisplayServiceImpl implements IDisplayService
{
    private static final Logger log = LoggerFactory.getLogger(DisplayServiceImpl.class);

    private static final String GP_PROCESS_CODE = "4100";

    @Autowired
    private LotDataProvider lotDataProvider;

    @Autowired
    private RfidLotBindingMapper rfidLotBindingMapper;

    @Autowired
    private EinkDisplayContentMapper einkDisplayContentMapper;

    @Autowired
    private CompletionDetailMapper completionDetailMapper;

    @Override
    public void refreshDisplay(String lotNo, String newProcessCode, String newProcessName)
    {
        log.info("刷新水墨屏显示: lotNo={}, 新工序={}({})", lotNo, newProcessName, newProcessCode);

        // 查rfid
        RfidLotBinding binding = rfidLotBindingMapper.selectActiveByLotNo(lotNo);
        if (binding == null)
        {
            log.warn("lot未绑定水墨屏，跳过刷新: lotNo={}", lotNo);
            return;
        }

        // 重新查lot基础数据
        LotBasicInfo lotInfo = lotDataProvider.getLotInfo(lotNo);

        // 查gp不良指示
        String gpDefectInfo = null;
        CompletionDetail gpCompletion = completionDetailMapper.selectByLotAndProcess(lotNo, GP_PROCESS_CODE);
        if (gpCompletion != null)
        {
            gpDefectInfo = gpCompletion.getDefectInfo();
        }

        // 更新eink_display_content
        EinkDisplayContent content = new EinkDisplayContent();
        content.setRfid(binding.getRfid());
        content.setLotNo(lotNo);
        if (lotInfo != null)
        {
            content.setProductName(lotInfo.getProductName());
            content.setProductModel(lotInfo.getProductModel());
            content.setCustomer(lotInfo.getCustomer());
        }
        content.setCurrentProcessCode(newProcessCode);
        content.setCurrentProcessName(newProcessName);
        content.setGpDefectInfo(gpDefectInfo);
        content.setStatus("0");
        einkDisplayContentMapper.updateByRfid(content);

        // 调用水墨屏硬件API
        pushToEinkScreen(binding.getRfid(), null);
    }

    @Override
    public void showAbnormal(String lotNo, String abnormalMsg)
    {
        log.warn("水墨屏显示异常: lotNo={}, msg={}", lotNo, abnormalMsg);

        RfidLotBinding binding = rfidLotBindingMapper.selectActiveByLotNo(lotNo);
        if (binding == null)
        {
            return;
        }

        // 清空显示数据，lot显示异常信息
        EinkDisplayContent content = new EinkDisplayContent();
        content.setRfid(binding.getRfid());
        content.setLotNo(lotNo);
        content.setProductName(null);
        content.setProductModel(null);
        content.setCustomer(null);
        content.setCurrentProcessCode("");
        content.setCurrentProcessName("异常");
        content.setGpDefectInfo(null);
        content.setDisplayData(abnormalMsg);
        content.setStatus("1");
        einkDisplayContentMapper.updateByRfid(content);

        pushToEinkScreen(binding.getRfid(), abnormalMsg);
    }

    @Override
    public void pushToEinkScreen(String rfid, String displayData)
    {
        // TODO: 预留接口 - 等水墨屏硬件API确定后实现
        // 目前只做日志输出
        log.info("[预留] 推送数据到水墨屏: rfid={}, data={}", rfid, displayData);
    }
}

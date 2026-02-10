package com.ruoyi.summonsSystem.service.impl;

import com.ruoyi.summonsSystem.mapper.CompletionDetailMapper;
import com.ruoyi.summonsSystem.service.ICompletionService;
import com.ruoyi.summonsSystem.vo.CompletionDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 完了输机服务实现
 */
@Service
@DataSource(DataSourceType.USER)
public class CompletionServiceImpl implements ICompletionService
{
    private static final Logger log = LoggerFactory.getLogger(CompletionServiceImpl.class);

    @Autowired
    private CompletionDetailMapper completionDetailMapper;

    @Override
    public int submit(CompletionDetail detail)
    {
        log.info("提交完了输机数据: lotNo={}, processCode={}", detail.getLotNo(), detail.getProcessCode());
        // 使用INSERT ON DUPLICATE KEY UPDATE，存在则更新，不存在则新增
        return completionDetailMapper.insert(detail);
    }
}

package com.ruoyi.summonsSystem.provider.impl;

import com.ruoyi.summonsSystem.provider.LotDataProvider;
import com.ruoyi.summonsSystem.vo.LotBasicInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * lot基础数据 - Mock实现（测试用）
 * 等甲方给了表结构后，写一个新的实现类替换即可
 */
@Component
public class MockLotDataProvider implements LotDataProvider
{
    private static final Logger log = LoggerFactory.getLogger(MockLotDataProvider.class);

    /** 模拟数据 */
    private static final Map<String, LotBasicInfo> MOCK_DATA = new HashMap<>();

    static
    {
        MOCK_DATA.put("LOT20260210001", new LotBasicInfo("LOT20260210001", "产品A", "PN-001", "客户甲"));
        MOCK_DATA.put("LOT20260210002", new LotBasicInfo("LOT20260210002", "产品B", "PN-002", "客户乙"));
        MOCK_DATA.put("LOT20260210003", new LotBasicInfo("LOT20260210003", "特殊产品X", "PN-003", "客户丙"));
    }

    @Override
    public LotBasicInfo getLotInfo(String lotNo)
    {
        log.info("[Mock] 查询lot基础数据: lotNo={}", lotNo);
        LotBasicInfo info = MOCK_DATA.get(lotNo);
        if (info == null)
        {
            // 对于未预设的lotNo，返回通用模拟数据
            log.info("[Mock] 未找到预设数据，返回通用模拟数据: lotNo={}", lotNo);
            return new LotBasicInfo(lotNo, "模拟品名", "模拟品番", "模拟客户");
        }
        return info;
    }
}

package com.ruoyi.summonsSystem.service;

import java.util.Map;

/**
 * 水墨屏绑定服务接口
 */
public interface IEinkBindService
{
    /**
     * 绑定水墨屏与lot
     *
     * @param rfid 水墨屏RFID
     * @param lotNo lot编号
     * @param processCode 当前工序code（由WS推送到前端）
     * @return 绑定结果信息
     */
    Map<String, Object> bind(String rfid, String lotNo, String processCode);
}

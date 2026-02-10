package com.ruoyi.summonsSystem.service;

/**
 * 显示服务接口（水墨屏API预留）
 */
public interface IDisplayService
{
    /**
     * 刷新水墨屏显示数据（工序流转时调用）
     *
     * @param lotNo lot编号
     * @param newProcessCode 新工序code
     * @param newProcessName 新工序名称
     */
    void refreshDisplay(String lotNo, String newProcessCode, String newProcessName);

    /**
     * 清空水墨屏数据并显示异常信息
     *
     * @param lotNo lot编号
     * @param abnormalMsg 异常信息
     */
    void showAbnormal(String lotNo, String abnormalMsg);

    /**
     * 调用水墨屏硬件API推送数据（预留接口）
     *
     * @param rfid 水墨屏RFID
     * @param displayData 显示数据JSON
     */
    void pushToEinkScreen(String rfid, String displayData);
}

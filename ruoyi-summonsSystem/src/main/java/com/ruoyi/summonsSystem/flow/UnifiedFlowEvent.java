package com.ruoyi.summonsSystem.flow;

import java.util.Date;

/**
 * 统一流转事件模型
 * 所有事件源（WS感应器、扫码枪、手动录入等）最终都转换为此对象
 */
public class UnifiedFlowEvent
{
    /** lot编号 */
    private String lotNo;

    /** 事件对应的工序code */
    private String processCode;

    /** 事件类型: IN/OUT */
    private String eventType;

    /** 事件时间 */
    private Date timestamp;

    /** 事件来源: WS_SENSOR / BARCODE / MANUAL */
    private String source;

    /** 原始数据（JSON字符串，用于调试） */
    private String rawData;

    /** 是否来自测定棚架（由适配层设置，流转引擎据此判断测定逻辑） */
    private boolean testingEvent;

    public static final String EVENT_IN = "IN";
    public static final String EVENT_OUT = "OUT";

    public UnifiedFlowEvent()
    {
    }

    public UnifiedFlowEvent(String lotNo, String processCode, String eventType, Date timestamp, String source)
    {
        this.lotNo = lotNo;
        this.processCode = processCode;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.source = source;
    }

    public String getLotNo()
    {
        return lotNo;
    }

    public void setLotNo(String lotNo)
    {
        this.lotNo = lotNo;
    }

    public String getProcessCode()
    {
        return processCode;
    }

    public void setProcessCode(String processCode)
    {
        this.processCode = processCode;
    }

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(Date timestamp)
    {
        this.timestamp = timestamp;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getRawData()
    {
        return rawData;
    }

    public void setRawData(String rawData)
    {
        this.rawData = rawData;
    }

    public boolean isTestingEvent()
    {
        return testingEvent;
    }

    public void setTestingEvent(boolean testingEvent)
    {
        this.testingEvent = testingEvent;
    }

    @Override
    public String toString()
    {
        return "UnifiedFlowEvent{lotNo='" + lotNo + "', processCode='" + processCode +
               "', eventType='" + eventType + "', source='" + source +
               "', testingEvent=" + testingEvent + "}";
    }
}

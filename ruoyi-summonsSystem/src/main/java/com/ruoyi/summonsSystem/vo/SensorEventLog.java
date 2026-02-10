package com.ruoyi.summonsSystem.vo;

import java.util.Date;

/**
 * 感应器事件日志对象 ticket_sensor_event_log
 */
public class SensorEventLog
{
    private Long id;
    private String rfid;
    private String ant;
    private String sensorIp;
    private String sensorPort;
    private Integer eventType;
    private String remark;
    private Date eventTime;
    private String processCode;
    private Integer processed;
    private String processResult;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getRfid()
    {
        return rfid;
    }

    public void setRfid(String rfid)
    {
        this.rfid = rfid;
    }

    public String getAnt()
    {
        return ant;
    }

    public void setAnt(String ant)
    {
        this.ant = ant;
    }

    public String getSensorIp()
    {
        return sensorIp;
    }

    public void setSensorIp(String sensorIp)
    {
        this.sensorIp = sensorIp;
    }

    public String getSensorPort()
    {
        return sensorPort;
    }

    public void setSensorPort(String sensorPort)
    {
        this.sensorPort = sensorPort;
    }

    public Integer getEventType()
    {
        return eventType;
    }

    public void setEventType(Integer eventType)
    {
        this.eventType = eventType;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public Date getEventTime()
    {
        return eventTime;
    }

    public void setEventTime(Date eventTime)
    {
        this.eventTime = eventTime;
    }

    public String getProcessCode()
    {
        return processCode;
    }

    public void setProcessCode(String processCode)
    {
        this.processCode = processCode;
    }

    public Integer getProcessed()
    {
        return processed;
    }

    public void setProcessed(Integer processed)
    {
        this.processed = processed;
    }

    public String getProcessResult()
    {
        return processResult;
    }

    public void setProcessResult(String processResult)
    {
        this.processResult = processResult;
    }
}

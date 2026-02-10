package com.ruoyi.summonsSystem.vo;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 感应器工序映射对象 ticket_sensor_process_mapping
 */
public class SensorProcessMapping extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String sensorIp;
    private String sensorPort;
    private String processCode;
    private String sensorType;
    private String locationDesc;
    private String status;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
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

    public String getProcessCode()
    {
        return processCode;
    }

    public void setProcessCode(String processCode)
    {
        this.processCode = processCode;
    }

    public String getSensorType()
    {
        return sensorType;
    }

    public void setSensorType(String sensorType)
    {
        this.sensorType = sensorType;
    }

    public String getLocationDesc()
    {
        return locationDesc;
    }

    public void setLocationDesc(String locationDesc)
    {
        this.locationDesc = locationDesc;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}

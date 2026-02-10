package com.ruoyi.summonsSystem.vo;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

/**
 * RFID与lot绑定关系对象 ticket_rfid_lot_binding
 */
public class RfidLotBinding extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String rfid;
    private String lotNo;
    private Date bindTime;
    private String status;

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

    public String getLotNo()
    {
        return lotNo;
    }

    public void setLotNo(String lotNo)
    {
        this.lotNo = lotNo;
    }

    public Date getBindTime()
    {
        return bindTime;
    }

    public void setBindTime(Date bindTime)
    {
        this.bindTime = bindTime;
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

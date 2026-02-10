package com.ruoyi.summonsSystem.vo;

import java.util.Date;

/**
 * lot流转状态对象 ticket_lot_flow_status
 */
public class LotFlowStatus
{
    private String lotNo;
    private String currentProcessCode;
    private Integer currentSeq;
    private Integer hasExited;
    private Integer testingRound;
    /** 测定工序code（首次进入测定时记录，用于识别测定循环） */
    private String testingProcessCode;
    private String lastEventType;
    private Date lastEventTime;
    private String status;
    private String abnormalMsg;
    private Date updateTime;

    public String getLotNo()
    {
        return lotNo;
    }

    public void setLotNo(String lotNo)
    {
        this.lotNo = lotNo;
    }

    public String getCurrentProcessCode()
    {
        return currentProcessCode;
    }

    public void setCurrentProcessCode(String currentProcessCode)
    {
        this.currentProcessCode = currentProcessCode;
    }

    public Integer getCurrentSeq()
    {
        return currentSeq;
    }

    public void setCurrentSeq(Integer currentSeq)
    {
        this.currentSeq = currentSeq;
    }

    public Integer getHasExited()
    {
        return hasExited;
    }

    public void setHasExited(Integer hasExited)
    {
        this.hasExited = hasExited;
    }

    public Integer getTestingRound()
    {
        return testingRound;
    }

    public void setTestingRound(Integer testingRound)
    {
        this.testingRound = testingRound;
    }

    public String getTestingProcessCode()
    {
        return testingProcessCode;
    }

    public void setTestingProcessCode(String testingProcessCode)
    {
        this.testingProcessCode = testingProcessCode;
    }

    public String getLastEventType()
    {
        return lastEventType;
    }

    public void setLastEventType(String lastEventType)
    {
        this.lastEventType = lastEventType;
    }

    public Date getLastEventTime()
    {
        return lastEventTime;
    }

    public void setLastEventTime(Date lastEventTime)
    {
        this.lastEventTime = lastEventTime;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getAbnormalMsg()
    {
        return abnormalMsg;
    }

    public void setAbnormalMsg(String abnormalMsg)
    {
        this.abnormalMsg = abnormalMsg;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }
}

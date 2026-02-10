package com.ruoyi.summonsSystem.vo;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * 完了输机明细对象 ticket_completion_detail
 */
public class CompletionDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String lotNo;
    private String processCode;
    private String processName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date workDate;

    private String worker;
    private Integer completionCount;
    private Integer quantity;
    private String deviceNo;
    private String filmRollNo;
    private String defectInfo;

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

    public String getProcessName()
    {
        return processName;
    }

    public void setProcessName(String processName)
    {
        this.processName = processName;
    }

    public Date getWorkDate()
    {
        return workDate;
    }

    public void setWorkDate(Date workDate)
    {
        this.workDate = workDate;
    }

    public String getWorker()
    {
        return worker;
    }

    public void setWorker(String worker)
    {
        this.worker = worker;
    }

    public Integer getCompletionCount()
    {
        return completionCount;
    }

    public void setCompletionCount(Integer completionCount)
    {
        this.completionCount = completionCount;
    }

    public Integer getQuantity()
    {
        return quantity;
    }

    public void setQuantity(Integer quantity)
    {
        this.quantity = quantity;
    }

    public String getDeviceNo()
    {
        return deviceNo;
    }

    public void setDeviceNo(String deviceNo)
    {
        this.deviceNo = deviceNo;
    }

    public String getFilmRollNo()
    {
        return filmRollNo;
    }

    public void setFilmRollNo(String filmRollNo)
    {
        this.filmRollNo = filmRollNo;
    }

    public String getDefectInfo()
    {
        return defectInfo;
    }

    public void setDefectInfo(String defectInfo)
    {
        this.defectInfo = defectInfo;
    }
}

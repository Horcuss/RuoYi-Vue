package com.ruoyi.summonsSystem.vo;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 不良入力明细对象 ticket_defect_detail
 */
public class DefectDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String lotNo;
    private String processCode;
    private String processName;
    private String defectItem;
    private Integer sampleCount;
    private String createBy;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
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

    public String getProcessName()
    {
        return processName;
    }

    public void setProcessName(String processName)
    {
        this.processName = processName;
    }

    public String getDefectItem()
    {
        return defectItem;
    }

    public void setDefectItem(String defectItem)
    {
        this.defectItem = defectItem;
    }

    public Integer getSampleCount()
    {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount)
    {
        this.sampleCount = sampleCount;
    }

    @Override
    public String getCreateBy()
    {
        return createBy;
    }

    @Override
    public void setCreateBy(String createBy)
    {
        this.createBy = createBy;
    }
}

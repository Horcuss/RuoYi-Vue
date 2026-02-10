package com.ruoyi.summonsSystem.vo;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * lot最终工序序列对象 ticket_lot_process_sequence
 */
public class LotProcessSequence extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String lotNo;
    private String processCode;
    private String processName;
    private Integer seq;
    private String processSource;

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

    public Integer getSeq()
    {
        return seq;
    }

    public void setSeq(Integer seq)
    {
        this.seq = seq;
    }

    public String getProcessSource()
    {
        return processSource;
    }

    public void setProcessSource(String processSource)
    {
        this.processSource = processSource;
    }
}

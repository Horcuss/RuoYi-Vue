package com.ruoyi.summonsSystem.vo;

/**
 * 工序步骤（非数据库实体，由 ProcessDataProvider 接口返回）
 */
public class ProcessStep
{
    private String processCode;
    private String processName;
    private Integer seq;

    public ProcessStep()
    {
    }

    public ProcessStep(String processCode, String processName, Integer seq)
    {
        this.processCode = processCode;
        this.processName = processName;
        this.seq = seq;
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
}

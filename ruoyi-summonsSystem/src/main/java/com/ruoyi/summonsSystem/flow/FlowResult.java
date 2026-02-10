package com.ruoyi.summonsSystem.flow;

/**
 * 流转结果模型
 */
public class FlowResult
{
    /** 是否发生了工序流转 */
    private boolean transitioned;

    /** 新工序code（流转时有值） */
    private String newProcessCode;

    /** 新工序名称 */
    private String newProcessName;

    /** 结果描述 */
    private String message;

    /** 是否异常 */
    private boolean abnormal;

    /** 异常信息 */
    private String abnormalMsg;

    public static FlowResult ignored(String message)
    {
        FlowResult r = new FlowResult();
        r.transitioned = false;
        r.message = message;
        return r;
    }

    public static FlowResult transitioned(String newProcessCode, String newProcessName)
    {
        FlowResult r = new FlowResult();
        r.transitioned = true;
        r.newProcessCode = newProcessCode;
        r.newProcessName = newProcessName;
        r.message = "工序流转: " + newProcessName;
        return r;
    }

    public static FlowResult abnormal(String abnormalMsg)
    {
        FlowResult r = new FlowResult();
        r.transitioned = false;
        r.abnormal = true;
        r.abnormalMsg = abnormalMsg;
        r.message = "异常: " + abnormalMsg;
        return r;
    }

    public static FlowResult exited(String message)
    {
        FlowResult r = new FlowResult();
        r.transitioned = false;
        r.message = message;
        return r;
    }

    public static FlowResult recovered(String message)
    {
        FlowResult r = new FlowResult();
        r.transitioned = true;
        r.message = message;
        return r;
    }

    public boolean isTransitioned()
    {
        return transitioned;
    }

    public String getNewProcessCode()
    {
        return newProcessCode;
    }

    public String getNewProcessName()
    {
        return newProcessName;
    }

    public String getMessage()
    {
        return message;
    }

    public boolean isAbnormal()
    {
        return abnormal;
    }

    public String getAbnormalMsg()
    {
        return abnormalMsg;
    }
}

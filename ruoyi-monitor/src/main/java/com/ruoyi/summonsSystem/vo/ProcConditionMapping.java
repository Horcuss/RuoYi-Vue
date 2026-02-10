package com.ruoyi.monitor.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 加工条件映射对象 proc_condition_mapping
 *
 * @author ruoyi
 * @date 2025-01-15
 */
public class ProcConditionMapping extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 加工条件group（01=非GP，02=GP） */
    @Excel(name = "加工条件group")
    @NotBlank(message = "加工条件group不能为空")
    private String procConditionGroup;

    /** 大分类cd */
    @Excel(name = "大分类cd")
    @NotBlank(message = "大分类cd不能为空")
    private String majorClassCd;

    /** 中分类cd */
    @Excel(name = "中分类cd")
    @NotBlank(message = "中分类cd不能为空")
    private String minorClassCd;

    /** 加工条件種cd */
    @Excel(name = "加工条件種cd")
    @NotBlank(message = "加工条件種cd不能为空")
    private String procConditionTypeCd;

    /** 多key区分（1=品名，2=有多key，3=其他） */
    @Excel(name = "多key区分")
    @NotBlank(message = "多key区分不能为空")
    private String multiKeyType;

    /** 加工条件序号 */
    @Excel(name = "加工条件序号")
    @NotNull(message = "加工条件序号不能为空")
    private Integer procConditionSeq;

    /** 加工条件名称 */
    @Excel(name = "加工条件名称")
    @NotBlank(message = "加工条件名称不能为空")
    private String conditionName;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setProcConditionGroup(String procConditionGroup)
    {
        this.procConditionGroup = procConditionGroup;
    }

    public String getProcConditionGroup()
    {
        return procConditionGroup;
    }

    public void setMajorClassCd(String majorClassCd)
    {
        this.majorClassCd = majorClassCd;
    }

    public String getMajorClassCd()
    {
        return majorClassCd;
    }

    public void setMinorClassCd(String minorClassCd)
    {
        this.minorClassCd = minorClassCd;
    }

    public String getMinorClassCd()
    {
        return minorClassCd;
    }

    public void setProcConditionTypeCd(String procConditionTypeCd)
    {
        this.procConditionTypeCd = procConditionTypeCd;
    }

    public String getProcConditionTypeCd()
    {
        return procConditionTypeCd;
    }

    public void setMultiKeyType(String multiKeyType)
    {
        this.multiKeyType = multiKeyType;
    }

    public String getMultiKeyType()
    {
        return multiKeyType;
    }

    public void setProcConditionSeq(Integer procConditionSeq)
    {
        this.procConditionSeq = procConditionSeq;
    }

    public Integer getProcConditionSeq()
    {
        return procConditionSeq;
    }

    public void setConditionName(String conditionName)
    {
        this.conditionName = conditionName;
    }

    public String getConditionName()
    {
        return conditionName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("procConditionGroup", getProcConditionGroup())
            .append("majorClassCd", getMajorClassCd())
            .append("minorClassCd", getMinorClassCd())
            .append("procConditionTypeCd", getProcConditionTypeCd())
            .append("multiKeyType", getMultiKeyType())
            .append("procConditionSeq", getProcConditionSeq())
            .append("conditionName", getConditionName())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}

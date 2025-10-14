package com.ruoyi.monitor.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 监控配置对象 monitor_config
 * 使用MyBatis标准映射
 *
 * @author ruoyi
 * @date 2025-01-15
 */
public class MonitorConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置ID */
    private Long configId;

    /** 监控页面KEY */
    @Excel(name = "监控页面KEY")
    @NotBlank(message = "监控页面KEY不能为空")
    @Size(min = 1, max = 100, message = "监控页面KEY长度不能超过100个字符")
    private String configKey;

    /** 监控页面名称 */
    @Excel(name = "监控页面名称")
    @NotBlank(message = "监控页面名称不能为空")
    @Size(min = 1, max = 200, message = "监控页面名称长度不能超过200个字符")
    private String configName;

    /** 配置JSON数据 */
    @NotBlank(message = "配置JSON数据不能为空")
    private String configJson;

    /** 状态（0=启用 1=停用） */
    @Excel(name = "状态", readConverterExp = "0=启用,1=停用")
    private String status;

    /** 删除标志（0=正常 2=删除） */
    private String delFlag;

    public void setConfigId(Long configId) 
    {
        this.configId = configId;
    }

    public Long getConfigId() 
    {
        return configId;
    }

    public void setConfigKey(String configKey) 
    {
        this.configKey = configKey;
    }

    public String getConfigKey() 
    {
        return configKey;
    }

    public void setConfigName(String configName) 
    {
        this.configName = configName;
    }

    public String getConfigName() 
    {
        return configName;
    }

    public void setConfigJson(String configJson) 
    {
        this.configJson = configJson;
    }

    public String getConfigJson() 
    {
        return configJson;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("configId", getConfigId())
            .append("configKey", getConfigKey())
            .append("configName", getConfigName())
            .append("configJson", getConfigJson())
            .append("status", getStatus())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .toString();
    }
}


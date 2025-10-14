package com.ruoyi.common.enums;

/**
 * 数据源
 *
 * @author ruoyi
 */
public enum DataSourceType
{
    /**
     * 主库
     */
    MASTER,

    /**
     * 从库
     */
    SLAVE,

    /**
     * Monitor数据源（Oracle - 存储监控配置）
     */
    MONITOR,

    /**
     * User数据源（Oracle - 查询业务数据）
     */
    USER
}

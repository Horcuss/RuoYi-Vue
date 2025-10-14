package com.ruoyi.monitor.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * User数据源Mapper接口
 * 使用@DataSource注解指定user数据源
 * 用于执行业务数据查询
 *
 * @author ruoyi
 * @date 2025-01-15
 */
@Mapper
@DataSource(DataSourceType.USER)  // 指定使用user数据源
public interface UserDataMapper
{
    /**
     * 执行自定义SQL查询
     * 
     * @param sql SQL语句
     * @return 查询结果列表
     */
    @Select("${sql}")
    List<Map<String, Object>> executeSql(@Param("sql") String sql);
    
    /**
     * 执行单条SQL查询（返回单行）
     * 
     * @param sql SQL语句
     * @return 查询结果
     */
    @Select("${sql}")
    Map<String, Object> executeSqlForOne(@Param("sql") String sql);
}


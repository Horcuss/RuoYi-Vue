package com.ruoyi.monitor.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.monitor.domain.ProcConditionMapping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 加工条件映射Mapper接口
 *
 * @author ruoyi
 * @date 2025-01-15
 */
@DataSource(DataSourceType.MONITOR)  // 指定使用monitor数据源
public interface ProcConditionMappingMapper
{
    /**
     * 根据完整条件查询加工条件名称
     *
     * @param group 加工条件group
     * @param majorCd 大分类cd
     * @param minorCd 中分类cd
     * @param typeCd 加工条件種cd
     * @param multiKey 多key区分
     * @param seq 加工条件序号
     * @return 加工条件名称
     */
    String findConditionName(
        @Param("group") String group,
        @Param("majorCd") String majorCd,
        @Param("minorCd") String minorCd,
        @Param("typeCd") String typeCd,
        @Param("multiKey") String multiKey,
        @Param("seq") Integer seq
    );

    /**
     * 批量插入映射数据（用于导入Excel）
     *
     * @param list 映射数据列表
     * @return 结果
     */
    int batchInsert(@Param("list") List<ProcConditionMapping> list);

    /**
     * 查询映射列表
     *
     * @param mapping 查询条件
     * @return 映射列表
     */
    List<ProcConditionMapping> selectList(ProcConditionMapping mapping);

    /**
     * 根据ID查询
     *
     * @param id 主键
     * @return 映射对象
     */
    ProcConditionMapping selectById(@Param("id") Long id);

    /**
     * 新增映射
     *
     * @param mapping 映射对象
     * @return 结果
     */
    int insert(ProcConditionMapping mapping);

    /**
     * 修改映射
     *
     * @param mapping 映射对象
     * @return 结果
     */
    int updateById(ProcConditionMapping mapping);

    /**
     * 删除映射
     *
     * @param id 主键
     * @return 结果
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除映射
     *
     * @param ids 主键列表
     * @return 结果
     */
    int deleteBatchIds(@Param("ids") List<Long> ids);
}

package com.micro.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.micro.platform.system.entity.SysTodoTag;
import com.micro.platform.system.entity.SysTodoTagRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 待办事项与标签关联 Mapper 接口
 */
@Mapper
public interface SysTodoTagRelationMapper extends BaseMapper<SysTodoTagRelation> {

    /**
     * 根据待办 ID 获取标签列表
     */
    List<SysTodoTag> selectTagsByTodoId(@Param("todoId") Long todoId);

    /**
     * 根据标签 ID 获取待办列表
     */
    List<SysTodoTagRelation> selectRelationsByTagId(@Param("tagId") Long tagId);

    /**
     * 批量插入关联关系
     */
    int batchInsert(@Param("relations") List<SysTodoTagRelation> relations);

    /**
     * 删除待办的所有标签关联
     */
    int deleteByTodoId(@Param("todoId") Long todoId);
}
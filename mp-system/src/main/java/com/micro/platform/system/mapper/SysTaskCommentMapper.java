package com.micro.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.micro.platform.system.entity.SysTaskComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务评论 Mapper 接口
 */
@Mapper
public interface SysTaskCommentMapper extends BaseMapper<SysTaskComment> {

    /**
     * 根据任务 ID 获取评论列表
     */
    List<SysTaskComment> selectCommentsByTodoId(@Param("todoId") Long todoId);

    /**
     * 获取评论总数
     */
    int selectCountByTodoId(@Param("todoId") Long todoId);
}
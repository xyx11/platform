package com.micro.platform.system.service;

import com.micro.platform.system.entity.SysTaskComment;

import java.util.List;

/**
 * 任务评论服务接口
 */
public interface SysTaskCommentService {

    /**
     * 获取任务评论列表
     */
    List<SysTaskComment> getCommentsByTodoId(Long todoId);

    /**
     * 添加评论
     */
    SysTaskComment addComment(SysTaskComment comment);

    /**
     * 更新评论
     */
    void updateComment(SysTaskComment comment);

    /**
     * 删除评论
     */
    void deleteComment(Long commentId);

    /**
     * 获取评论数量
     */
    int getCommentCount(Long todoId);
}
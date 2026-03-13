package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysTaskComment;
import com.micro.platform.system.mapper.SysTaskCommentMapper;
import com.micro.platform.system.service.SysTaskCommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务评论服务实现
 */
@Service
public class SysTaskCommentServiceImpl implements SysTaskCommentService {

    private static final Logger log = LoggerFactory.getLogger(SysTaskCommentServiceImpl.class);

    private final SysTaskCommentMapper commentMapper;

    public SysTaskCommentServiceImpl(SysTaskCommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    @Override
    public List<SysTaskComment> getCommentsByTodoId(Long todoId) {
        return commentMapper.selectCommentsByTodoId(todoId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysTaskComment addComment(SysTaskComment comment) {
        comment.setUserId(SecurityUtil.getUserId());
        comment.setCreateTime(LocalDateTime.now());
        comment.setDeleted(0);
        commentMapper.insert(comment);
        log.info("添加评论：{}, 任务 ID: {}", comment.getCommentId(), comment.getTodoId());
        return comment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateComment(SysTaskComment comment) {
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.updateById(comment);
        log.info("更新评论：{}", comment.getCommentId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        SysTaskComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        // 逻辑删除
        comment.setDeleted(1);
        commentMapper.updateById(comment);
        log.info("删除评论：{}", commentId);
    }

    @Override
    public int getCommentCount(Long todoId) {
        return commentMapper.selectCountByTodoId(todoId);
    }
}
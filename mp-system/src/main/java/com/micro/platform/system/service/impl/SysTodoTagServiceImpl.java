package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysTodoTag;
import com.micro.platform.system.entity.SysTodoTagRelation;
import com.micro.platform.system.mapper.SysTodoTagMapper;
import com.micro.platform.system.mapper.SysTodoTagRelationMapper;
import com.micro.platform.system.service.SysTodoTagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 待办事项标签服务实现
 */
@Service
public class SysTodoTagServiceImpl extends ServiceImplX<SysTodoTagMapper, SysTodoTag> implements SysTodoTagService {

    private static final Logger log = LoggerFactory.getLogger(SysTodoTagServiceImpl.class);

    private final SysTodoTagRelationMapper tagRelationMapper;

    public SysTodoTagServiceImpl(SysTodoTagRelationMapper tagRelationMapper) {
        this.tagRelationMapper = tagRelationMapper;
    }

    @Override
    public List<SysTodoTag> getUserTags(Long userId) {
        if (userId == null) {
            userId = SecurityUtil.getUserId();
        }
        return baseMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysTodoTag createTag(SysTodoTag tag) {
        tag.setCreateBy(SecurityUtil.getUserId());
        tag.setCreateTime(LocalDateTime.now());
        tag.setUserId(SecurityUtil.getUserId());
        baseMapper.insert(tag);
        log.info("创建标签：{}", tag.getTagName());
        return tag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTag(SysTodoTag tag) {
        tag.setUpdateBy(SecurityUtil.getUserId());
        tag.setUpdateTime(LocalDateTime.now());
        updateById(tag);
        log.info("更新标签：{}", tag.getTagId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long tagId) {
        // 删除标签关联
        tagRelationMapper.delete(new LambdaQueryWrapper<SysTodoTagRelation>()
                .eq(SysTodoTagRelation::getTagId, tagId));
        // 删除标签
        removeById(tagId);
        log.info("删除标签：{}", tagId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTagsToTodo(Long todoId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        // 先删除现有的关联
        tagRelationMapper.deleteByTodoId(todoId);

        // 批量插入新关联
        List<SysTodoTagRelation> relations = new ArrayList<>();
        for (Long tagId : tagIds) {
            SysTodoTagRelation relation = new SysTodoTagRelation();
            relation.setTodoId(todoId);
            relation.setTagId(tagId);
            relation.setCreateTime(LocalDateTime.now());
            relations.add(relation);
        }
        tagRelationMapper.batchInsert(relations);
        log.info("为待办添加标签：{}, 标签数：{}", todoId, tagIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeTagsFromTodo(Long todoId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<SysTodoTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTodoTagRelation::getTodoId, todoId)
                .in(SysTodoTagRelation::getTagId, tagIds);
        tagRelationMapper.delete(wrapper);
        log.info("移除待办标签：{}, 标签数：{}", todoId, tagIds.size());
    }

    @Override
    public List<SysTodoTag> getTagsByTodoId(Long todoId) {
        return tagRelationMapper.selectTagsByTodoId(todoId);
    }

    @Override
    public List<Long> getTodoIdsByTagId(Long tagId) {
        List<SysTodoTagRelation> relations = tagRelationMapper.selectRelationsByTagId(tagId);
        List<Long> todoIds = new ArrayList<>();
        for (SysTodoTagRelation relation : relations) {
            todoIds.add(relation.getTodoId());
        }
        return todoIds;
    }
}
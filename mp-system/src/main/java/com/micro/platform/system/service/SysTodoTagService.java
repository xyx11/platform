package com.micro.platform.system.service;

import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysTodoTag;
import com.micro.platform.system.entity.SysTodoTagRelation;

import java.util.List;

/**
 * 待办事项标签服务接口
 */
public interface SysTodoTagService extends IServiceX<SysTodoTag> {

    /**
     * 获取用户的标签列表
     */
    List<SysTodoTag> getUserTags(Long userId);

    /**
     * 创建标签
     */
    SysTodoTag createTag(SysTodoTag tag);

    /**
     * 更新标签
     */
    void updateTag(SysTodoTag tag);

    /**
     * 删除标签
     */
    void deleteTag(Long tagId);

    /**
     * 为待办事项添加标签
     */
    void addTagsToTodo(Long todoId, List<Long> tagIds);

    /**
     * 移除待办事项的标签
     */
    void removeTagsFromTodo(Long todoId, List<Long> tagIds);

    /**
     * 获取待办事项的标签列表
     */
    List<SysTodoTag> getTagsByTodoId(Long todoId);

    /**
     * 按标签筛选待办
     */
    List<Long> getTodoIdsByTagId(Long tagId);
}
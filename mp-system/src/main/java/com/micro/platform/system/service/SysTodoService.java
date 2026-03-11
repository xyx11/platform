package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysTodo;

import java.util.List;
import java.util.Map;

/**
 * 待办事项服务接口
 */
public interface SysTodoService extends IServiceX<SysTodo> {

    /**
     * 分页查询待办事项
     */
    Page<SysTodo> selectTodoPage(SysTodo todo, Integer pageNum, Integer pageSize);

    /**
     * 获取我的待办列表
     */
    Page<SysTodo> getMyTodos(Integer pageNum, Integer pageSize);

    /**
     * 完成待办
     */
    void completeTodo(Long todoId);

    /**
     * 取消待办
     */
    void cancelTodo(Long todoId);

    /**
     * 获取待办统计信息
     */
    Map<String, Object> getTodoStats();

    /**
     * 获取即将到期的待办（3 天内）
     */
    List<SysTodo> getExpiringTodos(Integer days);

    /**
     * 获取逾期待办列表
     */
    List<SysTodo> getOverdueTodos();

    /**
     * 批量完成待办
     */
    void batchComplete(List<Long> todoIds);

    /**
     * 按优先级获取待办
     */
    List<SysTodo> getByPriority(Integer priority, Integer limit);
}
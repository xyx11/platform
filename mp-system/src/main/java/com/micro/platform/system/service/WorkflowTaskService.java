package com.micro.platform.system.service;

import java.util.List;
import java.util.Map;

/**
 * 工作流任务服务
 */
public interface WorkflowTaskService {

    /**
     * 获取待办任务列表
     */
    List<Map<String, Object>> getTodoTasks(String userId, int pageNum, int pageSize);

    /**
     * 获取已办任务列表
     */
    List<Map<String, Object>> getDoneTasks(String userId, int pageNum, int pageSize);

    /**
     * 获取任务详情
     */
    Map<String, Object> getTask(String taskId);

    /**
     * 完成任务
     */
    void completeTask(String taskId, String userId, Map<String, Object> variables);

    /**
     * 委派任务
     */
    void delegateTask(String taskId, String assignee, String delegateUser);

    /**
     * 转办任务
     */
    void transferTask(String taskId, String currentAssignee, String newAssignee);

    /**
     * 获取任务历史
     */
    List<Map<String, Object>> getTaskHistory(String processInstanceId);

    /**
     * 获取任务数量统计
     */
    Map<String, Object> getTaskStats(String userId);
}
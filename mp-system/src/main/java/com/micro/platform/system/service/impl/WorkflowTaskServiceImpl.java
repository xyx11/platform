package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.system.service.WorkflowTaskService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricTaskInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作流任务服务实现
 */
@Slf4j
@Service
public class WorkflowTaskServiceImpl implements WorkflowTaskService {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    public WorkflowTaskServiceImpl(TaskService taskService,
                                   RuntimeService runtimeService,
                                   HistoryService historyService) {
        this.taskService = taskService;
        this.runtimeService = runtimeService;
        this.historyService = historyService;
    }

    @Override
    public List<Map<String, Object>> getTodoTasks(String userId, int pageNum, int pageSize) {
        log.debug("获取用户待办任务：{}, 页码：{}, 大小：{}", userId, pageNum, pageSize);

        Page<Task> page = new Page<>(pageNum, pageSize);
        var query = taskService.createTaskQuery()
                .taskAssignee(userId)
                .orderByTaskPriority()
                .desc()
                .orderByTaskCreateTime()
                .asc();

        List<Task> tasks = query.listPage((pageNum - 1) * pageSize, pageSize);

        return tasks.stream().map(this::convertTaskToMap).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getDoneTasks(String userId, int pageNum, int pageSize) {
        log.debug("获取用户已办任务：{}, 页码：{}, 大小：{}", userId, pageNum, pageSize);

        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .finished()
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .listPage((pageNum - 1) * pageSize, pageSize);

        return tasks.stream().map(this::convertHistoricTaskToMap).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getTask(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        Map<String, Object> result = convertTaskToMap(task);

        // 获取流程变量
        String processInstanceId = task.getProcessInstanceId();
        var variables = runtimeService.getVariables(processInstanceId);
        result.put("variables", variables);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(String taskId, String userId, Map<String, Object> variables) {
        log.info("完成任务：{}, 用户：{}", taskId, userId);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        taskService.complete(taskId, variables);
        log.info("任务完成：{}", taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegateTask(String taskId, String assignee, String delegateUser) {
        log.info("委派任务：{}, 原处理人：{}, 被委派人：{}", taskId, assignee, delegateUser);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        taskService.delegateTask(taskId, delegateUser);
        log.info("任务委派成功：{}", taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferTask(String taskId, String currentAssignee, String newAssignee) {
        log.info("转办任务：{}, 原处理人：{}, 新处理人：{}", taskId, currentAssignee, newAssignee);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        taskService.setAssignee(taskId, newAssignee);
        log.info("任务转办成功：{}", taskId);
    }

    @Override
    public List<Map<String, Object>> getTaskHistory(String processInstanceId) {
        log.debug("获取任务历史：{}", processInstanceId);

        List<HistoricTaskInstance> history = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceStartTime()
                .asc()
                .list();

        return history.stream().map(this::convertHistoricTaskToMap).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getTaskStats(String userId) {
        log.debug("获取任务统计：{}", userId);

        Map<String, Object> stats = new HashMap<>();

        // 待办任务数量
        long todoCount = taskService.createTaskQuery().taskAssignee(userId).count();
        stats.put("todoCount", todoCount);

        // 已办任务数量
        long doneCount = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .finished()
                .count();
        stats.put("doneCount", doneCount);

        // 按优先级统计
        long highPriority = taskService.createTaskQuery()
                .taskAssignee(userId)
                .taskPriority("high")
                .count();
        long normalPriority = taskService.createTaskQuery()
                .taskAssignee(userId)
                .taskPriority("normal")
                .count();
        long lowPriority = taskService.createTaskQuery()
                .taskAssignee(userId)
                .taskPriority("low")
                .count();

        stats.put("highPriorityCount", highPriority);
        stats.put("normalPriorityCount", normalPriority);
        stats.put("lowPriorityCount", lowPriority);

        return stats;
    }

    /**
     * 转换任务为 Map
     */
    private Map<String, Object> convertTaskToMap(Task task) {
        Map<String, Object> map = new HashMap<>();
        map.put("taskId", task.getId());
        map.put("taskName", task.getName());
        map.put("taskKey", task.getTaskDefinitionKey());
        map.put("assignee", task.getAssignee());
        map.put("owner", task.getOwner());
        map.put("priority", task.getPriority());
        map.put("createTime", task.getCreateTime());
        map.put("processInstanceId", task.getProcessInstanceId());
        map.put("processDefinitionId", task.getProcessDefinitionId());
        map.put("executionId", task.getExecutionId());
        map.put("description", task.getDescription());

        // 获取流程定义名称
        try {
            var definition = task.getProcessDefinitionId() != null ?
                null : null; // 简化处理
            map.put("processDefinitionName", "");
        } catch (Exception e) {
            map.put("processDefinitionName", "");
        }

        return map;
    }

    /**
     * 转换历史任务为 Map
     */
    private Map<String, Object> convertHistoricTaskToMap(HistoricTaskInstance task) {
        Map<String, Object> map = new HashMap<>();
        map.put("taskId", task.getId());
        map.put("taskName", task.getName());
        map.put("taskKey", task.getTaskDefinitionKey());
        map.put("assignee", task.getAssignee());
        map.put("owner", task.getOwner());
        map.put("priority", task.getPriority());
        map.put("createTime", task.getStartTime());
        map.put("endTime", task.getEndTime());
        map.put("duration", task.getDurationInMillis());
        map.put("processInstanceId", task.getProcessInstanceId());
        map.put("processDefinitionId", task.getProcessDefinitionId());
        map.put("description", task.getDescription());
        map.put("deleteReason", task.getDeleteReason());
        return map;
    }
}
package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.system.service.WorkflowTaskService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作流任务服务实现
 */
@Service
public class WorkflowTaskServiceImpl implements WorkflowTaskService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowTaskServiceImpl.class);

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
    public List<Map<String, Object>> getTodoTasks(String userId, int pageNum, int pageSize, Map<String, Object> params) {
        log.debug("获取用户待办任务：{}, 页码：{}, 大小：{}, 筛选条件：{}", userId, pageNum, pageSize, params);

        var query = taskService.createTaskQuery()
                .taskAssignee(userId)
                .orderByTaskPriority()
                .desc()
                .orderByTaskCreateTime()
                .asc();

        // 按任务名称筛选
        if (params != null && StringUtils.hasText((String) params.get("taskName"))) {
            String taskName = (String) params.get("taskName");
            query.taskNameLike("%" + taskName + "%");
        }

        // 按流程名称筛选 - 使用历史流程实例查询
        if (params != null && StringUtils.hasText((String) params.get("processName"))) {
            String processName = (String) params.get("processName");
            // 先查询所有历史流程实例，然后过滤名称
            var processQuery = historyService.createHistoricProcessInstanceQuery()
                    .unfinished()
                    .list();
            List<String> processInstanceIds = processQuery.stream()
                    .filter(p -> processName == null || p.getProcessDefinitionName() != null && p.getProcessDefinitionName().contains(processName))
                    .map(HistoricProcessInstance::getId)
                    .collect(Collectors.toList());
            if (!processInstanceIds.isEmpty()) {
                query.processInstanceIdIn(processInstanceIds);
            } else {
                return new ArrayList<>();
            }
        }

        // 按时间范围筛选（创建时间）
        if (params != null) {
            String startTime = (String) params.get("startTime");
            String endTime = (String) params.get("endTime");

            if (StringUtils.hasText(startTime)) {
                try {
                    LocalDateTime startDateTime = LocalDate.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
                    query.taskCreatedAfter(Date.from(startDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()));
                } catch (Exception e) {
                    log.warn("开始时间格式错误：{}", startTime);
                }
            }

            if (StringUtils.hasText(endTime)) {
                try {
                    LocalDateTime endDateTime = LocalDate.parse(endTime, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay().plusDays(1);
                    query.taskCreatedBefore(Date.from(endDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()));
                } catch (Exception e) {
                    log.warn("结束时间格式错误：{}", endTime);
                }
            }
        }

        List<Task> tasks = query.listPage((pageNum - 1) * pageSize, pageSize);

        return tasks.stream().map(this::convertTaskToMap).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getDoneTasks(String userId, int pageNum, int pageSize, Map<String, Object> params) {
        log.debug("获取用户已办任务：{}, 页码：{}, 大小：{}, 筛选条件：{}", userId, pageNum, pageSize, params);

        var query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .finished()
                .orderByHistoricTaskInstanceEndTime()
                .desc();

        // 按任务名称筛选
        if (params != null && StringUtils.hasText((String) params.get("taskName"))) {
            String taskName = (String) params.get("taskName");
            query.taskNameLike("%" + taskName + "%");
        }

        // 按流程名称筛选
        if (params != null && StringUtils.hasText((String) params.get("processName"))) {
            String processName = (String) params.get("processName");
            var processQuery = historyService.createHistoricProcessInstanceQuery()
                    .finished()
                    .list();
            List<String> processInstanceIds = processQuery.stream()
                    .filter(p -> processName == null || p.getProcessDefinitionName() != null && p.getProcessDefinitionName().contains(processName))
                    .map(p -> p.getId())
                    .collect(Collectors.toList());
            if (!processInstanceIds.isEmpty()) {
                query.processInstanceIdIn(processInstanceIds);
            } else {
                return new ArrayList<>();
            }
        }

        List<HistoricTaskInstance> tasks = query.listPage((pageNum - 1) * pageSize, pageSize);

        return tasks.stream().map(this::convertHistoricTaskToMap).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getTask(String taskId) {
        log.debug("获取任务详情：{}", taskId);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        Map<String, Object> result = convertTaskToMap(task);

        String processInstanceId = task.getProcessInstanceId();
        var variables = runtimeService.getVariables(processInstanceId);
        result.put("variables", variables);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(String taskId, String userId, Map<String, Object> variables) {
        log.info("完成任务：{}, 操作人：{}", taskId, userId);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        if (!userId.equals(task.getAssignee())) {
            throw new BusinessException("无权处理该任务");
        }

        taskService.complete(taskId, variables);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegateTask(String taskId, String assignee, String delegateUser) {
        log.info("委派任务：{}, 原处理人：{}, 被委派人：{}", taskId, assignee, delegateUser);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        taskService.setOwner(taskId, assignee);
        taskService.setAssignee(taskId, delegateUser);
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
    }

    @Override
    public List<Map<String, Object>> getTaskHistory(String processInstanceId) {
        log.debug("获取任务历史：{}", processInstanceId);

        var query = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceEndTime()
                .asc();

        List<HistoricTaskInstance> tasks = query.list();

        return tasks.stream().map(this::convertHistoricTaskToMap).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getTaskStats(String userId) {
        log.debug("获取任务统计：{}", userId);

        Map<String, Object> stats = new HashMap<>();

        long todoCount = taskService.createTaskQuery().taskAssignee(userId).count();
        stats.put("todoCount", todoCount);

        long doneCount = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .finished()
                .count();
        stats.put("doneCount", doneCount);

        List<Task> allTasks = taskService.createTaskQuery().taskAssignee(userId).list();
        long highPriority = allTasks.stream().filter(t -> t.getPriority() == 100).count();
        long normalPriority = allTasks.stream().filter(t -> t.getPriority() == 50).count();
        long lowPriority = allTasks.stream().filter(t -> t.getPriority() == 10).count();

        stats.put("highPriorityCount", highPriority);
        stats.put("normalPriorityCount", normalPriority);
        stats.put("lowPriorityCount", lowPriority);

        Map<String, Long> statsByProcess = new HashMap<>();
        for (Task task : allTasks) {
            String processName = task.getProcessDefinitionId();
            statsByProcess.put(processName, statsByProcess.getOrDefault(processName, 0L) + 1);
        }
        stats.put("statsByProcess", statsByProcess);

        return stats;
    }

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
        map.put("description", task.getDescription());
        return map;
    }

    private Map<String, Object> convertHistoricTaskToMap(HistoricTaskInstance task) {
        Map<String, Object> map = new HashMap<>();
        map.put("taskId", task.getId());
        map.put("taskName", task.getName());
        map.put("taskKey", task.getTaskDefinitionKey());
        map.put("assignee", task.getAssignee());
        map.put("createTime", task.getStartTime());
        map.put("endTime", task.getEndTime());
        map.put("duration", task.getDurationInMillis());
        map.put("description", task.getDescription());
        map.put("deleteReason", task.getDeleteReason());
        return map;
    }
}
package com.micro.platform.system.service.impl;

import com.micro.platform.system.service.WorkflowStatsService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 工作流统计服务实现
 */
@Service
public class WorkflowStatsServiceImpl implements WorkflowStatsService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowStatsServiceImpl.class);

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final ManagementService managementService;
    private final RepositoryService repositoryService;

    public WorkflowStatsServiceImpl(TaskService taskService,
                                    RuntimeService runtimeService,
                                    HistoryService historyService,
                                    ManagementService managementService,
                                    RepositoryService repositoryService) {
        this.taskService = taskService;
        this.runtimeService = runtimeService;
        this.historyService = historyService;
        this.managementService = managementService;
        this.repositoryService = repositoryService;
    }

    @Override
    public Map<String, Object> getOverviewStats(String userId) {
        log.debug("获取任务统计概览：{}", userId);

        Map<String, Object> stats = new HashMap<>();

        long todoCount = taskService.createTaskQuery().taskAssignee(userId).count();
        stats.put("todoCount", todoCount);

        long doneCount = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .finished()
                .count();
        stats.put("doneCount", doneCount);

        long runningProcessCount = runtimeService.createProcessInstanceQuery()
                .startedBy(userId)
                .count();
        stats.put("runningProcessCount", runningProcessCount);

        long finishedProcessCount = historyService.createHistoricProcessInstanceQuery()
                .startedBy(userId)
                .finished()
                .count();
        stats.put("finishedProcessCount", finishedProcessCount);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getTaskTrendStats(String userId, String startDate, String endDate) {
        log.debug("获取任务趋势统计：{}, {} -> {}", userId, startDate, endDate);

        List<Map<String, Object>> result = new ArrayList<>();

        if (!hasText(startDate)) {
            startDate = LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (!hasText(endDate)) {
            endDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);

        while (!start.isAfter(end)) {
            String date = start.format(DateTimeFormatter.ISO_LOCAL_DATE);
            Map<String, Object> dayStats = new HashMap<>();
            dayStats.put("date", date);
            dayStats.put("createdCount", 0);
            dayStats.put("completedCount", 0);
            result.add(dayStats);
            start = start.plusDays(1);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getProcessDefinitionStats(String userId) {
        log.debug("获取流程定义统计：{}", userId);

        List<Map<String, Object>> result = new ArrayList<>();

        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(userId)
                .list();

        Map<String, Long> statsByProcessDef = tasks.stream()
                .collect(Collectors.groupingBy(Task::getProcessDefinitionId, Collectors.counting()));

        for (Map.Entry<String, Long> entry : statsByProcessDef.entrySet()) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("processDefinitionId", entry.getKey());
            stat.put("pendingCount", entry.getValue());

            ProcessDefinition processDef = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(entry.getKey())
                    .singleResult();
            if (processDef != null) {
                stat.put("processDefinitionName", processDef.getName());
            }

            long finishedCount = historyService.createHistoricTaskInstanceQuery()
                    .taskAssignee(userId)
                    .processDefinitionId(entry.getKey())
                    .finished()
                    .count();
            stat.put("completedCount", finishedCount);

            result.add(stat);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getTaskDurationStats(String processInstanceId) {
        log.debug("获取任务耗时统计：{}", processInstanceId);

        List<Map<String, Object>> result = new ArrayList<>();

        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceStartTime()
                .asc()
                .list();

        for (HistoricTaskInstance task : tasks) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("taskId", task.getId());
            stat.put("taskName", task.getName());
            stat.put("assignee", task.getAssignee());
            stat.put("startTime", task.getStartTime());
            stat.put("endTime", task.getEndTime());
            stat.put("duration", task.getDurationInMillis());

            result.add(stat);
        }

        return result;
    }

    @Override
    public Map<String, Object> getUserTaskCompletionStats(String userId, String startDate, String endDate) {
        log.debug("获取用户任务完成情况：{}, {} -> {}", userId, startDate, endDate);

        Map<String, Object> stats = new HashMap<>();

        if (!hasText(startDate)) {
            startDate = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (!hasText(endDate)) {
            endDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);

        long totalCount = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .count();
        stats.put("totalCount", totalCount);

        long completedCount = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .finished()
                .count();
        stats.put("completedCount", completedCount);

        double completionRate = totalCount > 0 ? completedCount * 100.0 / totalCount : 0;
        stats.put("completionRate", String.format("%.2f", completionRate) + "%");

        List<HistoricTaskInstance> finishedTasks = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .finished()
                .list();

        if (!finishedTasks.isEmpty()) {
            double avgDurationDouble = finishedTasks.stream()
                    .mapToLong(HistoricTaskInstance::getDurationInMillis)
                    .average()
                    .orElse(0.0);
            long avgDuration = (long) avgDurationDouble;
            stats.put("avgDuration", avgDuration);
            stats.put("avgDurationFormatted", formatDuration(avgDuration));
        }

        return stats;
    }

    @Override
    public List<Map<String, Object>> getDeptTaskStats(String deptId, String startDate, String endDate) {
        log.debug("获取部门任务统计：{}", deptId);

        return new ArrayList<>();
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "天 " + (hours % 24) + "小时";
        } else if (hours > 0) {
            return hours + "小时 " + (minutes % 60) + "分钟";
        } else if (minutes > 0) {
            return minutes + "分钟 " + (seconds % 60) + "秒";
        } else {
            return seconds + "秒";
        }
    }

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
package com.micro.platform.system.controller;

import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.service.WorkflowStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流统计控制器
 */
@Tag(name = "工作流统计", description = "工作流任务统计报表")
@RestController
@RequestMapping("/system/workflow/stats")
public class WorkflowStatsController {

    private final WorkflowStatsService workflowStatsService;

    public WorkflowStatsController(WorkflowStatsService workflowStatsService) {
        this.workflowStatsService = workflowStatsService;
    }

    @Operation(summary = "获取统计概览")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        String userId = SecurityUtil.getUserId().toString();
        return Result.success(workflowStatsService.getOverviewStats(userId));
    }

    @Operation(summary = "获取任务趋势统计")
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> getTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        String userId = SecurityUtil.getUserId().toString();
        return Result.success(workflowStatsService.getTaskTrendStats(userId, startDate, endDate));
    }

    @Operation(summary = "获取流程定义统计")
    @GetMapping("/process-definition")
    @PreAuthorize("@ss.hasPermission('system:workflow:query')")
    public Result<List<Map<String, Object>>> getProcessDefinitionStats() {
        String userId = SecurityUtil.getUserId().toString();
        return Result.success(workflowStatsService.getProcessDefinitionStats(userId));
    }

    @Operation(summary = "获取任务耗时统计")
    @GetMapping("/duration")
    @PreAuthorize("@ss.hasPermission('system:workflow:query')")
    public Result<List<Map<String, Object>>> getDurationStats(@RequestParam String processInstanceId) {
        return Result.success(workflowStatsService.getTaskDurationStats(processInstanceId));
    }

    @Operation(summary = "获取用户任务完成情况")
    @GetMapping("/completion")
    public Result<Map<String, Object>> getCompletionStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        String userId = SecurityUtil.getUserId().toString();
        return Result.success(workflowStatsService.getUserTaskCompletionStats(userId, startDate, endDate));
    }

    @Operation(summary = "获取部门任务统计")
    @GetMapping("/dept")
    @PreAuthorize("@ss.hasPermission('system:workflow:query')")
    public Result<List<Map<String, Object>>> getDeptStats(
            @RequestParam(required = false) String deptId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(workflowStatsService.getDeptTaskStats(deptId, startDate, endDate));
    }
}
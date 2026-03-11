package com.micro.platform.system.controller;

import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.service.WorkflowTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流任务 Controller
 */
@RestController
@RequestMapping("/system/workflow/task")
@Tag(name = "工作流任务管理")
public class WorkflowTaskController {

    private final WorkflowTaskService workflowTaskService;

    public WorkflowTaskController(WorkflowTaskService workflowTaskService) {
        this.workflowTaskService = workflowTaskService;
    }

    @GetMapping("/todo")
    @Operation(summary = "获取待办任务列表")
    @OperationLog(module = "工作流任务", type = OperationType.SELECT)
    public Result<List<Map<String, Object>>> getTodoTasks(
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        String userId = SecurityUtil.getUserId().toString();
        List<Map<String, Object>> list = workflowTaskService.getTodoTasks(userId, pageNum, pageSize);
        return Result.success(list);
    }

    @GetMapping("/done")
    @Operation(summary = "获取已办任务列表")
    @OperationLog(module = "工作流任务", type = OperationType.SELECT)
    public Result<List<Map<String, Object>>> getDoneTasks(
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        String userId = SecurityUtil.getUserId().toString();
        List<Map<String, Object>> list = workflowTaskService.getDoneTasks(userId, pageNum, pageSize);
        return Result.success(list);
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "获取任务详情")
    @PreAuthorize("@ss.hasPermission('system:workflow:query')")
    @OperationLog(module = "工作流任务", type = OperationType.SELECT)
    public Result<Map<String, Object>> getTask(@PathVariable String taskId) {
        Map<String, Object> result = workflowTaskService.getTask(taskId);
        return Result.success(result);
    }

    @PostMapping("/{taskId}/complete")
    @Operation(summary = "完成任务")
    @PreAuthorize("@ss.hasPermission('system:workflow:complete')")
    @OperationLog(module = "工作流任务", type = OperationType.UPDATE)
    public Result<Void> completeTask(@PathVariable String taskId,
                                      @RequestBody(required = false) Map<String, Object> variables) {
        String userId = SecurityUtil.getUserId().toString();
        workflowTaskService.completeTask(taskId, userId, variables);
        return Result.success();
    }

    @PostMapping("/{taskId}/delegate")
    @Operation(summary = "委派任务")
    @PreAuthorize("@ss.hasPermission('system:workflow:delegate')")
    @OperationLog(module = "工作流任务", type = OperationType.UPDATE)
    public Result<Void> delegateTask(@PathVariable String taskId,
                                      @RequestParam String delegateUser) {
        String userId = SecurityUtil.getUserId().toString();
        workflowTaskService.delegateTask(taskId, userId, delegateUser);
        return Result.success();
    }

    @PostMapping("/{taskId}/transfer")
    @Operation(summary = "转办任务")
    @PreAuthorize("@ss.hasPermission('system:workflow:transfer')")
    @OperationLog(module = "工作流任务", type = OperationType.UPDATE)
    public Result<Void> transferTask(@PathVariable String taskId,
                                      @RequestParam String newAssignee) {
        String userId = SecurityUtil.getUserId().toString();
        workflowTaskService.transferTask(taskId, userId, newAssignee);
        return Result.success();
    }

    @GetMapping("/{processInstanceId}/history")
    @Operation(summary = "获取任务历史")
    @PreAuthorize("@ss.hasPermission('system:workflow:query')")
    @OperationLog(module = "工作流任务", type = OperationType.SELECT)
    public Result<List<Map<String, Object>>> getTaskHistory(@PathVariable String processInstanceId) {
        List<Map<String, Object>> list = workflowTaskService.getTaskHistory(processInstanceId);
        return Result.success(list);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取任务统计信息")
    @OperationLog(module = "工作流任务", type = OperationType.SELECT)
    public Result<Map<String, Object>> getTaskStats() {
        String userId = SecurityUtil.getUserId().toString();
        Map<String, Object> result = workflowTaskService.getTaskStats(userId);
        return Result.success(result);
    }
}
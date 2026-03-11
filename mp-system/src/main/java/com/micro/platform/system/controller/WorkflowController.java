package com.micro.platform.system.controller;

import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作流 Controller
 */
@RestController
@RequestMapping("/system/workflow")
@Tag(name = "工作流管理")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/start/{processDefinitionKey}")
    @Operation(summary = "启动流程")
    @PreAuthorize("@ss.hasPermission('system:workflow:start')")
    @OperationLog(module = "工作流", type = OperationType.OTHER)
    public Result<Map<String, Object>> startProcess(@PathVariable String processDefinitionKey,
                                                     @RequestParam String businessKey,
                                                     @RequestBody(required = false) Map<String, Object> variables) {
        ProcessInstance instance = workflowService.startProcess(processDefinitionKey, businessKey, variables);
        Map<String, Object> result = new HashMap<>();
        result.put("processInstanceId", instance.getProcessInstanceId());
        result.put("businessKey", businessKey);
        result.put("processDefinitionId", instance.getProcessDefinitionId());
        return Result.success(result);
    }

    @GetMapping("/todo")
    @Operation(summary = "获取待办任务")
    @OperationLog(module = "工作流", type = OperationType.SELECT)
    public Result<List<Map<String, Object>>> getTodoTasks() {
        String userId = SecurityUtil.getUserId().toString();
        List<Task> tasks = workflowService.getTodoTasks(userId);
        List<Map<String, Object>> result = tasks.stream().map(task -> {
            Map<String, Object> map = new HashMap<>();
            map.put("taskId", task.getId());
            map.put("taskName", task.getName());
            map.put("assignee", task.getAssignee());
            map.put("createTime", task.getCreateTime());
            map.put("processInstanceId", task.getProcessInstanceId());
            map.put("processDefinitionId", task.getProcessDefinitionId());
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    @PostMapping("/complete/{taskId}")
    @Operation(summary = "完成任务")
    @PreAuthorize("@ss.hasPermission('system:workflow:complete')")
    @OperationLog(module = "工作流", type = OperationType.UPDATE)
    public Result<Void> completeTask(@PathVariable String taskId,
                                      @RequestBody(required = false) Map<String, Object> variables) {
        workflowService.completeTask(taskId, variables);
        return Result.success();
    }

    @GetMapping("/variables/{processInstanceId}")
    @Operation(summary = "获取流程变量")
    @PreAuthorize("@ss.hasPermission('system:workflow:query')")
    @OperationLog(module = "工作流", type = OperationType.SELECT)
    public Result<Map<String, Object>> getVariables(@PathVariable String processInstanceId) {
        Map<String, Object> variables = workflowService.getProcessVariables(processInstanceId);
        return Result.success(variables);
    }

    @DeleteMapping("/{processInstanceId}")
    @Operation(summary = "删除流程")
    @PreAuthorize("@ss.hasPermission('system:workflow:delete')")
    @OperationLog(module = "工作流", type = OperationType.DELETE)
    public Result<Void> deleteProcess(@PathVariable String processInstanceId,
                                       @RequestParam(required = false) String deleteReason) {
        workflowService.deleteProcess(processInstanceId, deleteReason != null ? deleteReason : "正常删除");
        return Result.success();
    }

    @PostMapping("/suspend/{processInstanceId}")
    @Operation(summary = "挂起流程")
    @PreAuthorize("@ss.hasPermission('system:workflow:suspend')")
    @OperationLog(module = "工作流", type = OperationType.OTHER)
    public Result<Void> suspendProcess(@PathVariable String processInstanceId) {
        workflowService.suspendProcess(processInstanceId);
        return Result.success();
    }

    @PostMapping("/activate/{processInstanceId}")
    @Operation(summary = "激活流程")
    @PreAuthorize("@ss.hasPermission('system:workflow:activate')")
    @OperationLog(module = "工作流", type = OperationType.OTHER)
    public Result<Void> activateProcess(@PathVariable String processInstanceId) {
        workflowService.activateProcess(processInstanceId);
        return Result.success();
    }

    @PostMapping("/deploy")
    @Operation(summary = "部署流程定义")
    @PreAuthorize("@ss.hasPermission('system:workflow:deploy')")
    @OperationLog(module = "工作流", type = OperationType.OTHER)
    public Result<Map<String, Object>> deploy(@RequestParam String name,
                                               @RequestParam String bpmnXml) {
        Map<String, Object> result = workflowService.deployProcessDefinition(name, bpmnXml);
        return Result.success(result);
    }

    @PostMapping("/save")
    @Operation(summary = "保存流程定义")
    @PreAuthorize("@ss.hasPermission('system:workflow:save')")
    @OperationLog(module = "工作流", type = OperationType.INSERT)
    public Result<Void> saveDefinition(@RequestBody Map<String, String> params) {
        String name = params.get("name");
        String bpmnXml = params.get("bpmnXml");
        String category = params.get("category");
        workflowService.saveProcessDefinition(name, bpmnXml, category);
        return Result.success();
    }

    @GetMapping("/definition/list")
    @Operation(summary = "获取流程定义列表")
    @PreAuthorize("@ss.hasPermission('system:workflow:query')")
    @OperationLog(module = "工作流", type = OperationType.SELECT)
    public Result<List<Map<String, Object>>> getProcessDefinitions(@RequestParam(required = false) String category) {
        List<Map<String, Object>> result = workflowService.getProcessDefinitions(category);
        return Result.success(result);
    }

    @GetMapping("/definition/{processDefinitionId}")
    @Operation(summary = "获取流程定义详情")
    @PreAuthorize("@ss.hasPermission('system:workflow:query')")
    @OperationLog(module = "工作流", type = OperationType.SELECT)
    public Result<Map<String, Object>> getProcessDefinition(@PathVariable String processDefinitionId) {
        Map<String, Object> result = workflowService.getProcessDefinition(processDefinitionId);
        return Result.success(result);
    }

    @DeleteMapping("/definition/{deploymentId}")
    @Operation(summary = "删除流程定义")
    @PreAuthorize("@ss.hasPermission('system:workflow:delete')")
    @OperationLog(module = "工作流", type = OperationType.DELETE)
    public Result<Void> deleteProcessDefinition(@PathVariable String deploymentId) {
        workflowService.deleteProcessDefinition(deploymentId);
        return Result.success();
    }

    @GetMapping("/definition/bpmn/{processDefinitionId}")
    @Operation(summary = "获取 BPMN 流程定义 XML")
    @PreAuthorize("@ss.hasPermission('system:workflow:query')")
    @OperationLog(module = "工作流", type = OperationType.SELECT)
    public Result<Map<String, String>> getProcessDefinitionBpmn(@PathVariable String processDefinitionId) {
        String bpmnXml = workflowService.getProcessDefinitionBpmn(processDefinitionId);
        Map<String, String> result = new HashMap<>();
        result.put("bpmnXml", bpmnXml);
        return Result.success(result);
    }
}
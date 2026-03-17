package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.WorkflowFormBinding;
import com.micro.platform.system.service.WorkflowFormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流表单管理 Controller
 */
@RestController
@RequestMapping("/system/workflow-form")
@Tag(name = "工作流表单管理")
public class WorkflowFormController {

    private final WorkflowFormService workflowFormService;

    public WorkflowFormController(WorkflowFormService workflowFormService) {
        this.workflowFormService = workflowFormService;
    }

    @GetMapping("/list")
    @Operation(summary = "获取表单绑定列表")
    @PreAuthorize("@ss.hasPermission('system:workflow:query')")
    @OperationLog(module = "工作流表单", type = OperationType.QUERY)
    public Result<Page<WorkflowFormBinding>> getFormBindingList(
            @RequestParam(required = false) String processDefinitionKey,
            @RequestParam(required = false) String taskDefinitionKey,
            @RequestParam(required = false) String formName,
            @RequestParam(required = false) String formKey,
            @RequestParam(required = false) Integer formType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<WorkflowFormBinding> page = workflowFormService.getFormBindingsPage(
                processDefinitionKey, taskDefinitionKey, formName, formKey, formType, status, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/process/{processDefinitionKey}")
    @Operation(summary = "获取流程的所有表单绑定")
    @PreAuthorize("@ss.hasPermission('system:workflow:query')")
    @OperationLog(module = "工作流表单", type = OperationType.QUERY)
    public Result<List<WorkflowFormBinding>> getProcessBindings(@PathVariable String processDefinitionKey) {
        List<WorkflowFormBinding> list = workflowFormService.getProcessBindings(processDefinitionKey);
        return Result.success(list);
    }

    @PostMapping("/bind")
    @Operation(summary = "绑定表单")
    @PreAuthorize("@ss.hasPermission('system:workflow:save')")
    @OperationLog(module = "工作流表单", type = OperationType.INSERT)
    public Result<Void> bindForm(@RequestBody WorkflowFormBinding binding) {
        workflowFormService.bindForm(binding);
        return Result.success();
    }

    @PostMapping("/unbind")
    @Operation(summary = "解除绑定")
    @PreAuthorize("@ss.hasPermission('system:workflow:delete')")
    @OperationLog(module = "工作流表单", type = OperationType.DELETE)
    public Result<Void> unbindForm(@RequestParam String processDefinitionKey,
                                    @RequestParam(required = false) String taskDefinitionKey) {
        workflowFormService.unbindForm(processDefinitionKey, taskDefinitionKey);
        return Result.success();
    }

    @GetMapping("/available/{processDefinitionKey}")
    @Operation(summary = "获取可用的表单列表")
    @PreAuthorize("@ss.hasPermission('system:workflow:query')")
    @OperationLog(module = "工作流表单", type = OperationType.QUERY)
    public Result<List<Map<String, Object>>> getAvailableForms(@PathVariable String processDefinitionKey) {
        List<Map<String, Object>> list = workflowFormService.getAvailableForms(processDefinitionKey);
        return Result.success(list);
    }

    @GetMapping("/start/{processKey}")
    @Operation(summary = "获取启动表单")
    @PreAuthorize("@ss.hasPermission('system:workflow:query')")
    @OperationLog(module = "工作流表单", type = OperationType.QUERY)
    public Result<Map<String, Object>> getStartForm(@PathVariable String processKey) {
        Map<String, Object> form = workflowFormService.getStartForm(processKey);
        return Result.success(form);
    }

    @GetMapping("/task/{taskDefinitionKey}")
    @Operation(summary = "获取任务表单")
    @PreAuthorize("@ss.hasPermission('system:workflow:query')")
    @OperationLog(module = "工作流表单", type = OperationType.QUERY)
    public Result<Map<String, Object>> getTaskForm(@PathVariable String taskDefinitionKey) {
        Map<String, Object> form = workflowFormService.getTaskForm(taskDefinitionKey);
        return Result.success(form);
    }

    @PostMapping("/status")
    @Operation(summary = "更新表单状态")
    @PreAuthorize("@ss.hasPermission('system:workflow:save')")
    @OperationLog(module = "工作流表单", type = OperationType.UPDATE)
    public Result<Void> updateFormStatus(@RequestBody WorkflowFormBinding binding) {
        workflowFormService.updateFormStatus(binding);
        return Result.success();
    }
}

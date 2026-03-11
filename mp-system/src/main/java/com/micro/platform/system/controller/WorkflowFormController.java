package com.micro.platform.system.controller;

import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.FormData;
import com.micro.platform.system.entity.FormDefinition;
import com.micro.platform.system.service.FormDefinitionService;
import com.micro.platform.system.service.WorkflowFormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流表单集成 Controller
 */
@RestController
@RequestMapping("/system/workflow-form")
@Tag(name = "工作流表单集成")
public class WorkflowFormController {

    private final WorkflowFormService workflowFormService;
    private final FormDefinitionService formDefinitionService;

    public WorkflowFormController(WorkflowFormService workflowFormService,
                                  FormDefinitionService formDefinitionService) {
        this.workflowFormService = workflowFormService;
        this.formDefinitionService = formDefinitionService;
    }

    @PostMapping("/bind")
    @Operation(summary = "绑定表单到流程任务")
    @PreAuthorize("@ss.hasPermission('system:workflow-form:bind')")
    @OperationLog(module = "工作流表单", type = OperationType.INSERT)
    public Result<Void> bindForm(@RequestBody Map<String, String> params) {
        String processDefinitionKey = params.get("processDefinitionKey");
        String taskDefinitionKey = params.get("taskDefinitionKey");
        String formCode = params.get("formCode");
        Integer formType = Integer.valueOf(params.get("formType"));

        workflowFormService.bindFormToProcess(processDefinitionKey, taskDefinitionKey, formCode, formType);
        return Result.success();
    }

    @GetMapping("/bound-form")
    @Operation(summary = "获取流程绑定的表单")
    @PreAuthorize("@ss.hasPermission('system:workflow-form:list')")
    public Result<FormDefinition> getBoundForm(@RequestParam String processDefinitionKey,
                                               @RequestParam String taskDefinitionKey,
                                               @RequestParam Integer formType) {
        FormDefinition formDefinition = workflowFormService.getBoundForm(processDefinitionKey, taskDefinitionKey, formType);
        return Result.success(formDefinition);
    }

    @PostMapping("/start")
    @Operation(summary = "启动流程并提交表单")
    @PreAuthorize("@ss.hasPermission('system:workflow-form:start')")
    @OperationLog(module = "工作流表单", type = OperationType.INSERT)
    public Result<Map<String, String>> startProcess(@RequestBody Map<String, Object> params) {
        String processDefinitionKey = (String) params.get("processDefinitionKey");
        String businessKey = (String) params.get("businessKey");
        String formCode = (String) params.get("formCode");
        @SuppressWarnings("unchecked")
        Map<String, Object> formData = (Map<String, Object>) params.get("formData");

        ProcessInstance instance = workflowFormService.startProcessWithForm(processDefinitionKey, businessKey, formCode, formData);

        Map<String, String> result = Map.of(
                "processInstanceId", instance.getId(),
                "businessKey", instance.getBusinessKey()
        );
        return Result.success(result);
    }

    @PostMapping("/complete")
    @Operation(summary = "完成任务并提交表单")
    @PreAuthorize("@ss.hasPermission('system:workflow-form:complete')")
    @OperationLog(module = "工作流表单", type = OperationType.UPDATE)
    public Result<Void> completeTask(@RequestBody Map<String, Object> params) {
        String taskId = (String) params.get("taskId");
        String formCode = (String) params.get("formCode");
        @SuppressWarnings("unchecked")
        Map<String, Object> formData = (Map<String, Object>) params.get("formData");
        Boolean saveDraft = (Boolean) params.get("saveDraft");

        workflowFormService.completeTaskWithForm(taskId, formCode, formData, saveDraft);
        return Result.success();
    }

    @GetMapping("/task-form")
    @Operation(summary = "获取任务表单")
    @PreAuthorize("@ss.hasPermission('system:workflow-form:list')")
    public Result<FormDefinition> getTaskForm(@RequestParam String taskId) {
        FormDefinition formDefinition = workflowFormService.getTaskForm(taskId);
        return Result.success(formDefinition);
    }

    @PostMapping("/draft")
    @Operation(summary = "保存表单草稿")
    @PreAuthorize("@ss.hasPermission('system:workflow-form:draft')")
    @OperationLog(module = "工作流表单", type = OperationType.INSERT)
    public Result<Void> saveDraft(@RequestBody Map<String, Object> params) {
        String processInstanceId = (String) params.get("processInstanceId");
        String taskDefinitionKey = (String) params.get("taskDefinitionKey");
        String formCode = (String) params.get("formCode");
        @SuppressWarnings("unchecked")
        Map<String, Object> formData = (Map<String, Object>) params.get("formData");

        workflowFormService.saveFormDataDraft(processInstanceId, taskDefinitionKey, formCode, formData);
        return Result.success();
    }

    @GetMapping("/draft")
    @Operation(summary = "获取表单草稿")
    @PreAuthorize("@ss.hasPermission('system:workflow-form:list')")
    public Result<FormData> getDraft(@RequestParam String processInstanceId,
                                     @RequestParam String taskDefinitionKey) {
        FormData formData = workflowFormService.getFormDataDraft(processInstanceId, taskDefinitionKey);
        return Result.success(formData);
    }

    @GetMapping("/history")
    @Operation(summary = "获取流程表单历史")
    @PreAuthorize("@ss.hasPermission('system:workflow-form:list')")
    public Result<List<FormData>> getHistory(@RequestParam String processInstanceId) {
        List<FormData> history = workflowFormService.getProcessFormHistory(processInstanceId);
        return Result.success(history);
    }

    @GetMapping("/form-definition/{formCode}")
    @Operation(summary = "获取表单定义")
    @PreAuthorize("@ss.hasPermission('system:workflow-form:list')")
    public Result<FormDefinition> getFormDefinition(@PathVariable String formCode) {
        FormDefinition formDefinition = formDefinitionService.selectByCode(formCode);
        return Result.success(formDefinition);
    }
}
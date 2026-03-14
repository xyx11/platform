package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.PageResult;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.WfFormBinding;
import com.micro.platform.system.service.WfFormBindingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流表单绑定 Controller
 */
@Tag(name = "工作流表单绑定", description = "工作流表单绑定管理")
@RestController
@RequestMapping("/system/workflow-form")
public class WfFormBindingController {

    private final WfFormBindingService wfFormBindingService;

    public WfFormBindingController(WfFormBindingService wfFormBindingService) {
        this.wfFormBindingService = wfFormBindingService;
    }

    @Operation(summary = "获取表单绑定列表")
    @PreAuthorize("@ss.hasPermission('system:workflow-form:query')")
    @GetMapping("/list")
    public Result<PageResult<WfFormBinding>> list(WfFormBinding formBinding,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<WfFormBinding> page = wfFormBindingService.selectFormBindingPage(formBinding, pageNum, pageSize);
        return Result.success(PageResult.build(page));
    }

    @Operation(summary = "获取流程的所有表单绑定")
    @PreAuthorize("@ss.hasPermission('system:workflow-form:query')")
    @GetMapping("/process/{processDefinitionKey}")
    public Result<List<WfFormBinding>> getProcessBindings(@PathVariable String processDefinitionKey) {
        return Result.success(wfFormBindingService.getProcessFormBindings(processDefinitionKey));
    }

    @Operation(summary = "绑定表单")
    @OperationLog(module = "工作流表单绑定", type = OperationType.INSERT, description = "绑定表单")
    @PreAuthorize("@ss.hasPermission('system:workflow-form:add')")
    @PostMapping("/bind")
    public Result<Void> bind(@RequestBody WfFormBinding formBinding) {
        wfFormBindingService.bindForm(formBinding);
        return Result.success();
    }

    @Operation(summary = "解除绑定")
    @OperationLog(module = "工作流表单绑定", type = OperationType.DELETE, description = "解除绑定")
    @PreAuthorize("@ss.hasPermission('system:workflow-form:remove')")
    @PostMapping("/unbind")
    public Result<Void> unbind(@RequestParam String processDefinitionKey,
                                @RequestParam String taskDefinitionKey,
                                @RequestParam Integer formType) {
        wfFormBindingService.unbindForm(processDefinitionKey, taskDefinitionKey, formType);
        return Result.success();
    }

    @Operation(summary = "获取可用的表单列表")
    @PreAuthorize("@ss.hasPermission('system:workflow-form:query')")
    @GetMapping("/available/{processDefinitionKey}")
    public Result<List<Map<String, Object>>> getAvailableForms(@PathVariable String processDefinitionKey) {
        return Result.success(wfFormBindingService.getAvailableForms(processDefinitionKey));
    }
}
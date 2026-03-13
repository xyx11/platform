package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.PageResult;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.FormDefinition;
import com.micro.platform.system.service.FormDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 动态表单定义 Controller
 */
@RestController
@RequestMapping("/system/form-definition")
@Tag(name = "动态表单定义管理")
public class FormDefinitionController {

    private final FormDefinitionService formDefinitionService;

    public FormDefinitionController(FormDefinitionService formDefinitionService) {
        this.formDefinitionService = formDefinitionService;
    }

    @GetMapping("/list")
    @Operation(summary = "查询表单定义列表")
    @PreAuthorize("@ss.hasPermission('system:form-definition:list')")
    @OperationLog(module = "动态表单定义", type = OperationType.QUERY)
    public Result<PageResult<FormDefinition>> list(FormDefinition formDefinition,
                                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<FormDefinition> page = formDefinitionService.selectFormDefinitionPage(formDefinition, pageNum, pageSize);
        PageResult<FormDefinition> result = PageResult.build(page);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取表单定义详情")
    @PreAuthorize("@ss.hasPermission('system:form-definition:query')")
    @OperationLog(module = "动态表单定义", type = OperationType.QUERY)
    public Result<FormDefinition> get(@PathVariable Long id) {
        FormDefinition formDefinition = formDefinitionService.getById(id);
        return Result.success(formDefinition);
    }

    @GetMapping("/code/{formCode}")
    @Operation(summary = "根据编码获取表单定义")
    public Result<FormDefinition> getByCode(@PathVariable String formCode) {
        FormDefinition formDefinition = formDefinitionService.selectByCode(formCode);
        return Result.success(formDefinition);
    }

    @PostMapping
    @Operation(summary = "创建表单定义")
    @PreAuthorize("@ss.hasPermission('system:form-definition:add')")
    @OperationLog(module = "动态表单定义", type = OperationType.INSERT)
    public Result<Void> create(@RequestBody FormDefinition formDefinition) {
        formDefinitionService.createFormDefinition(formDefinition);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "更新表单定义")
    @PreAuthorize("@ss.hasPermission('system:form-definition:edit')")
    @OperationLog(module = "动态表单定义", type = OperationType.UPDATE)
    public Result<Void> update(@RequestBody FormDefinition formDefinition) {
        formDefinitionService.updateFormDefinition(formDefinition);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除表单定义")
    @PreAuthorize("@ss.hasPermission('system:form-definition:remove')")
    @OperationLog(module = "动态表单定义", type = OperationType.DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        formDefinitionService.removeById(id);
        return Result.success();
    }

    @PostMapping("/publish/{id}")
    @Operation(summary = "发布表单")
    @PreAuthorize("@ss.hasPermission('system:form-definition:publish')")
    @OperationLog(module = "动态表单定义", type = OperationType.OTHER)
    public Result<Void> publish(@PathVariable Long id) {
        formDefinitionService.publishForm(id);
        return Result.success();
    }

    @PostMapping("/disable/{id}")
    @Operation(summary = "停用表单")
    @PreAuthorize("@ss.hasPermission('system:form-definition:disable')")
    @OperationLog(module = "动态表单定义", type = OperationType.OTHER)
    public Result<Void> disable(@PathVariable Long id) {
        formDefinitionService.disableForm(id);
        return Result.success();
    }
}
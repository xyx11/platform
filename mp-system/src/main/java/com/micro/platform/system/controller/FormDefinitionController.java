package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.FormDefinition;
import com.micro.platform.system.service.FormDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 表单定义 Controller
 */
@RestController
@RequestMapping("/system/form-definition")
@Tag(name = "表单定义管理")
public class FormDefinitionController {

    private final FormDefinitionService formDefinitionService;

    public FormDefinitionController(FormDefinitionService formDefinitionService) {
        this.formDefinitionService = formDefinitionService;
    }

    @GetMapping("/list")
    @Operation(summary = "获取表单定义列表")
    @PreAuthorize("@ss.hasPermission('system:workflow:query')")
    @OperationLog(module = "表单定义", type = OperationType.QUERY)
    public Result<List<FormDefinition>> getFormDefinitionList(
            @RequestParam(required = false) String formName,
            @RequestParam(required = false) String formCode,
            @RequestParam(required = false) Integer status) {
        
        LambdaQueryWrapper<FormDefinition> wrapper = new LambdaQueryWrapper<>();
        if (formName != null && !formName.isEmpty()) {
            wrapper.like(FormDefinition::getFormName, formName);
        }
        if (formCode != null && !formCode.isEmpty()) {
            wrapper.eq(FormDefinition::getFormCode, formCode);
        }
        if (status != null) {
            wrapper.eq(FormDefinition::getStatus, status);
        }
        wrapper.eq(FormDefinition::getDelFlag, 0);
        
        List<FormDefinition> list = formDefinitionService.list(wrapper);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取表单定义详情")
    @PreAuthorize("@ss.hasPermission('system:workflow:query')")
    @OperationLog(module = "表单定义", type = OperationType.QUERY)
    public Result<FormDefinition> getFormDefinition(@PathVariable Long id) {
        FormDefinition formDefinition = formDefinitionService.getById(id);
        return Result.success(formDefinition);
    }

    @PostMapping
    @Operation(summary = "新增表单定义")
    @PreAuthorize("@ss.hasPermission('system:workflow:save')")
    @OperationLog(module = "表单定义", type = OperationType.INSERT)
    public Result<Void> addFormDefinition(@RequestBody FormDefinition formDefinition) {
        formDefinitionService.save(formDefinition);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改表单定义")
    @PreAuthorize("@ss.hasPermission('system:workflow:save')")
    @OperationLog(module = "表单定义", type = OperationType.UPDATE)
    public Result<Void> updateFormDefinition(@RequestBody FormDefinition formDefinition) {
        formDefinitionService.updateById(formDefinition);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除表单定义")
    @PreAuthorize("@ss.hasPermission('system:workflow:delete')")
    @OperationLog(module = "表单定义", type = OperationType.DELETE)
    public Result<Void> deleteFormDefinition(@PathVariable Long id) {
        formDefinitionService.removeById(id);
        return Result.success();
    }
}

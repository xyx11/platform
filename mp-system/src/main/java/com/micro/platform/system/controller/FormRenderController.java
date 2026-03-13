package com.micro.platform.system.controller;

import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.service.FormRenderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 表单渲染 Controller
 */
@RestController
@RequestMapping("/system/form-render")
@Tag(name = "表单渲染")
public class FormRenderController {

    private final FormRenderService formRenderService;

    public FormRenderController(FormRenderService formRenderService) {
        this.formRenderService = formRenderService;
    }

    @GetMapping("/{formCode}")
    @Operation(summary = "渲染表单")
    @OperationLog(module = "表单渲染", type = OperationType.QUERY)
    public Result<Map<String, Object>> render(@PathVariable String formCode) {
        Map<String, Object> form = formRenderService.renderForm(formCode);
        return Result.success(form);
    }

    @GetMapping("/{formCode}/fields")
    @Operation(summary = "渲染表单字段")
    @OperationLog(module = "表单渲染", type = OperationType.QUERY)
    public Result<Map<String, Object>> fields(@PathVariable String formCode) {
        Map<String, Object> fields = formRenderService.renderFormFields(formCode);
        return Result.success(fields);
    }

    @GetMapping("/{formCode}/schema")
    @Operation(summary = "获取表单 schema")
    @OperationLog(module = "表单渲染", type = OperationType.QUERY)
    public Result<String> schema(@PathVariable String formCode) {
        String schema = formRenderService.getFormSchema(formCode);
        return Result.success(schema);
    }

    @PostMapping("/validate")
    @Operation(summary = "验证表单数据")
    @OperationLog(module = "表单渲染", type = OperationType.OTHER)
    public Result<Map<String, Object>> validate(@RequestParam String formCode,
                                                 @RequestBody Map<String, Object> formData) {
        Map<String, Object> result = formRenderService.validateFormData(formCode, formData);
        return Result.success(result);
    }
}
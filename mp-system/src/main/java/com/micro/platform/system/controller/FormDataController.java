package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.PageResult;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.FormData;
import com.micro.platform.system.service.FormDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 动态表单数据 Controller
 */
@RestController
@RequestMapping("/system/form-data")
@Tag(name = "动态表单数据管理")
public class FormDataController {

    private final FormDataService formDataService;

    public FormDataController(FormDataService formDataService) {
        this.formDataService = formDataService;
    }

    @GetMapping("/list")
    @Operation(summary = "查询表单数据列表")
    @PreAuthorize("@ss.hasPermission('system:form-data:list')")
    @OperationLog(module = "动态表单数据", type = OperationType.SELECT)
    public Result<PageResult<FormData>> list(@RequestParam(required = false) Long formId,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<FormData> page = formDataService.selectFormDataPage(formId, pageNum, pageSize);
        PageResult<FormData> result = PageResult.build(page);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取表单数据详情")
    @PreAuthorize("@ss.hasPermission('system:form-data:query')")
    @OperationLog(module = "动态表单数据", type = OperationType.SELECT)
    public Result<FormData> get(@PathVariable Long id) {
        FormData formData = formDataService.getFormDataDetail(id);
        return Result.success(formData);
    }

    @PostMapping("/submit/{formId}")
    @Operation(summary = "提交表单数据")
    @OperationLog(module = "动态表单数据", type = OperationType.INSERT)
    public Result<Void> submit(@PathVariable Long formId, @RequestBody Map<String, Object> data) {
        formDataService.submitFormData(formId, data);
        return Result.success();
    }

    @PostMapping("/save/{formId}")
    @Operation(summary = "保存表单数据（草稿）")
    @OperationLog(module = "动态表单数据", type = OperationType.INSERT)
    public Result<Void> save(@PathVariable Long formId, @RequestBody Map<String, Object> data) {
        formDataService.saveFormData(formId, data);
        return Result.success();
    }

    @PostMapping("/audit/{id}")
    @Operation(summary = "审核表单数据")
    @PreAuthorize("@ss.hasPermission('system:form-data:audit')")
    @OperationLog(module = "动态表单数据", type = OperationType.UPDATE)
    public Result<Void> audit(@PathVariable Long id,
                              @RequestParam Integer status,
                              @RequestParam(required = false) String remark) {
        formDataService.auditFormData(id, status, remark);
        return Result.success();
    }

    @DeleteMapping("/{ids}")
    @Operation(summary = "删除表单数据")
    @PreAuthorize("@ss.hasPermission('system:form-data:remove')")
    @OperationLog(module = "动态表单数据", type = OperationType.DELETE)
    public Result<Void> delete(@PathVariable List<Long> ids) {
        formDataService.deleteFormData(ids);
        return Result.success();
    }
}
package com.micro.platform.system.controller;

import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysDept;
import com.micro.platform.system.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 */
@Tag(name = "部门管理", description = "部门增删改查")
@RestController
@RequestMapping("/system/dept")
public class SysDeptController {

    private final SysDeptService sysDeptService;

    public SysDeptController(SysDeptService sysDeptService) {
        this.sysDeptService = sysDeptService;
    }

    @Operation(summary = "获取部门列表")
    @GetMapping("/list")
    public Result<List<SysDept>> list() {
        return Result.success(sysDeptService.getDeptTree());
    }

    @Operation(summary = "获取部门详情")
    @GetMapping("/{deptId}")
    public Result<SysDept> get(@PathVariable Long deptId) {
        return Result.success(sysDeptService.getById(deptId));
    }

    @Operation(summary = "新增部门")
    @OperationLog(module = "部门管理", type = OperationType.CREATE, description = "新增部门")
    @PreAuthorize("hasAuthority('system:dept:add')")
    @PostMapping
    public Result<Void> add(@RequestBody SysDept dept) {
        dept.setCreateBy(SecurityUtil.getUserId());
        sysDeptService.save(dept);
        return Result.success();
    }

    @Operation(summary = "修改部门")
    @OperationLog(module = "部门管理", type = OperationType.UPDATE, description = "修改部门")
    @PreAuthorize("hasAuthority('system:dept:edit')")
    @PutMapping
    public Result<Void> update(@RequestBody SysDept dept) {
        dept.setUpdateBy(SecurityUtil.getUserId());
        sysDeptService.updateById(dept);
        return Result.success();
    }

    @Operation(summary = "删除部门")
    @OperationLog(module = "部门管理", type = OperationType.DELETE, description = "删除部门")
    @PreAuthorize("hasAuthority('system:dept:remove')")
    @DeleteMapping("/{deptId}")
    public Result<Void> remove(@PathVariable Long deptId) {
        sysDeptService.removeById(deptId);
        return Result.success();
    }
}

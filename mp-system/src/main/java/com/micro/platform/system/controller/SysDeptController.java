package com.micro.platform.system.controller;

import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysDept;
import com.micro.platform.system.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public Result<List<SysDept>> list(SysDept dept) {
        return Result.success(sysDeptService.selectDeptTree(dept));
    }

    @Operation(summary = "获取部门树")
    @GetMapping("/tree")
    public Result<List<SysDept>> tree() {
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

    @Operation(summary = "导出部门数据")
    @OperationLog(module = "部门管理", type = OperationType.EXPORT, description = "导出部门数据")
    @PreAuthorize("hasAuthority('system:dept:query')")
    @GetMapping("/export")
    public void export(HttpServletResponse response, SysDept dept) {
        sysDeptService.exportDept(response, dept);
    }

    @Operation(summary = "批量导出部门数据")
    @OperationLog(module = "部门管理", type = OperationType.EXPORT, description = "批量导出部门数据")
    @PreAuthorize("hasAuthority('system:dept:query')")
    @PostMapping("/export/batch")
    public void exportBatch(HttpServletResponse response, @RequestBody List<Long> deptIds) {
        sysDeptService.exportDeptBatch(response, deptIds);
    }

    @Operation(summary = "批量删除部门")
    @OperationLog(module = "部门管理", type = OperationType.DELETE, description = "批量删除部门")
    @PreAuthorize("hasAuthority('system:dept:remove')")
    @DeleteMapping("/batch")
    public Result<Void> batchRemove(@RequestBody List<Long> deptIds) {
        sysDeptService.removeBatchByIds(deptIds);
        return Result.success();
    }

    @Operation(summary = "获取部门统计信息")
    @PreAuthorize("hasAuthority('system:dept:query')")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(@RequestParam(required = false) Long deptId) {
        return Result.success(sysDeptService.getDeptStats(deptId));
    }

    @Operation(summary = "获取部门用户数量")
    @PreAuthorize("hasAuthority('system:dept:query')")
    @GetMapping("/user/count")
    public Result<Integer> userCount(@RequestParam Long deptId) {
        return Result.success(sysDeptService.getDeptUserCount(deptId));
    }

    @Operation(summary = "获取部门及子部门用户总数")
    @PreAuthorize("hasAuthority('system:dept:query')")
    @GetMapping("/user/count/with-children")
    public Result<Integer> userCountWithChildren(@RequestParam Long deptId) {
        return Result.success(sysDeptService.getDeptWithChildrenUserCount(deptId));
    }

    @Operation(summary = "获取子部门 ID 列表")
    @PreAuthorize("hasAuthority('system:dept:query')")
    @GetMapping("/children/ids")
    public Result<List<Long>> childrenIds(@RequestParam Long deptId) {
        return Result.success(sysDeptService.getDeptChildIds(deptId));
    }
}
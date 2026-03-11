package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.PageResult;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.SysDataPermission;
import com.micro.platform.system.service.SysDataPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据权限规则 Controller
 */
@RestController
@RequestMapping("/system/data-permission")
@Tag(name = "细粒度数据权限管理")
public class SysDataPermissionController {

    private final SysDataPermissionService permissionService;

    public SysDataPermissionController(SysDataPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/list")
    @Operation(summary = "查询数据权限规则列表")
    @PreAuthorize("@ss.hasPermission('system:data-permission:list')")
    @OperationLog(module = "细粒度数据权限", type = OperationType.SELECT)
    public Result<PageResult<SysDataPermission>> list(SysDataPermission permission,
                                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysDataPermission> page = permissionService.selectPermissionPage(permission, pageNum, pageSize);
        PageResult<SysDataPermission> result = PageResult.build(page);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取数据权限规则详情")
    @PreAuthorize("@ss.hasPermission('system:data-permission:query')")
    @OperationLog(module = "细粒度数据权限", type = OperationType.SELECT)
    public Result<SysDataPermission> get(@PathVariable Long id) {
        SysDataPermission permission = permissionService.getById(id);
        return Result.success(permission);
    }

    @GetMapping("/role/{roleId}")
    @Operation(summary = "获取角色的数据权限规则")
    @PreAuthorize("@ss.hasPermission('system:data-permission:query')")
    @OperationLog(module = "细粒度数据权限", type = OperationType.SELECT)
    public Result<List<SysDataPermission>> getByRole(@PathVariable Long roleId) {
        List<SysDataPermission> permissions = permissionService.selectByRoleId(roleId);
        return Result.success(permissions);
    }

    @PostMapping
    @Operation(summary = "创建数据权限规则")
    @PreAuthorize("@ss.hasPermission('system:data-permission:add')")
    @OperationLog(module = "细粒度数据权限", type = OperationType.INSERT)
    public Result<Void> create(@RequestBody SysDataPermission permission) {
        permissionService.createPermission(permission);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "更新数据权限规则")
    @PreAuthorize("@ss.hasPermission('system:data-permission:edit')")
    @OperationLog(module = "细粒度数据权限", type = OperationType.UPDATE)
    public Result<Void> update(@RequestBody SysDataPermission permission) {
        permissionService.updatePermission(permission);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除数据权限规则")
    @PreAuthorize("@ss.hasPermission('system:data-permission:remove')")
    @OperationLog(module = "细粒度数据权限", type = OperationType.DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return Result.success();
    }
}
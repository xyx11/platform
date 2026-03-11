package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.PageResult;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.SysTenantPackage;
import com.micro.platform.system.service.SysTenantPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户套餐 Controller
 */
@RestController
@RequestMapping("/system/tenant-package")
@Tag(name = "租户套餐管理")
public class SysTenantPackageController {

    private final SysTenantPackageService packageService;

    public SysTenantPackageController(SysTenantPackageService packageService) {
        this.packageService = packageService;
    }

    @GetMapping("/list")
    @Operation(summary = "查询套餐列表")
    @PreAuthorize("@ss.hasPermission('system:tenant-package:list')")
    @OperationLog(module = "租户套餐", type = OperationType.SELECT)
    public Result<PageResult<SysTenantPackage>> list(SysTenantPackage pkg,
                                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysTenantPackage> page = packageService.selectPackagePage(pkg, pageNum, pageSize);
        PageResult<SysTenantPackage> result = PageResult.build(page);
        return Result.success(result);
    }

    @GetMapping("/available")
    @Operation(summary = "获取可用套餐列表")
    public Result<List<SysTenantPackage>> available() {
        List<SysTenantPackage> packages = packageService.selectAvailablePackages();
        return Result.success(packages);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取套餐详情")
    @PreAuthorize("@ss.hasPermission('system:tenant-package:query')")
    @OperationLog(module = "租户套餐", type = OperationType.SELECT)
    public Result<SysTenantPackage> get(@PathVariable Long id) {
        SysTenantPackage pkg = packageService.getById(id);
        return Result.success(pkg);
    }

    @PostMapping
    @Operation(summary = "创建套餐")
    @PreAuthorize("@ss.hasPermission('system:tenant-package:add')")
    @OperationLog(module = "租户套餐", type = OperationType.INSERT)
    public Result<Void> create(@RequestBody SysTenantPackage pkg) {
        packageService.createPackage(pkg);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "更新套餐")
    @PreAuthorize("@ss.hasPermission('system:tenant-package:edit')")
    @OperationLog(module = "租户套餐", type = OperationType.UPDATE)
    public Result<Void> update(@RequestBody SysTenantPackage pkg) {
        packageService.updatePackage(pkg);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除套餐")
    @PreAuthorize("@ss.hasPermission('system:tenant-package:remove')")
    @OperationLog(module = "租户套餐", type = OperationType.DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        packageService.removeById(id);
        return Result.success();
    }

    @PostMapping("/disable/{id}")
    @Operation(summary = "停用套餐")
    @PreAuthorize("@ss.hasPermission('system:tenant-package:disable')")
    @OperationLog(module = "租户套餐", type = OperationType.OTHER)
    public Result<Void> disable(@PathVariable Long id) {
        packageService.disablePackage(id);
        return Result.success();
    }
}
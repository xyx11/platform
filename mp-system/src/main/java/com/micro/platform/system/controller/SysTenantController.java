package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.interceptor.TenantContext;
import com.micro.platform.common.core.result.PageResult;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.SysTenant;
import com.micro.platform.system.service.SysTenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 租户管理 Controller
 */
@RestController
@RequestMapping("/system/tenant")
@Tag(name = "租户管理")
public class SysTenantController {

    private final SysTenantService tenantService;

    public SysTenantController(SysTenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping("/list")
    @Operation(summary = "查询租户列表")
    @PreAuthorize("@ss.hasPermission('system:tenant:list')")
    @OperationLog(module = "租户管理", type = OperationType.SELECT)
    public Result<PageResult<SysTenant>> list(SysTenant tenant,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysTenant> page = tenantService.selectTenantPage(tenant, pageNum, pageSize);
        PageResult<SysTenant> result = PageResult.build(page);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取租户详情")
    @PreAuthorize("@ss.hasPermission('system:tenant:query')")
    @OperationLog(module = "租户管理", type = OperationType.SELECT)
    public Result<SysTenant> get(@PathVariable Long id) {
        SysTenant tenant = tenantService.getById(id);
        return Result.success(tenant);
    }

    @PostMapping
    @Operation(summary = "创建租户")
    @PreAuthorize("@ss.hasPermission('system:tenant:add')")
    @OperationLog(module = "租户管理", type = OperationType.INSERT)
    public Result<Void> create(@RequestBody SysTenant tenant) {
        tenantService.createTenant(tenant);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "更新租户")
    @PreAuthorize("@ss.hasPermission('system:tenant:edit')")
    @OperationLog(module = "租户管理", type = OperationType.UPDATE)
    public Result<Void> update(@RequestBody SysTenant tenant) {
        tenantService.updateTenant(tenant);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除租户")
    @PreAuthorize("@ss.hasPermission('system:tenant:remove')")
    @OperationLog(module = "租户管理", type = OperationType.DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        tenantService.deleteTenant(id);
        return Result.success();
    }

    @PostMapping("/switch/{id}")
    @Operation(summary = "切换租户")
    @OperationLog(module = "租户管理", type = OperationType.OTHER)
    public Result<Void> switchTenant(@PathVariable Long id) {
        tenantService.switchTenant(id);
        return Result.success();
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前租户")
    public Result<SysTenant> getCurrentTenant() {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            return Result.success(null);
        }
        SysTenant tenant = tenantService.getById(tenantId);
        return Result.success(tenant);
    }
}
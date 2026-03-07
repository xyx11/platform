package com.micro.platform.system.controller;

import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysMenu;
import com.micro.platform.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜单的管理控制器
 */
@Tag(name = "菜单管理", description = "菜单增删改查")
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    private final SysMenuService sysMenuService;

    public SysMenuController(SysMenuService sysMenuService) {
        this.sysMenuService = sysMenuService;
    }

    @Operation(summary = "获取菜单列表")
    @GetMapping("/list")
    public Result<List<SysMenu>> list() {
        return Result.success(sysMenuService.getMenuTree());
    }

    @Operation(summary = "获取用户的菜单权限")
    @GetMapping("/user")
    public Result<List<SysMenu>> getUserMenus() {
        Long userId = SecurityUtil.getUserId();
        return Result.success(sysMenuService.getMenusByUserId(userId));
    }

    @Operation(summary = "获取用户的权限标识")
    @GetMapping("/permissions")
    public Result<Set<String>> getPermissions() {
        Long userId = SecurityUtil.getUserId();
        return Result.success(sysMenuService.getMenuPermissions(userId));
    }

    @Operation(summary = "获取菜单详情")
    @GetMapping("/{menuId}")
    public Result<SysMenu> get(@PathVariable Long menuId) {
        return Result.success(sysMenuService.getById(menuId));
    }

    @Operation(summary = "新增菜单")
    @OperationLog(module = "菜单管理", type = OperationType.CREATE, description = "新增菜单")
    @PreAuthorize("hasAuthority('system:menu:add')")
    @PostMapping
    public Result<Void> add(@RequestBody SysMenu menu) {
        menu.setCreateBy(SecurityUtil.getUserId());
        sysMenuService.save(menu);
        return Result.success();
    }

    @Operation(summary = "修改菜单")
    @OperationLog(module = "菜单管理", type = OperationType.UPDATE, description = "修改菜单")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    @PutMapping
    public Result<Void> update(@RequestBody SysMenu menu) {
        menu.setUpdateBy(SecurityUtil.getUserId());
        sysMenuService.updateById(menu);
        return Result.success();
    }

    @Operation(summary = "删除菜单")
    @OperationLog(module = "菜单管理", type = OperationType.DELETE, description = "删除菜单")
    @PreAuthorize("hasAuthority('system:menu:remove')")
    @DeleteMapping("/{menuId}")
    public Result<Void> remove(@PathVariable Long menuId) {
        sysMenuService.removeById(menuId);
        return Result.success();
    }
}
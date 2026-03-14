package com.micro.platform.system.controller;

import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysMenu;
import com.micro.platform.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
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
    public Result<List<SysMenu>> list(SysMenu menu) {
        return Result.success(sysMenuService.selectMenuList(menu));
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
    @OperationLog(module = "菜单管理", type = OperationType.INSERT, description = "新增菜单")
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

    @Operation(summary = "导出菜单数据")
    @PreAuthorize("hasAuthority('system:menu:query')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(SysMenu menu) throws Exception {
        byte[] data = sysMenuService.exportMenu(menu);
        String fileName = URLEncoder.encode("菜单数据", "UTF-8").replaceAll("\\\\+", "%20");
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @Operation(summary = "获取菜单统计信息")
    @PreAuthorize("hasAuthority('system:menu:query')")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(sysMenuService.getMenuStats());
    }

    @Operation(summary = "获取菜单树形结构（包含按钮）")
    @PreAuthorize("hasAuthority('system:menu:query')")
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> getTreeWithButtons() {
        return Result.success(sysMenuService.getMenuTreeWithButtons());
    }

    @Operation(summary = "获取角色的菜单权限列表")
    @PreAuthorize("hasAuthority('system:menu:query')")
    @GetMapping("/role/{roleId}")
    public Result<List<Map<String, Object>>> getMenusByRoleId(@PathVariable Long roleId) {
        return Result.success(sysMenuService.getMenusByRoleId(roleId));
    }
}
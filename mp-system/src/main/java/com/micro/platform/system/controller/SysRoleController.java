package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysRole;
import com.micro.platform.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理", description = "角色增删改查")
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    private final SysRoleService sysRoleService;

    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    @Operation(summary = "获取角色列表")
    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/list")
    public Result<Page<SysRole>> list(SysRole role,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysRole> page = sysRoleService.selectRolePage(role, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取角色详情")
    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/{roleId}")
    public Result<SysRole> get(@PathVariable Long roleId) {
        return Result.success(sysRoleService.getById(roleId));
    }

    @Operation(summary = "新增角色")
    @OperationLog(module = "角色管理", type = OperationType.CREATE, description = "新增角色")
    @PreAuthorize("hasAuthority('system:role:add')")
    @PostMapping
    public Result<Void> add(@RequestBody SysRole role) {
        role.setCreateBy(SecurityUtil.getUserId());
        sysRoleService.save(role);
        return Result.success();
    }

    @Operation(summary = "修改角色")
    @OperationLog(module = "角色管理", type = OperationType.UPDATE, description = "修改角色")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @PutMapping
    public Result<Void> update(@RequestBody SysRole role) {
        role.setUpdateBy(SecurityUtil.getUserId());
        sysRoleService.updateById(role);
        return Result.success();
    }

    @Operation(summary = "批量删除角色")
    @OperationLog(module = "角色管理", type = OperationType.DELETE, description = "批量删除角色")
    @PreAuthorize("hasAuthority('system:role:remove')")
    @DeleteMapping("/batch")
    public Result<Void> batchRemove(@RequestBody List<Long> ids) {
        sysRoleService.batchDelete(ids);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @OperationLog(module = "角色管理", type = OperationType.DELETE, description = "删除角色")
    @PreAuthorize("hasAuthority('system:role:remove')")
    @DeleteMapping("/{roleId}")
    public Result<Void> remove(@PathVariable Long roleId) {
        sysRoleService.removeById(roleId);
        return Result.success();
    }

    @Operation(summary = "分配菜单权限")
    @OperationLog(module = "角色管理", type = OperationType.UPDATE, description = "分配菜单权限")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @PostMapping("/authMenu")
    public Result<Void> authMenu(@RequestBody RoleMenuRequest request) {
        sysRoleService.assignMenus(request.getRoleId(), request.getMenuIds());
        return Result.success();
    }

    @Operation(summary = "查询角色菜单权限")
    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/menus/{roleId}")
    public Result<List<Long>> menus(@PathVariable Long roleId) {
        return Result.success(sysRoleService.selectMenusByRoleId(roleId));
    }

    @Operation(summary = "导出角色数据")
    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/export")
    public void export(HttpServletResponse response, SysRole role) {
        sysRoleService.exportRole(response, role);
    }

    @Operation(summary = "获取角色统计信息")
    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(@RequestParam(required = false) Long roleId) {
        return Result.success(sysRoleService.getRoleStats(roleId));
    }

    @Operation(summary = "获取角色用户列表")
    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/users/{roleId}")
    public Result<List<Map<String, Object>>> getUsers(@PathVariable Long roleId) {
        return Result.success(sysRoleService.getRoleUsers(roleId));
    }

    @Operation(summary = "分配用户到角色")
    @OperationLog(module = "角色管理", type = OperationType.GRANT, description = "分配用户到角色")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @PostMapping("/users/assign")
    public Result<Void> assignUsers(@RequestParam Long roleId, @RequestBody List<Long> userIds) {
        sysRoleService.assignUsers(roleId, userIds);
        return Result.success();
    }

    @Operation(summary = "从角色移除用户")
    @OperationLog(module = "角色管理", type = OperationType.OTHER, description = "从角色移除用户")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @PostMapping("/users/remove")
    public Result<Void> removeUsers(@RequestParam Long roleId, @RequestBody List<Long> userIds) {
        sysRoleService.removeUsers(roleId, userIds);
        return Result.success();
    }

    /**
     * 角色菜单请求 DTO
     */
    public static class RoleMenuRequest {
        private Long roleId;
        private List<Long> menuIds;

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        public List<Long> getMenuIds() {
            return menuIds;
        }

        public void setMenuIds(List<Long> menuIds) {
            this.menuIds = menuIds;
        }
    }
}
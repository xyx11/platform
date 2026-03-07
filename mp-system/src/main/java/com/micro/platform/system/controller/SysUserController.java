package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysUser;
import com.micro.platform.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理", description = "用户增删改查")
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Operation(summary = "获取用户列表")
    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping("/list")
    public Result<Page<SysUser>> list(SysUser user,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysUser> page = sysUserService.selectUserPage(user, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取用户详情")
    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping("/{userId}")
    public Result<SysUser> get(@PathVariable Long userId) {
        return Result.success(sysUserService.getById(userId));
    }

    @Operation(summary = "新增用户")
    @OperationLog(module = "用户管理", type = OperationType.CREATE, description = "新增用户")
    @PreAuthorize("hasAuthority('system:user:add')")
    @PostMapping
    public Result<Void> add(@RequestBody SysUser user) {
        user.setCreateBy(SecurityUtil.getUserId());
        sysUserService.saveUser(user);
        return Result.success();
    }

    @Operation(summary = "修改用户")
    @OperationLog(module = "用户管理", type = OperationType.UPDATE, description = "修改用户")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        user.setUpdateBy(SecurityUtil.getUserId());
        sysUserService.updateUser(user);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @OperationLog(module = "用户管理", type = OperationType.DELETE, description = "删除用户")
    @PreAuthorize("hasAuthority('system:user:remove')")
    @DeleteMapping("/{userId}")
    public Result<Void> remove(@PathVariable Long userId) {
        sysUserService.removeById(userId);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @OperationLog(module = "用户管理", type = OperationType.UPDATE, description = "重置密码")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @PutMapping("/password")
    public Result<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        sysUserService.resetPassword(request.getUserId(), request.getPassword());
        return Result.success();
    }

    /**
     * 重置密码请求 DTO
     */
    public static class ResetPasswordRequest {
        private Long userId;
        private String password;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
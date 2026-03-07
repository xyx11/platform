package com.micro.platform.system.controller;

import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysUser;
import com.micro.platform.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 个人中心控制器
 */
@Tag(name = "个人中心", description = "个人信息管理")
@RestController
@RequestMapping("/system/user")
public class SysProfileController {

    private final SysUserService sysUserService;

    public SysProfileController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Operation(summary = "获取个人信息")
    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping("/profile")
    public Result<SysUser> getProfile() {
        Long userId = SecurityUtil.getUserId();
        SysUser user = sysUserService.getById(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "修改个人信息")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody SysUser user) {
        Long userId = SecurityUtil.getUserId();
        user.setUserId(userId);
        sysUserService.updateById(user);
        return Result.success();
    }

    @Operation(summary = "修改密码")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @PutMapping("/profile/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> params) {
        Long userId = SecurityUtil.getUserId();
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        sysUserService.changePassword(userId, oldPassword, newPassword);
        return Result.success();
    }
}

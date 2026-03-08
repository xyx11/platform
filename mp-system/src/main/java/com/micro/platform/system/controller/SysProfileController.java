package com.micro.platform.system.controller;

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
import org.springframework.web.multipart.MultipartFile;

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
    @GetMapping("/profile")
    public Result<SysUser> getProfile() {
        Long userId = SecurityUtil.getUserId();
        SysUser user = sysUserService.getById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @Operation(summary = "获取用户详情（包含部门信息）")
    @GetMapping("/detail")
    public Result<Map<String, Object>> getUserDetail() {
        Long userId = SecurityUtil.getUserId();
        return Result.success(sysUserService.getUserDetail(userId));
    }

    @Operation(summary = "修改个人信息")
    @OperationLog(module = "个人中心", type = OperationType.UPDATE, description = "修改个人信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody SysUser user) {
        Long userId = SecurityUtil.getUserId();
        user.setUserId(userId);
        // 不允许修改敏感字段
        user.setUsername(null);
        user.setPassword(null);
        user.setStatus(null);
        sysUserService.updateById(user);
        return Result.success();
    }

    @Operation(summary = "修改密码")
    @OperationLog(module = "个人中心", type = OperationType.UPDATE, description = "修改密码")
    @PutMapping("/profile/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> params) {
        Long userId = SecurityUtil.getUserId();
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        sysUserService.changePassword(userId, oldPassword, newPassword);
        return Result.success();
    }

    @Operation(summary = "上传头像")
    @OperationLog(module = "个人中心", type = OperationType.UPDATE, description = "上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            Long userId = SecurityUtil.getUserId();
            String avatarUrl = sysUserService.uploadAvatar(file, userId);
            return Result.success(avatarUrl);
        } catch (Exception e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }
}
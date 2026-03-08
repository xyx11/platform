package com.micro.platform.system.controller;

import cn.hutool.core.util.StrUtil;
import com.micro.platform.common.core.exception.BusinessException;
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

import java.util.HashMap;
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

    @Operation(summary = "修改手机号")
    @OperationLog(module = "个人中心", type = OperationType.UPDATE, description = "修改手机号")
    @PutMapping("/profile/phone")
    public Result<Void> updatePhone(@RequestBody Map<String, String> params) {
        Long userId = SecurityUtil.getUserId();
        String phone = params.get("phone");
        String code = params.get("code");

        if (StrUtil.isBlank(phone)) {
            return Result.error("手机号不能为空");
        }

        // TODO: 验证短信验证码
        // if (!validateSmsCode(userId, phone, code)) {
        //     return Result.error("验证码错误");
        // }

        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setPhone(phone);
        sysUserService.updateById(user);
        return Result.success();
    }

    @Operation(summary = "修改邮箱")
    @OperationLog(module = "个人中心", type = OperationType.UPDATE, description = "修改邮箱")
    @PutMapping("/profile/email")
    public Result<Void> updateEmail(@RequestBody Map<String, String> params) {
        Long userId = SecurityUtil.getUserId();
        String email = params.get("email");
        String code = params.get("code");

        if (StrUtil.isBlank(email)) {
            return Result.error("邮箱不能为空");
        }

        // 验证邮箱格式
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return Result.error("邮箱格式不正确");
        }

        // TODO: 验证邮箱验证码
        // if (!validateEmailCode(userId, email, code)) {
        //     return Result.error("验证码错误");
        // }

        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setEmail(email);
        sysUserService.updateById(user);
        return Result.success();
    }

    @Operation(summary = "发送手机验证码")
    @PostMapping("/sms/code")
    public Result<Void> sendSmsCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String type = params.get("type");
        if (type == null || type.isEmpty()) {
            type = "change_phone";
        }

        if (StrUtil.isBlank(phone)) {
            return Result.error("手机号不能为空");
        }

        // 生成 6 位验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // TODO: 存储验证码到 Redis，5 分钟过期
        // redisUtil.set("sms:" + type + ":" + phone, code, 300, TimeUnit.SECONDS);

        // TODO: 调用短信服务发送验证码
        // smsService.send(phone, code);

        return Result.success();
    }
}
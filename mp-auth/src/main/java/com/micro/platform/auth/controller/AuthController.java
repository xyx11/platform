package com.micro.platform.auth.controller;

import com.micro.platform.auth.dto.CaptchaResponse;
import com.micro.platform.auth.dto.LoginRequest;
import com.micro.platform.auth.dto.LoginResponse;
import com.micro.platform.auth.service.AuthService;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户登录、登出、验证码等")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public Result<CaptchaResponse> captcha() {
        return Result.success(authService.generateCaptcha());
    }

    @Operation(summary = "用户登录")
    @OperationLog(module = "认证管理", type = OperationType.LOGIN, description = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @Operation(summary = "用户登出")
    @OperationLog(module = "认证管理", type = OperationType.LOGOUT, description = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @Operation(summary = "刷新 Token")
    @PostMapping("/refresh")
    public Result<String> refresh(@RequestParam String refreshToken) {
        return Result.success(authService.refreshToken(refreshToken));
    }

    @Operation(summary = "发送找回密码验证码")
    @OperationLog(module = "认证管理", type = OperationType.OTHER, description = "发送找回密码验证码")
    @PostMapping("/reset-password/code")
    public Result<Void> sendResetPasswordCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        authService.sendResetPasswordCode(phone);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @OperationLog(module = "认证管理", type = OperationType.UPDATE, description = "重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String code = params.get("code");
        String newPassword = params.get("newPassword");
        authService.resetPassword(phone, code, newPassword);
        return Result.success();
    }
}
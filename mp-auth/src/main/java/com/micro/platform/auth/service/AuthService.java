package com.micro.platform.auth.service;

import com.micro.platform.auth.dto.CaptchaResponse;
import com.micro.platform.auth.dto.LoginRequest;
import com.micro.platform.auth.dto.LoginResponse;
import com.micro.platform.auth.entity.SysUser;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.redis.util.RedisUtil;
import com.wf.captcha.SpecCaptcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final RedisUtil redisUtil;

    public AuthService(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    /**
     * 生成验证码
     */
    public CaptchaResponse generateCaptcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);
        String code = specCaptcha.text().toLowerCase();
        String captchaKey = UUID.randomUUID().toString();

        // 存储验证码到 Redis，5 分钟过期
        redisUtil.set("captcha:" + captchaKey, code, 300, TimeUnit.SECONDS);

        CaptchaResponse response = new CaptchaResponse();
        response.setCaptchaKey(captchaKey);
        response.setCaptchaImg(specCaptcha.toBase64());
        response.setExpiresIn(300L);

        return response;
    }

    /**
     * 登录
     */
    public LoginResponse login(LoginRequest request) {
        // TODO: 实现登录逻辑
        // 1. 验证验证码
        // 2. 查询用户
        // 3. 验证密码
        // 4. 生成 Token

        return LoginResponse.builder()
                .accessToken("mock-token")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(1L)
                .username(request.getUsername())
                .nickname("管理员")
                .avatar("")
                .build();
    }

    /**
     * 登出
     */
    public void logout() {
        // TODO: 实现登出逻辑
    }

    /**
     * 刷新 Token
     */
    public String refreshToken(String refreshToken) {
        // TODO: 实现刷新逻辑
        return "new-token";
    }
}
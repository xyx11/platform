package com.micro.platform.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.micro.platform.auth.dto.CaptchaResponse;
import com.micro.platform.auth.dto.LoginRequest;
import com.micro.platform.auth.dto.LoginResponse;
import com.micro.platform.auth.entity.SysUser;
import com.micro.platform.auth.mapper.SysUserMapper;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.redis.util.RedisUtil;
import com.wf.captcha.SpecCaptcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final SysUserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(RedisUtil redisUtil, SysUserMapper userMapper) {
        this.redisUtil = redisUtil;
        this.userMapper = userMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
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
        // 1. 验证验证码
        validateCaptcha(request.getCaptchaKey(), request.getCaptchaCode());

        // 2. 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        wrapper.eq(SysUser::getDeleted, 0);
        SysUser user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 4. 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("用户已被禁用，请联系管理员");
        }

        // 5. 生成 Token
        StpUtil.login(user.getUserId());
        StpUtil.getSession().set("user", user);

        log.info("用户登录成功：{}", user.getUsername());

        return LoginResponse.builder()
                .accessToken(StpUtil.getTokenValue())
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar() != null ? user.getAvatar() : "")
                .build();
    }

    /**
     * 验证验证码
     */
    private void validateCaptcha(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaKey.isEmpty()) {
            throw new BusinessException("验证码参数错误");
        }

        String redisKey = "captcha:" + captchaKey;
        Object storedCode = redisUtil.get(redisKey);

        if (storedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }

        if (!String.valueOf(storedCode).equalsIgnoreCase(captchaCode)) {
            throw new BusinessException("验证码错误");
        }

        // 验证成功后删除验证码，防止重复使用
        redisUtil.delete(redisKey);
    }

    /**
     * 登出
     */
    public void logout() {
        StpUtil.logout();
        log.info("用户登出成功");
    }

    /**
     * 刷新 Token
     */
    public String refreshToken(String refreshToken) {
        // TODO: 实现刷新逻辑
        return "new-token";
    }
}
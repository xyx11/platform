package com.micro.platform.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.micro.platform.auth.dto.CaptchaResponse;
import com.micro.platform.auth.dto.LoginRequest;
import com.micro.platform.auth.dto.LoginResponse;
import com.micro.platform.auth.entity.SysUser;
import com.micro.platform.auth.mapper.SysUserMapper;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.redis.util.RedisUtil;
import com.micro.platform.system.entity.SysLoginLog;
import com.micro.platform.system.service.SysLoginLogService;
import com.wf.captcha.SpecCaptcha;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
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
    private final SysLoginLogService loginLogService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(RedisUtil redisUtil, SysUserMapper userMapper, SysLoginLogService loginLogService) {
        this.redisUtil = redisUtil;
        this.userMapper = userMapper;
        this.loginLogService = loginLogService;
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
        String ip = getIpAddr();
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setIp(ip);
        loginLog.setUsername(request.getUsername());
        loginLog.setLoginTime(LocalDateTime.now());

        try {
            // 1. 验证验证码
            validateCaptcha(request.getCaptchaKey(), request.getCaptchaCode());

            // 2. 查询用户
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getUsername, request.getUsername());
            wrapper.eq(SysUser::getDeleted, 0);
            SysUser user = userMapper.selectOne(wrapper);

            if (user == null) {
                loginLog.setStatus(0);
                loginLog.setMsg("用户名或密码错误");
                saveLoginLog(loginLog);
                throw new BusinessException("用户名或密码错误");
            }

            // 3. 验证密码
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                loginLog.setStatus(0);
                loginLog.setMsg("用户名或密码错误");
                saveLoginLog(loginLog);
                throw new BusinessException("用户名或密码错误");
            }

            // 4. 检查用户状态
            if (user.getStatus() == 0) {
                loginLog.setStatus(0);
                loginLog.setMsg("用户已被禁用，请联系管理员");
                saveLoginLog(loginLog);
                throw new BusinessException("用户已被禁用，请联系管理员");
            }

            // 5. 设置登录信息
            loginLog.setUserId(user.getUserId());
            loginLog.setStatus(1);
            loginLog.setMsg("登录成功");

            // 6. 获取浏览器和操作系统信息
            UserAgent userAgent = UserAgentUtil.parse(request.getUserAgent() != null ? request.getUserAgent() : "");
            loginLog.setBrowser(userAgent.getBrowser().getName());
            loginLog.setOs(userAgent.getOs().getName());

            // 7. 获取登录地点
            String location = getRegionByIp(ip);
            loginLog.setLocation(location);

            // 8. 保存登录日志
            saveLoginLog(loginLog);

            // 9. 更新用户登录信息
            updateUserLoginInfo(user.getUserId(), ip);

            // 10. 生成 Token
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
        } catch (BusinessException e) {
            loginLog.setStatus(0);
            loginLog.setMsg(e.getMessage());
            saveLoginLog(loginLog);
            throw e;
        } catch (Exception e) {
            loginLog.setStatus(0);
            loginLog.setMsg("登录异常：" + e.getMessage());
            saveLoginLog(loginLog);
            throw new BusinessException("登录失败：" + e.getMessage());
        }
    }

    /**
     * 获取 IP 地址
     */
    private String getIpAddr() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return "127.0.0.1";
            }
            HttpServletRequest request = attributes.getRequest();

            String ip = request.getHeader("x-forwarded-for");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            // 多个代理的情况，取第一个 IP
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        } catch (Exception e) {
            log.error("获取 IP 地址失败", e);
            return "127.0.0.1";
        }
    }

    /**
     * 保存登录日志
     */
    private void saveLoginLog(SysLoginLog loginLog) {
        try {
            loginLogService.save(loginLog);
        } catch (Exception e) {
            log.error("保存登录日志失败：{}", e.getMessage());
        }
    }

    /**
     * 更新用户登录信息
     */
    private void updateUserLoginInfo(Long userId, String ip) {
        try {
            SysUser user = new SysUser();
            user.setUserId(userId);
            user.setLoginIp(ip);
            user.setLoginDate(LocalDateTime.now());
            userMapper.updateById(user);
        } catch (Exception e) {
            log.error("更新用户登录信息失败：{}", e.getMessage());
        }
    }

    /**
     * 根据 IP 获取归属地
     */
    private String getRegionByIp(String ip) {
        try {
            String result = cn.hutool.http.HttpUtil.get("https://whois.pconline.com.cn/ipJson.jsp?json=true&ip=" + ip);
            if (result != null && !result.isEmpty()) {
                // 简单解析，返回格式如：{"ip":"...","addr":"..."}
                int idx = result.indexOf("\"addr\":\"");
                if (idx > -1) {
                    String addr = result.substring(idx + 8);
                    addr = addr.substring(0, addr.indexOf("\""));
                    return addr.split(" ")[0]; // 只取省份
                }
            }
        } catch (Exception e) {
            log.warn("获取 IP 归属地失败：{}", e.getMessage());
        }
        return "未知";
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
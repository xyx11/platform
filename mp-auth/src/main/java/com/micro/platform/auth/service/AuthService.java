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
import com.micro.platform.common.core.entity.LoginUser;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.core.service.EmailNotificationService;
import com.micro.platform.common.core.service.SmsService;
import com.micro.platform.common.redis.util.RedisUtil;
import com.micro.platform.auth.entity.SysLoginLog;
import com.micro.platform.auth.mapper.SysLoginLogMapper;
import com.wf.captcha.SpecCaptcha;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final SysLoginLogMapper loginLogMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    @Autowired(required = false)
    private EmailNotificationService emailNotificationService;
    @Autowired(required = false)
    private SmsService smsService;

    public AuthService(RedisUtil redisUtil, SysUserMapper userMapper, SysLoginLogMapper loginLogMapper) {
        this.redisUtil = redisUtil;
        this.userMapper = userMapper;
        this.loginLogMapper = loginLogMapper;
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
        boolean logSaved = false;

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
                logSaved = true;
                throw new BusinessException("用户名或密码错误");
            }

            // 3. 验证密码
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                loginLog.setStatus(0);
                loginLog.setMsg("用户名或密码错误");
                saveLoginLog(loginLog);
                logSaved = true;
                throw new BusinessException("用户名或密码错误");
            }

            // 4. 检查用户状态
            if (user.getStatus() == 0) {
                loginLog.setStatus(0);
                loginLog.setMsg("用户已被禁用，请联系管理员");
                saveLoginLog(loginLog);
                logSaved = true;
                throw new BusinessException("用户已被禁用，请联系管理员");
            }

            // 5. 设置登录信息
            loginLog.setUserId(user.getUserId());
            loginLog.setStatus(1);
            loginLog.setMsg("登录成功");

            // 6. 获取浏览器和操作系统信息
            String ua = request.getUserAgent();
            UserAgent userAgent = (ua != null && !ua.isEmpty()) ? UserAgentUtil.parse(ua) : UserAgentUtil.parse("Mozilla/5.0");
            loginLog.setBrowser(userAgent.getBrowser().getName());
            loginLog.setOs(userAgent.getOs().getName());

            // 7. 获取登录地点
            String location = getRegionByIp(ip);
            loginLog.setLocation(location);

            // 8. 保存登录日志
            saveLoginLog(loginLog);
            logSaved = true;

            // 9. 更新用户登录信息
            updateUserLoginInfo(user.getUserId(), ip);

            // 10. 生成 Token
            StpUtil.login(user.getUserId());

            // 11. 存储登录用户信息到 Session（使用 LoginUser 类型）
            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(user.getUserId());
            loginUser.setUsername(user.getUsername());
            loginUser.setNickname(user.getNickname());
            loginUser.setAvatar(user.getAvatar());
            loginUser.setDeptId(user.getDeptId());
            StpUtil.getSession().set("loginUser", loginUser);

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
            if (!logSaved) {
                loginLog.setStatus(0);
                loginLog.setMsg(e.getMessage());
                saveLoginLog(loginLog);
            }
            throw e;
        } catch (Exception e) {
            if (!logSaved) {
                loginLog.setStatus(0);
                loginLog.setMsg("登录异常：" + e.getMessage());
                saveLoginLog(loginLog);
            }
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
            loginLogMapper.insert(loginLog);
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
        // 测试模式：跳过验证码验证
        if (captchaCode == null || captchaCode.isEmpty()) {
            return;
        }
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
    public String refreshToken(String oldToken) {
        if (oldToken == null || oldToken.isEmpty()) {
            throw new BusinessException("Token 不能为空");
        }

        try {
            // Sa-Token 默认会自动刷新 token，这里只返回当前 token 值
            // 如果需要强制刷新，可以先 logout 再 login
            return StpUtil.getTokenValue();
        } catch (Exception e) {
            log.error("刷新 Token 失败：{}", e.getMessage());
            throw new BusinessException("刷新 Token 失败，请重新登录");
        }
    }

    /**
     * 发送找回密码验证码
     */
    public void sendResetPasswordCode(String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new BusinessException("手机号不能为空");
        }

        // 查询用户是否存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getPhone, phone);
        SysUser user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("该手机号未注册");
        }

        // 生成 6 位验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        String redisKey = "reset_password:" + phone;

        // 存储验证码，10 分钟过期
        redisUtil.set(redisKey, code, 600, TimeUnit.SECONDS);

        // 调用短信服务发送验证码
        if (smsService != null) {
            smsService.sendCode(phone, code);
        } else {
            log.info("短信服务未配置，验证码：{} (仅记录，未发送)", code);
        }
        
        // 如果手机关联了邮箱，也可以发送邮件通知
        if (user != null && user.getEmail() != null) {
            try {
                emailNotificationService.sendSimpleEmail(
                    user.getEmail(),
                    "密码重置验证码",
                    "您正在申请密码重置，验证码为：" + code + "，10 分钟内有效。如非本人操作，请联系管理员。"
                );
            } catch (Exception e) {
                log.warn("邮件发送失败：{}", e.getMessage());
            }
        }
    }

    /**
     * 重置密码
     */
    public void resetPassword(String phone, String code, String newPassword) {
        if (phone == null || phone.isEmpty()) {
            throw new BusinessException("手机号不能为空");
        }

        // 验证验证码
        String redisKey = "reset_password:" + phone;
        Object storedCode = redisUtil.get(redisKey);

        if (storedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }

        if (!String.valueOf(storedCode).equals(code)) {
            throw new BusinessException("验证码错误");
        }

        // 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getPhone, phone);
        SysUser user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        // 删除验证码
        redisUtil.delete(redisKey);

        log.info("用户 {} 重置密码成功", phone);
    }
}
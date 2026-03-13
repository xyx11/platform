package com.micro.platform.system.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置（覆盖 mp-common-security 中的默认配置）
 */
@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("初始化 Sa-Token 拦截器（本地配置）");

        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 认证相关接口
                        "/auth/**",
                        // 验证码
                        "/captcha",
                        // 首页统计接口
                        "/system/index/**",
                        // 待办统计接口（Dashboard 使用）
                        "/system/todo/stats",
                        // Swagger / API 文档
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/knife4j/**",
                        "/swagger-resources/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        // Actuator 监控
                        "/actuator/**",
                        // 错误页面
                        "/error",
                        // 静态资源
                        "/favicon.ico",
                        "/static/**",
                        "/public/**",
                        // WebSocket
                        "/ws/**"
                );

        log.info("Sa-Token 拦截器初始化完成（本地配置）");
    }
}

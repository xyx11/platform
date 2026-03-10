package com.micro.platform.common.security.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(SaTokenConfig.class);

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("初始化 Sa-Token 拦截器");
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",
                        "/captcha",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/knife4j/**",
                        "/swagger-resources/**",
                        "/actuator/**",
                        "/error"
                );
    }
}
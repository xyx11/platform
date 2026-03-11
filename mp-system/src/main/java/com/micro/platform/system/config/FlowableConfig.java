package com.micro.platform.system.config;

import org.flowable.spring.boot.FlowableProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Flowable 配置
 */
@Configuration
@EnableConfigurationProperties(FlowableProperties.class)
public class FlowableConfig {

    // Flowable 默认配置已通过 application.yml 配置
    // 这里可以添加自定义配置
}
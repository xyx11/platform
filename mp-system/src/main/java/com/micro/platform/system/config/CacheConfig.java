package com.micro.platform.system.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置
 * 使用 Caffeine 作为本地缓存
 */
@Configuration
public class CacheConfig {

    /**
     * Caffeine 缓存管理器
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // 配置缓存：在线用户列表
        cacheManager.registerCustomCache("onlineUsers",
            Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .maximumSize(100)
                .build());

        // 配置缓存：流程定义
        cacheManager.registerCustomCache("processDefinitions",
            Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(500)
                .build());

        // 配置缓存：表单定义
        cacheManager.registerCustomCache("formDefinitions",
            Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(500)
                .build());

        // 配置缓存：数据字典
        cacheManager.registerCustomCache("dictData",
            Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build());

        // 配置缓存：部门列表
        cacheManager.registerCustomCache("deptList",
            Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(200)
                .build());

        return cacheManager;
    }
}
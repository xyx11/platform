package com.micro.platform.system.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.dao.SaTokenDaoRedisJackson;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.Field;

/**
 * Sa-Token Redis 配置
 * 确保在应用启动时立即将 SaTokenDaoRedisJackson 注册到 SaManager
 */
@Configuration
public class SaTokenRedisConfig {

    private static final Logger log = LoggerFactory.getLogger(SaTokenRedisConfig.class);

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @PostConstruct
    public void init() {
        log.info("开始配置 Sa-Token Redis 存储...");

        try {
            // 创建 StringRedisTemplate
            StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
            stringRedisTemplate.setConnectionFactory(redisConnectionFactory);

            // 创建 SaTokenDaoRedisJackson 实例
            SaTokenDaoRedisJackson saTokenDao = new SaTokenDaoRedisJackson();

            // 通过反射设置 stringRedisTemplate
            Field stringRedisTemplateField = SaTokenDaoRedisJackson.class.getDeclaredField("stringRedisTemplate");
            stringRedisTemplateField.setAccessible(true);
            stringRedisTemplateField.set(saTokenDao, stringRedisTemplate);

            // 设置到 SaManager
            SaManager.setSaTokenDao(saTokenDao);

            log.info("Sa-Token Redis 存储配置完成");
        } catch (Exception e) {
            log.error("配置 Sa-Token Redis 存储失败", e);
        }
    }
}
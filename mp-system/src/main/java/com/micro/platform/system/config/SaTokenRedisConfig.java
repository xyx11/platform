package com.micro.platform.system.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoRedisJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Sa-Token Redis 配置
 * 通过 Spring Bean 方式注册 SaTokenDaoRedisJackson，确保优先级最高
 */
@Configuration
public class SaTokenRedisConfig {

    private static final Logger log = LoggerFactory.getLogger(SaTokenRedisConfig.class);

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "saTokenDao")
    public SaTokenDao saTokenDao() {
        log.info("开始配置 Sa-Token Redis 存储...");

        try {
            // 创建 SaTokenDaoRedisJackson 实例
            SaTokenDaoRedisJackson saTokenDao = new SaTokenDaoRedisJackson();

            // 调用 init 方法初始化 Redis 连接和序列化器
            saTokenDao.init(redisConnectionFactory);

            // 设置到 SaManager
            SaManager.setSaTokenDao(saTokenDao);

            log.info("Sa-Token Redis 存储配置完成");
            return saTokenDao;
        } catch (Exception e) {
            log.error("配置 Sa-Token Redis 存储失败", e);
            throw new RuntimeException("Sa-Token Redis 配置失败", e);
        }
    }
}
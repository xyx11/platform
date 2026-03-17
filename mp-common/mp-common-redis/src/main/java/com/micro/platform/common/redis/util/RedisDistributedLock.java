package com.micro.platform.common.redis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁工具类
 * 基于 Redis 实现
 */
public class RedisDistributedLock {

    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLock.class);

    private static final String LOCK_SUCCESS = "OK";
    private static final Long RELEASE_SUCCESS = 1L;

    private static final RedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "   return redis.call('del', KEYS[1]) " +
            "else " +
            "   return 0 " +
            "end", Long.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisDistributedLock(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试获取锁
     *
     * @param lockKey 锁键
     * @param clientId 客户端标识（用于释放锁时验证）
     * @param expireSeconds 过期时间（秒）
     * @return 是否成功获取锁
     */
    public boolean tryLock(String lockKey, String clientId, int expireSeconds) {
        try {
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, clientId, expireSeconds, TimeUnit.SECONDS);
            return success != null && success;
        } catch (Exception e) {
            log.error("获取分布式锁失败，lockKey: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 尝试获取锁（带等待时间）
     *
     * @param lockKey 锁键
     * @param clientId 客户端标识
     * @param expireSeconds 锁过期时间
     * @param waitTimeMillis 等待时间（毫秒）
     * @param intervalMillis 重试间隔（毫秒）
     * @return 是否成功获取锁
     */
    public boolean tryLock(String lockKey, String clientId, int expireSeconds,
                          long waitTimeMillis, long intervalMillis) {
        long end = System.currentTimeMillis() + waitTimeMillis;
        while (System.currentTimeMillis() < end) {
            if (tryLock(lockKey, clientId, expireSeconds)) {
                return true;
            }
            try {
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("获取分布式锁被中断，lockKey: {}", lockKey);
                return false;
            }
        }
        return false;
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁键
     * @param clientId 客户端标识
     * @return 是否成功释放锁
     */
    public boolean unlock(String lockKey, String clientId) {
        try {
            Long result = redisTemplate.execute(UNLOCK_SCRIPT,
                    Collections.singletonList(lockKey), clientId);
            return RELEASE_SUCCESS.equals(result);
        } catch (Exception e) {
            log.error("释放分布式锁失败，lockKey: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 执行加锁操作
     *
     * @param lockKey 锁键
     * @param expireSeconds 锁过期时间
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     * @throws RuntimeException 获取锁失败或执行异常时抛出
     */
    public <T> T executeWithLock(String lockKey, int expireSeconds, Supplier<T> action) {
        String clientId = generateClientId();
        boolean locked = false;
        try {
            locked = tryLock(lockKey, clientId, expireSeconds);
            if (!locked) {
                throw new RuntimeException("获取分布式锁失败：" + lockKey);
            }
            return action.get();
        } finally {
            if (locked) {
                unlock(lockKey, clientId);
            }
        }
    }

    /**
     * 执行加锁操作（无返回值）
     *
     * @param lockKey 锁键
     * @param expireSeconds 锁过期时间
     * @param action 要执行的操作
     */
    public void executeWithLock(String lockKey, int expireSeconds, Runnable action) {
        executeWithLock(lockKey, expireSeconds, () -> {
            action.run();
            return null;
        });
    }

    /**
     * 尝试执行加锁操作
     *
     * @param lockKey 锁键
     * @param expireSeconds 锁过期时间
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果，获取锁失败返回 null
     */
    public <T> T tryExecuteWithLock(String lockKey, int expireSeconds, Supplier<T> action) {
        String clientId = generateClientId();
        boolean locked = false;
        try {
            locked = tryLock(lockKey, clientId, expireSeconds);
            if (!locked) {
                log.warn("获取分布式锁失败，跳过执行，lockKey: {}", lockKey);
                return null;
            }
            return action.get();
        } finally {
            if (locked) {
                unlock(lockKey, clientId);
            }
        }
    }

    /**
     * 续期锁（看门狗机制）
     *
     * @param lockKey 锁键
     * @param clientId 客户端标识
     * @param expireSeconds 续期时间
     * @return 是否成功续期
     */
    public boolean renewLock(String lockKey, String clientId, int expireSeconds) {
        try {
            String value = (String) redisTemplate.opsForValue().get(lockKey);
            if (clientId.equals(value)) {
                redisTemplate.expire(lockKey, expireSeconds, TimeUnit.SECONDS);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("续期分布式锁失败，lockKey: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 检查是否持有锁
     *
     * @param lockKey 锁键
     * @param clientId 客户端标识
     * @return 是否持有锁
     */
    public boolean isLocked(String lockKey, String clientId) {
        try {
            String value = (String) redisTemplate.opsForValue().get(lockKey);
            return clientId.equals(value);
        } catch (Exception e) {
            log.error("检查锁状态失败，lockKey: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 生成客户端标识
     */
    private String generateClientId() {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * 删除锁（强制）
     *
     * @param lockKey 锁键
     */
    public void forceUnlock(String lockKey) {
        try {
            redisTemplate.delete(lockKey);
        } catch (Exception e) {
            log.error("强制删除锁失败，lockKey: {}", lockKey, e);
        }
    }
}
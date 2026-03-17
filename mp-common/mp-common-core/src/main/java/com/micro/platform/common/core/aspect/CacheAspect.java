package com.micro.platform.common.core.aspect;

import com.micro.platform.common.core.annotation.Cache;
import com.micro.platform.common.core.annotation.CacheEvict;
import com.micro.platform.common.core.util.SpElUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 缓存切面
 * 用于拦截带有 @Cache 和 @CacheEvict 注解的方法，实现缓存管理
 *
 * 注意：这是一个简单的内存缓存实现，生产环境建议使用 Redis
 */
@Aspect
@Component
@Order(3)
@Slf4j
public class CacheAspect {

    /**
     * 简单的内存缓存（生产环境应使用 Redis）
     */
    private final ConcurrentHashMap<String, CacheEntry> cacheStore = new ConcurrentHashMap<>();

    /**
     * 缓存条目
     */
    static class CacheEntry {
        Object value;
        long expireTime;

        CacheEntry(Object value, long ttlMs) {
            this.value = value;
            this.expireTime = System.currentTimeMillis() + ttlMs;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    @Pointcut("@annotation(com.micro.platform.common.core.annotation.Cache)")
    public void cachePointcut() {
    }

    @Pointcut("@annotation(com.micro.platform.common.core.annotation.CacheEvict)")
    public void cacheEvictPointcut() {
    }

    /**
     * 缓存读取
     */
    @Around("cachePointcut()")
    public Object aroundCache(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Object[] args = point.getArgs();

        Cache cacheAnnotation = method.getAnnotation(Cache.class);
        if (cacheAnnotation == null) {
            return point.proceed();
        }

        // 构建缓存键
        String cacheKey = buildCacheKey(cacheAnnotation.value(), cacheAnnotation.key(), method, args);

        // 尝试从缓存获取
        Object cachedValue = getFromCache(cacheKey);
        if (cachedValue != null) {
            log.debug("缓存命中：{}", cacheKey);
            return cachedValue;
        }

        // 执行方法
        Object result = point.proceed();

        // 存入缓存
        if (result != null) {
            putToCache(cacheKey, result, cacheAnnotation.expire());
            log.debug("缓存已更新：{}", cacheKey);
        }

        return result;
    }

    /**
     * 缓存删除
     */
    @Around("cacheEvictPointcut()")
    public Object aroundCacheEvict(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Object[] args = point.getArgs();

        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);
        if (cacheEvict == null) {
            return point.proceed();
        }

        // 先执行方法
        Object result = point.proceed();

        // 删除缓存
        if (cacheEvict.allEntries()) {
            evictAll(cacheEvict.value());
        } else {
            String cacheKey = buildCacheKey(cacheEvict.value(), cacheEvict.key(), method, args);
            evictFromCache(cacheKey);
        }

        return result;
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(String prefix, String keyExpression, Method method, Object[] args) {
        StringBuilder cacheKey = new StringBuilder(prefix);
        cacheKey.append(":");

        if (keyExpression != null && !keyExpression.isEmpty()) {
            try {
                Object keyValue = SpElUtils.parseExpression(keyExpression, method, args);
                cacheKey.append(keyValue != null ? keyValue.toString() : "null");
            } catch (Exception e) {
                cacheKey.append(keyExpression);
            }
        } else {
            // 默认使用所有参数作为键
            for (Object arg : args) {
                cacheKey.append(arg != null ? arg.toString() : "null").append(":");
            }
        }

        return cacheKey.toString();
    }

    /**
     * 从缓存获取
     */
    private Object getFromCache(String key) {
        CacheEntry entry = cacheStore.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            cacheStore.remove(key);
            return null;
        }
        return entry.value;
    }

    /**
     * 存入缓存
     */
    private void putToCache(String key, Object value, long expireSeconds) {
        long ttlMs = expireSeconds > 0 ?
            TimeUnit.SECONDS.toMillis(expireSeconds) :
            TimeUnit.HOURS.toMillis(24); // 默认 24 小时
        cacheStore.put(key, new CacheEntry(value, ttlMs));
    }

    /**
     * 删除缓存
     */
    private void evictFromCache(String key) {
        cacheStore.remove(key);
        log.debug("缓存已删除：{}", key);
    }

    /**
     * 删除所有缓存
     */
    private void evictAll(String prefix) {
        cacheStore.keySet().removeIf(key -> key.startsWith(prefix + ":"));
        log.debug("缓存已清空，前缀：{}", prefix);
    }

    /**
     * 清空所有缓存
     */
    public void clearAll() {
        cacheStore.clear();
    }

    /**
     * 获取缓存大小
     */
    public int getCacheSize() {
        // 清理过期条目
        cacheStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
        return cacheStore.size();
    }
}
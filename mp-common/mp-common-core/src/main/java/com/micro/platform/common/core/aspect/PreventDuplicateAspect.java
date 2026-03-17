package com.micro.platform.common.core.aspect;

import com.micro.platform.common.core.annotation.PreventDuplicate;
import com.micro.platform.common.core.util.SpElUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 防重复提交切面
 * 用于拦截带有 @PreventDuplicate 注解的方法，防止重复提交
 */
@Aspect
@Component
@Order(5)
@Slf4j
public class PreventDuplicateAspect {

    /**
     * 本地锁缓存
     */
    private final Map<String, ReentrantLock> lockCache = new ConcurrentHashMap<>();

    /**
     * 请求处理状态缓存
     */
    private final Map<String, Long> processingRequests = new ConcurrentHashMap<>();

    @Pointcut("@annotation(com.micro.platform.common.core.annotation.PreventDuplicate)")
    public void preventDuplicatePointcut() {
    }

    @Around("preventDuplicatePointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Object[] args = point.getArgs();

        PreventDuplicate preventDuplicate = method.getAnnotation(PreventDuplicate.class);
        if (preventDuplicate == null) {
            return point.proceed();
        }

        // 构建锁键
        String lockKey = buildLockKey(preventDuplicate.key(), method, args);

        // 检查是否有正在处理的请求
        Long startTime = processingRequests.get(lockKey);
        if (startTime != null) {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed < preventDuplicate.expire()) {
                log.warn("检测到重复提交：key={}, 已处理时长={}ms", lockKey, elapsed);
                throw new RuntimeException(preventDuplicate.message());
            }
            // 已过期，清理
            processingRequests.remove(lockKey, startTime);
        }

        // 尝试获取锁
        ReentrantLock lock = lockCache.computeIfAbsent(lockKey, k -> new ReentrantLock());
        long waitTime = preventDuplicate.timeUnit().toMillis(preventDuplicate.waitTime());
        long expireTime = preventDuplicate.timeUnit().toMillis(preventDuplicate.expire());

        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitTime, TimeUnit.MILLISECONDS);
            if (!acquired) {
                log.warn("获取锁超时，认为是重复提交：key={}", lockKey);
                throw new RuntimeException(preventDuplicate.message());
            }

            // 标记为正在处理
            processingRequests.put(lockKey, System.currentTimeMillis());

            return point.proceed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("请求被中断", e);
        } finally {
            if (acquired) {
                processingRequests.remove(lockKey);
                lock.unlock();

                // 清理锁缓存（避免内存泄漏）
                lockCache.remove(lockKey);
            }
        }
    }

    /**
     * 构建锁键
     */
    private String buildLockKey(String keyExpression, Method method, Object[] args) {
        if (keyExpression == null || keyExpression.isEmpty()) {
            // 默认使用请求路径 + 用户 ID 作为锁键
            String uri = SpElUtils.getCurrentUri();
            Long userId = SpElUtils.getCurrentUserId();
            return uri + ":" + (userId != null ? userId : "anonymous");
        }

        try {
            Object keyValue = SpElUtils.parseExpression(keyExpression, method, args);
            return keyValue != null ? keyValue.toString() : "null";
        } catch (Exception e) {
            return keyExpression;
        }
    }
}
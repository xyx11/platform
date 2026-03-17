package com.micro.platform.common.core.aspect;

import com.micro.platform.common.core.annotation.RateLimit;
import com.micro.platform.common.core.util.RateLimiterUtils;
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
 * 限流切面
 * 用于拦截带有 @RateLimit 注解的方法，实现限流控制
 */
@Aspect
@Component
@Order(4)
@Slf4j
public class RateLimitAspect {

    /**
     * 固定窗口限流器缓存
     */
    private final ConcurrentHashMap<String, RateLimiterUtils.FixedWindowLimiter> fixedWindowLimiters
            = new ConcurrentHashMap<>();

    /**
     * 滑动窗口限流器缓存
     */
    private final ConcurrentHashMap<String, RateLimiterUtils.SlidingWindowLimiter> slidingWindowLimiters
            = new ConcurrentHashMap<>();

    @Pointcut("@annotation(com.micro.platform.common.core.annotation.RateLimit)")
    public void rateLimitPointcut() {
    }

    @Around("rateLimitPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Object[] args = point.getArgs();

        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return point.proceed();
        }

        // 构建限流键
        String limitKey = buildLimitKey(rateLimit.key(), method, args);

        // 根据算法选择限流器
        boolean allowed = switch (rateLimit.algorithm()) {
            case TOKEN_BUCKET -> tryTokenBucket(limitKey, rateLimit.permitsPerSecond(), rateLimit.failFast());
            case FIXED_WINDOW -> tryFixedWindow(limitKey, rateLimit.permitsPerSecond(), rateLimit.windowMs(), rateLimit.failFast());
            case SLIDING_WINDOW -> trySlidingWindow(limitKey, rateLimit.permitsPerSecond(), rateLimit.windowMs(), rateLimit.failFast());
        };

        if (!allowed) {
            log.warn("请求被限流：key={}, 算法={}, 限制速率={}", limitKey, rateLimit.algorithm(), rateLimit.permitsPerSecond());
            throw new RuntimeException(rateLimit.message());
        }

        return point.proceed();
    }

    /**
     * 构建限流键
     */
    private String buildLimitKey(String keyExpression, Method method, Object[] args) {
        if (keyExpression == null || keyExpression.isEmpty()) {
            // 默认使用类名 + 方法名作为限流键
            return method.getDeclaringClass().getSimpleName() + ":" + method.getName();
        }

        try {
            Object keyValue = SpElUtils.parseExpression(keyExpression, method, args);
            return method.getDeclaringClass().getSimpleName() + ":" + method.getName() + ":" + keyValue;
        } catch (Exception e) {
            return method.getDeclaringClass().getSimpleName() + ":" + method.getName() + ":" + keyExpression;
        }
    }

    /**
     * 令牌桶限流
     */
    private boolean tryTokenBucket(String key, double permitsPerSecond, boolean failFast) {
        if (failFast) {
            return RateLimiterUtils.tryAcquire(key, permitsPerSecond);
        } else {
            RateLimiterUtils.acquire(key, permitsPerSecond);
            return true;
        }
    }

    /**
     * 固定窗口限流
     */
    private boolean tryFixedWindow(String key, double permitsPerSecond, long windowMs, boolean failFast) {
        int limit = (int) (permitsPerSecond * windowMs / 1000);
        RateLimiterUtils.FixedWindowLimiter limiter = fixedWindowLimiters.computeIfAbsent(
                key,
                k -> new RateLimiterUtils.FixedWindowLimiter(limit, windowMs)
        );
        return limiter.tryAcquire(key);
    }

    /**
     * 滑动窗口限流
     */
    private boolean trySlidingWindow(String key, double permitsPerSecond, long windowMs, boolean failFast) {
        int limit = (int) (permitsPerSecond * windowMs / 1000);
        int slots = 10; // 默认 10 个槽
        RateLimiterUtils.SlidingWindowLimiter limiter = slidingWindowLimiters.computeIfAbsent(
                key,
                k -> new RateLimiterUtils.SlidingWindowLimiter(limit, windowMs, slots)
        );
        return limiter.tryAcquire(key);
    }
}
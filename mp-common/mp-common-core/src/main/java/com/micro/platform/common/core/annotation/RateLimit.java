package com.micro.platform.common.core.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 * 用于限制方法的调用频率
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流键（支持 SpEL 表达式）
     */
    String key() default "";

    /**
     * 每秒允许的请求数
     */
    double permitsPerSecond() default 10.0;

    /**
     * 限流算法
     */
    Algorithm algorithm() default Algorithm.TOKEN_BUCKET;

    /**
     * 固定窗口大小（毫秒）
     * 仅在 algorithm 为 FIXED_WINDOW 时有效
     */
    long windowMs() default 1000;

    /**
     * 是否返回错误信息而不是阻塞
     */
    boolean failFast() default true;

    /**
     * 错误提示信息
     */
    String message() default "请求过于频繁，请稍后再试";

    /**
     * 限流算法
     */
    enum Algorithm {
        /**
         * 令牌桶算法
         */
        TOKEN_BUCKET,
        /**
         * 固定窗口算法
         */
        FIXED_WINDOW,
        /**
         * 滑动窗口算法
         */
        SLIDING_WINDOW
    }
}
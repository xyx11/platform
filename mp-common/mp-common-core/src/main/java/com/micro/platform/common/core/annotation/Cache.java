package com.micro.platform.common.core.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存注解
 * 用于标记需要缓存的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {

    /**
     * 缓存名称
     */
    String name();

    /**
     * 缓存键（支持 SpEL 表达式）
     */
    String key() default "";

    /**
     * 缓存过期时间
     */
    long expire() default 300;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 是否允许空值缓存
     */
    boolean allowNull() default true;

    /**
     * 空值缓存过期时间（秒）
     */
    int nullExpire() default 60;

    /**
     * 缓存描述
     */
    String description() default "";
}
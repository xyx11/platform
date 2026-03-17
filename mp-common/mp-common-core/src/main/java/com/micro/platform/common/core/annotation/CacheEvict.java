package com.micro.platform.common.core.annotation;

import java.lang.annotation.*;

/**
 * 缓存删除注解
 * 用于标记需要删除缓存的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheEvict {

    /**
     * 缓存名称
     */
    String name();

    /**
     * 缓存键（支持 SpEL 表达式）
     */
    String key() default "";

    /**
     * 是否删除所有缓存
     */
    boolean allEntries() default false;
}
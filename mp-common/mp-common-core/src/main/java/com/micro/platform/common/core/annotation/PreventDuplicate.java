package com.micro.platform.common.core.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 防重复提交注解
 * 用于防止用户重复提交表单或请求
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreventDuplicate {

    /**
     * 锁键（支持 SpEL 表达式）
     * 默认使用请求路径 + 用户 ID 作为锁键
     */
    String key() default "";

    /**
     * 锁持有时间（毫秒）
     * 即请求处理的最长时间，超过这个时间锁将自动释放
     */
    long expire() default 3000;

    /**
     * 等待时间（毫秒）
     * 获取锁的超时时间，超过这个时间还未获取到锁则认为是重复提交
     */
    long waitTime() default 0;

    /**
     * 提示信息
     */
    String message() default "请勿重复提交";

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
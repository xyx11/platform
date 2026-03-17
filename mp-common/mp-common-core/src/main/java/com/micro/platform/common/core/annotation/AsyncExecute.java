package com.micro.platform.common.core.annotation;

import java.lang.annotation.*;

/**
 * 异步执行注解
 * 用于标记需要异步执行的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AsyncExecute {

    /**
     * 线程池名称
     */
    String executor() default "taskExecutor";

    /**
     * 超时时间（毫秒）
     * 0 表示不限制
     */
    long timeout() default 0;

    /**
     * 重试次数
     */
    int retries() default 0;

    /**
     * 重试延迟（毫秒）
     */
    long retryDelay() default 1000;

    /**
     * 是否记录异常
     */
    boolean recordException() default true;
}
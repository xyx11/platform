package com.micro.platform.common.core.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于记录用户操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 操作类型
     */
    OperationType type() default OperationType.OTHER;

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 是否记录请求参数
     */
    boolean recordParams() default true;

    /**
     * 是否记录响应结果
     */
    boolean recordResult() default false;

    /**
     * 是否记录操作状态
     */
    boolean recordStatus() default true;

    /**
     * 操作人字段（SpEL 表达式）
     */
    String operator() default "";
}
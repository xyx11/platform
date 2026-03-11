package com.micro.platform.common.log.annotation;

import java.lang.annotation.*;

/**
 * 审计日志注解
 *
 * 用于记录敏感数据的变更历史，支持数据追踪和审计
 * 与 @OperationLog 的区别：
 * - @OperationLog 记录操作行为，用于行为分析
 * - @AuditLog 记录数据变更，用于数据审计和追踪
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 模块名称
     */
    String module() default "";

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 表名
     */
    String tableName() default "";

    /**
     * 是否记录变更前数据
     */
    boolean recordBefore() default true;

    /**
     * 是否记录变更后数据
     */
    boolean recordAfter() default true;

    /**
     * 忽略的字段（不记录变更）
     */
    String[] ignoreFields() default {};
}
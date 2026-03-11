package com.micro.platform.common.core.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 *
 * 用于在 Service 方法上标注，自动注入数据权限过滤条件
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 部门别名（用于 SQL 拼接）
     * 默认值："dept"
     */
    String deptAlias() default "dept";

    /**
     * 用户别名（用于 SQL 拼接）
     * 默认值："user"
     */
    String userAlias() default "user";

    /**
     * 自定义权限字段（用于多租户等场景）
     * 默认值：""
     */
    String customField() default "";
}
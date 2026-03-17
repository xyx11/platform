package com.micro.platform.common.core.annotation;

import java.lang.annotation.*;

/**
 * 数据字典注解
 * 用于标记需要字典翻译的字段
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Dict {

    /**
     * 字典类型（如：user_sex, sys_status 等）
     */
    String type();

    /**
     * 是否翻译标签
     * true: 翻译为标签文本
     * false: 保持原始值
     */
    boolean translate() default true;

    /**
     * 默认值（当字典值不存在时返回）
     */
    String defaultValue() default "";
}
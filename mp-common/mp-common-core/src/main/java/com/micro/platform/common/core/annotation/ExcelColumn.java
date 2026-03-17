package com.micro.platform.common.core.annotation;

import java.lang.annotation.*;

/**
 * Excel 列注解
 * 用于标记导出 Excel 的字段
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelColumn {

    /**
     * 列头名称
     */
    String header();

    /**
     * 列宽（字符数）
     */
    int width() default 15;

    /**
     * 日期格式
     */
    String dateFormat() default "yyyy-MM-dd HH:mm:ss";

    /**
     * 数字格式
     */
    String numberFormat() default "#,##0.00";

    /**
     * 是否允许为空
     */
    boolean nullable() default true;

    /**
     * 排序索引
     */
    int order() default 0;
}
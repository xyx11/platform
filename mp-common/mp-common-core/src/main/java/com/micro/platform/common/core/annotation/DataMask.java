package com.micro.platform.common.core.annotation;

import java.lang.annotation.*;

/**
 * 脱敏注解
 * 用于标记需要数据脱敏的字段
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataMask {

    /**
     * 脱敏类型
     */
    MaskType type() default MaskType.PHONE;

    /**
     * 脱敏模式
     * true: 显示前缀和后缀
     * false: 全部脱敏
     */
    boolean showPrefixAndSuffix() default true;

    /**
     * 前缀保留长度
     */
    int prefixLength() default 3;

    /**
     * 后缀保留长度
     */
    int suffixLength() default 4;

    /**
     * 脱敏类型枚举
     */
    enum MaskType {
        /**
         * 手机号脱敏：138****1234
         */
        PHONE,
        /**
         * 身份证号脱敏：110101********1234
         */
        ID_CARD,
        /**
         * 邮箱脱敏：tes****@example.com
         */
        EMAIL,
        /**
         * 银行卡号脱敏：6222 **** **** 1234
         */
        BANK_CARD,
        /**
         * 地址脱敏：北京市********
         */
        ADDRESS,
        /**
         * 姓名脱敏：张*三
         */
        NAME,
        /**
         * 密码脱敏：******
         */
        PASSWORD,
        /**
         * 自定义脱敏
         */
        CUSTOM
    }
}
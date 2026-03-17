package com.micro.platform.system.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 数学计算工具类
 * 提供精确的浮点数运算
 */
public class Arith {

    /** 默认除法运算精度 */
    private static final int DEFAULT_DIV_SCALE = 10;

    /**
     * 加法
     * @param v1 加数 1
     * @param v2 加数 2
     * @return 两个加数的和
     */
    public static double add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     * 加法
     * @param v1 加数 1
     * @param v2 加数 2
     * @param scale 保留小数位数
     * @return 两个加数的和
     */
    public static double add(double v1, double v2, int scale) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 减法
     * @param v1 被减数
     * @param v2 减数
     * @return 两个减数的差
     */
    public static double sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 减法
     * @param v1 被减数
     * @param v2 减数
     * @param scale 保留小数位数
     * @return 两个减数的差
     */
    public static double sub(double v1, double v2, int scale) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 乘法
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个乘数的积
     */
    public static double mul(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }

    /**
     * 乘法
     * @param v1 被乘数
     * @param v2 乘数
     * @param scale 保留小数位数
     * @return 两个乘数的积
     */
    public static double mul(double v1, double v2, int scale) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 除法
     * @param v1 被除数
     * @param v2 除数
     * @return 两个除数的商
     */
    public static double div(double v1, double v2) {
        return div(v1, v2, DEFAULT_DIV_SCALE);
    }

    /**
     * 除法
     * @param v1 被除数
     * @param v2 除数
     * @param scale 保留小数位数
     * @return 两个除数的商
     */
    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 四舍五入
     * @param v 需要四舍五入的数字
     * @param scale 保留几位小数
     * @return 四舍五入后的结果
     */
    public static double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 四舍五入
     * @param v 需要四舍五入的数字
     * @return 四舍五入后的结果（保留两位小数）
     */
    public static double round(double v) {
        return round(v, 2);
    }

    /**
     * 精确平方根
     * @param n 需要开方的数字
     * @param scale 保留小数位数
     * @return 平方根结果
     */
    public static double sqrt(double n, int scale) {
        if (n < 0) {
            throw new IllegalArgumentException("Negative argument");
        }
        if (n == 0) return 0;

        BigDecimal num = new BigDecimal(Double.toString(n));
        BigDecimal result = BigDecimal.valueOf(Math.sqrt(n));
        return result.setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 精确平方根
     * @param n 需要开方的数字
     * @return 平方根结果（保留 10 位小数）
     */
    public static double sqrt(double n) {
        return sqrt(n, DEFAULT_DIV_SCALE);
    }

    /**
     * 幂运算
     * @param base 底数
     * @param exponent 指数
     * @return 幂运算结果
     */
    public static double pow(double base, int exponent) {
        BigDecimal b = new BigDecimal(Double.toString(base));
        return b.pow(exponent).doubleValue();
    }

    /**
     * 幂运算（带精度）
     * @param base 底数
     * @param exponent 指数
     * @param scale 保留小数位数
     * @return 幂运算结果
     */
    public static double pow(double base, int exponent, int scale) {
        BigDecimal b = new BigDecimal(Double.toString(base));
        return b.pow(exponent).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 绝对值
     * @param v 需要取绝对值的数字
     * @return 绝对值
     */
    public static double abs(double v) {
        return Math.abs(v);
    }

    /**
     * 最大值
     * @param v1 数字 1
     * @param v2 数字 2
     * @return 较大值
     */
    public static double max(double v1, double v2) {
        return Math.max(v1, v2);
    }

    /**
     * 最小值
     * @param v1 数字 1
     * @param v2 数字 2
     * @return 较小值
     */
    public static double min(double v1, double v2) {
        return Math.min(v1, v2);
    }

    /**
     * 检查是否在范围内
     * @param value 待检查的值
     * @param min 最小值
     * @param max 最大值
     * @return 是否在范围内
     */
    public static boolean isBetween(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /**
     * 限制值在指定范围内
     * @param value 待限制的值
     * @param min 最小值
     * @param max 最大值
     * @return 限制后的值
     */
    public static double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * 百分比计算
     * @param part 部分
     * @param total 总体
     * @param scale 保留小数位数
     * @return 百分比（0-100）
     */
    public static double percentage(double part, double total, int scale) {
        if (total == 0) return 0;
        return mul(div(part, total, scale + 2), 100, scale);
    }

    /**
     * 百分比计算（保留 2 位小数）
     * @param part 部分
     * @param total 总体
     * @return 百分比（0-100）
     */
    public static double percentage(double part, double total) {
        return percentage(part, total, 2);
    }

    /**
     * 格式化金额（四舍五入到分）
     * @param amount 金额
     * @return 格式化后的金额
     */
    public static double formatMoney(double amount) {
        return round(amount, 2);
    }

    /**
     * 累加
     * @param values 数字数组
     * @return 累加结果
     */
    public static double sum(double... values) {
        BigDecimal sum = BigDecimal.ZERO;
        for (double value : values) {
            sum = sum.add(new BigDecimal(Double.toString(value)));
        }
        return sum.doubleValue();
    }

    /**
     * 平均值
     * @param values 数字数组
     * @return 平均值
     */
    public static double average(double... values) {
        if (values.length == 0) return 0;
        return div(sum(values), values.length);
    }

    /**
     * 平均值（带精度）
     * @param values 数字数组
     * @param scale 保留小数位数
     * @return 平均值
     */
    public static double average(int scale, double... values) {
        if (values.length == 0) return 0;
        return div(sum(values), values.length, scale);
    }
}
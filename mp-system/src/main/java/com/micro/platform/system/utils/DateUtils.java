package com.micro.platform.system.utils;

import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期时间工具类
 */
public class DateUtils {

    public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "HH:mm:ss";

    private static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern(DEFAULT_PATTERN);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_PATTERN);

    /**
     * 获取当前 LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前日期（ LocalDate）
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * 格式化日期时间
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DEFAULT_FORMATTER);
    }

    /**
     * 格式化日期
     */
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    /**
     * 格式化日期时间（指定格式）
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    /**
     * 解析日期时间字符串
     */
    public static LocalDateTime parse(String text) {
        if (!StringUtils.hasText(text)) return null;
        return LocalDateTime.parse(text, DEFAULT_FORMATTER);
    }

    /**
     * 解析日期字符串
     */
    public static LocalDate parseDate(String text) {
        if (!StringUtils.hasText(text)) return null;
        return LocalDate.parse(text, DATE_FORMATTER);
    }

    /**
     * 解析日期时间字符串（指定格式）
     */
    public static LocalDateTime parse(String text, String pattern) {
        if (!StringUtils.hasText(text)) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(text, formatter);
    }

    /**
     * Date 转 LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * LocalDate 转 Date（当天开始时间）
     */
    public static Date toDate(LocalDate date) {
        if (date == null) return null;
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDateTime 转 Date
     */
    public static Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 添加天数
     */
    public static LocalDateTime addDays(LocalDateTime dateTime, int days) {
        return dateTime.plusDays(days);
    }

    /**
     * 添加月数
     */
    public static LocalDateTime addMonths(LocalDateTime dateTime, int months) {
        return dateTime.plusMonths(months);
    }

    /**
     * 添加年数
     */
    public static LocalDateTime addYears(LocalDateTime dateTime, int years) {
        return dateTime.plusYears(years);
    }

    /**
     * 两个日期相差天数
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 两个日期时间相差小时数
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 两个日期时间相差分钟数
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * 两个日期时间相差秒数
     */
    public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.SECONDS.between(start, end);
    }

    /**
     * 获取当月第一天
     */
    public static LocalDate firstDayOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    /**
     * 获取当月最后一天
     */
    public static LocalDate lastDayOfMonth() {
        return LocalDate.now().withDayOfMonth(
                LocalDate.now().lengthOfMonth());
    }

    /**
     * 获取当周第一天（周一）
     */
    public static LocalDate firstDayOfWeek() {
        return LocalDate.now().with(DayOfWeek.MONDAY);
    }

    /**
     * 获取当周最后一天（周日）
     */
    public static LocalDate lastDayOfWeek() {
        return LocalDate.now().with(DayOfWeek.SUNDAY);
    }

    /**
     * 获取当天开始时间
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * 获取当天结束时间
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(23, 59, 59);
    }

    /**
     * 判断是否是今天
     */
    public static boolean isToday(LocalDate date) {
        return date.equals(LocalDate.now());
    }

    /**
     * 判断是否是昨天
     */
    public static boolean isYesterday(LocalDate date) {
        return date.equals(LocalDate.now().minusDays(1));
    }

    /**
     * 判断是否是本周
     */
    public static boolean isThisWeek(LocalDate date) {
        LocalDate firstDay = firstDayOfWeek();
        LocalDate lastDay = lastDayOfWeek();
        return !date.isBefore(firstDay) && !date.isAfter(lastDay);
    }

    /**
     * 判断是否是本月
     */
    public static boolean isThisMonth(LocalDate date) {
        LocalDate firstDay = firstDayOfMonth();
        LocalDate lastDay = lastDayOfMonth();
        return !date.isBefore(firstDay) && !date.isAfter(lastDay);
    }

    /**
     * 获取中文星期
     */
    public static String getChineseDayOfWeek(LocalDate date) {
        String[] weeks = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return weeks[date.getDayOfWeek().getValue() - 1];
    }

    /**
     * 获取相对时间描述
     */
    public static String getRelativeTime(LocalDateTime dateTime) {
        Duration duration = Duration.between(dateTime, now());
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        } else if (hours < 24) {
            return hours + "小时前";
        } else if (days < 30) {
            return days + "天前";
        } else {
            return format(dateTime);
        }
    }

    /**
     * 判断是否是闰年
     */
    public static boolean isLeapYear(int year) {
        return Year.of(year).isLeap();
    }

    /**
     * 获取某月天数
     */
    public static int getDaysInMonth(int year, int month) {
        return YearMonth.of(year, month).lengthOfMonth();
    }

    /**
     * 计算年龄
     */
    public static int calculateAge(LocalDate birthday) {
        return Period.between(birthday, LocalDate.now()).getYears();
    }
}
package com.micro.platform.system.utils;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 数据校验工具类
 */
public class ValidationUtils {

    // 手机号正则
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^1[3-9]\\d{9}$");

    // 邮箱正则
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    // 身份证正则
    private static final Pattern ID_CARD_PATTERN =
            Pattern.compile("(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)");

    // URL 正则
    private static final Pattern URL_PATTERN =
            Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$");

    // IPv4 正则
    private static final Pattern IP_PATTERN =
            Pattern.compile("^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}" +
                    "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$");

    // 中文正则
    private static final Pattern CHINESE_PATTERN =
            Pattern.compile("[\\u4e00-\\u9fa5]");

    // 用户名正则（字母开头，4-16 位）
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{3,15}$");

    // 密码正则（6-20 位，字母 + 数字）
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,20}$");

    /**
     * 验证手机号
     */
    public static boolean isValidPhone(String phone) {
        if (!StringUtils.hasText(phone)) return false;
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * 验证邮箱
     */
    public static boolean isValidEmail(String email) {
        if (!StringUtils.hasText(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * 验证身份证号
     */
    public static boolean isValidIdCard(String idCard) {
        if (!StringUtils.hasText(idCard)) return false;
        idCard = idCard.trim().toUpperCase();
        if (!ID_CARD_PATTERN.matcher(idCard).matches()) return false;

        // 18 位身份证校验码验证
        if (idCard.length() == 18) {
            return verifyIdCardCheckCode(idCard);
        }
        return true;
    }

    /**
     * 验证 URL
     */
    public static boolean isValidUrl(String url) {
        if (!StringUtils.hasText(url)) return false;
        return URL_PATTERN.matcher(url.trim()).matches();
    }

    /**
     * 验证 IP 地址
     */
    public static boolean isValidIp(String ip) {
        if (!StringUtils.hasText(ip)) return false;
        return IP_PATTERN.matcher(ip.trim()).matches();
    }

    /**
     * 验证用户名
     */
    public static boolean isValidUsername(String username) {
        if (!StringUtils.hasText(username)) return false;
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    /**
     * 验证密码强度
     */
    public static boolean isValidPassword(String password) {
        if (!StringUtils.hasText(password)) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * 验证是否包含中文
     */
    public static boolean containsChinese(String text) {
        if (!StringUtils.hasText(text)) return false;
        return CHINESE_PATTERN.matcher(text).find();
    }

    /**
     * 验证是否全为中文
     */
    public static boolean isAllChinese(String text) {
        if (!StringUtils.hasText(text)) return false;
        return text.matches("^[\\u4e00-\\u9fa5]+$");
    }

    /**
     * 验证字符串长度
     */
    public static boolean isValidLength(String text, int min, int max) {
        if (text == null) return false;
        int length = text.length();
        return length >= min && length <= max;
    }

    /**
     * 验证字符串长度（中文算 1 个）
     */
    public static boolean isValidLengthChinese(String text, int min, int max) {
        if (text == null) return false;
        int length = getChineseLength(text);
        return length >= min && length <= max;
    }

    /**
     * 获取字符串长度（中文算 1 个）
     */
    public static int getChineseLength(String text) {
        if (!StringUtils.hasText(text)) return 0;
        return text.length();
    }

    /**
     * 验证是否为数字
     */
    public static boolean isNumeric(String str) {
        if (!StringUtils.hasText(str)) return false;
        return str.matches("^-?\\d+(\\.\\d+)?$");
    }

    /**
     * 验证是否为整数
     */
    public static boolean isInteger(String str) {
        if (!StringUtils.hasText(str)) return false;
        return str.matches("^-?\\d+$");
    }

    /**
     * 验证是否为正整数
     */
    public static boolean isPositiveInteger(String str) {
        if (!StringUtils.hasText(str)) return false;
        return str.matches("^[1-9]\\d*$");
    }

    /**
     * 验证日期格式
     */
    public static boolean isValidDate(String date, String pattern) {
        if (!StringUtils.hasText(date)) return false;
        try {
            java.time.LocalDate.parse(date,
                    java.time.format.DateTimeFormatter.ofPattern(pattern));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证银行卡号（Luhn 算法）
     */
    public static boolean isValidBankCard(String cardNo) {
        if (!StringUtils.hasText(cardNo)) return false;
        cardNo = cardNo.replaceAll("\\s+", "");
        if (!cardNo.matches("^\\d+$")) return false;
        if (cardNo.length() < 13 || cardNo.length() > 19) return false;

        char[] chars = cardNo.toCharArray();
        int sum = 0;
        boolean isEven = false;

        for (int i = chars.length - 1; i >= 0; i--) {
            int digit = chars[i] - '0';
            if (isEven) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            isEven = !isEven;
        }

        return sum % 10 == 0;
    }

    /**
     * 验证 18 位身份证校验码
     */
    private static boolean verifyIdCardCheckCode(String idCard) {
        String[] checkCodes = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

        String body = idCard.substring(0, 17);
        String checkCode = idCard.substring(17);

        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (body.charAt(i) - '0') * weights[i];
        }

        int mod = sum % 11;
        return checkCodes[mod].equalsIgnoreCase(checkCode);
    }

    /**
     * 验证是否为空（null、空字符串、空数组、空集合）
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) return true;
        if (obj instanceof String) return ((String) obj).isEmpty();
        if (obj instanceof java.util.Collection)
            return ((java.util.Collection<?>) obj).isEmpty();
        if (obj instanceof Object[]) return ((Object[]) obj).length == 0;
        return false;
    }

    /**
     * 验证是否不为空
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 验证字符串是否匹配正则
     */
    public static boolean matches(String input, String regex) {
        if (!StringUtils.hasText(input)) return false;
        return Pattern.matches(regex, input);
    }

    /**
     * 验证 IP 是否在指定网段内
     */
    public static boolean isIpInRange(String ip, String cidr) {
        if (!StringUtils.hasText(ip) || !StringUtils.hasText(cidr)) return false;

        try {
            String[] parts = cidr.split("/");
            String network = parts[0];
            int prefix = Integer.parseInt(parts[1]);

            long ipNum = ipToLong(ip);
            long networkNum = ipToLong(network);
            long mask = 0xFFFFFFFFL << (32 - prefix);

            return (ipNum & mask) == (networkNum & mask);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * IP 地址转长整型
     */
    private static long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result = result * 256 + Long.parseLong(parts[i]);
        }
        return result;
    }
}
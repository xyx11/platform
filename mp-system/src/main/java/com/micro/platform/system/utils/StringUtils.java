package com.micro.platform.system.utils;

import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 字符串工具类
 */
public class StringUtils {

    /**
     * 检查字符串是否为空（null、空字符串、纯空格）
     */
    public static boolean isBlank(String str) {
        if (str == null || str.isEmpty()) return true;
        for (char c : str.toCharArray()) {
            if (!Character.isWhitespace(c)) return false;
        }
        return true;
    }

    /**
     * 检查字符串是否不为空
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 检查字符串是否为空（null 或空字符串）
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 检查字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 去除首尾空格，null 返回空字符串
     */
    public static String trim(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * 去除首尾空格，null 返回 null
     */
    public static String trimToNull(String str) {
        if (str == null) return null;
        String trimmed = str.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 首字母大写
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 首字母小写
     */
    public static String uncapitalize(String str) {
        if (isEmpty(str)) return str;
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 驼峰转下划线
     */
    public static String camelToUnderline(String str) {
        if (isEmpty(str)) return str;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) sb.append('_');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 下划线转驼峰
     */
    public static String underlineToCamel(String str) {
        if (isEmpty(str)) return str;
        String[] parts = str.split("_");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            sb.append(capitalize(parts[i]));
        }
        return sb.toString();
    }

    /**
     * 下划线转大驼峰
     */
    public static String underlineToPascal(String str) {
        return capitalize(underlineToCamel(str));
    }

    /**
     * 驼峰转短横线（kebab-case）
     */
    public static String camelToKebab(String str) {
        if (isEmpty(str)) return str;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) sb.append('-');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 短横线转驼峰
     */
    public static String kebabToCamel(String str) {
        if (isEmpty(str)) return str;
        String[] parts = str.split("-");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            sb.append(capitalize(parts[i]));
        }
        return sb.toString();
    }

    /**
     * 重复字符串 n 次
     */
    public static String repeat(String str, int count) {
        if (isEmpty(str) || count <= 0) return "";
        return Collections.nCopies(count, str).stream().collect(Collectors.joining());
    }

    /**
     * 字符串右侧补全
     */
    public static String padRight(String str, int length, char padChar) {
        if (str == null) str = "";
        if (str.length() >= length) return str;
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < length) {
            sb.append(padChar);
        }
        return sb.toString();
    }

    /**
     * 字符串左侧补全
     */
    public static String padLeft(String str, int length, char padChar) {
        if (str == null) str = "";
        if (str.length() >= length) return str;
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - str.length()) {
            sb.append(padChar);
        }
        sb.append(str);
        return sb.toString();
    }

    /**
     * 截取字符串（超出长度显示省略号）
     */
    public static String truncate(String str, int maxLength, String suffix) {
        if (isEmpty(str)) return str;
        if (suffix == null) suffix = "...";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - suffix.length()) + suffix;
    }

    /**
     * 截取字符串
     */
    public static String substring(String str, int start, int end) {
        if (isEmpty(str)) return str;
        if (start < 0) start = 0;
        if (end > str.length()) end = str.length();
        if (start >= end) return "";
        return str.substring(start, end);
    }

    /**
     * 截取字符串（前 N 个字符）
     */
    public static String left(String str, int n) {
        if (isEmpty(str) || n <= 0) return "";
        return str.substring(0, Math.min(n, str.length()));
    }

    /**
     * 截取字符串（后 N 个字符）
     */
    public static String right(String str, int n) {
        if (isEmpty(str) || n <= 0) return "";
        int start = Math.max(0, str.length() - n);
        return str.substring(start);
    }

    /**
     * 检查字符串是否包含子串
     */
    public static boolean contains(String str, String searchStr) {
        if (isEmpty(str) || isEmpty(searchStr)) return false;
        return str.contains(searchStr);
    }

    /**
     * 检查字符串是否以指定前缀开头
     */
    public static boolean startsWith(String str, String prefix) {
        if (isEmpty(str) || isEmpty(prefix)) return false;
        return str.startsWith(prefix);
    }

    /**
     * 检查字符串是否以指定后缀结尾
     */
    public static boolean endsWith(String str, String suffix) {
        if (isEmpty(str) || isEmpty(suffix)) return false;
        return str.endsWith(suffix);
    }

    /**
     * 忽略大小写检查字符串是否包含子串
     */
    public static boolean containsIgnoreCase(String str, String searchStr) {
        if (isEmpty(str) || isEmpty(searchStr)) return false;
        return str.toLowerCase().contains(searchStr.toLowerCase());
    }

    /**
     * 忽略大小写检查字符串是否相等
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == str2) return true;
        if (str1 == null || str2 == null) return false;
        return str1.equalsIgnoreCase(str2);
    }

    /**
     * 字符串转整数（失败返回默认值）
     */
    public static int toInt(String str, int defaultValue) {
        if (isEmpty(str)) return defaultValue;
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 字符串转整数（失败返回 null）
     */
    public static Integer toIntOrNull(String str) {
        if (isEmpty(str)) return null;
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 字符串转长整数（失败返回默认值）
     */
    public static long toLong(String str, long defaultValue) {
        if (isEmpty(str)) return defaultValue;
        try {
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 字符串转长整数（失败返回 null）
     */
    public static Long toLongOrNull(String str) {
        if (isEmpty(str)) return null;
        try {
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 字符串转浮点数（失败返回默认值）
     */
    public static double toDouble(String str, double defaultValue) {
        if (isEmpty(str)) return defaultValue;
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 字符串转浮点数（失败返回 null）
     */
    public static Double toDoubleOrNull(String str) {
        if (isEmpty(str)) return null;
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 字符串转布尔值
     */
    public static boolean toBoolean(String str) {
        return "true".equalsIgnoreCase(trim(str));
    }

    /**
     * 字符串转布尔值（失败返回默认值）
     */
    public static Boolean toBooleanOrNull(String str, Boolean defaultValue) {
        if (isEmpty(str)) return defaultValue;
        String trimmed = str.trim().toLowerCase();
        if ("true".equals(trimmed) || "1".equals(trimmed) || "yes".equals(trimmed)) {
            return true;
        }
        if ("false".equals(trimmed) || "0".equals(trimmed) || "no".equals(trimmed)) {
            return false;
        }
        return defaultValue;
    }

    /**
     * 分割字符串
     */
    public static List<String> split(String str, String delimiter) {
        if (isEmpty(str)) return Collections.emptyList();
        return Arrays.stream(str.split(delimiter))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 分割字符串（逗号分隔）
     */
    public static List<String> splitByComma(String str) {
        return split(str, ",");
    }

    /**
     * 分割字符串（分号分隔）
     */
    public static List<String> splitBySemicolon(String str) {
        return split(str, ";");
    }

    /**
     * 分割字符串（空格分隔）
     */
    public static List<String> splitBySpace(String str) {
        return split(str, "\\s+");
    }

    /**
     * 连接字符串
     */
    public static String join(Collection<?> collection, String delimiter) {
        if (collection == null || collection.isEmpty()) return "";
        return String.join(delimiter, collection.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toList()));
    }

    /**
     * 连接字符串（逗号分隔）
     */
    public static String joinByComma(Collection<?> collection) {
        return join(collection, ",");
    }

    /**
     * 替换字符串中第一次出现的子串
     */
    public static String replaceOnce(String str, String searchStr, String replaceStr) {
        if (isEmpty(str) || isEmpty(searchStr)) return str;
        int index = str.indexOf(searchStr);
        if (index < 0) return str;
        return str.substring(0, index) + replaceStr + str.substring(index + searchStr.length());
    }

    /**
     * 替换字符串中所有出现的子串（忽略大小写）
     */
    public static String replaceIgnoreCase(String str, String searchStr, String replaceStr) {
        if (isEmpty(str) || isEmpty(searchStr)) return str;
        String lowerStr = str.toLowerCase();
        String lowerSearch = searchStr.toLowerCase();
        StringBuilder result = new StringBuilder();
        int lastIndex = 0;

        while (lastIndex < str.length()) {
            int index = lowerStr.indexOf(lowerSearch, lastIndex);
            if (index < 0) {
                result.append(str.substring(lastIndex));
                break;
            }
            result.append(str, lastIndex, index);
            result.append(replaceStr);
            lastIndex = index + searchStr.length();
        }

        return result.toString();
    }

    /**
     * 移除字符串中所有出现的子串
     */
    public static String remove(String str, String removeStr) {
        if (isEmpty(str) || isEmpty(removeStr)) return str;
        return str.replace(removeStr, "");
    }

    /**
     * 移除字符串中的所有空格
     */
    public static String removeWhitespace(String str) {
        if (isEmpty(str)) return str;
        return str.replaceAll("\\s+", "");
    }

    /**
     * 反转字符串
     */
    public static String reverse(String str) {
        if (isEmpty(str)) return str;
        return new StringBuilder(str).reverse().toString();
    }

    /**
     * 检查字符串是否为数字
     */
    public static boolean isNumeric(String str) {
        if (isEmpty(str)) return false;
        return str.matches("^-?\\d+(\\.\\d+)?$");
    }

    /**
     * 检查字符串是否为字母
     */
    public static boolean isAlpha(String str) {
        if (isEmpty(str)) return false;
        return str.matches("^[a-zA-Z]+$");
    }

    /**
     * 检查字符串是否为字母或数字
     */
    public static boolean isAlphanumeric(String str) {
        if (isEmpty(str)) return false;
        return str.matches("^[a-zA-Z0-9]+$");
    }

    /**
     * 检查字符串是否为中文
     */
    public static boolean isChinese(String str) {
        if (isEmpty(str)) return false;
        return str.matches("^[\\u4e00-\\u9fa5]+$");
    }

    /**
     * 统计子串出现次数
     */
    public static int countMatches(String str, String sub) {
        if (isEmpty(str) || isEmpty(sub)) return 0;
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(sub, index)) != -1) {
            count++;
            index += sub.length();
        }
        return count;
    }

    /**
     * HTML 转义
     */
    public static String escapeHtml(String str) {
        if (isEmpty(str)) return str;
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * HTML 反转义
     */
    public static String unescapeHtml(String str) {
        if (isEmpty(str)) return str;
        return str.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&amp;", "&");
    }

    /**
     * 生成随机字符串
     */
    public static String randomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成随机数字字符串
     */
    public static String randomNumeric(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 检查字符串是否在数组中
     */
    public static boolean isIn(String str, String... values) {
        if (isEmpty(str) || values == null || values.length == 0) return false;
        for (String value : values) {
            if (str.equals(value)) return true;
        }
        return false;
    }

    /**
     * 检查字符串是否在数组中（忽略大小写）
     */
    public static boolean isInIgnoreCase(String str, String... values) {
        if (isEmpty(str) || values == null || values.length == 0) return false;
        for (String value : values) {
            if (str.equalsIgnoreCase(value)) return true;
        }
        return false;
    }
}
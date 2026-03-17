package com.micro.platform.system.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

/**
 * 加密工具类
 */
public class CryptoUtils {

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * MD5 加密
     *
     * @param input 输入字符串
     * @return MD5 加密后的字符串（32 位小写）
     */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            return byteArrayToHex(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * SHA-256 加密
     *
     * @param input 输入字符串
     * @return SHA-256 加密后的字符串（64 位小写）
     */
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return byteArrayToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * SHA-512 加密
     *
     * @param input 输入字符串
     * @return SHA-512 加密后的字符串（128 位小写）
     */
    public static String sha512(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(input.getBytes());
            return byteArrayToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 algorithm not found", e);
        }
    }

    /**
     * 字节数组转十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(HEX_DIGITS[(b >> 4) & 0xf]);
            sb.append(HEX_DIGITS[b & 0xf]);
        }
        return sb.toString();
    }

    /**
     * 生成随机盐
     *
     * @param length 盐长度
     * @return 随机盐字符串
     */
    public static String generateSalt(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(HEX_DIGITS[random.nextInt(HEX_DIGITS.length)]);
        }
        return sb.toString();
    }

    /**
     * 生成 UUID（无横杠）
     *
     * @return UUID 字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成短 UUID（8 位）
     *
     * @return 短 UUID 字符串
     */
    public static String generateShortUUID() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Base64 编码
     *
     * @param input 输入字符串
     * @return Base64 编码后的字符串
     */
    public static String base64Encode(String input) {
        return java.util.Base64.getEncoder().encodeToString(input.getBytes());
    }

    /**
     * Base64 解码
     *
     * @param input Base64 编码字符串
     * @return 解码后的字符串
     */
    public static String base64Decode(String input) {
        return new String(java.util.Base64.getDecoder().decode(input));
    }

    /**
     * HMAC-SHA256 加密
     *
     * @param data 数据
     * @param key  密钥
     * @return HMAC-SHA256 加密后的字符串
     */
    public static String hmacSHA256(String data, String key) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(
                    key.getBytes(), "HmacSHA256");
            mac.init(keySpec);
            byte[] hmacData = mac.doFinal(data.getBytes());
            return byteArrayToHex(hmacData);
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 error", e);
        }
    }
}
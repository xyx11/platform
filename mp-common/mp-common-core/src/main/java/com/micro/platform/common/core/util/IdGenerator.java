package com.micro.platform.common.core.util;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ID 生成器工具类
 * 基于雪花算法改进版
 */
public class IdGenerator {

    /** 起始时间戳（2024-01-01 00:00:00 UTC） */
    private static final long START_TIMESTAMP = 1704067200000L;

    /** 机器 ID 位数 */
    private static final long MACHINE_BIT = 5L;

    /** 数据中心 ID 位数 */
    private static final long DATA_CENTER_BIT = 5L;

    /** 序列号位数 */
    private static final long SEQUENCE_BIT = 12L;

    /** 机器 ID 最大值 */
    private static final long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);

    /** 数据中心 ID 最大值 */
    private static final long MAX_DATA_CENTER_NUM = ~(-1L << DATA_CENTER_BIT);

    /** 序列号最大值 */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    /** 机器 ID 偏移量 */
    private static final long MACHINE_SHIFT = SEQUENCE_BIT;

    /** 数据中心 ID 偏移量 */
    private static final long DATA_CENTER_SHIFT = SEQUENCE_BIT + MACHINE_BIT;

    /** 时间戳偏移量 */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BIT + MACHINE_BIT + DATA_CENTER_BIT;

    private final long machineId;
    private final long dataCenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private static final IdGenerator instance = new IdGenerator();

    public IdGenerator() {
        this(1L, 1L);
    }

    public IdGenerator(long machineId, long dataCenterId) {
        if (machineId < 0 || machineId > MAX_MACHINE_NUM) {
            throw new IllegalArgumentException("Machine ID must be between 0 and " + MAX_MACHINE_NUM);
        }
        if (dataCenterId < 0 || dataCenterId > MAX_DATA_CENTER_NUM) {
            throw new IllegalArgumentException("Data Center ID must be between 0 and " + MAX_DATA_CENTER_NUM);
        }
        this.machineId = machineId;
        this.dataCenterId = dataCenterId;
    }

    /**
     * 获取单例实例
     */
    public static IdGenerator getInstance() {
        return instance;
    }

    /**
     * 生成下一个 ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id for "
                + (lastTimestamp - timestamp) + " milliseconds");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0L) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_SHIFT)
                | (machineId << MACHINE_SHIFT)
                | sequence;
    }

    /**
     * 等待下一个毫秒
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 生成雪花 ID（字符串格式）
     */
    public static String nextSnowflakeId() {
        return String.valueOf(instance.nextId());
    }

    /**
     * 生成 UUID（不含横杠）
     */
    public static String nextUUID() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成简短 UUID（32 位）
     */
    public static String nextShortUUID() {
        return nextUUID();
    }

    /**
     * 生成随机字符串
     */
    public static String nextRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成随机数字字符串
     */
    public static String nextRandomNumeric(int length) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 生成验证码（6 位数字）
     */
    public static String nextVerificationCode() {
        return nextRandomNumeric(6);
    }

    /**
     * 生成验证码（4 位数字）
     */
    public static String nextVerificationCode4() {
        return nextRandomNumeric(4);
    }

    /**
     * 生成订单号（时间戳 + 随机数）
     */
    public static String nextOrderId() {
        long timestamp = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return timestamp + String.valueOf(random);
    }

    /**
     * 生成交易流水号
     */
    public static String nextTransactionId() {
        return "TXN" + System.currentTimeMillis() + nextRandomNumeric(4);
    }

    /**
     * 生成支付流水号
     */
    public static String nextPaymentId() {
        return "PAY" + System.currentTimeMillis() + nextRandomNumeric(4);
    }

    /**
     * 生成退款单号
     */
    public static String nextRefundId() {
        return "REF" + System.currentTimeMillis() + nextRandomNumeric(4);
    }
}
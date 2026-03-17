package com.micro.platform.common.core.util;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 限流工具类
 * 基于令牌桶算法
 */
public class RateLimiterUtils {

    private static final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    /**
     * 获取或创建限流器
     *
     * @param key 限流键
     * @param permitsPerSecond 每秒允许的请求数
     * @return 限流器
     */
    public static RateLimiter getLimiter(String key, double permitsPerSecond) {
        return limiters.computeIfAbsent(key, k -> RateLimiter.create(permitsPerSecond));
    }

    /**
     * 尝试获取许可
     *
     * @param key 限流键
     * @param permitsPerSecond 每秒允许的请求数
     * @return 是否获取成功
     */
    public static boolean tryAcquire(String key, double permitsPerSecond) {
        RateLimiter limiter = getLimiter(key, permitsPerSecond);
        return limiter.tryAcquire();
    }

    /**
     * 尝试获取许可（带超时）
     *
     * @param key 限流键
     * @param permitsPerSecond 每秒允许的请求数
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 是否获取成功
     */
    public static boolean tryAcquire(String key, double permitsPerSecond,
                                     long timeout, TimeUnit unit) {
        RateLimiter limiter = getLimiter(key, permitsPerSecond);
        return limiter.tryAcquire(timeout, unit);
    }

    /**
     * 阻塞获取许可
     *
     * @param key 限流键
     * @param permitsPerSecond 每秒允许的请求数
     */
    public static void acquire(String key, double permitsPerSecond) {
        RateLimiter limiter = getLimiter(key, permitsPerSecond);
        limiter.acquire();
    }

    /**
     * 移除限流器
     *
     * @param key 限流键
     */
    public static void removeLimiter(String key) {
        limiters.remove(key);
    }

    /**
     * 清空所有限流器
     */
    public static void clearAll() {
        limiters.clear();
    }

    /**
     * 简单的限流器实现（基于令牌桶）
     */
    public static class RateLimiter {

        private final double permitsPerSecond;
        private final long maxPermits;
        private final long microsecondsPerPermit;
        private long storedPermits;
        private long nextFreeTicketMicros;

        private RateLimiter(double permitsPerSecond) {
            if (permitsPerSecond <= 0) {
                throw new IllegalArgumentException("permitsPerSecond must be positive");
            }
            this.permitsPerSecond = permitsPerSecond;
            this.microsecondsPerPermit = (long) (TimeUnit.SECONDS.toMicros(1) / permitsPerSecond);
            this.maxPermits = (long) (permitsPerSecond * 2); // 桶容量为 2 秒的许可数
            this.storedPermits = maxPermits;
            this.nextFreeTicketMicros = 0L;
        }

        public static RateLimiter create(double permitsPerSecond) {
            return new RateLimiter(permitsPerSecond);
        }

        /**
         * 尝试获取许可
         */
        public synchronized boolean tryAcquire() {
            long nowMicros = System.nanoTime() / 1000;
            if (nextFreeTicketMicros > nowMicros) {
                return false; // 需要等待
            }

            // 补充令牌
            long elapsedMicros = nowMicros - nextFreeTicketMicros;
            long newPermits = elapsedMicros / microsecondsPerPermit;
            storedPermits = Math.min(maxPermits, storedPermits + newPermits);
            nextFreeTicketMicros = nowMicros;

            if (storedPermits > 0) {
                storedPermits--;
                return true;
            }

            return false;
        }

        /**
         * 尝试获取许可（带超时）
         */
        public synchronized boolean tryAcquire(long timeout, TimeUnit unit) {
            long nowMicros = System.nanoTime() / 1000;
            long timeoutMicros = unit.toMicros(timeout);

            if (nextFreeTicketMicros > nowMicros + timeoutMicros) {
                return false;
            }

            // 补充令牌
            long elapsedMicros = nowMicros - nextFreeTicketMicros;
            long newPermits = elapsedMicros / microsecondsPerPermit;
            storedPermits = Math.min(maxPermits, storedPermits + newPermits);
            nextFreeTicketMicros = nowMicros;

            if (storedPermits > 0) {
                storedPermits--;
                return true;
            }

            // 需要等待，检查等待时间是否在超时范围内
            long waitTime = nextFreeTicketMicros - nowMicros + microsecondsPerPermit;
            if (waitTime <= timeoutMicros) {
                try {
                    Thread.sleep(TimeUnit.MICROSECONDS.toMillis(waitTime));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
                nextFreeTicketMicros += waitTime;
                return true;
            }

            return false;
        }

        /**
         * 阻塞获取许可
         */
        public synchronized void acquire() {
            long nowMicros = System.nanoTime() / 1000;

            // 补充令牌
            if (nextFreeTicketMicros < nowMicros) {
                long elapsedMicros = nowMicros - nextFreeTicketMicros;
                long newPermits = elapsedMicros / microsecondsPerPermit;
                storedPermits = Math.min(maxPermits, storedPermits + newPermits);
                nextFreeTicketMicros = nowMicros;
            }

            if (storedPermits > 0) {
                storedPermits--;
            } else {
                long waitTime = nextFreeTicketMicros - nowMicros + microsecondsPerPermit;
                try {
                    Thread.sleep(TimeUnit.MICROSECONDS.toMillis(waitTime));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                nextFreeTicketMicros += waitTime;
            }
        }

        /**
         * 获取当前可用许可数
         */
        public synchronized double getAvailablePermits() {
            long nowMicros = System.nanoTime() / 1000;
            if (nextFreeTicketMicros < nowMicros) {
                long elapsedMicros = nowMicros - nextFreeTicketMicros;
                long newPermits = elapsedMicros / microsecondsPerPermit;
                return Math.min(maxPermits, storedPermits + newPermits);
            }
            return storedPermits;
        }

        /**
         * 获取限流速率
         */
        public double getRate() {
            return permitsPerSecond;
        }
    }

    /**
     * 固定窗口限流
     */
    public static class FixedWindowLimiter {

        private final int limit;
        private final long windowMs;
        private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

        public FixedWindowLimiter(int limit, long windowMs) {
            this.limit = limit;
            this.windowMs = windowMs;
        }

        /**
         * 尝试请求
         */
        public boolean tryAcquire(String key) {
            long now = System.currentTimeMillis();
            WindowCounter counter = counters.computeIfAbsent(key, k -> new WindowCounter(now));

            synchronized (counter) {
                if (now - counter.windowStart >= windowMs) {
                    // 窗口过期，重置
                    counter.windowStart = now;
                    counter.count = 0;
                }

                if (counter.count < limit) {
                    counter.count++;
                    return true;
                }
                return false;
            }
        }

        private static class WindowCounter {
            long windowStart;
            int count;

            WindowCounter(long windowStart) {
                this.windowStart = windowStart;
                this.count = 0;
            }
        }
    }

    /**
     * 滑动窗口限流
     */
    public static class SlidingWindowLimiter {

        private final int limit;
        private final long windowMs;
        private final int slots;
        private final long slotMs;
        private final Map<String, long[][]> windows = new ConcurrentHashMap<>();

        public SlidingWindowLimiter(int limit, long windowMs, int slots) {
            this.limit = limit;
            this.windowMs = windowMs;
            this.slots = slots;
            this.slotMs = windowMs / slots;
        }

        /**
         * 尝试请求
         */
        public boolean tryAcquire(String key) {
            long now = System.currentTimeMillis();
            long[][] window = windows.computeIfAbsent(key, k -> new long[slots][2]);

            synchronized (window) {
                int currentSlot = (int) ((now % windowMs) / slotMs);

                // 更新当前槽的时间戳和计数
                if (now - window[currentSlot][0] > windowMs) {
                    window[currentSlot][0] = now;
                    window[currentSlot][1] = 0;
                }

                // 计算窗口内的总请求数
                long totalCount = 0;
                for (int i = 0; i < slots; i++) {
                    if (now - window[i][0] <= windowMs) {
                        totalCount += window[i][1];
                    }
                }

                if (totalCount < limit) {
                    window[currentSlot][1]++;
                    return true;
                }
                return false;
            }
        }
    }
}
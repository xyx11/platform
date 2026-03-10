package com.micro.platform.common.redis.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 * 提供丰富的 Redis 操作方法
 */
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ==================== String 操作 ====================

    /**
     * 设置指定 key 的值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置指定 key 的值（带过期时间）
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 设置指定 key 的值（带毫秒级过期时间）
     */
    public void setWithMillis(String key, Object value, long milliseconds) {
        redisTemplate.opsForValue().set(key, value, milliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取指定 key 的值
     */
    public <T> T get(String key) {
        Object obj = redisTemplate.opsForValue().get(key);
        return obj != null ? (T) obj : null;
    }

    /**
     * 获取并删除指定 key 的值
     */
    public <T> T getAndDelete(String key) {
        Object obj = redisTemplate.opsForValue().getAndDelete(key);
        return obj != null ? (T) obj : null;
    }

    /**
     * 设置指定 key 的值（如果不存在）
     * @return 设置成功返回 true，否则返回 false
     */
    public Boolean setIfAbsent(String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 设置指定 key 的值（如果不存在，带过期时间）
     * @return 设置成功返回 true，否则返回 false
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }

    /**
     * 自增 1
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 自增指定值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 自减 1
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * 自减指定值
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    // ==================== 通用操作 ====================

    /**
     * 删除指定 key
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 根据 pattern 删除 key
     */
    public Long deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            return redisTemplate.delete(keys);
        }
        return 0L;
    }

    /**
     * 判断 key 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取剩余过期时间（秒）
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 移除过期时间
     */
    public Boolean persist(String key) {
        return redisTemplate.persist(key);
    }

    // ==================== Hash 操作 ====================

    /**
     * 哈希 Set - 放入
     */
    public void hPut(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 哈希 Set - 批量放入
     */
    public void hPutAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 哈希 Set - 获取
     */
    public <T> T hGet(String key, String hashKey) {
        Object obj = redisTemplate.opsForHash().get(key, hashKey);
        return obj != null ? (T) obj : null;
    }

    /**
     * 哈希 Set - 获取所有
     */
    public List<Object> hValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }

    /**
     * 哈希 Set - 获取所有键
     */
    public Set<Object> hKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    /**
     * 哈希 Set - 获取所有字段和值
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 哈希 Set - 删除
     */
    public Long hDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    /**
     * 哈希 Set - 判断字段是否存在
     */
    public Boolean hHasKey(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    /**
     * 哈希 Set - 自增
     */
    public Long hIncrement(String key, String hashKey, long delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    // ==================== List 操作 ====================

    /**
     * List - 左侧推入
     */
    public Long lLeftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * List - 左侧批量推入
     */
    public Long lLeftPushAll(String key, Collection<Object> values) {
        return redisTemplate.opsForList().leftPushAll(key, values);
    }

    /**
     * List - 右侧推入
     */
    public Long lRightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * List - 右侧批量推入
     */
    public Long lRightPushAll(String key, Collection<Object> values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    /**
     * List - 获取指定范围的值
     */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * List - 获取所有值
     */
    public List<Object> lValues(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * List - 获取指定位置的元素
     */
    public <T> T lIndex(String key, long index) {
        Object obj = redisTemplate.opsForList().index(key, index);
        return obj != null ? (T) obj : null;
    }

    /**
     * List - 获取长度
     */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * List - 移除元素
     */
    public Long lRemove(String key, long count, Object value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }

    /**
     * List - 裁剪
     */
    public Boolean lTrim(String key, long start, long end) {
        redisTemplate.opsForList().trim(key, start, end);
        return true;
    }

    // ==================== Set 操作 ====================

    /**
     * Set - 添加
     */
    public Long sAdd(String key, Object value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    /**
     * Set - 批量添加
     */
    public Long sAdd(String key, Collection<Object> values) {
        return redisTemplate.opsForSet().add(key, values.toArray());
    }

    /**
     * Set - 获取所有
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * Set - 随机获取一个元素
     */
    public <T> T sRandomMember(String key) {
        Object obj = redisTemplate.opsForSet().randomMember(key);
        return obj != null ? (T) obj : null;
    }

    /**
     * Set - 随机获取 count 个不重复元素
     */
    public Set<Object> sRandomMembers(String key, long count) {
        return redisTemplate.opsForSet().distinctRandomMembers(key, count);
    }

    /**
     * Set - 判断是否存在
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * Set - 获取大小
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * Set - 删除
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    // ==================== ZSet 操作 ====================

    /**
     * ZSet - 添加
     */
    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * ZSet - 批量添加
     */
    public Long zAdd(String key, Set<ZSetOperations.TypedTuple<Object>> tuples) {
        return redisTemplate.opsForZSet().add(key, tuples);
    }

    /**
     * ZSet - 获取指定排名范围的元素（含分数）
     */
    public Set<ZSetOperations.TypedTuple<Object>> zRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    /**
     * ZSet - 获取指定分数范围的元素
     */
    public Set<Object> zRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * ZSet - 获取元素的排名
     */
    public Long zRank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * ZSet - 获取元素的倒序排名
     */
    public Long zReverseRank(String key, Object value) {
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }

    /**
     * ZSet - 获取元素的分数
     */
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * ZSet - 获取大小
     */
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * ZSet - 删除
     */
    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * ZSet - 按排名删除
     */
    public Long zRemoveRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().removeRange(key, start, end);
    }

    /**
     * ZSet - 按分数删除
     */
    public Long zRemoveRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    // ==================== 分布式锁 ====================

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁的 key
     * @param requestId 请求标识，用于释放锁时验证
     * @param expireTime 过期时间（毫秒）
     * @return 获取成功返回 true，否则返回 false
     */
    public Boolean tryLock(String lockKey, String requestId, long expireTime) {
        String script = "if redis.call('set', KEYS[1], ARGV[1], 'NX', 'PX', ARGV[2]) then return 1 else return 0 end";
        RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), requestId, String.valueOf(expireTime));
        return result != null && result == 1;
    }

    /**
     * 释放分布式锁
     * @param lockKey 锁的 key
     * @param requestId 请求标识
     * @return 释放成功返回 true，否则返回 false
     */
    public Boolean releaseLock(String lockKey, String requestId) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), requestId);
        return result != null && result == 1;
    }

    // ==================== 缓存穿透/雪崩/击穿防护 ====================

    /**
     * 缓存空值，防止缓存穿透
     * @param key 缓存 key
     * @param expireTime 过期时间（秒）
     */
    public void cacheNull(String key, long expireTime) {
        redisTemplate.opsForValue().set(key, "", expireTime, TimeUnit.SECONDS);
    }

    /**
     * 判断是否为缓存的空值
     */
    public Boolean isCachedNull(String key) {
        Object obj = redisTemplate.opsForValue().get(key);
        return obj instanceof String && "".equals(obj);
    }

    // ==================== 批量操作 ====================

    /**
     * 批量获取
     */
    public <T> Map<String, T> multiGet(Collection<String> keys) {
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        Map<String, T> result = new HashMap<>();
        if (values != null) {
            Iterator<String> keyIter = keys.iterator();
            for (Object value : values) {
                String key = keyIter.next();
                if (value != null) {
                    result.put(key, (T) value);
                }
            }
        }
        return result;
    }

    /**
     * 批量设置
     */
    public void multiSet(Map<String, Object> map) {
        redisTemplate.opsForValue().multiSet(map);
    }
}
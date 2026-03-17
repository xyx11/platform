# 缓存配置与使用

## 概述

系统提供多级缓存支持：
- 本地缓存（Caffeine）
- 分布式缓存（Redis）
- 二级缓存（Caffeine + Redis）

## 缓存配置

### 依赖配置

```xml
<!-- Redis 缓存 -->
<dependency>
    <groupId>com.micro.platform</groupId>
    <artifactId>mp-common-redis</artifactId>
</dependency>

<!-- Caffeine 本地缓存 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

### 缓存配置类

```java
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Caffeine 本地缓存配置
     */
    @Bean
    public CacheManager caffeineCacheManager() {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats();

        return new CaffeineCacheManager("users", "dicts", "configs", caffeine);
    }

    /**
     * Redis 缓存管理器
     */
    @Bean
    public CacheManager redisCacheManager(RedisTemplate<String, Object> redisTemplate) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(300, TimeUnit.SECONDS)
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(redisTemplate.getStringSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(redisTemplate.getValueSerializer()))
            .disableCachingNullValues();

        return RedisCacheManager.builder(redisTemplate.getConnectionFactory())
            .cacheDefaults(config)
            .withInitialCacheConfig(cacheNames ->
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(300, TimeUnit.SECONDS))
            .build();
    }

    /**
     * 二级缓存管理器（Caffeine + Redis）
     */
    @Bean
    public CacheManager tieredCacheManager(
            CacheManager caffeineCacheManager,
            CacheManager redisCacheManager) {
        return new TieredCacheManager(caffeineCacheManager, redisCacheManager);
    }
}
```

### 配置文件

```yaml
spring:
  cache:
    type: caffeine
    cache-names: users,dicts,configs,permissions
    caffeine:
      spec: maximumSize=10000,expireAfterWrite=5m

  redis:
    host: localhost
    port: 6379
    password:
    database: 0
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
```

## 缓存注解

### @Cacheable - 缓存查询

```java
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据 ID 查询用户（带缓存）
     */
    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    public UserVO getUserById(Long userId) {
        return userMapper.selectUserVOById(userId);
    }

    /**
     * 根据用户名查询用户
     */
    @Cacheable(value = "users", key = "'name:' + #userName")
    public User getUserByUserName(String userName) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getUserName, userName));
    }

    /**
     * 查询用户列表（带条件）
     */
    @Cacheable(value = "users", key = "#root.methodName + ':' + #deptId + ':' + #status")
    public List<User> getUserList(Long deptId, String status) {
        return userMapper.selectList(new LambdaQueryWrapper<User>()
            .eq(deptId != null, User::getDeptId, deptId)
            .eq(StringUtils.isNotBlank(status), User::getStatus, status));
    }
}
```

### @CachePut - 缓存更新

```java
@Service
public class UserService {

    /**
     * 更新用户（同时更新缓存）
     */
    @CachePut(value = "users", key = "#user.userId")
    public UserVO updateUser(User user) {
        userMapper.updateById(user);
        return userMapper.selectUserVOById(user.getUserId());
    }

    /**
     * 更新用户信息（指定 key）
     */
    @CachePut(value = "users", key = "'name:' + #userName")
    public User updateUserByName(String userName, User user) {
        userMapper.update(new LambdaUpdateWrapper<User>()
            .set(User::getNickName, user.getNickName())
            .set(User::getEmail, user.getEmail())
            .eq(User::getUserName, userName));
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getUserName, userName));
    }
}
```

### @CacheEvict - 缓存删除

```java
@Service
public class UserService {

    /**
     * 删除用户（同时删除缓存）
     */
    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(Long userId) {
        userMapper.deleteById(userId);
    }

    /**
     * 批量删除用户
     */
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUsers(List<Long> userIds) {
        userMapper.deleteBatchIds(userIds);
    }

    /**
     * 清除所有用户缓存
     */
    @CacheEvict(value = "users", allEntries = true)
    public void clearUserCache() {
        log.info("清除所有用户缓存");
    }
}
```

### 自定义缓存注解

```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Cacheable(value = "users", key = "#userId")
public @interface CacheableUser {
}

// 使用
@CacheableUser
public UserVO getUserById(Long userId) {
    return userMapper.selectUserVOById(userId);
}
```

## 缓存工具类

### Redis 缓存工具

```java
@Component
public class CacheUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取缓存
     */
    public <T> T get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? (T) value : null;
    }

    /**
     * 设置缓存
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存（带过期时间）
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 删除批量缓存
     */
    public void delete(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 检查缓存是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置缓存过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取缓存过期时间
     */
    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    /**
     * 自增
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 自减
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * 设置分布式锁
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }
}
```

### 缓存管理器

```java
@Service
public class CacheManagerService {

    @Autowired
    private CacheManager cacheManager;

    /**
     * 获取缓存
     */
    public <T> T getCache(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            ValueWrapper wrapper = cache.get(key);
            return wrapper != null ? (T) wrapper.get() : null;
        }
        return null;
    }

    /**
     * 设置缓存
     */
    public void putCache(String cacheName, Object key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }

    /**
     * 删除缓存
     */
    public void evictCache(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    /**
     * 清空缓存
     */
    public void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * 获取所有缓存名称
     */
    public Set<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }
}
```

## 缓存策略

### 1. 热点数据缓存

```java
@Service
public class DictService {

    @Autowired
    private DictMapper dictMapper;

    /**
     * 查询字典数据（热点数据，长缓存）
     */
    @Cacheable(value = "dicts", key = "#dictType", expire = 3600)
    public List<DictData> getDictData(String dictType) {
        return dictMapper.selectDictData(dictType);
    }

    /**
     * 清除字典缓存
     */
    @CacheEvict(value = "dicts", key = "#dictType")
    public void clearDictCache(String dictType) {
        log.info("清除字典缓存：{}", dictType);
    }
}
```

### 2. 配置数据缓存

```java
@Service
public class ConfigService {

    @Autowired
    private ConfigMapper configMapper;

    /**
     * 查询配置（长期缓存）
     */
    @Cacheable(value = "configs", key = "#configKey")
    public String getConfigValue(String configKey) {
        SysConfig config = configMapper.selectConfigByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 清除配置缓存
     */
    @CacheEvict(value = "configs", key = "#configKey")
    public void clearConfigCache(String configKey) {
        log.info("清除配置缓存：{}", configKey);
    }
}
```

### 3. 权限数据缓存

```java
@Service
public class PermissionService {

    /**
     * 查询用户权限（短缓存）
     */
    @Cacheable(value = "permissions", key = "'user:' + #userId", expire = 300)
    public Set<String> getUserPermissions(Long userId) {
        // 查询用户权限
        return permissionMapper.selectUserPermissions(userId);
    }

    /**
     * 查询角色权限
     */
    @Cacheable(value = "permissions", key = "'role:' + #roleId", expire = 600)
    public Set<String> getRolePermissions(Long roleId) {
        return permissionMapper.selectRolePermissions(roleId);
    }

    /**
     * 清除用户权限缓存
     */
    @CacheEvict(value = "permissions", key = "'user:' + #userId")
    public void clearUserPermissionCache(Long userId) {
        log.info("清除用户权限缓存：{}", userId);
    }

    /**
     * 清除角色权限缓存
     */
    @CacheEvict(value = "permissions", key = "'role:' + #roleId")
    public void clearRolePermissionCache(Long roleId) {
        log.info("清除角色权限缓存：{}", roleId);
    }
}
```

### 4. 缓存穿透处理

```java
@Service
public class UserService {

    /**
     * 查询用户（处理缓存穿透）
     * 使用空值缓存防止缓存穿透
     */
    @Cacheable(value = "users", key = "#userId", unless = "#result == null", expire = 60)
    public UserVO getUserById(Long userId) {
        UserVO user = userMapper.selectUserVOById(userId);
        if (user == null) {
            // 缓存空值 60 秒
            cacheUserNull(userId);
        }
        return user;
    }

    private void cacheUserNull(Long userId) {
        redisTemplate.opsForValue().set("users:" + userId, null, 60, TimeUnit.SECONDS);
    }
}
```

### 5. 缓存击穿处理（分布式锁）

```java
@Service
public class UserService {

    @Autowired
    private RedisDistributedLock lock;

    /**
     * 查询热点数据（防止缓存击穿）
     */
    public UserVO getUserWithLock(Long userId) {
        String key = "users:" + userId;
        UserVO user = cacheUtils.get(key);
        if (user != null) {
            return user;
        }

        // 使用分布式锁
        return lock.executeWithLock("lock:user:" + userId, 30, () -> {
            // 双重检查
            user = cacheUtils.get(key);
            if (user != null) {
                return user;
            }
            // 查询数据库
            user = userMapper.selectUserVOById(userId);
            // 写入缓存
            cacheUtils.set(key, user, 300, TimeUnit.SECONDS);
            return user;
        });
    }
}
```

### 6. 缓存雪崩处理

```java
@Service
public class DictService {

    /**
     * 查询字典（随机过期时间防止雪崩）
     */
    public List<DictData> getDictData(String dictType) {
        String key = "dicts:" + dictType;
        List<DictData> data = cacheUtils.get(key);
        if (data != null) {
            return data;
        }

        data = dictMapper.selectDictData(dictType);
        // 基础时间 + 随机时间（防止雪崩）
        long expire = 3600 + new Random().nextInt(600);
        cacheUtils.set(key, data, expire, TimeUnit.SECONDS);
        return data;
    }
}
```

## 缓存监控

### 缓存统计

```java
@RestController
@RequestMapping("/monitor/cache")
public class CacheMonitorController {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取缓存统计
     */
    @GetMapping("/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        Set<String> cacheNames = cacheManager.getCacheNames();

        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache) {
                com.github.benmanes.caffeine.cache.Cache nativeCache =
                    ((CaffeineCache) cache).getNativeCache();
                Map<String, Object> cacheStats = new HashMap<>();
                cacheStats.put("size", nativeCache.estimatedSize());
                cacheStats.put("hits", nativeCache.stats().hitCount());
                cacheStats.put("misses", nativeCache.stats().missCount());
                cacheStats.put("hitRate", nativeCache.stats().hitRate());
                stats.put(cacheName, cacheStats);
            }
        }
        return stats;
    }

    /**
     * 获取 Redis 缓存信息
     */
    @GetMapping("/redis/info")
    public Map<String, Object> getRedisCacheInfo() {
        Map<String, Object> info = new HashMap<>();
        Properties props = redisTemplate.getConnectionFactory()
            .getConnection().info();
        info.put("usedMemory", props.getProperty("used_memory"));
        info.put("usedMemoryPeak", props.getProperty("used_memory_peak"));
        info.put("keys", redisTemplate.keys("*").size());
        return info;
    }
}
```

## 最佳实践

### 1. 缓存命名规范

```
users:{userId}           - 用户信息
dicts:{dictType}         - 字典数据
configs:{configKey}      - 配置信息
permissions:user:{userId} - 用户权限
permissions:role:{roleId} - 角色权限
sessions:{sessionId}     - 会话信息
tokens:{token}           - Token 信息
```

### 2. 缓存过期时间建议

| 数据类型 | 建议时间 | 说明 |
|----------|----------|------|
| 字典数据 | 1 小时 | 变动较少 |
| 配置信息 | 30 分钟 | 可能动态修改 |
| 用户信息 | 5 分钟 | 可能频繁修改 |
| 权限数据 | 5 分钟 | 可能动态调整 |
| 会话信息 | 30 分钟 | 与登录超时一致 |
| 验证码 | 5 分钟 | 短时效 |

### 3. 缓存更新策略

```java
// 策略 1：更新数据库后删除缓存
@Transactional
public void updateUser(User user) {
    userMapper.updateById(user);
    cacheEvict("users", user.getUserId());
}

// 策略 2：更新数据库后更新缓存
@Transactional
public void updateConfig(SysConfig config) {
    configMapper.updateById(config);
    cachePut("configs", config.getConfigKey(), config.getConfigValue());
}

// 策略 3：延迟双删
@Transactional
public void deleteWithDelay(Long userId) {
    // 先删缓存
    cacheEvict("users", userId);
    // 再删数据库
    userMapper.deleteById(userId);
    // 延迟后再删缓存（防止主从同步延迟）
    CompletableFuture.runAsync(() -> {
        try {
            Thread.sleep(500);
            cacheEvict("users", userId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    });
}
```
# Common Core 模块

`mp-common-core` 模块提供了微服务平台的通用核心功能，包括工具类、注解、切面、配置等。

## 目录结构

```
mp-common-core/
├── annotation/          # 注解类
│   ├── AsyncExecute.java      # 异步执行注解
│   ├── Cache.java             # 缓存注解
│   ├── CacheEvict.java        # 缓存删除注解
│   ├── DataMask.java          # 数据脱敏注解
│   ├── Dict.java              # 数据字典注解
│   ├── ExcelColumn.java       # Excel 列注解
│   ├── OperationLog.java      # 操作日志注解
│   ├── OperationType.java     # 操作类型枚举
│   ├── PreventDuplicate.java  # 防重复提交注解
│   └── RateLimit.java         # 限流注解
├── aspect/            # AOP 切面类
│   ├── AsyncExecuteAspect.java    # 异步执行切面
│   ├── CacheAspect.java           # 缓存切面
│   ├── DataDictAspect.java        # 字典翻译切面
│   ├── DataMaskAspect.java        # 数据脱敏切面
│   ├── OperationLogAspect.java    # 操作日志切面
│   ├── PreventDuplicateAspect.java # 防重复提交切面
│   └── RateLimitAspect.java       # 限流切面
├── config/            # 配置类
│   ├── WebSocketConfig.java     # WebSocket 配置
│   └── ...
├── entity/            # 实体类
├── enums/             # 枚举类
├── exception/         # 异常处理
├── interceptor/       # 拦截器
├── mapper/            # MyBatis 映射器
├── query/             # 查询对象
├── result/            # 响应结果
├── service/           # 服务层
└── util/              # 工具类
    ├── CollectionUtils.java     # 集合工具
    ├── DictUtils.java           # 字典工具
    ├── ExcelUtils.java          # Excel 工具
    ├── HttpUtils.java           # HTTP 工具
    ├── IdGenerator.java         # ID 生成器
    ├── RateLimiterUtils.java    # 限流工具
    ├── SpElUtils.java           # SpEL 表达式工具
    └── SecurityUtil.java        # 安全工具
```

## 功能模块

### 注解系统

| 注解 | 说明 | 切面支持 |
|------|------|----------|
| @OperationLog | 操作日志记录 | ✓ |
| @AsyncExecute | 异步执行 | ✓ |
| @Cacheable | 方法缓存 | ✓ |
| @CacheEvict | 缓存删除 | ✓ |
| @RateLimit | 限流控制 | ✓ |
| @PreventDuplicate | 防重复提交 | ✓ |
| @DataMask | 数据脱敏 | ✓ |
| @Dict | 字典翻译 | ✓ |
| @ExcelColumn | Excel 列定义 | - |

### 工具类

#### 核心工具

- **IdGenerator**: 分布式 ID 生成器（雪花算法）
- **CollectionUtils**: 集合操作（68+ 方法）
- **HttpUtils**: HTTP 客户端
- **SpElUtils**: SpEL 表达式解析

#### 业务工具

- **DictUtils**: 字典数据缓存与翻译
- **ExcelUtils**: Excel 导入导出（Apache POI）
- **RateLimiterUtils**: 限流器（令牌桶、固定窗口、滑动窗口）

### AOP 切面

| 切面 | 优先级 | 功能 |
|------|--------|------|
| OperationLogAspect | 1 | 记录操作日志 |
| AsyncExecuteAspect | 2 | 异步执行方法 |
| CacheAspect | 3 | 方法缓存管理 |
| RateLimitAspect | 4 | 接口限流控制 |
| PreventDuplicateAspect | 5 | 防重复提交 |
| DataMaskAspect | 6 | 数据脱敏处理 |
| DataDictAspect | 7 | 字典翻译处理 |

## 使用示例

### 1. 操作日志

```java
@PostMapping("/add")
@OperationLog(module = "用户管理", type = OperationType.CREATE,
              description = "新增用户：#user.userName")
public Result addUser(@RequestBody User user) {
    userService.add(user);
    return Result.success();
}
```

### 2. 缓存管理

```java
@Cacheable(value = "users", key = "#userId", expire = 300)
public User getUserById(Long userId) {
    return userMapper.selectById(userId);
}

@CacheEvict(value = "users", key = "#user.id")
public void updateUser(User user) {
    userMapper.updateById(user);
}
```

### 3. 限流控制

```java
@GetMapping("/data")
@RateLimit(permitsPerSecond = 10.0, algorithm = Algorithm.TOKEN_BUCKET)
public Result getData() {
    return Result.success(dataService.getData());
}
```

### 4. 防重复提交

```java
@PostMapping("/create")
@PreventDuplicate(key = "#orderNo", expire = 5000)
public Result createOrder(@RequestBody Order order) {
    return Result.success(orderService.create(order));
}
```

### 5. 数据脱敏

```java
@Data
public class UserVO {
    @DataMask(type = MaskType.NAME)
    private String userName;  // 张*三

    @DataMask(type = MaskType.PHONE)
    private String phone;     // 138****1234
}
```

### 6. 字典翻译

```java
@Data
public class UserVO {
    @Dict(type = "user_sex")
    private String sex;  // 1 -> "男"
}
```

## 分布式 ID 生成

```java
// 生成分布式 ID
long id = IdGenerator.getInstance().nextId();

// 生成 UUID
String uuid = IdGenerator.nextUUID();

// 生成订单号
String orderId = IdGenerator.nextOrderId();
```

## 集合操作

```java
// 分块
List<List<T>> chunks = CollectionUtils.chunk(list, 100);

// 去重
List<T> distinct = CollectionUtils.distinctBy(list, User::getId);

// 集合运算
List<T> intersect = CollectionUtils.intersect(list1, list2);
List<T> union = CollectionUtils.union(list1, list2);
List<T> difference = CollectionUtils.difference(list1, list2);
```

## HTTP 请求

```java
// GET
String result = HttpUtils.get(url, params);

// POST
String result = HttpUtils.post(url, body, headers);

// 下载文件
HttpUtils.downloadFile(fileUrl, savePath);

// 上传文件
String response = HttpUtils.uploadFile(url, "file", file, params);
```

## 注意事项

1. **切面执行顺序**：多个注解同时使用时，按照优先级顺序执行
2. **SpEL 表达式**：支持 `#参数名`、`#参数名。属性` 访问参数
3. **性能考虑**：数据脱敏和字典翻译使用反射，大数据量时注意性能
4. **分布式限流**：当前限流为单机版，分布式场景需使用 Redis 限流
5. **缓存实现**：当前缓存为内存版，生产环境建议集成 Redis

## 相关文档

- [AOP 切面使用指南](/docs/guide/aop-aspects.md)
- [常用注解文档](/docs/guide/annotations.md)
- [工具类参考](/docs/guide/utils-reference.md)
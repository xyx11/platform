# AOP 切面使用指南

本模块提供了基于 Spring AOP 的切面支持，用于实现横切关注点的统一管理。

## 目录

- [操作日志切面](#操作日志切面)
- [异步执行切面](#异步执行切面)
- [缓存切面](#缓存切面)
- [限流切面](#限流切面)
- [防重复提交切面](#防重复提交切面)
- [数据脱敏切面](#数据脱敏切面)
- [数据字典翻译切面](#数据字典翻译切面)

---

## 操作日志切面

### 功能说明

自动记录带有 `@OperationLog` 注解的方法执行信息，包括操作模块、类型、描述、操作人、IP 地址、耗时等。

### 注解参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| module | String | "" | 操作模块名称 |
| type | OperationType | OTHER | 操作类型 |
| description | String | "" | 操作描述（支持 SpEL） |
| recordParams | boolean | true | 是否记录请求参数 |
| recordResult | boolean | false | 是否记录响应结果 |
| recordStatus | boolean | true | 是否记录操作状态 |
| operator | String | "" | 操作人字段（SpEL 表达式） |

### 使用示例

```java
@RestController
@RequestMapping("/user")
public class UserController {

    @PostMapping("/add")
    @OperationLog(module = "用户管理", type = OperationType.CREATE, description = "新增用户：#user.userName")
    public Result addUser(@RequestBody User user) {
        userService.add(user);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "用户管理", type = OperationType.UPDATE, description = "修改用户信息")
    public Result updateUser(@RequestBody User user) {
        userService.update(user);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "用户管理", type = OperationType.DELETE, description = "删除用户")
    public Result deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return Result.success();
    }
}
```

### SpEL 表达式支持

在 `description` 参数中可以使用 SpEL 表达式访问方法参数：

```java
@OperationLog(module = "订单管理", description = "创建订单：#order.orderNo")
public Result createOrder(@RequestBody Order order) { ... }

@OperationLog(module = "权限管理", description = "分配角色：#userId 的角色")
public Result assignRole(Long userId, List<Long> roleIds) { ... }
```

### 日志输出示例

```
操作日志 - 模块：用户管理，类型：新增，描述：新增用户：张三，操作人：admin, IP: 192.168.1.100, URI: /user/add, 状态：成功，耗时：120ms，错误：null
```

---

## 异步执行切面

### 功能说明

将带有 `@AsyncExecute` 注解的方法异步执行，适用于不需要立即返回结果的操作，如发送通知、记录日志等。

### 注解参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| executor | String | taskExecutor | 线程池名称 |
| timeout | long | 0 | 超时时间（毫秒） |
| retries | int | 0 | 重试次数 |
| retryDelay | long | 1000 | 重试延迟（毫秒） |
| recordException | boolean | true | 是否记录异常 |

### 使用示例

```java
@Service
public class NotificationService {

    /**
     * 异步发送通知（void 方法）
     */
    @AsyncExecute
    public void sendNotification(Long userId, String message) {
        // 发送站内信
        messageService.send(userId, message);
        // 发送邮件
        emailService.send(userId, message);
        // 发送短信
        smsService.send(userId, message);
    }

    /**
     * 带超时的异步执行
     */
    @AsyncExecute(timeout = 5000)
    public void sendEmailWithTimeout(Long userId, String content) {
        emailService.send(userId, content);
    }

    /**
     * 带重试的异步执行
     */
    @AsyncExecute(retries = 3, retryDelay = 1000)
    public void sendSmsWithRetry(Long userId, String content) {
        smsService.send(userId, content);
    }
}
```

### 注意事项

1. **返回类型限制**：`@AsyncExecute` 仅对 `void` 返回类型的方法有效。对于有返回值的方法，建议改用 `CompletableFuture`
2. **线程池配置**：生产环境应配置合适的线程池参数
3. **事务问题**：异步方法中的数据库操作不会继承调用方的事务

---

## 缓存切面

### 功能说明

自动缓存带有 `@Cache` 注解的方法返回值，并在 `@CacheEvict` 注解的方法执行后删除缓存。

### Cache 注解参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value | String | "" | 缓存名称（前缀） |
| key | String | "" | 缓存键（SpEL 表达式） |
| expire | long | 3600 | 过期时间（秒） |

### CacheEvict 注解参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value | String | "" | 缓存名称（前缀） |
| key | String | "" | 缓存键（SpEL 表达式） |
| allEntries | boolean | false | 是否删除所有条目 |

### 使用示例

```java
@Service
public class UserService {

    /**
     * 缓存用户信息
     */
    @Cacheable(value = "users", key = "#userId", expire = 300)
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 缓存用户列表
     */
    @Cacheable(value = "user:list", key = "#deptId", expire = 600)
    public List<User> getUserListByDept(Long deptId) {
        return userMapper.selectList(new LambdaQueryWrapper<User>()
            .eq(User::getDeptId, deptId));
    }

    /**
     * 删除用户缓存
     */
    @CacheEvict(value = "users", key = "#user.id")
    public void updateUser(User user) {
        userMapper.updateById(user);
    }

    /**
     * 清空所有用户缓存
     */
    @CacheEvict(value = "users", allEntries = true)
    public void clearUserCache() {
        log.info("清空用户缓存");
    }
}
```

### SpEL 表达式示例

```java
// 使用单个参数作为键
@Cacheable(value = "users", key = "#id")

// 使用多个参数组合作为键
@Cacheable(value = "users", key = "#deptId + ':' + #status")

// 使用方法参数对象的属性
@Cacheable(value = "users", key = "#user.username")

// 使用类类型（用于静态方法）
@Cacheable(value = "config", key = "T(java.lang.String).valueOf(#key)")
```

---

## 限流切面

### 功能说明

对带有 `@RateLimit` 注解的方法进行限流控制，支持多种限流算法。

### 注解参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| key | String | "" | 限流键（SpEL 表达式） |
| permitsPerSecond | double | 10.0 | 每秒允许的请求数 |
| algorithm | Algorithm | TOKEN_BUCKET | 限流算法 |
| windowMs | long | 1000 | 窗口大小（毫秒） |
| failFast | boolean | true | 是否快速失败 |
| message | String | "请求过于频繁" | 错误提示 |

### 限流算法

- **TOKEN_BUCKET**：令牌桶算法，平滑限流
- **FIXED_WINDOW**：固定窗口算法，简单高效
- **SLIDING_WINDOW**：滑动窗口算法，精确控制

### 使用示例

```java
@RestController
@RequestMapping("/api")
public class ApiController {

    /**
     * 限制每秒 10 次请求
     */
    @GetMapping("/data")
    @RateLimit(permitsPerSecond = 10.0)
    public Result getData() {
        return Result.success(dataService.getData());
    }

    /**
     * 按用户 ID 限流（每个用户每秒 5 次）
     */
    @GetMapping("/profile")
    @RateLimit(key = "#userId", permitsPerSecond = 5.0)
    public Result getProfile(Long userId) {
        return Result.success(userService.getProfile(userId));
    }

    /**
     * 使用固定窗口限流（每分钟 60 次）
     */
    @PostMapping("/submit")
    @RateLimit(
        algorithm = Algorithm.FIXED_WINDOW,
        permitsPerSecond = 1.0,
        windowMs = 60000
    )
    public Result submitForm(@RequestBody Form form) {
        return Result.success(formService.submit(form));
    }

    /**
     * 滑动窗口限流（更精确）
     */
    @PostMapping("/order")
    @RateLimit(
        algorithm = Algorithm.SLIDING_WINDOW,
        permitsPerSecond = 2.0,
        windowMs = 1000,
        message = "下单太频繁，请稍后再试"
    )
    public Result createOrder(@RequestBody Order order) {
        return Result.success(orderService.create(order));
    }
}
```

---

## 防重复提交切面

### 功能说明

防止用户快速重复提交表单，基于锁机制实现。

### 注解参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| key | String | "" | 锁键（SpEL 表达式） |
| expire | long | 3000 | 锁持有时间（毫秒） |
| waitTime | long | 0 | 等待时间（毫秒） |
| message | String | "请勿重复提交" | 提示信息 |
| timeUnit | TimeUnit | MILLISECONDS | 时间单位 |

### 使用示例

```java
@RestController
@RequestMapping("/order")
public class OrderController {

    /**
     * 防止重复下单
     */
    @PostMapping("/create")
    @PreventDuplicate(expire = 5000, message = "订单提交中，请勿重复点击")
    public Result createOrder(@RequestBody Order order) {
        return Result.success(orderService.create(order));
    }

    /**
     * 基于用户 ID 防重（不同用户可以同时提交）
     */
    @PostMapping("/payment")
    @PreventDuplicate(key = "#userId", expire = 10000)
    public Result payment(Long userId, @RequestBody Payment payment) {
        return Result.success(paymentService.process(payment));
    }

    /**
     * 基于订单号防重
     */
    @PostMapping("/refund")
    @PreventDuplicate(key = "#orderNo", expire = 3000)
    public Result refund(String orderNo) {
        return Result.success(refundService.process(orderNo));
    }
}
```

---

## 数据脱敏切面

### 功能说明

自动对带有 `@DataMask` 注解的字段进行脱敏处理，保护敏感信息。

### 注解参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| type | MaskType | PHONE | 脱敏类型 |
| showPrefixAndSuffix | boolean | true | 显示前缀和后缀 |
| prefixLength | int | 3 | 前缀保留长度 |
| suffixLength | int | 4 | 后缀保留长度 |

### 脱敏类型

| 类型 | 说明 | 示例 |
|------|------|------|
| PHONE | 手机号 | 138****1234 |
| ID_CARD | 身份证 | 110101********1234 |
| EMAIL | 邮箱 | tes****@example.com |
| BANK_CARD | 银行卡 | 6222 **** **** 1234 |
| ADDRESS | 地址 | 北京市******** |
| NAME | 姓名 | 张*三 |
| PASSWORD | 密码 | ****** |
| CUSTOM | 自定义 | 按配置脱敏 |

### 使用示例

```java
@Data
public class UserVO {

    private Long id;

    @DataMask(type = MaskType.NAME)
    private String userName;

    @DataMask(type = MaskType.PHONE)
    private String phone;

    @DataMask(type = MaskType.ID_CARD)
    private String idCard;

    @DataMask(type = MaskType.EMAIL)
    private String email;

    @DataMask(type = MaskType.BANK_CARD)
    private String bankCard;

    @DataMask(type = MaskType.ADDRESS)
    private String address;
}

@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 返回的用户信息会自动脱敏
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        UserVO user = userService.getVO();
        return Result.success(user);
        // 返回结果中：phone=138****1234, idCard=110101********1234
    }
}
```

---

## 数据字典翻译切面

### 功能说明

自动对带有 `@Dict` 注解的字段进行字典翻译。

### 注解参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| type | String | - | 字典类型（必填） |
| translate | boolean | true | 是否翻译 |
| defaultValue | String | "" | 默认值 |

### 使用示例

```java
@Data
public class UserVO {

    private Long id;
    private String userName;

    /**
     * 用户性别：1=男，2=女，0=未知
     * 自动翻译为：男/女/未知
     */
    @Dict(type = "user_sex")
    private String sex;

    /**
     * 用户状态：0=禁用，1=正常
     */
    @Dict(type = "sys_status")
    private String status;

    /**
     * 用户类型
     */
    @Dict(type = "user_type", defaultValue = "普通用户")
    private String userType;
}

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/list")
    public Result<List<UserVO>> getUserList() {
        List<UserVO> list = userService.getVOList();
        // 返回结果中：sex="男", status="正常"
        return Result.success(list);
    }
}
```

### 字典数据配置

字典数据需要在数据库中配置，参考表结构：

```sql
CREATE TABLE sys_dict_data (
    dict_id bigint PRIMARY KEY,
    dict_type varchar(100),
    dict_label varchar(100),
    dict_value varchar(100),
    list_order int,
    status char(1) DEFAULT '0'
);

-- 示例数据
INSERT INTO sys_dict_data VALUES (1, 'user_sex', '男', '1', 1, '0');
INSERT INTO sys_dict_data VALUES (2, 'user_sex', '女', '2', 2, '0');
INSERT INTO sys_dict_data VALUES (3, 'user_sex', '未知', '0', 3, '0');
```

---

## 切面执行顺序

| 顺序 | 切面 | 说明 |
|------|------|------|
| 1 | OperationLogAspect | 操作日志 |
| 2 | AsyncExecuteAspect | 异步执行 |
| 3 | CacheAspect | 缓存处理 |
| 4 | RateLimitAspect | 限流控制 |
| 5 | PreventDuplicateAspect | 防重复提交 |
| 6 | DataMaskAspect | 数据脱敏 |
| 7 | DataDictAspect | 字典翻译 |

---

## 注意事项

1. **性能考虑**：
   - 数据脱敏和字典翻译切面使用反射，对大数据量列表可能有性能影响
   - 建议仅对必要的 DTO/VO 使用脱敏和翻译

2. **循环依赖**：
   - 切面中避免注入可能产生循环依赖的服务
   - 必要时使用 `@Lazy` 注解

3. **事务边界**：
   - 异步执行切面不会继承调用方的事务
   - 缓存切面在事务提交后执行更合适

4. **生产环境配置**：
   - 缓存切面当前使用内存缓存，生产环境建议集成 Redis
   - 限流切面是单机限流，分布式场景需使用 Redis 限流
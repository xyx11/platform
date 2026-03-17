# 最佳实践

## 数据库设计

### 命名规范

```sql
-- 表名：小写，下划线分隔，加模块前缀
sys_user          -- 系统模块用户表
workflow_form     -- 工作流模块表单表

-- 字段名：小写，下划线分隔
user_name         -- 用户名
create_time       -- 创建时间

-- 主键：统一使用 id 或 {模块}_id
user_id           -- 用户 ID
role_id           -- 角色 ID

-- 索引：idx_字段名
idx_user_name     -- 用户名索引

-- 唯一索引：uk_字段名
uk_user_name      -- 用户名唯一索引
```

### 数据类型选择

| 场景 | 推荐类型 | 说明 |
|------|---------|------|
| 主键 | bigint | 自增或雪花算法 |
| 金额 | decimal(10,2) | 精确计算 |
| 状态 | tinyint | 0/1 或 1/2/3 |
| 时间 | datetime | 日期时间 |
| 大文本 | text/longtext | 内容存储 |
| 金额（分） | bigint | 分单位存储 |

### 索引优化

```sql
-- 创建复合索引（最左匹配）
CREATE INDEX idx_dept_status ON sys_user(dept_id, status);

-- 覆盖索引（避免回表）
CREATE INDEX idx_phone_id ON sys_user(phone, user_id);

-- 避免索引失效
WHERE status != 0        -- 不使用索引
WHERE name LIKE '%test%' -- 不使用索引
WHERE date(create_time)  -- 不使用索引
```

## 代码规范

### Entity 设计

```java
@Data
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long userId;

    @TableField("user_name")
    private String userName;

    @TableField("nick_name")
    private String nickName;

    // 不包含敏感字段
    // @TableField(exist = false)
    // private String password;
}
```

### DTO/VO 分离

```java
// 请求参数 DTO
@Data
public class UserAddParam {
    @NotBlank(message = "用户名不能为空")
    private String userName;

    @NotBlank(message = "密码不能为空")
    private String password;

    private Long deptId;
}

// 响应 VO
@Data
public class UserVO {
    private Long userId;
    private String userName;
    private String nickName;
    private DeptVO dept;
    private List<RoleVO> roles;
}
```

### Service 设计

```java
@Service
public class SysUserService {

    @Autowired
    private SysUserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserAddParam param) {
        // 1. 参数校验
        if (existsUserName(param.getUserName())) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 构建实体
        SysUser user = new SysUser();
        BeanUtils.copyProperties(param, user);
        user.setPassword(passwordEncoder.encode(param.getPassword()));

        // 3. 插入数据库
        userMapper.insert(user);
    }

    private boolean existsUserName(String userName) {
        return userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUserName, userName)) > 0;
    }
}
```

### Controller 设计

```java
@RestController
@RequestMapping("/system/user")
@Tag(name = "用户管理")
public class SysUserController {

    @Autowired
    private SysUserService userService;

    @GetMapping("/list")
    @Operation(summary = "查询用户列表")
    public Result<Page<UserVO>> list(UserParam param) {
        return Result.success(userService.pageUser(param));
    }

    @PostMapping
    @Operation(summary = "新增用户")
    public Result<Void> add(@RequestBody @Validated UserAddParam param) {
        userService.addUser(param);
        return Result.success();
    }
}
```

## 异常处理

### 自定义异常

```java
public class BusinessException extends RuntimeException {

    private Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public Integer getCode() {
        return code;
    }
}
```

### 全局异常处理器

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return Result.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统内部错误");
    }
}
```

## 缓存使用

### Redis 缓存

```java
@Service
public class SysDictService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 查询字典数据（带缓存）
     */
    public List<DictData> getDictData(String dictType) {
        String key = "dict:" + dictType;

        // 1. 查询缓存
        List<DictData> cached = (List<DictData>) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }

        // 2. 查询数据库
        List<DictData> data = dictMapper.selectDictData(dictType);

        // 3. 写入缓存（30 分钟过期）
        redisTemplate.opsForValue().set(key, data, 30, TimeUnit.MINUTES);

        return data;
    }

    /**
     * 删除缓存
     */
    public void clearDictCache(String dictType) {
        String key = "dict:" + dictType;
        redisTemplate.delete(key);
    }
}
```

### 缓存注解

```java
@Service
public class SysUserService {

    @Cacheable(value = "user", key = "#userId", unless = "#result == null")
    public UserVO getUserById(Long userId) {
        return userMapper.selectUserVOById(userId);
    }

    @CachePut(value = "user", key = "#user.userId")
    public UserVO updateUser(User user) {
        userMapper.updateById(user);
        return userMapper.selectUserVOById(user.getUserId());
    }

    @CacheEvict(value = "user", key = "#userId")
    public void deleteUser(Long userId) {
        userMapper.deleteById(userId);
    }
}
```

## 异步处理

### 异步配置

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

### 异步方法

```java
@Service
public class OperationLogService {

    @Autowired
    private OperationLogMapper logMapper;

    @Async("taskExecutor")
    public void saveLogAsync(OperationLog log) {
        // 异步保存操作日志
        logMapper.insert(log);
    }

    @Async("taskExecutor")
    public CompletableFuture<Boolean> sendEmailAsync(String to, String subject, String content) {
        // 异步发送邮件
        boolean success = emailService.send(to, subject, content);
        return CompletableFuture.completedFuture(success);
    }
}
```

## 事务管理

### 事务传播

```java
@Service
public class OrderService {

    /**
     * 必须在新事务中执行
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createOrder(Order order) {
        // 创建订单
        orderMapper.insert(order);
        // 可能抛出异常
    }

    /**
     * 支持当前事务，没有事务也能执行
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public void updateStock(Long productId, Integer quantity) {
        // 更新库存
    }
}
```

### 事务回滚

```java
@Service
public class UserService {

    /**
     * 默认只回滚 RuntimeException
     */
    @Transactional
    public void addUser(User user) {
        userMapper.insert(user);
        // 抛出 RuntimeException 会回滚
        // 抛出 Checked Exception 不会回滚
    }

    /**
     * 指定回滚所有异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void addUserWithRollback(User user) {
        userMapper.insert(user);
    }

    /**
     * 指定不回滚某些异常
     */
    @Transactional(noRollbackFor = {DataIntegrityViolationException.class})
    public void addUserNoRollback(User user) {
        userMapper.insert(user);
    }
}
```

## 安全实践

### 密码加密

```java
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

// 使用
String encoded = passwordEncoder.encode(rawPassword);
boolean matches = passwordEncoder.matches(rawPassword, encoded);
```

### 权限校验

```java
@PreAuthorize("@ss.hasPermission('system:user:add')")
@PostMapping
public Result<Void> add(@RequestBody User user) {
    userService.add(user);
    return Result.success();
}

@PreAuthorize("@ss.hasRole('admin')")
@DeleteMapping("/{ids}")
public Result<Void> delete(@PathVariable Long[] ids) {
    userService.deleteByIds(ids);
    return Result.success();
}
```

### XSS 防护

```java
@JsonDeserialize(using = XssStringDeserializer.class)
public class XssStringDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        return XssUtils.escape(p.getValueAsString());
    }
}
```

## 性能优化

### 批量操作

```java
// 批量插入
public void batchInsert(List<User> users) {
    if (CollectionUtils.isEmpty(users)) return;

    // 分批处理，每批 1000 条
    List<List<User>> partitions = Lists.partition(users, 1000);
    for (List<User> partition : partitions) {
        userMapper.batchInsert(partition);
    }
}

// Mapper XML
<insert id="batchInsert">
    INSERT INTO sys_user (user_name, nick_name) VALUES
    <foreach collection="list" item="user" separator=",">
        (#{user.userName}, #{user.nickName})
    </foreach>
</insert>
```

### 分页查询优化

```java
// 避免深分页
// 不推荐：LIMIT 1000000, 10
// 推荐：使用游标分页
public List<User> pageWithCursor(Long lastId, int size) {
    return userMapper.selectLastId(lastId, size);
}

// Mapper XML
<select id="selectLastId">
    SELECT * FROM sys_user
    WHERE user_id > #{lastId}
    ORDER BY user_id
    LIMIT #{size}
</select>
```

### 连接池配置

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      pool-name: MyHikariCP
      max-lifetime: 600000
      connection-timeout: 30000
```

## 日志规范

### 日志级别使用

```java
@Slf4j
public class UserService {

    public User getById(Long id) {
        // TRACE: 详细追踪信息
        log.trace("查询用户，id={}", id);

        // DEBUG: 调试信息
        log.debug("从数据库查询用户");

        // INFO: 一般信息
        log.info("查询用户成功，id={}, name={}", id, user.getName());

        // WARN: 警告信息
        if (user == null) {
            log.warn("用户不存在，id={}", id);
        }

        // ERROR: 错误信息
        try {
            // 业务逻辑
        } catch (Exception e) {
            log.error("查询用户失败，id={}", id, e);
        }
    }
}
```

### 日志脱敏

```java
public class SensitivePatternConverter extends RegexConverter {
    @Override
    protected String format(String input) {
        // 手机号脱敏
        if (input.matches("1[3-9]\\d{9}")) {
            return input.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        }
        // 身份证脱敏
        if (input.matches("\\d{17}[\\dxX]")) {
            return input.replaceAll("(\\d{6})\\d{8}(\\w{4})", "$1********$2");
        }
        return input;
    }
}
```
## 工具类使用

### StringUtils 字符串工具

```java
// 空值检查
StringUtils.isBlank("  ")  // true
StringUtils.isNotEmpty("hello")  // true

// 驼峰与下划线转换
StringUtils.camelToUnderline("userName")  // user_name
StringUtils.underlineToCamel("user_name")  // userName
StringUtils.underlineToPascal("user_name")  // UserName

// 字符串截取
StringUtils.left("hello world", 5)  // hello
StringUtils.right("hello world", 5)  // world
StringUtils.truncate("hello world", 8)  // hello...

// 类型转换
StringUtils.toInt("123", 0)  // 123
StringUtils.toDouble("3.14", 0.0)  // 3.14
StringUtils.toBoolean("true")  // true

// 其他常用方法
StringUtils.repeat("*", 5)  // *****
StringUtils.padLeft("123", 5, '0')  // 00123
StringUtils.join(Arrays.asList("a", "b", "c"), ",")  // a,b,c
StringUtils.randomString(10)  // 随机字符串
```

### Arith 数学工具

```java
// 精确浮点运算（避免精度丢失）
Arith.add(0.1, 0.2)  // 0.3
Arith.sub(1.0, 0.1)  // 0.9
Arith.mul(3.14, 2, 4)  // 6.2800
Arith.div(1.0, 3, 4)  // 0.3333

// 四舍五入
Arith.round(3.14159, 2)  // 3.14

// 百分比计算
Arith.percentage(50, 200)  // 25.0

// 累加和平均值
Arith.sum(1.1, 2.2, 3.3)  // 6.6
Arith.average(1.0, 2.0, 3.0, 4.0)  // 2.5

// 金额格式化
Arith.formatMoney(123.456)  // 123.46
```

### JsonUtils JSON 工具

```java
// 对象转 JSON
String json = JsonUtils.toJson(user);
String prettyJson = JsonUtils.toPrettyJson(user);

// JSON 转对象
User user = JsonUtils.fromJson(json, User.class);
List<User> users = JsonUtils.fromJson(json, new TypeReference<List<User>>() {});

// JSON 解析
JsonNode node = JsonUtils.readTree(json);
String name = JsonUtils.getString(node, "name");
Integer age = JsonUtils.getInt(node, "age");

// 创建 JSON
ObjectNode obj = JsonUtils.createObjectNode();
obj.put("name", "张三");
obj.put("age", 25);

// JSON 验证
JsonUtils.isValidJson(json)  // true
JsonUtils.isJsonObject(json)  // true
JsonUtils.isJsonArray(json)  // false
```

### FileUtils 文件工具

```java
// 文件大小格式化
FileUtils.formatFileSize(1048576)  // "1 MB"

// 文件操作
FileUtils.readFile("/path/to/file.txt")  // 读取文件内容
FileUtils.writeFile("/path/to/file.txt", "content")  // 写入文件
FileUtils.copyFile("/src/file.txt", "/dest/file.txt")  // 复制文件
FileUtils.deleteFile("/path/to/file.txt")  // 删除文件

// 文件信息
FileUtils.getExtension("file.txt")  // "txt"
FileUtils.getFileNameWithoutExtension("file.txt")  // "file"
FileUtils.isTextFile("file.txt")  // true
```

### DateUtils 日期工具

```java
// 获取当前时间
LocalDateTime now = DateUtils.now()
LocalDate today = DateUtils.today()

// 格式化
String formatted = DateUtils.format(LocalDateTime.now())  // "2024-01-01 12:00:00"
String dateOnly = DateUtils.formatDate(LocalDate.now())  // "2024-01-01"

// 日期计算
DateUtils.addDays(LocalDateTime.now(), 7)  // 7 天后
DateUtils.addMonths(LocalDateTime.now(), 1)  // 1 个月后
DateUtils.daysBetween(startDate, endDate)  // 相差天数

// 边界时间
DateUtils.firstDayOfMonth()  // 当月第一天
DateUtils.startOfDay(LocalDate.now())  // 当天开始时间
DateUtils.endOfDay(LocalDate.now())  // 当天结束时间

// 时间判断
DateUtils.isToday(someDate)  // 是否今天
DateUtils.isThisWeek(someDate)  // 是否本周
DateUtils.isThisMonth(someDate)  // 是否本月

// 相对时间描述
DateUtils.getRelativeTime(LocalDateTime.now().minusHours(2))  // "2 小时前"
```

### ValidationUtils 校验工具

```java
// 基础验证
ValidationUtils.isValidPhone("13800138000")  // true
ValidationUtils.isValidEmail("test@example.com")  // true
ValidationUtils.isValidIdCard("110101199001011234")  // true

// 密码验证
ValidationUtils.isValidPassword("Abc123456")  // true（字母 + 数字，6-20 位）

// 其他验证
ValidationUtils.isValidUrl("https://example.com")  // true
ValidationUtils.isValidIp("192.168.1.1")  // true
ValidationUtils.containsChinese("你好")  // true
ValidationUtils.isNumeric("123.45")  // true

// 银行卡验证（Luhn 算法）
ValidationUtils.isValidBankCard("6222021234567890123")  // true/false

// IP 网段验证
ValidationUtils.isIpInRange("192.168.1.100", "192.168.1.0/24")  // true
```

### CryptoUtils 加密工具

```java
// 哈希加密
String md5 = CryptoUtils.md5("password")  // MD5 加密
String sha256 = CryptoUtils.sha256("password")  // SHA-256 加密
String sha512 = CryptoUtils.sha512("password")  // SHA-512 加密

// HMAC 加密
String hmac = CryptoUtils.hmacSHA256("data", "secret")

// Base64
String encoded = CryptoUtils.base64Encode("data")
String decoded = CryptoUtils.base64Decode(encoded)

// UUID 生成
String uuid = CryptoUtils.generateUUID()  // 550e8400-e29b-41d4-a716-446655440000
String shortUuid = CryptoUtils.generateShortUuid()  // 32 位短 UUID

// 盐值生成
String salt = CryptoUtils.generateSalt(16)  // 16 字节随机盐
```

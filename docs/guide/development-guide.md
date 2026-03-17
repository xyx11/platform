# 微服务平台开发指南

本文档提供微服务平台的一站式开发指南，包括常用注解、工具类和 AOP 切面的使用。

## 快速索引

### 注解系统

| 注解 | 位置 | 说明 |
|------|------|------|
| @OperationLog | `common.core.annotation` | 操作日志记录 |
| @AsyncExecute | `common.core.annotation` | 异步执行 |
| @Cacheable | `common.core.annotation` | 方法缓存 |
| @CacheEvict | `common.core.annotation` | 缓存删除 |
| @RateLimit | `common.core.annotation` | 限流控制 |
| @PreventDuplicate | `common.core.annotation` | 防重复提交 |
| @DataMask | `common.core.annotation` | 数据脱敏 |
| @Dict | `common.core.annotation` | 字典翻译 |
| @ExcelColumn | `common.core.annotation` | Excel 列定义 |
| @DataScope | `common.core.annotation` | 数据权限 |

### 工具类

| 工具类 | 位置 | 说明 |
|--------|------|------|
| IdGenerator | `common.core.util` | 分布式 ID 生成器 |
| CollectionUtils | `common.core.util` | 集合操作工具 |
| HttpUtils | `common.core.util` | HTTP 客户端 |
| ExcelUtils | `common.core.util` | Excel 导入导出 |
| DictUtils | `common.core.util` | 字典工具 |
| DateUtils | `common.core.util` | 日期时间工具 |
| SpElUtils | `common.core.util` | SpEL 表达式工具 |
| RateLimiterUtils | `common.core.util` | 限流器工具 |
| ValidationUtils | `system.utils` | 验证工具 |
| CryptoUtils | `system.utils` | 加密工具 |
| FileUtils | `system.utils` | 文件工具 |

### AOP 切面

| 切面 | 优先级 | 说明 |
|------|--------|------|
| OperationLogAspect | 1 | 操作日志记录 |
| AsyncExecuteAspect | 2 | 异步执行 |
| CacheAspect | 3 | 缓存管理 |
| RateLimitAspect | 4 | 限流控制 |
| PreventDuplicateAspect | 5 | 防重复提交 |
| DataMaskAspect | 6 | 数据脱敏 |
| DataDictAspect | 7 | 字典翻译 |

## 典型使用场景

### 1. 新增用户接口（完整示例）

```java
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 新增用户
     * - 记录操作日志
     * - 防止重复提交
     * - 清空用户缓存
     */
    @PostMapping("/add")
    @OperationLog(module = "用户管理", type = OperationType.CREATE,
                  description = "新增用户：#user.userName")
    @PreventDuplicate(key = "#user.userName", expire = 3000,
                      message = "请勿重复提交")
    @CacheEvict(value = "userCache", allEntries = true)
    public Result<UserVO> addUser(@RequestBody @Validated UserAddDTO user) {
        User saved = userService.add(user);
        return Result.success(convertToVO(saved));
    }

    /**
     * 查询用户详情
     * - 使用缓存
     * - 数据脱敏
     */
    @GetMapping("/{id}")
    @Cacheable(value = "userCache", key = "#id", expire = 300)
    public Result<UserVO> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(convertToVO(user));
    }

    /**
     * 用户列表（带限流）
     */
    @GetMapping("/list")
    @RateLimit(permitsPerSecond = 20.0, algorithm = Algorithm.TOKEN_BUCKET)
    public Result<PageResult<UserVO>> getList(PageQuery query) {
        return Result.success(userService.getPage(query));
    }
}
```

### 2. 数据传输对象（DTO/VO）

```java
/**
 * 用户 VO（视图对象）
 */
@Data
public class UserVO {

    private Long id;

    /**
     * 姓名 - 脱敏显示
     */
    @DataMask(type = MaskType.NAME)
    private String userName;

    /**
     * 手机号 - 脱敏显示
     */
    @DataMask(type = MaskType.PHONE)
    private String phone;

    /**
     * 邮箱 - 脱敏显示
     */
    @DataMask(type = MaskType.EMAIL)
    private String email;

    /**
     * 性别 - 字典翻译
     */
    @Dict(type = "user_sex")
    private String sex;

    /**
     * 状态 - 字典翻译
     */
    @Dict(type = "sys_status")
    private String status;

    /**
     * 创建时间
     */
    private Date createTime;
}

/**
 * 用户导出 VO
 */
@Data
public class UserExportVO {

    @ExcelColumn(header = "用户 ID", width = 15)
    private Long id;

    @ExcelColumn(header = "用户名", width = 20)
    private String userName;

    @ExcelColumn(header = "手机号", width = 18)
    private String phone;

    @ExcelColumn(header = "邮箱", width = 25)
    private String email;

    @ExcelColumn(header = "性别", width = 10)
    @Dict(type = "user_sex")
    private String sex;

    @ExcelColumn(header = "创建时间", width = 20, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
```

### 3. 服务层异步处理

```java
@Service
public class NotificationService {

    @Autowired
    private MessageService messageService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    /**
     * 发送站内信（异步）
     */
    @AsyncExecute
    public void send站内信(Long userId, String title, String content) {
        messageService.send(userId, title, content);
    }

    /**
     * 发送邮件通知（异步，带重试）
     */
    @AsyncExecute(retries = 3, retryDelay = 2000)
    public void sendEmailNotification(Long userId, String subject, String content) {
        User user = userService.getById(userId);
        emailService.send(user.getEmail(), subject, content);
    }

    /**
     * 发送短信（异步，带超时）
     */
    @AsyncExecute(timeout = 10000)
    public void sendSmsNotification(Long userId, String message) {
        User user = userService.getById(userId);
        smsService.send(user.getPhone(), message);
    }
}
```

### 4. 批量数据处理

```java
@Service
public class DataImportService {

    /**
     * 批量导入用户
     * - 分块处理（每批 100 条）
     * - 带进度日志
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(module = "数据导入", type = OperationType.IMPORT)
    public void importUsers(MultipartFile file) throws Exception {
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        List<UserImportDTO> dataList = ExcelUtils.importExcel(workbook, UserImportDTO.class);

        // 分块处理
        List<List<UserImportDTO>> chunks = CollectionUtils.chunk(dataList, 100);
        int processed = 0;

        for (List<UserImportDTO> chunk : chunks) {
            for (UserImportDTO dto : chunk) {
                // 验证数据
                ValidationUtils.checkPhone(dto.getPhone());
                ValidationUtils.checkEmail(dto.getEmail());

                // 保存数据
                userService.add(convertToEntity(dto));
                processed++;
            }
            log.info("已处理 {}/{} 条记录", processed, dataList.size());
        }
    }

    /**
     * 导出用户数据
     */
    public void exportUsers(HttpServletResponse response, UserQuery query) throws IOException {
        List<UserExportVO> list = userService.exportList(query);

        // 使用工具类导出
        ExcelUtils.exportExcel(list, UserExportVO.class, response, "用户数据导出");
    }
}
```

### 5. 分布式锁使用

```java
@Service
public class OrderService {

    @Autowired
    private RedisDistributedLock redisLock;

    /**
     * 创建订单（分布式锁防重）
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Order> createOrder(Long userId, OrderCreateDTO dto) {
        String lockKey = "lock:order:create:" + userId;

        return redisLock.executeWithLock(lockKey, 30, () -> {
            // 检查是否存在未支付订单
            long pendingCount = orderMapper.countPendingOrders(userId);
            if (pendingCount >= 5) {
                throw new BusinessException("您有 5 个未支付订单，请先完成支付");
            }

            // 创建订单
            Order order = new Order();
            order.setUserId(userId);
            // ... 设置其他字段
            orderMapper.insert(order);

            return order;
        });
    }
}
```

### 6. 限流配置

```java
@RestController
@RequestMapping("/api")
public class ApiController {

    /**
     * 公开 API - 严格限流
     */
    @GetMapping("/public/data")
    @RateLimit(permitsPerSecond = 5.0, algorithm = Algorithm.SLIDING_WINDOW,
               message = "访问过于频繁，请稍后再试")
    public Result getData() {
        return Result.success(dataService.getPublicData());
    }

    /**
     * 用户专属 API - 按用户限流
     */
    @GetMapping("/user/profile")
    @RateLimit(key = "#userId", permitsPerSecond = 10.0)
    public Result getProfile(Long userId) {
        return Result.success(userService.getProfile(userId));
    }

    /**
     * 管理 API - 宽松限流
     */
    @AdminAccess
    @GetMapping("/admin/stats")
    @RateLimit(permitsPerSecond = 50.0, algorithm = Algorithm.TOKEN_BUCKET)
    public Result getStats() {
        return Result.success(statsService.getOverview());
    }
}
```

## 最佳实践

### 1. 注解组合使用

```java
@PostMapping("/update")
@OperationLog(module = "用户管理", type = OperationType.UPDATE)
@PreventDuplicate(key = "#user.id", expire = 2000)
@CacheEvict(value = "userCache", key = "#user.id")
@Transactional(rollbackFor = Exception.class)
public Result updateUser(@RequestBody User user) {
    userService.update(user);
    return Result.success();
}
```

### 2. 工具类链式调用

```java
// 集合操作链式调用
List<String> userIds = CollectionUtils.distinctBy(users, User::getId)
    .stream()
    .map(User::getId)
    .map(String::valueOf)
    .collect(Collectors.toList());

// 数学计算
BigDecimal total = Arith.sum(
    Arith.mul(price, quantity),
    Arith.mul(taxRate, price),
    shippingFee
);

// 字符串处理
String camelCase = StringUtils.underlineToCamel("user_name");
```

### 3. 异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail("系统繁忙，请稍后再试");
    }
}
```

## 性能优化建议

1. **缓存策略**
   - 热点数据使用 @Cacheable
   - 设置合理的过期时间
   - 写操作后及时 @CacheEvict

2. **异步处理**
   - 耗时操作使用 @AsyncExecute
   - 设置合理的超时和重试
   - 避免在异步方法中使用事务

3. **限流降级**
   - 公开 API 必须配置限流
   - 根据业务重要性设置不同限流值
   - 使用滑动窗口算法更精确

4. **批量处理**
   - 大数据量使用 CollectionUtils.chunk 分块
   - 批量操作使用事务
   - 记录处理进度

## 相关文档

- [AOP 切面使用指南](./aop-aspects.md)
- [常用注解文档](./annotations.md)
- [工具类参考](./utils-reference.md)
- [最佳实践](./best-practices.md)

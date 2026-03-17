# 常用注解文档

本模块提供了丰富的注解支持，简化开发工作。

## 目录

- [操作日志注解](#操作日志注解)
- [异步执行注解](#异步执行注解)
- [缓存注解](#缓存注解)
- [限流注解](#限流注解)
- [防重复提交注解](#防重复提交注解)
- [数据脱敏注解](#数据脱敏注解)
- [数据字典注解](#数据数据字典注解)
- [Excel 列注解](#excel 列注解)
- [数据权限注解](#数据权限注解)

---

## 操作日志注解 `@OperationLog`

### 位置

`com.micro.platform.common.core.annotation.OperationLog`

### 用途

记录用户操作日志，自动捕获操作模块、类型、描述、操作人、IP 等信息。

### 属性

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {
    String module() default "";           // 操作模块
    OperationType type() default OTHER;   // 操作类型
    String description() default "";      // 操作描述（支持 SpEL）
    boolean recordParams() default true;  // 记录参数
    boolean recordResult() default false; // 记录结果
    boolean recordStatus() default true;  // 记录状态
    String operator() default "";         // 操作人（SpEL）
}
```

### 操作类型

```java
public enum OperationType {
    CREATE("新增"), UPDATE("修改"), DELETE("删除"),
    QUERY("查询"), EXPORT("导出"), IMPORT("导入"),
    GRANT("授权"), REVOKE("撤销授权"),
    LOGIN("登录"), LOGOUT("登出"),
    OTHER("其他")
}
```

### 示例

```java
@PostMapping("/add")
@OperationLog(module = "用户管理", type = OperationType.CREATE,
              description = "新增用户：#user.userName")
public Result addUser(@RequestBody User user) {
    return Result.success(userService.add(user));
}
```

---

## 异步执行注解 `@AsyncExecute`

### 位置

`com.micro.platform.common.core.annotation.AsyncExecute`

### 用途

标记需要异步执行的方法，适用于不需要立即返回结果的操作。

### 属性

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AsyncExecute {
    String executor() default "taskExecutor";  // 线程池名称
    long timeout() default 0;                   // 超时时间（毫秒）
    int retries() default 0;                    // 重试次数
    long retryDelay() default 1000;             // 重试延迟（毫秒）
    boolean recordException() default true;     // 记录异常
}
```

### 示例

```java
@AsyncExecute(retries = 3, retryDelay = 1000)
public void sendNotification(Long userId, String message) {
    // 发送通知
}
```

---

## 缓存注解

### `@Cacheable`

#### 位置

`com.micro.platform.common.core.annotation.Cache`

#### 用途

缓存方法返回值。

#### 属性

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {
    String value() default "";     // 缓存名称（前缀）
    String key() default "";       // 缓存键（SpEL）
    long expire() default 3600;    // 过期时间（秒）
}
```

#### 示例

```java
@Cacheable(value = "users", key = "#userId", expire = 300)
public User getUserById(Long userId) {
    return userMapper.selectById(userId);
}
```

### `@CacheEvict`

#### 位置

`com.micro.platform.common.core.annotation.CacheEvict`

#### 用途

删除缓存。

#### 属性

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEvict {
    String value() default "";       // 缓存名称（前缀）
    String key() default "";         // 缓存键（SpEL）
    boolean allEntries() default false; // 删除所有条目
}
```

#### 示例

```java
@CacheEvict(value = "users", key = "#user.id")
public void updateUser(User user) {
    userMapper.updateById(user);
}
```

---

## 限流注解 `@RateLimit`

### 位置

`com.micro.platform.common.core.annotation.RateLimit`

### 用途

限制方法调用频率，支持多种限流算法。

### 属性

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    String key() default "";                    // 限流键（SpEL）
    double permitsPerSecond() default 10.0;     // 每秒允许请求数
    Algorithm algorithm() default TOKEN_BUCKET; // 限流算法
    long windowMs() default 1000;               // 窗口大小（毫秒）
    boolean failFast() default true;            // 快速失败
    String message() default "请求过于频繁";     // 错误提示
}

enum Algorithm {
    TOKEN_BUCKET,     // 令牌桶
    FIXED_WINDOW,     // 固定窗口
    SLIDING_WINDOW    // 滑动窗口
}
```

### 示例

```java
@GetMapping("/data")
@RateLimit(permitsPerSecond = 10.0)
public Result getData() {
    return Result.success(dataService.getData());
}

@PostMapping("/submit")
@RateLimit(key = "#userId", permitsPerSecond = 5.0,
           algorithm = Algorithm.FIXED_WINDOW)
public Result submit(@RequestBody Form form) {
    return Result.success(formService.submit(form));
}
```

---

## 防重复提交注解 `@PreventDuplicate`

### 位置

`com.micro.platform.common.core.annotation.PreventDuplicate`

### 用途

防止用户快速重复提交表单。

### 属性

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreventDuplicate {
    String key() default "";              // 锁键（SpEL）
    long expire() default 3000;           // 锁持有时间（毫秒）
    long waitTime() default 0;            // 等待时间（毫秒）
    String message() default "请勿重复提交"; // 提示信息
    TimeUnit timeUnit() default MILLISECONDS; // 时间单位
}
```

### 示例

```java
@PostMapping("/create")
@PreventDuplicate(expire = 5000, message = "订单提交中，请勿重复点击")
public Result createOrder(@RequestBody Order order) {
    return Result.success(orderService.create(order));
}
```

---

## 数据脱敏注解 `@DataMask`

### 位置

`com.micro.platform.common.core.annotation.DataMask`

### 用途

标记需要数据脱敏的字段。

### 属性

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataMask {
    MaskType type() default PHONE;         // 脱敏类型
    boolean showPrefixAndSuffix() default true; // 显示前后缀
    int prefixLength() default 3;          // 前缀长度
    int suffixLength() default 4;          // 后缀长度
}

enum MaskType {
    PHONE,       // 手机号
    ID_CARD,     // 身份证
    EMAIL,       // 邮箱
    BANK_CARD,   // 银行卡
    ADDRESS,     // 地址
    NAME,        // 姓名
    PASSWORD,    // 密码
    CUSTOM       // 自定义
}
```

### 示例

```java
@Data
public class UserVO {
    private Long id;

    @DataMask(type = MaskType.NAME)
    private String userName;  // 张*三

    @DataMask(type = MaskType.PHONE)
    private String phone;     // 138****1234

    @DataMask(type = MaskType.ID_CARD)
    private String idCard;    // 110101********1234
}
```

---

## 数据字典注解 `@Dict`

### 位置

`com.micro.platform.common.core.annotation.Dict`

### 用途

标记需要字典翻译的字段。

### 属性

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Dict {
    String type();              // 字典类型（必填）
    boolean translate() default true;  // 是否翻译
    String defaultValue() default "";  // 默认值
}
```

### 示例

```java
@Data
public class UserVO {
    @Dict(type = "user_sex")
    private String sex;  // 1 -> "男"

    @Dict(type = "sys_status", defaultValue = "未知")
    private String status;  // 0 -> "正常"
}
```

---

## Excel 列注解 `@ExcelColumn`

### 位置

`com.micro.platform.common.core.annotation.ExcelColumn`

### 用途

标记 Excel 导入/导出的列。

### 属性

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumn {
    String header() default "";     // 列头
    int width() default 20;         // 列宽
    int order() default 0;          // 排序
    String pattern() default "";    // 日期格式
    String defaultValue() default ""; // 默认值
}
```

### 示例

```java
@Data
public class UserExport {
    @ExcelColumn(header = "用户 ID", width = 15)
    private Long id;

    @ExcelColumn(header = "用户名", width = 20)
    private String userName;

    @ExcelColumn(header = "手机号", width = 18)
    private String phone;

    @ExcelColumn(header = "创建时间", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}

// 导出
ExcelUtils.exportExcel(userList, UserExport.class, response, "用户列表");
```

---

## 数据权限注解 `@DataScope`

### 位置

`com.micro.platform.common.core.annotation.DataScope`

### 用途

标记需要数据权限控制的方法。

### 属性

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {
    String tableAlias() default "";  // 表别名
    String deptField() default "";   // 部门字段
    String userField() default "";   // 用户字段
}
```

### 示例

```java
@DataScope(tableAlias = "u", deptField = "dept_id", userField = "user_id")
public List<User> getUserList(UserQuery query) {
    return userMapper.selectList(query);
}
```

---

## 注解组合使用

多个注解可以组合使用，实现复杂功能：

```java
@PostMapping("/add")
@OperationLog(module = "用户管理", type = OperationType.CREATE)
@CacheEvict(value = "users", allEntries = true)
@PreventDuplicate(key = "#user.userName", expire = 5000)
public Result addUser(@RequestBody User user) {
    userService.add(user);
    return Result.success();
}
```

---

## 注意事项

1. **SpEL 表达式**：支持 `#参数名`、`#参数名。属性` 等方式访问参数
2. **注解顺序**：多个注解同时使用时，注意执行顺序
3. **性能影响**：部分注解（如脱敏、字典翻译）使用反射，大量数据时注意性能
4. **事务边界**：异步注解不会继承调用方事务
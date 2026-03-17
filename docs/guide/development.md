# 开发指南

## 项目结构

```
micro-platform/
├── mp-gateway/              # 网关服务 (8080)
│   ├── src/main/java/
│   │   └── com/micro/platform/gateway/
│   │       └── MpGatewayApplication.java
│   └── src/main/resources/
│       ├── application.yml
│       └── application-local.yml
├── mp-auth/                 # 认证服务 (8081)
│   ├── src/main/java/
│   │   └── com/micro/platform/auth/
│   │       └── MpAuthApplication.java
│   └── src/main/resources/
│       └── application.yml
├── mp-system/               # 系统服务 (8082)
│   ├── src/main/java/
│   │   └── com/micro/platform/system/
│   │       ├── controller/  # 控制器
│   │       ├── service/     # 服务层
│   │       ├── mapper/      # 数据访问层
│   │       ├── entity/      # 实体类
│   │       └── domain/      # 领域模型
│   └── src/main/resources/
│       └── application.yml
├── mp-generator/            # 代码生成 (8083)
├── mp-job/                  # 定时任务 (8084)
├── mp-vue/                  # 前端项目 (3001)
│   ├── src/
│   │   ├── api/             # API 接口
│   │   ├── assets/          # 静态资源
│   │   ├── components/      # 公共组件
│   │   ├── directives/      # 自定义指令
│   │   ├── layout/          # 布局组件
│   │   ├── router/          # 路由配置
│   │   ├── store/           # 状态管理
│   │   ├── utils/           # 工具函数
│   │   └── views/           # 页面组件
│   └── package.json
├── common/                  # 公共模块
│   ├── common-core/         # 核心工具
│   ├── common-log/          # 日志组件
│   ├── common-security/     # 安全组件
│   └── common-swagger/      # Swagger 配置
└── docs/                    # 文档站点 (5173)
```

## 环境搭建

### 1. 克隆项目

```bash
git clone https://github.com/xyx11/platform.git
cd micro-platform
```

### 2. 配置 IDE

**IntelliJ IDEA:**
1. 打开项目
2. File -> Settings -> Build -> Compiler -> Annotation Processors
3. 勾选 "Enable annotation processing"
4. Maven -> Reload All Maven Projects

**VS Code:**
1. 安装扩展：
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Volar (Vue 3)

### 3. 导入数据库

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE micro_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

# 导入 SQL
mysql -u root -p micro_platform < mp-system/src/main/resources/sql/ry_20240101.sql
```

### 4. 配置 Nacos

编辑 `mp-system/src/main/resources/application-local.yml`:

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        username: nacos
        password: nacos
      config:
        import-check:
          enabled: false
```

### 5. 启动服务

```bash
# 使用启动脚本
./start.sh

# 或单独启动
cd mp-gateway && mvn spring-boot:run
```

## 开发规范

### 命名规范

**类命名：**
- 控制器：`XxxController`
- 服务接口：`XxxService`
- 服务实现：`XxxServiceImpl`
- 实体类：`Xxx` 或 `XxxEntity`
- DTO：`XxxDTO`
- VO: `XxxVO`
- 参数对象：`XxxParam`

**方法命名：**
- 查询：`getXxx`, `listXxx`, `pageXxx`
- 新增：`addXxx`, `createXxx`, `insertXxx`
- 修改：`updateXxx`, `editXxx`
- 删除：`deleteXxx`, `removeXxx`

### 代码风格

**Controller 示例：**

```java
@RestController
@RequestMapping("/system/user")
@Tag(name = "用户管理")
public class SysUserController {

    @Autowired
    private SysUserService userService;

    @GetMapping("/list")
    @Operation(summary = "查询用户列表")
    public Result<Page<SysUser>> list(SysUserParam param) {
        return Result.success(userService.pageXxx(param));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public Result<SysUserVO> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增用户")
    public Result<Void> add(@RequestBody @Validated SysUserAddParam param) {
        userService.add(param);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改用户")
    public Result<Void> update(@RequestBody @Validated SysUserUpdateParam param) {
        userService.update(param);
        return Result.success();
    }

    @DeleteMapping("/{ids}")
    @Operation(summary = "删除用户")
    public Result<Void> delete(@PathVariable Long[] ids) {
        userService.deleteByIds(ids);
        return Result.success();
    }
}
```

**Service 示例：**

```java
@Service
public class SysUserService {

    @Autowired
    private SysUserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public void add(SysUserAddParam param) {
        // 检查用户名是否存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUserName, param.getUserName()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 构建实体
        SysUser user = new SysUser();
        BeanUtils.copyProperties(param, user);
        user.setPassword(passwordEncoder.encode(param.getPassword()));

        // 插入数据库
        userMapper.insert(user);
    }
}
```

### 注释规范

**类注释：**

```java
/**
 * 用户管理 Controller
 *
 * @author admin
 * @since 2024-01-01
 */
```

**方法注释：**

```java
/**
 * 查询用户列表
 *
 * @param param 查询参数
 * @return 用户列表分页数据
 */
```

### Git 提交规范

```
feat: 新功能
fix: 修复 Bug
docs: 文档更新
style: 代码格式调整
refactor: 重构代码
test: 测试相关
chore: 构建/工具链相关
perf: 性能优化
ci: CI 配置
revert: 回退提交
```

**示例：**

```bash
git commit -m "feat: 添加用户导出功能"
git commit -m "fix: 修复用户查询分页问题"
git commit -m "docs: 更新 API 文档"
```

## 调试技巧

### 1. 日志配置

```yaml
logging:
  level:
    root: INFO
    com.micro.platform.system: DEBUG
    com.micro.platform.system.mapper: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 2. 断点调试

在 IDEA 中：
1. 在代码行号旁点击设置断点
2. 右键 -> Debug 'MpSystemApplication'
3. 使用 F7(步入), F8(步过), F9(恢复)

### 3. 接口测试

**使用 Swagger:**
1. 访问 http://localhost:8082/doc.html
2. 找到对应接口
3. 点击 "Try it out"
4. 填写参数
5. 点击 "Execute"

**使用 Postman/curl:**

```bash
# 登录获取 Token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 使用 Token 请求
curl -X GET http://localhost:8080/system/user/list \
  -H "Authorization: Bearer <token>"
```

## 常见问题

### 1. 启动报错

**问题：** `Port 8080 was already in use`

**解决：**
```bash
lsof -i :8080
kill -9 <pid>
```

### 2. 数据库连接失败

**问题：** `Communications link failure`

**解决：**
- 检查 MySQL 是否启动
- 检查连接配置
- 检查防火墙设置

### 3. Nacos 配置不生效

**问题：** 修改配置后未生效

**解决：**
- 清除本地缓存：`rm -rf ~/nacos/data/`
- 重启 Nacos
- 检查配置格式

### 4. 前端跨域问题

**问题：** `Access-Control-Allow-Origin`

**解决：** 网关已配置跨域，确保请求经过网关

## 最佳实践

### 1. 性能优化

- 使用 Redis 缓存热点数据
- 分页查询避免全表扫描
- 使用异步处理耗时操作
- SQL 添加必要索引

### 2. 安全建议

- 密码加密存储
- 接口权限校验
- SQL 注入防护
- XSS 攻击防护

### 3. 代码质量

- 使用 Lombok 简化代码
- 使用 Optional 避免空指针
- 使用 Stream API 简化集合操作
- 合理划分包和模块

## 扩展开发

### 1. 添加新模块

```bash
# 复制现有模块结构
cp -r mp-system mp-new-module

# 修改 pom.xml
# 修改 application.yml
# 修改包名
```

### 2. 自定义注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    String deptAlias() default "";
    String userAlias() default "";
}
```

### 3. 自定义拦截器

```java
@Component
public class DemoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) {
        // 处理逻辑
        return true;
    }
}
```

## 参考资源

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Vue 3 官方文档](https://vuejs.org/)
- [Element Plus 文档](https://element-plus.org/)
- [MyBatis-Plus 文档](https://baomidou.com/)
- [Flowable 文档](https://www.flowable.org/docs/)
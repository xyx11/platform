# 项目总结

## 技术架构

### 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.5 | 核心框架 |
| Spring Cloud Alibaba | 2022.0.0.0 | 微服务套件 |
| Nacos | 2.2.x | 服务注册与配置中心 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| Flowable | 6.x | 工作流引擎 |
| Redis | 7.x | 缓存 |
| MySQL | 8.0+ | 数据库 |
| Spring Security | 6.x | 安全认证 |
| JWT | - | Token 认证 |
| Swagger/Knife4j | - | API 文档 |

### 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4.x | 渐进式框架 |
| Element Plus | 2.5.x | UI 组件库 |
| Vite | 5.x | 构建工具 |
| Vue Router | 4.x | 路由管理 |
| Pinia | 2.x | 状态管理 |
| Axios | 1.x | HTTP 客户端 |
| bpmn.js | - | BPMN 流程图 |
| ECharts | - | 数据可视化 |

## 模块划分

### 后端服务

| 模块 | 端口 | 说明 |
|------|------|------|
| mp-gateway | 8080 | API 网关，统一入口 |
| mp-auth | 8081 | 认证服务，登录鉴权 |
| mp-system | 8082 | 系统服务，核心业务 |
| mp-generator | 8083 | 代码生成 |
| mp-job | 8084 | 定时任务 |

### 前端应用

| 应用 | 端口 | 说明 |
|------|------|------|
| mp-vue | 3001 | Vue 前端应用 |
| docs | 5173 | VitePress 文档站点 |

## 核心功能

### 系统管理

- **用户管理**：用户 CRUD、角色分配、密码重置
- **角色管理**：角色 CRUD、权限分配、数据权限
- **菜单管理**：菜单 CRUD、按钮权限
- **部门管理**：组织架构树形管理
- **岗位管理**：岗位设置
- **字典管理**：数据字典配置

### 工作流

- **流程设计器**：基于 bpmn.js 的可视化流程设计
- **表单设计器**：拖拽式表单设计
- **表单配置**：启动表单、办理表单配置
- **任务管理**：待办、已办、任务办理
- **流程实例**：实例查询、挂起、激活、终止

### 高级功能

- **代码生成**：根据数据库表生成 CRUD 代码
- **数据权限**：基于角色的数据范围控制
- **多租户**：SaaS 多租户支持
- **操作日志**：完整的操作记录
- **系统监控**：服务器、JVM、Redis 监控

## 项目结构

```
micro-platform/
├── mp-gateway/              # 网关服务
├── mp-auth/                 # 认证服务
├── mp-system/               # 系统服务
├── mp-generator/            # 代码生成
├── mp-job/                  # 定时任务
├── mp-vue/                  # 前端应用
├── common/                  # 公共模块
│   ├── common-core/         # 核心工具
│   ├── common-log/          # 日志组件
│   ├── common-security/     # 安全组件
│   └── common-swagger/      # Swagger 配置
└── docs/                    # 文档站点
```

## 核心表结构

### 系统管理

| 表名 | 说明 |
|------|------|
| sys_user | 用户表 |
| sys_role | 角色表 |
| sys_menu | 菜单表 |
| sys_dept | 部门表 |
| sys_post | 岗位表 |
| sys_dict_type | 字典类型表 |
| sys_dict_data | 字典数据表 |
| sys_config | 参数配置表 |
| sys_notice | 通知公告表 |
| sys_oper_log | 操作日志表 |
| sys_logininfor | 登录日志表 |

### 工作流

| 表名 | 说明 |
|------|------|
| workflow_form_binding | 工作流表单绑定表 |
| form_definition | 表单定义表 |

### Flowable 内置表

| 表前缀 | 说明 |
|--------|------|
| ACT_RE | 流程定义表 |
| ACT_RU | 运行时表 |
| ACT_HI | 历史表 |
| ACT_ID | 身份表 |
| ACT_GE | 通用表 |

## API 设计

### RESTful 风格

```
GET    /system/user/list    # 查询列表
GET    /system/user/{id}    # 获取详情
POST   /system/user         # 新增
PUT    /system/user         # 修改
DELETE /system/user/{ids}   # 删除
```

### 统一响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```

### 认证方式

```
Authorization: Bearer <JWT Token>
```

## 开发规范

### 目录规范

```
src/main/java/com/micro/platform/system/
├── controller/      # 控制器层
├── service/         # 服务层接口
├── service/impl/    # 服务层实现
├── mapper/          # 数据访问层
├── entity/          # 实体类
├── domain/          # 领域模型
├── dto/             # 数据传输对象
└── utils/           # 工具类
```

### 命名规范

- 控制器：`XxxController`
- 服务接口：`XxxService`
- 服务实现：`XxxServiceImpl`
- 实体类：`Xxx`
- DTO：`XxxDTO`
- VO: `XxxVO`

### Git 提交规范

```
feat: 新功能
fix: 修复 Bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试
chore: 构建/工具
```

## 性能优化

### 数据库优化

- 添加必要索引
- 避免全表扫描
- 使用分页查询
- 慢查询优化

### 缓存优化

- Redis 缓存热点数据
- 本地缓存 Guava Cache
- 页面静态化

### 代码优化

- 使用连接池
- 异步处理耗时操作
- Stream API 简化集合操作
- Optional 避免空指针

## 安全加固

### 认证鉴权

- JWT Token 认证
- Spring Security 权限控制
- 接口权限注解

### 数据安全

- 密码加密存储（BCrypt）
- SQL 注入防护（预编译）
- XSS 攻击防护
- CSRF 防护

### 审计日志

- 操作日志记录
- 登录日志记录
- 敏感操作审计

## 部署方案

### 单机部署

```bash
# 启动脚本
./start.sh
```

### Docker 部署

```yaml
version: '3.8'
services:
  nacos:
    image: nacos/nacos-server
  mysql:
    image: mysql:8.0
  redis:
    image: redis:7-alpine
  mp-gateway:
    build: ./mp-gateway
  mp-auth:
    build: ./mp-auth
  mp-system:
    build: ./mp-system
```

### Kubernetes 部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mp-system
spec:
  replicas: 2
  selector:
    matchLabels:
      app: mp-system
  template:
    spec:
      containers:
      - name: mp-system
        image: mp-system:latest
        ports:
        - containerPort: 8082
```

## 监控告警

### Spring Boot Admin

- 服务健康检查
- JVM 监控
- 日志查看

### Prometheus + Grafana

- 指标采集
- 数据可视化
- 告警规则

### 日志收集

- ELK Stack（Elasticsearch, Logstash, Kibana）
- 分布式日志追踪

## 扩展开发

### 添加新模块

1. 复制现有模块结构
2. 修改 pom.xml
3. 修改包名
4. 配置 application.yml

### 自定义注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {
    String deptAlias() default "";
    String userAlias() default "";
}
```

### 自定义拦截器

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

- [GitHub 仓库](https://github.com/xyx11/platform)
- [文档站点](http://localhost:5173)
- [若依框架](http://ruoyi.vip/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Vue 3 官方文档](https://vuejs.org/)
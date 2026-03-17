# 更新日志

## v2.0.0 (2024-03-17)

### 新增

- **工作流模块**
  - BPMN 流程设计器
  - 表单设计器
  - 工作流表单绑定
  - 任务管理
  - 流程实例管理

- **文档站点**
  - 基于 VitePress 的文档系统
  - 完整的 API 文档
  - 使用指南
  - 部署指南

- **系统功能**
  - 用户管理增强
  - 角色权限管理
  - 数据权限控制
  - 操作日志记录

### 优化

- 前端性能优化
- 数据库查询优化
- 接口响应速度提升

### 修复

- 修复已知 Bug
- 优化用户体验

---

## v1.0.0 (2024-01-01)

### 新增

- **基础框架**
  - Spring Boot 3.2.5
  - Spring Cloud Alibaba
  - Vue 3 + Element Plus
  - MyBatis-Plus

- **核心模块**
  - mp-gateway 网关服务
  - mp-auth 认证服务
  - mp-system 系统服务
  - mp-generator 代码生成
  - mp-vue 前端应用

- **系统管理**
  - 用户管理
  - 角色管理
  - 菜单管理
  - 部门管理
  - 岗位管理
  - 字典管理

- **基础功能**
  - 登录认证
  - 权限控制
  - 操作日志
  - 登录日志

---

## 版本说明

| 版本 | 日期 | 说明 |
|------|------|------|
| v2.0.0 | 2024-03-17 | 工作流版本 |
| v1.0.0 | 2024-01-01 | 初始版本 |

---

## 技术栈更新

### 后端

| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | 核心框架 |
| Spring Cloud Alibaba | 2022.0.0.0 | 微服务套件 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| Flowable | 6.x | 工作流引擎 |
| Nacos | 2.2.x | 服务发现 |
| Redis | 7.x | 缓存 |
| MySQL | 8.0+ | 数据库 |

### 前端

| 组件 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.x | 渐进式框架 |
| Element Plus | 2.5.x | UI 组件库 |
| Vite | 5.x | 构建工具 |
| Vue Router | 4.x | 路由管理 |
| Pinia | 2.x | 状态管理 |
| Axios | 1.x | HTTP 客户端 |

---

## 升级指南

### v1.0.0 升级到 v2.0.0

1. **数据库升级**

```sql
-- 执行工作流相关 SQL
source /path/to/sql/workflow_form_binding.sql;
```

2. **依赖更新**

```xml
<!-- 添加 Flowable 依赖 -->
<dependency>
    <groupId>org.flowable</groupId>
    <artifactId>flowable-spring-boot-starter</artifactId>
    <version>6.x.x</version>
</dependency>
```

3. **配置更新**

```yaml
# 添加 Flowable 配置
flowable:
  async-executor-activate: true
  db-history-used: true
  history-level: full
```

4. **代码更新**

参考最新文档更新相关代码。

---

## 贡献者

感谢所有贡献者！

- [@xyx11](https://github.com/xyx11)
- ...

---

## 相关链接

- [GitHub](https://github.com/xyx11/platform)
- [文档站点](http://localhost:5173)
- [问题反馈](https://github.com/xyx11/platform/issues)
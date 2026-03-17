# 模块说明

## 服务模块

### mp-gateway (8080)

网关服务，基于 Spring Cloud Gateway

**主要功能：**
- 路由转发
- 统一鉴权
- 限流熔断
- 跨域配置
- 全局异常处理

**配置文件：**
```yaml
server:
  port: 8080

spring:
  cloud:
    gateway:
      routes:
        - id: mp-auth
          uri: lb://mp-auth
          predicates:
            - Path=/auth/**
        - id: mp-system
          uri: lb://mp-system
          predicates:
            - Path=/system/**,/workflow/**,/form/**
```

---

### mp-auth (8081)

认证服务，基于 Spring Security + OAuth2

**主要功能：**
- 用户登录认证
- Token 生成与校验
- 权限获取
- 单点登录 (SSO)

**API 接口：**
```
POST /auth/login          # 用户登录
POST /auth/logout         # 用户登出
GET  /auth/info           # 获取用户信息
POST /auth/refresh        # 刷新 Token
```

---

### mp-system (8082)

系统服务，核心业务服务

**主要功能：**
- 用户管理
- 角色管理
- 菜单管理
- 部门管理
- 岗位管理
- 字典管理
- 工作流管理
- 表单管理

---

### mp-generator (8083)

代码生成服务

**主要功能：**
- 数据库表导入
- 代码模板配置
- 代码生成
- 代码下载

---

## 前端模块 (mp-vue)

```
mp-vue/
├── src/
│   ├── api/               # API 接口
│   ├── assets/            # 静态资源
│   ├── components/        # 公共组件
│   ├── directives/        # 自定义指令
│   ├── layout/            # 布局组件
│   ├── router/            # 路由配置
│   ├── store/             # 状态管理
│   ├── utils/             # 工具函数
│   └── views/             # 页面组件
│       ├── system/        # 系统管理页面
│       ├── workflow/      # 工作流页面
│       └── ...
```
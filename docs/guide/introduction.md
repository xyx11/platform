# 项目简介

Micro Platform 是一个基于 Spring Boot + Vue 3 的微服务开发平台。

## 技术栈

### 后端
- **Spring Boot 3.2.5** - 核心框架
- **Spring Cloud Alibaba** - 微服务套件
- **Nacos** - 服务注册与配置中心
- **MyBatis-Plus** - ORM 框架
- **Flowable** - 工作流引擎
- **Redis** - 缓存
- **MySQL** - 数据库

### 前端
- **Vue 3** - 渐进式 JavaScript 框架
- **Element Plus** - UI 组件库
- **Vite** - 前端构建工具
- **Pinia** - 状态管理

## 模块划分

| 模块 | 端口 | 说明 |
|------|------|------|
| mp-gateway | 8080 | 网关服务 |
| mp-auth | 8081 | 认证服务 |
| mp-system | 8082 | 系统服务 |
| mp-generator | 8083 | 代码生成服务 |
| mp-vue | 3001 | 前端服务 |

## 核心功能

- ✅ 用户管理
- ✅ 角色管理
- ✅ 菜单管理
- ✅ 部门管理
- ✅ 岗位管理
- ✅ 字典管理
- ✅ 工作流管理
- ✅ 表单设计器
- ✅ 流程设计器
- ✅ 代码生成
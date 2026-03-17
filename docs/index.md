---
# https://vitepress.dev/reference/default-theme-home-page
layout: home

hero:
  name: 'Micro Platform'
  text: '微服务开发平台'
  tagline: 基于 Spring Boot + Vue 3 的企业级开发平台
  image:
    src: https://vitepress.dev/vitepress-logo-large.webp
    alt: Micro Platform
  actions:
    - theme: brand
      text: 快速开始
      link: /guide/introduction
    - theme: alt
      text: API 文档
      link: /api/overview
    - theme: alt
      text: GitHub
      link: https://github.com/xyx11/platform

features:
  - title: '微服务架构'
    details: 基于 Spring Cloud Alibaba，支持 Nacos 服务发现和配置中心
    icon: 🚀
    link: /guide/architecture

  - title: '工作流引擎'
    details: 集成 Flowable BPMN 引擎，支持流程设计和表单配置
    icon: ⚙️
    link: /guide/workflow/overview

  - title: '表单设计器'
    details: 可视化表单设计，拖拽式组件配置
    icon: 📝
    link: /guide/workflow/form-designer

  - title: '权限管理'
    details: 基于 RBAC 的权限控制，支持数据权限和多租户
    icon: 🔐
    link: /guide/system/user

  - title: '代码生成'
    details: 根据数据库表结构自动生成 CRUD 代码
    icon: ⚡
    link: /guide/advanced/generator

  - title: '前后端分离'
    details: Vue 3 + Element Plus 前端，RESTful API 后端
    icon: 💻
    link: /guide/modules

  - title: '操作日志'
    details: 完整的操作日志记录，支持查询和导出
    icon: 📋
    link: /guide/advanced/log

  - title: '多租户支持'
    details: 共享数据库 + 租户 ID 的数据隔离方案
    icon: 🏢
    link: /guide/advanced/tenant

  - title: '响应式设计'
    details: 支持 PC 端和移动端，自适应不同屏幕尺寸
    icon: 📱
---

## 快速开始

::: code-group

```bash [克隆项目]
git clone https://github.com/xyx11/platform.git
cd platform
```

```bash [安装依赖]
npm install
```

```bash [启动服务]
./start.sh
```

:::

## 技术栈

::: tip
后端采用 Java 17 + Spring Boot 3.2.5，前端采用 Vue 3 + Element Plus
:::

| 分类 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.5, Spring Cloud Alibaba |
| 持久层 | MyBatis-Plus, MySQL 8.0 |
| 缓存 | Redis 6.0 |
| 工作流 | Flowable 6.x |
| 认证授权 | Spring Security, JWT |
| 前端框架 | Vue 3, Element Plus, Vite |
| 服务发现 | Nacos 2.2 |

## 社区

- [GitHub](https://github.com/xyx11/platform)
- [Gitee](https://gitee.com/xyx11/platform)
- [问题反馈](https://github.com/xyx11/platform/issues)
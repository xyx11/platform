---
# https://vitepress.dev/reference/default-theme-home-page
layout: home

hero:
  name: 'Micro Platform'
  text: '微服务开发平台'
  tagline: 基于 Spring Boot + Vue 3 的企业级开发平台
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
  - title: '工作流引擎'
    details: 集成 Flowable BPMN 引擎，支持流程设计和表单配置
    icon: ⚙️
  - title: '前后端分离'
    details: Vue 3 + Element Plus 前端，RESTful API 后端
    icon: 💻
  - title: '权限管理'
    details: 基于 RBAC 的权限控制，支持数据权限
    icon: 🔐
---
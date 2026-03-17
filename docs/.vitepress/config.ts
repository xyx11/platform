import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: 'Micro Platform',
  description: '微服务开发平台文档',
  base: '/micro-platform/',
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: '首页', link: '/' },
      { text: '指南', link: '/guide/introduction' },
      { text: 'API', link: '/api/overview' }
    ],

    sidebar: [
      {
        text: '指南',
        items: [
          { text: '项目简介', link: '/guide/introduction' },
          { text: '快速开始', link: '/guide/quickstart' },
          { text: '架构设计', link: '/guide/architecture' },
          { text: '模块说明', link: '/guide/modules' }
        ]
      },
      {
        text: '工作流',
        items: [
          { text: '工作流概述', link: '/guide/workflow/overview' },
          { text: '流程设计器', link: '/guide/workflow/designer' },
          { text: '表单配置', link: '/guide/workflow/form-config' },
          { text: '任务管理', link: '/guide/workflow/task' }
        ]
      },
      {
        text: 'API',
        items: [
          { text: 'API 概览', link: '/api/overview' },
          { text: '工作流表单 API', link: '/api/workflow-form' },
          { text: '流程定义 API', link: '/api/workflow' },
          { text: '表单定义 API', link: '/api/form-definition' }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/xyx11/platform' }
    ],

    footer: {
      message: '基于 MIT 许可发布',
      copyright: 'Copyright © 2024-present Micro Platform'
    }
  },

  head: [
    ['link', { rel: 'icon', href: '/favicon.ico' }]
  ]
})
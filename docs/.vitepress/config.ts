import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'Micro Platform',
  description: '微服务开发平台',
  lastUpdated: true,
  cleanUrls: true,

  themeConfig: {
    logo: {
      src: 'https://vitepress.dev/vitepress-logo-mini.svg',
      alt: 'Logo'
    },

    nav: [
      { text: '首页', link: '/' },
      { text: '指南', link: '/guide/introduction' },
      { text: 'API 文档', link: '/api/overview' },
      { text: '更新日志', link: '/guide/changelog' },
      { text: 'GitHub', link: 'https://github.com/xyx11/platform' }
    ],

    sidebar: {
      '/guide/': [
        {
          text: '快速开始',
          items: [
            { text: '项目简介', link: '/guide/introduction' },
            { text: '快速开始', link: '/guide/quickstart' },
            { text: '环境配置', link: '/guide/environment' },
            { text: '架构设计', link: '/guide/architecture' },
            { text: '模块说明', link: '/guide/modules' },
            { text: '开发指南', link: '/guide/development' }
          ]
        },
        {
          text: '系统管理',
          items: [
            { text: '用户管理', link: '/guide/system/user' },
            { text: '角色管理', link: '/guide/system/role' },
            { text: '菜单管理', link: '/guide/system/menu' },
            { text: '部门管理', link: '/guide/system/dept' },
            { text: '字典管理', link: '/guide/system/dict' }
          ]
        },
        {
          text: '工作流',
          items: [
            { text: '工作流概述', link: '/guide/workflow/overview' },
            { text: '流程设计器', link: '/guide/workflow/designer' },
            { text: '表单设计器', link: '/guide/workflow/form-designer' },
            { text: '表单配置', link: '/guide/workflow/form-config' },
            { text: '任务管理', link: '/guide/workflow/task' },
            { text: '流程实例', link: '/guide/workflow/process-instance' }
          ]
        },
        {
          text: '高级功能',
          items: [
            { text: '代码生成', link: '/guide/advanced/generator' },
            { text: '数据权限', link: '/guide/advanced/data-permission' },
            { text: '多租户', link: '/guide/advanced/tenant' },
            { text: '操作日志', link: '/guide/advanced/log' },
            { text: '系统监控', link: '/guide/monitor' },
            { text: '定时任务', link: '/guide/scheduled-tasks' }
          ]
        },
        {
          text: '第三方服务',
          items: [
            { text: '服务集成', link: '/guide/integrations' }
          ]
        },
        {
          text: '部署与运维',
          items: [
            { text: '部署指南', link: '/guide/deploy' },
            { text: '常见问题', link: '/guide/faq' },
            { text: '项目总结', link: '/guide/summary' }
          ]
        },
        {
          text: '参考',
          items: [
            { text: '最佳实践', link: '/guide/best-practices' },
            { text: '快速参考', link: '/guide/quick-reference' },
            { text: '工具类参考', link: '/guide/utils-reference' },
            { text: '更新日志', link: '/guide/changelog' }
          ]
        }
      ],
      '/api/': [
        {
          text: 'API 概览',
          items: [
            { text: 'API 概述', link: '/api/overview' },
            { text: '认证说明', link: '/api/auth' },
            { text: '通用响应', link: '/api/response' },
            { text: '接口速查', link: '/api/quick-reference' }
          ]
        },
        {
          text: '系统管理 API',
          items: [
            { text: '用户管理', link: '/api/user' },
            { text: '角色管理', link: '/api/role' },
            { text: '菜单管理', link: '/api/menu' },
            { text: '部门管理', link: '/api/dept' },
            { text: '字典管理', link: '/api/dict' }
          ]
        },
        {
          text: '工作流 API',
          items: [
            { text: '工作流表单 API', link: '/api/workflow-form' },
            { text: '流程定义 API', link: '/api/workflow' },
            { text: '表单定义 API', link: '/api/form-definition' },
            { text: '任务管理 API', link: '/api/task' }
          ]
        }
      ]
    },

    outline: {
      label: '本页目录',
      level: [2, 3]
    },

    docFooter: {
      prev: '上一页',
      next: '下一页'
    },

    search: {
      provider: 'local',
      options: {
        locales: {
          root: {
            translations: {
              button: {
                buttonText: '搜索文档',
                buttonAriaLabel: '搜索文档'
              },
              modal: {
                noResultsText: '无法找到相关结果',
                resetButtonTitle: '清除查询条件',
                footer: {
                  selectText: '选择',
                  navigateText: '切换'
                }
              }
            }
          }
        }
      }
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/xyx11/platform' }
    ],

    footer: {
      message: '基于 MIT 许可发布',
      copyright: 'Copyright © 2024-present Micro Platform'
    }
  },

  head: [
    ['link', { rel: 'icon', href: '/favicon.ico' }],
    ['meta', { name: 'theme-color', content: '#3eaf7c' }]
  ],

  markdown: {
    lineNumbers: true
  }
})

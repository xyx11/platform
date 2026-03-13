import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      }
    ]
  },
  {
    path: '/user',
    redirect: '/system/user'
  },
  {
    path: '/system',
    component: () => import('@/layout/index.vue'),
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'user',
        name: 'SysUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'role',
        name: 'SysRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'Avatar' }
      },
      {
        path: 'menu',
        name: 'SysMenu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'Menu' }
      },
      {
        path: 'dept',
        name: 'SysDept',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'dict',
        name: 'SysDict',
        component: () => import('@/views/system/dict/index.vue'),
        meta: { title: '字典管理', icon: 'Collection' }
      },
      {
        path: 'post',
        name: 'SysPost',
        component: () => import('@/views/system/post/index.vue'),
        meta: { title: '岗位管理', icon: 'Postcard' }
      },
      {
        path: 'config',
        name: 'SysConfig',
        component: () => import('@/views/system/config/index.vue'),
        meta: { title: '参数设置', icon: 'Tools' }
      },
      {
        path: 'log',
        name: 'SysLog',
        component: () => import('@/views/system/log/index.vue'),
        meta: { title: '操作日志', icon: 'Document' }
      },
      {
        path: 'loginlog',
        name: 'SysLoginLog',
        component: () => import('@/views/system/loginlog/index.vue'),
        meta: { title: '登录日志', icon: 'Document' }
      },
      {
        path: 'notice',
        name: 'SysNotice',
        component: () => import('@/views/system/notice/index.vue'),
        meta: { title: '通知公告', icon: 'Bell' }
      },
      {
        path: 'command',
        name: 'SysCommand',
        component: () => import('@/views/system/command/index.vue'),
        meta: { title: '命令执行', icon: 'Terminal' }
      },
      {
        path: 'todo',
        name: 'SysTodo',
        component: () => import('@/views/system/todo/index.vue'),
        meta: { title: '待办事项', icon: 'List' }
      },
      {
        path: 'todo/recycle-bin',
        name: 'TodoRecycleBin',
        component: () => import('@/views/system/todo/recycle-bin.vue'),
        meta: { title: '待办回收站', icon: 'Delete' }
      },
      {
        path: 'todo/tag-manager',
        name: 'TodoTagManager',
        component: () => import('@/views/system/todo/tag.vue'),
        meta: { title: '标签管理', icon: 'PriceTag' }
      },
      {
        path: 'workflow/stats',
        name: 'WorkflowStats',
        component: () => import('@/views/system/workflow/stats.vue'),
        meta: { title: '工作流统计', icon: 'TrendCharts' }
      },
      {
        path: 'online-user',
        name: 'SysOnlineUser',
        component: () => import('@/views/system/online-user/index.vue'),
        meta: { title: '在线用户', icon: 'Connection' }
      },
      {
        path: 'workflow-designer',
        name: 'WorkflowDesigner',
        component: () => import('@/views/system/workflow/designer/index.vue'),
        meta: { title: '流程设计器', icon: 'EditPen' }
      },
      {
        path: 'workflow-form',
        name: 'WorkflowForm',
        component: () => import('@/views/system/workflow-form/index.vue'),
        meta: { title: '流程表单', icon: 'FileText' }
      },
      {
        path: 'websocket',
        name: 'WebSocket',
        component: () => import('@/views/system/websocket/index.vue'),
        meta: { title: '消息推送', icon: 'Connection' }
      },
      {
        path: 'workflow-definition',
        name: 'WorkflowDefinition',
        component: () => import('@/views/system/workflow-definition/index.vue'),
        meta: { title: '流程定义', icon: 'Flowchart' }
      },
      {
        path: 'process-instance',
        name: 'ProcessInstance',
        component: () => import('@/views/system/process-instance/index.vue'),
        meta: { title: '流程实例监控', icon: 'Monitor' }
      },
      {
        path: 'task',
        name: 'TaskManagement',
        component: () => import('@/views/system/task/index.vue'),
        meta: { title: '任务管理', icon: 'List' }
      },
      {
        path: 'tenant-package',
        name: 'TenantPackage',
        component: () => import('@/views/system/tenant-package/index.vue'),
        meta: { title: '套餐管理', icon: 'Package' }
      },
      {
        path: 'data-permission',
        name: 'DataPermission',
        component: () => import('@/views/system/data-permission/index.vue'),
        meta: { title: '数据权限规则', icon: 'Lock' }
      },
      {
        path: 'form-definition',
        name: 'FormDefinition',
        component: () => import('@/views/system/form-definition/index.vue'),
        meta: { title: '表单定义', icon: 'Document' }
      }
    ]
  },
  {
    path: '/job',
    component: () => import('@/layout/index.vue'),
    redirect: '/job/list',
    meta: { title: '定时任务', icon: 'Timer' },
    children: [
      {
        path: 'list',
        name: 'JobList',
        component: () => import('@/views/job/list/index.vue'),
        meta: { title: '任务管理', icon: 'List' }
      },
      {
        path: 'log',
        name: 'JobLog',
        component: () => import('@/views/job/log/index.vue'),
        meta: { title: '任务日志', icon: 'Document' }
      }
    ]
  },
  {
    path: '/generator',
    component: () => import('@/layout/index.vue'),
    redirect: '/generator/table',
    meta: { title: '代码生成', icon: 'Grid' },
    children: [
      {
        path: 'table',
        name: 'GeneratorTable',
        component: () => import('@/views/generator/table/index.vue'),
        meta: { title: '数据表', icon: 'Grid' }
      }
    ]
  },
  {
    path: '/monitor',
    component: () => import('@/layout/index.vue'),
    redirect: '/monitor/overview',
    meta: { title: '系统监控', icon: 'Monitor' },
    children: [
      {
        path: 'overview',
        name: 'SystemOverview',
        component: () => import('@/views/system/monitor/overview.vue'),
        meta: { title: '监控总览', icon: 'Monitor' }
      },
      {
        path: 'online',
        name: 'OnlineUser',
        component: () => import('@/views/system/monitor/online.vue'),
        meta: { title: '在线用户', icon: 'User' }
      },
      {
        path: 'redis',
        name: 'RedisMonitor',
        component: () => import('@/views/system/monitor/redis.vue'),
        meta: { title: 'Redis 监控', icon: 'Folder' }
      },
      {
        path: 'server',
        name: 'ServerMonitor',
        component: () => import('@/views/system/monitor/server.vue'),
        meta: { title: '服务器监控', icon: 'Platform' }
      },
      {
        path: 'cache',
        name: 'CacheMonitor',
        component: () => import('@/views/system/monitor/cache.vue'),
        meta: { title: '缓存监控', icon: 'DataLine' }
      }
    ]
  },
  {
    path: '/tool',
    component: () => import('@/layout/index.vue'),
    redirect: '/tool/build',
    meta: { title: '系统工具', icon: 'Tools' },
    children: [
      {
        path: 'build',
        name: 'ToolBuild',
        component: () => import('@/views/tool/build/index.vue'),
        meta: { title: '表单构建', icon: 'EditPen' }
      }
    ]
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/index.vue'),
    meta: { title: '个人中心' }
  },
  {
    path: '/system/file',
    name: 'SysFile',
    component: () => import('@/views/system/file/index.vue'),
    meta: { title: '文件管理', icon: 'Folder' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - Micro Platform`
  }

  const token = localStorage.getItem('access_token')

  if (to.path === '/login') {
    // 已登录则跳转到首页
    if (token) {
      next({ path: '/dashboard', replace: true })
    } else {
      next()
    }
    return
  }

  // 其他路由检查登录状态
  if (token) {
    next()
  } else {
    next({ path: '/login', replace: true })
  }
})

export default router
import { createRouter, createWebHistory } from 'vue-router'

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
        meta: { title: '日志管理', icon: 'Document' }
      },
      {
        path: 'notice',
        name: 'SysNotice',
        component: () => import('@/views/system/notice/index.vue'),
        meta: { title: '通知公告', icon: 'Bell' }
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
    meta: { title: '代码生成', icon: 'Code' },
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
    redirect: '/monitor/online',
    meta: { title: '系统监控', icon: 'Monitor' },
    children: [
      {
        path: 'online',
        name: 'OnlineUser',
        component: () => import('@/views/monitor/online/index.vue'),
        meta: { title: '在线用户', icon: 'User' }
      },
      {
        path: 'redis',
        name: 'RedisMonitor',
        component: () => import('@/views/monitor/redis/index.vue'),
        meta: { title: 'Redis 监控', icon: 'Folder' }
      },
      {
        path: 'server',
        name: 'ServerMonitor',
        component: () => import('@/views/monitor/server/index.vue'),
        meta: { title: '服务器监控', icon: 'Platform' }
      },
      {
        path: 'cache',
        name: 'CacheMonitor',
        component: () => import('@/views/monitor/cache/index.vue'),
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
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 简化版本
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - Micro Platform`
  }

  // 登录页直接访问
  if (to.path === '/login') {
    next()
    return
  }

  // 其他路由检查登录状态
  const token = localStorage.getItem('access_token')
  if (token) {
    next()
  } else {
    next('/login')
  }
})

export default router
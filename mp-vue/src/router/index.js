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

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - Micro Platform` : 'Micro Platform'

  const token = localStorage.getItem('access_token')

  if (to.path === '/login') {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router

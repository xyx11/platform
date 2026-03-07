<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span v-show="!isCollapse">Micro Platform</span>
        <span v-show="isCollapse">MP</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <!-- 有多个子路由，显示为子菜单 -->
          <el-sub-menu v-if="route.children && route.children.length > 1" :index="route.path">
            <template #title>
              <el-icon><component :is="route.meta.icon" /></el-icon>
              <span>{{ route.meta.title }}</span>
            </template>
            <el-menu-item
              v-for="child in route.children"
              :key="child.path"
              :index="child.path"
            >
              <el-icon><component :is="child.meta.icon" /></el-icon>
              <span>{{ child.meta.title }}</span>
            </el-menu-item>
          </el-sub-menu>
          <!-- 只有一个子路由或没有子路由，显示为单项菜单 -->
          <el-menu-item v-else :index="getChildPath(route)">
            <el-icon><component :is="getIcon(route)" /></el-icon>
            <span>{{ getTitle(route) }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute">{{ currentRoute.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <div class="header-actions">
            <el-button link @click="toggleTheme" :title="isDark ? '切换浅色模式' : '切换深色模式'">
              <el-icon :size="20"><component :is="isDark ? 'Sunny' : 'Moon'" /></el-icon>
            </el-button>
            <el-button link @click="refreshPage" title="刷新">
              <el-icon :size="20"><Refresh /></el-icon>
            </el-button>
            <NoticeIcon />
          </div>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <span class="username">管理员</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="setting">系统设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import NoticeIcon from '@/components/NoticeIcon.vue'
// 导入 Element Plus 图标
import {
  HomeFilled,
  Setting,
  User,
  Avatar,
  Menu,
  OfficeBuilding,
  Collection,
  Postcard,
  Tools,
  Document,
  Timer,
  List,
  Grid,
  Monitor,
  Folder,
  Platform,
  DataLine,
  EditPen,
  Expand,
  Fold,
  Moon,
  Sunny,
  Refresh
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const isCollapse = ref(false)
const isDark = ref(false)

// 菜单路由（直接定义，避免访问 router.options.routes）
const menuRoutes = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    meta: { title: '首页', icon: 'HomeFilled' }
  },
  {
    path: '/system',
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      { path: '/system/user', name: 'SysUser', meta: { title: '用户管理', icon: 'User' } },
      { path: '/system/role', name: 'SysRole', meta: { title: '角色管理', icon: 'Avatar' } },
      { path: '/system/menu', name: 'SysMenu', meta: { title: '菜单管理', icon: 'Menu' } },
      { path: '/system/dept', name: 'SysDept', meta: { title: '部门管理', icon: 'OfficeBuilding' } },
      { path: '/system/dict', name: 'SysDict', meta: { title: '字典管理', icon: 'Collection' } },
      { path: '/system/post', name: 'SysPost', meta: { title: '岗位管理', icon: 'Postcard' } },
      { path: '/system/config', name: 'SysConfig', meta: { title: '参数设置', icon: 'Tools' } },
      { path: '/system/log', name: 'SysLog', meta: { title: '日志管理', icon: 'Document' } },
      { path: '/system/notice', name: 'SysNotice', meta: { title: '通知公告', icon: 'Bell' } }
    ]
  },
  {
    path: '/job',
    redirect: '/job/list',
    meta: { title: '定时任务', icon: 'Timer' },
    children: [
      { path: '/job/list', name: 'JobList', meta: { title: '任务管理', icon: 'List' } },
      { path: '/job/log', name: 'JobLog', meta: { title: '任务日志', icon: 'Document' } }
    ]
  },
  {
    path: '/generator',
    redirect: '/generator/table',
    meta: { title: '代码生成', icon: 'Grid' },
    children: [
      { path: '/generator/table', name: 'GeneratorTable', meta: { title: '数据表', icon: 'Grid' } }
    ]
  },
  {
    path: '/monitor',
    redirect: '/monitor/online',
    meta: { title: '系统监控', icon: 'Monitor' },
    children: [
      { path: '/monitor/online', name: 'OnlineUser', meta: { title: '在线用户', icon: 'User' } },
      { path: '/monitor/redis', name: 'RedisMonitor', meta: { title: 'Redis 监控', icon: 'Folder' } },
      { path: '/monitor/server', name: 'ServerMonitor', meta: { title: '服务器监控', icon: 'Platform' } },
      { path: '/monitor/cache', name: 'CacheMonitor', meta: { title: '缓存监控', icon: 'DataLine' } }
    ]
  },
  {
    path: '/tool',
    redirect: '/tool/build',
    meta: { title: '系统工具', icon: 'Tools' },
    children: [
      { path: '/tool/build', name: 'ToolBuild', meta: { title: '表单构建', icon: 'EditPen' } }
    ]
  }
]

const activeMenu = computed(() => {
  return route.path
})

const currentRoute = computed(() => {
  return route.matched[route.matched.length - 1]
})

// 获取子路由路径
const getChildPath = (route) => {
  if (route.redirect) {
    return route.redirect
  }
  if (route.children && route.children.length === 1) {
    const childPath = route.children[0].path
    return childPath.startsWith('/') ? childPath : `${route.path}/${childPath}`
  }
  return route.path
}

// 获取标题
const getTitle = (route) => {
  if (route.meta && route.meta.title) {
    return route.meta.title
  }
  if (route.children && route.children.length === 1 && route.children[0].meta) {
    return route.children[0].meta.title
  }
  return ''
}

// 获取图标
const getIcon = (route) => {
  if (route.meta && route.meta.icon) {
    return route.meta.icon
  }
  if (route.children && route.children.length === 1 && route.children[0].meta) {
    return route.children[0].meta.icon
  }
  return ''
}

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const toggleTheme = () => {
  isDark.value = !isDark.value
  document.documentElement.classList.toggle('dark', isDark.value)
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
}

const refreshPage = () => {
  location.reload()
}

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      localStorage.removeItem('access_token')
      localStorage.removeItem('user_info')
      ElMessage.success('已退出登录')
      router.push('/login')
    })
  } else if (command === 'profile') {
    ElMessage.info('个人中心功能开发中')
  } else if (command === 'setting') {
    ElMessage.info('系统设置功能开发中')
  }
}
onMounted(() => {
  // 检查保存的主题
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme === 'dark') {
    isDark.value = true
    document.documentElement.classList.add('dark')
  }
})
</script>

<style lang="scss" scoped>
.layout-container {
  display: flex;
  height: 100%;
}

.sidebar {
  background-color: #304156;
  transition: width 0.3s;
  overflow-x: hidden;

  .logo {
    height: 60px;
    line-height: 60px;
    text-align: center;
    font-size: 20px;
    font-weight: bold;
    color: #fff;
    background-color: #2b3a4b;
  }

  .el-menu {
    border-right: none;
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 20px;

    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      transition: color 0.3s;

      &:hover {
        color: #409EFF;
      }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;

      .username {
        font-size: 14px;
      }
    }
  }
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.header-actions {
    display: flex;
    gap: 10px;
    margin-right: 15px;
  }
}

/* 暗黑模式 */
html.dark {
  .el-card {
    background-color: #1d1e1f;
    border-color: #434343;
  }
  
  .el-main {
    background-color: #141414;
  }
  
  .sidebar {
    background-color: #141414 !important;
  }
  
  .header {
    background-color: #1d1e1f !important;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
  }
}
</style>
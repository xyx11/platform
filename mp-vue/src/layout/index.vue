<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="sidebarWidth" class="sidebar">
      <div class="logo">
        <div class="logo-icon">
          <el-icon :size="22"><Platform /></el-icon>
        </div>
        <span v-show="!isCollapse" class="logo-text">Micro Platform</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        :unique-opened="true"
        background-color="#fff"
        text-color="#333"
        active-text-color="#1e80ff"
        @select="handleMenuSelect"
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <el-sub-menu v-if="route.children && route.children.length > 1" :index="route.path">
            <template #title>
              <el-icon class="menu-icon"><component :is="route.meta.icon" /></el-icon>
              <span>{{ route.meta.title }}</span>
            </template>
            <el-menu-item
              v-for="child in route.children"
              :key="child.path"
              :index="child.path"
            >
              <el-icon class="menu-icon"><component :is="child.meta.icon" /></el-icon>
              <span>{{ child.meta.title }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="getChildPath(route)">
            <el-icon class="menu-icon"><component :is="getIcon(route)" /></el-icon>
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
          <div class="collapse-btn" @click="toggleCollapse">
            <el-icon :size="18">
              <component :is="isCollapse ? 'Expand' : 'Fold'" />
            </el-icon>
          </div>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute">{{ currentRoute.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <div class="header-actions">
            <div class="action-btn" @click="toggleTheme" :title="isDark ? '切换浅色模式' : '切换深色模式'">
              <el-icon :size="18"><component :is="isDark ? 'Sunny' : 'Moon'" /></el-icon>
            </div>
            <div class="action-btn" @click="refreshPage" title="刷新">
              <el-icon :size="18"><Refresh /></el-icon>
            </div>
            <div class="action-btn">
              <NoticeIcon />
            </div>
          </div>
          <el-divider direction="vertical" />
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="28" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <span class="username">{{ userInfo.nickname || userInfo.username || '管理员' }}</span>
              <el-icon :size="14"><ArrowDown /></el-icon>
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

      <!-- 标签页 -->
      <div class="tabs-container">
        <div class="tabs-wrapper">
          <el-tabs
            ref="tabsRef"
            v-model="activeTab"
            type="card"
            closable
            @tab-click="handleTabClick"
            @tab-remove="handleTabClose"
            @edit="handleTabsEdit"
          >
            <el-tab-pane
              v-for="item in visitedViews"
              :key="item.path"
              
              :name="item.path"
            >
              <template #label>
                <span @contextmenu.prevent="handleContextMenu($event, item.path)">{{ item.title }}</span>
              </template>
            </el-tab-pane>
          </el-tabs>
        </div>
        <div class="tabs-extra">
          <el-dropdown :hide-on-click="false">
            <div class="extra-btn">
              <el-icon :size="16"><MoreFilled /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="refreshCurrent">
                  <el-icon><Refresh /></el-icon> 刷新
                </el-dropdown-item>
                <el-dropdown-item @click="closeCurrent">
                  <el-icon><Close /></el-icon> 关闭
                </el-dropdown-item>
                <el-dropdown-item @click="closeOtherTabs">
                  <el-icon><FolderDelete /></el-icon> 关闭其他
                </el-dropdown-item>
                <el-dropdown-item @click="closeAllTabs">
                  <el-icon><Delete /></el-icon> 关闭全部
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 右键上下文菜单 -->
      <div
        v-if="contextMenuVisible"
        class="context-menu"
        :style="{ left: contextMenuLeft + 'px', top: contextMenuTop + 'px' }"
      >
        <el-menu class="context-menu-list">
          <el-menu-item @click="executeMenuCommand('refresh')">
            <el-icon><Refresh /></el-icon> <span>刷新</span>
          </el-menu-item>
          <el-menu-item @click="executeMenuCommand('close')">
            <el-icon><Close /></el-icon> <span>关闭</span>
          </el-menu-item>
          <el-menu-item @click="executeMenuCommand('closeOther')">
            <el-icon><FolderDelete /></el-icon> <span>关闭其他</span>
          </el-menu-item>
          <el-menu-item @click="executeMenuCommand('closeLeft')">
            <el-icon><DArrowLeft /></el-icon> <span>关闭左侧</span>
          </el-menu-item>
          <el-menu-item @click="executeMenuCommand('closeRight')">
            <el-icon><DArrowRight /></el-icon> <span>关闭右侧</span>
          </el-menu-item>
          <el-divider style="margin: 4px 0;" />
          <el-menu-item @click="executeMenuCommand('closeAll')">
            <el-icon><Delete /></el-icon> <span>关闭全部</span>
          </el-menu-item>
        </el-menu>
      </div>
      <div v-if="contextMenuVisible" class="context-menu-overlay" @click="closeContextMenu"></div>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <keep-alive>
              <component :is="Component" :key="route.fullPath" v-if="isRouterAlive" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import NoticeIcon from '@/components/NoticeIcon.vue'
import {
  Platform, Expand, Fold, Moon, Sunny, Refresh, Close, FolderDelete, Delete,
  MoreFilled, ArrowDown, DArrowLeft, DArrowRight
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const tabsRef = ref(null)
const isRouterAlive = ref(true)

const isCollapse = ref(false)
const isDark = ref(false)
const activeTab = ref(route.path || '/dashboard')
const visitedViews = ref([])
const userInfo = ref({})

// 右键菜单相关
const contextMenuVisible = ref(false)
const contextMenuDropdownVisible = ref(false)
const contextMenuLeft = ref(0)
const contextMenuTop = ref(0)
const currentContextMenuPath = ref('')

const sidebarWidth = computed(() => {
  return isCollapse.value ? '64px' : '220px'
})

const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route.matched[route.matched.length - 1])

const menuRoutes = [
  { path: '/dashboard', name: 'Dashboard', meta: { title: '首页', icon: 'HomeFilled' } },
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
      { path: '/system/log', name: 'SysLog', meta: { title: '操作日志', icon: 'Document' } },
      { path: '/system/loginlog', name: 'SysLoginLog', meta: { title: '登录日志', icon: 'Document' } },
      { path: '/system/notice', name: 'SysNotice', meta: { title: '通知公告', icon: 'Bell' } },
      { path: '/system/command', name: 'SysCommand', meta: { title: '命令执行', icon: 'Tools' } },
      { path: '/system/todo', name: 'SysTodo', meta: { title: '待办事项', icon: 'Clock' } }
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
    redirect: '/monitor/overview',
    meta: { title: '系统监控', icon: 'Monitor' },
    children: [
      { path: '/monitor/overview', name: 'SystemOverview', meta: { title: '监控总览', icon: 'Monitor' } },
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

const getChildPath = (route) => {
  if (route.redirect) return route.redirect
  if (route.children && route.children.length === 1) {
    const childPath = route.children[0].path
    return childPath.startsWith('/') ? childPath : `${route.path}/${childPath}`
  }
  return route.path
}

const getTitle = (route) => {
  if (route.meta && route.meta.title) return route.meta.title
  if (route.children && route.children.length === 1 && route.children[0].meta) {
    return route.children[0].meta.title
  }
  return ''
}

const getIcon = (route) => {
  if (route.meta && route.meta.icon) return route.meta.icon
  if (route.children && route.children.length === 1 && route.children[0].meta) {
    return route.children[0].meta.icon
  }
  return ''
}

const handleMenuSelect = (index) => {
  const targetRoute = menuRoutes.find(r => r.path === index)
  if (targetRoute && targetRoute.redirect) {
    router.push(targetRoute.redirect)
    return
  }
  let foundRoute = null
  menuRoutes.forEach(r => {
    if (r.children) {
      const child = r.children.find(c => c.path === index)
      if (child) foundRoute = child
    }
    if (r.path === index) foundRoute = r
  })
  if (foundRoute) {
    addTab(foundRoute)
    router.push(index)
  }
}

const addTab = (route) => {
  const exists = visitedViews.value.find(item => item.path === route.path)
  if (!exists) {
    visitedViews.value.push({ path: route.path, title: route.meta.title, name: route.name })
  }
  activeTab.value = route.path
}

const handleTabClick = (tab) => {
  const view = visitedViews.value.find(item => item.path === tab.props.name)
  if (view) router.push(view.path)
}

const handleTabClose = (path) => {
  const index = visitedViews.value.findIndex(item => item.path === path)
  if (index !== -1) {
    visitedViews.value.splice(index, 1)
    if (activeTab.value === path) {
      const newLength = visitedViews.value.length
      if (newLength > 0) {
        const newIndex = index > 0 ? index - 1 : 0
        activeTab.value = visitedViews.value[newIndex].path
        router.push(activeTab.value)
      }
    }
  }
}

const handleTabsEdit = (target, action) => {
  if (action === 'remove') handleTabClose(target)
}

const refreshCurrent = () => {
  const view = visitedViews.value.find(item => item.path === activeTab.value)
  if (view) {
    ElMessage.success('刷新：' + view.title)
    isRouterAlive.value = false
    nextTick(() => {
      isRouterAlive.value = true
    })
  }
}

const closeCurrent = () => {
  handleTabClose(activeTab.value)
}

const closeOtherTabs = () => {
  const keep = ['/dashboard', activeTab.value]
  visitedViews.value = visitedViews.value.filter(item => keep.includes(item.path))
  ElMessage.success('已关闭其他标签页')
}

const closeAllTabs = () => {
  visitedViews.value = visitedViews.value.filter(item => item.path === '/dashboard')
  activeTab.value = '/dashboard'
  router.push('/dashboard')
  ElMessage.success('已关闭全部标签页')
}

const toggleCollapse = () => { isCollapse.value = !isCollapse.value }

// 右键菜单相关方法
const handleContextMenu = (e, path) => {
  e.preventDefault()
  e.stopPropagation()
  currentContextMenuPath.value = path
  contextMenuVisible.value = true
  
  // 计算菜单位置，确保不超出屏幕
  const menuWidth = 160
  const menuHeight = 220
  const screenWidth = window.innerWidth
  const screenHeight = window.innerHeight
  
  let left = e.clientX
  let top = e.clientY
  
  if (left + menuWidth > screenWidth) {
    left = screenWidth - menuWidth - 10
  }
  if (top + menuHeight > screenHeight) {
    top = screenHeight - menuHeight - 10
  }
  
  contextMenuLeft.value = left
  contextMenuTop.value = top
}

const closeContextMenu = () => {
  contextMenuVisible.value = false
}

const executeMenuCommand = (command) => {
  const path = currentContextMenuPath.value
  
  switch (command) {
    case 'refresh':
      if (path === activeTab.value) {
        refreshCurrent()
      } else {
        handleTabClick({ props: { name: path } })
        nextTick(() => {
          refreshCurrent()
        })
      }
      break
    case 'close':
      if (path !== activeTab.value) {
        handleTabClose(path)
      } else {
        closeCurrent()
      }
      break
    case 'closeOther':
      if (path === activeTab.value) {
        closeOtherTabs()
      } else {
        handleTabClick({ props: { name: path } })
        nextTick(() => {
          closeOtherTabs()
        })
      }
      break
    case 'closeLeft':
      closeLeftTabs(path)
      break
    case 'closeRight':
      closeRightTabs(path)
      break
    case 'closeAll':
      closeAllTabs()
      break
  }
  closeContextMenu()
}

const closeLeftTabs = (targetPath) => {
  const targetIndex = visitedViews.value.findIndex(item => item.path === targetPath)
  if (targetIndex === -1) return

  const keepPaths = ['/dashboard']
  visitedViews.value.forEach((item, index) => {
    if (index >= targetIndex || item.path === '/dashboard') {
      keepPaths.push(item.path)
    }
  })

  visitedViews.value = visitedViews.value.filter(item => keepPaths.includes(item.path))

  if (!keepPaths.includes(activeTab.value)) {
    activeTab.value = targetPath
    router.push(activeTab.value)
  }

  ElMessage.success('已关闭左侧标签页')
}

const closeRightTabs = (targetPath) => {
  const targetIndex = visitedViews.value.findIndex(item => item.path === targetPath)
  if (targetIndex === -1) return

  const keepPaths = ['/dashboard']
  visitedViews.value.forEach((item, index) => {
    if (index <= targetIndex || item.path === '/dashboard') {
      keepPaths.push(item.path)
    }
  })

  visitedViews.value = visitedViews.value.filter(item => keepPaths.includes(item.path))

  if (!keepPaths.includes(activeTab.value)) {
    activeTab.value = targetPath
    router.push(activeTab.value)
  }

  ElMessage.success('已关闭右侧标签页')
}

const toggleTheme = () => {
  isDark.value = !isDark.value
  document.documentElement.classList.toggle('dark', isDark.value)
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
}

const refreshPage = () => { location.reload() }

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
    router.push('/profile')
  } else if (command === 'setting') {
    ElMessage.info('系统设置功能开发中')
  }
}

const getUserInfo = () => {
  const info = localStorage.getItem('user_info')
  if (info) {
    try { userInfo.value = JSON.parse(info) }
    catch (e) { userInfo.value = {} }
  }
}

const addView = (routerView) => {
  if (!visitedViews.value.find(item => item.path === routerView.path)) {
    visitedViews.value.push({
      path: routerView.path,
      title: routerView.meta.title,
      name: routerView.name
    })
  }
  activeTab.value = routerView.path
}

watch(() => route.path, (newPath) => {
  if (newPath && !visitedViews.value.find(item => item.path === newPath)) {
    const currentRouteMatched = route.matched[route.matched.length - 1]
    if (currentRouteMatched && currentRouteMatched.meta) {
      addView({ path: newPath, meta: currentRouteMatched.meta, name: currentRouteMatched.name })
    }
  }
}, { immediate: true })

onMounted(() => {
  getUserInfo()
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme === 'dark') {
    isDark.value = true
    document.documentElement.classList.add('dark')
  }
})
</script>

<style lang="scss" scoped>
$primary-color: #1e80ff;
$primary-light: rgba(30, 128, 255, 0.1);
$text-primary: #333333;
$text-regular: #666666;
$text-secondary: #999999;
$bg-color-page: #f5f6f7;
$bg-color-white: #ffffff;
$bg-color-gray: #fafafa;
$border-color: #e3e4e6;
$sidebar-width: 220px;
$sidebar-collapse-width: 64px;
$header-height: 50px;
$tabs-height: 42px;

.layout-container {
  display: flex;
  height: 100vh;
  background: $bg-color-page;
}

.sidebar {
  width: $sidebar-width;
  background: $bg-color-white;
  box-shadow: 1px 0 0 rgba(0, 0, 0, 0.06);
  transition: width 0.2s ease;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .logo {
    height: 50px;
    display: flex;
    align-items: center;
    padding: 0 16px;
    gap: 10px;
    border-bottom: 1px solid $border-color;

    .logo-icon {
      width: 28px;
      height: 28px;
      background: linear-gradient(135deg, $primary-color 0%, #3d8bfd 100%);
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      flex-shrink: 0;
    }

    .logo-text {
      font-size: 15px;
      font-weight: 600;
      color: $text-primary;
      white-space: nowrap;
      transition: opacity 0.2s ease;
    }
  }

  :deep(.el-menu) {
    border-right: none;
    background: transparent;
    flex: 1;
    overflow-y: auto;

    .el-menu-item {
      height: 42px;
      line-height: 42px;
      margin: 2px 6px;
      border-radius: 4px;
      font-size: 14px;
      color: $text-regular;
      padding-left: 16px !important;

      &:hover {
        background: $bg-color-gray;
        color: $primary-color;
      }

      &.is-active {
        background: $primary-light;
        color: $primary-color;
        font-weight: 500;
      }

      .menu-icon {
        width: 16px;
        margin-right: 10px;
        font-size: 16px;
      }
    }

    .el-sub-menu__title {
      height: 42px;
      line-height: 42px;
      margin: 2px 6px;
      border-radius: 4px;
      font-size: 14px;
      color: $text-regular;
      padding-left: 16px !important;

      &:hover {
        background: $bg-color-gray;
      }

      .menu-icon {
        width: 16px;
        margin-right: 10px;
        font-size: 16px;
      }
    }

    .el-sub-menu .el-menu-item {
      padding-left: 44px !important;
      background: transparent;
    }
  }

  &.is-collapse {
    width: $sidebar-collapse-width;

    .logo-text {
      display: none;
    }

    :deep(.el-menu) {
      .el-menu-item, .el-sub-menu__title {
        padding-left: 20px !important;

        .menu-icon {
          margin-right: 0;
        }

        span {
          display: none;
        }
      }

      .el-sub-menu .el-menu-item {
        padding-left: 20px !important;
      }
    }
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.header {
  height: $header-height;
  background: $bg-color-white;
  border-bottom: 1px solid $border-color;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  flex-shrink: 0;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .collapse-btn {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.2s ease;
      color: $text-secondary;

      &:hover {
        background: $bg-color-gray;
        color: $primary-color;
      }
    }

    :deep(.el-breadcrumb) {
      .el-breadcrumb__item {
        .el-breadcrumb__inner {
          color: $text-secondary;
          font-size: 13px;
        }

        &:last-child .el-breadcrumb__inner {
          color: $text-primary;
          font-weight: 500;
        }
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 8px;

    .header-actions {
      display: flex;
      align-items: center;
      gap: 4px;

      .action-btn {
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.2s ease;
        color: $text-secondary;

        &:hover {
          background: $bg-color-gray;
          color: $primary-color;
        }
      }
    }

    :deep(.el-divider) {
      height: 20px;
      margin: 0 8px;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 6px 10px;
      border-radius: 4px;
      transition: all 0.2s ease;

      &:hover {
        background: $bg-color-gray;
      }

      .username {
        font-size: 13px;
        font-weight: 500;
        color: $text-primary;
      }
    }
  }
}

.tabs-container {
  height: $tabs-height;
  background: $bg-color-white;
  border-bottom: 1px solid $border-color;
  display: flex;
  align-items: center;
  padding: 4px 12px 0;
  flex-shrink: 0;
  position: relative;

  .tabs-wrapper {
    flex: 1;
    overflow: hidden;

    :deep(.el-tabs) {
      .el-tabs__header {
        margin-bottom: 0;
        border-bottom: none;
      }

      .el-tabs__item {
        height: 34px;
        line-height: 34px;
        border: 1px solid $border-color;
        background: $bg-color-gray;
        margin-right: 4px;
        border-radius: 4px 4px 0 0;
        padding: 0 14px;
        font-size: 13px;
        color: $text-regular;
        transition: all 0.2s ease;

        &:hover {
          background: #f0f2f5;
        }

        &.is-active {
          background: $bg-color-white;
          color: $primary-color;
          border-bottom-color: $bg-color-white;
          font-weight: 500;
          padding: 0 12px;
        }

        .is-icon-close {
          margin-left: 6px;
          font-size: 11px;
          padding: 2px;
          border-radius: 2px;

          &:hover {
            background-color: #ff4d4f;
            color: #fff;
          }
        }
      }

      .el-tabs__nav-next,
      .el-tabs__nav-prev {
        font-size: 14px;
        color: $text-secondary;
        height: 34px;
        line-height: 34px;

        &:hover {
          color: $primary-color;
        }
      }
    }
  }

  .tabs-extra {
    flex-shrink: 0;
    margin-left: 8px;

    .extra-btn {
      width: 28px;
      height: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px;
      cursor: pointer;
      color: $text-secondary;

      &:hover {
        background: $bg-color-gray;
        color: $primary-color;
      }
    }
  }
}

// 右键上下文菜单
.context-menu {
  position: fixed;
  z-index: 9999;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  padding: 4px 0;
  min-width: 140px;
}

.context-menu-list {
  background: transparent;
  border: none;
  
  .el-menu-item {
    height: 36px;
    line-height: 36px;
    font-size: 13px;
    color: $text-regular;
    padding: 0 16px;
    margin: 0;
    
    &:hover {
      background: $bg-color-gray;
      color: $primary-color;
    }
    
    .el-icon {
      margin-right: 8px;
      font-size: 14px;
    }
    
    span {
      white-space: nowrap;
    }
  }
}

.context-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9998;
}

.main-content {
  flex: 1;
  background: $bg-color-page;
  padding: 16px;
  overflow-y: auto;
  min-height: 0;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: #d9d9d9;
    border-radius: 3px;

    &:hover {
      background: #bfbfbf;
    }
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

/* 暗黑模式 */
html.dark {
  .sidebar {
    background: #1d1e1f;
    box-shadow: 1px 0 0 rgba(255, 255, 255, 0.06);

    .logo {
      border-color: #434343;

      .logo-text {
        color: #e2e8f0;
      }
    }

    :deep(.el-menu) {
      .el-menu-item {
        color: #94a3b8;

        &:hover {
          background: #2d2d2d;
          color: #667eea;
        }

        &.is-active {
          background: rgba(102, 126, 234, 0.15);
          color: #667eea;
        }
      }

      .el-sub-menu__title {
        color: #94a3b8;

        &:hover {
          background: #2d2d2d;
        }
      }
    }
  }

  .header {
    background: #1d1e1f;
    border-color: #434343;

    .header-left {
      .collapse-btn {
        color: #94a3b8;

        &:hover {
          background: #2d2d2d;
          color: #fff;
        }
      }

      :deep(.el-breadcrumb) {
        .el-breadcrumb__item {
          .el-breadcrumb__inner {
            color: #94a3b8;
          }
          &:last-child .el-breadcrumb__inner {
            color: #e2e8f0;
          }
        }
      }
    }

    .header-right {
      .header-actions {
        .action-btn {
          color: #94a3b8;

          &:hover {
            background: #2d2d2d;
            color: #fff;
          }
        }
      }

      :deep(.el-divider) {
        border-color: #434343;
      }

      .user-info {
        .username {
          color: #e2e8f0;
        }

        &:hover {
          background: #2d2d2d;
        }
      }
    }
  }

  .tabs-container {
    background: #1d1e1f;
    border-color: #434343;

    .tabs-extra {
      .extra-btn {
        color: #94a3b8;

        &:hover {
          background: #2d2d2d;
          color: #fff;
        }
      }
    }

    :deep(.el-tabs__item) {
      background: #2d2d3a;
      border-color: #434343;
      color: #94a3b8;

      &:hover {
        background: #3d3d5c;
      }

      &.is-active {
        background: #1d1e1f;
        color: #667eea;
        border-bottom-color: #1d1e1f;
      }

      .is-icon-close {
        &:hover {
          background-color: #f56c6c;
          color: #fff;
        }
      }
    }
  }

  .main-content {
    background: #141414;
  }

  .el-card {
    background-color: #1d1e1f;
    border-color: #434343;
  }

  .context-menu {
    background: #1d1e1f;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);

    .context-menu-list {
      .el-menu-item {
        color: #94a3b8;

        &:hover {
          background: #2d2d2d;
          color: #667eea;
        }
      }
    }
  }
}
</style>
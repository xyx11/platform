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
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        @select="handleMenuSelect"
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
              <span class="username">{{ userInfo.nickname || userInfo.username || '管理员' }}</span>
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
      <div class="tabs-container" @contextmenu.prevent="handleContainerContextmenu">
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
            :label="item.title"
            :name="item.path"
          >
            <template #label>
              <span
                :data-path="item.path"
                @contextmenu.prevent="handleContextMenu($event, item)"
              >
                {{ item.title }}
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>

        <!-- 右键菜单 -->
        <div
          v-if="contextMenuVisible"
          class="context-menu"
          :style="{ left: contextMenuLeft + 'px', top: contextMenuTop + 'px' }"
        >
          <el-menu @select="handleContextMenuSelect">
            <el-menu-item index="refresh">
              <el-icon><Refresh /></el-icon> 刷新
            </el-menu-item>
            <el-menu-item index="close">
              <el-icon><Close /></el-icon> 关闭
            </el-menu-item>
            <el-menu-item index="closeOther">
              <el-icon><FolderDelete /></el-icon> 关闭其他
            </el-menu-item>
            <el-menu-item index="closeAll">
              <el-icon><Delete /></el-icon> 关闭全部
            </el-menu-item>
          </el-menu>
        </div>
      </div>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <keep-alive>
              <component :is="Component" :key="route.fullPath" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage, ElDropdownMenu } from 'element-plus'
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
  Refresh,
  Bell,
  Terminal,
  Clock,
  Close,
  FolderDelete,
  Delete
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const tabsRef = ref(null)

const isCollapse = ref(false)
const isDark = ref(false)
const activeTab = ref('/dashboard')
const visitedViews = ref([
  { path: '/dashboard', title: '首页', name: 'Dashboard' }
])
const userInfo = ref({})

// 右键菜单相关
const contextMenuVisible = ref(false)
const contextMenuLeft = ref(0)
const contextMenuTop = ref(0)
const selectedTab = ref(null)

// 菜单路由
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
      { path: '/system/log', name: 'SysLog', meta: { title: '操作日志', icon: 'Document' } },
      { path: '/system/loginlog', name: 'SysLoginLog', meta: { title: '登录日志', icon: 'Document' } },
      { path: '/system/notice', name: 'SysNotice', meta: { title: '通知公告', icon: 'Bell' } },
      { path: '/system/command', name: 'SysCommand', meta: { title: '命令执行', icon: 'Terminal' } },
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

// 处理菜单点击
const handleMenuSelect = (index) => {
  const targetRoute = menuRoutes.find(r => r.path === index)
  if (targetRoute && targetRoute.redirect) {
    router.push(targetRoute.redirect)
    return
  }

  // 查找完整路由信息
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

// 添加标签页
const addTab = (route) => {
  const exists = visitedViews.value.find(item => item.path === route.path)
  if (!exists) {
    visitedViews.value.push({
      path: route.path,
      title: route.meta.title,
      name: route.name
    })
  }
  activeTab.value = route.path
}

// 点击标签页
const handleTabClick = (tab) => {
  const view = visitedViews.value.find(item => item.path === tab.props.name)
  if (view) {
    router.push(view.path)
  }
  closeContextMenu()
}

// 关闭标签页
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
  if (action === 'remove') {
    handleTabClose(target)
  }
  closeContextMenu()
}

// 右键菜单 - 容器
const handleContainerContextmenu = (e) => {
  e.preventDefault()
  closeContextMenu()
}

// 右键菜单 - 标签页
const handleContextMenu = (e, item) => {
  e.preventDefault()
  e.stopPropagation()
  selectedTab.value = item
  contextMenuVisible.value = true
  contextMenuLeft.value = e.clientX
  contextMenuTop.value = e.clientY

  nextTick(() => {
    document.addEventListener('click', closeContextMenu)
  })
}

// 关闭右键菜单
const closeContextMenu = () => {
  contextMenuVisible.value = false
  selectedTab.value = null
  document.removeEventListener('click', closeContextMenu)
}

// 处理右键菜单选择
const handleContextMenuSelect = (index) => {
  switch (index) {
    case 'refresh':
      refreshTab()
      break
    case 'close':
      closeSelectedTab()
      break
    case 'closeOther':
      closeOtherTabs()
      break
    case 'closeAll':
      closeAllTabs()
      break
  }
  closeContextMenu()
}

// 刷新标签页
const refreshTab = () => {
  if (selectedTab.value) {
    const view = visitedViews.value.find(item => item.path === selectedTab.value.path)
    if (view) {
      ElMessage.success('刷新：' + view.title)
      router.replace({ path: view.path + '?refresh=' + Date.now() })
    }
  }
}

// 关闭选中标签页
const closeSelectedTab = () => {
  if (selectedTab.value && selectedTab.value.path !== '/dashboard') {
    handleTabClose(selectedTab.value.path)
    ElMessage.success('已关闭：' + selectedTab.value.title)
  } else if (selectedTab.value && selectedTab.value.path === '/dashboard') {
    ElMessage.warning('首页不能关闭')
  }
}

// 关闭其他标签页
const closeOtherTabs = () => {
  if (selectedTab.value) {
    const keep = ['/dashboard', selectedTab.value.path]
    visitedViews.value = visitedViews.value.filter(item => keep.includes(item.path))
    activeTab.value = selectedTab.value.path
    router.push(selectedTab.value.path)
    ElMessage.success('已关闭其他标签页')
  }
}

// 关闭全部标签页
const closeAllTabs = () => {
  visitedViews.value = visitedViews.value.filter(item => item.path === '/dashboard')
  activeTab.value = '/dashboard'
  router.push('/dashboard')
  ElMessage.success('已关闭全部标签页')
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
    router.push('/profile')
  } else if (command === 'setting') {
    ElMessage.info('系统设置功能开发中')
  }
}

// 获取用户信息
const getUserInfo = () => {
  const info = localStorage.getItem('user_info')
  if (info) {
    try {
      userInfo.value = JSON.parse(info)
    } catch (e) {
      userInfo.value = {}
    }
  }
}

// 监听路由变化，自动添加标签页
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

onMounted(() => {
  getUserInfo()
  // 检查保存的主题
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme === 'dark') {
    isDark.value = true
    document.documentElement.classList.add('dark')
  }
  // 如果当前路由不是首页，添加标签页
  if (route.path !== '/dashboard') {
    addView(route)
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
  height: 50px;

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

    .header-actions {
      display: flex;
      gap: 10px;
      margin-right: 15px;
    }
  }
}

.tabs-container {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 10px;
  position: relative;

  :deep(.el-tabs) {
    .el-tabs__header {
      margin-bottom: 0;
    }

    .el-tabs__item {
      height: 40px;
      line-height: 40px;
      border: 1px solid #e4e7ed;
      background-color: #f5f7fa;
      margin-right: 5px;
      border-radius: 4px 4px 0 0;
      user-select: none;

      &.is-active {
        background-color: #409EFF;
        color: #fff;
        border-color: #409EFF;
      }

      .is-icon-close {
        margin-left: 8px;

        &:hover {
          background-color: #f56c6c;
          color: #fff;
        }
      }
    }
  }
}

.context-menu {
  position: fixed;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  z-index: 9999;
  min-width: 120px;

  .el-menu {
    border: none;

    .el-menu-item {
      height: 36px;
      line-height: 36px;
      font-size: 13px;

      .el-icon {
        margin-right: 8px;
      }

      &:hover {
        background-color: #f5f7fa;
      }
    }
  }
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
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

  .tabs-container {
    background-color: #1d1e1f;
    border-color: #434343;

    :deep(.el-tabs__item) {
      background-color: #2d2d2d;
      border-color: #434343;
      color: #bfcbd9;

      &.is-active {
        background-color: #409EFF;
        color: #fff;
        border-color: #409EFF;
      }
    }
  }

  .context-menu {
    background-color: #1d1e1f;
    border-color: #434343;

    .el-menu-item:hover {
      background-color: #2d2d2d;
    }
  }
}
</style>
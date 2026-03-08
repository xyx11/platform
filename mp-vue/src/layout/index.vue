<template>
  <div class="layout-container" :class="'layout-' + layoutMode">
    <!-- 横向顶部菜单 -->
    <template v-if="layoutMode === 'horizontal'">
      <div class="top-nav">
        <div class="top-nav-left">
          <div class="logo">
            <div class="logo-icon">
              <el-icon :size="24"><Platform /></el-icon>
            </div>
            <span class="logo-text">Micro Platform</span>
          </div>
          <el-menu
            :default-active="activeMenu"
            mode="horizontal"
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
                  <span>{{ child.meta.title }}</span>
                </el-menu-item>
              </el-sub-menu>
              <el-menu-item v-else :index="getChildPath(route)">
                <el-icon class="menu-icon"><component :is="getIcon(route)" /></el-icon>
                <span>{{ getTitle(route) }}</span>
              </el-menu-item>
            </template>
          </el-menu>
        </div>
        <div class="top-nav-right">
          <div class="header-actions">
            <el-tooltip :content="layoutMode === 'horizontal' ? '切换到侧边栏模式' : '切换到顶部菜单模式'" placement="bottom">
              <div class="action-btn" @click="toggleLayoutMode">
                <el-icon :size="18"><Menu /></el-icon>
              </div>
            </el-tooltip>
            <el-tooltip :content="isDark ? '切换浅色模式' : '切换深色模式'" placement="bottom">
              <div class="action-btn" @click="toggleTheme">
                <el-icon :size="18"><component :is="isDark ? 'Sunny' : 'Moon'" /></el-icon>
              </div>
            </el-tooltip>
            <el-tooltip content="刷新页面 (Ctrl+R)" placement="bottom">
              <div class="action-btn" @click="refreshPage">
                <el-icon :size="18"><Refresh /></el-icon>
              </div>
            </el-tooltip>
            <el-tooltip :content="isFullScreen ? '退出全屏' : '全屏'" placement="bottom">
              <div class="action-btn" @click="toggleFullScreen">
                <el-icon :size="18"><FullScreen /></el-icon>
              </div>
            </el-tooltip>
            <el-tooltip content="消息通知" placement="bottom">
              <div class="action-btn">
                <NoticeIcon />
              </div>
            </el-tooltip>
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
      </div>

      <el-container class="main-container">
        <div class="tabs-container">
          <div class="tabs-wrapper">
            <el-tabs
              ref="tabsRef"
              v-model="activeTab"
              type="card"
              closable
              draggable
              @tab-click="handleTabClick"
              @tab-remove="handleTabClose"
              @edit="handleTabsEdit"
              @tab-change="handleTabChange"
            >
              <el-tab-pane
                v-for="(item, index) in visitedViews"
                :key="item.path"
                :name="item.path"
                :closable="item.path !== '/dashboard'"
              >
                <template #label>
                  <span 
                    @contextmenu.prevent="handleContextMenu($event, item.path)"
                    @dblclick="handleTabDoubleClick(item.path)"
                    class="tab-label"
                  >
                    <el-icon v-if="item.path === '/dashboard'" style="margin-right: 4px; vertical-align: middle;"><HomeFilled /></el-icon>
                    {{ item.title }}
                  </span>
                </template>
              </el-tab-pane>
            </el-tabs>
          </div>
          <div class="tabs-extra">
            <span class="tabs-count" :class="{ warning: visitedViews.length >= MAX_TABS - 2 }">
              {{ visitedViews.length }}/{{ MAX_TABS }}
            </span>
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
    </template>

    <!-- 侧边栏菜单模式 -->
    <template v-else>
      <el-aside :width="sidebarWidth" :class="{ 'is-collapse': isCollapse }" class="sidebar">
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
      <el-container class="main-container">
        <el-header class="header">
          <div class="header-left">
            <div class="collapse-btn" @click="toggleCollapse">
              <el-icon :size="18">
                <component :is="isCollapse ? 'Expand' : 'Fold'" />
              </el-icon>
            </div>
            <div class="action-btn" @click="toggleLayoutMode" title="切换到顶部菜单模式">
              <el-icon :size="18"><Menu /></el-icon>
            </div>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentRoute">{{ currentRoute.meta.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <div class="header-actions">
              <el-tooltip :content="isDark ? '切换浅色模式' : '切换深色模式'" placement="bottom">
                <div class="action-btn" @click="toggleTheme">
                  <el-icon :size="18"><component :is="isDark ? 'Sunny' : 'Moon'" /></el-icon>
                </div>
              </el-tooltip>
              <el-tooltip content="刷新页面 (Ctrl+R)" placement="bottom">
                <div class="action-btn" @click="refreshPage">
                  <el-icon :size="18"><Refresh /></el-icon>
                </div>
              </el-tooltip>
              <el-tooltip :content="isFullScreen ? '退出全屏' : '全屏'" placement="bottom">
                <div class="action-btn" @click="toggleFullScreen">
                  <el-icon :size="18"><FullScreen /></el-icon>
                </div>
              </el-tooltip>
              <el-tooltip content="消息通知" placement="bottom">
                <div class="action-btn">
                  <NoticeIcon />
                </div>
              </el-tooltip>
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
        <div class="tabs-container">
          <div class="tabs-wrapper">
            <el-tabs
              ref="tabsRef"
              v-model="activeTab"
              type="card"
              closable
              draggable
              @tab-click="handleTabClick"
              @tab-remove="handleTabClose"
              @edit="handleTabsEdit"
              @tab-change="handleTabChange"
            >
              <el-tab-pane
                v-for="(item, index) in visitedViews"
                :key="item.path"
                :name="item.path"
                :closable="item.path !== '/dashboard'"
              >
                <template #label>
                  <span 
                    @contextmenu.prevent="handleContextMenu($event, item.path)"
                    @dblclick="handleTabDoubleClick(item.path)"
                    class="tab-label"
                  >
                    <el-icon v-if="item.path === '/dashboard'" style="margin-right: 4px; vertical-align: middle;"><HomeFilled /></el-icon>
                    {{ item.title }}
                  </span>
                </template>
              </el-tab-pane>
            </el-tabs>
          </div>
          <div class="tabs-extra">
            <span class="tabs-count" :class="{ warning: visitedViews.length >= MAX_TABS - 2 }">
              {{ visitedViews.length }}/{{ MAX_TABS }}
            </span>
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
    </template>

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
        <el-menu-item 
          v-if="currentContextMenuPath !== '/dashboard'"
          @click="executeMenuCommand('close')"
        >
          <el-icon><Close /></el-icon> <span>关闭</span>
        </el-menu-item>
        <el-menu-item @click="executeMenuCommand('closeOther')">
          <el-icon><FolderDelete /></el-icon> <span>关闭其他</span>
        </el-menu-item>
        <el-menu-item 
          v-if="currentContextMenuPath !== '/dashboard' && visitedViews.findIndex(i => i.path === currentContextMenuPath) > 1"
          @click="executeMenuCommand('closeLeft')"
        >
          <el-icon><DArrowLeft /></el-icon> <span>关闭左侧</span>
        </el-menu-item>
        <el-menu-item 
          v-if="currentContextMenuPath !== '/dashboard' && currentContextMenuPath !== activeTab"
          @click="executeMenuCommand('closeRight')"
        >
          <el-icon><DArrowRight /></el-icon> <span>关闭右侧</span>
        </el-menu-item>
        <el-divider style="margin: 4px 0;" />
        <el-menu-item @click="executeMenuCommand('closeAll')">
          <el-icon><Delete /></el-icon> <span>关闭全部</span>
        </el-menu-item>
      </el-menu>
    </div>
    <div v-if="contextMenuVisible" class="context-menu-overlay" @click="closeContextMenu"></div>
    
    <!-- 页面加载进度条 -->
    <div v-if="loading" class="page-loading">
      <div class="loading-spinner">
        <div class="spinner-circle"></div>
        <p class="loading-text">加载中...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import NoticeIcon from '@/components/NoticeIcon.vue'
import {
  Platform, Expand, Fold, Moon, Sunny, Refresh, Close, FolderDelete, Delete,
  MoreFilled, ArrowDown, DArrowLeft, DArrowRight, FullScreen, Menu, HomeFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const tabsRef = ref(null)
const isRouterAlive = ref(true)

const isDark = ref(false)
const isFullScreen = ref(false)
const loading = ref(false)
const activeTab = ref(route.path || '/dashboard')
const visitedViews = ref([])
const userInfo = ref({})
const MAX_TABS = 10  // 最大标签页数量

// 布局模式：horizontal = 横向菜单，vertical = 侧边栏菜单
const layoutMode = ref(localStorage.getItem('layoutMode') || 'horizontal')
const isCollapse = ref(false)

const sidebarWidth = computed(() => {
  return isCollapse.value ? '64px' : '220px'
})

// 右键菜单相关
const contextMenuVisible = ref(false)
const contextMenuLeft = ref(0)
const contextMenuTop = ref(0)
const currentContextMenuPath = ref('')

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
    if (visitedViews.value.length >= MAX_TABS) {
      ElMessage.warning(`最多只能打开${MAX_TABS}个标签页，请先关闭一些标签页`)
      return
    }
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

// 双击关闭标签页
const handleTabDoubleClick = (path) => {
  if (path !== '/dashboard') {
    handleTabClose(path)
  }
}

// 标签页顺序变化处理
const handleTabChange = (newOrder) => {
  // 标签页拖拽后自动保存顺序到 localStorage
  const order = visitedViews.value.map(item => item.path)
  localStorage.setItem('tabsOrder', JSON.stringify(order))
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
  if (activeTab.value !== '/dashboard') {
    ElMessage.success('已关闭其他标签页')
  } else {
    ElMessage.info('首页无法关闭')
  }
}

const closeAllTabs = () => {
  visitedViews.value = visitedViews.value.filter(item => item.path === '/dashboard')
  activeTab.value = '/dashboard'
  router.push('/dashboard')
  ElMessage.success('已关闭全部标签页')
}

// 检查是否是首页
const isHome = (path) => path === '/dashboard'

// 切换布局模式
const toggleLayoutMode = () => {
  layoutMode.value = layoutMode.value === 'horizontal' ? 'vertical' : 'horizontal'
  localStorage.setItem('layoutMode', layoutMode.value)
}

const toggleCollapse = () => { isCollapse.value = !isCollapse.value }

// 右键菜单相关方法
const handleContextMenu = (e, path) => {
  e.preventDefault()
  e.stopPropagation()
  currentContextMenuPath.value = path
  contextMenuVisible.value = true

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
        nextTick(() => { refreshCurrent() })
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
        nextTick(() => { closeOtherTabs() })
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
  if (targetPath === '/dashboard') {
    ElMessage.info('首页左侧无法关闭')
    return
  }
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
  if (targetPath === '/dashboard') {
    ElMessage.info('首页右侧无法关闭')
    return
  }
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

const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullScreen.value = true
  } else {
    document.exitFullscreen()
    isFullScreen.value = false
  }
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

watch(() => route.path, async (newPath) => {
  if (newPath) {
    loading.value = true
    if (!visitedViews.value.find(item => item.path === newPath)) {
      const currentRouteMatched = route.matched[route.matched.length - 1]
      if (currentRouteMatched && currentRouteMatched.meta) {
        addView({ path: newPath, meta: currentRouteMatched.meta, name: currentRouteMatched.name })
      }
    }
    await nextTick()
    loading.value = false
  }
}, { immediate: true })

onMounted(() => {
  getUserInfo()
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme === 'dark') {
    isDark.value = true
    document.documentElement.classList.add('dark')
  }
  document.addEventListener('fullscreenchange', () => {
    isFullScreen.value = !!document.fullscreenElement
  })

  // 键盘快捷键
  document.addEventListener('keydown', (e) => {
    // Ctrl/Cmd + R: 刷新当前标签页
    if ((e.ctrlKey || e.metaKey) && e.key === 'r') {
      e.preventDefault()
      refreshCurrent()
    }
    // Ctrl/Cmd + W: 关闭当前标签页
    if ((e.ctrlKey || e.metaKey) && e.key === 'w') {
      e.preventDefault()
      closeCurrent()
    }
    // Ctrl/Cmd + 数字：切换标签页
    if ((e.ctrlKey || e.metaKey) && /^[1-9]$/.test(e.key)) {
      e.preventDefault()
      const index = parseInt(e.key) - 1
      if (visitedViews.value[index]) {
        activeTab.value = visitedViews.value[index].path
        router.push(activeTab.value)
      }
    }
  })
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
$top-nav-height: 60px;

.layout-container {
  display: flex;
  height: 100vh;
  background: $bg-color-page;

  &.layout-horizontal {
    flex-direction: column;
  }

  &.layout-vertical {
    flex-direction: row;
  }
}

// 顶部导航栏（横向菜单模式）
.top-nav {
  height: $top-nav-height;
  background: $bg-color-white;
  border-bottom: 1px solid $border-color;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .top-nav-left {
    display: flex;
    align-items: center;
    flex: 1;
    overflow: hidden;

    .logo {
      display: flex;
      align-items: center;
      padding-right: 24px;
      flex-shrink: 0;

      .logo-icon {
        width: 32px;
        height: 32px;
        background: linear-gradient(135deg, $primary-color 0%, #3d8bfd 100%);
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        margin-right: 10px;
        transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
        box-shadow: 0 2px 8px rgba(30, 128, 255, 0.2);

        &:hover {
          transform: rotate(15deg) scale(1.1);
          box-shadow: 0 4px 16px rgba(30, 128, 255, 0.4);
        }

        .el-icon {
          transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }

        &:hover .el-icon {
          transform: scale(1.2);
        }
      }

      .logo-text {
        font-size: 16px;
        font-weight: 600;
        color: $text-primary;
        white-space: nowrap;
        transition: color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      }
    }

    .logo:hover .logo-text {
      color: $primary-color;
    }

    :deep(.el-menu) {
      display: flex;
      flex: 1;
      border: none;
      background: transparent;
      overflow: hidden;
      overflow-x: auto;

      &::-webkit-scrollbar {
        height: 4px;
      }

      &::-webkit-scrollbar-thumb {
        background: #d9d9d9;
        border-radius: 2px;

        &:hover {
          background: #bfbfbf;
        }
      }

      &::-webkit-scrollbar-track {
        background: transparent;
      }

      .el-menu-item {
        height: $top-nav-height;
        line-height: $top-nav-height;
        border: none;
        font-size: 14px;
        color: $text-regular;
        position: relative;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        &::after {
          content: '';
          position: absolute;
          bottom: 0;
          left: 50%;
          transform: translateX(-50%);
          width: 0;
          height: 3px;
          background: linear-gradient(90deg, $primary-color 0%, #3d8bfd 100%);
          opacity: 0;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          border-radius: 3px 3px 0 0;
        }

        &:hover {
          background: $bg-color-gray;
          color: $primary-color;

          &::after {
            width: 80%;
            opacity: 1;
          }
        }

        &.is-active {
          background: $primary-light;
          color: $primary-color;
          font-weight: 500;

          &::after {
            width: 80%;
            opacity: 1;
          }
        }

        .menu-icon {
          margin-right: 6px;
          font-size: 16px;
          transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }

        &:hover .menu-icon {
          transform: scale(1.1) rotate(5deg);
        }
      }

      .el-sub-menu {
        .el-sub-menu__title {
          height: $top-nav-height;
          line-height: $top-nav-height;
          border: none;
          font-size: 14px;
          color: $text-regular;

          &:hover {
            background: $bg-color-gray;
          }

          .menu-icon {
            margin-right: 6px;
            font-size: 16px;
          }
        }
      }
    }
  }

  .top-nav-right {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;

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
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
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
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

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

// 侧边栏样式
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
      position: relative;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 0;
        height: 60%;
        background: linear-gradient(90deg, $primary-color 0%, transparent 100%);
        opacity: 0;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        border-radius: 0 4px 4px 0;
      }

      &:hover {
        background: $bg-color-gray;
        color: $primary-color;
        transform: translateX(4px);

        &::before {
          width: 4px;
          opacity: 1;
        }
      }

      &.is-active {
        background: $primary-light;
        color: $primary-color;
        font-weight: 500;

        &::before {
          width: 4px;
          opacity: 1;
        }
      }

      .menu-icon {
        width: 16px;
        margin-right: 10px;
        font-size: 16px;
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      }

      &:hover .menu-icon {
        transform: scale(1.1);
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
      position: relative;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 0;
        height: 60%;
        background: linear-gradient(90deg, $primary-color 0%, transparent 100%);
        opacity: 0;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        border-radius: 0 4px 4px 0;
      }

      &:hover {
        background: $bg-color-gray;

        &::before {
          width: 4px;
          opacity: 1;
        }
      }

      .menu-icon {
        width: 16px;
        margin-right: 10px;
        font-size: 16px;
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      }

      &:hover .menu-icon {
        transform: scale(1.1);
      }
    }

    .el-sub-menu .el-menu-item {
      padding-left: 44px !important;
      background: transparent;
    }
  }

  &.is-collapse {
    width: $sidebar-collapse-width;

    .logo {
      justify-content: center;
      padding: 0;

      .logo-icon {
        margin-right: 0;
      }
    }

    .logo-text {
      display: none;
      opacity: 0;
    }

    :deep(.el-menu) {
      .el-menu-item, .el-sub-menu__title {
        padding-left: 20px !important;
        margin: 2px 4px;

        &::before {
          display: none !important;
        }

        .menu-icon {
          margin-right: 0;
          width: 20px;
          display: flex;
          justify-content: center;
        }

        span {
          display: none;
          opacity: 0;
          visibility: hidden;
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
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      color: $text-secondary;

      &:hover {
        background: $bg-color-gray;
        color: $primary-color;
      }
    }

    .action-btn {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
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
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
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
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

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
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: linear-gradient(180deg, rgba(255,255,255,0.3) 0%, transparent 100%);
          opacity: 0;
          transition: opacity 0.3s ease;
          pointer-events: none;
          border-radius: 4px 4px 0 0;
        }

        &:hover::before {
          opacity: 1;
        }

        .tab-label {
          cursor: pointer;
          user-select: none;
          display: inline-flex;
          align-items: center;
          gap: 4px;
          transition: color 0.3s ease;

          &:hover {
            color: $primary-color;
          }
        }

        &:hover {
          background: #f0f2f5;
          transform: translateY(-1px);
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
        }

        &.is-active {
          background: $bg-color-white;
          color: $primary-color;
          border-bottom-color: $bg-color-white;
          border-right: none;
          font-weight: 500;
          padding: 0 12px;
          box-shadow: 0 -2px 8px rgba(30, 128, 255, 0.1);
          z-index: 1;

          &::after {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 2px;
            background: linear-gradient(90deg, $primary-color 0%, #3d8bfd 100%);
            border-radius: 4px 4px 0 0;
          }
        }

        .is-icon-close {
          margin-left: 6px;
          font-size: 11px;
          padding: 2px;
          border-radius: 2px;
          transition: all 0.2s ease;

          &:hover {
            background-color: #ff4d4f;
            color: #fff;
            transform: scale(1.1);
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
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
    margin-left: 8px;

    .tabs-count {
      font-size: 12px;
      color: $text-secondary;
      padding: 2px 8px;
      background: $bg-color-gray;
      border-radius: 4px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &.warning {
        color: #ff4d4f;
        background: #fff1f0;
        font-weight: 500;
      }
    }

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
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  padding: 4px 0;
  min-width: 160px;
  animation: context-menu-fade-in 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

@keyframes context-menu-fade-in {
  from {
    opacity: 0;
    transform: translateY(-8px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.context-menu-list {
  background: transparent;
  border: none;

  .el-menu-item {
    height: 40px;
    line-height: 40px;
    font-size: 13px;
    color: $text-regular;
    padding: 0 16px;
    margin: 2px 0;
    position: relative;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 0;
      height: 60%;
      background: linear-gradient(90deg, $primary-color 0%, transparent 100%);
      opacity: 0;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      border-radius: 0 2px 2px 0;
    }

    &:hover {
      background: $bg-color-gray;
      color: $primary-color;

      &::before {
        width: 3px;
        opacity: 1;
      }
    }

    &.is-disabled {
      opacity: 0.5;
      cursor: not-allowed;

      &:hover {
        background: transparent;
        color: $text-regular;

        &::before {
          width: 0;
          opacity: 0;
        }
      }
    }

    .el-icon {
      margin-right: 10px;
      font-size: 15px;
      transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    }

    &:hover .el-icon {
      transform: scale(1.1);
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
  scroll-behavior: smooth;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: linear-gradient(180deg, #c1c1c1 0%, #d9d9d9 100%);
    border-radius: 3px;
    transition: background 0.3s ease;

    &:hover {
      background: linear-gradient(180deg, #a8a8a8 0%, #bfbfbf 100%);
    }
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1), transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(8px) scale(0.98);
}

.fade-enter-to {
  opacity: 1;
  transform: translateY(0) scale(1);
}

.fade-leave-from {
  opacity: 1;
  transform: translateY(0) scale(1);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.98);
}

// 标签页动画
.tabs-fade-enter-active,
.tabs-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tabs-fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.tabs-fade-enter-to {
  opacity: 1;
  transform: translateX(0);
}

.tabs-fade-leave-from {
  opacity: 1;
  transform: translateX(0);
  max-width: 200px;
}

.tabs-fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
  max-width: 0;
  overflow: hidden;
  padding: 0;
  margin-right: 0;
}

// 按钮点击波纹效果
@keyframes ripple {
  0% {
    transform: scale(0);
    opacity: 0.6;
  }
  100% {
    transform: scale(4);
    opacity: 0;
  }
}

/* 暗黑模式 */
html.dark {
  .top-nav {
    background: #1d1e1f;
    border-color: #434343;

    .logo .logo-text {
      color: #e2e8f0;
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

    .top-nav-right {
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
      .collapse-btn, .action-btn {
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

    .tabs-count {
      background: #2d2d3a;
      color: #94a3b8;

      &.warning {
        color: #ff4d4f;
        background: rgba(255, 77, 79, 0.15);
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

  .main-content {
    background: #141414;
  }

  .el-card {
    background-color: #1d1e1f;
    border-color: #434343;
  }

  .page-loading {
    background: #1d1e1f;

    .loading-spinner {
      .loading-text {
        color: #94a3b8;
      }

      .spinner-circle {
        border-color: #667eea;
      }
    }
  }
}
</style>

// 页面加载进度条
.page-loading {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(4px);

  .loading-spinner {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;

    .spinner-circle {
      width: 40px;
      height: 40px;
      border: 3px solid #e3e4e6;
      border-top-color: $primary-color;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    .loading-text {
      font-size: 14px;
      color: $text-secondary;
    }
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}


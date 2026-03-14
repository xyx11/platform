import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import { ElMessage, ElMessageBox, ElNotification, ElLoading } from 'element-plus'

import App from './App.vue'
import router from './router'
import request from './utils/request'

// 导入全局组件
import RightToolbar from './components/RightToolbar.vue'
import Pagination from './components/Pagination.vue'

const app = createApp(App)
const pinia = createPinia()

// 注册全局组件
app.component('RightToolbar', RightToolbar)
app.component('Pagination', Pagination)

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 配置全局方法
app.config.globalProperties.request = request

// 封装 modal 对象
app.config.globalProperties.$modal = {
  msgSuccess: (message) => {
    ElMessage.success(message)
  },
  msgError: (message) => {
    ElMessage.error(message)
  },
  msgWarning: (message) => {
    ElMessage.warning(message)
  },
  msgInfo: (message) => {
    ElMessage.info(message)
  },
  confirm: (message, title, options) => {
    return ElMessageBox.confirm(message, title || '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      ...options
    })
  },
  prompt: (message, title, options) => {
    return ElMessageBox.prompt(message, title || '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      ...options
    })
  },
  alert: (message, title, options) => {
    return ElMessageBox.alert(message, title || '提示', {
      confirmButtonText: '确定',
      type: 'info',
      ...options
    })
  },
  closeLoading: () => {
    ElLoading.service().close()
  },
  loading: (message) => {
    return ElLoading.service({
      lock: true,
      text: message || '加载中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
  }
}

// 表单重置方法
app.config.globalProperties.resetForm = function(refName) {
  if (refName && this[refName]) {
    this[refName].resetFields()
  }
}

app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
  size: 'default'
})

app.mount('#app')
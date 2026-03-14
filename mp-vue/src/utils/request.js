import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 添加请求方法别名 - 使用箭头函数绑定 this
service.get = (url, params = {}, config = {}) => {
  // 支持 { params: ... } 格式或直接传递 params 对象
  const finalParams = params && params.params ? params.params : params
  return service({ url, method: 'get', params: finalParams, ...config })
}
service.post = (url, data, config = {}) => {
  return service({ url, data, method: 'post', ...config })
}
service.put = (url, data, config = {}) => {
  return service({ url, data, method: 'put', ...config })
}
service.delete = (url, params) => {
  // 支持 { data: ... } 参数格式
  if (params && params.data) {
    return service({ url, data: params.data, method: 'delete' })
  }
  return service({ url, params, method: 'delete' })
}

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('access_token')
    if (token) {
      // 确保 token 以 Bearer 开头
      if (!token.startsWith('Bearer ')) {
        config.headers['Authorization'] = 'Bearer ' + token
      } else {
        config.headers['Authorization'] = token
      }
    }
    // 如果是 FormData（文件上传），删除默认的 Content-Type，让 axios 自动设置 multipart/form-data 和 boundary
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    // 开发环境日志
    if (import.meta.env.DEV) {
      console.log('[Request]', config.method.toUpperCase(), config.url, config.params || '')
    }
    return config
  },
  error => {
    console.error('[Request Error]', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 如果是 blob 类型（文件下载），直接返回
    if (response.config.responseType === 'blob' || response.headers['content-type']?.includes('application/vnd.openxmlformats-officedocument')) {
      return response.data
    }

    const res = response.data

    // 如果返回的状态码不是 200，说明接口有错误
    if (res.code !== 200) {
      // 401: 未登录或 token 过期
      if (res.code === 401) {
        // 防止重复弹出
        const isRelogin = localStorage.getItem('is_relogin')
        if (!isRelogin) {
          localStorage.setItem('is_relogin', 'true')
          ElMessageBox.confirm('登录状态已过期，请重新登录', '提示', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning',
            closeOnClickModal: false,
            showClose: false
          }).then(() => {
            localStorage.removeItem('is_relogin')
            localStorage.removeItem('access_token')
            localStorage.removeItem('user_info')
            location.href = '/login'
          }).catch(() => {
            localStorage.removeItem('is_relogin')
          })
        }
      } else {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    console.error('[Response Error]', error)
    let message = '网络错误'

    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未授权，请重新登录'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        case 504:
          message = '网关超时'
          break
        default:
          message = error.message || '网络错误'
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时'
    }

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service
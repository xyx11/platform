/**
 * 通用工具函数
 */

/**
 * 解析时间
 * @param {string|number|Date} time - 时间
 * @param {string} pattern - 格式化模板
 * @returns {string} 格式化后的时间字符串
 */
export function parseTime(time, pattern = '{y}-{m}-{d} {h}:{i}:{s}') {
  if (!time) return ''

  let date
  if (typeof time === 'string') {
    date = new Date(time.replace(/-/g, '/'))
  } else if (typeof time === 'number') {
    date = new Date(time)
  } else {
    date = time
  }

  if (isNaN(date.getTime())) return ''

  let format = pattern || '{y}-{m}-{d} {h}:{i}:{s}'
  const o = {
    'm+': date.getMonth() + 1,
    'd+': date.getDate(),
    'h+': date.getHours(),
    'i+': date.getMinutes(),
    's+': date.getSeconds(),
    'q+': Math.floor((date.getMonth() + 3) / 3),
    S: date.getMilliseconds()
  }

  if (/(y+)/.test(format)) {
    format = format.replace(
      RegExp.$1,
      (date.getFullYear() + '').substr(4 - RegExp.$1.length)
    )
  }

  for (const k in o) {
    if (new RegExp('(' + k + ')').test(format)) {
      format = format.replace(
        RegExp.$1,
        RegExp.$1.length === 1
          ? o[k]
          : ('00' + o[k]).substr(('' + o[k]).length)
      )
    }
  }

  return format
}

/**
 * 格式化时间
 * @param {Date|string|number} time - 时间
 * @returns {string} 格式化后的字符串
 */
export function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = (now.getTime() - date.getTime()) / 1000

  if (diff < 60) {
    return '刚刚'
  } else if (diff < 3600) {
    return Math.floor(diff / 60) + '分钟前'
  } else if (diff < 86400) {
    return Math.floor(diff / 3600) + '小时前'
  } else if (diff < 172800) {
    return '昨天'
  } else if (diff < 2592000) {
    return Math.floor(diff / 86400) + '天前'
  } else {
    return parseTime(time, '{y}-{m}-{d}')
  }
}

/**
 * 防抖
 * @param {Function} func - 函数
 * @param {number} wait - 等待时间
 * @returns {Function}
 */
export function debounce(func, wait = 500) {
  let timeout
  return function () {
    const context = this
    const args = arguments
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      func.apply(context, args)
    }, wait)
  }
}

/**
 * 节流
 * @param {Function} func - 函数
 * @param {number} wait - 等待时间
 * @returns {Function}
 */
export function throttle(func, wait = 500) {
  let timer = null
  return function () {
    const context = this
    const args = arguments
    if (!timer) {
      timer = setTimeout(() => {
        func.apply(context, args)
        timer = null
      }, wait)
    }
  }
}
/**
 * 下载 Excel 文件
 * @param {Blob} blob - Blob 数据
 * @param {string} filename - 文件名
 */
export function downloadExcel(blob, filename) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  window.URL.revokeObjectURL(url)
}

/**
 * 下载文件（通用）
 * @param {Blob} blob - Blob 数据
 * @param {string} filename - 文件名
 */
export function downloadFile(blob, filename) {
  downloadExcel(blob, filename)
}

/**
 * 格式化字节大小
 * @param {number} bytes - 字节数
 * @param {string} targetUnit - 目标单位（可选，如 'GB'）
 * @returns {string} 格式化后的大小
 */
export function formatBytes(bytes, targetUnit = null) {
  if (bytes === null || bytes === undefined || bytes === 0) return '0 B'
  
  // 如果指定了目标单位，直接转换
  if (targetUnit === 'GB') {
    return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
  }
  
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(2) + ' ' + sizes[i]
}

/**
 * 根据百分比获取进度条颜色
 * @param {number} percentage - 百分比
 * @returns {string} 颜色值
 */
export function getProgressColor(percentage) {
  if (percentage < 60) return '#00b578'  // 绿色
  if (percentage < 80) return '#faad14'  // 橙色
  return '#ff4d4f'  // 红色
}

/**
 * 复制文本到剪贴板
 * @param {string} text - 要复制的文本
 * @returns {Promise<boolean>} 是否成功
 */
export async function copyToClipboard(text) {
  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch (err) {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    try {
      document.execCommand('copy')
      return true
    } catch {
      return false
    } finally {
      document.body.removeChild(textarea)
    }
  }
}

/**
 * 深拷贝对象
 * @param {Object} obj - 要拷贝的对象
 * @returns {Object} 拷贝后的对象
 */
export function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj)
  if (obj instanceof Array) return obj.map(item => deepClone(item))
  if (obj instanceof Object) {
    const copy = {}
    Object.keys(obj).forEach(key => {
      copy[key] = deepClone(obj[key])
    })
    return copy
  }
  return obj
}

/**
 * 对象转 URL 参数
 * @param {Object} obj - 对象
 * @returns {string} URL 参数字符串
 */
export function toQueryString(obj) {
  if (!obj) return ''
  const params = Object.entries(obj)
    .filter(([_, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => {
      if (Array.isArray(value)) {
        return value.map(v => `${encodeURIComponent(key)}=${encodeURIComponent(v)}`).join('&')
      }
      return `${encodeURIComponent(key)}=${encodeURIComponent(value)}`
    })
    .join('&')
  return params ? `?${params}` : ''
}

/**
 * 格式化金额
 * @param {number} amount - 金额
 * @param {string} symbol - 货币符号
 * @param {number} decimals - 小数位数
 * @returns {string} 格式化后的金额
 */
export function formatMoney(amount, symbol = '¥', decimals = 2) {
  if (amount === null || amount === undefined) return symbol + '0.00'
  const num = parseFloat(amount)
  if (isNaN(num)) return symbol + '0.00'
  return symbol + num.toFixed(decimals).replace(/\d(?=(\d{3})+\.)/g, '$&,')
}

/**
 * 验证手机号
 * @param {string} phone - 手机号
 * @returns {boolean} 是否有效
 */
export function validatePhone(phone) {
  return /^1[3-9]\d{9}$/.test(phone)
}

/**
 * 验证邮箱
 * @param {string} email - 邮箱
 * @returns {boolean} 是否有效
 */
export function validateEmail(email) {
  return /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email)
}

/**
 * 验证身份证
 * @param {string} idCard - 身份证号
 * @returns {boolean} 是否有效
 */
export function validateIdCard(idCard) {
  return /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(idCard)
}

/**
 * 生成 UUID
 * @returns {string} UUID
 */
export function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

/**
 * 睡眠/延迟
 * @param {number} ms - 毫秒数
 * @returns {Promise}
 */
export function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 重试执行函数
 * @param {Function} fn - 要执行的函数
 * @param {number} retries - 重试次数
 * @param {number} delay - 延迟时间
 * @returns {Promise}
 */
export async function retry(fn, retries = 3, delay = 1000) {
  try {
    return await fn()
  } catch (err) {
    if (retries === 0) throw err
    await sleep(delay)
    return retry(fn, retries - 1, delay * 2)
  }
}

/**
 * 获取 URL 参数
 * @param {string} name - 参数名
 * @returns {string|null} 参数值
 */
export function getUrlParam(name) {
  const params = new URLSearchParams(window.location.search)
  return params.get(name)
}

/**
 * 格式化百分比
 * @param {number} value - 值
 * @param {number} total - 总数
 * @returns {string} 百分比字符串
 */
export function formatPercentage(value, total) {
  if (!total) return '0%'
  return ((value / total) * 100).toFixed(2) + '%'
}

/**
 * 数组分块
 * @param {Array} array - 数组
 * @param {number} size - 每块大小
 * @returns {Array} 分块后的数组
 */
export function chunk(array, size = 1) {
  const result = []
  for (let i = 0; i < array.length; i += size) {
    result.push(array.slice(i, i + size))
  }
  return result
}

/**
 * 数组去重
 * @param {Array} array - 数组
 * @param {string} key - 去重键
 * @returns {Array} 去重后的数组
 */
export function uniqueBy(array, key) {
  const seen = new Map()
  return array.filter(item => {
    const k = item[key]
    if (seen.has(k)) return false
    seen.set(k, true)
    return true
  })
}

/**
 * 数组转树形结构
 * @param {Array} array - 扁平数组
 * @param {string} idKey - ID 键
 * @param {string} parentKey - 父 ID 键
 * @param {string} childrenKey - 子节点键
 * @returns {Array} 树形数组
 */
export function arrayToTree(array, idKey = 'id', parentKey = 'parentId', childrenKey = 'children') {
  const map = new Map()
  const result = []

  array.forEach(item => {
    map.set(item[idKey], { ...item, [childrenKey]: [] })
  })

  array.forEach(item => {
    const node = map.get(item[idKey])
    const parent = map.get(item[parentKey])

    if (parent) {
      parent[childrenKey].push(node)
    } else {
      result.push(node)
    }
  })

  return result
}

/**
 * 树形转数组
 * @param {Array} tree - 树形数组
 * @param {string} childrenKey - 子节点键
 * @returns {Array} 扁平数组
 */
export function treeToArray(tree, childrenKey = 'children') {
  const result = []

  function traverse(nodes) {
    nodes.forEach(node => {
      const { [childrenKey]: children, ...rest } = node
      result.push(rest)
      if (children && children.length) {
        traverse(children)
      }
    })
  }

  traverse(tree)
  return result
}

/**
 * 树形转数组
 * @param {Array} tree - 树形数组
 * @param {string} childrenKey - 子节点键
 * @returns {Array} 扁平数组
 */
export function treeToArray(tree, childrenKey = 'children') {
  const result = []

  function traverse(nodes) {
    nodes.forEach(node => {
      const { [childrenKey]: children, ...rest } = node
      result.push(rest)
      if (children && children.length) {
        traverse(children)
      }
    })
  }

  traverse(tree)
  return result
}

/**
 * 本地存储工具
 */
export const storage = {
  /**
   * 设置存储
   * @param {string} key - 键
   * @param {any} value - 值
   */
  set(key, value) {
    try {
      localStorage.setItem(key, JSON.stringify(value))
    } catch (e) {
      console.error('localStorage set error:', e)
    }
  },

  /**
   * 获取存储
   * @param {string} key - 键
   * @param {any} defaultValue - 默认值
   * @returns {any} 值
   */
  get(key, defaultValue = null) {
    try {
      const item = localStorage.getItem(key)
      return item ? JSON.parse(item) : defaultValue
    } catch (e) {
      console.error('localStorage get error:', e)
      return defaultValue
    }
  },

  /**
   * 移除存储
   * @param {string} key - 键
   */
  remove(key) {
    try {
      localStorage.removeItem(key)
    } catch (e) {
      console.error('localStorage remove error:', e)
    }
  },

  /**
   * 清空存储
   */
  clear() {
    try {
      localStorage.clear()
    } catch (e) {
      console.error('localStorage clear error:', e)
    }
  }
}

/**
 * 会话存储工具
 */
export const session = {
  /**
   * 设置存储
   * @param {string} key - 键
   * @param {any} value - 值
   */
  set(key, value) {
    try {
      sessionStorage.setItem(key, JSON.stringify(value))
    } catch (e) {
      console.error('sessionStorage set error:', e)
    }
  },

  /**
   * 获取存储
   * @param {string} key - 键
   * @param {any} defaultValue - 默认值
   * @returns {any} 值
   */
  get(key, defaultValue = null) {
    try {
      const item = sessionStorage.getItem(key)
      return item ? JSON.parse(item) : defaultValue
    } catch (e) {
      console.error('sessionStorage get error:', e)
      return defaultValue
    }
  },

  /**
   * 移除存储
   * @param {string} key - 键
   */
  remove(key) {
    try {
      sessionStorage.removeItem(key)
    } catch (e) {
      console.error('sessionStorage remove error:', e)
    }
  },

  /**
   * 清空存储
   */
  clear() {
    try {
      sessionStorage.clear()
    } catch (e) {
      console.error('sessionStorage clear error:', e)
    }
  }
}

/**
 * 获取滚动条宽度
 * @returns {number} 滚动条宽度
 */
export function getScrollbarWidth() {
  const scrollDiv = document.createElement('div')
  scrollDiv.style.cssText = 'width: 99px; height: 99px; overflow: scroll; position: absolute; top: -9999px;'
  document.body.appendChild(scrollDiv)
  const scrollbarWidth = scrollDiv.offsetWidth - scrollDiv.clientWidth
  document.body.removeChild(scrollDiv)
  return scrollbarWidth
}

/**
 * 判断是否在全屏模式
 * @returns {boolean} 是否全屏
 */
export function isFullscreen() {
  return !!(document.fullscreenElement || document.mozFullScreenElement || document.webkitFullscreenElement)
}

/**
 * 切换全屏
 */
export function toggleFullscreen() {
  if (isFullscreen()) {
    document.exitFullscreen?.() || document.mozCancelFullScreen?.() || document.webkitExitFullscreen?.()
  } else {
    document.documentElement.requestFullscreen?.() ||
    document.documentElement.mozRequestFullScreen?.() ||
    document.documentElement.webkitRequestFullscreen?.()
  }
}

/**
 * 进入全屏
 */
export function enterFullscreen() {
  if (!isFullscreen()) {
    toggleFullscreen()
  }
}

/**
 * 退出全屏
 */
export function exitFullscreen() {
  if (isFullscreen()) {
    toggleFullscreen()
  }
}

/**
 * 获取滚动位置
 * @returns {Object} {scrollTop, scrollLeft}
 */
export function getScrollPosition() {
  return {
    scrollTop: window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0,
    scrollLeft: window.pageXOffset || document.documentElement.scrollLeft || document.body.scrollLeft || 0
  }
}

/**
 * 平滑滚动到顶部
 * @param {number} duration - 滚动时间（毫秒）
 */
export function scrollToTop(duration = 300) {
  const start = window.pageYOffset
  const change = -start
  const startTime = performance.now()

  function animateScroll(currentTime) {
    const elapsedTime = currentTime - startTime
    const progress = Math.min(elapsedTime / duration, 1)
    const ease = easeInOutCubic(progress)
    
    window.scrollTo(0, start + change * ease)
    
    if (elapsedTime < duration) {
      requestAnimationFrame(animateScroll)
    }
  }

  requestAnimationFrame(animateScroll)
}

/**
 * 缓动函数
 */
function easeInOutCubic(t) {
  return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2
}

/**
 * 图片懒加载
 * @param {HTMLElement} img - 图片元素
 * @param {string} src - 图片地址
 */
export function lazyLoadImage(img, src) {
  const image = new Image()
  image.src = src
  image.onload = () => {
    img.src = src
  }
}

/**
 * 图片预加载
 * @param {string[]} urls - 图片地址数组
 * @returns {Promise}
 */
export function preloadImages(urls) {
  return Promise.all(urls.map(url => {
    return new Promise((resolve, reject) => {
      const img = new Image()
      img.src = url
      img.onload = () => resolve(url)
      img.onerror = () => reject(url)
    })
  }))
}

/**
 * 压缩图片
 * @param {File} file - 图片文件
 * @param {number} quality - 压缩质量（0-1）
 * @returns {Promise<Blob>}
 */
export function compressImage(file, quality = 0.7) {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = (e) => {
      const img = new Image()
      img.src = e.target.result
      img.onload = () => {
        const canvas = document.createElement('canvas')
        canvas.width = img.width
        canvas.height = img.height
        const ctx = canvas.getContext('2d')
        ctx.drawImage(img, 0, 0)
        canvas.toBlob(
          (blob) => resolve(blob),
          file.type,
          quality
        )
      }
    }
  })
}

/**
 * 文件转 Base64
 * @param {File} file - 文件
 * @returns {Promise<string>}
 */
export function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = error => reject(error)
  })
}

/**
 * Base64 转 File
 * @param {string} base64 - Base64 字符串
 * @param {string} filename - 文件名
 * @param {string} mimeType - MIME 类型
 * @returns {File}
 */
export function base64ToFile(base64, filename, mimeType) {
  const arr = base64.split(',')
  const mime = arr[0].match(/:(.*?);/)[1]
  const bstr = atob(arr[1])
  let n = bstr.length
  const u8arr = new Uint8Array(n)
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n)
  }
  return new File([u8arr], filename, { type: mimeType || mime })
}

/**
 * 获取文件扩展名
 * @param {string} filename - 文件名
 * @returns {string} 扩展名
 */
export function getFileExtension(filename) {
  const parts = filename.split('.')
  return parts.length > 1 ? parts[parts.length - 1].toLowerCase() : ''
}

/**
 * 获取文件名（不含扩展名）
 * @param {string} filename - 文件名
 * @returns {string} 文件名
 */
export function getFileNameWithoutExtension(filename) {
  const lastDot = filename.lastIndexOf('.')
  return lastDot >= 0 ? filename.substring(0, lastDot) : filename
}

/**
 * 格式化数字（千分位）
 * @param {number} num - 数字
 * @returns {string} 格式化后的字符串
 */
export function formatNumber(num) {
  if (num === null || num === undefined) return '0'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 生成颜色
 * @param {string} str - 字符串
 * @returns {string} 颜色值
 */
export function stringToColor(str) {
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash)
  }
  const c = (hash & 0x00ffffff).toString(16).toUpperCase()
  return '#' + '00000'.substring(0, 6 - c.length) + c
}

/**
 * 生成随机颜色
 * @returns {string} 颜色值
 */
export function randomColor() {
  return '#' + Math.floor(Math.random() * 16777215).toString(16).padStart(6, '0')
}

/**
 * 判断是否为移动设备
 * @returns {boolean}
 */
export function isMobile() {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
}

/**
 * 判断是否为 iOS 设备
 * @returns {boolean}
 */
export function isIOS() {
  return /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream
}

/**
 * 判断是否为 Android 设备
 * @returns {boolean}
 */
export function isAndroid() {
  return /Android/.test(navigator.userAgent)
}

/**
 * 判断是否为微信浏览器
 * @returns {boolean}
 */
export function isWeChat() {
  return /MicroMessenger/i.test(navigator.userAgent)
}

/**
 * 获取设备信息
 * @returns {Object} 设备信息
 */
export function getDeviceInfo() {
  const userAgent = navigator.userAgent
  return {
    isMobile: isMobile(),
    isIOS: isIOS(),
    isAndroid: isAndroid(),
    isWeChat: isWeChat(),
    platform: navigator.platform,
    userAgent: userAgent
  }
}

/**
 * 复制到剪贴板（带提示）
 * @param {string} text - 要复制的文本
 * @param {string} successMessage - 成功提示
 * @param {string} errorMessage - 失败提示
 */
export async function copyWithMessage(text, successMessage = '已复制', errorMessage = '复制失败') {
  const success = await copyToClipboard(text)
  // 假设有 ElMessage 组件
  if (typeof ElMessage !== 'undefined') {
    ElMessage(success ? successMessage : errorMessage)
  }
  return success
}

/**
 * 下载 JSON 文件
 * @param {Object} data - 数据
 * @param {string} filename - 文件名
 */
export function downloadJson(data, filename = 'data.json') {
  const json = JSON.stringify(data, null, 2)
  const blob = new Blob([json], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

/**
 * 导出表格为 Excel
 * @param {Array} data - 表格数据
 * @param {string} filename - 文件名
 * @param {string} sheetName - 工作表名
 */
export function exportTableToExcel(data, filename = 'export.xlsx', sheetName = 'Sheet1') {
  // 需要 xlsx 库支持
  if (typeof XLSX === 'undefined') {
    console.error('XLSX library not found')
    return
  }
  const worksheet = XLSX.utils.json_to_sheet(data)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, sheetName)
  XLSX.writeFile(workbook, filename)
}

/**
 * 格式化相对时间
 * @param {Date|string|number} time - 时间
 * @returns {string} 相对时间字符串
 */
export function formatRelativeTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = (now - date) / 1000

  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  if (diff < 604800) return Math.floor(diff / 86400) + '天前'
  if (diff < 2592000) return Math.floor(diff / 604800) + '周前'
  if (diff < 31536000) return Math.floor(diff / 2592000) + '个月前'
  return Math.floor(diff / 31536000) + '年前'
}

/**
 * 脱敏手机号
 * @param {string} phone - 手机号
 * @returns {string} 脱敏后的手机号
 */
export function maskPhone(phone) {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 脱敏身份证号
 * @param {string} idCard - 身份证号
 * @returns {string} 脱敏后的身份证号
 */
export function maskIdCard(idCard) {
  if (!idCard || idCard.length < 14) return idCard
  return idCard.replace(/(\d{6})\d{8}(\w{4})/, '$1********$2')
}

/**
 * 脱敏邮箱
 * @param {string} email - 邮箱
 * @returns {string} 脱敏后的邮箱
 */
export function maskEmail(email) {
  if (!email) return ''
  const [username, domain] = email.split('@')
  if (!username || !domain) return email
  const maskedUsername = username.charAt(0) + '*'.repeat(username.length - 2) + username.charAt(username.length - 1)
  return maskedUsername + '@' + domain
}

/**
 * 解析 Cookie
 * @returns {Object} Cookie 对象
 */
export function parseCookies() {
  const cookies = {}
  document.cookie.split(';').forEach(cookie => {
    const [name, value] = cookie.trim().split('=')
    if (name) cookies[name] = decodeURIComponent(value || '')
  })
  return cookies
}

/**
 * 设置 Cookie
 * @param {string} name - 名称
 * @param {string} value - 值
 * @param {number} days - 过期天数
 */
export function setCookie(name, value, days = 7) {
  const date = new Date()
  date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000)
  document.cookie = `${name}=${encodeURIComponent(value)};expires=${date.toUTCString()};path=/`
}

/**
 * 删除 Cookie
 * @param {string} name - 名称
 */
export function deleteCookie(name) {
  document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/`
}

/**
 * 深度合并对象
 * @param {Object} target - 目标对象
 * @param {Object} source - 源对象
 * @returns {Object} 合并后的对象
 */
export function deepMerge(target, source) {
  const output = Object.assign({}, target)
  if (isObject(target) && isObject(source)) {
    Object.keys(source).forEach(key => {
      if (isObject(source[key])) {
        if (!(key in target)) {
          Object.assign(output, { [key]: source[key] })
        } else {
          output[key] = deepMerge(target[key], source[key])
        }
      } else {
        Object.assign(output, { [key]: source[key] })
      }
    })
  }
  return output
}

/**
 * 判断是否为对象
 * @param {any} item - 要判断的值
 * @returns {boolean}
 */
export function isObject(item) {
  return item && typeof item === 'object' && !Array.isArray(item)
}

/**
 * 对象深度比较
 * @param {Object} obj1 - 对象 1
 * @param {Object} obj2 - 对象 2
 * @returns {boolean} 是否相等
 */
export function deepEqual(obj1, obj2) {
  if (obj1 === obj2) return true
  if (obj1 === null || obj2 === null) return false
  if (typeof obj1 !== 'object' || typeof obj2 !== 'object') return false
  if (Array.isArray(obj1) !== Array.isArray(obj2)) return false
  
  const keys1 = Object.keys(obj1)
  const keys2 = Object.keys(obj2)
  
  if (keys1.length !== keys2.length) return false
  
  for (const key of keys1) {
    if (!keys2.includes(key)) return false
    if (!deepEqual(obj1[key], obj2[key])) return false
  }
  
  return true
}

/**
 * 休眠/延迟（Promise 版本）
 * @param {number} ms - 毫秒数
 * @returns {Promise}
 */
export function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 超时控制
 * @param {Promise} promise - 原始 Promise
 * @param {number} timeout - 超时时间（毫秒）
 * @param {string} message - 超时错误信息
 * @returns {Promise}
 */
export function withTimeout(promise, timeout, message = 'Request timeout') {
  return Promise.race([
    promise,
    new Promise((_, reject) => setTimeout(() => reject(new Error(message)), timeout))
  ])
}

/**
 * 并发限制
 * @param {Array} tasks - 任务数组
 * @param {number} limit - 并发数量
 * @returns {Promise}
 */
export async function runWithConcurrency(tasks, limit) {
  const results = []
  const running = []
  
  for (const task of tasks) {
    const promise = Promise.resolve().then(task)
    running.push(promise)
    
    const result = await promise
    results.push(result)
    
    if (running.length >= limit) {
      await Promise.race(running)
      const index = running.indexOf(promise)
      if (index > -1) running.splice(index, 1)
    }
  }
  
  await Promise.all(running)
  return results
}

/**
 * 获取 URL 参数对象
 * @returns {Object} 参数对象
 */
export function getUrlParams() {
  const params = new URLSearchParams(window.location.search)
  const result = {}
  params.forEach((value, key) => {
    result[key] = value
  })
  return result
}

/**
 * 添加/更新 URL 参数
 * @param {string} url - URL
 * @param {Object} params - 参数对象
 * @returns {string} 新的 URL
 */
export function addUrlParams(url, params) {
  const urlObj = new URL(url, window.location.origin)
  Object.entries(params).forEach(([key, value]) => {
    urlObj.searchParams.set(key, value)
  })
  return urlObj.href
}

/**
 * 移除 URL 参数
 * @param {string} url - URL
 * @param {string|string[]} paramNames - 参数名
 * @returns {string} 新的 URL
 */
export function removeUrlParams(url, paramNames) {
  const names = Array.isArray(paramNames) ? paramNames : [paramNames]
  const urlObj = new URL(url, window.location.origin)
  names.forEach(name => {
    urlObj.searchParams.delete(name)
  })
  return urlObj.href
}

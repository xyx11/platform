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

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

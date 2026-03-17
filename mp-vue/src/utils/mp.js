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

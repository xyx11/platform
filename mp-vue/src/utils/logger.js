/**
 * 统一的日志工具函数
 * 封装 console.log/error/warn，支持生产环境禁用
 */

/**
 * 检查是否开发环境
 */
function isDev() {
  return import.meta.env?.DEV || process.env?.NODE_ENV === 'development'
}

/**
 * 日志工具
 */
export const logger = {
  /**
   * 普通日志
   */
  log(...args) {
    if (isDev()) {
      console.log(...args)
    }
  },

  /**
   * 错误日志 - 始终输出，便于排查问题
   */
  error(...args) {
    console.error(...args)
  },

  /**
   * 警告日志
   */
  warn(...args) {
    console.warn(...args)
  },

  /**
   * 信息日志
   */
  info(...args) {
    if (isDev()) {
      console.info(...args)
    }
  },

  /**
   * 调试日志
   */
  debug(...args) {
    if (isDev()) {
      console.debug(...args)
    }
  }
}

/**
 * 简化导出
 */
export const log = logger.log
export const logError = logger.error
export const logWarn = logger.warn
export const logInfo = logger.info

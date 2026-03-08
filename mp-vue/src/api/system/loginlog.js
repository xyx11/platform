import request from '@/utils/request'

// 获取登录日志列表
export function getLoginLogList(params) {
  return request({
    url: '/system/loginlog/list',
    method: 'get',
    params
  })
}

// 删除登录日志
export function deleteLoginLog(logId) {
  return request({
    url: '/system/loginlog/' + logId,
    method: 'delete'
  })
}

// 批量删除登录日志
export function batchDeleteLoginLog(logIds) {
  return request({
    url: '/system/loginlog/batch',
    method: 'delete',
    data: logIds
  })
}

// 清空登录日志
export function clearLoginLog() {
  return request({
    url: '/system/loginlog/clear',
    method: 'delete'
  })
}

// 获取登录日志统计
export function getLoginLogStats() {
  return request({
    url: '/system/loginlog/stats',
    method: 'get'
  })
}

// 导出登录日志
export function exportLoginLog(params) {
  return request({
    url: '/system/loginlog/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
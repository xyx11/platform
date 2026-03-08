import request from '@/utils/request'

// 获取操作日志列表
export function getOperationLogList(params) {
  return request({
    url: '/system/log/list',
    method: 'get',
    params
  })
}

// 删除操作日志
export function deleteOperationLog(id) {
  return request({
    url: '/system/log/' + id,
    method: 'delete'
  })
}

// 批量删除操作日志
export function batchDeleteOperationLog(ids) {
  return request({
    url: '/system/log/batch',
    method: 'delete',
    data: ids
  })
}

// 获取操作日志统计
export function getOperationLogStats() {
  return request({
    url: '/system/log/stats',
    method: 'get'
  })
}

// 导出操作日志
export function exportOperationLog(params) {
  return request({
    url: '/system/log/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
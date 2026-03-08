import request from '@/utils/request'

// 查询命令执行记录列表
export function listCommand(query) {
  return request({
    url: '/system/command/list',
    method: 'get',
    params: query
  })
}

// 查询命令执行记录详细
export function getCommand(id) {
  return request({
    url: '/system/command/' + id,
    method: 'get'
  })
}

// 执行命令
export function executeCommand(data) {
  return request({
    url: '/system/command/execute',
    method: 'post',
    data: data
  })
}

// 删除命令执行记录
export function delCommand(id) {
  return request({
    url: '/system/command/' + id,
    method: 'delete'
  })
}

// 批量删除命令执行记录
export function batchDelCommand(ids) {
  return request({
    url: '/system/command/batch',
    method: 'delete',
    data: ids
  })
}

// 清空命令执行记录
export function clearCommand() {
  return request({
    url: '/system/command/clear',
    method: 'delete'
  })
}

// 获取命令执行统计信息
export function getCommandStats() {
  return request({
    url: '/system/command/stats',
    method: 'get'
  })
}
import request from '@/utils/request'

// 获取在线用户列表
export function getOnlineList() {
  return request({
    url: '/monitor/online/list',
    method: 'get'
  })
}

// 强制下线
export function forceLogout(token) {
  return request({
    url: '/monitor/online/' + token,
    method: 'delete'
  })
}

// 获取 Redis 信息
export function getRedisInfo() {
  return request({
    url: '/monitor/redis',
    method: 'get'
  })
}

// 获取服务器信息
export function getServerInfo() {
  return request({
    url: '/monitor/server',
    method: 'get'
  })
}

// 获取缓存统计
export function getCacheStats() {
  return request({
    url: '/monitor/cache',
    method: 'get'
  })
}
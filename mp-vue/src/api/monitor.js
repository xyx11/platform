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

// 清除指定缓存
export function clearCache(cacheName) {
  return request({
    url: '/monitor/cache/' + cacheName,
    method: 'delete'
  })
}

// 清除所有缓存
export function clearAllCache() {
  return request({
    url: '/monitor/cache/all',
    method: 'delete'
  })
}

// 获取缓存键列表
export function getCacheKeys(params) {
  return request({
    url: '/monitor/cache/keys',
    method: 'get',
    params
  })
}

// 获取健康检查状态
export function getHealthStatus() {
  return request({
    url: '/monitor/health',
    method: 'get'
  })
}

// 获取线程详细信息
export function getThreads() {
  return request({
    url: '/monitor/threads',
    method: 'get'
  })
}

// 获取数据库连接池统计
export function getDatasource() {
  return request({
    url: '/monitor/datasource',
    method: 'get'
  })
}
import request from '@/utils/request'

// 获取系统健康检查
export function getHealth() {
  return request({
    url: '/monitor/health',
    method: 'get'
  })
}

// 获取线程监控
export function getThreads() {
  return request({
    url: '/monitor/threads',
    method: 'get'
  })
}

// 获取数据库连接池监控
export function getDatasource() {
  return request({
    url: '/monitor/datasource',
    method: 'get'
  })
}
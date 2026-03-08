import request from '@/utils/request'

// 查询待办事项列表
export function listTodo(query) {
  return request({
    url: '/system/todo/list',
    method: 'get',
    params: query
  })
}

// 获取我的待办列表
export function listMyTodo(query) {
  return request({
    url: '/system/todo/my/list',
    method: 'get',
    params: query
  })
}

// 查询待办事项详细
export function getTodo(id) {
  return request({
    url: '/system/todo/' + id,
    method: 'get'
  })
}

// 新增待办事项
export function addTodo(data) {
  return request({
    url: '/system/todo',
    method: 'post',
    data: data
  })
}

// 修改待办事项
export function updateTodo(data) {
  return request({
    url: '/system/todo',
    method: 'put',
    data: data
  })
}

// 完成待办
export function completeTodo(id) {
  return request({
    url: '/system/todo/complete/' + id,
    method: 'put'
  })
}

// 取消待办
export function cancelTodo(id) {
  return request({
    url: '/system/todo/cancel/' + id,
    method: 'put'
  })
}

// 删除待办事项
export function delTodo(id) {
  return request({
    url: '/system/todo/' + id,
    method: 'delete'
  })
}

// 批量删除待办事项
export function batchDelTodo(ids) {
  return request({
    url: '/system/todo/batch',
    method: 'delete',
    data: ids
  })
}

// 获取待办统计信息
export function getTodoStats() {
  return request({
    url: '/system/todo/stats',
    method: 'get'
  })
}
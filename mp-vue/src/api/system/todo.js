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
    url: '/system/todo/my-list',
    method: 'get',
    params: query
  })
}

// 获取已办事项列表
export function listDoneTodo(query) {
  return request({
    url: '/system/todo/done',
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

// 删除待办事项（移动到回收站）
export function delTodo(id) {
  return request({
    url: '/system/todo/delete/' + id,
    method: 'delete'
  })
}

// 批量删除待办事项（移动到回收站）
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

// 获取即将到期的待办
export function getExpiringTodos(days) {
  return request({
    url: '/system/todo/expiring',
    method: 'get',
    params: { days }
  })
}

// 获取逾期待办列表
export function getOverdueTodos() {
  return request({
    url: '/system/todo/overdue',
    method: 'get'
  })
}

// 批量完成待办
export function batchCompleteTodo(ids) {
  return request({
    url: '/system/todo/batch-complete',
    method: 'put',
    data: ids
  })
}

// ============ 标签管理 ============
export function listTodoTags() {
  return request({ url: '/system/todo/tag/list', method: 'get' })
}

export function getTodoTag(id) {
  return request({ url: '/system/todo/tag/' + id, method: 'get' })
}

export function addTodoTag(data) {
  return request({ url: '/system/todo/tag', method: 'post', data: data })
}

export function updateTodoTag(data) {
  return request({ url: '/system/todo/tag', method: 'put', data: data })
}

export function delTodoTag(id) {
  return request({ url: '/system/todo/tag/' + id, method: 'delete' })
}

export function addTagsToTodo(todoId, tagIds) {
  return request({
    url: '/system/todo/tag/todo/' + todoId,
    method: 'post',
    data: tagIds
  })
}

export function removeTagsFromTodo(todoId, tagIds) {
  return request({
    url: '/system/todo/tag/todo/' + todoId,
    method: 'delete',
    data: tagIds
  })
}

export function getTagsByTodoId(todoId) {
  return request({ url: '/system/todo/tag/todo/' + todoId, method: 'get' })
}

// ============ 评论管理 ============
export function listComments(todoId) {
  return request({ url: '/system/todo/comment/list', method: 'get', params: { todoId } })
}

export function getCommentCount(todoId) {
  return request({ url: '/system/todo/comment/count', method: 'get', params: { todoId } })
}

export function addComment(data) {
  return request({ url: '/system/todo/comment', method: 'post', data: data })
}

export function updateComment(data) {
  return request({ url: '/system/todo/comment', method: 'put', data: data })
}

export function delComment(id) {
  return request({ url: '/system/todo/comment/' + id, method: 'delete' })
}

// ============ 附件管理 ============
export function listAttachments(todoId) {
  return request({ url: '/system/todo/attachment/list', method: 'get', params: { todoId } })
}

export function getAttachmentCount(todoId) {
  return request({ url: '/system/todo/attachment/count', method: 'get', params: { todoId } })
}

export function uploadAttachment(todoId, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/system/todo/attachment/upload',
    method: 'post',
    params: { todoId },
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function downloadAttachment(id) {
  return request({
    url: '/system/todo/attachment/download/' + id,
    method: 'get',
    responseType: 'blob'
  })
}

export function delAttachment(id) {
  return request({ url: '/system/todo/attachment/' + id, method: 'delete' })
}

// ============ 回收站管理 ============
export function listRecycleBin(query) {
  return request({ url: '/system/todo/recycle-bin/list', method: 'get', params: query })
}

export function moveToRecycleBin(todoId) {
  return request({ url: '/system/todo/recycle-bin/move/' + todoId, method: 'post' })
}

export function recoverTodo(recycleId) {
  return request({ url: '/system/todo/recycle-bin/recover/' + recycleId, method: 'post' })
}

export function deletePermanently(recycleId) {
  return request({ url: '/system/todo/recycle-bin/' + recycleId, method: 'delete' })
}

export function clearRecycleBin() {
  return request({ url: '/system/todo/recycle-bin/clear', method: 'delete' })
}
import request from '@/utils/request'

// 获取待办任务列表
export function listTodo(params) {
  return request({
    url: '/system/workflow/task/todo',
    method: 'get',
    params
  })
}

// 获取已办任务列表
export function listDone(params) {
  return request({
    url: '/system/workflow/task/done',
    method: 'get',
    params
  })
}

// 获取任务详情
export function getTask(taskId) {
  return request({
    url: '/system/workflow/task/' + taskId,
    method: 'get'
  })
}

// 完成任务
export function completeTask(taskId, data) {
  return request({
    url: '/system/workflow/task/' + taskId + '/complete',
    method: 'post',
    data
  })
}

// 转办任务
export function transferTask(taskId, newAssignee) {
  return request({
    url: '/system/workflow/task/' + taskId + '/transfer',
    method: 'post',
    params: { newAssignee }
  })
}

// 委派任务
export function delegateTask(taskId, delegateUser) {
  return request({
    url: '/system/workflow/task/' + taskId + '/delegate',
    method: 'post',
    params: { delegateUser }
  })
}

// 获取任务历史
export function getTaskHistory(processInstanceId) {
  return request({
    url: '/system/workflow/task/' + processInstanceId + '/history',
    method: 'get'
  })
}

// 获取任务统计
export function getTaskStats() {
  return request({
    url: '/system/workflow/task/stats',
    method: 'get'
  })
}

// ============ 工作流统计报表 ============
// 获取统计概览
export function getOverviewStats() {
  return request({
    url: '/system/workflow/stats/overview',
    method: 'get'
  })
}

// 获取任务趋势统计
export function getTaskTrend(params) {
  return request({
    url: '/system/workflow/stats/trend',
    method: 'get',
    params
  })
}

// 获取流程定义统计
export function getProcessDefinitionStats() {
  return request({
    url: '/system/workflow/stats/process-definition',
    method: 'get'
  })
}

// 获取任务耗时统计
export function getTaskDuration(processInstanceId) {
  return request({
    url: '/system/workflow/stats/duration',
    method: 'get',
    params: { processInstanceId }
  })
}

// 获取用户任务完成情况
export function getCompletionStats(params) {
  return request({
    url: '/system/workflow/stats/completion',
    method: 'get',
    params
  })
}

// ============ 通知管理 ============
// 发送测试 WebSocket 通知
export function sendWebSocketTest() {
  return request({
    url: '/system/notification/websocket/test',
    method: 'post'
  })
}

// 发送测试邮件
export function sendEmailTest(email) {
  return request({
    url: '/system/notification/email/test',
    method: 'post',
    params: { email }
  })
}

// 获取通知配置
export function getNotificationConfig() {
  return request({
    url: '/system/notification/config',
    method: 'get'
  })
}
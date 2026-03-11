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
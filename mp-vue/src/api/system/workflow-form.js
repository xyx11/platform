import request from '@/utils/request'

// 获取表单绑定列表
export function getFormBindingList(params) {
  return request({
    url: '/system/workflow-form/list',
    method: 'get',
    params
  })
}

// 获取流程的所有表单绑定
export function getProcessBindings(processDefinitionKey) {
  return request({
    url: '/system/workflow-form/process/' + processDefinitionKey,
    method: 'get'
  })
}

// 绑定表单
export function bindForm(data) {
  return request({
    url: '/system/workflow-form/bind',
    method: 'post',
    data
  })
}

// 解除绑定
export function unbindForm(params) {
  return request({
    url: '/system/workflow-form/unbind',
    method: 'post',
    params
  })
}

// 获取可用的表单列表
export function getAvailableForms(processDefinitionKey) {
  return request({
    url: '/system/workflow-form/available/' + processDefinitionKey,
    method: 'get'
  })
}
// 获取表单定义列表
export function getFormDefinitionList(params) {
  return request({
    url: '/system/form-definition/list',
    method: 'get',
    params
  })
}

// 更新表单绑定状态
export function updateFormStatus(data) {
  return request({
    url: '/system/workflow-form/status',
    method: 'post',
    data
  })
}

// 批量删除表单绑定
export function batchDeleteFormBindings(data) {
  return request({
    url: '/system/workflow-form/batch-delete',
    method: 'post',
    data
  })
}

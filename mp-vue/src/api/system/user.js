import request from '@/utils/request'

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/system/user/list',
    method: 'get',
    params
  })
}

// 获取用户详情
export function getUserDetail(userId) {
  return request({
    url: '/system/user/detail/' + userId,
    method: 'get'
  })
}

// 新增用户
export function addUser(data) {
  return request({
    url: '/system/user',
    method: 'post',
    data
  })
}

// 修改用户
export function updateUser(data) {
  return request({
    url: '/system/user',
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(userId) {
  return request({
    url: '/system/user/' + userId,
    method: 'delete'
  })
}

// 批量删除用户
export function batchDeleteUser(userIds) {
  return request({
    url: '/system/user/batch',
    method: 'delete',
    data: userIds
  })
}

// 重置密码
export function resetPassword(data) {
  return request({
    url: '/system/user/password',
    method: 'put',
    data
  })
}

// 批量重置密码
export function batchResetPassword(data) {
  return request({
    url: '/system/user/password/batch',
    method: 'put',
    data
  })
}

// 解锁用户
export function unlockUser(userId) {
  return request({
    url: '/system/user/unlock/' + userId,
    method: 'post'
  })
}

// 批量解锁用户
export function batchUnlockUsers(userIds) {
  return request({
    url: '/system/user/unlock/batch',
    method: 'post',
    data: userIds
  })
}

// 获取用户角色
export function getUserRoles(userId) {
  return request({
    url: '/system/user/roles/' + userId,
    method: 'get'
  })
}

// 分配角色
export function assignRoles(userId, roleIds) {
  return request({
    url: '/system/user/roles?userId=' + userId,
    method: 'post',
    data: roleIds
  })
}

// 获取用户统计
export function getUserStats(userId) {
  return request({
    url: '/system/user/stats',
    method: 'get',
    params: { userId }
  })
}

// 导出用户数据
export function exportUser(params) {
  return request({
    url: '/system/user/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 下载导入模板
export function downloadTemplate() {
  return request({
    url: '/system/user/downloadTemplate',
    method: 'get',
    responseType: 'blob'
  })
}

// 导入用户数据
export function importUser(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/system/user/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
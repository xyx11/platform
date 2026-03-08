import request from '@/utils/request'

// 获取通知公告列表
export function getNoticeList(params) {
  return request({
    url: '/system/notice/list',
    method: 'get',
    params
  })
}

// 获取通知公告详情
export function getNotice(id) {
  return request({
    url: '/system/notice/' + id,
    method: 'get'
  })
}

// 新增通知公告
export function addNotice(data) {
  return request({
    url: '/system/notice',
    method: 'post',
    data
  })
}

// 修改通知公告
export function updateNotice(data) {
  return request({
    url: '/system/notice',
    method: 'put',
    data
  })
}

// 删除通知公告
export function deleteNotice(id) {
  return request({
    url: '/system/notice/' + id,
    method: 'delete'
  })
}

// 批量删除通知公告
export function batchDeleteNotice(ids) {
  return request({
    url: '/system/notice/batch',
    method: 'delete',
    data: ids
  })
}

// 获取通知公告统计
export function getNoticeStats() {
  return request({
    url: '/system/notice/stats',
    method: 'get'
  })
}

// 导出通知公告
export function exportNotice(params) {
  return request({
    url: '/system/notice/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 标记公告为已读
export function markAsRead(noticeId) {
  return request({
    url: '/system/notice/read/' + noticeId,
    method: 'post'
  })
}

// 批量标记公告为已读
export function batchMarkAsRead(noticeIds) {
  return request({
    url: '/system/notice/read/batch',
    method: 'post',
    data: noticeIds
  })
}

// 获取我的未读公告列表
export function getUnreadNotices(params) {
  return request({
    url: '/system/notice/unread/list',
    method: 'get',
    params
  })
}

// 获取未读公告数量
export function getUnreadCount() {
  return request({
    url: '/system/notice/unread/count',
    method: 'get'
  })
}
import request from '@/utils/request'

// 获取文件列表
export function getFileList(params) {
  return request({
    url: '/system/file/list',
    method: 'get',
    params
  })
}

// 条件查询文件列表
export function getFileListByCondition(params) {
  return request({
    url: '/system/file/listByCondition',
    method: 'get',
    params
  })
}

// 获取文件详情
export function getFileDetail(fileId) {
  return request({
    url: '/system/file/' + fileId,
    method: 'get'
  })
}

// 删除文件
export function deleteFile(fileId) {
  return request({
    url: '/system/file/' + fileId,
    method: 'delete'
  })
}

// 批量删除文件
export function batchDeleteFile(fileIds) {
  return request({
    url: '/system/file/batch',
    method: 'delete',
    data: fileIds
  })
}

// 获取文件统计
export function getFileStats() {
  return request({
    url: '/system/file/stats',
    method: 'get'
  })
}

// 导出文件数据
export function exportFile(params) {
  return request({
    url: '/system/file/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 上传文件
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/system/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
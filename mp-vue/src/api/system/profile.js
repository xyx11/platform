import request from '@/utils/request'

// 发送短信验证码
export function sendSmsCode(phone) {
  return request({
    url: '/system/user/sms/code',
    method: 'post',
    data: { phone }
  })
}

// 发送邮箱验证码
export function sendEmailCode(email) {
  return request({
    url: '/system/user/email/code',
    method: 'post',
    data: { email }
  })
}

// 修改手机号
export function changePhone(data) {
  return request({
    url: '/system/user/profile/phone',
    method: 'put',
    data
  })
}

// 修改邮箱
export function changeEmail(data) {
  return request({
    url: '/system/user/profile/email',
    method: 'put',
    data
  })
}

// 上传头像
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/system/user/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 修改个人资料
export function updateProfile(data) {
  return request({
    url: '/system/user/profile',
    method: 'put',
    data
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/system/user/profile/password',
    method: 'put',
    data
  })
}
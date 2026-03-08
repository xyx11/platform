import request from '@/utils/request'

// 登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 登出
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

// 获取验证码
export function getCaptcha() {
  return request({
    url: '/auth/captcha',
    method: 'get'
  })
}

// 发送找回密码验证码
export function sendResetPasswordCode(data) {
  return request({
    url: '/auth/reset-password/code',
    method: 'post',
    data
  })
}

// 重置密码
export function resetPassword(data) {
  return request({
    url: '/auth/reset-password',
    method: 'post',
    data
  })
}
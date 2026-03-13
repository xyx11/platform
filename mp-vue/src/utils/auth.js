// 认证工具函数

// 获取 token
export function getToken() {
  return localStorage.getItem('access_token')
}

// 设置 token
export function setToken(token) {
  return localStorage.setItem('access_token', token)
}

// 移除 token
export function removeToken() {
  return localStorage.removeItem('access_token')
}

// 获取用户信息
export function getUserInfo() {
  const info = localStorage.getItem('user_info')
  if (info) {
    return JSON.parse(info)
  }
  return null
}

// 设置用户信息
export function setUserInfo(info) {
  return localStorage.setItem('user_info', JSON.stringify(info))
}

// 移除用户信息
export function removeUserInfo() {
  return localStorage.removeItem('user_info')
}

// 清除所有认证信息
export function clearAuth() {
  removeToken()
  removeUserInfo()
}
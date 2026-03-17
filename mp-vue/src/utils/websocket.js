import { Client } from '@stomp/stompjs'
import { ElMessage } from 'element-plus'
import { logger } from './logger'

let stompClient = null
let reconnectAttempts = 0
const MAX_RECONNECT_ATTEMPTS = 5

// 连接 WebSocket
export function connectWebSocket(userId, onNotification) {
  if (stompClient && stompClient.active) {
    logger.log('WebSocket 已连接')
    return
  }

  const token = localStorage.getItem('access_token')
  if (!token) {
    logger.log('未登录，跳过 WebSocket 连接')
    return
  }

  // 构建 WebSocket URL
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/ws`

  stompClient = new Client({
    brokerURL: wsUrl,
    connectHeaders: {
      'Authorization': `Bearer ${token}`
    },
    debug: function (str) {
      logger.debug('STOMP: ' + str)
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000
  })

  stompClient.onConnect = function (frame) {
    logger.log('WebSocket 连接成功')
    reconnectAttempts = 0

    // 订阅个人通知
    stompClient.subscribe(`/queue/notification.${userId}`, function (message) {
      const notification = JSON.parse(message.body)
      logger.log('收到通知:', notification)
      handleNotification(notification, onNotification)
    })

    // 订阅全局通知（可选）
    stompClient.subscribe('/topic/notification', function (message) {
      const notification = JSON.parse(message.body)
      logger.log('收到全局通知:', notification)
      handleNotification(notification, onNotification)
    })
  }

  stompClient.onError = function (error) {
    logger.error('WebSocket 错误:', error)
  }

  stompClient.onDisconnect = function () {
    logger.log('WebSocket 断开连接')
    handleDisconnect(userId, onNotification)
  }

  stompClient.activate()
}

// 处理通知
function handleNotification(notification, onNotification) {
  const { title, message, type } = notification

  // 显示通知
  if (onNotification) {
    onNotification(notification)
  }

  // 显示消息提示
  const messageType = type === 'todo_assigned' ? 'warning' :
                      type === 'todo_reminder' ? 'info' :
                      type === 'todo_comment' ? 'success' : 'info'

  ElMessage({
    message: `${title}: ${message}`,
    type: messageType,
    duration: 5000,
    showClose: true
  })

  // 更新浏览器标题（可选）
  const originalTitle = document.title
  document.title = `【新消息】${originalTitle}`
  setTimeout(() => {
    document.title = originalTitle
  }, 3000)
}

// 处理断线重连
function handleDisconnect(userId, onNotification) {
  if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
    reconnectAttempts++
    logger.log(`尝试重连 WebSocket (${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})...`)
    setTimeout(() => {
      connectWebSocket(userId, onNotification)
    }, 3000)
  } else {
    logger.log('WebSocket 重连失败次数过多，停止重连')
    ElMessage.error('WebSocket 连接断开，请刷新页面重试')
  }
}

// 断开 WebSocket
export function disconnectWebSocket() {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
    logger.log('WebSocket 已断开')
  }
}

// 发送测试消息
export function sendTestNotification() {
  if (stompClient && stompClient.connected) {
    stompClient.publish({
      destination: '/app/notification/test',
      body: JSON.stringify({ message: 'Test message' })
    })
  }
}

// 检查连接状态
export function isConnected() {
  return stompClient && stompClient.connected
}

<template>
  <div class="websocket-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>WebSocket 消息推送</span>
          <div>
            <el-button :type="connected ? 'success' : 'danger'" @click="toggleConnection">
              {{ connected ? '已连接' : '断开连接' }}
            </el-button>
            <el-button type="primary" @click="dialogVisible = true">发送消息</el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <span>连接状态</span>
            </template>
            <el-descriptions :column="1">
              <el-descriptions-item label="连接状态">
                <el-tag :type="connected ? 'success' : 'danger'">
                  {{ connected ? '已连接' : '未连接' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="用户 ID">{{ userId }}</el-descriptions-item>
              <el-descriptions-item label="连接时间">{{ connectTime }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <span>消息统计</span>
            </template>
            <el-descriptions :column="1">
              <el-descriptions-item label="接收消息数">{{ receivedCount }}</el-descriptions-item>
              <el-descriptions-item label="发送消息数">{{ sentCount }}</el-descriptions-item>
              <el-descriptions-item label="最后消息时间">{{ lastMessageTime }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="hover" style="margin-top: 20px;">
        <template #header>
          <div class="message-header">
            <span>消息记录</span>
            <el-button size="small" @click="clearMessages">清空</el-button>
          </div>
        </template>
        <div class="message-list" ref="messageListRef">
          <div v-for="(msg, index) in messages" :key="index" :class="['message-item', msg.type]">
            <div class="message-time">{{ msg.time }}</div>
            <div class="message-content">
              <el-tag size="small" :type="msg.type === 'sent' ? 'primary' : 'info'">
                {{ msg.type === 'sent' ? '发送' : '接收' }}
              </el-tag>
              <span class="message-text">{{ msg.content }}</span>
            </div>
          </div>
          <el-empty v-if="messages.length === 0" description="暂无消息" />
        </div>
      </el-card>
    </el-card>

    <!-- 发送消息对话框 -->
    <el-dialog v-model="dialogVisible" title="发送消息" width="600px">
      <el-form ref="formRef" :model="messageForm" label-width="100px">
        <el-form-item label="消息类型">
          <el-radio-group v-model="messageForm.type">
            <el-radio label="user">点对点</el-radio>
            <el-radio label="broadcast">广播</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="接收用户 ID" v-if="messageForm.type === 'user'">
          <el-input v-model="messageForm.userId" placeholder="请输入接收用户 ID" />
        </el-form-item>
        <el-form-item label="消息内容" required>
          <el-input
            v-model="messageForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入消息内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="sendMessage" :loading="sending">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WebSocket">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import request from '@/utils/request'

const connected = ref(false)
const connectTime = ref('')
const userId = ref('')
const receivedCount = ref(0)
const sentCount = ref(0)
const lastMessageTime = ref('-')
const sending = ref(false)
const dialogVisible = ref(false)
const messages = ref([])
const messageListRef = ref(null)

const messageForm = reactive({
  type: 'user',
  userId: '',
  content: ''
})

let stompClient = null

// 连接 WebSocket
const connectWebSocket = () => {
  const token = localStorage.getItem('access_token')

  stompClient = new Client({
    webSocketFactory: () => new SockJS('/ws-endpoint'),
    connectHeaders: {
      Authorization: token
    },
    debug: (str) => console.log('[STOMP]', str),
    onConnect: () => {
      connected.value = true
      connectTime.value = new Date().toLocaleString()
      ElMessage.success('WebSocket 连接成功')

      // 订阅个人消息
      stompClient.subscribe('/user/queue/message', (message) => {
        handleMessage(message.body, 'received')
      })

      // 订阅广播消息
      stompClient.subscribe('/topic/broadcast', (message) => {
        handleMessage(message.body, 'received')
      })
    },
    onStompError: (frame) => {
      console.error('STOMP error:', frame)
      ElMessage.error('连接失败：' + frame.headers.message)
    },
    onDisconnect: () => {
      connected.value = false
      ElMessage.warning('WebSocket 连接断开')
    }
  })

  stompClient.activate()
}

// 断开连接
const disconnectWebSocket = () => {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
  }
  connected.value = false
  connectTime.value = ''
}

// 切换连接状态
const toggleConnection = () => {
  if (connected.value) {
    disconnectWebSocket()
  } else {
    connectWebSocket()
  }
}

// 处理接收的消息
const handleMessage = (body, type) => {
  try {
    const msg = JSON.parse(body)
    addMessage(msg.content || JSON.stringify(msg), type)
    receivedCount.value++
    lastMessageTime.value = new Date().toLocaleTimeString()
  } catch (e) {
    addMessage(body, type)
    receivedCount.value++
    lastMessageTime.value = new Date().toLocaleTimeString()
  }
}

// 添加消息记录
const addMessage = (content, type) => {
  messages.value.push({
    type,
    content,
    time: new Date().toLocaleTimeString()
  })
  // 滚动到底部
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

// 发送消息
const sendMessage = async () => {
  if (!messageForm.content) {
    ElMessage.warning('请输入消息内容')
    return
  }

  if (!connected.value) {
    ElMessage.warning('WebSocket 未连接')
    return
  }

  sending.value = true

  try {
    if (messageForm.type === 'broadcast') {
      // 发送广播消息
      stompClient.publish({
        destination: '/app/ws/broadcast',
        body: JSON.stringify({ content: messageForm.content })
      })
    } else {
      // 发送点对点消息
      if (!messageForm.userId) {
        ElMessage.warning('请输入接收用户 ID')
        return
      }
      stompClient.publish({
        destination: '/app/ws/send/' + messageForm.userId,
        body: JSON.stringify({
          userId: messageForm.userId,
          content: messageForm.content
        })
      })
    }

    addMessage(messageForm.content, 'sent')
    sentCount.value++
    lastMessageTime.value = new Date().toLocaleTimeString()

    ElMessage.success('发送成功')
    dialogVisible.value = false
    messageForm.content = ''
    messageForm.userId = ''
  } catch (error) {
    ElMessage.error('发送失败：' + error.message)
  } finally {
    sending.value = false
  }
}

// 清空消息
const clearMessages = () => {
  messages.value = []
}

// 获取当前用户 ID
const getCurrentUser = async () => {
  try {
    const res = await request({
      url: '/system/user/current',
      method: 'get'
    })
    userId.value = res.data?.id || '未知'
  } catch (e) {
    userId.value = '未知'
  }
}

onMounted(() => {
  getCurrentUser()
  connectWebSocket()
})

onUnmounted(() => {
  disconnectWebSocket()
})
</script>

<style scoped>
.websocket-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-list {
  height: 400px;
  overflow-y: auto;
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
}

.message-item {
  margin-bottom: 10px;
  padding: 10px;
  border-radius: 4px;
  background: #fff;
}

.message-item.sent {
  border-left: 3px solid #409eff;
}

.message-item.received {
  border-left: 3px solid #67c23a;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.message-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.message-text {
  flex: 1;
  word-break: break-all;
}
</style>
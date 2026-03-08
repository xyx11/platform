<template>
  <div class="notice-icon" @click="handleClick">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notice-badge">
      <el-icon :size="20" class="bell-icon"><Bell /></el-icon>
    </el-badge>

    <!-- 通知下拉面板 -->
    <div v-if="visible" class="notice-dropdown">
      <div class="dropdown-header">
        <span class="header-title">消息通知</span>
        <span class="header-actions">
          <el-link type="primary" :underline="false" @click="markAllAsRead" size="small">
            全部已读
          </el-link>
          <el-link type="info" :underline="false" @click="clearAll" size="small" style="margin-left: 12px;">
            清空
          </el-link>
        </span>
      </div>
      <div class="dropdown-content">
        <div v-if="loading" class="loading-content">
          <el-skeleton :rows="3" animated />
        </div>
        <div v-else-if="notifications.length === 0" class="empty-content">
          <el-empty :image-size="80" description="暂无通知" />
        </div>
        <div v-else class="notice-list">
          <div
            v-for="(item, index) in notifications"
            :key="index"
            class="notice-item"
            :class="{ 'is-read': item.isRead }"
            @click="handleNoticeClick(item)"
          >
            <div class="notice-avatar" v-if="item.avatar">
              <el-avatar :size="36" :src="item.avatar" />
            </div>
            <div class="notice-body">
              <div class="notice-title">{{ item.title }}</div>
              <div class="notice-desc">{{ item.desc }}</div>
              <div class="notice-time">{{ item.time }}</div>
            </div>
            <div class="notice-status">
              <el-tag v-if="item.type === 'info'" size="small" type="info">通知</el-tag>
              <el-tag v-else-if="item.type === 'warning'" size="small" type="warning">提醒</el-tag>
              <el-tag v-else-if="item.type === 'urgent'" size="small" type="danger">紧急</el-tag>
            </div>
          </div>
        </div>
      </div>
      <div class="dropdown-footer" @click="goToNoticePage">
        <span>查看全部</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, ArrowRight } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const visible = ref(false)
const unreadCount = ref(0)
const notifications = ref([])
const loading = ref(false)

const handleClick = () => {
  visible.value = !visible.value
  if (!visible.value && notifications.value.length === 0) {
    loadNotifications()
  }
}

const handleNoticeClick = (item) => {
  item.isRead = true
  updateUnreadCount()
}

const markAllAsRead = () => {
  notifications.value.forEach(item => {
    item.isRead = true
  })
  updateUnreadCount()
}

const clearAll = () => {
  notifications.value = []
  updateUnreadCount()
}

const updateUnreadCount = () => {
  unreadCount.value = notifications.value.filter(item => !item.isRead).length
}

const goToNoticePage = () => {
  router.push('/system/notice')
  visible.value = false
}

// 加载通知数据
const loadNotifications = () => {
  if (loading.value) return
  loading.value = true
  request.get('/system/notice/list', {
    params: { pageNum: 1, pageSize: 10 }
  }).then(res => {
    const list = res.data.rows || res.data || []
    notifications.value = list.map(item => ({
      id: item.noticeId || item.id,
      title: item.noticeTitle || item.title,
      desc: item.noticeContent || item.content || item.desc,
      time: formatTime(item.createTime || item.time),
      type: item.noticeType || item.type || 'info',
      isRead: item.isRead === 1 || item.isRead === true,
      avatar: ''
    }))
    updateUnreadCount()
  }).catch(() => {
    // 如果 API 失败，使用模拟数据
    notifications.value = [
      {
        title: '系统升级通知',
        desc: '系统将于本周末进行升级维护，请提前保存工作内容',
        time: '10 分钟前',
        type: 'warning',
        isRead: false,
        avatar: ''
      },
      {
        title: '新的待办事项',
        desc: '您有一个新的待办事项需要处理',
        time: '1 小时前',
        type: 'info',
        isRead: false,
        avatar: ''
      },
      {
        title: '服务器告警',
        desc: '服务器 CPU 使用率超过 80%，请检查系统状态',
        time: '2 小时前',
        type: 'urgent',
        isRead: false,
        avatar: ''
      }
    ]
    updateUnreadCount()
  }).finally(() => {
    loading.value = false
  })
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  if (typeof time === 'string' && (time.includes('分钟') || time.includes('小时') || time.includes('天'))) {
    return time
  }
  const now = Date.now()
  const target = new Date(time).getTime()
  const diff = now - target

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return new Date(time).toLocaleDateString('zh-CN')
}

// 点击外部关闭下拉框
const handleClickOutside = (e) => {
  if (!e.target.closest('.notice-icon')) {
    visible.value = false
  }
}

onMounted(() => {
  loadNotifications()
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style lang="scss" scoped>
$primary-color: #1e80ff;
$text-primary: #1a1a1a;
$text-regular: #666666;
$text-secondary: #999999;
$bg-color-page: #f5f6f7;
$border-color: #e3e4e6;

.notice-icon {
  position: relative;
  cursor: pointer;
  padding: 8px 10px;
  border-radius: 4px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 0;
    height: 0;
    background: rgba(30, 128, 255, 0.08);
    border-radius: 4px;
    opacity: 0;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &:hover::before {
    width: 100%;
    height: 100%;
    opacity: 1;
  }

  &:hover {
    background: #f5f5f5;
    transform: translateY(-1px);

    .bell-icon {
      animation: bell-shake 0.5s ease;
      color: $primary-color;
    }
  }

  .notice-badge {
    :deep(.el-badge__content) {
      border: 2px solid #fff;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }

    &:hover .el-badge__content {
      transform: scale(1.1);
    }
  }

  .bell-icon {
    color: $text-regular;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }
}

@keyframes bell-shake {
  0%, 100% { transform: rotate(0deg); }
  10%, 30%, 50%, 70%, 90% { transform: rotate(-10deg); }
  20%, 40%, 60%, 80% { transform: rotate(10deg); }
}

.notice-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  width: 360px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  margin-top: 8px;
  overflow: hidden;
  animation: dropdown-fade-in 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &::before {
    content: '';
    position: absolute;
    top: -6px;
    right: 20px;
    width: 12px;
    height: 12px;
    background: #fff;
    transform: rotate(45deg);
    box-shadow: -1px -1px 4px rgba(0, 0, 0, 0.06);
  }
}

@keyframes dropdown-fade-in {
  from {
    opacity: 0;
    transform: translateY(-12px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid $border-color;
  background: #fafafa;

  .header-title {
    font-size: 14px;
    font-weight: 600;
    color: $text-primary;
  }

  .header-actions {
    display: flex;
    gap: 8px;
  }
}

.dropdown-content {
  max-height: 400px;
  overflow-y: auto;

  .loading-content {
    padding: 16px;
  }

  .empty-content {
    padding: 40px 0;
    display: flex;
    justify-content: center;
  }

  .notice-list {
    .notice-item {
      display: flex;
      align-items: flex-start;
      padding: 14px 16px;
      border-bottom: 1px solid #f5f5f5;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 0;
        height: 70%;
        background: linear-gradient(90deg, $primary-color 0%, transparent 100%);
        opacity: 0;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        border-radius: 0 2px 2px 0;
      }

      &:hover {
        background: linear-gradient(90deg, #fafafa 0%, transparent 100%);
        transform: translateX(4px);

        &::before {
          width: 4px;
          opacity: 1;
        }
      }

      &.is-read {
        opacity: 0.6;

        &:hover {
          opacity: 1;
        }
      }

      .notice-avatar {
        margin-right: 12px;
        flex-shrink: 0;
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      }

      &:hover .notice-avatar {
        transform: scale(1.05);
      }

      .notice-body {
        flex: 1;
        min-width: 0;

        .notice-title {
          font-size: 14px;
          color: $text-primary;
          margin-bottom: 4px;
          font-weight: 500;
          transition: color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }

        .notice-desc {
          font-size: 13px;
          color: $text-regular;
          margin-bottom: 6px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .notice-time {
          font-size: 12px;
          color: $text-secondary;
        }
      }

      .notice-status {
        margin-left: 12px;
        flex-shrink: 0;
      }

      &:hover .notice-title {
        color: $primary-color;
      }
    }
  }
}

.dropdown-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  padding: 14px 0;
  background: linear-gradient(180deg, #fafafa 0%, #f5f5f5 100%);
  border-top: 1px solid $border-color;
  cursor: pointer;
  font-size: 14px;
  color: $text-regular;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
    transition: left 0.5s ease;
  }

  &:hover {
    background: linear-gradient(180deg, #f0f0f0 0%, #e8e8e8 100%);
    color: $primary-color;

    &::before {
      left: 100%;
    }

    .el-icon {
      transform: translateX(6px);
    }
  }

  .el-icon {
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }
}
</style>
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
        <div v-if="notifications.length === 0" class="empty-content">
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const visible = ref(false)
const unreadCount = ref(5)

// 模拟通知数据
const notifications = ref([
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
  },
  {
    title: '用户反馈',
    desc: '收到新的用户反馈，请及时处理',
    time: '昨天',
    type: 'info',
    isRead: true,
    avatar: ''
  },
  {
    title: '安全更新',
    desc: '系统安全补丁已安装完成',
    time: '2 天前',
    type: 'info',
    isRead: true,
    avatar: ''
  }
])

const handleClick = () => {
  visible.value = !visible.value
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

// 点击外部关闭下拉框
const handleClickOutside = (e) => {
  if (!e.target.closest('.notice-icon')) {
    visible.value = false
  }
}

onMounted(() => {
  updateUnreadCount()
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
  transition: all 0.2s ease;

  &:hover {
    background: #f5f5f5;

    .bell-icon {
      animation: bell-shake 0.5s ease;
    }
  }

  .notice-badge {
    :deep(.el-badge__content) {
      border: 2px solid #fff;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
  }

  .bell-icon {
    color: $text-regular;
    transition: color 0.2s ease;
  }

  &:hover .bell-icon {
    color: $primary-color;
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
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  z-index: 1000;
  margin-top: 8px;
  overflow: hidden;
  animation: dropdown-fade-in 0.2s ease;

  &::before {
    content: '';
    position: absolute;
    top: -6px;
    right: 20px;
    width: 12px;
    height: 12px;
    background: #fff;
    transform: rotate(45deg);
  }
}

@keyframes dropdown-fade-in {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
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
      transition: all 0.2s ease;

      &:hover {
        background: #fafafa;
      }

      &.is-read {
        opacity: 0.6;
      }

      .notice-avatar {
        margin-right: 12px;
        flex-shrink: 0;
      }

      .notice-body {
        flex: 1;
        min-width: 0;

        .notice-title {
          font-size: 14px;
          color: $text-primary;
          margin-bottom: 4px;
          font-weight: 500;
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
    }
  }
}

.dropdown-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  padding: 12px 0;
  background: #fafafa;
  border-top: 1px solid $border-color;
  cursor: pointer;
  font-size: 14px;
  color: $text-regular;
  transition: all 0.2s ease;

  &:hover {
    background: #f0f0f0;
    color: $primary-color;

    .el-icon {
      transform: translateX(4px);
    }
  }

  .el-icon {
    transition: transform 0.2s ease;
  }
}
</style>
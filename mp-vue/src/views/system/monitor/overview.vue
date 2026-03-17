<template>
  <div class="monitor-overview-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-card-content" @click="goToPage('/monitor/server')">
            <div class="stat-icon cpu-icon">
              <el-icon :size="28"><Cpu /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">CPU 使用率</div>
              <div class="stat-value" :style="{ color: getColor(cpuUsage) }">{{ cpuUsage }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-card-content" @click="goToPage('/monitor/server')">
            <div class="stat-icon memory-icon">
              <el-icon :size="28"><Coin /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">内存使用率</div>
              <div class="stat-value" :style="{ color: getColor(memoryUsage) }">{{ memoryUsage }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-card-content" @click="goToPage('/monitor/redis')">
            <div class="stat-icon redis-icon">
              <el-icon :size="28"><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">Redis 状态</div>
              <div class="stat-value">
                <el-tag :type="redisStatus === 'UP' ? 'success' : 'danger'" size="small">
                  {{ redisStatus === 'UP' ? '正常' : '异常' }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-card-content" @click="goToPage('/monitor/online')">
            <div class="stat-icon user-icon">
              <el-icon :size="28"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">在线用户</div>
              <div class="stat-value">{{ onlineUserCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">快捷入口</div>
          </template>
          <div class="quick-links">
            <div class="quick-link" @click="goToPage('/monitor/server')">
              <div class="quick-link-icon cpu-icon">
                <el-icon :size="24"><Cpu /></el-icon>
              </div>
              <span class="quick-link-name">服务器监控</span>
            </div>
            <div class="quick-link" @click="goToPage('/monitor/redis')">
              <div class="quick-link-icon redis-icon">
                <el-icon :size="24"><DataLine /></el-icon>
              </div>
              <span class="quick-link-name">Redis 监控</span>
            </div>
            <div class="quick-link" @click="goToPage('/monitor/cache')">
              <div class="quick-link-icon cache-icon">
                <el-icon :size="24"><Folder /></el-icon>
              </div>
              <span class="quick-link-name">缓存监控</span>
            </div>
            <div class="quick-link" @click="goToPage('/monitor/online')">
              <div class="quick-link-icon user-icon">
                <el-icon :size="24"><User /></el-icon>
              </div>
              <span class="quick-link-name">在线用户</span>
            </div>
            <div class="quick-link" @click="goToPage('/system/log')">
              <div class="quick-link-icon log-icon">
                <el-icon :size="24"><Document /></el-icon>
              </div>
              <span class="quick-link-name">操作日志</span>
            </div>
            <div class="quick-link" @click="goToPage('/system/loginlog')">
              <div class="quick-link-icon login-icon">
                <el-icon :size="24"><List /></el-icon>
              </div>
              <span class="quick-link-name">登录日志</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统信息 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">系统信息</div>
          </template>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="系统名称">Micro Platform</el-descriptions-item>
            <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
            <el-descriptions-item label="启动时间">{{ systemInfo.startTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="运行时长">{{ systemInfo.uptime || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">服务状态</div>
          </template>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="Gateway">
              <el-tag type="success" size="small">运行中</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Auth 服务">
              <el-tag type="success" size="small">运行中</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="System 服务">
              <el-tag type="success" size="small">运行中</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Generator 服务">
              <el-tag type="success" size="small">运行中</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <div class="refresh-btn">
      <el-button type="primary" icon="Refresh" @click="loadOverviewData" :loading="loading">
        刷新
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Cpu, Coin, DataLine, User, Folder, Document, List, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)

const cpuUsage = ref(0)
const memoryUsage = ref(0)
const redisStatus = ref('')
const onlineUserCount = ref(0)

const systemInfo = reactive({
  startTime: '',
  uptime: ''
})

const getColor = (value) => {
  if (value < 60) return '#00b578'
  if (value < 80) return '#faad14'
  return '#ff4d4f'
}

const goToPage = (path) => {
  router.push(path)
}

const loadOverviewData = () => {
  loading.value = true
  Promise.all([
    request.get('/monitor/server').catch(() => ({ data: {} })),
    request.get('/monitor/health').catch(() => ({ data: {} })),
    request.get('/monitor/online/list').catch(() => ({ data: [] }))
  ]).then(([serverRes, healthRes, onlineRes]) => {
    // 处理服务器数据
    if (serverRes.data) {
      if (serverRes.data.cpu) {
        cpuUsage.value = Math.round(serverRes.data.cpu.usagePercent || 0)
      }
      if (serverRes.data.memory) {
        memoryUsage.value = Math.round(serverRes.data.memory.usagePercent || 0)
      }
    }
    // 处理健康状态
    if (healthRes.data) {
      redisStatus.value = healthRes.data.details?.redis?.status || ''
    }
    // 处理在线用户
    if (onlineRes.data) {
      onlineUserCount.value = onlineRes.data.length || 0
    }
  }).catch(err => {
    // 加载监控总览数据失败
  }).finally(() => {
    loading.value = false
  })
}

onMounted(() => {
  loadOverviewData()
})
</script>

<style lang="scss" scoped>
.monitor-overview-container {
  padding: 0;

  .stat-card {
    :deep(.el-card__header) {
      display: none;
    }

    :deep(.el-card__body) {
      padding: 16px;
    }

    .stat-card-content {
      display: flex;
      align-items: center;
      gap: 16px;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        transform: scale(1.02);
      }

      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;

        &.cpu-icon {
          background: $gradient-total;
          color: #fff;
        }

        &.memory-icon {
          background: $gradient-process;
          color: #fff;
        }

        &.redis-icon {
          background: $gradient-start;
          color: #fff;
        }

        &.user-icon {
          background: $gradient-task;
          color: #fff;
        }
      }

      .stat-info {
        flex: 1;

        .stat-label {
          font-size: 13px;
          color: #666;
          margin-bottom: 8px;
        }

        .stat-value {
          font-size: 24px;
          font-weight: 600;
        }
      }
    }
  }

  .card-header {
    font-weight: 500;
    font-size: 15px;
  }

  .quick-links {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 16px;

    .quick-link {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px 12px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s ease;
      background: #fafafa;

      &:hover {
        background: #f0f2f5;
        transform: translateY(-4px);
      }

      .quick-link-icon {
        width: 48px;
        height: 48px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 12px;
        color: #fff;

        &.cpu-icon {
          background: $gradient-total;
        }

        &.redis-icon {
          background: $gradient-start;
        }

        &.cache-icon {
          background: $gradient-process;
        }

        &.user-icon {
          background: $gradient-task;
        }

        &.log-icon {
          background: $gradient-user;
        }

        &.login-icon {
          background: $gradient-role;
        }
      }

      .quick-link-name {
        font-size: 13px;
        color: #666;
      }
    }
  }

  .refresh-btn {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
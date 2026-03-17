<template>
  <div class="redis-monitor-container">
    <el-row :gutter="16">
      <!-- Redis 状态 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon><DataLine /></el-icon>
              <span>Redis 状态</span>
            </div>
          </template>
          <div class="stat-content">
            <el-tag :type="redisStatus.status === 'UP' ? 'success' : 'danger'" size="large">
              {{ redisStatus.status === 'UP' ? '运行正常' : '异常' }}
            </el-tag>
          </div>
        </el-card>
      </el-col>

      <!-- 内存使用 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon><Coin /></el-icon>
              <span>内存使用</span>
            </div>
          </template>
          <div class="stat-content">
            <div class="memory-value">{{ formatBytes(redisInfo.usedMemory) }}</div>
            <div class="stat-detail">峰值：{{ formatBytes(redisInfo.usedMemoryPeak) }}</div>
          </div>
        </el-card>
      </el-col>

      <!-- 连接数 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon><Link /></el-icon>
              <span>连接数</span>
            </div>
          </template>
          <div class="stat-content">
            <div class="connection-value">{{ redisInfo.connectedClients || 0 }}</div>
            <div class="stat-detail">最大：{{ redisInfo.maxclients || '-' }}</div>
          </div>
        </el-card>
      </el-col>

      <!-- 命中率 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon>
              <span>缓存命中率</span>
            </div>
          </template>
          <div class="stat-content">
            <el-progress
              :percentage="cacheHitRate"
              :color="getProgressColor(cacheHitRate)"
              :stroke-width="12"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细信息 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><DataLine /></el-icon>
              <span>Redis 信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="Redis 版本">{{ redisInfo.redisVersion || '-' }}</el-descriptions-item>
            <el-descriptions-item label="运行模式">{{ redisInfo.redisMode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="TCP 端口">{{ redisInfo.tcpPort || '-' }}</el-descriptions-item>
            <el-descriptions-item label="运行时间">{{ redisInfo.uptimeInDays || '-' }} 天</el-descriptions-item>
            <el-descriptions-item label="主机">{{ redisInfo.redisHost || '-' }}</el-descriptions-item>
            <el-descriptions-item label="密码">********</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><Coin /></el-icon>
              <span>内存信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="已用内存">{{ formatBytes(redisInfo.usedMemory) }}</el-descriptions-item>
            <el-descriptions-item label="内存峰值">{{ formatBytes(redisInfo.usedMemoryPeak) }}</el-descriptions-item>
            <el-descriptions-item label="碎片率">{{ redisInfo.memFragmentRatio || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最大内存">{{ formatBytes(redisInfo.maxmemory) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>键统计</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="数据库总数">{{ redisInfo.dbsize || '-' }}</el-descriptions-item>
            <el-descriptions-item label="Keyspace hits">{{ redisInfo.keyspaceHits || '-' }}</el-descriptions-item>
            <el-descriptions-item label="Keyspace misses">{{ redisInfo.keyspaceMisses || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><Refresh /></el-icon>
              <span>命令统计</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="命令处理总数">{{ redisInfo.totalCommandsProcessed || '-' }}</el-descriptions-item>
            <el-descriptions-item label="每秒命令数">{{ redisInfo.instantaneousOpsPerSec || '-' }}</el-descriptions-item>
            <el-descriptions-item label="网络入口">{{ formatBytes(redisInfo.totalNetInputBytes) }}/s</el-descriptions-item>
            <el-descriptions-item label="网络出口">{{ formatBytes(redisInfo.totalNetOutputBytes) }}/s</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><Timer /></el-icon>
              <span>持久化</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="RDB 上次保存">{{ redisInfo.rdbLastSaveTime || '-' }} 秒前</el-descriptions-item>
            <el-descriptions-item label="RDB 状态">{{ redisInfo.rdbLastBgsaveStatus || '-' }}</el-descriptions-item>
            <el-descriptions-item label="AOF 状态">{{ redisInfo.aofEnabled ? '开启' : '关闭' }}</el-descriptions-item>
            <el-descriptions-item label="AOF 重写">{{ redisInfo.aofLastBgsaveStatus || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <div class="refresh-btn">
      <el-button type="primary" icon="Refresh" @click="loadRedisInfo" :loading="loading">
        刷新
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { formatBytes, getProgressColor } from '@/utils/mp'
import { ref, reactive, computed, onMounted } from 'vue'
import { DataLine, Coin, Link, TrendCharts, Document, Refresh, Timer } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)

const redisStatus = reactive({
  status: ''
})

const redisInfo = reactive({
  redisVersion: '',
  redisMode: '',
  tcpPort: '',
  uptimeInDays: '',
  redisHost: '',
  usedMemory: 0,
  usedMemoryPeak: 0,
  maxmemory: 0,
  connectedClients: 0,
  maxclients: 0,
  memFragmentRatio: '',
  dbsize: 0,
  keyspaceHits: 0,
  keyspaceMisses: 0,
  totalCommandsProcessed: 0,
  instantaneousOpsPerSec: 0,
  totalNetInputBytes: 0,
  totalNetOutputBytes: 0,
  rdbLastSaveTime: 0,
  rdbLastBgsaveStatus: '',
  aofEnabled: false,
  aofLastBgsaveStatus: ''
})

const cacheHitRate = computed(() => {
  const hits = redisInfo.keyspaceHits || 0
  const misses = redisInfo.keyspaceMisses || 0
  const total = hits + misses
  if (total === 0) return 0
  return Math.round((hits / total) * 100)
})

const loadRedisInfo = () => {
  loading.value = true
  request.get('/monitor/redis').then(res => {
    const data = res.data
    // 后端返回的是扁平对象，字段名是小驼峰格式
    redisStatus.status = 'UP' // 能成功返回数据就说明 Redis 正常

    // 映射后端字段到前端
    redisInfo.redisVersion = data.version || '-'
    redisInfo.redisMode = '-' // 后端未提供
    redisInfo.tcpPort = '-' // 后端未提供
    redisInfo.uptimeInDays = data.uptime ? data.uptime.replace(' 天', '') : '-'
    redisInfo.redisHost = '127.0.0.1' // 后端未提供
    redisInfo.usedMemory = parseInt(data.usedMemory) || 0
    redisInfo.usedMemoryPeak = parseInt(data.usedMemoryPeak) || 0
    redisInfo.maxmemory = 0 // 后端未提供
    redisInfo.connectedClients = parseInt(data.connectedClients) || 0
    redisInfo.maxclients = '-' // 后端未提供
    redisInfo.memFragmentRatio = data.memFragmentationRatio || '-'
    redisInfo.dbsize = data.dbInfo?.[0]?.keysCount || 0
    redisInfo.keyspaceHits = parseInt(data.keyspaceHits) || 0
    redisInfo.keyspaceMisses = parseInt(data.keyspaceMisses) || 0
    redisInfo.totalCommandsProcessed = parseInt(data.totalCommandsProcessed) || 0
    redisInfo.instantaneousOpsPerSec = parseInt(data.instantaneousOpsPerSec) || 0
    redisInfo.totalNetInputBytes = 0 // 后端未提供
    redisInfo.totalNetOutputBytes = 0 // 后端未提供
    redisInfo.rdbLastSaveTime = 0 // 后端未提供
    redisInfo.rdbLastBgsaveStatus = '-' // 后端未提供
    redisInfo.aofEnabled = false // 后端未提供
    redisInfo.aofLastBgsaveStatus = '-' // 后端未提供
  }).catch(err => {
    // 加载 Redis 信息失败
    redisStatus.status = 'DOWN'
  }).finally(() => {
    loading.value = false
  })
}

onMounted(() => {
  loadRedisInfo()
})
</script>

<style lang="scss" scoped>
.redis-monitor-container {
  padding: 0;

  .stat-card {
    :deep(.el-card__header) {
      padding: 14px 16px;
    }

    :deep(.el-card__body) {
      padding: 16px;
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500;
      font-size: 14px;

      .el-icon {
        font-size: 16px;
      }
    }

    .stat-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 100px;

      .memory-value {
        font-size: 20px;
        font-weight: 600;
        color: #1a1a1a;
      }

      .connection-value {
        font-size: 24px;
        font-weight: 600;
        color: #1e80ff;
      }

      .stat-detail {
        margin-top: 8px;
        font-size: 12px;
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
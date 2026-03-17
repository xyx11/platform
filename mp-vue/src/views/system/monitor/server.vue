<template>
  <div class="server-monitor-container">
    <el-row :gutter="16">
      <!-- CPU 使用率 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon><Cpu /></el-icon>
              <span>CPU 使用率</span>
            </div>
          </template>
          <div class="stat-content">
            <el-progress
              type="dashboard"
              :percentage="cpuUsage"
              :color="getProgressColor(cpuUsage)"
              :width="120"
            />
            <div class="stat-detail">
              <span>{{ cpuInfo.model || '-' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 内存使用率 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon><Coin /></el-icon>
              <span>内存使用率</span>
            </div>
          </template>
          <div class="stat-content">
            <el-progress
              type="dashboard"
              :percentage="memoryUsage"
              :color="getProgressColor(memoryUsage)"
              :width="120"
            />
            <div class="stat-detail">
              <span>{{ formatBytes(memoryInfo.used, 'GB') }} / {{ formatBytes(memoryInfo.total, 'GB') }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 磁盘使用率 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon><Folder /></el-icon>
              <span>磁盘使用率</span>
            </div>
          </template>
          <div class="stat-content">
            <el-progress
              type="dashboard"
              :percentage="diskUsage"
              :color="getProgressColor(diskUsage)"
              :width="120"
            />
            <div class="stat-detail">
              <span>{{ formatBytes(diskInfo.used, 'GB') }} / {{ formatBytes(diskInfo.total, 'GB') }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 系统负载 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon>
              <span>系统负载</span>
            </div>
          </template>
          <div class="stat-content">
            <div class="load-display">
              <div class="load-item">
                <span class="load-label">1 分钟</span>
                <span class="load-value">{{ systemLoad.oneMinute || '0.00' }}</span>
              </div>
              <div class="load-item">
                <span class="load-label">5 分钟</span>
                <span class="load-value">{{ systemLoad.fiveMinute || '0.00' }}</span>
              </div>
              <div class="load-item">
                <span class="load-label">15 分钟</span>
                <span class="load-value">{{ systemLoad.fifteenMinute || '0.00' }}</span>
              </div>
            </div>
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
              <el-icon><Monitor /></el-icon>
              <span>系统信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="操作系统">{{ systemInfo.osName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="系统架构">{{ systemInfo.osArch || '-' }}</el-descriptions-item>
            <el-descriptions-item label="系统版本">{{ systemInfo.osVersion || '-' }}</el-descriptions-item>
            <el-descriptions-item label="主机名">{{ systemInfo.hostName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="运行时间">{{ systemInfo.uptime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="时区">{{ systemInfo.timezone || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><Cpu /></el-icon>
              <span>CPU 信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="核心数">{{ cpuInfo.cores || '-' }}</el-descriptions-item>
            <el-descriptions-item label="逻辑处理器">{{ cpuInfo.logicalProcessors || '-' }}</el-descriptions-item>
            <el-descriptions-item label="CPU 型号">{{ cpuInfo.model || '-' }}</el-descriptions-item>
            <el-descriptions-item label="主频">{{ cpuInfo.mhz || '-' }} MHz</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><Coin /></el-icon>
              <span>内存信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="总内存">{{ formatBytes(memoryInfo.total) }}</el-descriptions-item>
            <el-descriptions-item label="已使用">{{ formatBytes(memoryInfo.used) }}</el-descriptions-item>
            <el-descriptions-item label="空闲内存">{{ formatBytes(memoryInfo.free) }}</el-descriptions-item>
            <el-descriptions-item label="使用率">
              <el-tag :type="getMemoryTagType(memoryUsage)">{{ memoryUsage }}%</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><Folder /></el-icon>
              <span>磁盘信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="总磁盘">{{ formatBytes(diskInfo.total) }}</el-descriptions-item>
            <el-descriptions-item label="已使用">{{ formatBytes(diskInfo.used) }}</el-descriptions-item>
            <el-descriptions-item label="空闲磁盘">{{ formatBytes(diskInfo.free) }}</el-descriptions-item>
            <el-descriptions-item label="使用率">
              <el-tag :type="getDiskTagType(diskUsage)">{{ diskUsage }}%</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <div class="refresh-btn">
      <el-button type="primary" icon="Refresh" @click="loadServerInfo" :loading="loading">
        刷新
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { formatBytes, getProgressColor } from '@/utils/mp'
import { ref, reactive, onMounted } from 'vue'
import { Cpu, Coin, Folder, Monitor, TrendCharts, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)

const cpuUsage = ref(0)
const memoryUsage = ref(0)
const diskUsage = ref(0)

const cpuInfo = reactive({
  model: '',
  cores: null,
  logicalProcessors: null,
  mhz: null
})

const memoryInfo = reactive({
  total: 0,
  used: 0,
  free: 0
})

const diskInfo = reactive({
  total: 0,
  used: 0,
  free: 0
})

const systemLoad = reactive({
  oneMinute: null,
  fiveMinute: null,
  fifteenMinute: null
})

const systemInfo = reactive({
  osName: '',
  osArch: '',
  osVersion: '',
  hostName: '',
  uptime: '',
  timezone: ''
})

const formatBytes = (bytes, unit = 'B') => {
  if (bytes === null || bytes === undefined || bytes === 0) return '0 ' + unit
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  if (unit === 'GB') {
    return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
  }
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(2) + ' ' + sizes[i]
}

const getMemoryTagType = (usage) => {
  if (usage < 60) return 'success'
  if (usage < 80) return 'warning'
  return 'danger'
}

const getDiskTagType = (usage) => {
  if (usage < 70) return 'success'
  if (usage < 85) return 'warning'
  return 'danger'
}

const loadServerInfo = () => {
  loading.value = true
  request.get('/monitor/server').then(res => {
    const data = res.data
    // 后端返回的是扁平结构，不是嵌套对象
    // CPU 信息
    if (data.cpuProcessors) {
      cpuInfo.cores = data.cpuProcessors
      cpuInfo.logicalProcessors = data.cpuProcessors
    }
    // 内存信息 - 解析字符串如 "8192 MB"
    if (data.totalMemory) {
      memoryInfo.total = parseInt(data.totalMemory) * 1024 * 1024 || 0
    }
    if (data.freeMemory) {
      memoryInfo.free = parseInt(data.freeMemory) * 1024 * 1024 || 0
    }
    if (data.usableMemory) {
      memoryInfo.used = parseInt(data.usableMemory) * 1024 * 1024 || 0
    }
    // 计算内存使用率
    if (memoryInfo.total > 0) {
      memoryUsage.value = Math.round((memoryInfo.used / memoryInfo.total) * 100)
    }
    // 磁盘信息
    if (data.diskInfo && data.diskInfo.length > 0) {
      const disk = data.diskInfo[0] || {}
      diskInfo.total = parseSizeToBytes(disk.total) || 0
      diskInfo.used = parseSizeToBytes(disk.used) || 0
      diskInfo.free = parseSizeToBytes(disk.free) || 0
      if (diskInfo.total > 0) {
        diskUsage.value = Math.round((diskInfo.used / diskInfo.total) * 100)
      }
    }
    // 系统信息
    if (data.osName) {
      systemInfo.osName = data.osName
    }
    if (data.osArch) {
      systemInfo.osArch = data.osArch
    }
    if (data.osVersion) {
      systemInfo.osVersion = data.osVersion
    }
    if (data.jvmRunTime) {
      systemInfo.uptime = data.jvmRunTime
    }
  }).catch(err => {
    // 加载服务器信息失败
  }).finally(() => {
    loading.value = false
  })
}

// 解析文件大小字符串如 "100 GB" 为字节
const parseSizeToBytes = (sizeStr) => {
  if (!sizeStr) return 0
  const match = sizeStr.match(/(\d+(?:\.\d+)?)\s*(B|KB|MB|GB|TB)?/i)
  if (!match) return 0
  const value = parseFloat(match[1])
  const unit = (match[2] || 'B').toUpperCase()
  const sizes = { B: 1, KB: 1024, MB: 1024 * 1024, GB: 1024 * 1024 * 1024, TB: 1024 * 1024 * 1024 * 1024 }
  return value * (sizes[unit] || 1)
}

onMounted(() => {
  loadServerInfo()
})
</script>

<style lang="scss" scoped>
.server-monitor-container {
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
      min-height: 140px;

      .stat-detail {
        margin-top: 12px;
        font-size: 12px;
        color: #666;
      }
    }
  }

  .load-display {
    display: flex;
    justify-content: space-around;
    align-items: center;
    height: 100px;

    .load-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;

      .load-label {
        font-size: 12px;
        color: #666;
      }

      .load-value {
        font-size: 20px;
        font-weight: 600;
        color: #1a1a1a;
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
<template>
  <div class="monitor-container">
    <el-row :gutter="20">
      <!-- 系统健康状态 -->
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon><Monitor /></el-icon>
              <span>系统健康状态</span>
            </div>
          </template>
          <div class="stat-content">
            <el-tag :type="healthStatus.status === 'UP' ? 'success' : 'danger'" size="large">
              {{ healthStatus.status === 'UP' ? '运行正常' : '异常' }}
            </el-tag>
          </div>
        </el-card>
      </el-col>

      <!-- 数据库状态 -->
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon><Monitor /></el-icon>
              <span>数据库状态</span>
            </div>
          </template>
          <div class="stat-content">
            <el-tag :type="healthStatus.details?.database?.status === 'UP' ? 'success' : 'danger'" size="large">
              {{ healthStatus.details?.database?.status === 'UP' ? '连接正常' : '异常' }}
            </el-tag>
          </div>
        </el-card>
      </el-col>

      <!-- Redis 状态 -->
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon><DataLine /></el-icon>
              <span>Redis 状态</span>
            </div>
          </template>
          <div class="stat-content">
            <el-tag :type="healthStatus.details?.redis?.status === 'UP' ? 'success' : 'danger'" size="large">
              {{ healthStatus.details?.redis?.status === 'UP' ? '连接正常' : '异常' }}
            </el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- JVM 信息 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><Cpu /></el-icon>
              <span>JVM 运行时信息</span>
            </div>
          </template>
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="可用处理器">{{ healthStatus.details?.jvm?.availableProcessors || '-' }} 核</el-descriptions-item>
            <el-descriptions-item label="系统负载">{{ healthStatus.details?.jvm?.systemLoadAverage || '-' }}</el-descriptions-item>
            <el-descriptions-item label="运行时间">{{ healthStatus.details?.jvm?.uptime || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><Cpu /></el-icon>
              <span>线程信息</span>
            </div>
          </template>
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="线程总数">{{ threadInfo.totalThreads || '-' }}</el-descriptions-item>
            <el-descriptions-item label="峰值线程数">{{ threadInfo.peakThreadCount || '-' }}</el-descriptions-item>
            <el-descriptions-item label="守护线程数">{{ threadInfo.daemonThreadCount || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><Platform /></el-icon>
              <span>数据库连接池</span>
            </div>
          </template>
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="状态">
              <el-tag :type="datasourceInfo.status === 'UP' ? 'success' : 'danger'" size="small">
                {{ datasourceInfo.status === 'UP' ? '正常' : '异常' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="连接测试">{{ datasourceInfo.connectionTest || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 刷新按钮 -->
    <div class="refresh-btn">
      <el-button type="primary" icon="Refresh" @click="loadAllData" :loading="loading">刷新</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Monitor, DataLine, Cpu, Refresh, Platform } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)

const healthStatus = reactive({
  status: '',
  details: {}
})

const threadInfo = reactive({
  totalThreads: null,
  peakThreadCount: null,
  daemonThreadCount: null
})

const datasourceInfo = reactive({
  status: '',
  connectionTest: ''
})

const loadHealthData = () => {
  request.get('/monitor/health').then(res => {
    Object.assign(healthStatus, res.data)
  })
}

const loadThreadData = () => {
  request.get('/monitor/threads').then(res => {
    Object.assign(threadInfo, res.data)
  })
}

const loadDatasourceData = () => {
  request.get('/monitor/datasource').then(res => {
    Object.assign(datasourceInfo, res.data)
  })
}

const loadAllData = () => {
  loading.value = true
  Promise.all([
    loadHealthData(),
    loadThreadData(),
    loadDatasourceData()
  ]).finally(() => {
    loading.value = false
  })
}

onMounted(() => {
  loadAllData()
})
</script>

<style lang="scss" scoped>
.monitor-container {
  .stat-card {
    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: bold;
    }

    .stat-content {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 100px;
    }
  }

  .refresh-btn {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
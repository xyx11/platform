<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <el-button type="danger" icon="Delete" @click="handleClearAll">清除所有缓存</el-button>
        <el-button type="warning" icon="Refresh" @click="refreshCache">刷新缓存</el-button>
      </div>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 缓存统计 -->
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-statistic title="总命令数" :value="stats.totalCommandsProcessed" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-statistic title="每秒操作数" :value="stats.instantaneousOpsPerSec" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-statistic title="缓存命中次数" :value="stats.keyspaceHits" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-statistic title="缓存命中率" :value="stats.hitRate" suffix="%" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 内存使用 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>内存使用</span>
          </template>
          <div class="memory-info">
            <el-progress
              type="dashboard"
              :percentage="memoryUsagePercentage"
              :color="colors"
            />
            <div class="memory-detail">
              <p>当前：{{ stats.usedMemory }}</p>
              <p>峰值：{{ stats.usedMemoryPeak }}</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 键数量 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>键数量</span>
          </template>
          <div class="stat-large">
            {{ stats.dbSize }}
          </div>
          <div class="stat-label">总键数</div>
        </el-card>
      </el-col>

      <!-- 缓存 Miss -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>缓存 Miss 次数</span>
          </template>
          <div class="stat-large">
            {{ stats.keyspaceMisses }}
          </div>
          <div class="stat-label">未命中次数</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCacheStats, clearAllCache } from '@/api/monitor'

const stats = ref({
  totalCommandsProcessed: 0,
  instantaneousOpsPerSec: 0,
  keyspaceHits: 0,
  keyspaceMisses: 0,
  hitRate: '0.00',
  dbSize: 0,
  usedMemory: '0B',
  usedMemoryPeak: '0B'
})

const colors = [
  { color: '#5cb87a', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#f56c6c', percentage: 80 }
]

const memoryUsagePercentage = ref(0)

const getCacheStatsData = async () => {
  try {
    const response = await getCacheStats()
    const data = response.data || {}
    stats.value = {
      totalCommandsProcessed: parseInt(data.totalCommandsProcessed) || 0,
      instantaneousOpsPerSec: parseInt(data.instantaneousOpsPerSec) || 0,
      keyspaceHits: parseInt(data.keyspaceHits) || 0,
      keyspaceMisses: parseInt(data.keyspaceMisses) || 0,
      hitRate: parseFloat(data.hitRate) || 0,
      dbSize: data.dbSize || 0,
      usedMemory: data.usedMemory || '0B',
      usedMemoryPeak: data.usedMemoryPeak || '0B'
    }

    // 计算内存使用百分比（估算）
    if (data.usedMemory) {
      const match = data.usedMemory.match(/(\d+\.?\d*)([KMGT]?B)/)
      if (match) {
        const value = parseFloat(match[1])
        const unit = match[2]
        const multipliers = { 'B': 1, 'KB': 1024, 'MB': 1024 * 1024, 'GB': 1024 * 1024 * 1024 }
        const bytes = value * (multipliers[unit] || 1)
        // 假设最大内存 1GB
        memoryUsagePercentage.value = Math.min(100, Math.round((bytes / (1024 * 1024 * 1024)) * 100))
      }
    }
  } catch (error) {
    ElMessage.error('获取缓存统计失败')
  }
}

// 清除所有缓存
const handleClearAll = () => {
  ElMessageBox.confirm('确定要清除所有缓存吗？此操作不可恢复！', '警告', {
    type: 'warning'
  }).then(() => {
    clearAllCache().then(() => {
      ElMessage.success('清除缓存成功')
      getCacheStatsData()
    }).catch(() => {
      ElMessage.error('清除缓存失败')
    })
  })
}

// 刷新缓存
const refreshCache = () => {
  getCacheStatsData()
  ElMessage.success('刷新成功')
}

onMounted(() => {
  getCacheStatsData()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;

  .toolbar {
    display: flex;
    gap: 10px;
  }
}

.stat-card {
  text-align: center;
  padding: 10px 0;
}

.memory-info {
  text-align: center;
  padding: 20px 0;
}

.memory-detail {
  margin-top: 20px;
  font-size: 14px;
  color: #666;

  p {
    margin: 5px 0;
  }
}

.stat-large {
  font-size: 48px;
  font-weight: bold;
  color: #409EFF;
  text-align: center;
  padding: 20px 0;
}

.stat-label {
  text-align: center;
  font-size: 14px;
  color: #999;
  padding-bottom: 20px;
}
</style>
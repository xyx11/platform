<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- CPU 信息 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>CPU</span>
            </div>
          </template>
          <div class="card-content">
            <el-progress
              type="dashboard"
              :percentage="cpuPercentage"
              :color="colors"
            />
            <div class="card-info">{{ serverInfo.cpuProcessors }}</div>
          </div>
        </el-card>
      </el-col>

      <!-- 内存信息 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>内存</span>
            </div>
          </template>
          <div class="card-content">
            <el-progress
              type="dashboard"
              :percentage="memoryPercentage"
              :color="colors"
            />
            <div class="card-info">
              已用：{{ serverInfo.usableMemory }} / {{ serverInfo.totalMemory }}
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- JVM 信息 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>JVM</span>
            </div>
          </template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="名称">{{ serverInfo.jvmName }}</el-descriptions-item>
            <el-descriptions-item label="版本">{{ serverInfo.jvmVersion }}</el-descriptions-item>
            <el-descriptions-item label="启动时间">{{ serverInfo.jvmStartTime }}</el-descriptions-item>
            <el-descriptions-item label="运行时长">{{ serverInfo.jvmRunTime }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统信息 -->
    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <span>系统信息</span>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="系统名称">{{ serverInfo.osName }}</el-descriptions-item>
        <el-descriptions-item label="系统架构">{{ serverInfo.osArch }}</el-descriptions-item>
        <el-descriptions-item label="系统版本">{{ serverInfo.osVersion }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 磁盘信息 -->
    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <span>磁盘信息</span>
      </template>
      <el-table :data="serverInfo.diskInfo" style="width: 100%">
        <el-table-column prop="diskName" label="盘符" />
        <el-table-column prop="total" label="总容量" />
        <el-table-column prop="used" label="已使用" />
        <el-table-column prop="free" label="可用空间" />
        <el-table-column label="使用率">
          <template #default="{ row }">
            <el-progress
              :percentage="getDiskPercentage(row)"
              :status="getDiskStatus(row)"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getServerInfo } from '@/api/monitor'

const serverInfo = ref({
  cpuProcessors: '',
  jvmName: '',
  jvmVersion: '',
  jvmStartTime: '',
  jvmRunTime: '',
  jvmHome: '',
  maxMemory: '',
  totalMemory: '',
  freeMemory: '',
  usableMemory: '',
  osName: '',
  osArch: '',
  osVersion: '',
  diskInfo: []
})

const colors = [
  { color: '#5cb87a', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#f56c6c', percentage: 80 }
]

const cpuPercentage = ref(30)
const memoryPercentage = ref(0)

const getServerInfoData = async () => {
  try {
    const response = await getServerInfo()
    const data = response.data || {}
    serverInfo.value = data

    // 计算内存使用率
    if (data.totalMemory && data.usableMemory) {
      const total = parseFloat(data.totalMemory)
      const used = parseFloat(data.usableMemory)
      memoryPercentage.value = Math.round((used / total) * 100) || 0
    }
  } catch (error) {
    ElMessage.error('获取服务器信息失败')
  }
}

const getDiskPercentage = (row) => {
  if (row.total && row.used) {
    const total = parseFloat(row.total)
    const used = parseFloat(row.used)
    return Math.round((used / total) * 100) || 0
  }
  return 0
}

const getDiskStatus = (row) => {
  const percentage = getDiskPercentage(row)
  if (percentage > 80) return 'exception'
  if (percentage > 40) return 'warning'
  return 'success'
}

onMounted(() => {
  getServerInfoData()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  text-align: center;
  padding: 20px 0;
}

.card-info {
  margin-top: 10px;
  font-size: 14px;
  color: #666;
}
</style>
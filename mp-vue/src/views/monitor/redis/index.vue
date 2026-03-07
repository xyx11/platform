<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 基本信息 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>基本信息</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="Redis 版本">{{ redisInfo.version }}</el-descriptions-item>
            <el-descriptions-item label="运行时间">{{ redisInfo.uptime }}</el-descriptions-item>
            <el-descriptions-item label="连接客户端">{{ redisInfo.connectedClients }}</el-descriptions-item>
            <el-descriptions-item label="内存使用">{{ redisInfo.usedMemory }}</el-descriptions-item>
            <el-descriptions-item label="内存峰值">{{ redisInfo.usedMemoryPeak }}</el-descriptions-item>
            <el-descriptions-item label="内存使用率">{{ redisInfo.usedMemoryRatio }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 性能指标 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>性能指标</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="执行命令数">{{ redisInfo.totalCommandsProcessed }}</el-descriptions-item>
            <el-descriptions-item label="每秒操作数">{{ redisInfo.instantaneousOpsPerSec }}</el-descriptions-item>
            <el-descriptions-item label="缓存命中次数">{{ redisInfo.keyspaceHits }}</el-descriptions-item>
            <el-descriptions-item label="缓存 Miss 次数">{{ redisInfo.keyspaceMisses }}</el-descriptions-item>
            <el-descriptions-item label="内存碎片比率">{{ redisInfo.memFragmentationRatio }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据库信息 -->
    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <span>数据库信息</span>
      </template>
      <el-table :data="redisInfo.dbInfo" style="width: 100%">
        <el-table-column prop="dbName" label="数据库" />
        <el-table-column prop="keysCount" label="键数量" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRedisInfo } from '@/api/monitor'

const redisInfo = ref({
  version: '',
  uptime: '',
  connectedClients: '',
  usedMemory: '',
  usedMemoryPeak: '',
  usedMemoryRatio: '',
  memFragmentationRatio: '',
  totalCommandsProcessed: '',
  instantaneousOpsPerSec: '',
  keyspaceHits: '',
  keyspaceMisses: '',
  dbInfo: []
})

const getRedisInfoData = async () => {
  try {
    const response = await getRedisInfo()
    redisInfo.value = response.data || {}
  } catch (error) {
    ElMessage.error('获取 Redis 信息失败')
  }
}

onMounted(() => {
  getRedisInfoData()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}
</style>
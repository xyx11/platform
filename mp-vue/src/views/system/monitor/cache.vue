<template>
  <div class="cache-monitor-container">
    <el-row :gutter="16">
      <!-- 缓存命中率 -->
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
              type="dashboard"
              :percentage="cacheHitRate"
              :color="getProgressColor(cacheHitRate)"
              :width="120"
            />
          </div>
        </el-card>
      </el-col>

      <!-- 缓存键数量 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon><Folder /></el-icon>
              <span>缓存键数量</span>
            </div>
          </template>
          <div class="stat-content">
            <div class="stat-value">{{ cacheStats.keysCount || 0 }}</div>
            <div class="stat-detail">数据库：{{ cacheStats.currentDb || 0 }}</div>
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
            <div class="stat-value">{{ formatBytes(cacheStats.usedMemory) }}</div>
            <div class="stat-detail">峰值：{{ formatBytes(cacheStats.usedMemoryPeak) }}</div>
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
            <div class="stat-value">{{ cacheStats.connectedClients || 0 }}</div>
            <div class="stat-detail">阻塞：{{ cacheStats.blockedClients || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 缓存命令统计 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>命令统计</span>
            </div>
          </template>
          <el-row :gutter="16">
            <el-col :span="6">
              <div class="command-stat">
                <div class="stat-label">累计命令数</div>
                <div class="stat-value-lg">{{ cacheStats.totalCommandsProcessed || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="command-stat">
                <div class="stat-label">每秒命令数</div>
                <div class="stat-value-lg">{{ cacheStats.instantaneousOpsPerSec || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="command-stat">
                <div class="stat-label">Keyspace Hits</div>
                <div class="stat-value-lg">{{ cacheStats.keyspaceHits || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="command-stat">
                <div class="stat-label">Keyspace Misses</div>
                <div class="stat-value-lg">{{ cacheStats.keyspaceMisses || 0 }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <!-- 缓存键详情 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>缓存键详情</span>
              <el-button type="primary" size="small" icon="Refresh" @click="loadCacheKeys" :loading="keysLoading">
                刷新
              </el-button>
            </div>
          </template>

          <el-table :data="cacheKeys" v-loading="keysLoading" style="width: 100%">
            <el-table-column prop="key" label="键名" min-width="200" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getKeyTypeTag(row.type)" size="small">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="ttl" label="剩余 TTL(秒)" width="120" />
            <el-table-column prop="length" label="长度/元素数" width="100" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  link
                  @click="handleViewKey(row)"
                >
                  查看
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  link
                  @click="handleDeleteKey(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="keyPage"
              v-model:page-size="keyPageSize"
              :total="keyTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="handleKeySizeChange"
              @current-change="handleKeyCurrentChange"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 查看键值对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="查看键值"
      width="600px"
    >
      <el-descriptions :column="1" border v-if="selectedKey">
        <el-descriptions-item label="键名">{{ selectedKey.key }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ selectedKey.type }}</el-descriptions-item>
        <el-descriptions-item label="TTL">{{ selectedKey.ttl }} 秒</el-descriptions-item>
        <el-descriptions-item label="值">
          <pre class="value-content">{{ selectedKey.value }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { TrendCharts, Folder, Coin, Document, Link, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const keysLoading = ref(false)
const viewDialogVisible = ref(false)
const selectedKey = ref(null)

const cacheStats = reactive({
  keysCount: 0,
  currentDb: 0,
  usedMemory: 0,
  usedMemoryPeak: 0,
  connectedClients: 0,
  blockedClients: 0,
  totalCommandsProcessed: 0,
  instantaneousOpsPerSec: 0,
  keyspaceHits: 0,
  keyspaceMisses: 0
})

const cacheKeys = ref([])
const keyPage = ref(1)
const keyPageSize = ref(10)
const keyTotal = ref(0)

const cacheHitRate = computed(() => {
  const hits = cacheStats.keyspaceHits || 0
  const misses = cacheStats.keyspaceMisses || 0
  const total = hits + misses
  if (total === 0) return 0
  return Math.round((hits / total) * 100)
})

const formatBytes = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(2) + ' ' + sizes[i]
}

const getProgressColor = (percentage) => {
  if (percentage < 60) return '#00b578'
  if (percentage < 80) return '#faad14'
  return '#ff4d4f'
}

const getKeyTypeTag = (type) => {
  const typeMap = {
    string: '',
    list: 'warning',
    set: 'success',
    zset: 'danger',
    hash: 'info'
  }
  return typeMap[type.toLowerCase()] || ''
}

const loadCacheStats = () => {
  loading.value = true
  request.get('/monitor/cache').then(res => {
    const data = res.data
    // 后端直接返回统计对象，不是嵌套的 stats 属性
    cacheStats.keyspaceHits = parseInt(data.keyspaceHits) || 0
    cacheStats.keyspaceMisses = parseInt(data.keyspaceMisses) || 0
    cacheStats.totalCommandsProcessed = parseInt(data.totalCommandsProcessed) || 0
    cacheStats.instantaneousOpsPerSec = parseInt(data.instantaneousOpsPerSec) || 0
    cacheStats.usedMemory = parseInt(data.usedMemory) || 0
    cacheStats.usedMemoryPeak = parseInt(data.usedMemoryPeak) || 0
    cacheStats.connectedClients = parseInt(data.connectedClients) || 0
    // 计算 keys 数量（从 hitRate 推断或直接使用 dbSize）
    cacheStats.keysCount = parseInt(data.dbSize) || 0
  }).catch(err => {
    console.error('加载缓存统计失败:', err)
  }).finally(() => {
    loading.value = false
  })
}

const loadCacheKeys = () => {
  keysLoading.value = true
  request.get('/monitor/cache/keys').then(res => {
    // 后端直接返回键名列表数组
    const keys = res.data || []
    // 前端需要转换为带 type/ttl/length 的对象
    cacheKeys.value = keys.map((key, index) => ({
      key,
      type: 'string', // 默认类型，后端未提供详细信息
      ttl: -1,
      length: 1,
      value: ''
    }))
    keyTotal.value = keys.length
  }).catch(err => {
    console.error('加载缓存键失败:', err)
  }).finally(() => {
    keysLoading.value = false
  })
}

const handleKeySizeChange = () => {
  loadCacheKeys()
}

const handleKeyCurrentChange = () => {
  loadCacheKeys()
}

const handleViewKey = (row) => {
  request.get(`/monitor/cache/key/${encodeURIComponent(row.key)}`).then(res => {
    selectedKey.value = {
      ...row,
      value: res.data.value
    }
    viewDialogVisible.value = true
  }).catch(err => {
    console.error('查看键值失败:', err)
  })
}

const handleDeleteKey = (row) => {
  ElMessageBox.confirm(
    `确定要删除键 "${row.key}" 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    request.delete(`/monitor/cache/key/${encodeURIComponent(row.key)}`).then(() => {
      ElMessage.success('删除成功')
      loadCacheKeys()
    }).catch(err => {
      console.error('删除失败:', err)
    })
  }).catch(() => {})
}

onMounted(() => {
  loadCacheStats()
  loadCacheKeys()
})
</script>

<style lang="scss" scoped>
.cache-monitor-container {
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
      min-height: 120px;

      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #1a1a1a;
      }

      .stat-detail {
        margin-top: 8px;
        font-size: 12px;
        color: #666;
      }
    }
  }

  .command-stat {
    text-align: center;
    padding: 16px 0;

    .stat-label {
      font-size: 13px;
      color: #666;
      margin-bottom: 8px;
    }

    .stat-value-lg {
      font-size: 22px;
      font-weight: 600;
      color: #1e80ff;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 500;
  }

  .pagination-wrapper {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }

  .value-content {
    background: #f5f6f7;
    padding: 12px;
    border-radius: 4px;
    max-height: 300px;
    overflow-y: auto;
    font-family: 'Courier New', monospace;
    font-size: 13px;
    white-space: pre-wrap;
    word-break: break-all;
  }
}
</style>
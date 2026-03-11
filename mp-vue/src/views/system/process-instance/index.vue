<template>
  <div class="process-instance-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>流程实例监控</span>
          <div>
            <el-button type="primary" @click="refreshStats">刷新统计</el-button>
            <el-button type="success" @click="refreshList">刷新列表</el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">运行中</div>
              <div class="stat-value running">{{ stats.runningCount || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">已挂起</div>
              <div class="stat-value suspended">{{ stats.suspendedCount || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">历史完成</div>
              <div class="stat-value historic">{{ stats.historicCount || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">流程定义数</div>
              <div class="stat-value definitions">{{ stats.definitionStats?.length || 0 }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 流程实例列表 -->
    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>运行中流程实例</span>
          <div>
            <el-input
              v-model="searchForm.processDefinitionKey"
              placeholder="流程定义 Key"
              clearable
              style="width: 200px"
              class="mr-2"
              @change="search"
            />
            <el-input
              v-model="searchForm.businessKey"
              placeholder="业务 Key"
              clearable
              style="width: 200px"
              class="mr-2"
              @change="search"
            />
            <el-button type="primary" @click="search">搜索</el-button>
          </div>
        </div>
      </template>

      <el-table :data="instanceList" v-loading="loading" border stripe>
        <el-table-column prop="processInstanceId" label="实例 ID" min-width="200" show-overflow-tooltip />
        <el-table-column prop="processDefinitionKey" label="流程定义 Key" width="150" />
        <el-table-column prop="processDefinitionName" label="流程名称" width="150" />
        <el-table-column prop="businessKey" label="业务 Key" width="150" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isSuspended ? 'warning' : 'success'">
              {{ row.isSuspended ? '已挂起' : '运行中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="startUserId" label="启动人" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">
              详情
            </el-button>
            <el-button type="info" link size="small" @click="viewHistory(row)">
              轨迹
            </el-button>
            <el-button type="warning" link size="small" @click="suspendOrActivate(row)" v-if="!row.isSuspended">
              挂起
            </el-button>
            <el-button type="success" link size="small" @click="suspendOrActivate(row)" v-else>
              激活
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="流程实例详情" width="600px">
      <el-descriptions :column="2" border v-if="currentInstance">
        <el-descriptions-item label="实例 ID">{{ currentInstance.processInstanceId }}</el-descriptions-item>
        <el-descriptions-item label="流程定义 ID">{{ currentInstance.processDefinitionId }}</el-descriptions-item>
        <el-descriptions-item label="流程定义 Key">{{ currentInstance.processDefinitionKey }}</el-descriptions-item>
        <el-descriptions-item label="业务 Key">{{ currentInstance.businessKey }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentInstance.isSuspended ? 'warning' : 'success'">
            {{ currentInstance.isSuspended ? '已挂起' : '运行中' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentInstance.startTime }}</el-descriptions-item>
        <el-descriptions-item label="启动人">{{ currentInstance.startUserId }}</el-descriptions-item>
        <el-descriptions-item label="当前活动">
          <el-tag v-for="activity in currentInstance.activeActivities" :key="activity" size="small">
            {{ activity }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 历史轨迹对话框 -->
    <el-dialog v-model="historyVisible" title="流程历史轨迹" width="800px">
      <el-timeline v-if="historyList.length > 0">
        <el-timeline-item
          v-for="(item, index) in historyList"
          :key="index"
          :timestamp="item.startTime"
          placement="top"
          :color="getActivityColor(item.activityType)"
        >
          <el-card>
            <h4>{{ item.activityName || item.activityId }}</h4>
            <p>活动类型：{{ getActivityTypeName(item.activityType) }}</p>
            <p v-if="item.assignee">处理人：{{ item.assignee }}</p>
            <p v-if="item.duration">耗时：{{ formatDuration(item.duration) }}</p>
            <p v-if="item.endTime">完成时间：{{ item.endTime }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无历史记录" />
      <template #footer>
        <el-button @click="historyVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 统计数据
const stats = ref({})

// 实例列表
const instanceList = ref([])
const loading = ref(false)

// 搜索条件
const searchForm = reactive({
  processDefinitionKey: '',
  businessKey: ''
})

// 对话框
const detailVisible = ref(false)
const historyVisible = ref(false)
const currentInstance = ref(null)
const historyList = ref([])

// 获取统计数据
const fetchStats = async () => {
  try {
    const { data } = await request.get('/system/workflow/stats')
    stats.value = data || {}
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取实例列表
const fetchInstanceList = async () => {
  loading.value = true
  try {
    const { data } = await request.get('/system/workflow/instance/list', {
      params: searchForm
    })
    instanceList.value = data || []
  } catch (error) {
    console.error('获取实例列表失败:', error)
    ElMessage.error('获取流程实例列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const search = () => {
  fetchInstanceList()
}

// 刷新统计
const refreshStats = () => {
  fetchStats()
  ElMessage.success('统计已刷新')
}

// 刷新列表
const refreshList = () => {
  fetchInstanceList()
  ElMessage.success('列表已刷新')
}

// 查看详情
const viewDetail = async (row) => {
  try {
    const { data } = await request.get(`/system/workflow/instance/${row.processInstanceId}`)
    currentInstance.value = data
    detailVisible.value = true
  } catch (error) {
    console.error('获取实例详情失败:', error)
    ElMessage.error('获取实例详情失败')
  }
}

// 查看历史轨迹
const viewHistory = async (row) => {
  try {
    const { data } = await request.get(`/system/workflow/instance/${row.processInstanceId}/history`)
    historyList.value = data || []
    historyVisible.value = true
  } catch (error) {
    console.error('获取历史轨迹失败:', error)
    ElMessage.error('获取历史轨迹失败')
  }
}

// 挂起/激活
const suspendOrActivate = async (row) => {
  const action = row.isSuspended ? '激活' : '挂起'
  const url = row.isSuspended ? '/activate' : '/suspend'

  try {
    await ElMessageBox.confirm(`确定要${action}该流程实例吗？`, '提示', {
      type: 'warning'
    })

    await request.post(`/system/workflow/${url}/${row.processInstanceId}`)
    ElMessage.success(`${action}成功`)
    fetchInstanceList()
    fetchStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${action}失败:`, error)
      ElMessage.error(`${action}失败`)
    }
  }
}

// 获取活动类型名称
const getActivityTypeName = (type) => {
  const typeMap = {
    'startEvent': '开始事件',
    'endEvent': '结束事件',
    'userTask': '用户任务',
    'serviceTask': '服务任务',
    'exclusiveGateway': '排他网关',
    'parallelGateway': '并行网关',
    'intermediateCatchEvent': '中间捕获事件',
    'intermediateThrowEvent': '中间抛出事件'
  }
  return typeMap[type] || type
}

// 获取活动颜色
const getActivityColor = (type) => {
  const colorMap = {
    'startEvent': '#52c41a',
    'endEvent': '#ff4d4f',
    'userTask': '#1890ff',
    'serviceTask': '#722ed1',
    'exclusiveGateway': '#faad14',
    'parallelGateway': '#13c2c2'
  }
  return colorMap[type] || '#1890ff'
}

// 格式化耗时
const formatDuration = (ms) => {
  if (!ms) return '-'
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)

  if (hours > 0) {
    return `${hours}小时${minutes % 60}分钟`
  } else if (minutes > 0) {
    return `${minutes}分钟${seconds % 60}秒`
  } else {
    return `${seconds}秒`
  }
}

onMounted(() => {
  fetchStats()
  fetchInstanceList()
})
</script>

<style scoped lang="scss">
.process-instance-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    span {
      font-weight: bold;
      font-size: 16px;
    }
  }

  .stats-row {
    margin-bottom: 20px;

    .stat-card {
      text-align: center;

      .stat-item {
        padding: 10px;

        .stat-label {
          color: #666;
          font-size: 14px;
          margin-bottom: 10px;
        }

        .stat-value {
          font-size: 32px;
          font-weight: bold;

          &.running {
            color: #52c41a;
          }

          &.suspended {
            color: #faad14;
          }

          &.historic {
            color: #1890ff;
          }

          &.definitions {
            color: #722ed1;
          }
        }
      }
    }
  }

  .mt-4 {
    margin-top: 20px;
  }

  .mr-2 {
    margin-right: 10px;
  }
}
</style>
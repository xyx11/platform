<template>
  <div class="app-container">
    <el-row :gutter="16" class="mb8">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #e6f7ff">
              <el-icon :size="32" color="#1890ff"><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.todoCount || 0 }}</div>
              <div class="stat-label">待办任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #f6ffed">
              <el-icon :size="32" color="#52c41a"><Select /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.doneCount || 0 }}</div>
              <div class="stat-label">已办任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #fff7e6">
              <el-icon :size="32" color="#fa8c16"><Connection /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.runningProcessCount || 0 }}</div>
              <div class="stat-label">进行中流程</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #f9f0ff">
              <el-icon :size="32" color="#722ed1"><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.finishedProcessCount || 0 }}</div>
              <div class="stat-label">已完成流程</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mb8">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>待办任务优先级分布</span>
          </template>
          <div class="chart-container">
            <div class="priority-item" v-for="item in priorityStats" :key="item.label">
              <div class="priority-info">
                <span class="priority-label">{{ item.label }}</span>
                <span class="priority-value">{{ item.value }}</span>
              </div>
              <el-progress :percentage="item.percentage" :status="item.status" />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>流程定义任务分布</span>
          </template>
          <el-table :data="processStats" :show-overflow-tooltip="true">
            <el-table-column label="流程定义" prop="processDefinitionName" />
            <el-table-column label="待办数" prop="pendingCount" width="100" align="center" />
            <el-table-column label="已完成" prop="completedCount" width="100" align="center" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mb8">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>任务趋势统计（最近 7 天）</span>
          </template>
          <div ref="trendChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>任务类型分布</span>
          </template>
          <div ref="typeChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>任务完成情况</span>
          <el-button type="primary" size="small" @click="refreshCompletion">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>
      <el-descriptions :column="4" border>
        <el-descriptions-item label="总任务数">
          {{ completionStats.totalCount || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="已完成数">
          {{ completionStats.completedCount || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="完成率">
          <el-tag type="success">{{ completionStats.completionRate || '0.00%' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="平均处理时长">
          {{ completionStats.avgDurationFormatted || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup name="WorkflowStats">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Document, Select, Connection, CircleCheck, Refresh } from '@element-plus/icons-vue'
import { getOverviewStats, getProcessDefinitionStats, getCompletionStats, getTaskTrend } from '@/api/system/workflow-task'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const stats = ref({})
const priorityStats = ref([])
const processStats = ref([])
const completionStats = ref({})
const trendChartRef = ref(null)
const typeChartRef = ref(null)
let trendChart = null
let typeChart = null

// 获取统计概览
function getOverview() {
  getOverviewStats().then(res => {
    stats.value = res.data
    // 计算优先级百分比
    const total = (res.data.highPriorityCount || 0) +
                  (res.data.normalPriorityCount || 0) +
                  (res.data.lowPriorityCount || 0)
    priorityStats.value = [
      { label: '高优先级', value: res.data.highPriorityCount || 0,
        percentage: total ? Math.round((res.data.highPriorityCount || 0) * 100 / total) : 0,
        status: 'exception' },
      { label: '普通优先级', value: res.data.normalPriorityCount || 0,
        percentage: total ? Math.round((res.data.normalPriorityCount || 0) * 100 / total) : 0,
        status: 'warning' },
      { label: '低优先级', value: res.data.lowPriorityCount || 0,
        percentage: total ? Math.round((res.data.lowPriorityCount || 0) * 100 / total) : 0,
        status: '' }
    ]
    // 更新类型图表
    initTypeChart()
  })
}

// 获取流程定义统计
function getProcessStats() {
  getProcessDefinitionStats().then(res => {
    processStats.value = res.data || []
  })
}

// 获取完成情况统计
function getCompletion() {
  getCompletionStats({}).then(res => {
    completionStats.value = res.data || {}
  })
}

// 刷新完成情况
function refreshCompletion() {
  getCompletionStats({}).then(res => {
    completionStats.value = res.data || {}
    ElMessage.success('刷新成功')
  })
}

// 初始化趋势图表
function initTrendChart() {
  if (!trendChartRef.value) return

  trendChart = echarts.init(trendChartRef.value)

  // 模拟最近 7 天的数据（实际应从 API 获取）
  const dates = []
  const todoData = []
  const doneData = []
  for (let i = 6; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    dates.push(`${date.getMonth() + 1}/${date.getDate()}`)
    todoData.push(Math.floor(Math.random() * 20) + 5)
    doneData.push(Math.floor(Math.random() * 15) + 8)
  }

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['待办任务', '已办任务']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '待办任务',
        type: 'line',
        data: todoData,
        smooth: true,
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.5)' },
            { offset: 1, color: 'rgba(64,158,255,0.05)' }
          ])
        }
      },
      {
        name: '已办任务',
        type: 'line',
        data: doneData,
        smooth: true,
        itemStyle: { color: '#67C23A' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103,194,58,0.5)' },
            { offset: 1, color: 'rgba(103,194,58,0.05)' }
          ])
        }
      }
    ]
  }

  trendChart.setOption(option)

  window.addEventListener('resize', () => {
    trendChart.resize()
  })
}

// 初始化类型图表
function initTypeChart() {
  if (!typeChartRef.value) return

  typeChart = echarts.init(typeChartRef.value)

  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '任务类型',
        type: 'pie',
        radius: '60%',
        data: [
          { value: stats.value.highPriorityCount || 0, name: '高优先级', itemStyle: { color: '#F56C6C' } },
          { value: stats.value.normalPriorityCount || 0, name: '普通优先级', itemStyle: { color: '#E6A23C' } },
          { value: stats.value.lowPriorityCount || 0, name: '低优先级', itemStyle: { color: '#909399' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }

  typeChart.setOption(option)

  window.addEventListener('resize', () => {
    typeChart.resize()
  })
}

// 销毁图表
function disposeCharts() {
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
  if (typeChart) {
    typeChart.dispose()
    typeChart = null
  }
}

onMounted(() => {
  getOverview()
  getProcessStats()
  getCompletion()
  nextTick(() => {
    initTrendChart()
  })
})

// 组件卸载时清理
import { onUnmounted } from 'vue'
onUnmounted(() => {
  disposeCharts()
})
</script>

<style scoped lang="scss">
.app-container {
  .mb8 {
    margin-bottom: 8px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .stat-card {
    :deep(.el-card__body) {
      padding: 20px;
    }

    .stat-item {
      display: flex;
      align-items: center;

      .stat-icon {
        width: 64px;
        height: 64px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;
      }

      .stat-content {
        flex: 1;

        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #333;
        }

        .stat-label {
          font-size: 14px;
          color: #999;
          margin-top: 5px;
        }
      }
    }
  }

  .chart-container {
    padding: 10px 0;

    .priority-item {
      margin-bottom: 20px;

      .priority-info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;

        .priority-label {
          font-size: 14px;
          color: #666;
        }

        .priority-value {
          font-size: 14px;
          font-weight: bold;
          color: #333;
        }
      }
    }
  }

  .chart-box {
    height: 300px;
    width: 100%;
  }
}
</style>
<template>
  <div class="workflow-stats-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <el-icon class="stat-icon" color="#52c41a"><VideoPlay /></el-icon>
            <div class="stat-content">
              <div class="stat-value running">{{ stats.runningCount || 0 }}</div>
              <div class="stat-label">运行中实例</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <el-icon class="stat-icon" color="#faad14"><VideoPause /></el-icon>
            <div class="stat-content">
              <div class="stat-value suspended">{{ stats.suspendedCount || 0 }}</div>
              <div class="stat-label">已挂起实例</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <el-icon class="stat-icon" color="#1890ff"><Checked /></el-icon>
            <div class="stat-content">
              <div class="stat-value historic">{{ stats.historicCount || 0 }}</div>
              <div class="stat-label">历史完成实例</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <el-icon class="stat-icon" color="#722ed1"><Flowchart /></el-icon>
            <div class="stat-content">
              <div class="stat-value definitions">{{ stats.definitionCount || 0 }}</div>
              <div class="stat-label">流程定义数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 更多统计卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <el-icon class="stat-icon" color="#13c2c2"><Document /></el-icon>
            <div class="stat-content">
              <div class="stat-value">{{ stats.taskCount || 0 }}</div>
              <div class="stat-label">总任务数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <el-icon class="stat-icon" color="#409EFF"><Bell /></el-icon>
            <div class="stat-content">
              <div class="stat-value todo">{{ stats.todoTaskCount || 0 }}</div>
              <div class="stat-label">待办任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <el-icon class="stat-icon" color="#67C23A"><Select /></el-icon>
            <div class="stat-content">
              <div class="stat-value done">{{ stats.doneTaskCount || 0 }}</div>
              <div class="stat-label">已办任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <el-icon class="stat-icon" color="#E6A23A"><Form /></el-icon>
            <div class="stat-content">
              <div class="stat-value forms">{{ stats.formCount || 0 }}</div>
              <div class="stat-label">表单数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>流程实例趋势</span>
              <el-radio-group v-model="trendPeriod" size="small" @change="loadTrendData">
                <el-radio-button label="week">近 7 天</el-radio-button>
                <el-radio-button label="month">近 30 天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>流程定义分布</span>
          </template>
          <div ref="pieChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span>部门流程统计</span>
          </template>
          <div ref="barChartRef" class="chart-bar"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 流程定义统计表格 -->
    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>流程定义统计</span>
          <el-button type="primary" size="small" @click="refreshStats">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>
      <el-table :data="definitionStats" v-loading="loading" border stripe>
        <el-table-column prop="key" label="流程定义 Key" min-width="150" show-overflow-tooltip />
        <el-table-column prop="name" label="流程名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.category === 'hr'">人事</el-tag>
            <el-tag v-else-if="row.category === 'finance'" type="success">财务</el-tag>
            <el-tag v-else-if="row.category === 'admin'" type="warning">行政</el-tag>
            <el-tag v-else-if="row.category === 'purchase'" type="danger">采购</el-tag>
            <el-tag v-else>其他</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" align="center" />
        <el-table-column prop="instanceCount" label="实例数" width="100" align="center" />
        <el-table-column prop="runningCount" label="运行中" width="100" align="center">
          <template #default="{ row }">
            <span :class="{ 'text-running': row.runningCount > 0 }">{{ row.runningCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="suspendedCount" label="已挂起" width="100" align="center">
          <template #default="{ row }">
            <span :class="{ 'text-suspended': row.suspendedCount > 0 }">{{ row.suspendedCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="deploymentTime" label="部署时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="WorkflowStats">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoPlay, VideoPause, Checked, Flowchart, Document, Bell, Select, Form, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const loading = ref(false)
const trendPeriod = ref('week')

const stats = ref({
  runningCount: 0,
  suspendedCount: 0,
  historicCount: 0,
  definitionCount: 0,
  taskCount: 0,
  todoTaskCount: 0,
  doneTaskCount: 0,
  formCount: 0
})

const definitionStats = ref([])

const trendChartRef = ref(null)
const pieChartRef = ref(null)
const barChartRef = ref(null)

let trendChart = null
let pieChart = null
let barChart = null

// 获取统计数据
const fetchStats = async () => {
  try {
    const { data } = await request.get('/system/workflow/stats')
    stats.value = data || {}
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取流程定义统计
const fetchDefinitionStats = async () => {
  loading.value = true
  try {
    const { data } = await request.get('/system/workflow/stats/definitions')
    definitionStats.value = data || []
  } catch (error) {
    console.error('获取流程定义统计失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  try {
    const { data } = await request.get('/system/workflow/stats/trend', {
      params: { period: trendPeriod.value }
    })
    initTrendChart(data)
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

// 初始化趋势图表
const initTrendChart = (data) => {
  if (!trendChartRef.value) return

  trendChart = echarts.init(trendChartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['启动', '完成']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data?.dates || []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '启动',
        type: 'bar',
        data: data?.started || [],
        itemStyle: { color: '#52c41a' }
      },
      {
        name: '完成',
        type: 'bar',
        data: data?.completed || [],
        itemStyle: { color: '#1890ff' }
      }
    ]
  }

  trendChart.setOption(option)
}

// 初始化饼图
const initPieChart = (data) => {
  if (!pieChartRef.value) return

  pieChart = echarts.init(pieChartRef.value)

  const categoryData = {}
  data?.forEach(item => {
    const category = item.category || '其他'
    if (!categoryData[category]) {
      categoryData[category] = 0
    }
    categoryData[category]++
  })

  const pieData = Object.entries(categoryData).map(([name, value]) => ({ name, value }))

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
        name: '流程定义',
        type: 'pie',
        radius: '50%',
        data: pieData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          formatter: '{b}: {c} ({d}%)'
        }
      }
    ]
  }

  pieChart.setOption(option)
}

// 初始化柱状图
const initBarChart = (data) => {
  if (!barChartRef.value) return

  barChart = echarts.init(barChartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '实例数'
    },
    yAxis: {
      type: 'category',
      data: data?.map(item => item.name) || []
    },
    series: [
      {
        name: '实例数',
        type: 'bar',
        data: data?.map(item => item.value) || [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }
    ]
  }

  barChart.setOption(option)
}

// 刷新统计
const refreshStats = () => {
  fetchStats()
  fetchDefinitionStats()
  loadTrendData()
  ElMessage.success('刷新成功')
}

// 窗口大小变化时重绘图表
const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
  barChart?.resize()
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)

  await fetchStats()
  await fetchDefinitionStats()
  await loadTrendData()

  // 初始化图表
  setTimeout(() => {
    loadTrendData()
    initPieChart(definitionStats.value)
    initBarChart([
      { name: '人事部', value: 15 },
      { name: '财务部', value: 22 },
      { name: '行政部', value: 18 },
      { name: '采购部', value: 12 },
      { name: '销售部', value: 25 }
    ])
  }, 100)
})
</script>

<style scoped lang="scss">
.workflow-stats-container {
  padding: 20px;

  .mb-4 {
    margin-bottom: 20px;
  }

  .mt-4 {
    margin-top: 20px;
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
        font-size: 40px;
        margin-right: 15px;
      }

      .stat-content {
        flex: 1;

        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #333;

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

          &.todo {
            color: #409EFF;
          }

          &.done {
            color: #67C23A;
          }
        }

        .stat-label {
          font-size: 14px;
          color: #999;
          margin-top: 5px;
        }
      }
    }
  }

  .chart {
    height: 300px;
    width: 100%;
  }

  .chart-bar {
    height: 400px;
    width: 100%;
  }

  .text-running {
    color: #52c41a;
    font-weight: bold;
  }

  .text-suspended {
    color: #faad14;
    font-weight: bold;
  }
}
</style>
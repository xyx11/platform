<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="3" v-for="(stat, index) in statList" :key="index">
        <div class="stat-card">
          <div class="stat-icon" :style="{ background: stat.bgColor }">
            <el-icon :size="24" color="#fff"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-value" :style="{ color: stat.color }">{{ formatNumber(stat.value) }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="charts">
      <el-col :span="16">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">访问趋势</span>
              <el-radio-group size="small" v-model="trendPeriod">
                <el-radio-button label="7 天">7 天</el-radio-button>
                <el-radio-button label="30 天">30 天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="lineChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">用户分布</span>
            </div>
          </template>
          <div ref="pieChartRef" class="chart pie-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 & 通知公告 -->
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">快捷入口</span>
            </div>
          </template>
          <div class="quick-links">
            <div
              v-for="(item, index) in quickLinks"
              :key="index"
              class="quick-link"
              @click="router.push(item.path)"
            >
              <div class="quick-link-icon" :style="{ background: item.bgColor }">
                <el-icon :size="22" color="#fff"><component :is="item.icon" /></el-icon>
              </div>
              <span class="quick-link-name">{{ item.name }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">通知公告</span>
              <el-link type="primary" :underline="false" size="small" @click="router.push('/system/notice')">
                全部 <el-icon><ArrowRight /></el-icon>
              </el-link>
            </div>
          </template>
          <div class="notice-list">
            <div
              v-for="(notice, index) in noticeList"
              :key="index"
              class="notice-item"
              @click="router.push('/system/notice')"
            >
              <span class="notice-tag" :class="'tag-' + notice.type">{{ notice.typeName }}</span>
              <span class="notice-title">{{ notice.title }}</span>
              <span class="notice-date">{{ notice.date }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import request from '@/utils/request'
import {
  User, Avatar, Menu, Document, Bell, Timer, Clock, Warning,
  ArrowRight, OfficeBuilding, Collection, Grid, Monitor
} from '@element-plus/icons-vue'

// 渐变背景常量
const GRADIENTS = {
  total: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  process: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  start: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  task: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  user: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  dept: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
  role: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
  menu: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
  notice: 'linear-gradient(135deg, #f6d365 0%, #fda085 100%)',
  log: 'linear-gradient(135deg, #5ee7df 0%, #b490ca 100%)',
  urgent: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)'
}

const router = useRouter()
const lineChartRef = ref(null)
const pieChartRef = ref(null)
const trendPeriod = ref('7 天')

const statistics = reactive({
  userCount: 0,
  roleCount: 0,
  menuCount: 0,
  logCount: 0,
  deptCount: 0,
  postCount: 0,
  noticeCount: 0,
  jobCount: 0,
  todoPendingCount: 0,
  todoUrgentCount: 0
})

// 统计卡片数据
const statList = computed(() => [
  { label: '用户总数', value: statistics.userCount, icon: User, color: '#1e80ff', bgColor: GRADIENTS.total, trend: 12 },
  { label: '角色总数', value: statistics.roleCount, icon: Avatar, color: '#00b578', bgColor: GRADIENTS.process, trend: 5 },
  { label: '菜单总数', value: statistics.menuCount, icon: Menu, color: '#ff7a45', bgColor: GRADIENTS.start, trend: 8 },
  { label: '日志总数', value: statistics.logCount, icon: Document, color: '#36cfc9', bgColor: GRADIENTS.task, trend: 15 },
  { label: '公告总数', value: statistics.noticeCount, icon: Bell, color: '#722ed1', bgColor: GRADIENTS.role, trend: 3 },
  { label: '任务总数', value: statistics.jobCount, icon: Timer, color: '#f04e98', bgColor: GRADIENTS.user, trend: 20 },
  { label: '待处理待办', value: statistics.todoPendingCount, icon: Clock, color: '#faad14', bgColor: GRADIENTS.total, trend: 7 },
  { label: '紧急待办', value: statistics.todoUrgentCount, icon: Warning, color: '#ff4d4f', bgColor: GRADIENTS.urgent, trend: 2 }
])

// 快捷入口
const quickLinks = [
  { name: '用户管理', path: '/system/user', icon: User, bgColor: '#1e80ff' },
  { name: '角色管理', path: '/system/role', icon: Avatar, bgColor: '#00b578' },
  { name: '菜单管理', path: '/system/menu', icon: Menu, bgColor: '#ff7a45' },
  { name: '部门管理', path: '/system/dept', icon: OfficeBuilding, bgColor: '#722ed1' },
  { name: '字典管理', path: '/system/dict', icon: Collection, bgColor: '#36cfc9' },
  { name: '待办事项', path: '/system/todo', icon: Clock, bgColor: '#faad14' },
  { name: '定时任务', path: '/job/list', icon: Timer, bgColor: '#f04e98' },
  { name: '代码生成', path: '/generator/table', icon: Grid, bgColor: '#1e80ff' },
  { name: '系统监控', path: '/monitor/server', icon: Monitor, bgColor: '#00b578' }
]

// 通知公告数据
const noticeList = ref([
  { type: 'notice', typeName: '公告', title: '系统将于本周末进行维护升级', date: '03-05' },
  { type: 'news', typeName: '资讯', title: '平台新版本功能介绍', date: '03-03' },
  { type: 'notice', typeName: '公告', title: '关于加强账户安全管理的通知', date: '02-28' },
  { type: 'news', typeName: '资讯', title: '微服务架构最佳实践分享', date: '02-25' }
])

// 获取统计数据
const getStatistics = () => {
  request.get('/system/index/statistics').then(res => {
    Object.assign(statistics, res.data)
    request.get('/system/todo/stats').then(todoRes => {
      statistics.todoPendingCount = todoRes.data.pendingCount || 0
      statistics.todoUrgentCount = todoRes.data.urgentCount || 0
    }).catch(() => {})
  }).catch(() => {})
}

const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  return num
}

// 初始化折线图
const initLineChart = () => {
  if (!lineChartRef.value) return
  const chart = echarts.init(lineChartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e8eaed',
      textStyle: { color: '#333' },
      extraCssText: 'box-shadow: 0 4px 12px rgba(0,0,0,0.1); border-radius: 8px;'
    },
    grid: {
      left: '50px',
      right: '20px',
      bottom: '30px',
      top: '10px',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisLine: { lineStyle: { color: '#e8eaed' } },
      axisLabel: { color: '#666', margin: 8 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f5f6f7', type: 'dashed' } },
      axisLabel: { color: '#666', margin: 8 }
    },
    series: [{
      name: '访问量',
      type: 'line',
      smooth: true,
      data: [820, 932, 901, 934, 1290, 1330, 1320],
      symbol: 'circle',
      symbolSize: 6,
      itemStyle: {
        color: '#1e80ff',
        borderWidth: 2,
        borderColor: '#fff'
      },
      lineStyle: {
        width: 2,
        color: '#1e80ff'
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(30, 128, 255, 0.25)' },
          { offset: 1, color: 'rgba(30, 128, 255, 0.02)' }
        ])
      }
    }]
  }

  chart.setOption(option)
  window.addEventListener('resize', () => { chart.resize() })
}

// 初始化饼图
const initPieChart = () => {
  if (!pieChartRef.value) return
  const chart = echarts.init(pieChartRef.value)

  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff',
      borderColor: '#e8eaed',
      textStyle: { color: '#333' },
      extraCssText: 'box-shadow: 0 4px 12px rgba(0,0,0,0.1); border-radius: 8px;'
    },
    legend: {
      bottom: '0%',
      left: 'center',
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { color: '#666' }
    },
    series: [{
      name: '用户来源',
      type: 'pie',
      radius: ['50%', '70%'],
      center: ['50%', '42%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 16,
          fontWeight: '600',
          color: '#333'
        }
      },
      labelLine: {
        show: false
      },
      data: [
        { value: 1048, name: '搜索引擎', itemStyle: { color: '#1e80ff' } },
        { value: 735, name: '直接访问', itemStyle: { color: '#00b578' } },
        { value: 580, name: '邮件营销', itemStyle: { color: '#ff7a45' } },
        { value: 484, name: '联盟广告', itemStyle: { color: '#722ed1' } },
        { value: 300, name: '视频广告', itemStyle: { color: '#36cfc9' } }
      ]
    }]
  }

  chart.setOption(option)
  window.addEventListener('resize', () => { chart.resize() })
}

onMounted(() => {
  getStatistics()
  initLineChart()
  initPieChart()
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 16px;
  background: #f5f6f7;
  min-height: calc(100vh - 104px);
}

.stat-cards {
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
  height: 80px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  }

  .stat-content {
    flex: 1;
    padding-left: 12px;

    .stat-label {
      font-size: 13px;
      color: #666;
      margin-bottom: 6px;
    }

    .stat-value {
      font-size: 24px;
      font-weight: 600;
    }
  }
}

.charts {
  margin-bottom: 16px;

  .chart-card {
    border-radius: 8px;

    :deep(.el-card__header) {
      background: #fff;
      border-bottom: 1px solid #f0f0f0;
      padding: 14px 16px;
    }

    :deep(.el-card__body) {
      padding: 16px;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card-title {
        font-size: 15px;
        font-weight: 600;
        color: #1a1a1a;
      }
    }

    .chart {
      height: 300px;
      width: 100%;
    }

    .pie-chart {
      height: 280px;
    }
  }
}

.section-card {
  border-radius: 8px;

  :deep(.el-card__header) {
    background: #fff;
    border-bottom: 1px solid #f0f0f0;
    padding: 14px 16px;
  }

  :deep(.el-card__body) {
    padding: 16px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      font-size: 15px;
      font-weight: 600;
      color: #1a1a1a;
    }
  }
}

.quick-links {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;

  .quick-link {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16px 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    background: #fafafa;

    &:hover {
      background: #f0f2f5;
      transform: translateY(-2px);
    }

    .quick-link-icon {
      width: 44px;
      height: 44px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 10px;
      box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
    }

    .quick-link-name {
      font-size: 13px;
      color: #666;
    }
  }
}

.notice-list {
  .notice-item {
    display: flex;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid #f5f6f7;
    cursor: pointer;
    transition: all 0.2s ease;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      .notice-title {
        color: #1e80ff;
      }
    }

    .notice-tag {
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 12px;
      margin-right: 10px;
      flex-shrink: 0;

      &.tag-notice {
        background: #e6f7ff;
        color: #1e80ff;
      }

      &.tag-news {
        background: #f6ffed;
        color: #00b578;
      }
    }

    .notice-title {
      flex: 1;
      font-size: 13px;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      transition: color 0.2s ease;
    }

    .notice-date {
      font-size: 12px;
      color: #999;
      margin-left: 10px;
    }
  }
}
</style>
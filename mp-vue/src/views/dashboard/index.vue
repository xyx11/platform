<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon user">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.userCount || 0 }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon role">
              <el-icon><Avatar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.roleCount || 0 }}</div>
              <div class="stat-label">角色总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon menu">
              <el-icon><Menu /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.menuCount || 0 }}</div>
              <div class="stat-label">菜单总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon log">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.logCount || 0 }}</div>
              <div class="stat-label">日志总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>访问趋势</span>
            </div>
          </template>
          <div ref="lineChartRef" class="chart"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户分布</span>
            </div>
          </template>
          <div ref="pieChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-card shadow="hover" class="quick-links">
      <template #header>
        <div class="card-header">
          <span>快捷入口</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="4" v-for="item in quickLinks" :key="item.path" @click="router.push(item.path)">
          <div class="quick-link">
            <el-icon :size="32"><component :is="item.icon" /></el-icon>
            <span>{{ item.name }}</span>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import request from '@/utils/request'
// 导入 Element Plus 图标
import { User, Avatar, Menu, Document, OfficeBuilding, Collection, Timer, Setting, Grid, Monitor } from '@element-plus/icons-vue'

const router = useRouter()
const lineChartRef = ref(null)
const pieChartRef = ref(null)

const statistics = reactive({
  userCount: 0,
  roleCount: 0,
  menuCount: 0,
  logCount: 0,
  deptCount: 0,
  postCount: 0,
  noticeCount: 0,
  jobCount: 0
})

const quickLinks = [
  { name: '用户管理', path: '/system/user', icon: User },
  { name: '角色管理', path: '/system/role', icon: Avatar },
  { name: '菜单管理', path: '/system/menu', icon: Menu },
  { name: '部门管理', path: '/system/dept', icon: OfficeBuilding },
  { name: '字典管理', path: '/system/dict', icon: Collection },
  { name: '定时任务', path: '/job/list', icon: Timer },
  { name: '代码生成', path: '/generator/table', icon: Grid },
  { name: '系统监控', path: '/monitor/server', icon: Monitor },
  { name: '系统设置', path: '/system/config', icon: Setting }
]

// 获取统计数据
const getStatistics = () => {
  request.get('/system/index/statistics').then(res => {
    Object.assign(statistics, res.data)
  }).catch(() => {})
}

// 初始化折线图
const initLineChart = () => {
  if (!lineChartRef.value) return
  const chart = echarts.init(lineChartRef.value)
  
  // 获取访问趋势数据
  request.get('/system/index/visitTrend').then(res => {
    const { dates, values } = res.data
    chart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates || []
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '访问量',
          type: 'line',
          smooth: true,
          data: values || [],
          itemStyle: {
            color: '#409EFF'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.01)' }
            ])
          }
        }
      ]
    })
  }).catch(() => {
    // 使用默认数据
    chart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
      yAxis: { type: 'value' },
      series: [{
        name: '访问量',
        type: 'line',
        smooth: true,
        data: [820, 932, 901, 934, 1290, 1330, 1320],
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.01)' }
          ])
        }
      }]
    })
  })

  window.addEventListener('resize', () => { chart.resize() })
}

// 初始化饼图
const initPieChart = () => {
  if (!pieChartRef.value) return
  const chart = echarts.init(pieChartRef.value)
  
  // 获取用户分布数据
  request.get('/system/index/userDistribution').then(res => {
    const { data } = res.data
    chart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '5%', left: 'center' },
      series: [{
        name: '用户来源',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
        labelLine: { show: false },
        data: data || []
      }]
    })
  }).catch(() => {
    // 使用默认数据
    chart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '5%', left: 'center' },
      series: [{
        name: '用户来源',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
        labelLine: { show: false },
        data: [
          { value: 1048, name: '搜索引擎' },
          { value: 735, name: '直接访问' },
          { value: 580, name: '邮件营销' },
          { value: 484, name: '联盟广告' },
          { value: 300, name: '视频广告' }
        ]
      }]
    })
  })

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
  padding: 20px;
  min-height: calc(100vh - 84px);
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  .stat-item {
    display: flex;
    align-items: center;
    gap: 15px;
    padding: 10px;
  }

  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: #fff;

    &.user {
      background: linear-gradient(135deg, #667eea, #764ba2);
    }

    &.role {
      background: linear-gradient(135deg, #f093fb, #f5576c);
    }

    &.menu {
      background: linear-gradient(135deg, #4facfe, #00f2fe);
    }

    &.log {
      background: linear-gradient(135deg, #43e97b, #38f9d7);
    }
  }

  .stat-info {
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

.charts {
  margin-bottom: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .chart {
    height: 300px;
    width: 100%;
  }
}

.quick-links {
  .quick-link {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px;
    cursor: pointer;
    transition: all 0.3s;
    border-radius: 8px;

    &:hover {
      background-color: #f5f7fa;
      transform: translateY(-3px);
    }

    span {
      margin-top: 10px;
      font-size: 14px;
      color: #606266;
    }
  }
}
</style>

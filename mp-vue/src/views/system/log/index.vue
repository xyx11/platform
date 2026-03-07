<template>
  <div class="log-container">
    <el-card>
      <el-form :model="queryParams" inline>
        <el-form-item label="模块名称">
          <el-input v-model="queryParams.module" placeholder="请输入模块名称" clearable />
        </el-form-item>
        <el-form-item label="操作人员">
          <el-input v-model="queryParams.operatorName" placeholder="请输入操作人员" clearable />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-input v-model="queryParams.operationType" placeholder="请输入操作类型" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="header-actions">
          <el-button type="danger" icon="Delete" :disabled="multiple" @click="handleBatchDelete">批量删除</el-button>
          <el-button type="danger" icon="Delete" @click="handleClear">清空日志</el-button>
          <el-button type="success" icon="Download" @click="handleExport">导出日志</el-button>
        </div>
      </template>

      <el-table
        :data="logList"
        border
        stripe
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="日志 ID" width="100" />
        <el-table-column prop="module" label="模块名称" width="120" />
        <el-table-column prop="description" label="操作描述" min-width="150" />
        <el-table-column prop="requestMethod" label="请求方法" width="100" />
        <el-table-column prop="requestUrl" label="请求 URL" min-width="200" />
        <el-table-column prop="operatorName" label="操作人员" width="100" />
        <el-table-column prop="operationIp" label="操作 IP" width="140" />
        <el-table-column prop="executeTime" label="执行时间" width="100">
          <template #default="{ row }">
            <span>{{ row.executeTime }}ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getLogList"
          @current-change="getLogList"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog title="日志详情" v-model="detailDialog.visible" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志 ID">{{ detailLog.id }}</el-descriptions-item>
        <el-descriptions-item label="模块名称">{{ detailLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ detailLog.operationType }}</el-descriptions-item>
        <el-descriptions-item label="操作描述">{{ detailLog.description }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ detailLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求 URL">{{ detailLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="操作人员">{{ detailLog.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="操作 IP">{{ detailLog.operationIp }}</el-descriptions-item>
        <el-descriptions-item label="操作地点">{{ detailLog.operationLocation }}</el-descriptions-item>
        <el-descriptions-item label="浏览器">{{ detailLog.browser }}</el-descriptions-item>
        <el-descriptions-item label="操作系统">{{ detailLog.os }}</el-descriptions-item>
        <el-descriptions-item label="执行时间">{{ detailLog.executeTime }}ms</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailLog.status === 1 ? 'success' : 'danger'">
            {{ detailLog.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detailLog.createTime }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <el-input v-model="detailLog.requestParams" type="textarea" :rows="3" readonly />
        </el-descriptions-item>
        <el-descriptions-item label="响应结果" :span="2">
          <el-input v-model="detailLog.responseResult" type="textarea" :rows="3" readonly />
        </el-descriptions-item>
        <el-descriptions-item label="错误消息" :span="2" v-if="detailLog.errorMsg">
          <el-input v-model="detailLog.errorMsg" type="textarea" :rows="3" readonly />
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const logList = ref([])
const loading = ref(false)
const multiple = ref(true)
const logIds = ref([])

const queryParams = reactive({
  module: '',
  operationType: '',
  operatorName: '',
  status: null,
  createTime: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const detailDialog = reactive({
  visible: false
})

const detailLog = reactive({
  id: null,
  module: '',
  operationType: '',
  description: '',
  requestMethod: '',
  requestUrl: '',
  requestParams: '',
  responseResult: '',
  operationIp: '',
  operationLocation: '',
  browser: '',
  os: '',
  executeTime: 0,
  operatorId: null,
  operatorName: '',
  deptId: null,
  deptName: '',
  status: 0,
  errorMsg: '',
  createTime: null
})

// 获取日志列表
const getLogList = () => {
  loading.value = true
  request.get('/system/log/list', { params: queryParams }).then(res => {
    logList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// 查询
const handleQuery = () => {
  pagination.current = 1
  getLogList()
}

// 重置
const resetQuery = () => {
  queryParams.module = ''
  queryParams.operationType = ''
  queryParams.operatorName = ''
  queryParams.status = null
  queryParams.createTime = null
  handleQuery()
}

// 查看详情
const handleDetail = (row) => {
  detailDialog.visible = true
  Object.assign(detailLog, row)
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  logIds.value = selection.map(item => item.id)
  multiple.value = !selection.length
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确认删除选中的 ${logIds.value.length} 条日志吗？`, '警告', {
    type: 'warning'
  }).then(() => {
    request.delete('/system/log/batch', { data: logIds.value }).then(() => {
      ElMessage.success('批量删除成功')
      getLogList()
    })
  })
}

// 清空日志
const handleClear = () => {
  ElMessageBox.confirm('确认清空所有操作日志吗？此操作不可恢复！', '警告', {
    type: 'warning'
  }).then(() => {
    request.delete('/system/log/clear').then(() => {
      ElMessage.success('清空成功')
      getLogList()
    })
  })
}

// 导出日志
const handleExport = () => {
  const params = {
    ...queryParams,
    pageNum: 1,
    pageSize: 1000
  }
  request.get('/system/log/export', { params, responseType: 'blob' }).then(res => {
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '操作日志_' + new Date().getTime() + '.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  })
}

onMounted(() => {
  getLogList()
})
</script>

<style lang="scss" scoped>
.log-container {
  .table-card {
    margin-top: 20px;

    .header-actions {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
    }

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
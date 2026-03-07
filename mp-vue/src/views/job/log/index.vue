<template>
  <div class="job-log-container">
    <el-card>
      <!-- 搜索栏 -->
      <el-form :model="queryParams" inline>
        <el-form-item label="任务名称">
          <el-input v-model="queryParams.jobName" placeholder="请输入任务名称" clearable />
        </el-form-item>
        <el-form-item label="执行状态">
          <el-select v-model="queryParams.status" placeholder="请选择执行状态" clearable>
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
          <el-button type="danger" icon="Delete" @click="handleBatchDelete" :disabled="selectedIds.length === 0">
            批量删除
          </el-button>
          <el-button type="warning" icon="Delete" @click="handleClear">清空日志</el-button>
        </div>
      </template>

      <el-table 
        :data="jobLogList" 
        border 
        stripe 
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="jobLogId" label="日志 ID" width="80" />
        <el-table-column prop="jobName" label="任务名称" min-width="150" />
        <el-table-column prop="jobGroup" label="任务组名" width="100" />
        <el-table-column prop="invokeTarget" label="调用目标" min-width="200" :show-overflow-tooltip="true" />
        <el-table-column prop="jobMessage" label="日志信息" min-width="200" :show-overflow-tooltip="true" />
        <el-table-column prop="status" label="执行状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="执行时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-show="pagination.total > 0"
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getJobLogList"
          @current-change="getJobLogList"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog title="任务日志详情" v-model="detailDialog.visible" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志 ID">{{ detailLog.jobLogId }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ detailLog.jobName }}</el-descriptions-item>
        <el-descriptions-item label="任务组名">{{ detailLog.jobGroup }}</el-descriptions-item>
        <el-descriptions-item label="调用目标">{{ detailLog.invokeTarget }}</el-descriptions-item>
        <el-descriptions-item label="日志信息" :span="2">{{ detailLog.jobMessage }}</el-descriptions-item>
        <el-descriptions-item label="执行状态">
          <el-tag :type="detailLog.status === 1 ? 'success' : 'danger'">
            {{ detailLog.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="执行时间">{{ detailLog.createTime }}</el-descriptions-item>
        <el-descriptions-item label="异常信息" :span="2" v-if="detailLog.exceptionInfo">
          <el-input v-model="detailLog.exceptionInfo" type="textarea" :rows="5" readonly />
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

const jobLogList = ref([])
const loading = ref(false)
const selectedIds = ref([])

const queryParams = reactive({
  jobName: '',
  status: null
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
  jobLogId: null,
  jobName: '',
  jobGroup: '',
  invokeTarget: '',
  jobMessage: '',
  status: null,
  createTime: '',
  exceptionInfo: ''
})

// 获取任务日志列表
const getJobLogList = () => {
  loading.value = true
  request.get('/system/job/log/list', { params: queryParams }).then(res => {
    jobLogList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// 查询
const handleQuery = () => {
  pagination.current = 1
  getJobLogList()
}

// 重置
const resetQuery = () => {
  queryParams.jobName = ''
  queryParams.status = null
  handleQuery()
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.jobLogId)
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条日志吗？`, '警告', {
    type: 'warning'
  }).then(() => {
    request.delete('/system/job/log/batch', selectedIds.value.join(',')).then(() => {
      ElMessage.success('删除成功')
      getJobLogList()
    })
  })
}

// 清空日志
const handleClear = () => {
  ElMessageBox.confirm('确认清空所有任务日志吗？此操作不可恢复！', '警告', {
    type: 'warning'
  }).then(() => {
    request.delete('/system/job/log/clear').then(() => {
      ElMessage.success('清空成功')
      getJobLogList()
    })
  })
}

// 详情
const handleDetail = (row) => {
  Object.assign(detailLog, row)
  detailDialog.visible = true
}

onMounted(() => {
  getJobLogList()
})
</script>

<style lang="scss" scoped>
.job-log-container {
  .table-card {
    margin-top: 20px;
  }

  .header-actions {
    display: flex;
    gap: 10px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>

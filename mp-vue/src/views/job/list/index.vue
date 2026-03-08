<template>
  <div class="job-container">
    <el-card>
      <!-- 搜索栏 -->
      <el-form :model="queryParams" inline>
        <el-form-item label="任务名称">
          <el-input v-model="queryParams.jobName" placeholder="请输入任务名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="任务组名">
          <el-select v-model="queryParams.jobGroup" placeholder="请选择任务组名" clearable>
            <el-option label="系统任务" value="system" />
            <el-option label="备份任务" value="backup" />
            <el-option label="分析任务" value="analysis" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="暂停" :value="0" />
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
          <el-button type="primary" icon="Plus" @click="handleAdd">新增任务</el-button>
          <el-button type="danger" icon="Delete" :disabled="multiple" @click="handleBatchDelete">批量删除</el-button>
          <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
        </div>
      </template>

      <el-table :data="jobList" border stripe v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="jobId" label="任务 ID" width="80" />
        <el-table-column prop="jobName" label="任务名称" min-width="150" />
        <el-table-column prop="jobGroup" label="任务组名" width="100">
          <template #default="{ row }">
            <el-tag>{{ getGroupName(row.jobGroup) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="invokeTarget" label="调用目标" min-width="200" :show-overflow-tooltip="true" />
        <el-table-column prop="cronExpression" label="Cron 表达式" width="150" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '正常' : '暂停' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              link
              type="warning"
              icon="VideoPause"
              @click="handleStop(row)"
            >暂停</el-button>
            <el-button
              v-else
              link
              type="success"
              icon="VideoPlay"
              @click="handleStart(row)"
            >启动</el-button>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
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
          @size-change="getJobList"
          @current-change="getJobList"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="700px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="任务名称" prop="jobName">
          <el-input v-model="form.jobName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务组名" prop="jobGroup">
          <el-select v-model="form.jobGroup" placeholder="请选择任务组名">
            <el-option label="系统任务" value="system" />
            <el-option label="备份任务" value="backup" />
            <el-option label="分析任务" value="analysis" />
            <el-option label="其他任务" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="调用目标" prop="invokeTarget">
          <el-input v-model="form.invokeTarget" placeholder="请输入调用目标，如：SystemTask.clean()" />
          <div class="form-tip">Bean 调用示例：systemTask.clean() 或 类全路径 # 方法</div>
        </el-form-item>
        <el-form-item label="Cron 表达式" prop="cronExpression">
          <el-input v-model="form.cronExpression" placeholder="请输入 Cron 表达式" />
          <div class="form-tip">如：0 0 2 * * ? 表示每天凌晨 2 点执行</div>
        </el-form-item>
        <el-form-item label="执行策略" prop="misfirePolicy">
          <el-radio-group v-model="form.misfirePolicy">
            <el-radio :label="1">立即执行</el-radio>
            <el-radio :label="2">取消执行</el-radio>
            <el-radio :label="3">跳过执行</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="并发执行" prop="concurrent">
          <el-radio-group v-model="form.concurrent">
            <el-radio :label="0">允许</el-radio>
            <el-radio :label="1">禁止</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">暂停</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const jobList = ref([])
const loading = ref(false)
const multiple = ref(true)
const jobIds = ref([])

const queryParams = reactive({
  jobName: '',
  jobGroup: '',
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const dialog = reactive({
  visible: false,
  title: ''
})

const formRef = ref(null)

const form = reactive({
  jobId: null,
  jobName: '',
  jobGroup: '',
  invokeTarget: '',
  cronExpression: '',
  misfirePolicy: 3,
  concurrent: 1,
  status: 1,
  remark: ''
})

const rules = {
  jobName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  jobGroup: [{ required: true, message: '请选择任务组名', trigger: 'change' }],
  invokeTarget: [{ required: true, message: '请输入调用目标', trigger: 'blur' }],
  cronExpression: [{ required: true, message: '请输入 Cron 表达式', trigger: 'blur' }]
}

// 获取任务列表
const getJobList = () => {
  loading.value = true
  request.get('/job/list', { params: queryParams }).then(res => {
    jobList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// 查询
const handleQuery = () => {
  pagination.current = 1
  getJobList()
}

// 重置
const resetQuery = () => {
  queryParams.jobName = ''
  queryParams.jobGroup = ''
  queryParams.status = null
  handleQuery()
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  jobIds.value = selection.map(item => item.jobId)
  multiple.value = !selection.length
}

// 获取组名
const getGroupName = (group) => {
  const names = {
    system: '系统任务',
    backup: '备份任务',
    analysis: '分析任务',
    other: '其他任务'
  }
  return names[group] || group
}

// 新增
const handleAdd = () => {
  resetForm()
  dialog.title = '新增任务'
  dialog.visible = true
}

// 修改
const handleUpdate = (row) => {
  resetForm()
  dialog.title = '修改任务'
  dialog.visible = true
  Object.assign(form, row)
}

// 启动任务
const handleStart = (row) => {
  ElMessageBox.confirm(`确认要启动任务"${row.jobName}"吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    request.post(`/job/${row.jobId}/start`).then(() => {
      ElMessage.success('任务已启动')
      getJobList()
    })
  })
}

// 暂停任务
const handleStop = (row) => {
  ElMessageBox.confirm(`确认要暂停任务"${row.jobName}"吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    request.post(`/job/${row.jobId}/stop`).then(() => {
      ElMessage.success('任务已暂停')
      getJobList()
    })
  })
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除任务"${row.jobName}"吗？`, '警告', {
    type: 'warning'
  }).then(() => {
    request.delete(`/job/${row.jobId}`).then(() => {
      ElMessage.success('删除成功')
      getJobList()
    })
  })
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确认删除选中的 ${jobIds.value.length} 个任务吗？`, '警告', {
    type: 'warning'
  }).then(() => {
    request.delete('/job/batch', { data: jobIds.value }).then(() => {
      ElMessage.success('批量删除成功')
      getJobList()
    })
  })
}

// 导出
const handleExport = () => {
  const params = {
    jobName: queryParams.jobName,
    jobGroup: queryParams.jobGroup,
    status: queryParams.status
  }
  request.get('/job/export', { params, responseType: 'blob' }).then(res => {
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '定时任务_' + new Date().getTime() + '.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  })
}

// 提交表单
const submitForm = () => {
  formRef.value.validate(valid => {
    if (valid) {
      const api = form.jobId ? request.put : request.post
      api('/system/job', form).then(() => {
        ElMessage.success('操作成功')
        dialog.visible = false
        getJobList()
      })
    }
  })
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    jobId: null,
    jobName: '',
    jobGroup: '',
    invokeTarget: '',
    cronExpression: '',
    misfirePolicy: 3,
    concurrent: 1,
    status: 1,
    remark: ''
  })
}

onMounted(() => {
  getJobList()
})
</script>

<style lang="scss" scoped>
.job-container {
  .table-card {
    margin-top: 20px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .header-actions {
    display: flex;
    gap: 10px;
  }

  .form-tip {
    font-size: 12px;
    color: #999;
    margin-top: 5px;
  }
}
</style>
<template>
  <div class="workflow-form-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="流程定义 Key">
          <el-input
            v-model="searchForm.processDefinitionKey"
            placeholder="请输入流程定义 Key"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="表单名称">
          <el-input
            v-model="searchForm.formName"
            placeholder="请输入表单名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="表单类型">
          <el-select v-model="searchForm.formType" placeholder="全部" clearable style="width: 120px">
            <el-option label="启动表单" :value="1" />
            <el-option label="办理表单" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>流程表单绑定</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleBind">
              <el-icon><Plus /></el-icon> 绑定表单
            </el-button>
            <el-button @click="handleRefresh" :loading="loading">
              <el-icon><RefreshRight /></el-icon> 刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        ref="tableRef"
        :data="formBindingList"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" :selectable="selectable" />
        <el-table-column type="index" label="序号" width="60" :index="indexMethod" />
        <el-table-column prop="processDefinitionKey" label="流程定义 Key" min-width="180" show-overflow-tooltip />
        <el-table-column prop="taskDefinitionKey" label="任务节点 Key" min-width="160" show-overflow-tooltip />
        <el-table-column prop="formCode" label="表单编码" width="150" />
        <el-table-column prop="formName" label="表单名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="表单类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.formType === 1 ? 'success' : 'warning'" size="small">
              <el-icon v-if="row.formType === 1"><VideoPlay /></el-icon>
              <el-icon v-else><Files /></el-icon>
              {{ row.formType === 1 ? '启动表单' : '办理表单' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :loading="row.statusChanging"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)" link>
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)" link>
              <el-icon><Delete /></el-icon> 删除
            </el-button>
            <el-button type="info" size="small" @click="handleView(row)" link>
              <el-icon><View /></el-icon> 查看
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-state">
            <el-icon :size="60" color="#909399"><DocumentRemove /></el-icon>
            <p>暂无数据</p>
          </div>
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <!-- 批量操作提示 -->
    <el-card v-if="selectedRows.length > 0" shadow="never" class="batch-operation-card">
      <div class="batch-operation-content">
        <span>已选择 {{ selectedRows.length }} 项</span>
        <el-button type="danger" size="small" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon> 批量删除
        </el-button>
      </div>
    </el-card>

    <!-- 表单绑定对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="650px"
      @close="handleDialogClose"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="流程定义 Key" prop="processDefinitionKey">
          <el-input
            v-model="formData.processDefinitionKey"
            placeholder="请输入流程定义 Key，如：leave_approval"
            clearable
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="任务节点 Key" prop="taskDefinitionKey">
          <el-input
            v-model="formData.taskDefinitionKey"
            placeholder="请输入任务节点 Key，如：Approve"
            clearable
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="表单编码" prop="formCode">
          <el-select
            v-model="formData.formCode"
            placeholder="请选择表单"
            filterable
            style="width: 100%"
            :loading="formDefinitionLoading"
          >
            <el-option
              v-for="item in formDefinitionList"
              :key="item.code"
              :label="item.name"
              :value="item.code"
            >
              <span style="float: left">{{ item.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ item.code }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="表单类型" prop="formType">
          <el-radio-group v-model="formData.formType">
            <el-radio :label="1" border>
              <el-icon><VideoPlay /></el-icon> 启动表单
            </el-radio>
            <el-radio :label="2" border>
              <el-icon><Files /></el-icon> 办理表单
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            <el-icon><Check /></el-icon> 确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看对话框 -->
    <el-dialog v-model="viewDialogVisible" title="查看表单绑定" width="650px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="流程定义 Key">
          {{ viewData.processDefinitionKey }}
        </el-descriptions-item>
        <el-descriptions-item label="任务节点 Key">
          {{ viewData.taskDefinitionKey }}
        </el-descriptions-item>
        <el-descriptions-item label="表单编码">
          {{ viewData.formCode }}
        </el-descriptions-item>
        <el-descriptions-item label="表单名称">
          {{ viewData.formName }}
        </el-descriptions-item>
        <el-descriptions-item label="表单类型">
          <el-tag :type="viewData.formType === 1 ? 'success' : 'warning'" size="small">
            {{ viewData.formType === 1 ? '启动表单' : '办理表单' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewData.status === 1 ? 'success' : 'info'" size="small">
            {{ viewData.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ viewData.remark || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ viewData.createTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ viewData.updateTime || '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WorkflowForm">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Plus, RefreshRight, Edit, Delete, View,
  VideoPlay, Files, DocumentRemove, Check
} from '@element-plus/icons-vue'
import {
  getFormBindingList as getFormBindingListApi,
  bindForm,
  unbindForm,
  
  getFormDefinitionList as getFormDefinitionListApi
} from '@/api/system/workflow-form'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formDefinitionLoading = ref(false)
const viewDialogVisible = ref(false)

const tableRef = ref(null)
const selectedRows = ref([])
const formBindingList = ref([])
const formDefinitionList = ref([])

const searchForm = reactive({
  processDefinitionKey: '',
  formName: '',
  formType: null
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const formRef = ref(null)
const formData = reactive({
  processDefinitionKey: '',
  taskDefinitionKey: '',
  formCode: '',
  formType: 1,
  status: 1,
  remark: ''
})

const viewData = ref({})

const formRules = {
  processDefinitionKey: [
    { required: true, message: '请输入流程定义 Key', trigger: 'blur' },
    { max: 100, message: '长度不能超过 100 个字符', trigger: 'blur' }
  ],
  taskDefinitionKey: [
    { required: true, message: '请输入任务节点 Key', trigger: 'blur' },
    { max: 100, message: '长度不能超过 100 个字符', trigger: 'blur' }
  ],
  formCode: [{ required: true, message: '请选择表单', trigger: 'change' }],
  formType: [{ required: true, message: '请选择表单类型', trigger: 'change' }]
}

// 获取表单绑定列表
const getFormBindingList = async () => {
  loading.value = true
  try {
    const res = await getFormBindingListApi({
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    formBindingList.value = res.data?.records || res.data?.list || []
    pagination.total = res.data?.total || 0

    // 为每行添加状态切换标记
    formBindingList.value.forEach(row => {
      row.statusChanging = false
    })
  } catch (error) {
    ElMessage.error('获取表单绑定列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 获取表单定义列表
const getFormDefinitionList = async () => {
  formDefinitionLoading.value = true
  try {
    const res = await getFormDefinitionListApi()
    formDefinitionList.value = res.data?.records || res.data || []
  } catch (error) {
    console.error('获取表单定义列表失败:', error)
  } finally {
    formDefinitionLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  getFormBindingList()
}

// 重置搜索
const handleReset = () => {
  searchForm.processDefinitionKey = ''
  searchForm.formName = ''
  searchForm.formType = null
  handleSearch()
}

// 刷新
const handleRefresh = () => {
  getFormBindingList()
  getFormDefinitionList()
  ElMessage.success('刷新成功')
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的记录')
    return
  }

  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 条记录吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const deletePromises = selectedRows.value.map(row =>
        unbindForm({
          processDefinitionKey: row.processDefinitionKey,
          taskDefinitionKey: row.taskDefinitionKey,
          formType: row.formType
        })
      )
      await Promise.all(deletePromises)
      ElMessage.success('批量删除成功')
      selectedRows.value = []
      getFormBindingList()
    } catch (error) {
      ElMessage.error('批量删除失败：' + error.message)
    }
  }).catch(() => {})
}

// 打开绑定对话框
const handleBind = () => {
  dialogTitle.value = '绑定表单'
  formData.processDefinitionKey = ''
  formData.taskDefinitionKey = ''
  formData.formCode = ''
  formData.formType = 1
  formData.status = 1
  formData.remark = ''
  dialogVisible.value = true
}

// 编辑绑定
const handleEdit = (row) => {
  dialogTitle.value = '编辑表单绑定'
  formData.processDefinitionKey = row.processDefinitionKey
  formData.taskDefinitionKey = row.taskDefinitionKey
  formData.formCode = row.formCode
  formData.formType = row.formType
  formData.status = row.status
  formData.remark = row.remark || ''
  dialogVisible.value = true
}

// 查看
const handleView = (row) => {
  viewData.value = { ...row }
  viewDialogVisible.value = true
}

// 删除绑定
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除流程（${row.processDefinitionKey}）的表单绑定吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await unbindForm({
        processDefinitionKey: row.processDefinitionKey,
        taskDefinitionKey: row.taskDefinitionKey,
        formType: row.formType
      })
      ElMessage.success('删除成功')
      getFormBindingList()
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  }).catch(() => {})
}

// 状态变化
const handleStatusChange = async (row) => {
  row.statusChanging = true
  try {
    await request({
      url: '/system/workflow-form/status',
      method: 'post',
      data: {
        processDefinitionKey: row.processDefinitionKey,
        taskDefinitionKey: row.taskDefinitionKey,
        formType: row.formType,
        status: row.status
      }
    })
    ElMessage.success(row.status === 1 ? '已启用' : '已停用')
    getFormBindingList()
  } catch (error) {
    ElMessage.error('状态更新失败：' + error.message)
    row.status = row.status === 1 ? 0 : 1 // 恢复原状态
    getFormBindingList()
  } finally {
    row.statusChanging = false
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await bindForm(formData)
        ElMessage.success('绑定成功')
        dialogVisible.value = false
        getFormBindingList()
      } catch (error) {
        ElMessage.error('绑定失败：' + error.message)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.resetFields()
  formData.processDefinitionKey = ''
  formData.taskDefinitionKey = ''
  formData.formCode = ''
  formData.formType = 1
  formData.status = 1
  formData.remark = ''
}

// 序号方法
const indexMethod = (index) => {
  return (pagination.currentPage - 1) * pagination.pageSize + index + 1
}

// 选择限制
const selectable = (row) => {
  return true
}

onMounted(() => {
  getFormBindingList()
  getFormDefinitionList()
})
</script>

<style scoped lang="scss">
.workflow-form-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.search-card {
  margin-bottom: 16px;

  .search-form {
    display: flex;
    flex-wrap: wrap;
  }
}

.table-card {
  margin-bottom: 16px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-actions {
      display: flex;
      gap: 8px;
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;

  p {
    margin-top: 16px;
    font-size: 14px;
  }
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.batch-operation-card {
  margin-top: 16px;
  background: linear-gradient(135deg, #fef0f0, #ffffff);

  .batch-operation-content {
    display: flex;
    justify-content: space-between;
    align-items: center;

    span {
      font-weight: 500;
      color: #f56c6c;
    }
  }
}

.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

// 表格样式优化
:deep(.el-table) {
  --el-table-header-bg-color: #f5f7fa;
  --el-table-header-text-color: #606266;
  --el-table-row-hover-bg-color: #f5f7fa;

  th.el-table__cell {
    background-color: #f5f7fa;
    color: #606266;
    font-weight: 600;
  }
}

// 卡片样式
:deep(.el-card) {
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .el-card__header {
    background-color: #fff;
    border-bottom: 1px solid #ebeef5;
    padding: 16px 20px;
  }

  .el-card__body {
    padding: 20px;
  }
}

// 对话框样式
:deep(.el-dialog) {
  border-radius: 12px;

  .el-dialog__header {
    padding: 16px 24px;
    border-bottom: 1px solid #ebeef5;
  }

  .el-dialog__title {
    font-size: 16px;
    font-weight: 600;
  }

  .el-dialog__body {
    padding: 24px;
  }

  .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid #ebeef5;
  }
}

// 响应式
@media (max-width: 768px) {
  .workflow-form-container {
    padding: 10px;
  }

  .search-card .search-form {
    flex-direction: column;

    .el-form-item {
      width: 100%;
    }
  }

  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>

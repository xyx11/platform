<template>
  <div class="workflow-form-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总绑定数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon process">
              <el-icon :size="28"><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.processCount }}</div>
              <div class="stat-label">关联流程</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon start">
              <el-icon :size="28"><VideoPlay /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.startFormCount }}</div>
              <div class="stat-label">启动表单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon task">
              <el-icon :size="28"><Files /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.taskFormCount }}</div>
              <div class="stat-label">办理表单</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <template #header>
        <div class="card-header">
          <span><el-icon><Search /></el-icon> 搜索条件</span>
          <el-button link type="primary" @click="toggleSearch">
            {{ showSearch ? '收起' : '展开' }}
            <el-icon><Arrow-Down /></el-icon>
          </el-button>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="流程定义 Key">
          <el-input
            v-model="searchForm.processDefinitionKey"
            placeholder="请输入流程定义 Key"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Connection /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="表单名称">
          <el-input
            v-model="searchForm.formName"
            placeholder="请输入表单名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Document /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="表单编码">
          <el-input
            v-model="searchForm.formKey"
            placeholder="请输入表单编码"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Ticket /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="任务节点 Key" v-if="showSearch">
          <el-input
            v-model="searchForm.taskDefinitionKey"
            placeholder="请输入任务节点 Key"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Setting /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="表单类型" v-if="showSearch">
          <el-select v-model="searchForm.formType" placeholder="全部" clearable style="width: 120px">
            <el-option label="启动表单" :value="1" />
            <el-option label="办理表单" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" v-if="showSearch">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon> 导出
          </el-button>
          <el-dropdown @command="handleColumnCommand" trigger="click">
            <el-button>
              <el-icon><Grid /></el-icon> 列显示
              <el-icon><Arrow-Down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :disabled="true" icon="Check">已选择 {{ visibleColumns.length }} 列</el-dropdown-item>
                <el-dropdown-item command="processDefinitionKey" :icon="visibleColumns.includes('processDefinitionKey') ? 'Check' : 'Close'">流程定义 Key</el-dropdown-item>
                <el-dropdown-item command="taskDefinitionKey" :icon="visibleColumns.includes('taskDefinitionKey') ? 'Check' : 'Close'">任务节点 Key</el-dropdown-item>
                <el-dropdown-item command="formKey" :icon="visibleColumns.includes('formKey') ? 'Check' : 'Close'">表单编码</el-dropdown-item>
                <el-dropdown-item command="formName" :icon="visibleColumns.includes('formName') ? 'Check' : 'Close'">表单名称</el-dropdown-item>
                <el-dropdown-item command="formType" :icon="visibleColumns.includes('formType') ? 'Check' : 'Close'">表单类型</el-dropdown-item>
                <el-dropdown-item command="status" :icon="visibleColumns.includes('status') ? 'Check' : 'Close'">状态</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span>流程表单绑定</span>
            <el-tag type="info" size="small" style="margin-left: 12px">
              共 {{ pagination.total }} 条
            </el-tag>
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="handleBind">
              <el-icon><Plus /></el-icon> 绑定表单
            </el-button>
            <el-button @click="handleBatchEdit" :disabled="selectedRows.length === 0">
              <el-icon><Edit /></el-icon> 批量编辑
            </el-button>
            <el-button @click="handleBatchDelete" :disabled="selectedRows.length === 0" type="danger">
              <el-icon><Delete /></el-icon> 批量删除
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
        element-loading-text="加载中..."
        element-loading-background="rgba(255, 255, 255, 0.8)"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" :selectable="selectable" />
        <el-table-column type="index" label="序号" width="60" :index="indexMethod" />
        <el-table-column v-if="visibleColumns.includes('processDefinitionKey')" prop="processDefinitionKey" label="流程定义 Key" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="handleViewProcess(row.processDefinitionKey)">
              {{ row.processDefinitionKey }}
            </el-link>
            <el-button link type="primary" size="small" @click="copyToClipboard(row.processDefinitionKey)">
              <el-icon><CopyDocument /></el-icon>
            </el-button>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('taskDefinitionKey')" prop="taskDefinitionKey" label="任务节点 Key" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.taskDefinitionKey }}
            <el-button link type="primary" size="small" @click="copyToClipboard(row.taskDefinitionKey)">
              <el-icon><CopyDocument /></el-icon>
            </el-button>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('formKey')" prop="formKey" label="表单编码" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.formKey }}
            <el-button link type="primary" size="small" @click="copyToClipboard(row.formKey)">
              <el-icon><CopyDocument /></el-icon>
            </el-button>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('formName')" prop="formName" label="表单名称" min-width="160" show-overflow-tooltip />
        <el-table-column v-if="visibleColumns.includes('formType')" label="表单类型" width="110">
          <template #default="{ row }">
            <el-tag :type="row.formType === 1 ? 'success' : 'warning'" size="small" effect="light">
              <el-icon v-if="row.formType === 1"><VideoPlay /></el-icon>
              <el-icon v-else><Files /></el-icon>
              {{ row.formType === 1 ? '启动表单' : '办理表单' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('status')" label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :loading="row.statusChanging"
              inline-prompt
              active-text="启"
              inactive-text="停"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)" link>
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="info" size="small" @click="handleView(row)" link>
              <el-icon><View /></el-icon> 查看
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)" link>
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-state">
            <el-icon :size="60" color="#909399"><DocumentRemove /></el-icon>
            <p>暂无数据</p>
            <el-button type="primary" size="small" @click="handleBind">
              <el-icon><Plus /></el-icon> 绑定表单
            </el-button>
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
        <div class="selected-info">
          <el-icon><Select /></el-icon>
          <span>已选择 {{ selectedRows.length }} 项</span>
        </div>
        <div class="batch-actions">
          <el-button type="warning" size="small" @click="openBatchEdit">
            <el-icon><Edit /></el-icon> 批量编辑
          </el-button>
          <el-button type="danger" size="small" @click="handleBatchDelete">
            <el-icon><Delete /></el-icon> 批量删除
          </el-button>
          <el-button @click="clearSelection">
            <el-icon><Close /></el-icon> 取消选择
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 批量编辑对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      :title="`批量编辑 (${selectedRows.length} 项)`"
      width="600px"
      @close="handleBatchDialogClose"
    >
      <el-form :model="batchFormData" label-width="120px">
        <el-alert
          title="批量设置说明"
          type="info"
          :closable="false"
          style="margin-bottom: 16px"
        >
          <p>勾选需要批量修改的字段，未勾选的字段将保持原值不变。</p>
        </el-alert>
        <el-form-item label="批量设置表单">
          <el-checkbox v-model="batchFormData.updateForm" style="width: 100%" />
          <el-select
            v-if="batchFormData.updateForm"
            v-model="batchFormData.formKey"
            placeholder="请选择表单"
            filterable
            style="width: 100%; margin-top: 8px"
            :loading="formDefinitionLoading"
          >
            <el-option
              v-for="item in formDefinitionList"
              :key="item.formCode"
              :label="item.formName"
              :value="item.formCode"
            >
              <span style="float: left">{{ item.formName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ item.formCode }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="批量设置状态">
          <el-checkbox v-model="batchFormData.updateStatus" style="width: 100%" />
          <el-radio-group v-if="batchFormData.updateStatus" v-model="batchFormData.status" style="margin-top: 8px">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBatchEdit" :loading="batchSubmitting">
          <el-icon><Check /></el-icon> 确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 表单绑定对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="650px"
      @close="handleDialogClose"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="流程定义" prop="processDefinitionKey">
          <el-select
            v-model="formData.processDefinitionKey"
            placeholder="请选择流程定义"
            filterable
            style="width: 100%"
            :loading="processDefinitionLoading"
          >
            <el-option
              v-for="item in processDefinitionList"
              :key="item.id"
              :label="item.name"
              :value="item.key"
            >
              <span style="float: left">{{ item.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ item.key }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="任务节点 Key" prop="taskDefinitionKey">
          <el-input
            v-model="formData.taskDefinitionKey"
            placeholder="请输入任务节点 Key，如：Approve（启动表单留空）"
            clearable
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="表单编码" prop="formKey">
          <el-select
            v-model="formData.formKey"
            placeholder="请选择表单"
            filterable
            style="width: 100%"
            :loading="formDefinitionLoading"
          >
            <el-option
              v-for="item in formDefinitionList"
              :key="item.formCode"
              :label="item.formName"
              :value="item.formCode"
            >
              <span style="float: left">{{ item.formName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ item.formCode }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="表单类型" prop="formType">
          <el-radio-group v-model="formData.formType">
            <el-radio :value="1" border>
              <el-icon><VideoPlay /></el-icon> 启动表单
            </el-radio>
            <el-radio :value="2" border>
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
    <el-dialog v-model="viewDialogVisible" title="查看表单绑定" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="流程定义 Key">
          <el-link type="primary" @click="handleViewProcess(viewData.processDefinitionKey)">
            {{ viewData.processDefinitionKey }}
          </el-link>
          <el-button link type="primary" size="small" @click="copyToClipboard(viewData.processDefinitionKey)">
            <el-icon><CopyDocument /></el-icon>
          </el-button>
        </el-descriptions-item>
        <el-descriptions-item label="任务节点 Key">
          {{ viewData.taskDefinitionKey }}
          <el-button link type="primary" size="small" @click="copyToClipboard(viewData.taskDefinitionKey)">
            <el-icon><CopyDocument /></el-icon>
          </el-button>
        </el-descriptions-item>
        <el-descriptions-item label="表单编码">
          {{ viewData.formKey }}
          <el-button link type="primary" size="small" @click="copyToClipboard(viewData.formKey)">
            <el-icon><CopyDocument /></el-icon>
          </el-button>
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
        <el-button type="primary" @click="handleEdit(viewData)">
          <el-icon><Edit /></el-icon> 编辑
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WorkflowForm">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Plus, RefreshRight, Edit, Delete, View,
  VideoPlay, Files, DocumentRemove, Check, Download, Link,
  CopyDocument, Select, Close, ArrowDown, Grid, Connection,
  Document, Ticket, Setting
} from '@element-plus/icons-vue'
import {
  getFormBindingList as getFormBindingListApi,
  bindForm,
  unbindForm,
  getFormDefinitionList as getFormDefinitionListApi,
  updateFormStatus
} from '@/api/system/workflow-form'
import { parseTime } from '@/utils/mp'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const batchSubmitting = ref(false)
const dialogVisible = ref(false)
const batchDialogVisible = ref(false)
const dialogTitle = ref('')
const formDefinitionLoading = ref(false)
const processDefinitionLoading = ref(false)
const viewDialogVisible = ref(false)
const showSearch = ref(true)

const tableRef = ref(null)
const selectedRows = ref([])
const formBindingList = ref([])
const formDefinitionList = ref([])
const processDefinitionList = ref([])

// 统计信息
const stats = computed(() => {
  const list = formBindingList.value
  return {
    total: pagination.total,
    processCount: new Set(list.map(item => item.processDefinitionKey)).size,
    startFormCount: list.filter(item => item.formType === 1).length,
    taskFormCount: list.filter(item => item.formType === 2).length
  }
})

// 可见列控制
const visibleColumns = ref(['processDefinitionKey', 'taskDefinitionKey', 'formKey', 'formName', 'formType', 'status'])

const searchForm = reactive({
  processDefinitionKey: '',
  formName: '',
  formKey: '',
  taskDefinitionKey: '',
  formType: null,
  status: null
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
  formKey: '',
  formType: 1,
  status: 1,
  remark: ''
})

// 批量编辑表单数据
const batchFormData = reactive({
  updateForm: false,
  updateStatus: false,
  formKey: '',
  status: 1
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
  formKey: [{ required: true, message: '请选择表单', trigger: 'change' }],
  formType: [{ required: true, message: '请选择表单类型', trigger: 'change' }]
}

// 切换搜索栏展开/收起
const toggleSearch = () => {
  showSearch.value = !showSearch.value
}

// 列显示控制
const handleColumnCommand = (command) => {
  const index = visibleColumns.value.indexOf(command)
  if (index > -1) {
    visibleColumns.value.splice(index, 1)
  } else {
    visibleColumns.value.push(command)
  }
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
    const res = await getFormDefinitionListApi({
      status: 1 // 只获取启用的表单
    })
    // 后端返回的是 Result<List<FormDefinition>>，直接使用 data
    formDefinitionList.value = res.data || []
  } catch (error) {
    console.error('获取表单定义列表失败:', error)
    formDefinitionList.value = []
  } finally {
    formDefinitionLoading.value = false
  }
}

// 获取流程定义列表
const getProcessDefinitionList = async () => {
  processDefinitionLoading.value = true
  try {
    const { data } = await request.get('/system/workflow/definition/list')
    processDefinitionList.value = data?.records || data?.list || []
  } catch (error) {
    console.error('获取流程定义列表失败:', error)
  } finally {
    processDefinitionLoading.value = false
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
  searchForm.formKey = ''
  searchForm.taskDefinitionKey = ''
  searchForm.formType = null
  searchForm.status = null
  handleSearch()
}

// 刷新
const handleRefresh = () => {
  getFormBindingList()
  getFormDefinitionList()
  ElMessage.success('刷新成功')
}

// 导出功能
const handleExport = () => {
  if (formBindingList.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }

  const headers = ['流程定义 Key', '任务节点 Key', '表单编码', '表单名称', '表单类型', '状态', '备注']
  const data = formBindingList.value.map(row => [
    row.processDefinitionKey,
    row.taskDefinitionKey,
    row.formKey,
    row.formName,
    row.formType === 1 ? '启动表单' : '办理表单',
    row.status === 1 ? '启用' : '停用',
    row.remark || ''
  ])

  // 创建 CSV 内容
  const csvContent = [
    headers.join(','),
    ...data.map(row => row.map(cell => `"${cell || ''}"`).join(','))
  ].join('\n')

  // 下载文件
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `流程表单绑定_${parseTime(new Date(), '{y}{m}{d}_{h}{i}{s}')}.csv`
  link.click()
  URL.revokeObjectURL(link.href)

  ElMessage.success('导出成功')
}

// 查看流程定义
const handleViewProcess = (processDefinitionKey) => {
  const route = `/system/workflow?processKey=${processDefinitionKey}`
  window.open(route, '_blank')
  ElMessage.info('在新窗口打开流程设计器')
}

// 复制到剪贴板
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch (err) {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    try {
      document.execCommand('copy')
      ElMessage.success('已复制到剪贴板')
    } catch (e) {
      ElMessage.error('复制失败')
    }
    document.body.removeChild(textarea)
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 取消选择
const clearSelection = () => {
  tableRef.value?.clearSelection()
  selectedRows.value = []
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
  formData.formKey = ''
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
  formData.formKey = row.formKey || row.formCode
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
    await updateFormStatus({
      processDefinitionKey: row.processDefinitionKey,
      taskDefinitionKey: row.taskDefinitionKey,
      formType: row.formType,
      status: row.status
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
        // 不发送 status 和 remark 字段，后端实体没有这些字段
        const { status, remark, ...submitData } = formData
        await bindForm(submitData)
        ElMessage.success('绑定成功')
        dialogVisible.value = false
        getFormBindingList()
      } catch (error) {
        console.error('绑定失败详情:', error)
        const errorMsg = error.response?.data?.message || error.response?.data?.msg || error.message || '未知错误'
        const statusCode = error.response?.status || error.code || '未知'
        ElMessage.error('绑定失败 (状态码：' + statusCode + ')：' + errorMsg)
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
  formData.formKey = ''
  formData.formType = 1
  formData.status = 1
  formData.remark = ''
}

// 打开批量编辑对话框
const openBatchEdit = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择记录')
    return
  }
  batchFormData.updateForm = false
  batchFormData.updateStatus = false
  batchFormData.formKey = ''
  batchFormData.status = 1
  batchDialogVisible.value = true
}

// 打开批量编辑
const handleBatchEdit = () => {
  openBatchEdit()
}

// 提交批量编辑
const submitBatchEdit = async () => {
  if (!batchFormData.updateForm && !batchFormData.updateStatus) {
    ElMessage.warning('请至少选择一个要修改的字段')
    return
  }
  batchSubmitting.value = true
  try {
    const updatePromises = selectedRows.value.map(row => {
      const params = {
        processDefinitionKey: row.processDefinitionKey,
        taskDefinitionKey: row.taskDefinitionKey,
        formType: row.formType
      }
      if (batchFormData.updateForm && batchFormData.formKey) {
        params.formKey = batchFormData.formKey
      }
      if (batchFormData.updateStatus) {
        params.status = batchFormData.status
      }
      return updateFormStatus(params)
    })
    await Promise.all(updatePromises)
    ElMessage.success(`批量更新 ${selectedRows.value.length} 条记录成功`)
    batchDialogVisible.value = false
    getFormBindingList()
  } catch (error) {
    ElMessage.error('批量更新失败：' + error.message)
  } finally {
    batchSubmitting.value = false
  }
}

// 关闭批量编辑对话框
const handleBatchDialogClose = () => {
  batchFormData.updateForm = false
  batchFormData.updateStatus = false
  batchFormData.formKey = ''
  batchFormData.status = 1
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
  getProcessDefinitionList()
})
</script>

<style scoped lang="scss">
.workflow-form-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

// 统计卡片
.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  .stat-content {
    display: flex;
    align-items: center;

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;

      &.total {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
      }
      &.process {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        color: white;
      }
      &.start {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        color: white;
      }
      &.task {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        color: white;
      }
    }

    .stat-info {
      flex: 1;
      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: #303133;
        line-height: 1;
      }
      .stat-label {
        font-size: 13px;
        color: #909399;
      }
    }
  }
}

.search-card {
  margin-bottom: 16px;

  .search-form {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }
}

.table-card {
  margin-bottom: 16px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 600;
    }

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
  padding: 80px 20px;
  color: #909399;

  p {
    margin: 16px 0 24px;
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

    .selected-info {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500;
      color: #f56c6c;
      font-size: 14px;
    }

    .batch-actions {
      display: flex;
      gap: 8px;
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

    .header-left {
      width: 100%;
      justify-content: space-between;
    }
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .batch-operation-content {
    flex-direction: column;
    gap: 12px;

    .batch-actions {
      width: 100%;
      justify-content: flex-start;
    }
  }
}
</style>

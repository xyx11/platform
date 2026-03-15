<template>
  <div class="task-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>任务管理</span>
          <el-radio-group v-model="activeTab" size="default" @change="switchTab">
            <el-radio-button label="todo">待办任务</el-radio-button>
            <el-radio-button label="done">已办任务</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" label-width="80px" class="mb-4">
        <el-form-item label="任务名称">
          <el-input v-model="queryParams.taskName" placeholder="请输入任务名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="queryParams.priority" placeholder="请选择优先级" clearable style="width: 120px">
            <el-option label="高" value="high" />
            <el-option label="普通" value="normal" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 统计卡片 -->
      <el-row :gutter="16" class="mb-4">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <el-icon class="stat-icon" color="#409EFF"><Document /></el-icon>
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
              <el-icon class="stat-icon" color="#67C23A"><Select /></el-icon>
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
              <el-icon class="stat-icon" color="#E6A23C"><Warning /></el-icon>
              <div class="stat-content">
                <div class="stat-value">{{ stats.highPriorityCount || 0 }}</div>
                <div class="stat-label">高优先级</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <el-icon class="stat-icon" color="#909399"><Clock /></el-icon>
              <div class="stat-content">
                <div class="stat-value">{{ stats.normalPriorityCount || 0 }}</div>
                <div class="stat-label">普通优先级</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 待办任务列表 -->
      <el-table v-if="activeTab === 'todo'" :data="todoList" v-loading="loading" border stripe>
        <el-table-column label="任务名称" prop="taskName" min-width="200" show-overflow-tooltip />
        <el-table-column label="任务 Key" prop="taskKey" width="150" />
        <el-table-column label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityColor(row.priority)" size="small">
              {{ getPriorityLabel(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理人" prop="assignee" width="100" />
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link size="small" @click="handleComplete(row)">
              <el-icon><Select /></el-icon> 完成
            </el-button>
            <el-button type="primary" link size="small" @click="handleTransfer(row)">
              <el-icon><Switch /></el-icon> 转办
            </el-button>
            <el-button type="warning" link size="small" @click="handleDelegate(row)">
              <el-icon><User /></el-icon> 委派
            </el-button>
            <el-button type="info" link size="small" @click="handleDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 已办任务列表 -->
      <el-table v-else :data="doneList" v-loading="loading" border stripe>
        <el-table-column label="任务名称" prop="taskName" min-width="200" show-overflow-tooltip />
        <el-table-column label="任务 Key" prop="taskKey" width="150" />
        <el-table-column label="处理人" prop="assignee" width="100" />
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column label="完成时间" prop="endTime" width="180" />
        <el-table-column label="耗时" width="100">
          <template #default="{ row }">
            {{ formatDuration(row.duration) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="info" link size="small" @click="handleDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-show="total > 0"
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>

    <!-- 任务详情对话框 -->
    <el-dialog v-model="detailVisible" title="任务详情" width="700px">
      <el-descriptions :column="2" border v-if="currentTask">
        <el-descriptions-item label="任务 ID">{{ currentTask.taskId }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ currentTask.taskName }}</el-descriptions-item>
        <el-descriptions-item label="任务 Key">{{ currentTask.taskKey }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ currentTask.assignee }}</el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityColor(currentTask.priority)" size="small">
            {{ getPriorityLabel(currentTask.priority) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentTask.createTime }}</el-descriptions-item>
        <el-descriptions-item label="流程实例 ID" :span="2">{{ currentTask.processInstanceId }}</el-descriptions-item>
        <el-descriptions-item label="流程定义 ID" :span="2">{{ currentTask.processDefinitionId }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentTask.description || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>流程变量</el-divider>
      <el-table :data="variableList" border size="small" v-if="currentTask?.variables">
        <el-table-column prop="key" label="变量名" width="200" />
        <el-table-column prop="value" label="变量值" show-overflow-tooltip />
      </el-table>
      <el-empty v-else description="暂无流程变量" />

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 完成任务对话框 -->
    <el-dialog v-model="completeVisible" title="完成任务" width="700px">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="任务名称">
          <span>{{ currentTask?.taskName }}</span>
        </el-form-item>

        <!-- 任务表单 -->
        <template v-if="taskFormComponents.length > 0">
          <el-divider>任务表单</el-divider>
          <div class="task-form-container">
            <el-form-item v-for="comp in taskFormComponents" :key="comp.id" :label="comp.label" :required="comp.required">
              <el-input v-if="comp.type === 'input'" v-model="completeForm.formValues[comp.prop]" :placeholder="comp.placeholder || '请输入'" />
              <el-input v-else-if="comp.type === 'textarea'" v-model="completeForm.formValues[comp.prop]" type="textarea" :rows="3" :placeholder="comp.placeholder || '请输入'" />
              <el-input-number v-else-if="comp.type === 'number'" v-model="completeForm.formValues[comp.prop]" :min="0" style="width: 100%" />
              <el-select v-else-if="comp.type === 'select'" v-model="completeForm.formValues[comp.prop]" :placeholder="comp.placeholder || '请选择'" style="width: 100%">
                <el-option v-for="opt in parseOptions(comp.options)" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
              <el-radio-group v-else-if="comp.type === 'radio'" v-model="completeForm.formValues[comp.prop]">
                <el-radio v-for="opt in parseOptions(comp.options)" :key="opt.value" :label="opt.value">{{ opt.label }}</el-radio>
              </el-radio-group>
              <el-checkbox-group v-else-if="comp.type === 'checkbox'" v-model="completeForm.formValues[comp.prop]">
                <el-checkbox v-for="opt in parseOptions(comp.options)" :key="opt.value" :label="opt.value">{{ opt.label }}</el-checkbox>
              </el-checkbox-group>
              <el-date-picker v-else-if="comp.type === 'date'" v-model="completeForm.formValues[comp.prop]" type="date" :placeholder="comp.placeholder || '请选择'" style="width: 100%" />
              <el-date-picker v-else-if="comp.type === 'datetime'" v-model="completeForm.formValues[comp.prop]" type="datetime" :placeholder="comp.placeholder || '请选择'" style="width: 100%" />
            </el-form-item>
          </div>
        </template>
        <el-empty v-else description="该任务没有配置表单" />

        <!-- 流程变量 -->
        <el-divider>流程变量</el-divider>
        <el-table :data="completeForm.variables" border>
          <el-table-column prop="key" label="变量名" width="200">
            <template #header>
              <el-input v-model="newVar.key" placeholder="变量名" size="small" style="width: 180px" />
            </template>
            <template #default="{ row, $index }">
              <el-input v-model="row.key" size="small" />
            </template>
          </el-table-column>
          <el-table-column prop="value" label="变量值">
            <template #header>
              <el-input v-model="newVar.value" placeholder="变量值" size="small" />
            </template>
            <template #default="{ row }">
              <el-input v-model="row.value" size="small" />
            </template>
          </el-table-column>
          <el-table-column width="80" align="center">
            <template #default="{ $index }">
              <el-button type="danger" link @click="removeVariable($index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" link @click="addVariable" style="margin-top: 10px">
          <el-icon><Plus /></el-icon> 添加变量
        </el-button>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComplete">确定</el-button>
      </template>
    </el-dialog>

    <!-- 转办/委派对话框 -->
    <el-dialog v-model="transferVisible" :title="transferType === 'transfer' ? '转办任务' : '委派任务'" width="500px">
      <el-form :model="transferForm" label-width="80px">
        <el-form-item label="任务名称">
          <span>{{ currentTask?.taskName }}</span>
        </el-form-item>
        <el-form-item label="原处理人">
          <span>{{ currentTask?.assignee }}</span>
        </el-form-item>
        <el-form-item :label="transferType === 'transfer' ? '新处理人' : '被委派人'">
          <el-input v-model="transferForm.targetUser" placeholder="请输入用户 ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTransfer">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TaskManagement">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Select, Warning, Clock, Switch, User, Delete, Plus, Search, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

const activeTab = ref('todo')
const loading = ref(false)
const todoList = ref([])
const doneList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const stats = ref({})

const detailVisible = ref(false)
const completeVisible = ref(false)
const transferVisible = ref(false)
const currentTask = ref(null)
const variableList = ref([])
const transferType = ref('transfer')

const completeForm = reactive({
  taskId: '',
  variables: [],
  formValues: {}
})

const taskFormComponents = ref([])

const newVar = reactive({
  key: '',
  value: ''
})

const transferForm = reactive({
  taskId: '',
  targetUser: ''
})

// 搜索表单
const queryParams = reactive({
  taskName: undefined,
  priority: undefined
})

// 获取统计信息
const getStats = () => {
  request.get('/system/workflow/task/stats').then(res => {
    stats.value = res.data || {}
  })
}

// 获取列表
const getList = () => {
  loading.value = true
  const url = activeTab.value === 'todo' ? '/system/workflow/task/todo' : '/system/workflow/task/done'
  request.get(url, {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...queryParams
    }
  })
    .then(res => {
      if (activeTab.value === 'todo') {
        todoList.value = res.data?.records || []
        total.value = res.data?.total || 0
      } else {
        doneList.value = res.data?.records || []
        total.value = res.data?.total || 0
      }
    })
    .catch(() => {})
    .finally(() => {
      loading.value = false
    })
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  getList()
}

// 重置搜索
const resetSearch = () => {
  queryParams.taskName = undefined
  queryParams.priority = undefined
  handleSearch()
}

// 切换标签
const switchTab = () => {
  pageNum.value = 1
  getList()
}

// 获取优先级颜色
const getPriorityColor = (priority) => {
  const colors = { high: 'danger', normal: '', low: 'info' }
  return colors[priority] || ''
}

// 获取优先级标签
const getPriorityLabel = (priority) => {
  const labels = { high: '高', normal: '普通', low: '低' }
  return labels[priority] || '普通'
}

// 格式化耗时
const formatDuration = (ms) => {
  if (!ms) return '-'
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  if (hours > 0) return `${hours}小时${minutes % 60}分钟`
  if (minutes > 0) return `${minutes}分钟${seconds % 60}秒`
  return `${seconds}秒`
}

// 解析选项
const parseOptions = (options) => {
  if (!options) return []
  if (typeof options === 'string') {
    try {
      return JSON.parse(options)
    } catch {
      return []
    }
  }
  return Array.isArray(options) ? options : []
}

// 加载任务表单
const loadTaskForm = async (taskKey) => {
  try {
    taskFormComponents.value = []
    completeForm.formValues = {}
    const { data } = await request.get(`/system/workflow/form/task/\${taskKey}`)
    if (data?.formContent) {
      try {
        taskFormComponents.value = JSON.parse(data.formContent)
      } catch (e) {
        console.error('任务表单解析失败:', e)
        if (Array.isArray(data.formContent)) {
          taskFormComponents.value = data.formContent
        }
      }
    }
  } catch (error) {
    console.error('加载任务表单失败:', error)
    taskFormComponents.value = []
  }
}

// 完成任务
const handleComplete = (row) => {
  currentTask.value = row
  completeForm.taskId = row.taskId
  completeForm.variables = [{ key: '', value: '' }]
  completeForm.formValues = {}
  // 加载任务表单
  if (row.taskKey) {
    loadTaskForm(row.taskKey)
  }
  completeVisible.value = true
}

// 添加变量
const addVariable = () => {
  if (newVar.key || newVar.value) {
    completeForm.variables.push({ ...newVar })
    newVar.key = ''
    newVar.value = ''
  } else {
    completeForm.variables.push({ key: '', value: '' })
  }
}

// 移除变量
const removeVariable = (index) => {
  completeForm.variables.splice(index, 1)
}

// 提交完成
const submitComplete = () => {
  const variables = {}
  completeForm.variables.forEach(v => {
    if (v.key) variables[v.key] = v.value
  })

  request.post(`/system/workflow/task/${completeForm.taskId}/complete`, variables)
    .then(() => {
      ElMessage.success('任务已完成')
      completeVisible.value = false
      getList()
      getStats()
    })
    .catch(() => {})
}

// 转办任务
const handleTransfer = (row) => {
  currentTask.value = row
  transferForm.taskId = row.taskId
  transferType.value = 'transfer'
  transferForm.targetUser = ''
  transferVisible.value = true
}

// 委派任务
const handleDelegate = (row) => {
  currentTask.value = row
  transferForm.taskId = row.taskId
  transferType.value = 'delegate'
  transferForm.targetUser = ''
  transferVisible.value = true
}

// 提交转办/委派
const submitTransfer = () => {
  if (!transferForm.targetUser) {
    ElMessage.warning('请输入用户 ID')
    return
  }

  const url = transferType.value === 'transfer' ? '/transfer' : '/delegate'
  request.post(`/system/workflow/task/${transferForm.taskId}${url}`, null, {
    params: { [transferType.value === 'transfer' ? 'newAssignee' : 'delegateUser']: transferForm.targetUser }
  })
    .then(() => {
      ElMessage.success(transferType.value === 'transfer' ? '转办成功' : '委派成功')
      transferVisible.value = false
      getList()
      getStats()
    })
    .catch(() => {})
}

// 查看详情
const handleDetail = async (row) => {
  try {
    const { data } = await request.get(`/system/workflow/task/${row.taskId}`)
    currentTask.value = data
    variableList.value = data.variables ? Object.entries(data.variables).map(([key, value]) => ({ key, value })) : []
    detailVisible.value = true
  } catch (error) {
    // 获取任务详情失败
    ElMessage.error('获取任务详情失败')
  }
}

onMounted(() => {
  getList()
  getStats()
})
</script>

<style scoped lang="scss">
.task-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .mb-4 {
    margin-bottom: 20px;
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

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .task-form-container {
    max-height: 400px;
    overflow-y: auto;
    margin-bottom: 15px;
  }
}
</style>
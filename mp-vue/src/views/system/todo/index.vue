<template>
  <div class="app-container">
    <el-card>
      <!-- 搜索表单 -->
      <el-form :model="queryParams" :inline="true" label-width="80px">
        <el-form-item label="待办标题">
          <el-input v-model="queryParams.todoTitle" placeholder="请输入待办标题" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="待办类型">
          <el-select v-model="queryParams.todoType" placeholder="请选择类型" clearable>
            <el-option label="工作" value="1" />
            <el-option label="会议" value="2" />
            <el-option label="提醒" value="3" />
            <el-option label="其他" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="待处理" :value="0" />
            <el-option label="已完成" :value="1" />
            <el-option label="已取消" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain :disabled="multiple" @click="handleDelete">
            <el-icon><Delete /></el-icon> 删除
          </el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </el-row>

      <!-- 统计卡片 -->
      <el-row :gutter="16" class="mb8">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <el-icon class="stat-icon" color="#409EFF"><Document /></el-icon>
              <div class="stat-content">
                <div class="stat-value">{{ stats.totalCount || 0 }}</div>
                <div class="stat-label">全部待办</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <el-icon class="stat-icon" color="#E6A23C"><Clock /></el-icon>
              <div class="stat-content">
                <div class="stat-value">{{ stats.pendingCount || 0 }}</div>
                <div class="stat-label">待处理</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <el-icon class="stat-icon" color="#67C23A"><Select /></el-icon>
              <div class="stat-content">
                <div class="stat-value">{{ stats.completedCount || 0 }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <el-icon class="stat-icon" color="#F56C6C"><Warning /></el-icon>
              <div class="stat-content">
                <div class="stat-value">{{ stats.urgentCount || 0 }}</div>
                <div class="stat-label">紧急事项</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="todoList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" type="index" width="80" align="center" />
        <el-table-column label="待办标题" prop="todoTitle" :show-overflow-tooltip="true" min-width="200" />
        <el-table-column label="类型" prop="todoType" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagColor(row.todoType)">
              {{ getTypeLabel(row.todoType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优先级" prop="priority" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityTagColor(row.priority)" size="small">
              {{ getPriorityLabel(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagColor(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="计划完成时间" prop="planTime" width="180">
          <template #default="{ row }">
            {{ row.planTime ? parseTime(row.planTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="success"
              link
              icon="Select"
              @click="handleComplete(row)"
            >
              完成
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="info"
              link
              icon="Close"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button type="primary" link icon="Edit" @click="handleUpdate(row)">
              编辑
            </el-button>
            <el-button type="danger" link icon="Delete" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-show="total > 0"
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="title" v-model="open" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="待办标题" prop="todoTitle">
          <el-input v-model="form.todoTitle" placeholder="请输入待办标题" />
        </el-form-item>
        <el-form-item label="待办内容" prop="todoContent">
          <el-input v-model="form.todoContent" type="textarea" :rows="4" placeholder="请输入待办内容" />
        </el-form-item>
        <el-form-item label="待办类型" prop="todoType">
          <el-radio-group v-model="form.todoType">
            <el-radio value="1">工作</el-radio>
            <el-radio value="2">会议</el-radio>
            <el-radio value="3">提醒</el-radio>
            <el-radio value="4">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="form.priority">
            <el-radio :value="1">紧急重要</el-radio>
            <el-radio :value="2">重要</el-radio>
            <el-radio :value="3">一般</el-radio>
            <el-radio :value="4">次要</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="计划完成时间" prop="planTime">
          <el-date-picker
            v-model="form.planTime"
            type="datetime"
            placeholder="选择计划完成时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="form.todoId">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">待处理</el-radio>
            <el-radio :value="1">已完成</el-radio>
            <el-radio :value="2">已取消</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Todo">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Clock, Select, Warning } from '@element-plus/icons-vue'
import { listTodo, getTodo, addTodo, updateTodo, completeTodo, cancelTodo, delTodo, batchDelTodo, getTodoStats } from '@/api/system/todo'
import { parseTime } from '@/utils/mp'

const { proxy } = getCurrentInstance()

const todoList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref('')
const stats = ref({})

const form = ref({})
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  todoTitle: undefined,
  todoType: undefined,
  status: undefined,
  priority: undefined
})

const rules = {
  todoTitle: [{ required: true, message: '待办标题不能为空', trigger: 'blur' }],
  todoType: [{ required: true, message: '待办类型不能为空', trigger: 'change' }],
  priority: [{ required: true, message: '优先级不能为空', trigger: 'change' }]
}

const formRef = ref(null)

// 获取类型标签
function getTypeLabel(type) {
  const labels = { '1': '工作', '2': '会议', '3': '提醒', '4': '其他' }
  return labels[type] || '其他'
}

function getTypeTagColor(type) {
  const colors = { '1': 'primary', '2': 'warning', '3': 'info', '4': '' }
  return colors[type] || ''
}

// 获取优先级标签
function getPriorityLabel(priority) {
  const labels = { 1: '紧急重要', 2: '重要', 3: '一般', 4: '次要' }
  return labels[priority] || '一般'
}

function getPriorityTagColor(priority) {
  const colors = { 1: 'danger', 2: 'warning', 3: '', 4: 'info' }
  return colors[priority] || ''
}

// 获取状态标签
function getStatusLabel(status) {
  const labels = { 0: '待处理', 1: '已完成', 2: '已取消' }
  return labels[status] || '待处理'
}

function getStatusTagColor(status) {
  const colors = { 0: 'warning', 1: 'success', 2: 'info' }
  return colors[status] || ''
}

// 获取统计信息
function getStats() {
  getTodoStats().then(res => {
    stats.value = res.data
  })
}

/** 查询待办列表 */
function getList() {
  loading.value = true
  listTodo(queryParams.value).then(res => {
    todoList.value = res.data.records
    total.value = res.data.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    todoId: undefined,
    todoTitle: undefined,
    todoContent: undefined,
    todoType: '1',
    priority: 3,
    status: 0,
    planTime: undefined
  }
  formRef.value?.resetFields()
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm('queryParams')
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.todoId)
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = '添加待办'
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  getTodo(row.todoId).then(res => {
    form.value = res.data
    open.value = true
    title.value = '编辑待办'
  })
}

/** 完成待办 */
function handleComplete(row) {
  ElMessageBox.confirm('确认完成待办 "' + row.todoTitle + '" 吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    completeTodo(row.todoId).then(() => {
      ElMessage.success('已完成')
      getList()
      getStats()
    })
  })
}

/** 取消待办 */
function handleCancel(row) {
  ElMessageBox.confirm('确认取消待办 "' + row.todoTitle + '" 吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    cancelTodo(row.todoId).then(() => {
      ElMessage.success('已取消')
      getList()
      getStats()
    })
  })
}

/** 提交按钮 */
function submitForm() {
  formRef.value.validate(valid => {
    if (valid) {
      if (form.value.todoId) {
        updateTodo(form.value).then(() => {
          ElMessage.success('修改成功')
          open.value = false
          getList()
          getStats()
        })
      } else {
        addTodo(form.value).then(() => {
          ElMessage.success('新增成功')
          open.value = false
          getList()
          getStats()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const todoIds = row.todoId ? [row.todoId] : ids.value
  const message = row.todoId
    ? `是否确认删除待办 "${row.todoTitle}"？`
    : `是否确认删除选中的 ${todoIds.length} 条数据？`
  ElMessageBox.confirm(message, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    if (row.todoId) {
      delTodo(row.todoId).then(() => {
        ElMessage.success('删除成功')
        getList()
        getStats()
      })
    } else {
      batchDelTodo(todoIds).then(() => {
        ElMessage.success('批量删除成功')
        getList()
        getStats()
      })
    }
  }).catch(() => {
    if (!row.todoId) {
      ids.value = []
    }
  })
}

onMounted(() => {
  getList()
  getStats()
})
</script>

<style scoped lang="scss">
.app-container {
  .mb8 {
    margin-bottom: 8px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .stat-card {
    :deep(.el-card__body) {
      padding: 20px;
    }

    .stat-item {
      display: flex;
      align-items: center;

      .stat-icon {
        font-size: 48px;
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
}
</style>
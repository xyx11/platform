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
        <el-form-item label="优先级">
          <el-select v-model="queryParams.priority" placeholder="请选择优先级" clearable>
            <el-option label="紧急重要" :value="1" />
            <el-option label="重要" :value="2" />
            <el-option label="一般" :value="3" />
            <el-option label="次要" :value="4" />
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
        <el-col :span="1.5">
          <el-button type="warning" plain @click="showRecycleBin">
            <el-icon><Delete /></el-icon> 回收站
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="info" plain @click="showTagManager">
            <el-icon><PriceTag /></el-icon> 标签管理
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
        <el-table-column label="标签" width="150">
          <template #default="{ row }">
            <el-tag
              v-for="tag in row.tags"
              :key="tag.tagId"
              size="small"
              :style="{ backgroundColor: tag.tagColor, color: '#fff', marginRight: '4px' }"
            >
              {{ tag.tagName }}
            </el-tag>
          </template>
        </el-table-column>
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
        <el-table-column label="评论" width="80" align="center">
          <template #default="{ row }">
            <el-badge :value="row.commentCount || 0" :hidden="!row.commentCount" type="info">
              <el-button link type="info" icon="ChatDotRound" @click="showComments(row)" />
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column label="附件" width="80" align="center">
          <template #default="{ row }">
            <el-badge :value="row.attachmentCount || 0" :hidden="!row.attachmentCount" type="warning">
              <el-button link type="warning" icon="Paperclip" @click="showAttachments(row)" />
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column label="计划完成时间" prop="planTime" width="180">
          <template #default="{ row }">
            {{ row.planTime ? parseTime(row.planTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center" fixed="right">
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
        <el-form-item label="标签" prop="tags">
          <el-select v-model="selectedTagIds" multiple placeholder="请选择标签" style="width: 100%">
            <el-option
              v-for="tag in allTags"
              :key="tag.tagId"
              :label="tag.tagName"
              :value="tag.tagId"
            >
              <span :style="{ color: tag.tagColor }">●</span> {{ tag.tagName }}
            </el-option>
          </el-select>
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
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 评论对话框 -->
    <el-dialog title="评论" v-model="commentDialogVisible" width="600px">
      <div class="comment-list">
        <div v-for="comment in commentList" :key="comment.commentId" class="comment-item">
          <div class="comment-header">
            <span class="comment-user">{{ comment.userName }}</span>
            <span class="comment-time">{{ parseTime(comment.createTime) }}</span>
          </div>
          <div class="comment-content">{{ comment.commentContent }}</div>
        </div>
        <el-empty v-if="commentList.length === 0" description="暂无评论" />
      </div>
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="3"
        placeholder="写下你的评论..."
        style="margin-top: 15px"
      />
      <template #footer>
        <el-button @click="commentDialogVisible = false">关 闭</el-button>
        <el-button type="primary" @click="submitComment">发 表</el-button>
      </template>
    </el-dialog>

    <!-- 附件对话框 -->
    <el-dialog title="附件" v-model="attachmentDialogVisible" width="600px">
      <el-upload
        ref="uploadRef"
        :action="uploadUrl"
        :headers="uploadHeaders"
        :data="{ todoId: currentTodoId }"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
      >
        <el-button type="primary" plain>
          <el-icon><Upload /></el-icon> 上传附件
        </el-button>
      </el-upload>
      <el-table :data="attachmentList" style="margin-top: 15px">
        <el-table-column label="文件名" prop="attachmentName" :show-overflow-tooltip="true" />
        <el-table-column label="大小" prop="fileSize" width="100">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button link type="primary" icon="Download" @click="downloadAttachment(row)" />
            <el-button link type="danger" icon="Delete" @click="deleteAttachment(row)" />
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 标签管理对话框 -->
    <el-dialog title="标签管理" v-model="tagDialogVisible" width="800px">
      <TodoTag />
    </el-dialog>
  </div>
</template>

<script setup name="Todo">
import { ref, reactive, onMounted, onUnmounted, computed, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document, Clock, Select, Warning, Search, Refresh, Plus, Delete,
  PriceTag, ChatDotRound, Paperclip, Upload, Download
} from '@element-plus/icons-vue'
import {
  listTodo, getTodo, addTodo, updateTodo, completeTodo, cancelTodo,
  delTodo, batchDelTodo, getTodoStats, listTodoTags, addTagsToTodo,
  listComments, addComment, listAttachments, delAttachment, downloadAttachment as downloadAttachmentApi
} from '@/api/system/todo'
import { parseTime } from '@/utils/mp'
import { getToken } from '@/utils/auth'
import { connectWebSocket, disconnectWebSocket } from '@/utils/websocket'
import TodoTag from './tag.vue'

const { proxy } = getCurrentInstance()
const userId = ref(null)

const todoList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref('')
const stats = ref({})
const allTags = ref([])
const selectedTagIds = ref([])

// 评论相关
const commentDialogVisible = ref(false)
const commentList = ref([])
const newComment = ref('')
const currentTodoId = ref(null)

// 附件相关
const attachmentDialogVisible = ref(false)
const attachmentList = ref([])
const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/system/todo/attachment/upload'
const uploadHeaders = { Authorization: 'Bearer ' + getToken() }

// 标签管理
const tagDialogVisible = ref(false)

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

function getTypeLabel(type) {
  const labels = { '1': '工作', '2': '会议', '3': '提醒', '4': '其他' }
  return labels[type] || '其他'
}

function getTypeTagColor(type) {
  const colors = { '1': 'primary', '2': 'warning', '3': 'info', '4': '' }
  return colors[type] || ''
}

function getPriorityLabel(priority) {
  const labels = { 1: '紧急重要', 2: '重要', 3: '一般', 4: '次要' }
  return labels[priority] || '一般'
}

function getPriorityTagColor(priority) {
  const colors = { 1: 'danger', 2: 'warning', 3: '', 4: 'info' }
  return colors[priority] || ''
}

function getStatusLabel(status) {
  const labels = { 0: '待处理', 1: '已完成', 2: '已取消' }
  return labels[status] || '待处理'
}

function getStatusTagColor(status) {
  const colors = { 0: 'warning', 1: 'success', 2: 'info' }
  return colors[status] || ''
}

function getStats() {
  getTodoStats().then(res => {
    stats.value = res.data
  })
}

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

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    todoId: undefined,
    todoTitle: undefined,
    todoContent: undefined,
    todoType: '1',
    priority: 3,
    planTime: undefined
  }
  selectedTagIds.value = []
  formRef.value?.resetFields()
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryParams')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.todoId)
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加待办'
  getAllTags()
}

function handleUpdate(row) {
  reset()
  getTodo(row.todoId).then(res => {
    form.value = res.data
    if (res.data.tags) {
      selectedTagIds.value = res.data.tags.map(t => t.tagId)
    }
    open.value = true
    title.value = '编辑待办'
  })
  getAllTags()
}

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

function submitForm() {
  formRef.value.validate(valid => {
    if (valid) {
      const data = { ...form.value }
      if (form.value.todoId) {
        updateTodo(data).then(() => {
          if (selectedTagIds.value.length) {
            addTagsToTodo(form.value.todoId, selectedTagIds.value)
          }
          ElMessage.success('修改成功')
          open.value = false
          getList()
          getStats()
        })
      } else {
        addTodo(data).then(() => {
          ElMessage.success('新增成功')
          open.value = false
          getList()
          getStats()
        })
      }
    }
  })
}

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
  })
}

// 标签管理
function getAllTags() {
  listTodoTags().then(res => {
    allTags.value = res.data || []
  })
}

function showTagManager() {
  tagDialogVisible.value = true
}

// 评论
function showComments(row) {
  currentTodoId.value = row.todoId
  commentDialogVisible.value = true
  listComments(row.todoId).then(res => {
    commentList.value = res.data || []
  })
}

function submitComment() {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  addComment({ todoId: currentTodoId.value, commentContent: newComment.value }).then(() => {
    ElMessage.success('评论成功')
    newComment.value = ''
    listComments(currentTodoId.value).then(res => {
      commentList.value = res.data || []
    })
  })
}

// 附件
function showAttachments(row) {
  currentTodoId.value = row.todoId
  attachmentDialogVisible.value = true
  listAttachments(row.todoId).then(res => {
    attachmentList.value = res.data || []
  })
}

function handleUploadSuccess(res) {
  if (res.code === 200) {
    ElMessage.success('上传成功')
    listAttachments(currentTodoId.value).then(res => {
      attachmentList.value = res.data || []
    })
  }
}

function handleUploadError() {
  ElMessage.error('上传失败')
}

function downloadAttachment(row) {
  downloadAttachmentApi(row.attachmentId).then(res => {
    const blob = new Blob([res])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.attachmentName
    link.click()
    window.URL.revokeObjectURL(url)
  })
}

function deleteAttachment(row) {
  ElMessageBox.confirm('确认删除附件 "' + row.attachmentName + '" 吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    delAttachment(row.attachmentId).then(() => {
      ElMessage.success('删除成功')
      listAttachments(currentTodoId.value).then(res => {
        attachmentList.value = res.data || []
      })
    })
  })
}

function formatFileSize(bytes) {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

function showRecycleBin() {
  proxy.$router.push('/system/todo/recycle-bin')
}

onMounted(() => {
  getList()
  getStats()
  getAllTags()
  // 初始化 WebSocket 连接
  initWebSocket()
})

// 初始化 WebSocket
function initWebSocket() {
  // 从 localStorage 获取用户 ID（需要根据实际存储方式调整）
  const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}')
  userId.value = userInfo.userId || userInfo.id

  if (userId.value) {
    connectWebSocket(userId.value, handleNotification)
  }
}

// 处理 WebSocket 通知
function handleNotification(notification) {
  console.log('收到 WebSocket 通知:', notification)
  // 刷新列表和统计
  getList()
  getStats()
}

// 组件卸载时断开 WebSocket
onUnmounted(() => {
  disconnectWebSocket()
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

  .comment-list {
    max-height: 400px;
    overflow-y: auto;

    .comment-item {
      padding: 12px 0;
      border-bottom: 1px solid #eee;

      .comment-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;

        .comment-user {
          font-weight: bold;
          color: #333;
        }

        .comment-time {
          font-size: 12px;
          color: #999;
        }
      }

      .comment-content {
        font-size: 14px;
        color: #666;
      }
    }
  }
}
</style>
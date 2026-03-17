<template>
  <div class="app-container">
    <el-card>
      <!-- 搜索表单 -->
      <el-form :model="queryParams" :inline="true" label-width="80px">
        <el-form-item label="公告标题">
          <el-input v-model="queryParams.noticeTitle" placeholder="请输入公告标题" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="公告类型">
          <el-select v-model="queryParams.noticeType" placeholder="请选择公告类型" clearable>
            <el-option label="通知" value="1" />
            <el-option label="公告" value="2" />
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
          <el-button type="success" plain @click="handleExport">
            <el-icon><Download /></el-icon> 导出
          </el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </el-row>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="noticeList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" type="index" width="80" align="center" />
        <el-table-column label="公告标题" prop="noticeTitle" :show-overflow-tooltip="true" />
        <el-table-column label="公告类型" prop="noticeType" width="100">
          <template #default="{ row }">
            <el-tag :type="row.noticeType === '1' ? 'warning' : 'success'">
              {{ row.noticeType === '1' ? '通知' : '公告' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange"
            />
          </template>
        </el-table-column>
        <el-table-column label="阅读情况" width="150" align="center">
          <template #default="{ row }">
            <el-tooltip placement="top">
              <template #content>
                <div>已读：{{ row.readCount || 0 }} 人</div>
                <div>未读：{{ row.unreadCount || 0 }} 人</div>
              </template>
              <div class="read-status">
                <el-tag size="small" type="success">已读：{{ row.readCount || 0 }}</el-tag>
                <el-tag size="small" type="warning" style="margin-left: 5px">未读：{{ row.unreadCount || 0 }}</el-tag>
              </div>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180">
          <template #default="{ row }">
            {{ row.createTime ? row.createTime.substring(0, 10) : '' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link icon="View" @click="handleView(row)">
              详情
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

    <!-- 公告详情对话框 -->
    <el-dialog title="公告详情" v-model="viewVisible" width="800px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="公告标题">
          {{ viewForm.noticeTitle }}
        </el-descriptions-item>
        <el-descriptions-item label="公告类型">
          <el-tag :type="viewForm.noticeType === '1' ? 'warning' : 'success'">
            {{ viewForm.noticeType === '1' ? '通知' : '公告' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewForm.status === 1 ? 'success' : 'info'">
            {{ viewForm.status === 1 ? '正常' : '关闭' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ viewForm.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="阅读统计" :span="2">
          <el-tag type="success">已读：{{ viewForm.readCount || 0 }} 人</el-tag>
          <el-tag type="warning" style="margin-left: 10px">未读：{{ viewForm.unreadCount || 0 }} 人</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="公告内容" :span="2">
          <div class="notice-content" v-html="viewForm.noticeContent"></div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="title" v-model="open" width="800px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="公告标题" prop="noticeTitle">
          <el-input v-model="form.noticeTitle" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告类型" prop="noticeType">
          <el-radio-group v-model="form.noticeType">
            <el-radio value="1">通知</el-radio>
            <el-radio value="2">公告</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="公告内容" prop="noticeContent">
          <el-input v-model="form.noticeContent" type="textarea" :rows="10" placeholder="请输入公告内容" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">关闭</el-radio>
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

<script setup name="Notice">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(true)
const open = ref(false)
const title = ref('')
const viewVisible = ref(false)
const noticeList = ref([])
const multiple = ref(true)
const total = ref(0)
const ids = ref([])
const showSearch = ref(true)
const form = ref({})
const viewForm = ref({})
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  noticeTitle: undefined,
  noticeType: undefined,
  status: undefined
})
const rules = {
  noticeTitle: [{ required: true, message: '公告标题不能为空', trigger: 'blur' }],
  noticeType: [{ required: true, message: '公告类型不能为空', trigger: 'change' }],
  noticeContent: [{ required: true, message: '公告内容不能为空', trigger: 'blur' }]
}
const formRef = ref(null)

/** 查询公告列表 */
function getList() {
  loading.value = true
  request.get('/system/notice/list', { params: queryParams.value }).then(res => {
    noticeList.value = res.data.records || []
    // 为每条公告获取阅读统计
    noticeList.value.forEach(item => {
      if (item.noticeId) {
        request.get('/system/notice/stats/' + item.noticeId).then(statRes => {
          item.readCount = statRes.data.readCount
          item.unreadCount = statRes.data.unreadCount
        }).catch(() => {
          item.readCount = 0
          item.unreadCount = 0
        })
      }
    })
    total.value = res.data.total || 0
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
    noticeId: undefined,
    noticeTitle: undefined,
    noticeType: '1',
    noticeContent: undefined,
    status: 1
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
  queryParams.value = { pageNum: 1, pageSize: 10, noticeTitle: undefined, noticeType: undefined, status: undefined }
  handleQuery()
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = '添加公告'
}

/** 查看详情 */
function handleView(row) {
  request.get('/system/notice/' + row.noticeId).then(res => {
    viewForm.value = res.data
    viewVisible.value = true
  })
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const noticeId = row.noticeId || ids.value[0]
  request.get('/system/notice/' + noticeId).then(res => {
    form.value = res.data
    open.value = true
    title.value = '编辑公告'
  })
}

/** 提交按钮 */
function submitForm() {
  formRef.value.validate(valid => {
    if (valid) {
      if (form.value.noticeId) {
        request.put('/system/notice', form.value).then(() => {
          ElMessage.success('修改成功')
          open.value = false
          getList()
        })
      } else {
        request.post('/system/notice', form.value).then(() => {
          ElMessage.success('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const noticeIds = row.noticeId ? [row.noticeId] : ids.value
  const message = row.noticeId ? `是否确认删除公告编号为"${row.noticeId}"的数据项？` : `是否确认批量删除选中的 ${noticeIds.length} 条数据？`
  ElMessageBox.confirm(message, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    if (row.noticeId) {
      request.delete('/system/notice/' + noticeIds[0]).then(() => {
        ElMessage.success('删除成功')
        getList()
      })
    } else {
      request.delete('/system/notice/batch', { data: noticeIds }).then(() => {
        ElMessage.success('批量删除成功')
        getList()
      })
    }
  }).catch(() => {
    if (!row.noticeId) {
      ids.value = []
    }
  })
}

/** 状态修改 */
function handleStatusChange(row) {
  const text = row.status === 1 ? '启用' : '停用'
  ElMessageBox.confirm('确认要"' + text + '""' + row.noticeTitle + '"公告吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.put('/system/notice/status', row).then(() => {
      ElMessage.success(text + '成功')
    })
  }).catch(() => {
    row.status = row.status === 1 ? 0 : 1
  })
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.noticeId)
  multiple.value = !selection.length
}

/** 导出按钮操作 */
function handleExport() {
  const params = {
    noticeTitle: queryParams.value.noticeTitle,
    noticeType: queryParams.value.noticeType,
    status: queryParams.value.status
  }
  request.get('/system/notice/export', { params, responseType: 'blob' }).then(res => {
    downloadExcel(res, '通知公告_' + new Date().getTime() + '.xlsx')
    ElMessage.success('导出成功')
  })
}

getList()
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

  .read-status {
    display: flex;
    align-items: center;
  }

  .notice-content {
    line-height: 1.8;
    white-space: pre-wrap;
    word-break: break-all;
  }
}
</style>
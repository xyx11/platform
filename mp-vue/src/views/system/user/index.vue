<template>
  <div class="user-container">
    <el-card>
      <el-form :model="queryParams" inline>
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
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
          <el-button type="primary" icon="Plus" @click="handleAdd">新增用户</el-button>
          <el-button type="danger" icon="Delete" :disabled="multiple" @click="handleBatchDelete">批量删除</el-button>
          <el-button type="warning" icon="Upload" @click="handleImport">导入</el-button>
          <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
          <el-button type="info" icon="Document" @click="downloadTemplate">下载模板</el-button>
        </div>
      </template>

      <el-table
        :data="userList"
        border
        stripe
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="userId" label="用户 ID" width="100" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="deptName" label="部门" width="150" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
            <el-button link type="warning" icon="Key" @click="handleResetPwd(row)">重置密码</el-button>
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
          @size-change="getUserList"
          @current-change="getUserList"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="!!form.userId" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.userId">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-tree-select
            v-model="form.deptId"
            :data="deptList"
            :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
            check-strictly
            placeholder="请选择部门"
          />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-checkbox-group v-model="form.roleIds">
            <el-checkbox v-for="role in roleList" :key="role.roleId" :label="role.roleId">
              {{ role.roleName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog title="重置密码" v-model="pwdDialog.visible" width="500px">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
        <el-form-item label="新密码" prop="password">
          <el-input v-model="pwdForm.password" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitPwdForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog title="导入用户数据" v-model="importDialog.visible" width="500px">
      <el-upload
        ref="uploadRef"
        drag
        :limit="1"
        :accept="'.xlsx, .xls'"
        :before-upload="handleBeforeUpload"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 xlsx/xls 文件，且不超过 10MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitImport" :loading="importing">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

const userList = ref([])
const deptList = ref([])
const roleList = ref([])
const loading = ref(false)
const multiple = ref(true)
const userIds = ref([])
const importDialog = reactive({
  visible: false
})
const importing = ref(false)
const uploadFile = ref(null)

const queryParams = reactive({
  username: '',
  phone: '',
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

const pwdDialog = reactive({
  visible: false
})

const formRef = ref(null)
const pwdFormRef = ref(null)
const uploadRef = ref(null)

const form = reactive({
  userId: null,
  username: '',
  password: '',
  nickname: '',
  deptId: null,
  gender: 1,
  phone: '',
  email: '',
  status: 1,
  roleIds: []
})

const pwdForm = reactive({
  userId: null,
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }]
}

const pwdRules = {
  password: [{ required: true, message: '请输入新密码', trigger: 'blur' }]
}

// 获取用户列表
const getUserList = () => {
  loading.value = true
  request.get('/system/user/list', { params: queryParams }).then(res => {
    userList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// 获取部门列表
const getDeptList = () => {
  request.get('/system/dept/list').then(res => {
    deptList.value = res.data
  })
}

// 获取角色列表
const getRoleList = () => {
  request.get('/system/role/list').then(res => {
    roleList.value = res.data
  })
}

// 查询
const handleQuery = () => {
  pagination.current = 1
  getUserList()
}

// 重置查询
const resetQuery = () => {
  queryParams.username = ''
  queryParams.phone = ''
  queryParams.status = null
  handleQuery()
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  userIds.value = selection.map(item => item.userId)
  multiple.value = !selection.length
}

// 新增
const handleAdd = () => {
  dialog.title = '新增用户'
  dialog.visible = true
}

// 修改
const handleUpdate = (row) => {
  dialog.title = '修改用户'
  dialog.visible = true
  Object.assign(form, row, { roleIds: row.roleIds || [] })
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除用户 "${row.username}" 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    request.delete(`/system/user/${row.userId}`).then(() => {
      ElMessage.success('删除成功')
      getUserList()
    })
  })
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确认删除选中的 ${userIds.value.length} 个用户吗？`, '警告', {
    type: 'warning'
  }).then(() => {
    request.delete('/system/user/batch', { data: userIds.value }).then(() => {
      ElMessage.success('批量删除成功')
      getUserList()
    })
  })
}

// 重置密码
const handleResetPwd = (row) => {
  pwdDialog.visible = true
  pwdForm.userId = row.userId
  pwdForm.password = ''
}

// 导出
const handleExport = () => {
  const params = {
    ...queryParams,
    pageNum: 1,
    pageSize: 1000
  }
  request.get('/system/user/export', { params, responseType: 'blob' }).then(res => {
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '用户数据_' + new Date().getTime() + '.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  })
}

// 下载模板
const downloadTemplate = () => {
  request.get('/system/user/downloadTemplate', { responseType: 'blob' }).then(res => {
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '用户导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  })
}

// 导入
const handleImport = () => {
  importDialog.visible = true
  uploadFile.value = null
  uploadRef.value?.clearFiles()
}

// 上传前校验
const handleBeforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                  file.type === 'application/vnd.ms-excel'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isExcel) {
    ElMessage.error('只能上传 Excel 文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

// 文件改变
const handleFileChange = (file) => {
  uploadFile.value = file.raw
}

// 文件移除
const handleFileRemove = () => {
  uploadFile.value = null
}

// 提交导入
const submitImport = () => {
  if (!uploadFile.value) {
    ElMessage.error('请选择要导入的文件')
    return
  }

  importing.value = true
  const formData = new FormData()
  formData.append('file', uploadFile.value)

  request.post('/system/user/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(res => {
    ElMessage.success(`导入成功！成功：${res.data?.success || 0}，失败：${res.data?.failed || 0}`)
    importDialog.visible = false
    getUserList()
  }).catch(() => {
    ElMessage.error('导入失败')
  }).finally(() => {
    importing.value = false
  })
}

// 提交表单
const submitForm = () => {
  formRef.value.validate(valid => {
    if (valid) {
      const api = form.userId ? request.put : request.post
      api('/system/user', form).then(() => {
        ElMessage.success('操作成功')
        dialog.visible = false
        getUserList()
      })
    }
  })
}

// 提交密码重置
const submitPwdForm = () => {
  pwdFormRef.value.validate(valid => {
    if (valid) {
      request.put('/system/user/password', pwdForm).then(() => {
        ElMessage.success('密码重置成功')
        pwdDialog.visible = false
      })
    }
  })
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    userId: null,
    username: '',
    password: '',
    nickname: '',
    deptId: null,
    gender: 1,
    phone: '',
    email: '',
    status: 1,
    roleIds: []
  })
}

onMounted(() => {
  getUserList()
  getDeptList()
  getRoleList()
})
</script>

<style lang="scss" scoped>
.user-container {
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

  .el-icon--upload {
    font-size: 67px;
    color: #8c939d;
    margin: 40px 0 16px;
  }
}
</style>
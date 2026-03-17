<template>
  <div class="user-container">
    <el-row :gutter="16" style="height: calc(100vh - 100px)">
      <!-- 左侧部门树 -->
      <el-col :span="4" class="dept-tree-col">
        <el-card shadow="never" class="dept-tree-card">
          <template #header>
            <div class="dept-tree-header">
              <span class="card-title"><el-icon><OfficeBuilding /></el-icon> 组织机构</span>
              <el-button link type="primary" icon="Refresh" @click="refreshDeptTree" circle size="small" />
            </div>
          </template>
          <el-tree
            ref="deptTreeRef"
            :data="deptTreeData"
            :props="{ id: 'deptId', label: 'deptName', children: 'children' }"
            node-key="deptId"
            highlight-current
            default-expand-all
            @node-click="handleNodeClick"
            class="dept-tree"
          >
            <template #default="{ data }">
              <span class="dept-tree-node">
                <el-icon class="dept-icon"><OfficeBuilding /></el-icon>
                <span>{{ data.deptName }}</span>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <!-- 右侧用户列表 -->
      <el-col :span="20">
        <el-card shadow="never" class="search-card">
          <el-form :model="queryParams" inline label-width="70px">
            <el-form-item label="用户名">
              <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable style="width: 160px" @keyup.enter="handleQuery">
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable style="width: 150px" @keyup.enter="handleQuery">
                <template #prefix><el-icon><Cellphone /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
                <el-option label="正常" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQuery">
                <el-icon><Search /></el-icon> 查询
              </el-button>
              <el-button @click="resetQuery">
                <el-icon><Refresh /></el-icon> 重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="header-top">
              <div class="header-actions">
                <el-button type="primary" @click="handleAdd">
                  <el-icon><Plus /></el-icon> 新增
                </el-button>
                <el-button type="danger" :disabled="multiple" @click="handleBatchDelete">
                  <el-icon><Delete /></el-icon> 批量删除
                </el-button>
                <el-button type="warning" :disabled="multiple" @click="handleBatchResetPwd">
                  <el-icon><Key /></el-icon> 批量重置
                </el-button>
                <el-dropdown @command="handleCommand">
                  <el-button type="info">
                    <el-icon><Download /></el-icon> 导入导出 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="import"><el-icon><Upload /></el-icon> 导入数据</el-dropdown-item>
                      <el-dropdown-item command="export"><el-icon><Download /></el-icon> 导出数据</el-dropdown-item>
                      <el-dropdown-item command="template"><el-icon><Document /></el-icon> 下载模板</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <el-dropdown @command="handleSettingCommand" class="ml-auto">
                  <el-button type="default" circle>
                    <el-icon><Setting /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item divided>表格设置</el-dropdown-item>
                      <el-dropdown-item v-for="col in columns" :key="col.prop" :disabled="col.disabled">
                        <el-checkbox
                          v-model="col.visible"
                          @change="toggleColumn(col.prop)"
                          :disabled="col.disabled"
                        >
                          {{ col.label }}
                        </el-checkbox>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>

              <!-- 统计信息 -->
              <div class="header-stats">
                <div class="stat-item">
                  <el-icon class="stat-icon" style="color: #1e80ff"><User /></el-icon>
                  <div class="stat-content">
                    <span class="stat-label">用户总数</span>
                    <span class="stat-value">{{ userCount }}</span>
                  </div>
                </div>
                <div class="stat-item">
                  <el-icon class="stat-icon" style="color: #67c23a"><CircleCheck /></el-icon>
                  <div class="stat-content">
                    <span class="stat-label">正常</span>
                    <span class="stat-value" style="color: #67c23a">{{ normalCount }}</span>
                  </div>
                </div>
                <div class="stat-item">
                  <el-icon class="stat-icon" style="color: #f56c6c"><CircleClose /></el-icon>
                  <div class="stat-content">
                    <span class="stat-label">禁用</span>
                    <span class="stat-value" style="color: #f56c6c">{{ disabledCount }}</span>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <el-table
            :key="tableKey"
            :data="userList"
            v-loading="loading"
            @selection-change="handleSelectionChange"
            :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
            class="user-table"
          >
            <el-table-column type="selection" width="45" align="center" />
            <el-table-column
              v-if="columns.find(c => c.prop === 'userId')?.visible"
              prop="userId"
              label="用户 ID"
              width="100"
            />
            <el-table-column
              v-if="columns.find(c => c.prop === 'userInfo')?.visible"
              label="用户信息"
              min-width="200"
            >
              <template #default="{ row }">
                <div class="user-info">
                  <el-avatar :size="40" :src="row.avatar || getAvatarUrl(row.gender)">
                    {{ row.username?.charAt(0)?.toUpperCase() }}
                  </el-avatar>
                  <div class="user-detail">
                    <div class="user-name">{{ row.username }}</div>
                    <div class="user-nickname">{{ row.nickname }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column
              v-if="columns.find(c => c.prop === 'deptName')?.visible"
              prop="deptName"
              label="部门"
              width="140"
              show-overflow-tooltip
            />
            <el-table-column
              v-if="columns.find(c => c.prop === 'phone')?.visible"
              prop="phone"
              label="手机号"
              width="130"
            >
              <template #default="{ row }">
                <el-tag type="info" size="small" v-if="row.phone">
                  <el-icon><Cellphone /></el-icon> {{ row.phone }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
              v-if="columns.find(c => c.prop === 'email')?.visible"
              prop="email"
              label="邮箱"
              width="180"
              show-overflow-tooltip
            />
            <el-table-column
              v-if="columns.find(c => c.prop === 'status')?.visible"
              prop="status"
              label="状态"
              width="100"
              align="center"
            >
              <template #default="{ row }">
                <el-switch
                  v-model="row.status"
                  :active-value="1"
                  :inactive-value="0"
                  @change="handleStatusChange(row)"
                  :active-color="'#13ce66'"
                  :inactive-color="'#ff4949'"
                />
              </template>
            </el-table-column>
            <el-table-column
              v-if="columns.find(c => c.prop === 'createTime')?.visible"
              prop="createTime"
              label="创建时间"
              width="170"
            />
            <el-table-column label="操作" width="280" fixed="right" align="center">
              <template #default="{ row }">
                <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">编辑</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
                <el-button link type="warning" icon="Key" @click="handleResetPwd(row)">密码</el-button>
                <el-button link type="success" icon="UserFilled" @click="handleAssignRole(row)">角色</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              :current-page="pagination.current"
              :page-size="pagination.size"
              :total="pagination.total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="getUserList"
              @current-change="getUserList"
            />
            <div class="pagination-info">
              共 {{ pagination.total }} 条，每页 {{ pagination.size }} 条，当前第 {{ pagination.current }} 页
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="700px"
      @close="resetForm"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" :disabled="!!form.userId" />
              <div class="form-tip">用户名必须以字母开头，只能包含字母、数字和下划线</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" prop="password" v-if="!form.userId">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
              <div class="form-tip">密码长度至少 6 位</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio-button :label="1">男</el-radio-button>
                <el-radio-button :label="0">女</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="部门" prop="deptId">
              <el-tree-select
                v-model="form.deptId"
                :data="deptTreeData"
                :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
                check-strictly
                placeholder="请选择部门"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio-button :label="1">正常</el-radio-button>
                <el-radio-button :label="0">禁用</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11">
                <template #prefix><el-icon><Cellphone /></el-icon></template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱">
                <template #prefix><el-icon><Message /></el-icon></template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="角色" prop="roleIds">
              <div class="role-checkbox-group">
                <el-checkbox v-for="role in roleList" :key="role.roleId" :label="String(role.roleId)" border>
                  {{ role.roleName }}
                </el-checkbox>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog title="重置密码" v-model="pwdDialog.visible" width="500px" @close="resetPwdForm">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
        <el-alert
          title="提示"
          description="重置密码后，用户需要使用新密码重新登录"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        />
        <el-form-item label="新密码" prop="password">
          <el-input v-model="pwdForm.password" type="password" placeholder="请输入新密码，至少 6 位" show-password maxlength="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitPwdForm" :loading="pwdSubmitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog title="分配角色" v-model="roleDialog.visible" width="500px" @close="roleForm.roleIds = []">
      <el-form label-width="100px">
        <el-form-item label="当前用户">
          <span>{{ roleForm.username }}</span>
        </el-form-item>
        <el-form-item label="选择角色">
          <el-checkbox-group v-model="roleForm.roleIds">
            <el-checkbox v-for="role in roleList" :key="role.roleId" :label="String(role.roleId)" border>
              {{ role.roleName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitRoleForm" :loading="roleSubmitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog title="导入用户数据" v-model="importDialog.visible" width="550px" @close="handleImportClose">
      <el-alert
        title="导入说明"
        description="请先下载模板，按照模板格式填写用户数据后上传"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />
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

<script setup name="SysUser">
import { logger } from '@/utils/logger'
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  UploadFilled, OfficeBuilding, User, Cellphone, Search, Refresh, Plus, Delete,
  Key, Download, Upload, Document, ArrowDown, Setting, CircleCheck, CircleClose, Message
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const userList = ref([])
const deptTreeData = ref([])
const roleList = ref([])
const loading = ref(false)
const submitting = ref(false)
const pwdSubmitting = ref(false)
const roleSubmitting = ref(false)
const multiple = ref(true)
const userIds = ref([])
const tableKey = ref(0)
const importDialog = reactive({
  visible: false
})
const importing = ref(false)
const uploadFile = ref(null)
const deptTreeRef = ref(null)

const queryParams = reactive({
  username: '',
  phone: '',
  status: null,
  deptId: null
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

const roleDialog = reactive({
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

const roleForm = reactive({
  userId: null,
  username: '',
  roleIds: []
})

// 表格列配置
const columns = reactive([
  { prop: 'userId', label: '用户 ID', visible: true, disabled: false },
  { prop: 'userInfo', label: '用户信息', visible: true, disabled: true },
  { prop: 'deptName', label: '部门', visible: true, disabled: false },
  { prop: 'phone', label: '手机号', visible: true, disabled: false },
  { prop: 'email', label: '邮箱', visible: true, disabled: false },
  { prop: 'status', label: '状态', visible: true, disabled: false },
  { prop: 'createTime', label: '创建时间', visible: true, disabled: false }
])

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur', min: 2, max: 20 },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]{1,19}$/, message: '用户名必须以字母开头，只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur', min: 6, max: 20 }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const pwdRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur', min: 6, max: 20 },
    { pattern: /^\S{6,}$/, message: '密码长度至少为 6 位', trigger: 'blur' }
  ]
}

// 统计信息
const userCount = computed(() => pagination.total)
const normalCount = computed(() => userList.value.filter(u => u.status === 1).length)
const disabledCount = computed(() => userList.value.filter(u => u.status === 0).length)

// 获取默认头像
const getAvatarUrl = (gender) => {
  const baseUrl = 'https://api.dicebear.com/7.x/avataaars/svg?seed='
  return gender === 0 ? baseUrl + 'female' : baseUrl + 'male'
}

// 获取部门树
const getDeptTree = () => {
  request.get('/system/dept/list').then(res => {
    deptTreeData.value = res.data || []
  }).catch(err => {
    // 获取部门树失败
  })
}

// 刷新部门树
const refreshDeptTree = () => {
  getDeptTree()
  ElMessage.success('刷新成功')
}

// 部门树节点点击
const handleNodeClick = (data) => {
  queryParams.deptId = data.deptId
  handleQuery()
}

// 获取用户列表
const getUserList = () => {
  loading.value = true
  const params = {
    ...queryParams,
    pageNum: pagination.current,
    pageSize: pagination.size
  }
  request.get('/system/user/list', { params }).then(res => {
    const pageData = res.data || {}
    userList.value = pageData.records || []
    pagination.total = Number(pageData.total) || 0
  }).catch(err => {
    logger.error('获取用户列表失败:', err)
    ElMessage.error('获取用户列表失败')
  }).finally(() => {
    loading.value = false
  })
}

// 获取角色列表
const getRoleList = () => {
  request.get('/system/role/list').then(res => {
    roleList.value = res.data?.records || []
  }).catch(err => {
    // 获取角色列表失败
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
  queryParams.deptId = null
  if (deptTreeRef.value) {
    deptTreeRef.value.setCurrentKey(null)
  }
  handleQuery()
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  userIds.value = selection.map(item => item.userId)
  multiple.value = !selection.length
}

// 下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'import':
      handleImport()
      break
    case 'export':
      handleExport()
      break
    case 'template':
      downloadTemplate()
      break
  }
}

// 设置菜单命令
const handleSettingCommand = (command) => {
  // 表格设置
}

// 切换列显示
const toggleColumn = (prop) => {
  tableKey.value++
  ElMessage.success('列显示已更新')
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
  form.userId = row.userId
  form.username = row.username
  form.nickname = row.nickname
  form.deptId = row.deptId
  form.gender = row.gender
  form.phone = row.phone
  form.email = row.email
  form.status = row.status
  form.roleIds = (row.roleIds || []).map(id => String(id))
  form.avatar = row.avatar
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除用户 "${row.username}" 吗？删除后无法恢复！`, '警告', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    request.delete(`/system/user/${row.userId}`).then(() => {
      ElMessage.success('删除成功')
      getUserList()
    })
  }).catch(() => {})
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确认删除选中的 ${userIds.value.length} 个用户吗？删除后无法恢复！`, '警告', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    request.delete('/system/user/batch', { data: userIds.value }).then(() => {
      ElMessage.success('批量删除成功')
      getUserList()
    })
  }).catch(() => {})
}

// 状态改变
const handleStatusChange = (row) => {
  const statusText = row.status === 1 ? '启用' : '禁用'
  ElMessageBox.confirm(`确认${statusText}用户 "${row.username}" 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    request.put('/system/user/status', { userId: row.userId, status: row.status }).then(() => {
      ElMessage.success(`${statusText}成功`)
    }).catch(() => {
      row.status = row.status === 1 ? 0 : 1
    })
  }).catch(() => {
    row.status = row.status === 1 ? 0 : 1
  })
}

// 重置密码
const handleResetPwd = (row) => {
  pwdDialog.visible = true
  pwdForm.userId = row.userId
  pwdForm.password = ''
}

// 批量重置密码
const handleBatchResetPwd = () => {
  if (userIds.value.length === 0) {
    ElMessage.warning('请先选择用户')
    return
  }
  ElMessageBox.confirm(`确认重置选中的 ${userIds.value.length} 个用户的密码吗？`, '警告', {
    type: 'warning'
  }).then(() => {
    ElMessageBox.prompt('请输入新密码', '提示', {
      inputType: 'password',
      inputPattern: /.{6,}/,
      inputErrorMessage: '密码长度至少为 6 位',
      inputValue: ''
    }).then(({ value }) => {
      request.put('/system/user/password/batch', {
        userIds: userIds.value,
        password: value
      }).then(() => {
        ElMessage.success('密码重置成功')
        getUserList()
      })
    })
  }).catch(() => {})
}

// 分配角色
const handleAssignRole = (row) => {
  roleDialog.visible = true
  roleForm.userId = row.userId
  roleForm.username = row.username
  roleForm.roleIds = []
  request.get('/system/user/roles/' + row.userId).then(res => {
    roleForm.roleIds = (res.data?.roleIds || []).map(id => String(id))
  })
}

// 提交角色分配
const submitRoleForm = () => {
  roleSubmitting.value = true
  request.post('/system/user/roles?userId=' + roleForm.userId, roleForm.roleIds).then(() => {
    ElMessage.success('角色分配成功')
    roleDialog.visible = false
  }).catch(() => {}).finally(() => {
    roleSubmitting.value = false
  })
}

// 导出
const handleExport = () => {
  loading.value = true
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
  }).catch(() => {
    ElMessage.error('导出失败')
  }).finally(() => {
    loading.value = false
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
  }).catch(() => {
    ElMessage.error('模板下载失败')
  })
}

// 导入
const handleImport = () => {
  importDialog.visible = true
  uploadFile.value = null
  uploadRef.value?.clearFiles()
}

// 关闭导入对话框
const handleImportClose = () => {
  uploadRef.value?.clearFiles()
  uploadFile.value = null
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

  request.post('/system/user/import', formData).then(res => {
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
      submitting.value = true
      const api = form.userId !== null && form.userId !== undefined && form.userId !== '' ? request.put : request.post
      const submitData = { ...form, roleIds: form.roleIds }
      api('/system/user', submitData).then(() => {
        ElMessage.success('操作成功')
        dialog.visible = false
        getUserList()
      }).catch(() => {}).finally(() => {
        submitting.value = false
      })
    }
  })
}

// 提交密码重置
const submitPwdForm = () => {
  pwdFormRef.value.validate(valid => {
    if (valid) {
      pwdSubmitting.value = true
      request.put('/system/user/password', pwdForm).then(() => {
        ElMessage.success('密码重置成功')
        pwdDialog.visible = false
      }).catch(() => {}).finally(() => {
        pwdSubmitting.value = false
      })
    }
  })
}

// 重置密码表单
const resetPwdForm = () => {
  pwdFormRef.value?.resetFields()
  pwdForm.userId = null
  pwdForm.password = ''
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

// 键盘快捷键
const handleKeyPress = (e) => {
  if ((e.ctrlKey || e.metaKey) && e.key === 'n') {
    e.preventDefault()
    handleAdd()
  }
  if ((e.ctrlKey || e.metaKey) && e.key === 'r') {
    e.preventDefault()
    getUserList()
    ElMessage.success('刷新成功')
  }
}

onMounted(() => {
  getUserList()
  getDeptTree()
  getRoleList()
  document.addEventListener('keydown', handleKeyPress)
})
</script>

<style lang="scss" scoped>
$primary-color: #1e80ff;
$success-color: #13ce66;
$warning-color: #e6a23c;
$danger-color: #ff4949;
$text-primary: #333333;
$text-regular: #606266;
$text-secondary: #909399;
$border-color: #e3e4e6;
$bg-color-page: #f5f6f7;

.user-container {
  padding: 16px;
  height: 100%;
  background-color: $bg-color-page;

  .dept-tree-col {
    height: 100%;

    .dept-tree-card {
      height: 100%;
      border-radius: 8px;

      .dept-tree-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .card-title {
          display: flex;
          align-items: center;
          gap: 8px;
          font-weight: 600;
          font-size: 15px;
          color: $text-primary;

          .el-icon {
            color: $primary-color;
          }
        }
      }

      .dept-tree {
        padding: 8px 0;

        .dept-tree-node {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 8px 12px;
          border-radius: 6px;
          transition: all 0.2s;

          .dept-icon {
            color: $primary-color;
            font-size: 16px;
          }

          &:hover {
            background-color: #f0f9ff;
          }
        }

        .is-current > .dept-tree-node {
          background-color: #e6f4ff;
          color: $primary-color;
          font-weight: 500;
        }

        .el-tree-node__content {
          height: auto;
          padding: 4px 0;
        }
      }
    }
  }

  .search-card {
    margin-bottom: 16px;
    border-radius: 8px;

    .el-form {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }
  }

  .table-card {
    border-radius: 8px;

    .header-top {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .header-actions {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
      align-items: center;

      .ml-auto {
        margin-left: auto;
      }
    }

    .header-stats {
      display: flex;
      gap: 32px;
      padding: 12px 0;
      border-top: 1px solid $border-color;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 12px;

        .stat-icon {
          font-size: 28px;
          width: 40px;
          height: 40px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: rgba(0, 0, 0, 0.04);
          border-radius: 8px;
        }

        .stat-content {
          display: flex;
          flex-direction: column;

          .stat-label {
            font-size: 12px;
            color: $text-secondary;
          }

          .stat-value {
            font-size: 20px;
            font-weight: 600;
            color: $text-primary;
          }
        }
      }
    }

    .user-table {
      .user-info {
        display: flex;
        align-items: center;
        gap: 12px;

        .user-detail {
          display: flex;
          flex-direction: column;
          gap: 4px;

          .user-name {
            font-weight: 500;
            color: $text-primary;
          }

          .user-nickname {
            font-size: 12px;
            color: $text-secondary;
          }
        }
      }

      .el-tag {
        display: inline-flex;
        align-items: center;
        gap: 4px;
      }
    }

    .pagination-wrapper {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
      align-items: center;
      gap: 16px;
      padding-top: 16px;
      border-top: 1px solid $border-color;

      .pagination-info {
        font-size: 13px;
        color: $text-regular;
        white-space: nowrap;
      }
    }
  }

  .el-icon--upload {
    font-size: 67px;
    color: #8c939d;
    margin: 40px 0 16px;
  }

  // 对话框样式优化
  :deep(.el-dialog) {
    border-radius: 12px;

    .el-dialog__header {
      padding: 16px 24px;
      border-bottom: 1px solid $border-color;
    }

    .el-dialog__title {
      font-weight: 600;
      font-size: 16px;
    }

    .el-dialog__body {
      padding: 24px;
    }

    .el-dialog__footer {
      padding: 12px 24px;
      border-top: 1px solid $border-color;
    }
  }

  // 复选框样式
  :deep(.el-checkbox) {
    margin-right: 16px;
    margin-bottom: 12px;
  }

  // 角色复选框组样式
  .role-checkbox-group {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }

  // 表格单元格内边距
  :deep(.el-table__cell) {
    padding: 12px 0;
  }

  // 表单提示文字
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
    line-height: 1.5;
  }
}
</style>
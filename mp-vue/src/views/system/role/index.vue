<template>
  <div class="role-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="角色名称">
          <el-input v-model="queryParams.roleName" placeholder="请输入角色名称" clearable @keyup.enter="handleQuery" style="width: 200px" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="queryParams.roleCode" placeholder="请输入角色编码" clearable @keyup.enter="handleQuery" style="width: 200px" />
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

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="header-actions">
          <span class="card-title">角色列表</span>
          <div class="card-buttons">
            <el-button type="primary" @click="handleAdd">新增角色</el-button>
            <el-button type="danger" :disabled="multiple" @click="handleBatchDelete">批量删除</el-button>
            <el-button @click="handleExport">导出</el-button>
          </div>
        </div>
      </template>

      <el-table :data="roleList" border v-loading="loading" @selection-change="handleSelectionChange" :cell-style="{ padding: '8px 0' }" :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="roleId" label="角色 ID" width="100" />
        <el-table-column prop="roleName" label="角色名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="roleCode" label="角色编码" min-width="150" show-overflow-tooltip />
        <el-table-column prop="dataScope" label="数据范围" width="120">
          <template #default="{ row }">
            <el-tag :type="getDataScopeType(row.dataScope)" size="small">
              {{ getDataScopeText(row.dataScope) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleUpdate(row)">修改</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            <el-button link type="warning" size="small" @click="handleAuthMenu(row)">分配权限</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          :current-page="pagination.current"
          :page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
        <div class="pagination-info">
          共 {{ pagination.total }} 条，每页 {{ pagination.size }} 条，当前第 {{ pagination.current }} 页
        </div>
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="500px"
      @close="resetForm"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" maxlength="30" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" maxlength="30" />
        </el-form-item>
        <el-form-item label="数据范围" prop="dataScope">
          <el-select v-model="form.dataScope" placeholder="请选择数据范围" style="width: 100%">
            <el-option label="全部数据" :value="1" />
            <el-option label="本部门及以下" :value="2" />
            <el-option label="本部门" :value="3" />
            <el-option label="仅本人" :value="4" />
          </el-select>
          <div class="form-tip">分配用户时，数据范围决定用户可看到哪些部门的数据</div>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
          <div class="form-tip">数值越小越靠前</div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入备注" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog title="分配权限" v-model="authDialog.visible" width="500px" :close-on-click-modal="false">
      <div class="auth-toolbar">
        <el-input v-model="menuFilterText" placeholder="搜索菜单..." prefix-icon="Search" clearable style="width: 200px" />
        <div class="auth-actions">
          <el-checkbox v-model="expandAll" @change="handleExpandChange">展开/收起</el-checkbox>
          <el-button size="small" @click="handleCheckAll">全选</el-button>
          <el-button size="small" @click="handleUncheckAll">取消全选</el-button>
        </div>
      </div>
      <el-tree
        ref="menuTreeRef"
        :data="menuList"
        :props="{ children: 'children', label: 'menuName' }"
        :filter-node-method="filterNode"
        show-checkbox
        node-key="menuId"
        :default-checked-keys="checkedKeys"
        :expand-on-click-node="false"
      />
      <template #footer>
        <el-button @click="authDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitAuth" :loading="submittingAuth">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SysRole">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

const roleList = ref([])
const menuList = ref([])
const loading = ref(false)
const submitting = ref(false)
const submittingAuth = ref(false)
const multiple = ref(true)
const roleIds = ref([])
const expandAll = ref(true)

const queryParams = reactive({
  roleName: '',
  roleCode: '',
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

const authDialog = reactive({
  visible: false
})

const menuFilterText = ref('')
const formRef = ref(null)
const menuTreeRef = ref(null)

const form = reactive({
  roleId: null,
  roleName: '',
  roleCode: '',
  dataScope: 1,
  sort: 0,
  status: 1,
  description: ''
})

const checkedKeys = ref([])
const currentRoleId = ref(null)

const rules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-z_]+$/, message: '只能输入小写字母和下划线', trigger: 'blur' }
  ]
}

// 监听搜索框输入
watch(menuFilterText, (val) => {
  menuTreeRef.value?.filter(val)
})

// 获取角色列表
const getRoleList = () => {
  loading.value = true
  const params = {
    ...queryParams,
    pageNum: pagination.current,
    pageSize: pagination.size
  }
  request.get('/system/role/list', { params }).then(res => {
    const pageData = res.data || {}
    roleList.value = pageData.records || []
    pagination.total = Number(pageData.total) || 0
  }).catch(() => {
    ElMessage.error('获取角色列表失败')
  }).finally(() => {
    loading.value = false
  })
}

// 查询
const handleQuery = () => {
  pagination.current = 1
  getRoleList()
}

// 重置
const resetQuery = () => {
  queryParams.roleName = ''
  queryParams.roleCode = ''
  queryParams.status = null
  handleQuery()
}

// 分页大小改变
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  getRoleList()
}

// 当前页改变
const handleCurrentChange = (page) => {
  pagination.current = page
  getRoleList()
}

// 获取菜单列表
const getMenuList = () => {
  request.get('/system/menu/list').then(res => {
    menuList.value = res.data || []
  })
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  roleIds.value = selection.map(item => item.roleId)
  multiple.value = !selection.length
}

// 数据范围类型
const getDataScopeType = (dataScope) => {
  const types = { 1: 'success', 2: 'warning', 3: 'primary', 4: 'info' }
  return types[dataScope] || 'info'
}

const getDataScopeText = (dataScope) => {
  const texts = { 1: '全部数据', 2: '本部门及以下', 3: '本部门', 4: '仅本人' }
  return texts[dataScope] || '未知'
}

// 状态变更
const handleStatusChange = (row) => {
  const text = row.status === 1 ? '启用' : '禁用'
  ElMessageBox.confirm(`确认要${text}角色"${row.roleName}"吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    request.put('/system/role', { roleId: row.roleId, status: row.status }).then(() => {
      ElMessage.success(`${text}成功`)
    }).catch(() => {
      row.status = row.status === 1 ? 0 : 1
    })
  }).catch(() => {
    row.status = row.status === 1 ? 0 : 1
  })
}

// 新增
const handleAdd = () => {
  dialog.title = '新增角色'
  dialog.visible = true
}

// 修改
const handleUpdate = (row) => {
  dialog.title = '修改角色'
  dialog.visible = true
  Object.assign(form, row)
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除角色 "${row.roleName}" 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    request.delete(`/system/role/${row.roleId}`).then(() => {
      ElMessage.success('删除成功')
      getRoleList()
    })
  }).catch(() => {})
}

// 批量删除
const handleBatchDelete = () => {
  if (!roleIds.value.length) {
    ElMessage.warning('请选择要删除的角色')
    return
  }
  ElMessageBox.confirm(`确认删除选中的 ${roleIds.value.length} 个角色吗？`, '警告', {
    type: 'warning'
  }).then(() => {
    request.delete('/system/role/batch', { data: roleIds.value }).then(() => {
      ElMessage.success('批量删除成功')
      getRoleList()
    })
  }).catch(() => {})
}

// 分配权限
const handleAuthMenu = (row) => {
  authDialog.visible = true
  currentRoleId.value = row.roleId
  checkedKeys.value = []
  // 获取角色的菜单权限
  request.get(`/system/role/menus/${row.roleId}`).then(res => {
    checkedKeys.value = res.data || []
  })
}

// 过滤节点
const filterNode = (value, data) => {
  if (!value) return true
  return data.menuName && data.menuName.toLowerCase().includes(value.toLowerCase())
}

// 展开/收起
const handleExpandChange = () => {
  const nodes = menuTreeRef.value.store.nodesMap
  for (const node in nodes) {
    nodes[node].expanded = expandAll.value
  }
}

// 全选
const handleCheckAll = () => {
  const allKeys = getAllMenuIds(menuList.value)
  menuTreeRef.value.setCheckedKeys(allKeys)
}

// 取消全选
const handleUncheckAll = () => {
  menuTreeRef.value.setCheckedKeys([])
}

// 递归获取所有菜单 ID
const getAllMenuIds = (menus) => {
  const ids = []
  menus.forEach(menu => {
    ids.push(menu.menuId)
    if (menu.children && menu.children.length) {
      ids.push(...getAllMenuIds(menu.children))
    }
  })
  return ids
}

// 提交权限分配
const submitAuth = () => {
  const checkedKeys = menuTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
  const menuIds = [...checkedKeys, ...halfCheckedKeys]

  if (!menuIds.length) {
    ElMessageBox.confirm('确定不分配任何权限吗？', '提示', {
      type: 'warning'
    }).then(() => {
      doSubmitAuth(menuIds)
    })
    return
  }
  doSubmitAuth(menuIds)
}

const doSubmitAuth = (menuIds) => {
  submittingAuth.value = true
  request.post('/system/role/authMenu', {
    roleId: currentRoleId.value,
    menuIds
  }).then(() => {
    ElMessage.success('权限分配成功')
    authDialog.visible = false
  }).catch(() => {}).finally(() => {
    submittingAuth.value = false
  })
}

// 导出
const handleExport = () => {
  loading.value = true
  const params = {
    roleName: queryParams.roleName,
    roleCode: queryParams.roleCode,
    status: queryParams.status
  }
  request.get('/system/role/export', { params, responseType: 'blob' }).then(res => {
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '角色数据_' + new Date().getTime() + '.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  }).catch(() => {
    ElMessage.error('导出失败')
  }).finally(() => {
    loading.value = false
  })
}

// 提交表单
const submitForm = () => {
  if (!formRef.value) return
  
  formRef.value.validate(valid => {
    if (valid) {
      submitting.value = true
      const api = form.roleId ? request.put : request.post
      api('/system/role', form).then(() => {
        ElMessage.success('操作成功')
        dialog.visible = false
        getRoleList()
      }).catch(() => {}).finally(() => {
        submitting.value = false
      })
    }
  })
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    roleId: null,
    roleName: '',
    roleCode: '',
    dataScope: 1,
    sort: 0,
    status: 1,
    description: ''
  })
}

onMounted(() => {
  getRoleList()
  getMenuList()
})
</script>

<style lang="scss" scoped>
.role-container {
  padding: 16px;

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

    .header-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 12px;

      .card-title {
        font-size: 16px;
        font-weight: 500;
        color: #303133;
      }

      .card-buttons {
        display: flex;
        gap: 12px;
      }
    }

    .pagination {
      position: relative;
      z-index: 10;
      display: flex;
      justify-content: flex-end;
      align-items: center;
      gap: 16px;
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #e3e4e6;

      .pagination-info {
        font-size: 13px;
        color: #606266;
        white-space: nowrap;
      }
    }
  }
}
</style>
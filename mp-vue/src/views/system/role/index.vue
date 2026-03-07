<template>
  <div class="role-container">
    <el-card>
      <el-form :model="queryParams" inline>
        <el-form-item label="角色名称">
          <el-input v-model="queryParams.roleName" placeholder="请输入角色名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="queryParams.roleCode" placeholder="请输入角色编码" clearable @keyup.enter="handleQuery" />
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
          <el-button type="primary" icon="Plus" @click="handleAdd">新增角色</el-button>
          <el-button type="danger" icon="Delete" :disabled="multiple" @click="handleBatchDelete">批量删除</el-button>
          <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
        </div>
      </template>

      <el-table :data="roleList" border stripe v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="roleId" label="角色 ID" width="100" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
            <el-button link type="warning" icon="Setting" @click="handleAuthMenu(row)">分配权限</el-button>
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
          @size-change="getRoleList"
          @current-change="getRoleList"
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
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="数据范围" prop="dataScope">
          <el-select v-model="form.dataScope" placeholder="请选择数据范围">
            <el-option label="全部数据" :value="1" />
            <el-option label="本部门及以下" :value="2" />
            <el-option label="本部门" :value="3" />
            <el-option label="仅本人" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog title="分配权限" v-model="authDialog.visible" width="500px">
      <el-tree
        ref="menuTreeRef"
        :data="menuList"
        :props="{ children: 'children', label: 'menuName' }"
        show-checkbox
        node-key="menuId"
        :default-checked-keys="checkedKeys"
      />
      <template #footer>
        <el-button @click="authDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitAuth">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const roleList = ref([])
const menuList = ref([])
const loading = ref(false)
const multiple = ref(true)
const roleIds = ref([])

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
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

// 获取角色列表
const getRoleList = () => {
  loading.value = true
  request.get('/system/role/list', { params: queryParams }).then(res => {
    roleList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
    loading.value = false
  }).catch(() => {
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

// 获取菜单列表
const getMenuList = () => {
  request.get('/system/menu/list').then(res => {
    menuList.value = res.data
  })
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  roleIds.value = selection.map(item => item.roleId)
  multiple.value = !selection.length
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
  })
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确认删除选中的 ${roleIds.value.length} 个角色吗？`, '警告', {
    type: 'warning'
  }).then(() => {
    request.delete('/system/role/batch', { data: roleIds.value }).then(() => {
      ElMessage.success('批量删除成功')
      getRoleList()
    })
  })
}

// 分配权限
const handleAuthMenu = (row) => {
  authDialog.visible = true
  currentRoleId.value = row.roleId
  // 获取角色的菜单权限
  request.get(`/system/role/menus/${row.roleId}`).then(res => {
    checkedKeys.value = res.data || []
  })
}

// 提交权限分配
const submitAuth = () => {
  const checkedKeys = menuTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
  const menuIds = [...checkedKeys, ...halfCheckedKeys]

  request.post('/system/role/authMenu', {
    roleId: currentRoleId.value,
    menuIds
  }).then(() => {
    ElMessage.success('权限分配成功')
    authDialog.visible = false
  })
}

// 导出
const handleExport = () => {
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
  })
}

// 提交表单
const submitForm = () => {
  formRef.value.validate(valid => {
    if (valid) {
      const api = form.roleId ? request.put : request.post
      api('/system/role', form).then(() => {
        ElMessage.success('操作成功')
        dialog.visible = false
        getRoleList()
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
  .table-card {
    margin-top: 20px;

    .header-actions {
      display: flex;
      gap: 10px;
      margin-bottom: 10px;
    }

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
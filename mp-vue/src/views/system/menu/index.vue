<template>
  <div class="menu-container">
    <el-card shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="菜单名称">
          <el-input v-model="queryParams.menuName" placeholder="请输入菜单名称" clearable @keyup.enter="handleQuery" style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="header-actions">
          <span class="card-title">菜单列表</span>
          <div class="card-buttons">
            <el-button type="primary" @click="handleAdd(null)">新增菜单</el-button>
            <el-button @click="expandAll">展开</el-button>
            <el-button @click="collapseAll">收起</el-button>
            <el-button @click="handleExport">导出</el-button>
          </div>
        </div>
      </template>

      <el-table
        ref="menuTableRef"
        :data="menuList"
        row-key="menuId"
        :default-expand-all="defaultExpandAll"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        border
        v-loading="loading"
        :cell-style="{ padding: '8px 0' }"
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon" :size="18"><component :is="row.icon" /></el-icon>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column prop="type" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 1" type="warning">目录</el-tag>
            <el-tag v-else-if="row.type === 2" type="success">菜单</el-tag>
            <el-tag v-else type="info">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="permission" label="权限标识" min-width="180" show-overflow-tooltip />
        <el-table-column prop="visible" label="显示" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.visible === 1 ? 'success' : 'info'" size="small">
              {{ row.visible === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
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
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleUpdate(row)">修改</el-button>
            <el-button link type="primary" size="small" @click="handleAdd(row)">新增</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="600px"
      @close="resetForm"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="form.type" @change="handleTypeChange">
            <el-radio :label="1">目录</el-radio>
            <el-radio :label="2">菜单</el-radio>
            <el-radio :label="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuList"
            :props="{ value: 'menuId', label: 'menuName', children: 'children' }"
            check-strictly
            placeholder="请选择上级菜单"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" maxlength="30" />
        </el-form-item>
        <el-form-item label="路由地址" prop="path" v-if="form.type !== 3">
          <el-input v-model="form.path" placeholder="请输入路由地址，如：/system/user" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component" v-if="form.type === 2">
          <el-input v-model="form.component" placeholder="请输入组件路径，如：system/user/index" />
          <div class="form-tip">注意：不需要写 .vue 后缀</div>
        </el-form-item>
        <el-form-item label="权限标识" prop="permission" v-if="form.type === 3">
          <el-input v-model="form.permission" placeholder="请输入权限标识，如：system:user:add" />
          <div class="form-tip">格式：模块：功能：操作</div>
        </el-form-item>
        <el-form-item label="图标" prop="icon" v-if="form.type === 1 || form.type === 2">
          <el-input v-model="form.icon" placeholder="请输入图标名称，如：User, Setting, Menu" clearable>
            <template #prefix>
              <el-icon v-if="form.icon"><component :is="form.icon" /></el-icon>
            </template>
          </el-input>
          <div class="form-tip">Element Plus 图标名称，首字母大写</div>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" style="width: 100px" />
          <span class="form-tip-inline">数值越小越靠前</span>
        </el-form-item>
        <el-form-item label="是否外链" prop="isFrame">
          <el-radio-group v-model="form.isFrame">
            <el-radio :label="0">是</el-radio>
            <el-radio :label="1">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否缓存" prop="isCache">
          <el-radio-group v-model="form.isCache">
            <el-radio :label="0">缓存</el-radio>
            <el-radio :label="1">不缓存</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否显示" prop="visible">
          <el-radio-group v-model="form.visible">
            <el-radio :label="1">显示</el-radio>
            <el-radio :label="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SysMenu">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const menuTableRef = ref(null)
const menuList = ref([])
const loading = ref(false)
const submitting = ref(false)
const defaultExpandAll = ref(true)
const treeExpandAll = ref(true)

const queryParams = reactive({
  menuName: '',
  status: null
})

const dialog = reactive({
  visible: false,
  title: ''
})

const formRef = ref(null)

const form = reactive({
  menuId: null,
  parentId: null,
  menuName: '',
  path: '',
  component: '',
  permission: '',
  icon: '',
  type: 2,
  sort: 0,
  isFrame: 1,
  isCache: 1,
  visible: 1,
  status: 1,
  remark: ''
})

const rules = {
  menuName: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  path: [{ required: true, message: '请输入路由地址', trigger: 'blur' }],
  component: [{ required: true, message: '请输入组件路径', trigger: 'blur' }],
  permission: [
    { required: true, message: '请输入权限标识', trigger: 'blur' },
    { pattern: /^[a-z]+:[a-z]+:[a-z]+$/, message: '格式：system:user:add', trigger: 'blur' }
  ]
}

// 获取菜单列表
const getMenuList = () => {
  loading.value = true
  request.get('/system/menu/list', { params: queryParams }).then(res => {
    menuList.value = handleTreeData(res.data || [])
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// 树形数据转换
const handleTreeData = (data, parentId = 0) => {
  const result = []
  data.forEach(item => {
    if (item.parentId === parentId) {
      const children = handleTreeData(data, item.menuId)
      if (children.length > 0) {
        item.children = children
      }
      result.push(item)
    }
  })
  return result
}

// 查询
const handleQuery = () => {
  getMenuList()
}

// 重置
const resetQuery = () => {
  queryParams.menuName = ''
  queryParams.status = null
  getMenuList()
}

// 展开所有
const expandAll = () => {
  treeExpandAll.value = true
  // 通过 row-key 展开所有节点
  if (menuTableRef.value) {
    menuTableRef.value.toggleAllExpansion()
  }
}

// 收起所有
const collapseAll = () => {
  treeExpandAll.value = false
  if (menuTableRef.value) {
    menuTableRef.value.clearSelection()
  }
  // 重新渲染表格以收起所有节点
  const temp = menuList.value
  menuList.value = []
  setTimeout(() => {
    menuList.value = temp
  }, 0)
}

// 类型变更
const handleTypeChange = () => {
  // 根据类型设置默认值
  if (form.type === 3) {
    form.path = ''
    form.component = ''
  } else if (form.type === 2) {
    form.permission = ''
  } else {
    form.path = ''
    form.component = ''
    form.permission = ''
  }
}

// 状态变更
const handleStatusChange = (row) => {
  const text = row.status === 1 ? '启用' : '禁用'
  ElMessageBox.confirm(`确认要${text}菜单"${row.menuName}"吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    request.put('/system/menu', { menuId: row.menuId, status: row.status }).then(() => {
      ElMessage.success(`${text}成功`)
    }).catch(() => {
      row.status = row.status === 1 ? 0 : 1
    })
  }).catch(() => {
    row.status = row.status === 1 ? 0 : 1
  })
}

// 新增
const handleAdd = (row) => {
  resetForm()
  dialog.title = '新增菜单'
  dialog.visible = true
  if (row) {
    form.parentId = row.menuId
    form.type = row.type === 1 ? 2 : 3
  }
}

// 修改
const handleUpdate = (row) => {
  dialog.title = '修改菜单'
  dialog.visible = true
  Object.assign(form, row)
}

// 删除
const handleDelete = (row) => {
  // 检查是否有子菜单
  const hasChildren = menuList.value.some(item => item.parentId === row.menuId)
  if (hasChildren) {
    ElMessage.warning('该菜单下存在子菜单，无法删除')
    return
  }

  ElMessageBox.confirm(`确认删除菜单 "${row.menuName}" 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    request.delete(`/system/menu/${row.menuId}`).then(() => {
      ElMessage.success('删除成功')
      getMenuList()
    })
  }).catch(() => {})
}

// 提交表单
const submitForm = () => {
  if (!formRef.value) return

  formRef.value.validate(valid => {
    if (valid) {
      submitting.value = true
      const api = form.menuId ? request.put : request.post
      api('/system/menu', form).then(() => {
        ElMessage.success('操作成功')
        dialog.visible = false
        getMenuList()
      }).catch(() => {}).finally(() => {
        submitting.value = false
      })
    }
  })
}

// 导出
const handleExport = () => {
  loading.value = true
  const params = {
    menuName: queryParams.menuName,
    status: queryParams.status
  }
  request.get('/system/menu/export', { params, responseType: 'blob' }).then(res => {
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '菜单数据_' + new Date().getTime() + '.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  }).catch(() => {
    ElMessage.error('导出失败')
  }).finally(() => {
    loading.value = false
  })
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    menuId: null,
    parentId: null,
    menuName: '',
    path: '',
    component: '',
    permission: '',
    icon: '',
    type: 2,
    sort: 0,
    isFrame: 1,
    isCache: 1,
    visible: 1,
    status: 1,
    remark: ''
  })
}

onMounted(() => {
  getMenuList()
})
</script>

<style lang="scss" scoped>
.menu-container {
  padding: 16px;

  .table-card {
    margin-top: 16px;

    .header-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;

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
  }

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
    line-height: 1.5;
  }

  .form-tip-inline {
    font-size: 12px;
    color: #909399;
    margin-left: 8px;
  }
}
</style>
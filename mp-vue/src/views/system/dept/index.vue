<template>
  <div class="dept-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="部门名称">
          <el-input
            v-model="queryParams.deptName"
            placeholder="请输入部门名称"
            clearable
            @keyup.enter="handleQuery"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格卡片 -->
    <el-card class="table-card">
      <template #header>
        <div class="header-actions">
          <el-button type="primary" icon="Plus" @click="handleAdd()">新增部门</el-button>
          <el-divider direction="vertical" />
          <el-button icon="Expand" @click="expandAll">展开</el-button>
          <el-button icon="Fold" @click="collapseAll">收起</el-button>
          <el-divider direction="vertical" />
          <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
        </div>
      </template>

      <el-table
        ref="deptTableRef"
        :data="deptList"
        v-loading="loading"
        row-key="deptId"
        :default-expand-all="defaultExpandAll"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        border
        stripe
        :indent="24"
      >
        <el-table-column prop="deptName" label="部门名称" min-width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="dept-name">{{ row.deptName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="leader" label="负责人" width="120" align="center" show-overflow-tooltip />
        <el-table-column prop="phone" label="联系电话" width="150" align="center" />
        <el-table-column prop="email" label="邮箱" width="180" align="center" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="plain">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
            <el-button link type="primary" icon="Plus" @click="handleAdd(row)">新增</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
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
      @open="handleDialogOpen"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="deptTreeData"
            :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
            check-strictly
            placeholder="请选择上级部门"
            :default-expand-all="true"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="显示排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :step="1" style="width: 200px" />
          <span class="form-tip">数值越小越靠前</span>
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="请输入负责人" maxlength="20" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="20" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">
              <el-icon><CircleCheck /></el-icon>
              正常
            </el-radio>
            <el-radio :label="0">
              <el-icon><CircleClose /></el-icon>
              停用
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck, CircleClose } from '@element-plus/icons-vue'
import request from '@/utils/request'

const deptList = ref([])
const deptTreeData = ref([])
const loading = ref(false)
const submitLoading = ref(false)

const queryParams = reactive({
  deptName: '',
  status: null
})

const dialog = reactive({
  visible: false,
  title: '',
  type: 'add'
})

const formRef = ref(null)
const deptTableRef = ref(null)
const defaultExpandAll = ref(true)

const form = reactive({
  deptId: null,
  parentId: null,
  deptName: '',
  sort: 0,
  leader: '',
  phone: '',
  email: '',
  status: 1
})

const rules = {
  deptName: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 50, message: '部门名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 将平铺数据转换为树形结构
const transformTreeData = (data, parentId = 0) => {
  if (!data || data.length === 0) return []
  
  return data
    .filter(item => item.parentId === parentId)
    .map(item => ({
      ...item,
      children: transformTreeData(data, item.deptId)
    }))
}

// 获取部门列表
const getDeptList = () => {
  loading.value = true
  request.get('/system/dept/list', { params: queryParams }).then(res => {
    const rawData = res.data || []
    // 转换为树形结构
    deptList.value = transformTreeData(rawData)
    deptTreeData.value = deptList.value
    loading.value = false
  }).catch(() => {
    loading.value = false
    ElMessage.error('获取部门列表失败')
  })
}

// 查询
const handleQuery = () => {
  getDeptList()
}

// 重置
const resetQuery = () => {
  queryParams.deptName = ''
  queryParams.status = null
  getDeptList()
}

// 新增
const handleAdd = (row) => {
  dialog.type = 'add'
  dialog.title = '新增部门'
  dialog.visible = true
  form.parentId = null
  form.deptId = null
  if (row) {
    form.parentId = row.deptId
  }
}

// 修改
const handleUpdate = (row) => {
  dialog.type = 'edit'
  dialog.title = '修改部门'
  dialog.visible = true
  Object.assign(form, row)
}

// 对话框打开时
const handleDialogOpen = () => {
  if (dialog.type === 'add') {
    form.deptId = null
    form.deptName = ''
    form.sort = 0
    form.leader = ''
    form.phone = ''
    form.email = ''
    form.status = 1
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除部门 "${row.deptName}" 吗？`, '提示', {
    type: 'warning',
    distinguishCancelAndClose: true,
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    request.delete(`/system/dept/${row.deptId}`).then(() => {
      ElMessage.success('删除成功')
      getDeptList()
    }).catch(() => {
      ElMessage.error('删除失败')
    })
  }).catch(() => {})
}

// 提交表单
const submitForm = () => {
  formRef.value.validate(valid => {
    if (valid) {
      submitLoading.value = true
      const api = form.deptId ? request.put : request.post
      api('/system/dept', form).then(() => {
        ElMessage.success('操作成功')
        dialog.visible = false
        getDeptList()
      }).catch(() => {
        ElMessage.error('操作失败')
      }).finally(() => {
        submitLoading.value = false
      })
    }
  })
}

// 展开所有
const expandAll = () => {
  defaultExpandAll.value = true
}

// 收起所有
const collapseAll = () => {
  defaultExpandAll.value = false
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    deptId: null,
    parentId: null,
    deptName: '',
    sort: 0,
    leader: '',
    phone: '',
    email: '',
    status: 1
  })
}

// 导出
const handleExport = () => {
  const params = {
    deptName: queryParams.deptName,
    status: queryParams.status
  }
  request.get('/system/dept/export', { params, responseType: 'blob' }).then(res => {
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '部门数据_' + new Date().getTime() + '.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  }).catch(() => {
    ElMessage.error('导出失败')
  })
}

onMounted(() => {
  getDeptList()
})
</script>

<style lang="scss" scoped>
$primary-color: #1e80ff;
$text-primary: #333333;
$text-regular: #666666;
$text-secondary: #999999;
$border-color: #e3e4e6;
$bg-color-page: #f5f6f7;

.dept-container {
  padding: 0;
  background: $bg-color-page;
  
  .search-card {
    margin-bottom: 16px;
    border: none;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
    
    :deep(.el-card__body) {
      padding: 16px;
    }
    
    .el-form-item {
      margin-bottom: 0;
    }
  }
  
  .table-card {
    border: none;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
    
    :deep(.el-card__header) {
      padding: 12px 16px;
      border-bottom: 1px solid $border-color;
    }
    
    .header-actions {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 8px;
    }
  }
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: $text-secondary;
}

.dept-name {
  font-weight: 500;
  color: $text-primary;
}

// 对话框样式优化
:deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
  
  .el-dialog__header {
    padding: 16px 20px;
    border-bottom: 1px solid $border-color;
    margin-right: 0;
  }
  
  .el-dialog__title {
    font-size: 16px;
    font-weight: 600;
  }
  
  .el-dialog__body {
    padding: 24px 20px;
  }
  
  .el-dialog__footer {
    padding: 12px 20px;
    border-top: 1px solid $border-color;
  }
}

// 表格样式优化
:deep(.el-table) {
  --el-table-header-bg-color: #fafafa;
  --el-table-row-hover-bg-color: #f5f7fa;
  
  th.el-table__cell {
    background: #fafafa;
    font-weight: 600;
    color: $text-primary;
  }
  
  .el-table__body tr:hover > td {
    background: #f5f7fa;
  }
  
  .cell {
    padding: 0 10px;
  }
  
  // 展开图标样式
  .el-table__expand-icon {
    color: $text-secondary;
    transition: transform 0.2s ease;
    
    &.el-table__expand-icon--expanded {
      color: $primary-color;
    }
  }
}
</style>

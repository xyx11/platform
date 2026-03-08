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
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" :loading="resetting" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格卡片 -->
    <el-card class="table-card">
      <template #header>
        <div class="header-top">
          <div class="header-actions">
            <el-button type="primary" icon="Plus" @click="handleAdd()">新增部门</el-button>
            <el-button icon="Refresh" circle @click="refresh" :loading="refreshing" />
            <el-divider direction="vertical" />
            <el-dropdown trigger="click" @command="handleExpandCommand">
              <el-button icon="List">展开/收起</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="expand"><el-icon icon="Expand" /> 展开全部</el-dropdown-item>
                  <el-dropdown-item command="collapse"><el-icon icon="Fold" /> 收起全部</el-dropdown-item>
                  <el-dropdown-item command="expand-first"><el-icon icon="DArrowRight" /> 展开一级</el-dropdown-item>
                  <el-dropdown-item command="collapse-first"><el-icon icon="DArrowLeft" /> 收起一级</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-divider direction="vertical" />
            <el-dropdown trigger="click" @command="handleBatchCommand">
              <el-button icon="Grid" :disabled="selectedRows.length === 0">批量操作</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="export-selected"><el-icon icon="Download" /> 导出选中</el-dropdown-item>
                  <el-dropdown-item command="delete-selected" divided style="color: #f56c6c">
                    <el-icon icon="Delete" /> 批量删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button type="success" icon="Download" @click="handleExport">全部导出</el-button>
            
            <!-- 表格设置 -->
            <el-dropdown trigger="click" class="table-setting">
              <el-button icon="Setting" circle />
              <template #dropdown>
                <el-dropdown-menu>
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
              <el-icon class="stat-icon" style="color: #1e80ff"><OfficeBuilding /></el-icon>
              <div class="stat-content">
                <span class="stat-label">部门总数</span>
                <span class="stat-value">{{ deptCount }}</span>
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
                <span class="stat-label">停用</span>
                <span class="stat-value" style="color: #f56c6c">{{ disabledCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 选中提示 -->
      <el-alert
        v-if="selectedRows.length > 0"
        type="info"
        :closable="true"
        show-icon
        class="selection-alert"
      >
        <span>已选择 <strong>{{ selectedRows.length }}</strong> 项</span>
        <el-button link type="primary" @click="clearSelection">清空选择</el-button>
      </el-alert>

      <el-table :key="tableKey"
        ref="deptTableRef"
        :data="deptList"
        v-loading="loading"
        element-loading-text="加载中..."
        element-loading-spinner="el-icon-loading"
        row-key="deptId"
        :default-expand-all="true"
        :tree-props="{ children: 'children' }"
        border
        :stripe="true"
        :indent="24"
        :highlight-current-row="true"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
        @row-dblclick="handleRowDblClick"
      >
        <el-table-column type="selection" width="55" align="center" fixed />
        <el-table-column 
          v-if="columns.find(c => c.prop === 'deptName')?.visible" 
          prop="deptName" 
          label="部门名称" 
          min-width="250" 
          show-overflow-tooltip
          :resizable="true"
        >
          <template #default="{ row }">
            <span class="dept-name">
              <el-icon v-if="row.children && row.children.length > 0" class="dept-has-children"><FolderOpened /></el-icon>
              <el-icon v-else class="dept-no-children"><Folder /></el-icon>
              {{ row.deptName }}
            </span>
            <el-tag v-if="row.parentId === '0'" size="small" type="info" class="root-tag">根部门</el-tag>
          </template>
        </el-table-column>
        <el-table-column 
          v-if="columns.find(c => c.prop === 'sort')?.visible" 
          prop="sort" 
          label="排序" 
          width="80" 
          align="center"
          sortable
        />
        <el-table-column 
          v-if="columns.find(c => c.prop === 'leader')?.visible" 
          prop="leader" 
          label="负责人" 
          width="120" 
          align="center" 
          show-overflow-tooltip
        />
        <el-table-column 
          v-if="columns.find(c => c.prop === 'phone')?.visible" 
          prop="phone" 
          label="联系电话" 
          width="150" 
          align="center"
        >
          <template #default="{ row }">
            <el-tooltip content="点击复制" placement="top">
              <span class="clickable" @click="copyToClipboard(row.phone, '电话号码')">{{ row.phone || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column 
          v-if="columns.find(c => c.prop === 'email')?.visible" 
          prop="email" 
          label="邮箱" 
          width="180" 
          align="center" 
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <el-tooltip content="点击复制" placement="top">
              <span class="clickable" @click="copyToClipboard(row.email, '邮箱')">{{ row.email || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
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
              inline-prompt
              active-text="正常"
              inactive-text="停用"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column 
          v-if="columns.find(c => c.prop === 'createTime')?.visible" 
          prop="createTime" 
          label="创建时间" 
          width="180" 
          align="center"
          sortable
        />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" @click.stop="handleUpdate(row)">修改</el-button>
            <el-button link type="primary" icon="Plus" @click.stop="handleAdd(row)">新增</el-button>
            <el-button 
              link 
              type="danger" 
              icon="Delete" 
              @click.stop="handleDelete(row)"
              :disabled="row.children && row.children.length > 0"
            >
              删除
            </el-button>
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
      :close-on-click-modal="false"
      top="8vh"
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
            :filterable="true"
            :clearable="true"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input 
            v-model="form.deptName" 
            placeholder="请输入部门名称" 
            maxlength="50" 
            show-word-limit 
            clearable
            ref="deptNameInputRef"
          >
            <template #prefix>
              <el-icon><OfficeBuilding /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="显示排序" prop="sort">
          <el-input-number 
            v-model="form.sort" 
            :min="0" 
            :step="1" 
            style="width: 150px" 
            controls-position="right"
          />
          <span class="form-tip">数值越小越靠前</span>
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="请输入负责人" maxlength="20" clearable>
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="20" clearable>
            <template #prefix>
              <el-icon><Phone /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" clearable>
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
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
        <el-button type="primary" @click="submitForm" :loading="submitLoading">
          <el-icon v-if="submitLoading" class="is-loading"><Loading /></el-icon>
          {{ submitLoading ? '提交中...' : '确定' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 快速提示 -->
    <el-backtop target=".dept-container" :bottom="100">
      <el-tooltip content="返回顶部" placement="top">
        <el-icon><Top /></el-icon>
      </el-tooltip>
    </el-backtop>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, Plus, List, Expand, Fold, DArrowRight, DArrowLeft,
  Grid, Download, Setting, OfficeBuilding, CircleCheck, CircleClose,
  FolderOpened, Folder, User, Phone, Message, Loading, Top, DocumentCopy,
  Delete
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const deptList = ref([])
const tableKey = ref(0)
const deptTreeData = ref([])
const loading = ref(false)
const refreshing = ref(false)
const resetting = ref(false)
const submitLoading = ref(false)
const selectedRows = ref([])
const dateRange = ref([])

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
const deptNameInputRef = ref(null)
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

// 表格列配置
const columns = reactive([
  { prop: 'deptName', label: '部门名称', visible: true, disabled: true },
  { prop: 'sort', label: '排序', visible: true, disabled: false },
  { prop: 'leader', label: '负责人', visible: true, disabled: false },
  { prop: 'phone', label: '联系电话', visible: true, disabled: false },
  { prop: 'email', label: '邮箱', visible: true, disabled: false },
  { prop: 'status', label: '状态', visible: true, disabled: false },
  { prop: 'createTime', label: '创建时间', visible: true, disabled: false }
])

const rules = {
  deptName: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 50, message: '部门名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 统计信息
const deptCount = computed(() => countNodes(deptList.value))
const normalCount = computed(() => countStatus(deptList.value, 1))
const disabledCount = computed(() => countStatus(deptList.value, 0))

const countNodes = (data) => {
  if (!data || data.length === 0) return 0
  return data.reduce((acc, item) => acc + 1 + countNodes(item.children), 0)
}

const countStatus = (data, status) => {
  if (!data || data.length === 0) return 0
  return data.reduce((acc, item) => {
    return acc + (item.status === status ? 1 : 0) + countStatus(item.children, status)
  }, 0)
}

// 获取部门列表
const getDeptList = () => {
  loading.value = true
  const params = { ...queryParams }
  if (dateRange.value && dateRange.value.length === 2) {
    params.startTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }

  request.get('/system/dept/list', { params }).then(res => {
    const rawData = res.data || res || []
    console.log('原始数据:', rawData)
    // 使用 JSON 序列化再反序列化，确保响应式
    const treeData = JSON.parse(JSON.stringify(rawData))
    console.log('树形数据:', treeData)
    tableKey.value++
    nextTick(() => {
      deptList.value = treeData
      window.debugDeptList = treeData
      deptTreeData.value = treeData
      loading.value = false
    })
  }).catch(err => {
    loading.value = false
    console.error('获取部门列表失败:', err)
    ElMessage.error('获取部门列表失败')
  })
}

// 树形数据转换
const handleTreeData = (data, parentId = '0') => {
  const result = []
  data.forEach(item => {
    if (String(item.parentId) === String(parentId)) {
      const children = handleTreeData(data, String(item.deptId))
      if (children.length > 0) {
        item.children = children
      }
      result.push(item)
    }
  })
  return result
}

// 刷新
const refresh = () => {
  refreshing.value = true
  getDeptList()
  setTimeout(() => {
    refreshing.value = false
  }, 500)
}

// 查询
const handleQuery = () => {
  getDeptList()
}

// 重置
const resetQuery = () => {
  resetting.value = true
  queryParams.deptName = ''
  queryParams.status = null
  dateRange.value = []
  getDeptList()
  setTimeout(() => {
    resetting.value = false
  }, 300)
}

// 展开收起命令
const handleExpandCommand = (command) => {
  switch (command) {
    case 'expand':
      defaultExpandAll.value = true
      ElMessage.success('展开全部')
      break
    case 'collapse':
      defaultExpandAll.value = false
      ElMessage.success('收起全部')
      break
    case 'expand-first':
      defaultExpandAll.value = false
      nextTick(() => {
        deptList.value.forEach(item => {
          deptTableRef.value?.toggleRowExpansion(item, true)
        })
        ElMessage.success('展开一级')
      })
      break
    case 'collapse-first':
      deptList.value.forEach(item => {
        deptTableRef.value?.toggleRowExpansion(item, false)
      })
      ElMessage.success('收起一级')
      break
  }
}

// 批量操作命令
const handleBatchCommand = (command) => {
  switch (command) {
    case 'export-selected':
      exportSelected()
      break
    case 'delete-selected':
      handleBatchDelete()
      break
  }
}

// 导出选中
const exportSelected = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要导出的部门')
    return
  }

  const deptIds = selectedRows.value.map(item => item.deptId)
  request.post('/system/dept/export/batch', deptIds, { responseType: 'blob' }).then(res => {
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '部门数据_' + new Date().getTime() + '.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success(`导出 ${selectedRows.value.length} 项成功`)
  }).catch(() => {
    ElMessage.error('导出失败')
  })
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的部门')
    return
  }

  // 检查是否有子部门的项
  const hasChildren = selectedRows.value.some(item => item.children && item.children.length > 0)
  if (hasChildren) {
    ElMessage.warning('选中的部门中存在子部门，无法批量删除')
    return
  }

  const deptNames = selectedRows.value.map(item => item.deptName).join(',')
  ElMessageBox.confirm(
    `确认删除选中的 ${selectedRows.value.length} 个部门吗？\n\n${deptNames}`,
    '警告',
    {
      type: 'warning',
      distinguishCancelAndClose: true,
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      closeOnClickModal: false
    }
  ).then(() => {
    const deptIds = selectedRows.value.map(item => item.deptId)
    request.delete('/system/dept/batch', { data: deptIds }).then(() => {
      ElMessage.success('批量删除成功')
      clearSelection()
      getDeptList()
    }).catch(() => {
      ElMessage.error('批量删除失败')
    })
  }).catch(() => {})
}

// 切换列显示
const toggleColumn = (prop) => {
  ElMessage.success('列显示已更新')
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 清空选择
const clearSelection = () => {
  deptTableRef.value?.clearSelection()
}

// 行点击
const handleRowClick = (row, column) => {
  if (column.property && column.property !== 'deptName') {
    deptTableRef.value?.toggleRowExpansion(row)
  }
}

// 行双击
const handleRowDblClick = (row) => {
  // 双击行可以查看详情或快速编辑
  ElMessage.info(`双击了：${row.deptName}`)
}

// 状态变化
const handleStatusChange = (row) => {
  const statusText = row.status === 1 ? '正常' : '停用'
  ElMessageBox.confirm(
    `确认${statusText === '正常' ? '启用' : '停用'}部门 "${row.deptName}" 吗？`,
    '提示',
    { type: 'warning' }
  ).then(() => {
    request.put('/system/dept/status', { deptId: row.deptId, status: row.status }).then(() => {
      ElMessage.success(`${statusText === '正常' ? '启用' : '停用'}成功`)
      getDeptList()
    }).catch(() => {
      row.status = row.status === 1 ? 0 : 1
      ElMessage.error('操作失败')
    })
  }).catch(() => {
    row.status = row.status === 1 ? 0 : 1
  })
}

// 复制
const copyToClipboard = (text, label) => {
  if (!text || text === '-') {
    ElMessage.warning(`${label}为空`)
    return
  }
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success(`${label}已复制`)
  }).catch(() => {
    ElMessage.error('复制失败')
  })
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
  nextTick(() => {
    deptNameInputRef.value?.focus()
  })
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
  nextTick(() => {
    deptNameInputRef.value?.focus()
  })
}

// 删除
const handleDelete = (row) => {
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该部门下存在子部门，无法删除')
    return
  }
  
  ElMessageBox.confirm(`确认删除部门 "${row.deptName}" 吗？`, '警告', {
    type: 'warning',
    distinguishCancelAndClose: true,
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    closeOnClickModal: false
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

// 快捷键
onMounted(() => {
  getDeptList()
  
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && dialog.visible) {
      dialog.visible = false
    }
    if ((e.ctrlKey || e.metaKey) && e.key === 'n' && !dialog.visible) {
      e.preventDefault()
      handleAdd()
    }
    if ((e.ctrlKey || e.metaKey) && e.key === 'f' && !dialog.visible) {
      e.preventDefault()
    }
    if ((e.ctrlKey || e.metaKey) && e.key === 'r' && !dialog.visible) {
      e.preventDefault()
      refresh()
    }
  })
})
</script>

<style lang="scss" scoped>
$primary-color: #1e80ff;
$success-color: #67c23a;
$warning-color: #e6a23c;
$danger-color: #f56c6c;
$info-color: #909399;
$text-primary: #333333;
$text-regular: #666666;
$text-secondary: #999999;
$border-color: #e3e4e6;
$bg-color-page: #f5f6f7;

.dept-container {
  padding: 0;
  background: $bg-color-page;
  min-height: 100vh;
  
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
      padding: 16px;
      border-bottom: 1px solid $border-color;
    }
    
    .header-top {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
    
    .header-actions {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 8px;
      
      .table-setting {
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
  }
  
  .selection-alert {
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    strong {
      color: $primary-color;
      font-size: 16px;
    }
  }
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: $text-secondary;
}

.dept-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  color: $text-primary;
  
  .dept-has-children {
    color: $warning-color;
  }
  
  .dept-no-children {
    color: $text-secondary;
  }
}

.root-tag {
  margin-left: 8px;
  transform: scale(0.85);
}

.clickable {
  cursor: pointer;
  color: $primary-color;
  
  &:hover {
    text-decoration: underline;
  }
}

.row-expand-info {
  padding: 12px 20px;
  background: #fafafa;
  
  p {
    margin: 4px 0;
    font-size: 13px;
    
    .info-label {
      color: $text-secondary;
      margin-right: 8px;
    }
  }
}

// 对话框样式优化
:deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
  
  .el-dialog__header {
    padding: 16px 20px;
    border-bottom: 1px solid $border-color;
    margin-right: 0;
    background: #fafafa;
  }
  
  .el-dialog__title {
    font-size: 16px;
    font-weight: 600;
  }
  
  .el-dialog__body {
    padding: 24px 20px;
    max-height: 65vh;
    overflow-y: auto;
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
  
  .el-table__expand-icon {
    color: $text-secondary;
    transition: transform 0.2s ease;
    cursor: pointer;
    
    &.el-table__expand-icon--expanded {
      color: $primary-color;
    }
    
    &:hover {
      color: $primary-color;
    }
  }
  
  .el-table__indent {
    border-right: 1px dashed $border-color;
  }
}

// 加载动画
.is-loading {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// 开关样式
:deep(.el-switch) {
  &.is-checked {
    --el-switch-on-color: #67c23a;
  }
  --el-switch-off-color: #f56c6c;
}
</style>

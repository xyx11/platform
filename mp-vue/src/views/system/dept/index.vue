<template>
  <div class="dept-container">
    <el-row :gutter="16" style="height: calc(100vh - 120px)">
      <!-- 左侧部门树 -->
      <el-col :span="5" class="dept-tree-col">
        <el-card shadow="never" class="dept-tree-card">
          <template #header>
            <div class="dept-tree-header">
              <span>
                <el-icon class="header-icon"><OfficeBuilding /></el-icon>
                组织机构
              </span>
              <div class="header-actions">
                <el-badge :value="deptCount" type="primary" class="dept-count-badge">
                  <el-button link type="primary" icon="Refresh" @click="refreshTree" />
                </el-badge>
              </div>
            </div>
          </template>
          
          <el-input
            v-model="treeSearchText"
            placeholder="搜索部门..."
            clearable
            prefix-icon="Search"
            class="tree-search"
            @input="handleTreeSearch"
          />
          
          <div class="tree-actions">
            <el-button link size="small" @click="expandTreeAll">展开全部</el-button>
            <el-button link size="small" @click="collapseTreeAll">收起全部</el-button>
          </div>
          
          <el-tree
            ref="deptTreeRef"
            :data="filteredTreeData"
            :props="{ id: 'deptId', label: 'deptName', children: 'children' }"
            node-key="deptId"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            highlight-current
            default-expand-all
            @node-click="handleNodeClick"
            class="dept-tree"
          >
            <template #default="{ data, node }">
              <span class="dept-tree-node">
                <el-icon class="tree-icon" :class="{ 'icon-disabled': data.status === 0 }">
                  <OfficeBuilding />
                </el-icon>
                <span class="node-label">{{ node.label }}</span>
                <el-tag v-if="data.status === 0" size="small" type="info" class="status-tag">停用</el-tag>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <!-- 右侧部门表格 -->
      <el-col :span="19">
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

        <el-card class="table-card">
          <template #header>
            <div class="header-actions">
              <el-button type="primary" icon="Plus" @click="handleAdd()">新增部门</el-button>
              <el-divider direction="vertical" />
              <el-button icon="Expand" @click="expandTableAll">展开</el-button>
              <el-button icon="Fold" @click="collapseTableAll">收起</el-button>
              <el-divider direction="vertical" />
              <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
            </div>
          </template>

          <el-table
            ref="deptTableRef"
            :data="tableDeptList"
            v-loading="loading"
            row-key="deptId"
            :default-expand-all="defaultExpandAll"
            :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
            border
            stripe
            :highlight-current-row="true"
          >
            <el-table-column prop="deptName" label="部门名称" min-width="200" show-overflow-tooltip />
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
                <el-button 
                  link 
                  type="primary" 
                  icon="Edit" 
                  @click="handleUpdate(row)"
                >修改</el-button>
                <el-button 
                  link 
                  type="primary" 
                  icon="Plus" 
                  @click="handleAdd(row)"
                >新增</el-button>
                <el-button 
                  link 
                  type="danger" 
                  icon="Delete" 
                  @click="handleDelete(row)"
                >删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

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
import { ref, reactive, onMounted, watch, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { OfficeBuilding, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import request from '@/utils/request'

const deptList = ref([])
const deptTreeData = ref([])
const filteredTreeData = ref([])
const tableDeptList = ref([])
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
const deptTreeRef = ref(null)
const defaultExpandAll = ref(true)
const treeSearchText = ref('')

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
  ],
  parentId: [
    { required: false, message: '请选择上级部门', trigger: 'change' }
  ]
}

// 部门数量统计
const deptCount = computed(() => {
  const countNodes = (data) => {
    if (!data || data.length === 0) return 0
    return data.reduce((acc, item) => {
      return acc + 1 + countNodes(item.children)
    }, 0)
  }
  return countNodes(deptTreeData.value)
})

// 监听搜索输入
watch(treeSearchText, (val) => {
  if (deptTreeRef.value) {
    deptTreeRef.value.filter(val)
  }
})

// 树过滤方法
const filterNode = (value, data) => {
  if (!value) return true
  return data.deptName.includes(value)
}

// 树搜索处理
const handleTreeSearch = () => {
  // 搜索时自动展开匹配的节点
  if (treeSearchText.value && deptTreeRef.value) {
    expandFilteredNodes()
  }
}

// 展开过滤后的节点
const expandFilteredNodes = () => {
  nextTick(() => {
    const expandNode = (data) => {
      if (!data || !Array.isArray(data)) return
      data.forEach(item => {
        if (item.deptName.includes(treeSearchText.value)) {
          deptTreeRef.value.expandNode(item)
          // 展开父节点
          const parent = deptTreeRef.value.getNode(item.deptId)?.parent
          if (parent && parent.level > 0) {
            deptTreeRef.value.expandNode(parent.data)
          }
        }
        expandNode(item.children)
      })
    }
    expandNode(deptTreeData.value)
  })
}

// 构建树形数据
const buildTreeData = (data) => {
  if (!data) return []
  return data.map(item => ({
    ...item,
    children: item.children ? buildTreeData(item.children) : []
  }))
}

// 获取部门列表
const getDeptList = () => {
  loading.value = true
  request.get('/system/dept/list', { params: queryParams }).then(res => {
    deptList.value = res.data || []
    deptTreeData.value = buildTreeData(deptList.value)
    filteredTreeData.value = deptTreeData.value
    filterDeptList()
    loading.value = false
  }).catch(() => {
    loading.value = false
    ElMessage.error('获取部门列表失败')
  })
}

// 根据左侧树选中状态筛选表格数据
const filterDeptList = () => {
  const currentKey = deptTreeRef.value?.getCurrentKey()
  if (currentKey) {
    const selectedDept = findDeptById(deptTreeData.value, currentKey)
    if (selectedDept) {
      tableDeptList.value = [selectedDept]
      return
    }
  }
  tableDeptList.value = deptList.value
}

// 递归查找部门
const findDeptById = (data, id) => {
  for (const item of data) {
    if (item.deptId === id) return item
    if (item.children) {
      const found = findDeptById(item.children, id)
      if (found) return found
    }
  }
  return null
}

// 刷新树
const refreshTree = () => {
  getDeptList()
  ElMessage.success('刷新成功')
}

// 部门树节点点击
const handleNodeClick = (data) => {
  filterDeptList()
}

// 展开全部
const expandTreeAll = () => {
  const expandAll = (data) => {
    if (!data || !Array.isArray(data)) return
    data.forEach(item => {
      deptTreeRef.value?.expandNode(item)
      expandAll(item.children)
    })
  }
  expandAll(deptTreeData.value)
}

// 收起全部
const collapseTreeAll = () => {
  const collapseAll = (data) => {
    if (!data || !Array.isArray(data)) return
    data.forEach(item => {
      const node = deptTreeRef.value?.getNode(item.deptId)
      if (node && node.expanded) {
        deptTreeRef.value?.collapseNode(item.deptId)
      }
      collapseAll(item.children)
    })
  }
  collapseAll(deptTreeData.value)
}

// 查询
const handleQuery = () => {
  getDeptList()
}

// 重置
const resetQuery = () => {
  queryParams.deptName = ''
  queryParams.status = null
  treeSearchText.value = ''
  if (deptTreeRef.value) {
    deptTreeRef.value.setCurrentKey(null)
  }
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

// 展开表格所有
const expandTableAll = () => {
  defaultExpandAll.value = true
  nextTick(() => {
    deptTableRef.value?.toggleAllSelection?.()
  })
}

// 收起表格所有
const collapseTableAll = () => {
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
  height: 100%;
  background: $bg-color-page;
  
  .dept-tree-col {
    height: 100%;
    
    .dept-tree-card {
      height: 100%;
      display: flex;
      flex-direction: column;
      border: none;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
      
      :deep(.el-card__header) {
        padding: 12px 16px;
        border-bottom: 1px solid $border-color;
      }
      
      .dept-tree-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        span {
          display: flex;
          align-items: center;
          gap: 8px;
          font-weight: 600;
          font-size: 14px;
          color: $text-primary;
          
          .header-icon {
            color: $primary-color;
            font-size: 16px;
          }
        }
        
        .header-actions {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .dept-count-badge {
            .el-button {
              padding: 4px 6px;
            }
          }
        }
      }
      
      .tree-search {
        margin: 12px 16px;
        
        :deep(.el-input__wrapper) {
          border-radius: 20px;
          box-shadow: 0 0 0 1px $border-color inset;
        }
      }
      
      .tree-actions {
        display: flex;
        gap: 8px;
        padding: 0 16px 12px;
        border-bottom: 1px solid $border-color;
        
        .el-button {
          font-size: 12px;
          color: $text-secondary;
          
          &:hover {
            color: $primary-color;
          }
        }
      }
      
      .dept-tree {
        flex: 1;
        overflow-y: auto;
        padding: 8px 0;
        
        .dept-tree-node {
          display: flex;
          align-items: center;
          gap: 6px;
          flex: 1;
          
          .tree-icon {
            color: $primary-color;
            font-size: 14px;
            
            &.icon-disabled {
              color: $info-color;
            }
          }
          
          .node-label {
            flex: 1;
          }
          
          .status-tag {
            transform: scale(0.85);
          }
        }
        
        :deep(.el-tree-node.is-current) > .el-tree-node__content {
          background: rgba(30, 128, 255, 0.1);
          border-radius: 4px;
          margin: 0 8px;
          
          .dept-tree-node {
            color: $primary-color;
            font-weight: 500;
            
            .tree-icon {
              color: $primary-color;
            }
          }
        }
        
        :deep(.el-tree-node__content) {
          height: 36px;
          border-radius: 4px;
          margin: 2px 0;
          
          &:hover {
            background: rgba(0, 0, 0, 0.04);
          }
        }
      }
    }
  }
  
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
    }
  }
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: $text-secondary;
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
}
</style>

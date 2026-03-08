<template>
  <div class="dept-container">
    <el-row :gutter="16" style="height: calc(100vh - 120px)">
      <!-- 左侧部门树 -->
      <el-col :span="5" class="dept-tree-col">
        <el-card shadow="never" class="dept-tree-card">
          <template #header>
            <div class="dept-tree-header">
              <span>组织机构</span>
              <el-button link type="primary" icon="Refresh" @click="refreshTree" />
            </div>
          </template>
          <el-input
            v-model="treeSearchText"
            placeholder="搜索部门"
            clearable
            prefix-icon="Search"
            class="tree-search"
          />
          <el-tree
            ref="deptTreeRef"
            :data="deptTreeData"
            :props="{ id: 'deptId', label: 'deptName', children: 'children' }"
            node-key="deptId"
            :expand-on-click-node="false"
            highlight-current
            default-expand-all
            @node-click="handleNodeClick"
            @node-expand="handleNodeExpand"
            @node-collapse="handleNodeCollapse"
            class="dept-tree"
          >
            <template #default="{ data }">
              <span class="dept-tree-node">
                <el-icon class="tree-icon"><OfficeBuilding /></el-icon>
                <span>{{ data.deptName }}</span>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <!-- 右侧部门表格 -->
      <el-col :span="19">
        <el-card>
          <el-form :model="queryParams" inline>
            <el-form-item label="部门名称">
              <el-input v-model="queryParams.deptName" placeholder="请输入部门名称" clearable @keyup.enter="handleQuery" />
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
              <el-button type="primary" icon="Plus" @click="handleAdd">新增部门</el-button>
              <el-button icon="Expand" @click="expandAll">展开</el-button>
              <el-button icon="Fold" @click="collapseAll">收起</el-button>
              <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
            </div>
          </template>

          <el-table
            ref="deptTableRef"
            :data="tableDeptList"
            row-key="deptId"
            :default-expand-all="defaultExpandAll"
            :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
            border
            stripe
          >
            <el-table-column prop="deptName" label="部门名称" min-width="200" />
            <el-table-column prop="sort" label="排序" width="80" />
            <el-table-column prop="leader" label="负责人" width="120" />
            <el-table-column prop="phone" label="联系电话" width="150" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
                <el-button link type="primary" icon="Plus" @click="handleAdd(row)">新增</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
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
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="form.parentId"
            :data="deptTreeData"
            :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
            check-strictly
            placeholder="请选择上级部门"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
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
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { OfficeBuilding } from '@element-plus/icons-vue'
import request from '@/utils/request'

const deptList = ref([])
const deptTreeData = ref([])
const tableDeptList = ref([])
const queryParams = reactive({
  deptName: '',
  status: null
})

const dialog = reactive({
  visible: false,
  title: ''
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
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

// 构建树形数据（用于左侧树）
const buildTreeData = (data) => {
  if (!data) return []
  return data.map(item => ({
    ...item,
    children: item.children ? buildTreeData(item.children) : []
  }))
}

// 获取部门列表
const getDeptList = () => {
  request.get('/system/dept/list', { params: queryParams }).then(res => {
    deptList.value = res.data || []
    deptTreeData.value = buildTreeData(deptList.value)
    filterDeptList()
  })
}

// 根据左侧树选中状态筛选表格数据
const filterDeptList = () => {
  const currentKey = deptTreeRef.value?.getCurrentKey()
  if (currentKey) {
    // 选中了某个部门，筛选该部门及其子部门的数据
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

// 部门树节点展开
const handleNodeExpand = (data) => {
  // 可选：记录展开状态
}

// 部门树节点收起
const handleNodeCollapse = (data) => {
  // 可选：记录收起状态
}

// 查询
const handleQuery = () => {
  getDeptList()
}

// 重置
const resetQuery = () => {
  queryParams.deptName = ''
  queryParams.status = null
  if (deptTreeRef.value) {
    deptTreeRef.value.setCurrentKey(null)
  }
  getDeptList()
}

// 新增
const handleAdd = (row) => {
  dialog.title = '新增部门'
  dialog.visible = true
  if (row) {
    form.parentId = row.deptId
  }
}

// 修改
const handleUpdate = (row) => {
  dialog.title = '修改部门'
  dialog.visible = true
  Object.assign(form, row)
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除部门 "${row.deptName}" 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    request.delete(`/system/dept/${row.deptId}`).then(() => {
      ElMessage.success('删除成功')
      getDeptList()
    })
  })
}

// 提交表单
const submitForm = () => {
  formRef.value.validate(valid => {
    if (valid) {
      const api = form.deptId ? request.put : request.post
      api('/system/dept', form).then(() => {
        ElMessage.success('操作成功')
        dialog.visible = false
        getDeptList()
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
$border-color: #e3e4e6;
$bg-color-page: #f5f6f7;

.dept-container {
  padding: 0;
  height: 100%;
  
  .dept-tree-col {
    height: 100%;
    
    .dept-tree-card {
      height: 100%;
      display: flex;
      flex-direction: column;
      
      .dept-tree-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        span {
          font-weight: 600;
          font-size: 14px;
          color: $text-primary;
        }
      }
      
      .tree-search {
        margin-bottom: 12px;
      }
      
      .dept-tree {
        flex: 1;
        overflow-y: auto;
        
        .dept-tree-node {
          display: flex;
          align-items: center;
          gap: 6px;
          
          .tree-icon {
            color: $primary-color;
            font-size: 14px;
          }
        }
        
        .is-current > .dept-tree-node {
          color: $primary-color;
          font-weight: 500;
          
          .tree-icon {
            color: $primary-color;
          }
        }
      }
    }
  }
  
  .header-actions {
    display: flex;
    gap: 10px;
  }

  .table-card {
    margin-top: 16px;
  }
}
</style>

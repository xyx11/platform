<template>
  <div class="dict-container">
    <!-- 字典类型管理 -->
    <el-card class="search-card">
      <el-form :model="typeQueryParams" inline>
        <el-form-item label="字典名称">
          <el-input v-model="typeQueryParams.dictName" placeholder="请输入字典名称" clearable @keyup.enter="handleTypeQuery" />
        </el-form-item>
        <el-form-item label="字典类型">
          <el-input v-model="typeQueryParams.dictType" placeholder="请输入字典类型" clearable @keyup.enter="handleTypeQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="typeQueryParams.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleTypeQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetTypeQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="header-actions">
          <span class="card-title">字典类型列表</span>
          <div class="card-buttons">
            <el-button type="primary" icon="Plus" @click="handleAddType">新增字典类型</el-button>
            <el-button type="danger" icon="Delete" :disabled="typeMultiple" @click="handleBatchDeleteType">批量删除</el-button>
            <el-button type="success" icon="Download" @click="handleExportType">导出字典</el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="typeList"
        border
        stripe
        v-loading="typeLoading"
        @selection-change="handleTypeSelectionChange"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="dictId" label="字典 ID" width="100" />
        <el-table-column prop="dictName" label="字典名称" width="150" />
        <el-table-column prop="dictType" label="字典类型" width="150" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Setting" @click="handleDataList(row)">字典数据</el-button>
            <el-button link type="primary" icon="Edit" @click="handleEditType(row)">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDeleteType(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-show="typePagination.total > 0"
          v-model:current-page="typePagination.current"
          v-model:page-size="typePagination.size"
          :total="typePagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getTypeList"
          @current-change="getTypeList"
        />
        <div class="pagination-info">
          共 {{ typePagination.total }} 条，每页 {{ typePagination.size }} 条，当前第 {{ typePagination.current }} 页
        </div>
      </div>
    </el-card>

    <!-- 字典类型新增/编辑对话框 -->
    <el-dialog
      :title="typeDialog.title"
      v-model="typeDialog.visible"
      width="500px"
      @close="resetTypeForm"
    >
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="100px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="typeForm.dictType" placeholder="请输入字典类型" :disabled="!!typeForm.dictId" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="typeForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="typeForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitTypeForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据对话框 -->
    <el-dialog
      title="字典数据"
      v-model="dataDialog.visible"
      width="900px"
    >
      <div class="data-header">
        <div class="data-header-actions">
          <el-button type="primary" icon="Plus" @click="handleAddData">新增字典数据</el-button>
          <el-button type="danger" icon="Delete" :disabled="dataMultiple" @click="handleBatchDeleteData">批量删除</el-button>
        </div>
      </div>
      <el-table
        :data="dataList"
        border
        stripe
        v-loading="dataLoading"
        @selection-change="handleDataSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="dictCode" label="字典编码" width="100" />
        <el-table-column prop="dictLabel" label="字典标签" width="150" />
        <el-table-column prop="dictValue" label="字典值" width="150" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="cssClass" label="样式属性" width="120" />
        <el-table-column prop="listClass" label="回显样式" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" @click="handleEditData(row)">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDeleteData(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="dataDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据新增/编辑对话框 -->
    <el-dialog
      :title="dataFormDialog.title"
      v-model="dataFormDialog.visible"
      width="500px"
      @close="resetDataForm"
    >
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px">
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="请输入字典值" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="dataForm.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dataForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="样式属性" prop="cssClass">
          <el-input v-model="dataForm.cssClass" placeholder="如：text-primary" />
        </el-form-item>
        <el-form-item label="回显样式" prop="listClass">
          <el-input v-model="dataForm.listClass" placeholder="如：success" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataFormDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitDataForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const typeList = ref([])
const dataList = ref([])
const typeLoading = ref(false)
const dataLoading = ref(false)
const typeMultiple = ref(true)
const dataMultiple = ref(true)
const typeIds = ref([])
const dataIds = ref([])

const typeQueryParams = reactive({
  dictName: '',
  dictType: '',
  status: null
})

const typePagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const typeDialog = reactive({
  visible: false,
  title: ''
})

const dataDialog = reactive({
  visible: false
})

const dataFormDialog = reactive({
  visible: false,
  title: ''
})

const typeFormRef = ref(null)
const dataFormRef = ref(null)

const typeForm = reactive({
  dictId: null,
  dictName: '',
  dictType: '',
  status: 1,
  remark: ''
})

const dataForm = reactive({
  dictCode: null,
  dictType: '',
  dictLabel: '',
  dictValue: '',
  sort: 0,
  status: 1,
  cssClass: '',
  listClass: ''
})

const typeRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
}

const dataRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }]
}

// 获取字典类型列表
const getTypeList = () => {
  typeLoading.value = true
  const params = {
    ...typeQueryParams,
    pageNum: typePagination.current,
    pageSize: typePagination.size
  }
  request.get('/system/dict/type/list', { params }).then(res => {
    const pageData = res.data || {}
    typeList.value = pageData.records || []
    typePagination.total = pageData.total || 0
    typeLoading.value = false
  }).catch(() => {
    typeLoading.value = false
  })
}

// 获取字典数据列表
const getDataList = () => {
  dataLoading.value = true
  request.get('/system/dict/data/list', { params: { dictType: typeForm.dictType } }).then(res => {
    dataList.value = res.data?.records || []
    dataLoading.value = false
  }).catch(() => {
    dataLoading.value = false
  })
}

// 查询类型
const handleTypeQuery = () => {
  typePagination.current = 1
  getTypeList()
}

// 重置类型查询
const resetTypeQuery = () => {
  typeQueryParams.dictName = ''
  typeQueryParams.dictType = ''
  typeQueryParams.status = null
  handleTypeQuery()
}

// 类型多选框选中数据
const handleTypeSelectionChange = (selection) => {
  typeIds.value = selection.map(item => item.dictId)
  typeMultiple.value = !selection.length
}

// 数据多选框选中数据
const handleDataSelectionChange = (selection) => {
  dataIds.value = selection.map(item => item.dictCode)
  dataMultiple.value = !selection.length
}

// 新增类型
const handleAddType = () => {
  typeDialog.title = '新增字典类型'
  typeDialog.visible = true
}

// 编辑类型
const handleEditType = (row) => {
  typeDialog.title = '修改字典类型'
  typeDialog.visible = true
  Object.assign(typeForm, row)
}

// 删除类型
const handleDeleteType = (row) => {
  ElMessageBox.confirm(`确认删除字典类型 "${row.dictName}" 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    request.delete(`/system/dict/type/${row.dictId}`).then(() => {
      ElMessage.success('删除成功')
      getTypeList()
    })
  })
}

// 批量删除类型
const handleBatchDeleteType = () => {
  ElMessageBox.confirm(`确认删除选中的 ${typeIds.value.length} 条字典类型吗？`, '警告', {
    type: 'warning'
  }).then(() => {
    request.delete('/system/dict/type/batch', { data: typeIds.value }).then(() => {
      ElMessage.success('批量删除成功')
      getTypeList()
    })
  })
}

// 导出字典
const handleExportType = () => {
  const queryParams = {
    ...typeQueryParams,
    pageNum: 1,
    pageSize: 1000
  }
  request.get('/system/dict/type/export', { params: queryParams, responseType: 'blob' }).then(res => {
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '字典数据_' + new Date().getTime() + '.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  })
}

// 查看字典数据
const handleDataList = (row) => {
  dataDialog.visible = true
  Object.assign(typeForm, row)
  getDataList()
}

// 新增数据
const handleAddData = () => {
  dataFormDialog.title = '新增字典数据'
  dataFormDialog.visible = true
  dataForm.dictType = typeForm.dictType
}

// 编辑数据
const handleEditData = (row) => {
  dataFormDialog.title = '修改字典数据'
  dataFormDialog.visible = true
  Object.assign(dataForm, row)
}

// 删除数据
const handleDeleteData = (row) => {
  ElMessageBox.confirm(`确认删除字典数据 "${row.dictLabel}" 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    request.delete(`/system/dict/data/${row.dictCode}`).then(() => {
      ElMessage.success('删除成功')
      getDataList()
    })
  })
}

// 批量删除数据
const handleBatchDeleteData = () => {
  ElMessageBox.confirm(`确认删除选中的 ${dataIds.value.length} 条字典数据吗？`, '警告', {
    type: 'warning'
  }).then(() => {
    request.delete('/system/dict/data/batch', { data: dataIds.value }).then(() => {
      ElMessage.success('批量删除成功')
      getDataList()
    })
  })
}

// 提交类型表单
const submitTypeForm = () => {
  typeFormRef.value.validate(valid => {
    if (valid) {
      const api = typeForm.dictId ? request.put : request.post
      api('/system/dict/type', typeForm).then(() => {
        ElMessage.success('操作成功')
        typeDialog.visible = false
        getTypeList()
      })
    }
  })
}

// 提交数据表单
const submitDataForm = () => {
  dataFormRef.value.validate(valid => {
    if (valid) {
      const api = dataForm.dictCode ? request.put : request.post
      api('/system/dict/data', dataForm).then(() => {
        ElMessage.success('操作成功')
        dataFormDialog.visible = false
        getDataList()
      })
    }
  })
}

// 重置类型表单
const resetTypeForm = () => {
  typeFormRef.value?.resetFields()
  Object.assign(typeForm, {
    dictId: null,
    dictName: '',
    dictType: '',
    status: 1,
    remark: ''
  })
}

// 重置数据表单
const resetDataForm = () => {
  dataFormRef.value?.resetFields()
  Object.assign(dataForm, {
    dictCode: null,
    dictType: '',
    dictLabel: '',
    dictValue: '',
    sort: 0,
    status: 1,
    cssClass: '',
    listClass: ''
  })
}

onMounted(() => {
  getTypeList()
})
</script>

<style lang="scss" scoped>
.dict-container {
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

  .data-header {
    margin-bottom: 15px;

    .data-header-actions {
      display: flex;
      gap: 10px;
    }
  }
}
</style>
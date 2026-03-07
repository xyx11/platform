<template>
  <div class="config-container">
    <el-card>
      <el-form :model="queryParams" inline>
        <el-form-item label="参数名称">
          <el-input v-model="queryParams.configName" placeholder="请输入参数名称" clearable />
        </el-form-item>
        <el-form-item label="参数键名">
          <el-input v-model="queryParams.configKey" placeholder="请输入参数键名" clearable />
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
          <el-button type="primary" icon="Plus" @click="handleAdd">新增参数</el-button>
          <el-button type="danger" icon="Delete" :disabled="multiple" @click="handleBatchDelete">批量删除</el-button>
          <el-button type="warning" icon="Refresh" @click="handleRefreshCache">刷新缓存</el-button>
        </div>
      </template>

      <el-table
        :data="configList"
        border
        stripe
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="configId" label="参数主键" width="100" />
        <el-table-column prop="configName" label="参数名称" width="150" />
        <el-table-column prop="configKey" label="参数键名" width="150" />
        <el-table-column prop="configValue" label="参数键值" min-width="150" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
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
          @size-change="getConfigList"
          @current-change="getConfigList"
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
        <el-form-item label="参数名称" prop="configName">
          <el-input v-model="form.configName" placeholder="请输入参数名称" />
        </el-form-item>
        <el-form-item label="参数键名" prop="configKey">
          <el-input v-model="form.configKey" placeholder="请输入参数键名" :disabled="!!form.configId" />
        </el-form-item>
        <el-form-item label="参数键值" prop="configValue">
          <el-input v-model="form.configValue" placeholder="请输入参数键值" />
        </el-form-item>
        <el-form-item label="系统内置" prop="configType">
          <el-radio-group v-model="form.configType">
            <el-radio :label="0">是</el-radio>
            <el-radio :label="1">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const configList = ref([])
const loading = ref(false)
const multiple = ref(true)
const configIds = ref([])

const queryParams = reactive({
  configName: '',
  configKey: '',
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

const formRef = ref(null)

const form = reactive({
  configId: null,
  configName: '',
  configKey: '',
  configValue: '',
  configType: 1,
  status: 1,
  remark: ''
})

const rules = {
  configName: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
  configKey: [{ required: true, message: '请输入参数键名', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入参数键值', trigger: 'blur' }]
}

// 获取参数配置列表
const getConfigList = () => {
  loading.value = true
  request.get('/system/config/list', { params: queryParams }).then(res => {
    configList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// 查询
const handleQuery = () => {
  pagination.current = 1
  getConfigList()
}

// 重置
const resetQuery = () => {
  queryParams.configName = ''
  queryParams.configKey = ''
  queryParams.status = null
  handleQuery()
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  configIds.value = selection.map(item => item.configId)
  multiple.value = !selection.length
}

// 新增
const handleAdd = () => {
  dialog.title = '新增参数'
  dialog.visible = true
}

// 修改
const handleUpdate = (row) => {
  dialog.title = '修改参数'
  dialog.visible = true
  Object.assign(form, row)
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除参数 "${row.configName}" 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    request.delete(`/system/config/${row.configId}`).then(() => {
      ElMessage.success('删除成功')
      getConfigList()
    })
  })
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确认删除选中的 ${configIds.value.length} 条参数吗？`, '警告', {
    type: 'warning'
  }).then(() => {
    request.delete('/system/config/batch', { data: configIds.value }).then(() => {
      ElMessage.success('批量删除成功')
      getConfigList()
    })
  })
}

// 刷新缓存
const handleRefreshCache = () => {
  ElMessageBox.confirm('确认刷新参数配置缓存吗？', '提示', {
    type: 'warning'
  }).then(() => {
    request.delete('/system/config/refreshCache').then(() => {
      ElMessage.success('缓存刷新成功')
    })
  })
}

// 提交表单
const submitForm = () => {
  formRef.value.validate(valid => {
    if (valid) {
      const api = form.configId ? request.put : request.post
      api('/system/config', form).then(() => {
        ElMessage.success('操作成功')
        dialog.visible = false
        getConfigList()
      })
    }
  })
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    configId: null,
    configName: '',
    configKey: '',
    configValue: '',
    configType: 1,
    status: 1,
    remark: ''
  })
}

onMounted(() => {
  getConfigList()
})
</script>

<style lang="scss" scoped>
.config-container {
  .table-card {
    margin-top: 20px;

    .header-actions {
      display: flex;
      gap: 10px;
    }

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
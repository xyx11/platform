<template>
  <div class="form-definition-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>表单定义管理</span>
          <div>
            <el-button type="primary" @click="handleAdd">新建表单</el-button>
          </div>
        </div>
      </template>

      <el-table :data="formList" v-loading="loading" style="width: 100%">
        <el-table-column prop="formName" label="表单名称" show-overflow-tooltip />
        <el-table-column prop="formCode" label="表单编码" width="150" />
        <el-table-column label="表单类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.formType === 1">普通表单</el-tag>
            <el-tag v-else-if="row.formType === 2" type="warning">流程表单</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'info' : 'success'">
              {{ row.status === 0 ? '草稿' : '已发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              @click="handlePublish(row)"
            >
              发布
            </el-button>
            <el-button
              v-else
              type="warning"
              size="small"
              @click="handleDisable(row)"
            >
              停用
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="表单名称" prop="formName">
              <el-input v-model="formData.formName" placeholder="请输入表单名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="表单编码" prop="formCode">
              <el-input v-model="formData.formCode" placeholder="请输入表单编码（英文）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="表单类型" prop="formType">
              <el-select v-model="formData.formType" placeholder="请选择表单类型">
                <el-option label="普通表单" :value="1" />
                <el-option label="流程表单" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="formData.category" placeholder="请选择分类" clearable>
                <el-option label="人事表单" value="hr" />
                <el-option label="财务表单" value="finance" />
                <el-option label="行政表单" value="admin" />
                <el-option label="采购表单" value="purchase" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="表单 Schema" prop="formSchema">
          <el-input
            v-model="formData.formSchema"
            type="textarea"
            :rows="15"
            placeholder="请输入 JSON Schema 格式的表单定义"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FormDefinition">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formRef = ref(null)
const formData = reactive({
  id: null,
  formName: '',
  formCode: '',
  formType: 1,
  formSchema: '',
  category: '',
  status: 0,
  remark: ''
})

const defaultFormSchema = `{
  "type": "object",
  "properties": {
    "title": {
      "type": "string",
      "title": "标题",
      "required": true
    },
    "content": {
      "type": "string",
      "title": "内容",
      "required": true
    }
  }
}`

const formRules = {
  formName: [{ required: true, message: '请输入表单名称', trigger: 'blur' }],
  formCode: [
    { required: true, message: '请输入表单编码', trigger: 'blur' },
    { pattern: /^[a-z_][a-z0-9_]*$/, message: '编码只能包含小写字母、数字和下划线', trigger: 'blur' }
  ],
  formType: [{ required: true, message: '请选择表单类型', trigger: 'change' }],
  formSchema: [{ required: true, message: '请输入表单 Schema', trigger: 'blur' }]
}

// 获取表单列表
const getFormList = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/system/form-definition/list',
      method: 'get',
      params: { pageNum: pageNum.value, pageSize: pageSize.value }
    })
    formList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取表单列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新建表单'
  formData.formSchema = defaultFormSchema
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑表单'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 发布
const handlePublish = (row) => {
  ElMessageBox.confirm(
    `确定要发布"${row.formName}"表单吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url: `/system/form-definition/publish/${row.id}`,
        method: 'post'
      })
      ElMessage.success('发布成功')
      getFormList()
    } catch (error) {
      ElMessage.error('发布失败：' + error.message)
    }
  }).catch(() => {})
}

// 停用
const handleDisable = (row) => {
  ElMessageBox.confirm(
    `确定要停用"${row.formName}"表单吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url: `/system/form-definition/disable/${row.id}`,
        method: 'post'
      })
      ElMessage.success('停用成功')
      getFormList()
    } catch (error) {
      ElMessage.error('停用失败：' + error.message)
    }
  }).catch(() => {})
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除"${row.formName}"表单吗？删除后无法恢复！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url: `/system/form-definition/${row.id}`,
        method: 'delete'
      })
      ElMessage.success('删除成功')
      getFormList()
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  }).catch(() => {})
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 验证 JSON Schema
        try {
          JSON.parse(formData.formSchema)
        } catch (e) {
          ElMessage.error('表单 Schema 必须是有效的 JSON 格式')
          return
        }

        const url = formData.id ? '/system/form-definition' : '/system/form-definition'
        const method = formData.id ? 'put' : 'post'

        await request({
          url,
          method,
          data: formData
        })

        ElMessage.success(formData.id ? '更新成功' : '新建成功')
        dialogVisible.value = false
        getFormList()
      } catch (error) {
        ElMessage.error('操作失败：' + error.message)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.resetFields()
  Object.assign(formData, {
    id: null,
    formName: '',
    formCode: '',
    formType: 1,
    formSchema: '',
    category: '',
    status: 0,
    remark: ''
  })
}

// 分页
const handleSizeChange = () => getFormList()
const handleCurrentChange = () => getFormList()

onMounted(() => {
  getFormList()
})
</script>

<style scoped>
.form-definition-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
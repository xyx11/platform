<template>
  <div class="workflow-form-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>流程表单绑定</span>
          <div>
            <el-button type="primary" @click="handleBind">绑定表单</el-button>
          </div>
        </div>
      </template>

      <el-table :data="formBindingList" v-loading="loading" style="width: 100%">
        <el-table-column prop="processDefinitionKey" label="流程定义 Key" width="200" />
        <el-table-column prop="taskDefinitionKey" label="任务节点 Key" width="200" />
        <el-table-column prop="formCode" label="表单编码" width="150" />
        <el-table-column prop="formName" label="表单名称" show-overflow-tooltip />
        <el-table-column label="表单类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.formType === 1 ? 'success' : 'warning'">
              {{ row.formType === 1 ? '启动表单' : '办理表单' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 表单绑定对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="流程定义 Key" prop="processDefinitionKey">
          <el-input
            v-model="formData.processDefinitionKey"
            placeholder="请输入流程定义 Key"
            clearable
          />
        </el-form-item>
        <el-form-item label="任务节点 Key" prop="taskDefinitionKey">
          <el-input
            v-model="formData.taskDefinitionKey"
            placeholder="请输入任务节点 Key"
            clearable
          />
        </el-form-item>
        <el-form-item label="表单编码" prop="formCode">
          <el-select
            v-model="formData.formCode"
            placeholder="请选择表单"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in formDefinitionList"
              :key="item.code"
              :label="item.name"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="表单类型" prop="formType">
          <el-radio-group v-model="formData.formType">
            <el-radio :label="1">启动表单</el-radio>
            <el-radio :label="2">办理表单</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WorkflowForm">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFormBindingList as getFormBindingListApi, bindForm, unbindForm, getFormDefinitionList as getFormDefinitionListApi } from '@/api/system/workflow-form'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formBindingList = ref([])
const formDefinitionList = ref([])

const formRef = ref(null)
const formData = reactive({
  processDefinitionKey: '',
  taskDefinitionKey: '',
  formCode: '',
  formType: 1
})

const formRules = {
  processDefinitionKey: [{ required: true, message: '请输入流程定义 Key', trigger: 'blur' }],
  taskDefinitionKey: [{ required: true, message: '请输入任务节点 Key', trigger: 'blur' }],
  formCode: [{ required: true, message: '请选择表单', trigger: 'change' }],
  formType: [{ required: true, message: '请选择表单类型', trigger: 'change' }]
}

// 获取表单绑定列表
const getFormBindingList = async () => {
  loading.value = true
  try {
    const res = await getFormBindingListApi({ pageNum: 1, pageSize: 100 })
    formBindingList.value = res.data?.records || res.data?.list || []
  } catch (error) {
    ElMessage.error('获取表单绑定列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 获取表单定义列表
const getFormDefinitionList = async () => {
  try {
    const res = await getFormDefinitionListApi()
    formDefinitionList.value = res.data?.records || res.data || []
  } catch (error) {
    // 获取表单定义列表失败
  }
}

// 打开绑定对话框
const handleBind = () => {
  dialogTitle.value = '绑定表单'
  dialogVisible.value = true
}

// 编辑绑定
const handleEdit = (row) => {
  dialogTitle.value = '编辑表单绑定'
  formData.processDefinitionKey = row.processDefinitionKey
  formData.taskDefinitionKey = row.taskDefinitionKey
  formData.formCode = row.formCode
  formData.formType = row.formType
  dialogVisible.value = true
}

// 删除绑定
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除流程（${row.processDefinitionKey}）的表单绑定吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await unbindForm({
          processDefinitionKey: row.processDefinitionKey,
          taskDefinitionKey: row.taskDefinitionKey,
          formType: row.formType
        })
      ElMessage.success('删除成功')
      getFormBindingList()
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
        await bindForm(formData)
        ElMessage.success('绑定成功')
        dialogVisible.value = false
        getFormBindingList()
      } catch (error) {
        ElMessage.error('绑定失败：' + error.message)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.resetFields()
  formData.processDefinitionKey = ''
  formData.taskDefinitionKey = ''
  formData.formCode = ''
  formData.formType = 1
}

onMounted(() => {
  getFormBindingList()
  getFormDefinitionList()
})
</script>

<style scoped>
.workflow-form-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
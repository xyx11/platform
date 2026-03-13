<template>
  <div class="workflow-definition-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>流程定义管理</span>
          <div>
            <el-input
              v-model="searchForm.name"
              placeholder="流程名称"
              clearable
              style="width: 150px"
              class="mr-2"
              @change="search"
            />
            <el-select
              v-model="searchForm.category"
              placeholder="分类"
              clearable
              style="width: 120px"
              class="mr-2"
              @change="search"
            >
              <el-option label="人事管理" value="hr" />
              <el-option label="财务管理" value="finance" />
              <el-option label="行政管理" value="admin" />
              <el-option label="采购管理" value="purchase" />
              <el-option label="其他" value="other" />
            </el-select>
            <el-button type="primary" @click="search">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
            <el-button type="primary" @click="handleDeploy">部署流程</el-button>
            <el-button @click="refresh">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="definitionList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="流程 ID" min-width="150" show-overflow-tooltip />
        <el-table-column prop="name" label="流程名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="key" label="流程 Key" width="150" show-overflow-tooltip />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.category === 'hr'">人事</el-tag>
            <el-tag v-else-if="row.category === 'finance'" type="success">财务</el-tag>
            <el-tag v-else-if="row.category === 'admin'" type="warning">行政</el-tag>
            <el-tag v-else-if="row.category === 'purchase'" type="danger">采购</el-tag>
            <el-tag v-else>其他</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.suspended ? 'danger' : 'success'">
              {{ row.suspended ? '挂起' : '激活' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deploymentTime" label="部署时间" width="180" />
        <el-table-column prop="resourceName" label="资源名称" width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="400" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewBpmn(row)">
              查看 BPMN
            </el-button>
            <el-button type="info" size="small" @click="handleViewDetail(row)">
              详情
            </el-button>
            <el-button type="warning" size="small" @click="handleSuspendOrActivate(row)">
              {{ row.suspended ? '激活' : '挂起' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-show="total > 0"
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getProcessDefinitions"
          @current-change="getProcessDefinitions"
        />
      </div>
    </el-card>

    <!-- 部署流程对话框 -->
    <el-dialog
      v-model="deployDialogVisible"
      title="部署流程"
      width="800px"
      @close="handleDeployClose"
    >
      <el-form ref="deployFormRef" :model="deployForm" :rules="deployRules" label-width="100px">
        <el-form-item label="流程名称" prop="name">
          <el-input v-model="deployForm.name" placeholder="请输入流程名称" />
        </el-form-item>
        <el-form-item label="流程分类" prop="category">
          <el-select v-model="deployForm.category" placeholder="请选择流程分类" clearable>
            <el-option label="人事管理" value="hr" />
            <el-option label="财务管理" value="finance" />
            <el-option label="行政管理" value="admin" />
            <el-option label="采购管理" value="purchase" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="BPMN 文件" prop="bpmnFile">
          <el-upload
            ref="uploadRef"
            drag
            :auto-upload="false"
            :limit="1"
            accept=".bpmn,.xml"
            :on-change="handleFileChange"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                只能上传 bpmn/xml 文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="BPMN 内容" prop="bpmnXml">
          <el-input
            v-model="deployForm.bpmnXml"
            type="textarea"
            :rows="10"
            placeholder="或直接粘贴 BPMN XML 内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deployDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDeploy" :loading="deploying">部署</el-button>
      </template>
    </el-dialog>

    <!-- 查看 BPMN 对话框 -->
    <el-dialog
      v-model="bpmnDialogVisible"
      title="查看 BPMN"
      width="800px"
    >
      <el-input
        v-model="currentBpmnXml"
        type="textarea"
        :rows="20"
        readonly
      />
    </el-dialog>

    <!-- 流程详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="流程定义详情"
      width="700px"
    >
      <el-descriptions :column="2" border v-if="currentDefinition">
        <el-descriptions-item label="流程 ID">{{ currentDefinition.id }}</el-descriptions-item>
        <el-descriptions-item label="流程名称">{{ currentDefinition.name }}</el-descriptions-item>
        <el-descriptions-item label="流程 Key">{{ currentDefinition.key }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ currentDefinition.version }}</el-descriptions-item>
        <el-descriptions-item label="分类">
          <el-tag v-if="currentDefinition.category === 'hr'">人事</el-tag>
          <el-tag v-else-if="currentDefinition.category === 'finance'" type="success">财务</el-tag>
          <el-tag v-else-if="currentDefinition.category === 'admin'" type="warning">行政</el-tag>
          <el-tag v-else-if="currentDefinition.category === 'purchase'" type="danger">采购</el-tag>
          <el-tag v-else>其他</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentDefinition.suspended ? 'danger' : 'success'">
            {{ currentDefinition.suspended ? '挂起' : '激活' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="部署 ID">{{ currentDefinition.deploymentId }}</el-descriptions-item>
        <el-descriptions-item label="部署时间">{{ currentDefinition.deploymentTime }}</el-descriptions-item>
        <el-descriptions-item label="资源名称" :span="2">{{ currentDefinition.resourceName }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentDefinition.description || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WorkflowDefinition">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const deploying = ref(false)
const deployDialogVisible = ref(false)
const bpmnDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const definitionList = ref([])
const currentBpmnXml = ref('')
const currentDefinition = ref(null)

// 分页
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索条件
const searchForm = reactive({
  name: '',
  category: ''
})

const deployFormRef = ref(null)
const uploadRef = ref(null)
const deployForm = reactive({
  name: '',
  category: '',
  bpmnFile: null,
  bpmnXml: ''
})

const deployRules = {
  name: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
  bpmnXml: [{ required: true, message: '请上传 BPMN 文件或粘贴内容', trigger: 'blur' }]
}

// 获取流程定义列表
const getProcessDefinitions = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/system/workflow/definition/list',
      method: 'get',
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        ...searchForm
      }
    })
    definitionList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取流程定义列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索
const search = () => {
  pageNum.value = 1
  getProcessDefinitions()
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.category = ''
  search()
}

// 打开部署对话框
const handleDeploy = () => {
  deployDialogVisible.value = true
}

// 关闭部署对话框
const handleDeployClose = () => {
  deployFormRef.value?.resetFields()
  uploadRef.value?.clearFiles()
  deployForm.name = ''
  deployForm.category = ''
  deployForm.bpmnFile = null
  deployForm.bpmnXml = ''
}

// 处理文件上传
const handleFileChange = (file) => {
  const reader = new FileReader()
  reader.onload = (event) => {
    deployForm.bpmnXml = event.target.result
  }
  reader.readAsText(file.raw)
}

// 提交部署
const submitDeploy = async () => {
  if (!deployFormRef.value) return

  await deployFormRef.value.validate(async (valid) => {
    if (valid && deployForm.bpmnXml) {
      deploying.value = true
      try {
        const formData = new FormData()
        formData.append('name', deployForm.name)
        formData.append('bpmnXml', deployForm.bpmnXml)
        if (deployForm.category) {
          formData.append('category', deployForm.category)
        }

        await request({
          url: '/system/workflow/deploy',
          method: 'post',
          data: formData,
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        })

        ElMessage.success('部署成功')
        deployDialogVisible.value = false
        getProcessDefinitions()
      } catch (error) {
        ElMessage.error('部署失败：' + error.message)
      } finally {
        deploying.value = false
      }
    }
  })
}

// 查看 BPMN
const handleViewBpmn = async (row) => {
  try {
    const res = await request({
      url: `/system/workflow/definition/bpmn/${row.id}`,
      method: 'get'
    })
    currentBpmnXml.value = res.data?.bpmnXml || ''
    bpmnDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取 BPMN 失败：' + error.message)
  }
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const res = await request({
      url: `/system/workflow/definition/${row.id}`,
      method: 'get'
    })
    currentDefinition.value = res.data
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取流程详情失败：' + error.message)
  }
}

// 挂起/激活
const handleSuspendOrActivate = async (row) => {
  const action = row.suspended ? '激活' : '挂起'
  const url = row.suspended
    ? `/system/workflow/activate/${row.deploymentId}`
    : `/system/workflow/suspend/${row.deploymentId}`

  await ElMessageBox.confirm(
    `确定要${action}该流程吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url,
        method: 'post'
      })
      ElMessage.success(`${action}成功`)
      getProcessDefinitions()
    } catch (error) {
      ElMessage.error(`${action}失败：` + error.message)
    }
  }).catch(() => {})
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除流程"${row.name}"吗？删除后无法恢复！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url: `/system/workflow/definition/${row.deploymentId}`,
        method: 'delete'
      })
      ElMessage.success('删除成功')
      getProcessDefinitions()
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  }).catch(() => {})
}

// 刷新
const refresh = () => {
  getProcessDefinitions()
}

onMounted(() => {
  getProcessDefinitions()
})
</script>

<style scoped>
.workflow-definition-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mr-2 {
  margin-right: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
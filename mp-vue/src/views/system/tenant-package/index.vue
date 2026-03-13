<template>
  <div class="tenant-package-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>租户套餐管理</span>
          <div>
            <el-button type="primary" @click="handleAdd">新增套餐</el-button>
          </div>
        </div>
      </template>

      <el-table :data="packageList" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="套餐名称" width="150" />
        <el-table-column prop="code" label="套餐编码" width="120" />
        <el-table-column prop="description" label="套餐描述" show-overflow-tooltip />
        <el-table-column label="套餐类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.packageType === 1" type="info">免费版</el-tag>
            <el-tag v-else-if="row.packageType === 2" type="success">基础版</el-tag>
            <el-tag v-else-if="row.packageType === 3" type="warning">专业版</el-tag>
            <el-tag v-else-if="row.packageType === 4" type="primary">企业版</el-tag>
            <el-tag v-else>定制版</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格（元/月）" width="100" />
        <el-table-column prop="maxUsers" label="最大用户数" width="100" />
        <el-table-column prop="maxDepts" label="最大部门数" width="100" />
        <el-table-column prop="maxStorage" label="最大存储（MB）" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="60" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 1"
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
      width="700px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="套餐名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="套餐编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入套餐编码" />
        </el-form-item>
        <el-form-item label="套餐描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入套餐描述"
          />
        </el-form-item>
        <el-form-item label="套餐类型" prop="packageType">
          <el-select v-model="formData.packageType" placeholder="请选择套餐类型">
            <el-option label="免费版" :value="1" />
            <el-option label="基础版" :value="2" />
            <el-option label="专业版" :value="3" />
            <el-option label="企业版" :value="4" />
            <el-option label="定制版" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格（元/月）" prop="price">
          <el-input-number v-model="formData.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="最大用户数" prop="maxUsers">
          <el-input-number v-model="formData.maxUsers" :min="0" />
        </el-form-item>
        <el-form-item label="最大部门数" prop="maxDepts">
          <el-input-number v-model="formData.maxDepts" :min="0" />
        </el-form-item>
        <el-form-item label="最大存储（MB）" prop="maxStorage">
          <el-input-number v-model="formData.maxStorage" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TenantPackage">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const packageList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formRef = ref(null)
const formData = reactive({
  id: null,
  name: '',
  code: '',
  description: '',
  packageType: 1,
  price: 0,
  maxUsers: 0,
  maxDepts: 0,
  maxStorage: 0,
  status: 1,
  sort: 0
})

const formRules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入套餐编码', trigger: 'blur' }],
  packageType: [{ required: true, message: '请选择套餐类型', trigger: 'change' }]
}

// 获取套餐列表
const getPackageList = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/system/tenant-package/list',
      method: 'get',
      params: { pageNum: pageNum.value, pageSize: pageSize.value }
    })
    packageList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取套餐列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增套餐'
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑套餐'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 停用
const handleDisable = (row) => {
  ElMessageBox.confirm(
    `确定要停用"${row.name}"套餐吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url: `/system/tenant-package/disable/${row.id}`,
        method: 'post'
      })
      ElMessage.success('停用成功')
      getPackageList()
    } catch (error) {
      ElMessage.error('停用失败：' + error.message)
    }
  }).catch(() => {})
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除"${row.name}"套餐吗？删除后无法恢复！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url: `/system/tenant-package/${row.id}`,
        method: 'delete'
      })
      ElMessage.success('删除成功')
      getPackageList()
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
        const url = formData.id
          ? '/system/tenant-package'
          : '/system/tenant-package'
        const method = formData.id ? 'put' : 'post'

        await request({
          url,
          method,
          data: formData
        })

        ElMessage.success(formData.id ? '更新成功' : '新增成功')
        dialogVisible.value = false
        getPackageList()
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
    name: '',
    code: '',
    description: '',
    packageType: 1,
    price: 0,
    maxUsers: 0,
    maxDepts: 0,
    maxStorage: 0,
    status: 1,
    sort: 0
  })
}

// 分页
const handleSizeChange = () => getPackageList()
const handleCurrentChange = () => getPackageList()

onMounted(() => {
  getPackageList()
})
</script>

<style scoped>
.tenant-package-container {
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
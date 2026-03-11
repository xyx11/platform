<template>
  <div class="data-permission-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>数据权限规则管理</span>
          <div>
            <el-button type="primary" @click="handleAdd">新增规则</el-button>
          </div>
        </div>
      </template>

      <el-table :data="permissionList" v-loading="loading" style="width: 100%">
        <el-table-column prop="roleId" label="角色 ID" width="100" />
        <el-table-column prop="menuId" label="菜单 ID" width="100" />
        <el-table-column prop="tableName" label="表名" width="120" />
        <el-table-column label="权限类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.permissionType === 1">行级</el-tag>
            <el-tag v-else-if="row.permissionType === 2" type="warning">列级</el-tag>
            <el-tag v-else-if="row.permissionType === 3" type="danger">字段级</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ruleExpression" label="规则表达式" show-overflow-tooltip />
        <el-table-column prop="dataFilter" label="数据过滤条件" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
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
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="140px">
        <el-form-item label="角色 ID" prop="roleId">
          <el-input-number v-model="formData.roleId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="菜单 ID" prop="menuId">
          <el-input-number v-model="formData.menuId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="表名" prop="tableName">
          <el-input v-model="formData.tableName" placeholder="请输入表名" />
        </el-form-item>
        <el-form-item label="权限类型" prop="permissionType">
          <el-select v-model="formData.permissionType" placeholder="请选择权限类型">
            <el-option label="行级" :value="1" />
            <el-option label="列级" :value="2" />
            <el-option label="字段级" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则表达式 (SpEL)" prop="ruleExpression">
          <el-input
            v-model="formData.ruleExpression"
            type="textarea"
            :rows="3"
            placeholder="例：#userId == data.userId"
          />
        </el-form-item>
        <el-form-item label="允许的字段" prop="allowedColumns">
          <el-input
            v-model="formData.allowedColumns"
            type="textarea"
            :rows="2"
            placeholder="逗号分隔，例：id,name,create_time"
          />
        </el-form-item>
        <el-form-item label="禁止的字段" prop="deniedColumns">
          <el-input
            v-model="formData.deniedColumns"
            type="textarea"
            :rows="2"
            placeholder="逗号分隔，例：password,secret"
          />
        </el-form-item>
        <el-form-item label="数据过滤条件" prop="dataFilter">
          <el-input
            v-model="formData.dataFilter"
            type="textarea"
            :rows="3"
            placeholder="例：dept_id = 100 OR create_by = #{userId}"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
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

<script setup name="DataPermission">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const permissionList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formRef = ref(null)
const formData = reactive({
  id: null,
  roleId: null,
  menuId: null,
  tableName: '',
  permissionType: 1,
  ruleExpression: '',
  allowedColumns: '',
  deniedColumns: '',
  dataFilter: '',
  status: 1,
  remark: ''
})

const formRules = {
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }],
  tableName: [{ required: true, message: '请输入表名', trigger: 'blur' }],
  permissionType: [{ required: true, message: '请选择权限类型', trigger: 'change' }]
}

// 获取权限规则列表
const getPermissionList = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/system/data-permission/list',
      method: 'get',
      params: { pageNum: pageNum.value, pageSize: pageSize.value }
    })
    permissionList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取权限规则列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增数据权限规则'
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑数据权限规则'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除该权限规则吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url: `/system/data-permission/${row.id}`,
        method: 'delete'
      })
      ElMessage.success('删除成功')
      getPermissionList()
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
        const url = formData.id ? '/system/data-permission' : '/system/data-permission'
        const method = formData.id ? 'put' : 'post'

        await request({
          url,
          method,
          data: formData
        })

        ElMessage.success(formData.id ? '更新成功' : '新增成功')
        dialogVisible.value = false
        getPermissionList()
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
    roleId: null,
    menuId: null,
    tableName: '',
    permissionType: 1,
    ruleExpression: '',
    allowedColumns: '',
    deniedColumns: '',
    dataFilter: '',
    status: 1,
    remark: ''
  })
}

// 分页
const handleSizeChange = () => getPermissionList()
const handleCurrentChange = () => getPermissionList()

onMounted(() => {
  getPermissionList()
})
</script>

<style scoped>
.data-permission-container {
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
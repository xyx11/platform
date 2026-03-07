<template>
  <div class="app-container">
    <el-card>
      <!-- 搜索栏 -->
      <el-form :model="queryParams" :inline="true" label-width="68px">
        <el-form-item label="岗位编码">
          <el-input v-model="queryParams.postCode" placeholder="请输入岗位编码" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="岗位名称">
          <el-input v-model="queryParams.postName" placeholder="请输入岗位名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <el-row :gutter="10" style="margin-bottom: 10px">
        <el-col :span="1.5">
          <el-button type="primary" plain @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain :disabled="multiple" @click="handleDelete">
            <el-icon><Delete /></el-icon> 删除
          </el-button>
        </el-col>
      </el-row>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="postList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="岗位 ID" prop="postId" width="80" />
        <el-table-column label="岗位编码" prop="postCode" />
        <el-table-column label="岗位名称" prop="postName" />
        <el-table-column label="岗位排序" prop="postSort" />
        <el-table-column label="状态" prop="status">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleUpdate(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="岗位名称" prop="postName">
          <el-input v-model="form.postName" placeholder="请输入岗位名称" />
        </el-form-item>
        <el-form-item label="岗位编码" prop="postCode">
          <el-input v-model="form.postCode" placeholder="请输入岗位编码" />
        </el-form-item>
        <el-form-item label="岗位排序" prop="postSort">
          <el-input-number v-model="form.postSort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
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
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const loading = ref(false)
const postList = ref([])
const total = ref(0)
const multiple = ref(true)
const ids = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  postCode: '',
  postName: '',
  status: undefined
})

const dialog = reactive({
  visible: false,
  title: '',
  isEdit: false
})

const form = ref({
  postId: undefined,
  postCode: '',
  postName: '',
  postSort: 0,
  status: 1,
  remark: ''
})

const rules = {
  postCode: [{ required: true, message: '岗位编码不能为空', trigger: 'blur' }],
  postName: [{ required: true, message: '岗位名称不能为空', trigger: 'blur' }],
  postSort: [{ required: true, message: '岗位排序不能为空', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/system/post/list', { params: queryParams })
    postList.value = response.data.data.records || []
    total.value = response.data.data.total || 0
  } catch (error) {
    ElMessage.error('获取岗位列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryParams.pageNum = 1
  queryParams.postCode = ''
  queryParams.postName = ''
  queryParams.status = undefined
  getList()
}

const handleAdd = () => {
  dialog.title = '新增岗位'
  dialog.isEdit = false
  form.value = { postId: undefined, postCode: '', postName: '', postSort: 0, status: 1, remark: '' }
  dialog.visible = true
}

const handleUpdate = (row) => {
  dialog.title = '编辑岗位'
  dialog.isEdit = true
  form.value = { ...row }
  dialog.visible = true
}

const submitForm = async () => {
  try {
    if (dialog.isEdit) {
      await axios.put('/api/system/post', form.value)
      ElMessage.success('修改成功')
    } else {
      await axios.post('/api/system/post', form.value)
      ElMessage.success('新增成功')
    }
    dialog.visible = false
    getList()
  } catch (error) {
    ElMessage.error(dialog.isEdit ? '修改失败' : '新增失败')
  }
}

const handleDelete = async (row) => {
  const postIds = row.postId ? [row.postId] : ids.value
  const idsStr = postIds.join(',')

  try {
    await ElMessageBox.confirm(`确认删除已选中的数据项？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await axios.delete(`/api/system/post/${idsStr}`)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.postId)
  multiple.value = !selection.length
}

getList()
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}
</style>
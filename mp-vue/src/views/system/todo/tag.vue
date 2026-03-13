<template>
  <div class="todo-tag-manager">
    <!-- 标签列表 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>我的标签</span>
          <el-button type="primary" size="small" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新建标签
          </el-button>
        </div>
      </template>
      <div class="tag-list">
        <div v-for="tag in tagList" :key="tag.tagId" class="tag-item">
          <span class="tag-dot" :style="{ backgroundColor: tag.tagColor }"></span>
          <span class="tag-name">{{ tag.tagName }}</span>
          <div class="tag-actions">
            <el-button link type="primary" size="small" @click="handleEdit(tag)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(tag)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
        <el-empty v-if="tagList.length === 0" description="暂无标签" :image-size="80" />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标签名称" prop="tagName">
          <el-input v-model="form.tagName" placeholder="请输入标签名称" maxlength="20" />
        </el-form-item>
        <el-form-item label="标签颜色" prop="tagColor">
          <el-color-picker v-model="form.tagColor" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TodoTag">
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { listTodoTags, addTodoTag, updateTodoTag, delTodoTag } from '@/api/system/todo'

const { proxy } = getCurrentInstance()

const tagList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const form = ref({
  tagId: undefined,
  tagName: '',
  tagColor: '#409EFF',
  sort: 0
})

const rules = {
  tagName: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
  tagColor: [{ required: true, message: '请选择标签颜色', trigger: 'change' }]
}

// 获取标签列表
function getTagList() {
  listTodoTags().then(res => {
    tagList.value = res.data || []
  })
}

// 新增
function handleAdd() {
  form.value = { tagId: undefined, tagName: '', tagColor: '#409EFF', sort: 0 }
  dialogTitle.value = '新建标签'
  dialogVisible.value = true
}

// 编辑
function handleEdit(row) {
  form.value = { ...row }
  dialogTitle.value = '编辑标签'
  dialogVisible.value = true
}

// 删除
function handleDelete(row) {
  ElMessageBox.confirm('确认删除标签 "' + row.tagName + '" 吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    delTodoTag(row.tagId).then(() => {
      ElMessage.success('删除成功')
      getTagList()
    })
  })
}

// 提交
function submitForm() {
  formRef.value.validate(valid => {
    if (valid) {
      if (form.value.tagId) {
        updateTodoTag(form.value).then(() => {
          ElMessage.success('修改成功')
          dialogVisible.value = false
          getTagList()
        })
      } else {
        addTodoTag(form.value).then(() => {
          ElMessage.success('新建成功')
          dialogVisible.value = false
          getTagList()
        })
      }
    }
  })
}

function cancel() {
  dialogVisible.value = false
  formRef.value?.resetFields()
}

onMounted(() => {
  getTagList()
})
</script>

<style scoped lang="scss">
.todo-tag-manager {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }

  .tag-item {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    background: #f5f7fa;
    border-radius: 4px;
    transition: all 0.3s;

    &:hover {
      background: #ecf5ff;
    }

    .tag-dot {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      margin-right: 8px;
    }

    .tag-name {
      font-size: 14px;
      color: #333;
      flex: 1;
    }

    .tag-actions {
      display: flex;
      gap: 4px;
      margin-left: 8px;
    }
  }
}
</style>
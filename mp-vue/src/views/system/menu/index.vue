<template>
  <div class="menu-container">
    <el-card class="table-card">
      <template #header>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增菜单</el-button>
      </template>

      <el-table
        :data="menuList"
        row-key="menuId"
        :default-expand-all="true"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        border
        stripe
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="150" />
        <el-table-column prop="icon" label="图标" width="80">
          <template #default="{ row }">
            <el-icon v-if="row.icon" :size="20"><component :is="row.icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="permission" label="权限标识" width="200" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.type === 1">目录</el-tag>
            <el-tag v-else-if="row.type === 2" type="success">菜单</el-tag>
            <el-tag v-else type="warning">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="visible" label="显示" width="80">
          <template #default="{ row }">
            <el-tag :type="row.visible === 1 ? 'success' : 'info'">
              {{ row.visible === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="700px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">目录</el-radio>
            <el-radio :label="2">菜单</el-radio>
            <el-radio :label="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuList"
            :props="{ value: 'menuId', label: 'menuName', children: 'children' }"
            check-strictly
            placeholder="请选择上级菜单"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路由地址" prop="path" v-if="form.type !== 3">
          <el-input v-model="form.path" placeholder="请输入路由地址" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component" v-if="form.type === 2">
          <el-input v-model="form.component" placeholder="请输入组件路径，如：system/user/index" />
        </el-form-item>
        <el-form-item label="权限标识" prop="permission" v-if="form.type === 3">
          <el-input v-model="form.permission" placeholder="请输入权限标识，如：system:user:add" />
        </el-form-item>
        <el-form-item label="图标" prop="icon" v-if="form.type !== 3">
          <el-input v-model="form.icon" placeholder="请输入图标名称，如：User" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="是否外链" prop="isFrame">
          <el-radio-group v-model="form.isFrame">
            <el-radio :label="0">是</el-radio>
            <el-radio :label="1">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否缓存" prop="isCache">
          <el-radio-group v-model="form.isCache">
            <el-radio :label="0">缓存</el-radio>
            <el-radio :label="1">不缓存</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否显示" prop="visible">
          <el-radio-group v-model="form.visible">
            <el-radio :label="1">显示</el-radio>
            <el-radio :label="0">隐藏</el-radio>
          </el-radio-group>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const menuList = ref([])

const dialog = reactive({
  visible: false,
  title: ''
})

const formRef = ref(null)

const form = reactive({
  menuId: null,
  parentId: null,
  menuName: '',
  path: '',
  component: '',
  permission: '',
  icon: '',
  type: 2,
  sort: 0,
  isFrame: 1,
  isCache: 1,
  visible: 1,
  status: 1
})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }]
}

// 获取菜单列表
const getMenuList = () => {
  request.get('/system/menu/list').then(res => {
    menuList.value = res.data
  })
}

// 新增
const handleAdd = (row) => {
  dialog.title = '新增菜单'
  dialog.visible = true
  if (row) {
    form.parentId = row.menuId
  }
}

// 修改
const handleUpdate = (row) => {
  dialog.title = '修改菜单'
  dialog.visible = true
  Object.assign(form, row)
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除菜单 "${row.menuName}" 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    request.delete(`/system/menu/${row.menuId}`).then(() => {
      ElMessage.success('删除成功')
      getMenuList()
    })
  })
}

// 提交表单
const submitForm = () => {
  formRef.value.validate(valid => {
    if (valid) {
      const api = form.menuId ? request.put : request.post
      api('/system/menu', form).then(() => {
        ElMessage.success('操作成功')
        dialog.visible = false
        getMenuList()
      })
    }
  })
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    menuId: null,
    parentId: null,
    menuName: '',
    path: '',
    component: '',
    permission: '',
    icon: '',
    type: 2,
    sort: 0,
    isFrame: 1,
    isCache: 1,
    visible: 1,
    status: 1
  })
}

onMounted(() => {
  getMenuList()
})
</script>

<style lang="scss" scoped>
.menu-container {
  .table-card {
    margin-top: 20px;
  }
}
</style>

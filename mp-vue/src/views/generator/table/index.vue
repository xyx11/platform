<template>
  <div class="generator-container">
    <el-card>
      <!-- 搜索栏 -->
      <el-form :model="queryParams" inline>
        <el-form-item label="表名">
          <el-input v-model="queryParams.tableName" placeholder="请输入表名" clearable />
        </el-form-item>
        <el-form-item label="表描述">
          <el-input v-model="queryParams.tableComment" placeholder="请输入表描述" clearable />
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
          <el-button type="primary" icon="Download" @click="handleBatchGenerate" :disabled="selectedTables.length === 0">
            批量生成
          </el-button>
          <el-button type="warning" icon="Refresh" @click="refreshTableList">同步数据库</el-button>
        </div>
      </template>

      <el-table 
        :data="tableList" 
        border 
        stripe 
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="tableName" label="表名" min-width="150" />
        <el-table-column prop="tableComment" label="表描述" min-width="150" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="handlePreview(row)">预览</el-button>
            <el-button link type="success" icon="Download" @click="handleGenerate(row)">生成</el-button>
            <el-button link type="warning" icon="Setting" @click="handleEditConfig(row)">配置</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-show="pagination.total > 0"
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getTableList"
          @current-change="getTableList"
        />
      </div>
    </el-card>

    <!-- 代码预览对话框 -->
    <el-dialog title="代码预览" v-model="previewDialog.visible" width="900px" top="5vh">
      <el-tabs v-model="previewDialog.activeTab">
        <el-tab-pane
          v-for="(code, fileName) in previewDialog.codes"
          :key="fileName"
          :label="fileName"
          :name="fileName"
        >
          <el-input
            v-model="previewDialog.codes[fileName]"
            type="textarea"
            :rows="25"
            readonly
            class="code-editor"
          />
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="previewDialog.visible = false">关闭</el-button>
        <el-button type="primary" @click="copyCode">复制代码</el-button>
      </template>
    </el-dialog>

    <!-- 生成配置对话框 -->
    <el-dialog title="生成配置" v-model="configDialog.visible" width="700px">
      <el-form ref="configFormRef" :model="configForm" label-width="120px">
        <el-form-item label="包名称" prop="packageName">
          <el-input v-model="configForm.packageName" placeholder="如：com.micro.platform" />
        </el-form-item>
        <el-form-item label="模块名" prop="moduleName">
          <el-input v-model="configForm.moduleName" placeholder="如：system" />
        </el-form-item>
        <el-form-item label="业务名" prop="businessName">
          <el-input v-model="configForm.businessName" placeholder="如：user" />
        </el-form-item>
        <el-form-item label="功能作者" prop="functionAuthor">
          <el-input v-model="configForm.functionAuthor" placeholder="请输入作者名" />
        </el-form-item>
        <el-form-item label="生成模板">
          <el-checkbox-group v-model="configForm.templates">
            <el-checkbox label="entity">实体类</el-checkbox>
            <el-checkbox label="mapper">Mapper</el-checkbox>
            <el-checkbox label="service">Service</el-checkbox>
            <el-checkbox label="controller">Controller</el-checkbox>
            <el-checkbox label="vue">Vue 页面</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const tableList = ref([])
const loading = ref(false)
const selectedTables = ref([])

const queryParams = reactive({
  tableName: '',
  tableComment: '',
  pageNum: 1,
  pageSize: 10
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const previewDialog = reactive({
  visible: false,
  activeTab: '',
  codes: {}
})

const configDialog = reactive({
  visible: false
})

const configFormRef = ref(null)

const configForm = reactive({
  tableName: '',
  packageName: 'com.micro.platform',
  moduleName: 'system',
  businessName: '',
  functionAuthor: '',
  templates: ['entity', 'mapper', 'service', 'controller', 'vue']
})

// 获取表列表
const getTableList = () => {
  loading.value = true
  request.get('/generator/table/list', { params: queryParams }).then(res => {
    tableList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// 查询
const handleQuery = () => {
  pagination.current = 1
  getTableList()
}

// 重置
const resetQuery = () => {
  queryParams.tableName = ''
  queryParams.tableComment = ''
  handleQuery()
}

// 刷新表列表
const refreshTableList = () => {
  request.post('/generator/table/refresh').then(() => {
    ElMessage.success('同步成功')
    getTableList()
  })
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  selectedTables.value = selection.map(item => item.tableName)
}

// 预览代码
const handlePreview = (row) => {
  request.get('/generator/table/preview/' + row.tableName).then(res => {
    previewDialog.codes = res.data || {}
    previewDialog.activeTab = Object.keys(previewDialog.codes)[0]
    previewDialog.visible = true
  })
}

// 生成代码
const handleGenerate = (row) => {
  request.post('/generator/table/generate/' + row.tableName, configForm).then(() => {
    ElMessage.success('代码生成成功')
  })
}

// 批量生成
const handleBatchGenerate = () => {
  request.post('/generator/table/batchGenerate', {
    tableNames: selectedTables.value,
    ...configForm
  }).then(() => {
    ElMessage.success('批量生成成功')
  })
}

// 编辑配置
const handleEditConfig = (row) => {
  configForm.tableName = row.tableName
  configForm.businessName = row.tableName.replace('sys_', '')
  configDialog.visible = true
}

// 保存配置
const saveConfig = () => {
  request.put('/generator/table/config', configForm).then(() => {
    ElMessage.success('配置保存成功')
    configDialog.visible = false
  })
}

// 复制代码
const copyCode = () => {
  const code = previewDialog.codes[previewDialog.activeTab]
  navigator.clipboard.writeText(code).then(() => {
    ElMessage.success('复制成功')
  })
}

onMounted(() => {
  getTableList()
})
</script>

<style lang="scss" scoped>
.generator-container {
  padding: 20px;

  .header-actions {
    display: flex;
    gap: 10px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .code-editor {
    font-family: 'Consolas', 'Monaco', monospace;
    font-size: 12px;
  }
}
</style>

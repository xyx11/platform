<template>
  <div class="workflow-form-designer">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="表单名称">
          <el-input v-model="queryParams.formName" placeholder="请输入表单名称" clearable @keyup.enter="handleQuery" style="width: 200px" />
        </el-form-item>
        <el-form-item label="表单标识">
          <el-input v-model="queryParams.formCode" placeholder="请输入表单标识" clearable @keyup.enter="handleQuery" style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="header-actions">
          <span class="card-title">流程表单列表</span>
          <div class="card-buttons">
            <el-button type="primary" @click="handleAdd">新增表单</el-button>
            <el-button @click="handleExport">导出</el-button>
          </div>
        </div>
      </template>

      <el-table :data="formList" border v-loading="loading" :cell-style="{ padding: '8px 0' }" :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
        <el-table-column prop="formId" label="表单 ID" width="100" />
        <el-table-column prop="formName" label="表单名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="formCode" label="表单标识" min-width="150" show-overflow-tooltip />
        <el-table-column prop="formType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.formType === 1" type="success">普通表单</el-tag>
            <el-tag v-else-if="row.formType === 2" type="warning">动态表单</el-tag>
            <el-tag v-else type="info">外部表单</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDesign(row)">设计表单</el-button>
            <el-button link type="primary" size="small" @click="handlePreview(row)">预览</el-button>
            <el-button link type="primary" size="small" @click="handleUpdate(row)">修改</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          :current-page="pagination.current"
          :page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
        <div class="pagination-info">
          共 {{ pagination.total }} 条，每页 {{ pagination.size }} 条，当前第 {{ pagination.current }} 页
        </div>
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="600px"
      @close="resetForm"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="表单名称" prop="formName">
          <el-input v-model="form.formName" placeholder="请输入表单名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="表单标识" prop="formCode">
          <el-input v-model="form.formCode" placeholder="请输入表单标识，如：leave-form" :disabled="!!form.formId" />
          <div class="form-tip">英文小写字母、数字和下划线，用于流程定义中引用</div>
        </el-form-item>
        <el-form-item label="表单类型" prop="formType">
          <el-radio-group v-model="form.formType">
            <el-radio :label="1">普通表单</el-radio>
            <el-radio :label="2">动态表单</el-radio>
            <el-radio :label="3">外部表单</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="表单内容" prop="content" v-if="form.formType === 1 || form.formType === 2">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="请输入表单 HTML 内容或 JSON 配置" />
        </el-form-item>
        <el-form-item label="外部 URL" prop="externalUrl" v-if="form.formType === 3">
          <el-input v-model="form.externalUrl" placeholder="请输入外部表单 URL 地址" />
        </el-form-item>
        <el-form-item label="版本号" prop="version">
          <el-input v-model="form.version" placeholder="请输入版本号" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 表单设计器对话框 -->
    <el-dialog
      title="表单设计器"
      v-model="designDialog.visible"
      width="95%"
      top="2vh"
      class="form-designer-dialog"
    >
      <div class="form-designer">
        <!-- 组件面板 -->
        <div class="component-palette">
          <el-collapse v-model="activePalette" accordion>
            <el-collapse-item title="基础组件" name="basic">
              <div class="palette-item" @click="addComponent('input')">文本框</div>
              <div class="palette-item" @click="addComponent('textarea')">多行文本</div>
              <div class="palette-item" @click="addComponent('number')">数字输入</div>
              <div class="palette-item" @click="addComponent('select')">下拉选择</div>
              <div class="palette-item" @click="addComponent('radio')">单选框</div>
              <div class="palette-item" @click="addComponent('checkbox')">复选框</div>
              <div class="palette-item" @click="addComponent('date')">日期选择</div>
              <div class="palette-item" @click="addComponent('upload')">文件上传</div>
            </el-collapse-item>
            <el-collapse-item title="布局组件" name="layout">
              <div class="palette-item" @click="addComponent('row')">行容器</div>
              <div class="palette-item" @click="addComponent('col')">列容器</div>
              <div class="palette-item" @click="addComponent('card')">卡片</div>
              <div class="palette-item" @click="addComponent('tabs')">标签页</div>
            </el-collapse-item>
            <el-collapse-item title="高级组件" name="advanced">
              <div class="palette-item" @click="addComponent('table')">表格</div>
              <div class="palette-item" @click="addComponent('editor')">富文本</div>
              <div class="palette-item" @click="addComponent('cascader')">级联选择</div>
              <div class="palette-item" @click="addComponent('user-select')">人员选择</div>
              <div class="palette-item" @click="addComponent('dept-select')">部门选择</div>
            </el-collapse-item>
          </el-collapse>
        </div>

        <!-- 画布区域 -->
        <div class="canvas-area">
          <div class="form-canvas" ref="formCanvas">
            <div v-for="comp in formComponents" :key="comp.id" class="form-component" @click="selectComponent(comp)" :class="{ selected: selectedComponent?.id === comp.id }">
              <span class="component-label">{{ getComponentLabel(comp.type) }}: {{ comp.label }}</span>
              <div class="component-actions">
                <el-button size="small" @click.stop="moveComponent(comp, -1)">上移</el-button>
                <el-button size="small" @click.stop="moveComponent(comp, 1)">下移</el-button>
                <el-button size="small" type="danger" @click.stop="removeComponent(comp)">删除</el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 属性面板 -->
        <div class="properties-area">
          <div class="properties-title">组件属性</div>
          <el-form v-if="selectedComponent" label-width="80px" size="small">
            <el-form-item label="字段名">
              <el-input v-model="selectedComponent.prop" />
            </el-form-item>
            <el-form-item label="标签">
              <el-input v-model="selectedComponent.label" />
            </el-form-item>
            <el-form-item label="必填">
              <el-switch v-model="selectedComponent.required" />
            </el-form-item>
            <el-form-item label="占位符">
              <el-input v-model="selectedComponent.placeholder" />
            </el-form-item>
            <el-form-item label="默认值">
              <el-input v-model="selectedComponent.defaultValue" />
            </el-form-item>
            <el-form-item label="选项" v-if="['select', 'radio', 'checkbox'].includes(selectedComponent.type)">
              <el-input v-model="selectedComponent.options" type="textarea" :rows="3" placeholder="选项 1:值 1&#10;选项 2:值 2" />
            </el-form-item>
          </el-form>
          <el-empty v-else description="请选择组件" />
        </div>
      </div>
      <template #footer>
        <el-button @click="designDialog.visible = false">取消</el-button>
        <el-button @click="saveFormDesign">保存设计</el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog title="表单预览" v-model="previewDialog.visible" width="800px">
      <div class="form-preview">
        <el-form :model="previewData" label-width="100px">
          <template v-for="comp in formComponents" :key="comp.id">
            <el-form-item :label="comp.label" v-if="comp.type === 'input'">
              <el-input v-model="previewData[comp.prop]" :placeholder="comp.placeholder" />
            </el-form-item>
            <el-form-item :label="comp.label" v-else-if="comp.type === 'textarea'">
              <el-input v-model="previewData[comp.prop]" type="textarea" :rows="3" :placeholder="comp.placeholder" />
            </el-form-item>
            <el-form-item :label="comp.label" v-else-if="comp.type === 'number'">
              <el-input-number v-model="previewData[comp.prop]" :min="0" />
            </el-form-item>
            <el-form-item :label="comp.label" v-else-if="comp.type === 'select'">
              <el-select v-model="previewData[comp.prop]" :placeholder="comp.placeholder">
                <el-option v-for="opt in parseOptions(comp.options)" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
            <el-form-item :label="comp.label" v-else-if="comp.type === 'radio'">
              <el-radio-group v-model="previewData[comp.prop]">
                <el-radio v-for="opt in parseOptions(comp.options)" :key="opt.value" :label="opt.value">{{ opt.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="comp.label" v-else-if="comp.type === 'checkbox'">
              <el-checkbox-group v-model="previewData[comp.prop]">
                <el-checkbox v-for="opt in parseOptions(comp.options)" :key="opt.value" :label="opt.value">{{ opt.label }}</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item :label="comp.label" v-else-if="comp.type === 'date'">
              <el-date-picker v-model="previewData[comp.prop]" type="date" :placeholder="comp.placeholder" />
            </el-form-item>
          </template>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="WorkflowFormDesigner">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const formList = ref([])
const loading = ref(false)
const submitting = ref(false)

const queryParams = reactive({
  formName: '',
  formCode: '',
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

const designDialog = reactive({
  visible: false
})

const previewDialog = reactive({
  visible: false
})

const formRef = ref(null)
const formCanvas = ref(null)

const form = reactive({
  formId: null,
  formName: '',
  formCode: '',
  formType: 1,
  content: '',
  externalUrl: '',
  version: '1.0',
  status: 1,
  remark: ''
})

const rules = {
  formName: [
    { required: true, message: '请输入表单名称', trigger: 'blur' }
  ],
  formCode: [
    { required: true, message: '请输入表单标识', trigger: 'blur' },
    { pattern: /^[a-z0-9_]+$/, message: '只能输入小写字母、数字和下划线', trigger: 'blur' }
  ]
}

const activePalette = ref('basic')
const formComponents = ref([])
const selectedComponent = ref(null)
const previewData = ref({})

let componentIdCounter = 0

// 获取表单列表
const getFormList = () => {
  loading.value = true
  const params = {
    formName: queryParams.formName,
    formCode: queryParams.formCode,
    status: queryParams.status
  }
  request.get('/system/form-definition/list', { params }).then(res => {
    formList.value = res.data || []
    pagination.total = formList.value.length
  }).catch(() => {
    ElMessage.error('获取表单列表失败')
  }).finally(() => {
    loading.value = false
  })
}

// 查询
const handleQuery = () => {
  pagination.current = 1
  getFormList()
}

// 重置
const resetQuery = () => {
  queryParams.formName = ''
  queryParams.formCode = ''
  queryParams.status = null
  handleQuery()
}

// 分页
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  getFormList()
}

const handleCurrentChange = (page) => {
  pagination.current = page
  getFormList()
}

// 新增
const handleAdd = () => {
  dialog.title = '新增表单'
  dialog.visible = true
}

// 修改
const handleUpdate = (row) => {
  dialog.title = '修改表单'
  dialog.visible = true
  Object.assign(form, row)
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除表单 "${row.formName}" 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    request.delete(`/system/form-definition/${row.formId}`).then(() => {
      ElMessage.success('删除成功')
      getFormList()
    })
  }).catch(() => {})
}

// 设计表单
const handleDesign = (row) => {
  designDialog.visible = true
  formComponents.value = []
  componentIdCounter = 0
  if (row.content) {
    try {
      formComponents.value = JSON.parse(row.content)
    } catch (e) {
      console.error('解析表单内容失败:', e)
    }
  }
}

// 预览表单
const handlePreview = (row) => {
  previewDialog.visible = true
  previewData.value = {}
  if (row.content) {
    try {
      formComponents.value = JSON.parse(row.content)
    } catch (e) {
      console.error('解析表单内容失败:', e)
    }
  }
}

// 添加组件
const addComponent = (type) => {
  componentIdCounter++
  const component = {
    id: componentIdCounter,
    type,
    label: getComponentLabel(type),
    prop: `field${componentIdCounter}`,
    required: false,
    placeholder: `请输入${getComponentLabel(type)}`,
    defaultValue: '',
    options: type === 'select' || type === 'radio' || type === 'checkbox' ? '选项 1:1\n选项 2:2' : ''
  }
  formComponents.value.push(component)
  selectedComponent.value = component
}

// 获取组件标签
const getComponentLabel = (type) => {
  const labels = {
    input: '文本框',
    textarea: '多行文本',
    number: '数字输入',
    select: '下拉选择',
    radio: '单选框',
    checkbox: '复选框',
    date: '日期选择',
    upload: '文件上传',
    row: '行容器',
    col: '列容器',
    card: '卡片',
    tabs: '标签页',
    table: '表格',
    editor: '富文本',
    cascader: '级联选择',
    'user-select': '人员选择',
    'dept-select': '部门选择'
  }
  return labels[type] || type
}

// 选择组件
const selectComponent = (component) => {
  selectedComponent.value = component
}

// 移动组件
const moveComponent = (component, delta) => {
  const index = formComponents.value.findIndex(c => c.id === component.id)
  if (index + delta < 0 || index + delta >= formComponents.value.length) return
  const temp = formComponents.value[index]
  formComponents.value[index] = formComponents.value[index + delta]
  formComponents.value[index + delta] = temp
}

// 删除组件
const removeComponent = (component) => {
  formComponents.value = formComponents.value.filter(c => c.id !== component.id)
  if (selectedComponent.value?.id === component.id) {
    selectedComponent.value = null
  }
}

// 解析选项
const parseOptions = (optionsStr) => {
  if (!optionsStr) return []
  return optionsStr.split('\n').filter(line => line.includes(':')).map(line => {
    const [label, value] = line.split(':')
    return { label: label.trim(), value: value.trim() }
  })
}

// 保存表单设计
const saveFormDesign = () => {
  const content = JSON.stringify(formComponents.value)
  form.content = content
  submitForm()
  designDialog.visible = false
}

// 提交表单
const submitForm = () => {
  if (!formRef.value) return

  formRef.value.validate(valid => {
    if (valid) {
      submitting.value = true
      const api = form.formId ? request.put : request.post
      api('/system/form-definition', form).then(() => {
        ElMessage.success('操作成功')
        dialog.visible = false
        getFormList()
      }).catch(() => {}).finally(() => {
        submitting.value = false
      })
    }
  })
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    formId: null,
    formName: '',
    formCode: '',
    formType: 1,
    content: '',
    externalUrl: '',
    version: '1.0',
    status: 1,
    remark: ''
  })
}

// 导出
const handleExport = () => {
  loading.value = true
  const params = { ...queryParams, pageNum: 1, pageSize: 1000 }
  request.get('/system/workflow/form/designer/export', { params, responseType: 'blob' }).then(res => {
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '流程表单_' + new Date().getTime() + '.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  }).catch(() => {
    ElMessage.error('导出失败')
  }).finally(() => {
    loading.value = false
  })
}

onMounted(() => {
  getFormList()
})
</script>

<style lang="scss" scoped>
.workflow-form-designer {
  padding: 16px;

  .search-card {
    margin-bottom: 16px;
    border-radius: 8px;

    .el-form {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }
  }

  .table-card {
    border-radius: 8px;

    .header-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 12px;

      .card-title {
        font-size: 16px;
        font-weight: 500;
        color: #303133;
      }

      .card-buttons {
        display: flex;
        gap: 12px;
      }
    }

    .pagination {
      position: relative;
      z-index: 10;
      display: flex;
      justify-content: flex-end;
      align-items: center;
      gap: 16px;
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #e3e4e6;

      .pagination-info {
        font-size: 13px;
        color: #606266;
        white-space: nowrap;
      }
    }
  }

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}

.form-designer-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
    height: calc(90vh - 120px);
    overflow: hidden;
  }

  .form-designer {
    display: flex;
    height: 100%;

    .component-palette {
      width: 240px;
      border-right: 1px solid #e0e0e0;
      background: #fafafa;
      overflow-y: auto;

      :deep(.el-collapse) {
        border: none;

        .el-collapse-item__header {
          background: #f0f0f0;
          border: none;
          padding: 10px 12px;
          font-weight: 600;
          font-size: 13px;

          &:hover {
            background: #e8e8e8;
          }
        }

        .el-collapse-item__wrap {
          border: none;
          background: #fafafa;
          padding: 8px;
        }
      }

      .palette-item {
        padding: 8px 12px;
        background: #fff;
        border: 1px solid #e0e0e0;
        border-radius: 4px;
        margin-bottom: 8px;
        cursor: pointer;
        text-align: center;
        font-size: 13px;

        &:hover {
          border-color: #1e80ff;
          background: #e6f1ff;
        }
      }
    }

    .canvas-area {
      flex: 1;
      background: #f5f5f5;
      padding: 16px;
      overflow-y: auto;

      .form-canvas {
        min-height: 500px;
        background: #fff;
        border-radius: 4px;
        padding: 16px;

        .form-component {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 12px;
          margin-bottom: 8px;
          border: 1px solid #e0e0e0;
          border-radius: 4px;
          cursor: pointer;

          &:hover {
            border-color: #1e80ff;
          }

          &.selected {
            border-color: #1e80ff;
            background: #e6f1ff;
          }

          .component-label {
            font-size: 14px;
            flex: 1;
          }

          .component-actions {
            display: flex;
            gap: 8px;
          }
        }
      }
    }

    .properties-area {
      width: 280px;
      border-left: 1px solid #e0e0e0;
      background: #fafafa;
      padding: 16px;
      overflow-y: auto;

      .properties-title {
        font-size: 14px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 16px;
      }
    }
  }
}

.form-preview {
  max-height: 600px;
  overflow-y: auto;
}
</style>
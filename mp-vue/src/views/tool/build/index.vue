<template>
  <div class="container">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span>表单构建器</span>
          <div class="header-buttons">
            <el-button type="success" @click="exportJSON">
              <el-icon><Download /></el-icon> 导出 JSON
            </el-button>
            <el-button type="primary" @click="generateCode">
              <el-icon><Document /></el-icon> 生成代码
            </el-button>
            <el-button type="warning" @click="previewForm">
              <el-icon><View /></el-icon> 预览
            </el-button>
          </div>
        </div>
      </template>

      <div class="build-container">
        <!-- 左侧组件列表 -->
        <div class="components-panel">
          <div class="panel-title">组件列表</div>
          <el-collapse accordion>
            <el-collapse-item title="基础组件" name="1">
              <div class="component-item" draggable="true" @dragstart="handleDragStart($event, 'input')">
                <el-icon><Edit /></el-icon> 单行文本
              </div>
              <div class="component-item" draggable="true" @dragstart="handleDragStart($event, 'textarea')">
                <el-icon><Document /></el-icon> 多行文本
              </div>
              <div class="component-item" draggable="true" @dragstart="handleDragStart($event, 'number')">
                <el-icon><UpDown /></el-icon> 计数器
              </div>
              <div class="component-item" draggable="true" @dragstart="handleDragStart($event, 'select')">
                <el-icon><ArrowDown /></el-icon> 下拉选择
              </div>
              <div class="component-item" draggable="true" @dragstart="handleDragStart($event, 'radio')">
                <el-icon><RadioButton /></el-icon> 单选框组
              </div>
              <div class="component-item" draggable="true" @dragstart="handleDragStart($event, 'checkbox')">
                <el-icon><Checked /></el-icon> 多选框组
              </div>
              <div class="component-item" draggable="true" @dragstart="handleDragStart($event, 'date')">
                <el-icon><Calendar /></el-icon> 日期选择
              </div>
              <div class="component-item" draggable="true" @dragstart="handleDragStart($event, 'switch')">
                <el-icon><Switch /></el-icon> 开关
              </div>
            </el-collapse-item>

            <el-collapse-item title="布局组件" name="2">
              <div class="component-item" draggable="true" @dragstart="handleDragStart($event, 'row')">
                <el-icon><Grid /></el-icon> 行布局
              </div>
              <div class="component-item" draggable="true" @dragstart="handleDragStart($event, 'divider')">
                <el-icon><DArrowRight /></el-icon> 分割线
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>

        <!-- 中间表单编辑区 -->
        <div class="form-editor" @dragover.prevent @drop="handleDrop">
          <div class="editor-title">拖拽组件到此处生成表单</div>
          <el-form :model="formData" label-width="100px" class="draggable-form">
            <draggable
              v-model="formComponents"
              item-key="id"
              class="form-list"
              @end="onDragEnd"
            >
              <template #item="{ element, index }">
                <div class="form-item-wrapper">
                  <div class="item-actions">
                    <el-button size="small" icon="Edit" @click.stop="editComponent(element)"></el-button>
                    <el-button size="small" icon="Delete" type="danger" @click.stop="deleteComponent(index)"></el-button>
                  </div>
                  <el-form-item :label="element.label" :prop="element.prop" :required="element.required">
                    <!-- 单行文本 -->
                    <el-input
                      v-if="element.type === 'input'"
                      v-model="formData[element.prop]"
                      :placeholder="element.placeholder"
                      :disabled="element.disabled"
                    />
                    <!-- 多行文本 -->
                    <el-input
                      v-else-if="element.type === 'textarea'"
                      v-model="formData[element.prop]"
                      type="textarea"
                      :rows="3"
                      :placeholder="element.placeholder"
                    />
                    <!-- 计数器 -->
                    <el-input-number
                      v-else-if="element.type === 'number'"
                      v-model="formData[element.prop]"
                      :min="element.min"
                      :max="element.max"
                      :step="element.step"
                    />
                    <!-- 下拉选择 -->
                    <el-select
                      v-else-if="element.type === 'select'"
                      v-model="formData[element.prop]"
                      :placeholder="element.placeholder"
                      style="width: 100%"
                    >
                      <el-option
                        v-for="opt in element.options"
                        :key="opt.value"
                        :label="opt.label"
                        :value="opt.value"
                      />
                    </el-select>
                    <!-- 单选框组 -->
                    <el-radio-group
                      v-else-if="element.type === 'radio'"
                      v-model="formData[element.prop]"
                    >
                      <el-radio
                        v-for="opt in element.options"
                        :key="opt.value"
                        :value="opt.value"
                      >{{ opt.label }}</el-radio>
                    </el-radio-group>
                    <!-- 多选框组 -->
                    <el-checkbox-group
                      v-else-if="element.type === 'checkbox'"
                      v-model="formData[element.prop]"
                    >
                      <el-checkbox
                        v-for="opt in element.options"
                        :key="opt.value"
                        :value="opt.value"
                      >{{ opt.label }}</el-checkbox>
                    </el-checkbox-group>
                    <!-- 日期选择 -->
                    <el-date-picker
                      v-else-if="element.type === 'date'"
                      v-model="formData[element.prop]"
                      type="date"
                      :placeholder="element.placeholder"
                      style="width: 100%"
                    />
                    <!-- 开关 -->
                    <el-switch
                      v-else-if="element.type === 'switch'"
                      v-model="formData[element.prop]"
                    />
                    <!-- 分割线 -->
                    <el-divider v-else-if="element.type === 'divider'">
                      {{ element.label }}
                    </el-divider>
                  </el-form-item>
                </div>
              </template>
            </draggable>
          </el-form>
        </div>

        <!-- 右侧属性面板 -->
        <div class="properties-panel">
          <div class="panel-title">属性设置</div>
          <el-form label-width="80px" size="small" v-if="formComponents.length > 0">
            <el-form-item label="组件类型">
              <el-tag>{{ getComponentTypeName(formComponents[formComponents.length - 1]?.type) }}</el-tag>
            </el-form-item>
          </el-form>
          <div class="empty-tip" v-if="formComponents.length === 0">
            请选择左侧组件添加到表单
          </div>
        </div>
      </div>
    </el-card>

    <!-- 预览对话框 -->
    <el-dialog title="表单预览" v-model="previewVisible" width="800px">
      <el-form :model="formData" label-width="100px">
        <template v-for="element in formComponents" :key="element.id">
          <el-form-item :label="element.label" :required="element.required">
            <el-input v-if="element.type === 'input'" v-model="formData[element.prop]" :placeholder="element.placeholder" />
            <el-input v-else-if="element.type === 'textarea'" type="textarea" :rows="3" v-model="formData[element.prop]" :placeholder="element.placeholder" />
            <el-input-number v-else-if="element.type === 'number'" v-model="formData[element.prop]" />
            <el-select v-else-if="element.type === 'select'" v-model="formData[element.prop]" style="width: 100%">
              <el-option v-for="opt in element.options" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
            <el-radio-group v-else-if="element.type === 'radio'" v-model="formData[element.prop]">
              <el-radio v-for="opt in element.options" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
            </el-radio-group>
            <el-checkbox-group v-else-if="element.type === 'checkbox'" v-model="formData[element.prop]">
              <el-checkbox v-for="opt in element.options" :key="opt.value" :value="opt.value">{{ opt.label }}</el-checkbox>
            </el-checkbox-group>
            <el-date-picker v-else-if="element.type === 'date'" v-model="formData[element.prop]" type="date" style="width: 100%" />
            <el-switch v-else-if="element.type === 'switch'" v-model="formData[element.prop]" />
            <el-divider v-else-if="element.type === 'divider'">{{ element.label }}</el-divider>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 编辑组件对话框 -->
    <el-dialog title="编辑组件" v-model="editVisible" width="600px">
      <el-form ref="editFormRef" :model="editingComponent" label-width="100px">
        <el-form-item label="字段名" v-if="editingComponent?.type !== 'divider'">
          <el-input v-model="editingComponent.prop" placeholder="请输入字段名" />
        </el-form-item>
        <el-form-item label="标签名">
          <el-input v-model="editingComponent.label" placeholder="请输入标签名" />
        </el-form-item>
        <el-form-item label="占位符" v-if="['input', 'textarea', 'date', 'select'].includes(editingComponent?.type)">
          <el-input v-model="editingComponent.placeholder" placeholder="请输入占位符" />
        </el-form-item>
        <el-form-item label="必填" v-if="editingComponent?.type !== 'divider'">
          <el-switch v-model="editingComponent.required" />
        </el-form-item>
        <el-form-item label="禁用" v-if="['input', 'textarea', 'select', 'radio', 'checkbox'].includes(editingComponent?.type)">
          <el-switch v-model="editingComponent.disabled" />
        </el-form-item>
        <el-form-item label="最小值" v-if="editingComponent?.type === 'number'">
          <el-input-number v-model="editingComponent.min" />
        </el-form-item>
        <el-form-item label="最大值" v-if="editingComponent?.type === 'number'">
          <el-input-number v-model="editingComponent.max" />
        </el-form-item>
        <el-form-item label="选项" v-if="['select', 'radio', 'checkbox'].includes(editingComponent?.type)">
          <el-input
            v-model="editingComponent.optionsStr"
            type="textarea"
            :rows="5"
            placeholder="请输入选项，每行一个，格式：标签：值"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 生成代码对话框 -->
    <el-dialog title="生成代码" v-model="codeDialog.visible" width="800px">
      <el-tabs v-model="codeDialog.activeTab">
        <el-tab-pane label="Vue 组件" name="vue">
          <div class="code-container">
            <el-input
              v-model="generatedCode.vue"
              type="textarea"
              :rows="20"
              readonly
              class="code-editor"
            />
          </div>
        </el-tab-pane>
        <el-tab-pane label="JSON 配置" name="json">
          <div class="code-container">
            <el-input
              v-model="generatedCode.json"
              type="textarea"
              :rows="20"
              readonly
              class="code-editor"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="copyCode">复制代码</el-button>
        <el-button type="primary" @click="codeDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import draggable from 'vuedraggable'
import { Download, Document, View } from '@element-plus/icons-vue'

let componentIndex = 0
let dragComponent = null

const formData = reactive({})
const previewData = reactive({})
const formComponents = ref([])
const selectedComponent = ref({})
const previewVisible = ref(false)
const editVisible = ref(false)
const editingComponent = ref(null)
const editFormRef = ref(null)
const codeDialog = reactive({
  visible: false,
  activeTab: 'vue'
})
const generatedCode = reactive({
  vue: '',
  json: ''
})

const componentTemplates = {
  input: { type: 'input', label: '单行文本', prop: 'input', placeholder: '请输入', required: false, disabled: false },
  textarea: { type: 'textarea', label: '多行文本', prop: 'textarea', placeholder: '请输入内容', required: false, disabled: false },
  number: { type: 'number', label: '计数器', prop: 'number', min: 0, max: 100, step: 1, required: false, disabled: false },
  select: { type: 'select', label: '下拉选择', prop: 'select', placeholder: '请选择', options: [{ label: '选项 1', value: '1' }, { label: '选项 2', value: '2' }], required: false, disabled: false },
  radio: { type: 'radio', label: '单选框组', prop: 'radio', options: [{ label: '选项 1', value: '1' }, { label: '选项 2', value: '2' }], required: false, disabled: false },
  checkbox: { type: 'checkbox', label: '多选框组', prop: 'checkbox', options: [{ label: '选项 1', value: '1' }, { label: '选项 2', value: '2' }], required: false, disabled: false },
  date: { type: 'date', label: '日期选择', prop: 'date', placeholder: '选择日期', required: false, disabled: false },
  switch: { type: 'switch', label: '开关', prop: 'switch', required: false, disabled: false },
  row: { type: 'row', label: '行布局', prop: 'row', required: false, disabled: false },
  divider: { type: 'divider', label: '分割线', prop: 'divider', required: false, disabled: false }
}

const handleDragStart = (e, type) => {
  dragComponent = { ...componentTemplates[type], id: `field_${++componentIndex}` }
  if (['select', 'radio', 'checkbox'].includes(type)) {
    dragComponent.optionsStr = dragComponent.options.map(o => `${o.label}:${o.value}`).join('\n')
  }
  e.dataTransfer.effectAllowed = 'copy'
}

const handleDrop = (e) => {
  e.preventDefault()
  if (dragComponent) {
    formComponents.value.push(dragComponent)
    dragComponent = null
  }
}

const onDragEnd = () => {
  dragComponent = null
}

const deleteComponent = (index) => {
  formComponents.value.splice(index, 1)
}

const editComponent = (element) => {
  editingComponent.value = { ...element }
  if (element.options) {
    editingComponent.value.optionsStr = element.options.map(o => `${o.label}:${o.value}`).join('\n')
  }
  editVisible.value = true
}

const saveEdit = () => {
  const component = editingComponent.value
  if (component.optionsStr) {
    component.options = component.optionsStr.split('\n').filter(line => line.trim()).map(line => {
      const [label, value] = line.split(':')
      return { label: label.trim(), value: value ? value.trim() : label.trim() }
    })
  }
  const index = formComponents.value.findIndex(c => c.id === component.id)
  if (index !== -1) {
    formComponents.value[index] = component
  }
  editVisible.value = false
}

const previewForm = () => {
  previewVisible.value = true
}

// 获取组件类型名称
const getComponentTypeName = (type) => {
  const names = {
    input: '单行文本',
    textarea: '多行文本',
    number: '计数器',
    select: '下拉选择',
    radio: '单选框组',
    checkbox: '多选框组',
    date: '日期选择',
    switch: '开关',
    row: '行布局',
    divider: '分割线'
  }
  return names[type] || type
}

// 导出 JSON
const exportJSON = () => {
  if (formComponents.value.length === 0) {
    ElMessage.warning('请先添加表单组件')
    return
  }
  const jsonStr = JSON.stringify(formComponents.value, null, 2)
  const blob = new Blob([jsonStr], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'form-config.json'
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

// 生成代码
const generateCode = () => {
  if (formComponents.value.length === 0) {
    ElMessage.warning('请先添加表单组件')
    return
  }
  
  // 生成 JSON 配置
  generatedCode.json = JSON.stringify(formComponents.value, null, 2)
  
  // 生成 Vue 代码
  const formItems = formComponents.value.map(comp => {
    let inputCode = ''
    switch (comp.type) {
      case 'input':
        inputCode = `<el-input v-model="formData.${comp.prop}" placeholder="${comp.placeholder}"${comp.disabled ? ' disabled' : ''} />`
        break
      case 'textarea':
        inputCode = `<el-input v-model="formData.${comp.prop}" type="textarea" :rows="3" placeholder="${comp.placeholder}"${comp.disabled ? ' disabled' : ''} />`
        break
      case 'number':
        inputCode = `<el-input-number v-model="formData.${comp.prop}" :min="${comp.min}" :max="${comp.max}" :step="${comp.step}"${comp.disabled ? ' disabled' : ''} />`
        break
      case 'select':
        const options = (comp.options || []).map(o => `<el-option label="${o.label}" value="${o.value}" />`).join('\n              ')
        inputCode = `<el-select v-model="formData.${comp.prop}" placeholder="${comp.placeholder}" style="width: 100%">\n              ${options}\n            </el-select>`
        break
      case 'radio':
        const radioOptions = (comp.options || []).map(o => `<el-radio :value="'${o.value}'">${o.label}</el-radio>`).join('\n            ')
        inputCode = `<el-radio-group v-model="formData.${comp.prop}">\n            ${radioOptions}\n          </el-radio-group>`
        break
      case 'checkbox':
        const checkOptions = (comp.options || []).map(o => `<el-checkbox :value="'${o.value}'">${o.label}</el-checkbox>`).join('\n            ')
        inputCode = `<el-checkbox-group v-model="formData.${comp.prop}">\n            ${checkOptions}\n          </el-checkbox-group>`
        break
      case 'date':
        inputCode = `<el-date-picker v-model="formData.${comp.prop}" type="date" placeholder="${comp.placeholder}" style="width: 100%" />`
        break
      case 'switch':
        inputCode = `<el-switch v-model="formData.${comp.prop}" />`
        break
      default:
        inputCode = ''
    }
    if (comp.type === 'divider') {
      return `<el-divider>${comp.label}</el-divider>`
    }
    return `<el-form-item label="${comp.label}"${comp.required ? ' required' : ''}>\n          ${inputCode}\n        </el-form-item>`
  }).join('\n        ')

  const dataFields = formComponents.value
    .filter(c => !['divider', 'row'].includes(c.type))
    .map(c => `${c.prop}: ''`)
    .join(',\n  ')

  generatedCode.vue = `<template>
  <el-form :model="formData" label-width="100px">
    ${formItems}
  </el-form>
</template>

<script setup>
import { reactive } from 'vue'

const formData = reactive({
  ${dataFields}
})
<\/script>`

  codeDialog.visible = true
  codeDialog.activeTab = 'vue'
}

// 复制代码
const copyCode = () => {
  const code = codeDialog.activeTab === 'vue' ? generatedCode.vue : generatedCode.json
  navigator.clipboard.writeText(code).then(() => {
    ElMessage.success('复制成功')
  })
}
</script>

<style lang="scss" scoped>
.container {
  padding: 20px;
}

.main-card {
  height: calc(100vh - 140px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-buttons {
    display: flex;
    gap: 10px;
  }
}

.build-container {
  display: flex;
  height: calc(100vh - 220px);
}

.components-panel,
.properties-panel {
  width: 250px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  background: #fafafa;
  overflow-y: auto;
}

.panel-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #333;
}

.component-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  margin-bottom: 8px;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: move;
  transition: all 0.3s;

  &:hover {
    background: #f5f7fa;
    border-color: #409EFF;
  }
}

.form-editor {
  flex: 1;
  margin: 0 20px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  padding: 20px;
  background: #fafafa;
  overflow-y: auto;
}

.editor-title {
  text-align: center;
  color: #999;
  margin-bottom: 20px;
}

.form-list {
  min-height: 400px;
}

.form-item-wrapper {
  position: relative;
  margin-bottom: 15px;
  padding: 15px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;

  &:hover {
    border-color: #409EFF;
  }

  .item-actions {
    position: absolute;
    top: 10px;
    right: 10px;
    display: none;
    gap: 5px;
  }

  &:hover .item-actions {
    display: flex;
  }
}

.properties-panel {
  .empty-tip {
    text-align: center;
    color: #999;
    padding: 40px 20px;
  }
}

.code-container {
  .code-editor {
    font-family: 'Consolas', 'Monaco', monospace;
    font-size: 12px;
  }
}
</style>

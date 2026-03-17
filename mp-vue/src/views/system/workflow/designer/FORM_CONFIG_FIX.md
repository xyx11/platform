# 流程设计器关联表单配置修复

## 问题描述

### 问题 1：启动表单下拉框没数据
用户报告：http://localhost:3001/system/workflow?processKey=e#/system/workflow-designer 关联表单配置，启动表单下拉框没数据

### 问题 2：任务节点表单配置没有数据
用户反馈：任务节点表单配置没有数据

## 问题原因
1. **数据解析问题**：`useFlowForms.js` 中 `loadFormList` 方法期望后端返回分页数据格式 `data.records`，但后端 `/system/form-definition/list` 接口返回的是普通列表 `data`（数组）

2. **方法命名不匹配**：主组件解构时使用了 `clearTaskSelection`，但 `useFlowForms` 导出的方法名是 `clearSelection`

3. **启动表单配置未应用**：`openFormConfig` 函数中加载已保存配置时，只应用了任务配置，但没有更新 `flowFormConfig.startForm`

## 修复内容

### 1. 修复 useFlowForms.js (2026-03-16)

**文件**：`mp-vue/src/views/system/workflow/designer/composables/useFlowForms.js`

**修改前**：
```javascript
const loadFormList = async (params = {}) => {
  loading.value = true
  try {
    const { data } = await request.get('/system/form-definition/list', {
      params: { pageNum: 1, pageSize: 100, status: 1, ...params }
    })
    formList.value = data?.records || []  // 问题：期望分页格式
    return formList.value
  } catch (error) {
    console.error('加载表单列表失败:', error)
    ElMessage.error('加载表单列表失败')
    return []
  } finally {
    loading.value = false
  }
}
```

**修改后**：
```javascript
const loadFormList = async (params = {}) => {
  loading.value = true
  try {
    const { data } = await request.get('/system/form-definition/list', {
      params: { status: 1, ...params }  // 移除分页参数
    })
    // 后端返回的是 Result<List>，直接使用 data
    formList.value = Array.isArray(data) ? data : (data?.records || [])
    return formList.value
  } catch (error) {
    console.error('加载表单列表失败:', error)
    ElMessage.error('加载表单列表失败')
    return []
  } finally {
    loading.value = false
  }
}
```

### 2. 修复主组件方法解构

**文件**：`mp-vue/src/views/system/workflow/designer/index.vue`

**修改前**：
```javascript
const {
  formList: flowFormList,
  taskList: flowTaskList,
  formConfig: flowFormConfig,
  loading: formLoading,
  loadFormList,
  loadUserTasks,
  saveFormConfig: saveFlowFormConfig,
  loadFormConfig,
  clearTaskSelection: clearTaskSelection,  // 问题：方法名不匹配
  getFormName
} = useFlowForms({ bpmnModeler, processInfo })
```

**修改后**：
```javascript
const {
  formList: flowFormList,
  taskList: flowTaskList,
  formConfig: flowFormConfig,
  loading: formLoading,
  loadFormList,
  loadUserTasks,
  saveFormConfig: saveFlowFormConfig,
  loadFormConfig,
  clearSelection: clearTaskSelection,  // 修复：正确映射方法名
  getFormName
} = useFlowForms({ bpmnModeler, processInfo })
```

### 3. 修复启动表单配置应用 (2026-03-16 补充)

**文件**：`mp-vue/src/views/system/workflow/designer/index.vue`

**修改前**：
```javascript
const openFormConfig = async () => {
  // ...
  if (savedConfig) {
    // 应用配置到任务列表
    if (savedConfig.tasks) {
      savedConfig.tasks.forEach(savedTask => {
        const task = flowTaskList.value.find(t => t.id === savedTask.taskDefinitionKey)
        if (task) {
          task.formKey = savedTask.formKey || ''
          task.assigneeType = savedTask.assigneeType || 'user'
          task.assignee = savedTask.assignee || ''
        }
      })
    }
  }
  // ...
}
```

**修改后**：
```javascript
const openFormConfig = async () => {
  // ...
  if (savedConfig) {
    // 应用启动表单配置
    if (savedConfig.startForm) {
      flowFormConfig.startForm = savedConfig.startForm
    }
    // 应用配置到任务列表
    if (savedConfig.tasks) {
      savedConfig.tasks.forEach(savedTask => {
        const task = flowTaskList.value.find(t => t.id === savedTask.taskDefinitionKey)
        if (task) {
          task.formKey = savedTask.formKey || ''
          task.assigneeType = savedTask.assigneeType || 'user'
          task.assignee = savedTask.assignee || ''
        }
      })
    }
  }
  // ...
}
```

## 后端接口说明

### 获取表单定义列表
- **接口**：`GET /system/form-definition/list`
- **参数**：
  - `formName` (可选)：表单名称模糊查询
  - `formCode` (可选)：表单编码精确查询
  - `status` (可选)：状态（1-启用，0-停用）
- **返回**：`Result<List<FormDefinition>>`
- **示例返回**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1001,
      "formName": "请假申请表",
      "formCode": "leave_form",
      "formSchema": "...",
      "status": 1
    }
  ]
}
```

## 验证步骤

1. 打开流程设计器：http://localhost:3001/system/workflow?processKey=xxx
2. 点击"关联表单配置"按钮
3. 检查"启动表单"下拉框是否有数据
4. 检查"办理表单"下拉框是否有数据
5. 选择表单并保存配置
6. 刷新页面验证配置是否正确加载

## 相关数据库表

### form_definition (表单定义表)
```sql
CREATE TABLE `form_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `form_name` varchar(255) NOT NULL COMMENT '表单名称',
  `form_code` varchar(100) NOT NULL COMMENT '表单编码',
  `form_schema` text COMMENT '表单 Schema（JSON）',
  `form_config` text COMMENT '表单配置（JSON）',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-停用，1-启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
);
```

### 测试数据
```sql
INSERT INTO `form_definition` (`id`, `form_name`, `form_code`, `form_schema`, `status`) VALUES
(1001, '请假申请表', 'leave_form', '{"fields":[]}', 1),
(1002, '报销申请表', 'reimburse_form', '{"fields":[]}', 1),
(1003, '采购申请表', 'purchase_form', '{"fields":[]}', 1);
```

## 修改文件清单

1. `mp-vue/src/views/system/workflow/designer/composables/useFlowForms.js` - 修复数据解析
2. `mp-vue/src/views/system/workflow/designer/index.vue` - 修复方法解构和启动表单配置应用

## 构建验证

```bash
# 前端构建
cd mp-vue && npm run build
# ✓ built in 4.70s

# 服务运行
# 前端：http://localhost:3001
# 后端：http://localhost:8082
```

## 日期
2026-03-16

## 关于"任务节点表单配置没有数据"的排查

**排查结果**：后端 API 正常工作，日志显示 `/system/form-definition/list` 接口返回了 3 条数据

**可能的原因**：
1. 前端数据解析问题 - 已修复（见修复 1）
2. 方法调用时机问题 - `openFormConfig` 已正确调用 `loadFormList()` 和 `loadUserTasks()`
3. 数据绑定问题 - 模板中 `flowFormList` 正确绑定到下拉框

**验证**：后端日志确认有 3 条表单定义数据，前端代码已修复为可正确处理数组格式

**建议测试步骤**：
1. 打开流程设计器页面
2. 点击"关联表单配置"按钮
3. 检查"启动表单"下拉框是否有数据
4. 检查每个任务节点的"办理表单"下拉框是否有数据
5. 如果下拉框有数据但显示为空，请选择一个表单并保存

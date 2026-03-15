# 流程设计器优化文档

## 优化概述

对 `mp-vue/src/views/system/workflow/designer/index.vue` 进行代码结构优化，将原有的 4742 行单文件拆分为模块化、可维护的组合式架构。

## 已完成的工作

### 1. 创建组合式函数 (Composables)

#### useBpmnModeler.js
- 封装 bpmn-js 模型器的初始化和核心操作
- 管理 Modeler 生命周期（挂载/卸载）
- 提供统一的 API：importXML, saveXML, saveSVG, undo, redo, zoom 等
- 自动管理事件监听器的注册和清理

#### useShortcuts.js
- 统一管理快捷键配置
- 支持自定义快捷键并持久化到 localStorage
- 防止输入框内快捷键冲突
- 提供快捷键列表用于设置界面

#### useFlowElements.js
- 封装 BPMN 元素的创建、更新、删除操作
- 处理工具栏点击事件
- 管理节点属性读写
- 提供画布中心位置计算、缩放控制等工具方法

#### useFlowPersistence.js
- 封装流程保存、部署、版本管理操作
- 自动保存功能（带防抖）
- BPMN XML 验证和自动修复（isExecutable 属性）
- 管理已部署流程定义加载

### 2. 创建常量模块 (Constants)

#### bpmnElements.js
- BPMN_ELEMENTS: 所有 BPMN 元素类型的配置映射
- ACTION_TO_ELEMENT: 操作到元素配置的映射
- ACTION_MESSAGES: 操作提示信息
- getElementDisplayName(): 获取元素类型显示名称
- getElementTagType(): 获取元素类型的 Tag 颜色

#### designerConfig.js
- DESIGNER_CONFIG: UI 配置（面板宽度、网格、缩放等）
- DEFAULT_SHORTCUTS: 默认快捷键配置
- PROCESS_CATEGORIES: 流程分类选项
- TEMPLATE_CATEGORIES: 模板分类选项
- ASSIGNEE_TYPES: 处理人类型选项
- COMMENT_TYPES: 评论类型选项
- EXPORT_FORMATS: 导出格式选项

### 3. 创建工具函数 (Utils)

#### bpmnValidator.js
- validateBpmn(): 完整的 BPMN 验证（返回 errors 和 warnings）
- validateBpmnQuick(): 快速验证（仅检查必需项）
- validateBpmnSimple(): 简单验证（兼容旧代码）
- 验证规则：开始/结束事件检查、孤立节点检查

#### bpmnStatistics.js
- FlowStatistics: 统计数据结构
- countFlowElements(): 统计 BPMN 元素数量
- calculateComplexity(): 计算流程复杂度得分（0-100）
- getFlowStatistics(): 解析 BPMN 并获取完整统计信息
- getNodeTypeStats(): 获取各类型节点数量

#### storageUtils.js
- 自动备份管理：saveAutoBackup, getAutoBackup, clearAutoBackup
- 模板管理：saveTemplate, getTemplates, deleteTemplate, exportTemplate, importTemplate
- 统一 STORAGE_KEYS 常量
- getStorageStats(): 获取存储使用情况

### 4. 模块索引

#### index.js
- 统一导出所有模块
- 简化导入语句：`import { useBpmnModeler, ... } from './designer'`

## 优化效果

### 代码结构改进

| 改进项 | 优化前 | 优化后 |
|--------|--------|--------|
| 文件大小 | 4742 行单文件 | 拆分为 10+ 个模块 |
| 职责分离 | 混合在一起 | 清晰的单一职责 |
| 代码复用 | 重复代码多 | 提取公共函数 |
| 可测试性 | 难以单元测试 | 独立函数可测试 |
| 可维护性 | 修改风险高 | 模块化易于维护 |

### 主要改进点

1. **单一职责原则**: 每个模块专注于一个功能领域
2. **代码复用**: 公共函数提取到工具模块
3. **类型安全**: 使用 JSDoc 添加类型注释
4. **错误处理**: 统一的错误处理模式
5. **资源管理**: 自动清理事件监听器，防止内存泄漏
6. **配置集中**: 所有配置常量集中管理
7. **命名规范**: 语义化的函数和变量命名

## 后续优化建议

### 短期优化
1. 将主组件中的其他逻辑继续抽取到对应的组合式函数中
2. 创建 `useSelection.js` 管理节点选择状态
3. 创建 `useSearch.js` 管理节点搜索功能
4. 创建 `useStats.js` 管理流程统计

### 中期优化
1. 将模板库、评论、协作等功能抽取为独立的子组件
2. 使用 TypeScript 替换 JSDoc 类型注释
3. 添加单元测试覆盖核心函数
4. 优化性能：大流程的渲染优化

### 长期优化
1. 考虑引入状态管理（如 Pinia）管理复杂状态
2. 实现增量保存，只保存变更部分
3. 支持离线编辑
4. 实现协作编辑功能（WebSocket 实时同步）

## 使用示例

### 在新组件中使用优化后的模块

```vue
<template>
  <div class="workflow-designer">
    <div ref="bpmnCanvas" class="bpmn-canvas"></div>
    <div ref="propertiesPanel" class="properties-panel"></div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import {
  useBpmnModeler,
  useFlowElements,
  useFlowPersistence,
  useShortcuts
} from './designer'

const bpmnCanvas = ref(null)
const propertiesPanel = ref(null)
const processInfo = ref({ name: '', id: '' })

// 初始化 Modeler
const { bpmnModeler, currentZoom, isInitialized } = useBpmnModeler(
  bpmnCanvas,
  propertiesPanel
)

// 流程元素操作
const { handlePaletteClick, deleteSelected, undo, redo, zoomIn } = useFlowElements(bpmnModeler)

// 流程保存与部署
const { doSave, deployDiagram, enableAutoSave, hasUnsavedChanges } = useFlowPersistence({
  bpmnModeler,
  processInfo,
  onSaved: (data) => console.log('保存成功', data),
  onDeployed: (data) => console.log('部署成功', data)
})

// 快捷键
const { registerHandler } = useShortcuts({
  saveDiagram: () => doSave(),
  deployDiagram: () => deployDiagram()
})

// 启用自动保存
enableAutoSave(30000)
</script>
```

## 注意事项

1. **向后兼容**: 优化过程中保持原有功能不变
2. **渐进式重构**: 逐步替换，不影响现有功能
3. **测试覆盖**: 核心函数添加单元测试
4. **文档同步**: 更新代码注释和使用文档

## 迁移计划

1. **第一阶段** (已完成): 创建基础模块和工具函数
2. **第二阶段**: 重构主组件，使用新的组合式函数
3. **第三阶段**: 拆分大型对话框为子组件
4. **第四阶段**: 性能优化和测试覆盖

## 相关文件

- 主组件：`mp-vue/src/views/system/workflow/designer/index.vue`
- 模块目录：`mp-vue/src/views/system/workflow/designer/`
  - `composables/` - 组合式函数
  - `constants/` - 配置常量
  - `utils/` - 工具函数
  - `index.js` - 模块索引
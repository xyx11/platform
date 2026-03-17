# 流程设计器优化计划

## 当前问题分析

### 1. 重复定义的状态和函数
主组件 `index.vue` (4731 行) 中存在大量重复代码：

**重复的状态定义：**
- `bpmnModeler` - 已在 `useBpmnModeler` 中管理
- `currentZoom` - 已在 `useBpmnModeler` 中返回
- `processInfo` - 作为参数传给多个组合式函数，但在主组件定义
- `deployedDefinitions` - 已在 `useFlowPersistence` 中管理
- `flowStats` - 已在 `useFlowStats` 中返回
- `selectedNode` - 已在 `useFlowElements` 中返回

**重复的常量定义：**
- `BPMN_ELEMENTS` - 已在 `constants/bpmnElements.js` 中定义
- `ACTION_TO_ELEMENT` - 已在 `constants/bpmnElements.js` 中定义
- `ACTION_MESSAGES` - 已在 `constants/bpmnElements.js` 中定义
- `SKIP_SUCCESS_ACTIONS` - 已在 `constants/bpmnElements.js` 中定义
- `DESIGNER_CONFIG` - 已在 `constants/designerConfig.js` 中定义

**重复的函数实现：**
- `handlePaletteClick` - 已在 `useFlowElements` 中实现
- `createBpmnElement` - 已在 `useFlowElements` 中实现
- `getCanvasCenter` - 已在 `useFlowElements` 中实现
- `saveDiagram`, `doSave` - 已在 `useFlowPersistence` 中实现
- `deployDiagram` - 已在 `useFlowPersistence` 中实现

### 2. 未使用的组合式函数
以下组合式函数已创建但主组件未使用：
- `useBpmnModeler` - BPMN 模型器管理（核心）
- `useFlowElements` - 流程元素操作（核心）
- `useFlowPersistence` - 流程保存与部署（核心）

## 优化目标

### 阶段一：核心功能整合（本次优化重点）
1. 使用 `useBpmnModeler` 替换本地 `bpmnModeler` 管理
2. 使用 `useFlowElements` 替换本地元素操作函数
3. 使用 `useFlowPersistence` 替换本地保存/部署函数
4. 移除重复的常量定义，改用导入
5. 移除重复的状态定义，改用组合式函数返回

### 阶段二：辅助功能整合
1. 整合 `useNodeSearch` - 节点搜索
2. 整合 `useContextMenu` - 右键菜单
3. 整合 `useNodeProperties` - 节点属性

### 阶段三：代码清理
1. 删除已迁移的内联代码
2. 统一命名规范
3. 添加 JSDoc 注释

## 预期效果

| 指标 | 优化前 | 优化后 |
|------|--------|--------|
| 代码行数 | 4731 行 | ~2000 行 |
| 本地状态 | 50+ 个 | ~20 个 |
| 本地函数 | 80+ 个 | ~30 个 |
| 常量定义 | 5 个完整定义 | 0 个（全部导入） |
| 组合式函数使用 | 14 个 | 18 个 |

## 修改步骤

### 步骤 1：修改导入语句
```javascript
// 优化前 - 导入不完整
import { useDashboard } from './composables/useDashboard'
import { useCollaboration } from './composables/useCollaboration'
// ... 缺少 useBpmnModeler, useFlowElements, useFlowPersistence

import BpmnModeler from 'bpmn-js/lib/Modeler'
import {
  BpmnPropertiesPanelModule,
  BpmnPropertiesProviderModule,
  CamundaPlatformPropertiesProviderModule
} from 'bpmn-js-properties-panel'
import '@bpmn-io/properties-panel/assets/properties-panel.css'
// ... 大量样式导入

// 优化后 - 统一导入
import {
  useBpmnModeler,
  useFlowElements,
  useFlowPersistence,
  // ... 其他组合式函数
} from './designer'
import { BPMN_ELEMENTS, ACTION_TO_ELEMENT } from './constants/bpmnElements'
import { DESIGNER_CONFIG } from './constants/designerConfig'
```

### 步骤 2：使用组合式函数
```javascript
// 优化前 - 本地定义
const bpmnCanvas = ref(null)
const propertiesPanel = ref(null)
const bpmnModeler = ref(null)
const currentZoom = ref(100)

// 优化后 - 使用组合式函数
const bpmnCanvas = ref(null)
const propertiesPanel = ref(null)
const { bpmnModeler, currentZoom, importXML, saveXML } = useBpmnModeler(bpmnCanvas, propertiesPanel)
```

### 步骤 3：移除重复代码
删除主组件中重复定义的常量和函数，改用导入。

## 风险评估
- **低风险**：模板部分保持不变，只修改 script 部分
- **向后兼容**：保留原有的方法调用接口
- **测试建议**：修改后需要测试保存、部署、元素创建等核心功能
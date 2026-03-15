/**
 * 流程设计器模块索引
 * 统一导出所有组合式函数、工具和常量
 */

// 组合式函数
export { useBpmnModeler, DEFAULT_BPMN_XML, MODELER_CONFIG } from './composables/useBpmnModeler'
export { useShortcuts } from './composables/useShortcuts'
export { useFlowElements } from './composables/useFlowElements'
export { useFlowPersistence } from './composables/useFlowPersistence'
export { useFlowStats } from './composables/useFlowStats'
export { useFlowForms } from './composables/useFlowForms'
export { useExport } from './composables/useExport'
export { useNodeSearch } from './composables/useNodeSearch'
export { useContextMenu } from './composables/useContextMenu'
export { useBatchOperation } from './composables/useBatchOperation'
export { useNodeProperties } from './composables/useNodeProperties'
export { useAutoLayout } from './composables/useAutoLayout'
export { useSimulation } from './composables/useSimulation'
export { useTemplate } from './composables/useTemplate'
export { useDashboard } from './composables/useDashboard'

// 常量
export * from './constants/bpmnElements'
export * from './constants/designerConfig'

// 工具函数
export * from './utils/bpmnValidator'
export * from './utils/bpmnStatistics'
export * from './utils/storageUtils'
export * from './utils/commonUtils'

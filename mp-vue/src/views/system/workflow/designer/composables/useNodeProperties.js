/**
 * 节点属性管理组合式函数
 * 封装节点属性的读取、编辑、保存等功能
 */

import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 节点属性管理
 * @param {Object} options - 配置选项
 * @returns {Object} 节点属性 API
 */
export function useNodeProperties(options = {}) {
  const { bpmnModeler } = options

  // 状态
  const selectedNode = ref(null)
  const nodePropsVisible = ref(false)

  /**
   * 获取选中的节点
   */
  const getSelectedNode = () => {
    if (!bpmnModeler?.value) return null

    const selection = bpmnModeler.value.get('selection')
    const selected = selection.get()

    if (!selected || selected.length === 0) return null

    const element = selected[0]
    const bo = element.businessObject

    if (!bo || bo.$type === 'bpmn:Process' || bo.$type === 'bpmn:SequenceFlow') {
      return null
    }

    return {
      id: bo.id,
      name: bo.name || '',
      type: bo.$type,
      element
    }
  }

  /**
   * 打开节点属性对话框
   */
  const openNodeProps = () => {
    const node = getSelectedNode()
    if (!node) {
      ElMessage.warning('请先选择一个节点')
      return
    }

    // 从 BPMN 元素中读取最新属性
    const bo = node.element.businessObject

    selectedNode.value = {
      ...node,
      documentation: bo.documentation?.[0]?.body || '',
      timerExpression: bo.eventDefinitions?.[0]?.timeDuration?.body || '',
      conditionExpression: bo.conditionExpression?.body || '',
      resultVariable: bo['camunda:resultVariable'] || '',
      assigneeType: bo['camunda:assigneeType'] || 'user',
      assignee: bo.assignee || '',
      serviceClass: bo['camunda:class'] || '',
      method: bo['camunda:method'] || '',
      formKey: bo['camunda:formKey'] || ''
    }

    nodePropsVisible.value = true
  }

  /**
   * 保存节点属性
   */
  const saveNodeProps = () => {
    if (!selectedNode.value || !bpmnModeler?.value) return false

    const elementRegistry = bpmnModeler.value.get('elementRegistry')
    const modeling = bpmnModeler.value.get('modeling')
    const element = elementRegistry.get(selectedNode.value.id)

    if (!element) {
      ElMessage.error('节点不存在')
      return false
    }

    const bo = element.businessObject
    const updates = {
      name: selectedNode.value.name || ''
    }

    // 用户任务属性
    if (selectedNode.value.type?.includes('UserTask')) {
      updates.assignee = selectedNode.value.assignee || ''
      if (selectedNode.value.assigneeType) {
        updates['camunda:assigneeType'] = selectedNode.value.assigneeType
      }
    }

    // 服务任务属性
    if (selectedNode.value.type?.includes('ServiceTask')) {
      updates['camunda:class'] = selectedNode.value.serviceClass || ''
      if (selectedNode.value.method) {
        updates['camunda:method'] = selectedNode.value.method
      }
      if (selectedNode.value.resultVariable) {
        updates['camunda:resultVariable'] = selectedNode.value.resultVariable
      }
    }

    // 表单键
    if (selectedNode.value.formKey !== undefined) {
      updates['camunda:formKey'] = selectedNode.value.formKey
    }

    // 文档注释
    if (selectedNode.value.documentation) {
      updates.documentation = [{ body: selectedNode.value.documentation }]
    }

    // 定时器表达式
    if (selectedNode.value.timerExpression) {
      updates.eventDefinitions = [{
        $type: 'bpmn:TimerEventDefinition',
        timeDuration: { body: selectedNode.value.timerExpression }
      }]
    }

    // 条件表达式
    if (selectedNode.value.conditionExpression !== undefined) {
      updates.conditionExpression = { body: selectedNode.value.conditionExpression }
    }

    modeling.updateProperties(element, updates)
    ElMessage.success('节点属性已更新')
    nodePropsVisible.value = false
    return true
  }

  /**
   * 从 BPMN 读取节点属性
   */
  const readNodeProperties = (nodeId) => {
    if (!bpmnModeler?.value || !nodeId) return null

    const elementRegistry = bpmnModeler.value.get('elementRegistry')
    const element = elementRegistry.get(nodeId)

    if (!element) return null

    const bo = element.businessObject
    return {
      id: bo.id,
      name: bo.name || '',
      type: bo.$type,
      documentation: bo.documentation?.[0]?.body || '',
      timerExpression: bo.eventDefinitions?.[0]?.timeDuration?.body || '',
      conditionExpression: bo.conditionExpression?.body || '',
      resultVariable: bo['camunda:resultVariable'] || '',
      assigneeType: bo['camunda:assigneeType'] || 'user',
      assignee: bo.assignee || '',
      serviceClass: bo['camunda:class'] || '',
      method: bo['camunda:method'] || '',
      formKey: bo['camunda:formKey'] || ''
    }
  }

  /**
   * 更新节点属性
   */
  const updateNodeProperty = (nodeId, property, value) => {
    if (!bpmnModeler?.value || !nodeId) return false

    const elementRegistry = bpmnModeler.value.get('elementRegistry')
    const modeling = bpmnModeler.value.get('modeling')
    const element = elementRegistry.get(nodeId)

    if (!element) return false

    const updates = {}
    updates[property] = value

    modeling.updateProperties(element, updates)
    return true
  }

  /**
   * 关闭属性对话框
   */
  const closeNodeProps = () => {
    nodePropsVisible.value = false
    selectedNode.value = null
  }

  /**
   * 获取节点类型显示名称
   */
  const getNodeTypeName = (type) => {
    const typeMap = {
      'bpmn:StartEvent': '开始事件',
      'bpmn:EndEvent': '结束事件',
      'bpmn:UserTask': '用户任务',
      'bpmn:ServiceTask': '服务任务',
      'bpmn:ScriptTask': '脚本任务',
      'bpmn:ExclusiveGateway': '排他网关',
      'bpmn:ParallelGateway': '并行网关',
      'bpmn:InclusiveGateway': '包容网关',
      'bpmn:SequenceFlow': '顺序流',
      'bpmn:SubProcess': '子流程'
    }
    return typeMap[type] || type
  }

  /**
   * 获取节点类型的 Tag 颜色
   */
  const getNodeTagType = (type) => {
    if (type?.includes('Event')) return 'info'
    if (type?.includes('Task')) return 'success'
    if (type?.includes('Gateway')) return 'warning'
    if (type?.includes('Flow')) return ''
    return 'info'
  }

  return {
    // 状态
    selectedNode,
    nodePropsVisible,

    // 方法
    openNodeProps,
    saveNodeProps,
    closeNodeProps,
    getSelectedNode,
    readNodeProperties,
    updateNodeProperty,
    getNodeTypeName,
    getNodeTagType
  }
}
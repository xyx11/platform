/**
 * 流程元素操作组合式函数
 * 封装 BPMN 元素的创建、更新、删除等操作
 */

import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { BPMN_ELEMENTS, ACTION_TO_ELEMENT, ACTION_MESSAGES, SKIP_SUCCESS_ACTIONS } from '../constants/bpmnElements'
import { logger } from '@/utils/logger'

/**
 * 流程元素操作
 * @param {Ref<Object>} bpmnModeler - BPMN Modeler 引用
 * @returns {Object} 元素操作 API
 */
export function useFlowElements(bpmnModeler) {
  const selectedNode = ref(null)

  /**
   * 获取画布中心位置
   */
  const getCanvasCenter = () => {
    if (!bpmnModeler.value) return { x: 0, y: 0 }
    const canvas = bpmnModeler.value.get('canvas')
    const viewbox = canvas.viewbox()
    return {
      x: viewbox.x + viewbox.width / 2,
      y: viewbox.y + viewbox.height / 2
    }
  }

  /**
   * 创建 BPMN 元素
   */
  const createBpmnElement = (type, name, position, extraProps = {}) => {
    if (!bpmnModeler.value) return

    const modeling = bpmnModeler.value.get('modeling')
    const canvas = bpmnModeler.value.get('canvas')
    const rootElement = canvas.getRootElement()

    const shape = { type, name, ...extraProps }
    modeling.createShape(shape, position, rootElement)
  }

  /**
   * 处理工具栏点击
   */
  const handlePaletteClick = (action) => {
    // 处理连接类操作
    if (ACTION_MESSAGES[action]) {
      ElMessage.info(ACTION_MESSAGES[action])
      return
    }

    // 获取元素配置
    const elementKey = ACTION_TO_ELEMENT[action]
    if (!elementKey || !BPMN_ELEMENTS[elementKey]) {
      ElMessage.warning('未知操作：' + action)
      return
    }

    const config = BPMN_ELEMENTS[elementKey]
    const center = getCanvasCenter()
    const position = {
      x: center.x + config.offset.x,
      y: center.y + config.offset.y
    }

    try {
      createBpmnElement(config.type, config.name, position, config)

      // 显示成功提示（连接类操作除外）
      if (!SKIP_SUCCESS_ACTIONS.includes(action)) {
        ElMessage.success('已添加元素')
      }
    } catch (err) {
      logger.error('创建元素失败:', err)
      ElMessage.error('创建元素失败：' + err.message)
    }
  }

  /**
   * 删除选中元素
   */
  const deleteSelected = () => {
    if (!bpmnModeler.value) return

    const modeling = bpmnModeler.value.get('modeling')
    const selection = bpmnModeler.value.get('selection')
    const selected = selection.get()

    if (selected.length > 0) {
      modeling.removeElements(selected)
      ElMessage.success('已删除')
    } else {
      ElMessage.warning('请先选择要删除的元素')
    }
  }

  /**
   * 撤销
   */
  const undo = () => {
    if (!bpmnModeler.value) return
    bpmnModeler.value.get('commandStack').undo()
    ElMessage.success('已撤销')
  }

  /**
   * 重做
   */
  const redo = () => {
    if (!bpmnModeler.value) return
    bpmnModeler.value.get('commandStack').redo()
    ElMessage.success('已重做')
  }

  /**
   * 缩放控制
   */
  const zoom = (action) => {
    if (!bpmnModeler.value) return
    const canvas = bpmnModeler.value.get('canvas')
    const actions = {
      in: 'step-in',
      out: 'step-out',
      fit: 'fit-viewport'
    }
    canvas.zoom(actions[action] || 'step-in')
  }

  const zoomIn = () => zoom('in')
  const zoomOut = () => zoom('out')
  const zoomFit = () => zoom('fit')

  /**
   * 设置缩放比例
   */
  const setZoom = (percent) => {
    if (!bpmnModeler.value) return
    const canvas = bpmnModeler.value.get('canvas')
    const viewbox = canvas.viewbox()
    const scale = percent / 100
    canvas.zoom(viewbox.scale / scale)
  }

  /**
   * 更新节点属性
   */
  const updateNodeProperties = (nodeData) => {
    if (!bpmnModeler.value || !nodeData) return false

    const elementRegistry = bpmnModeler.value.get('elementRegistry')
    const modeling = bpmnModeler.value.get('modeling')
    const element = elementRegistry.get(nodeData.id)

    if (!element) return false

    const bo = element.businessObject
    const updates = {
      name: nodeData.name || ''
    }

    // 用户任务属性
    if (nodeData.type?.includes('UserTask')) {
      updates.assignee = nodeData.assignee || ''
      if (nodeData.assigneeType) {
        updates['camunda:assigneeType'] = nodeData.assigneeType
      }
    }

    // 服务任务属性
    if (nodeData.type?.includes('ServiceTask')) {
      updates['camunda:class'] = nodeData.serviceClass || ''
      if (nodeData.method) {
        updates['camunda:method'] = nodeData.method
      }
      if (nodeData.resultVariable) {
        updates['camunda:resultVariable'] = nodeData.resultVariable
      }
    }

    // 文档注释
    if (nodeData.documentation) {
      updates.documentation = [{ body: nodeData.documentation }]
    }

    // 定时器表达式
    if (nodeData.timerExpression) {
      updates.eventDefinitions = [{
        $type: 'bpmn:TimerEventDefinition',
        timeDuration: { body: nodeData.timerExpression }
      }]
    }

    // 条件表达式
    if (nodeData.conditionExpression !== undefined) {
      updates.conditionExpression = { body: nodeData.conditionExpression }
    }

    modeling.updateProperties(element, updates)
    return true
  }

  /**
   * 获取节点属性
   */
  const getNodeProperties = (nodeId) => {
    if (!bpmnModeler.value || !nodeId) return null

    const elementRegistry = bpmnModeler.value.get('elementRegistry')
    const element = elementRegistry.get(nodeId)

    if (!element) return null

    const bo = element.businessObject
    return {
      id: bo.id,
      name: bo.name || '',
      type: bo.$type,
      assignee: bo.assignee || '',
      assigneeType: bo['camunda:assigneeType'] || 'user',
      serviceClass: bo['camunda:class'] || '',
      method: bo['camunda:method'] || '',
      resultVariable: bo['camunda:resultVariable'] || '',
      documentation: bo.documentation?.[0]?.body || '',
      timerExpression: bo.eventDefinitions?.[0]?.timeDuration?.body || '',
      conditionExpression: bo.conditionExpression?.body || ''
    }
  }

  return {
    // 状态
    selectedNode,

    // 方法
    handlePaletteClick,
    deleteSelected,
    undo,
    redo,
    zoomIn,
    zoomOut,
    zoomFit,
    setZoom,
    updateNodeProperties,
    getNodeProperties,
    createBpmnElement,
    getCanvasCenter
  }
}

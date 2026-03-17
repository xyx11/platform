/**
 * BPMN 验证工具函数
 */

import { logger } from '@/utils/logger'

/**
 * BPMN 基础验证规则
 */
const VALIDATION_RULES = {
  /**
   * 检查开始事件数量
   */
  checkStartEvents(xmlDoc) {
    const startEvents = xmlDoc.getElementsByTagName('bpmn:startEvent')
    if (startEvents.length === 0) {
      return '流程必须包含至少一个开始事件'
    }
    if (startEvents.length > 1) {
      return `流程只能包含一个开始事件，当前有 ${startEvents.length} 个`
    }
    return null
  },

  /**
   * 检查结束事件数量
   */
  checkEndEvents(xmlDoc) {
    const endEvents = xmlDoc.getElementsByTagName('bpmn:endEvent')
    if (endEvents.length === 0) {
      return '流程必须包含至少一个结束事件'
    }
    return null
  },

  /**
   * 检查流程是否有孤立节点（没有入度或出度的节点）
   */
  checkOrphanNodes(xmlDoc) {
    const sequenceFlows = xmlDoc.getElementsByTagName('bpmn:sequenceFlow')
    const flowIds = new Set()

    for (let i = 0; i < sequenceFlows.length; i++) {
      const flow = sequenceFlows[i]
      flowIds.add(flow.getAttribute('sourceRef'))
      flowIds.add(flow.getAttribute('targetRef'))
    }

    // 获取所有流程元素
    const processElements = xmlDoc.getElementsByTagName('bpmn:process')[0]
    if (!processElements) return null

    const orphanNodes = []
    const elementTypes = ['startEvent', 'endEvent', 'userTask', 'serviceTask', 'scriptTask', 'exclusiveGateway', 'parallelGateway', 'inclusiveGateway']

    for (const type of elementTypes) {
      const elements = xmlDoc.getElementsByTagName(`bpmn:${type}`)
      for (let i = 0; i < elements.length; i++) {
        const element = elements[i]
        const id = element.getAttribute('id')
        // 开始事件不需要入度，结束事件不需要出度
        if (type === 'startEvent' && !hasOutgoing(sequenceFlows, id)) continue
        if (type === 'endEvent' && !hasIncoming(sequenceFlows, id)) continue
        if (!hasIncoming(sequenceFlows, id) && !hasOutgoing(sequenceFlows, id)) {
          orphanNodes.push(element.getAttribute('name') || id)
        }
      }
    }

    if (orphanNodes.length > 0) {
      return `发现 ${orphanNodes.length} 个孤立节点：${orphanNodes.join(', ')}`
    }
    return null
  }
}

/**
 * 检查元素是否有入边
 */
function hasIncoming(flows, id) {
  for (let i = 0; i < flows.length; i++) {
    if (flows[i].getAttribute('targetRef') === id) return true
  }
  return false
}

/**
 * 检查元素是否有出边
 */
function hasOutgoing(flows, id) {
  for (let i = 0; i < flows.length; i++) {
    if (flows[i].getAttribute('sourceRef') === id) return true
  }
  return false
}

/**
 * 验证 BPMN XML
 * @param {string} xml - BPMN XML 字符串
 * @param {Object} options - 验证选项
 * @returns {{ valid: boolean, errors: string[], warnings: string[] }} 验证结果
 */
export function validateBpmn(xml, options = {}) {
  const result = {
    valid: true,
    errors: [],
    warnings: []
  }

  try {
    const parser = new DOMParser()
    const xmlDoc = parser.parseFromString(xml, 'text/xml')

    // 检查 XML 解析错误
    const parseError = xmlDoc.getElementsByTagName('parsererror')[0]
    if (parseError) {
      result.valid = false
      result.errors.push('XML 解析失败：' + parseError.textContent)
      return result
    }

    // 执行必选验证
    const requiredChecks = [
      VALIDATION_RULES.checkStartEvents,
      VALIDATION_RULES.checkEndEvents
    ]

    for (const check of requiredChecks) {
      const error = check(xmlDoc)
      if (error) {
        result.errors.push(error)
        result.valid = false
      }
    }

    // 执行可选验证
    if (options.checkOrphans !== false) {
      const orphanWarning = VALIDATION_RULES.checkOrphanNodes(xmlDoc)
      if (orphanWarning) {
        result.warnings.push(orphanWarning)
      }
    }

  } catch (e) {
    result.valid = false
    result.errors.push('BPMN 验证失败：' + e.message)
  }

  return result
}

/**
 * 快速验证 BPMN XML（仅检查必需项）
 * @param {string} xml - BPMN XML 字符串
 * @returns {string|null} 错误信息，无错误返回 null
 */
export function validateBpmnQuick(xml) {
  const result = validateBpmn(xml, { checkOrphans: false })
  return result.errors[0] || null
}

/**
 * 验证 BPMN 并返回第一个错误（兼容旧代码）
 * @param {string} xml - BPMN XML 字符串
 * @returns {string|null} 错误信息
 */
export function validateBpmnSimple(xml) {
  try {
    const parser = new DOMParser()
    const xmlDoc = parser.parseFromString(xml, 'text/xml')

    // 检查开始事件
    const startEvents = xmlDoc.getElementsByTagName('bpmn:startEvent')
    if (startEvents.length === 0) {
      return '流程必须包含至少一个开始事件'
    }
    if (startEvents.length > 1) {
      return `流程只能包含一个开始事件，当前有 ${startEvents.length} 个`
    }

    // 检查结束事件
    const endEvents = xmlDoc.getElementsByTagName('bpmn:endEvent')
    if (endEvents.length === 0) {
      return '流程必须包含至少一个结束事件'
    }

    return null
  } catch (e) {
    logger.error('BPMN 验证失败:', e)
    return 'BPMN 验证失败：' + e.message
  }
}

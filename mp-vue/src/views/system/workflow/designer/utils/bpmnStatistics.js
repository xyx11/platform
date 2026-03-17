/**
 * BPMN 流程统计工具
 */

import { logger } from '@/utils/logger'

/**
 * 流程统计数据结构
 */
export class FlowStatistics {
  constructor() {
    this.totalNodes = 0
    this.totalFlows = 0
    this.startEvents = 0
    this.endEvents = 0
    this.userTasks = 0
    this.serviceTasks = 0
    this.scriptTasks = 0
    this.sendReceiveTasks = 0
    this.exclusiveGateways = 0
    this.parallelGateways = 0
    this.inclusiveGateways = 0
    this.subProcesses = 0
    this.dataObjects = 0
  }
}

/**
 * 统计 BPMN 流程元素
 * @param {Document} xmlDoc - BPMN XML 文档
 * @returns {FlowStatistics} 统计数据
 */
export function countFlowElements(xmlDoc) {
  const stats = new FlowStatistics()

  // 统计事件
  stats.startEvents = xmlDoc.getElementsByTagName('bpmn:startEvent').length
  stats.endEvents = xmlDoc.getElementsByTagName('bpmn:endEvent').length

  // 统计任务
  stats.userTasks = xmlDoc.getElementsByTagName('bpmn:userTask').length
  stats.serviceTasks = xmlDoc.getElementsByTagName('bpmn:serviceTask').length
  stats.scriptTasks = xmlDoc.getElementsByTagName('bpmn:scriptTask').length
  const sendTasks = xmlDoc.getElementsByTagName('bpmn:sendTask').length
  const receiveTasks = xmlDoc.getElementsByTagName('bpmn:receiveTask').length
  stats.sendReceiveTasks = sendTasks + receiveTasks

  // 统计网关
  stats.exclusiveGateways = xmlDoc.getElementsByTagName('bpmn:exclusiveGateway').length
  stats.parallelGateways = xmlDoc.getElementsByTagName('bpmn:parallelGateway').length
  stats.inclusiveGateways = xmlDoc.getElementsByTagName('bpmn:inclusiveGateway').length

  // 统计子流程
  stats.subProcesses = xmlDoc.getElementsByTagName('bpmn:subProcess').length

  // 统计数据对象
  stats.dataObjects = xmlDoc.getElementsByTagName('bpmn:dataObjectReference').length

  // 统计连线
  stats.totalFlows = xmlDoc.getElementsByTagName('bpmn:sequenceFlow').length

  // 计算总节点数
  stats.totalNodes =
    stats.startEvents +
    stats.endEvents +
    stats.userTasks +
    stats.serviceTasks +
    stats.scriptTasks +
    stats.sendReceiveTasks +
    stats.exclusiveGateways +
    stats.parallelGateways +
    stats.inclusiveGateways +
    stats.subProcesses

  return stats
}

/**
 * 计算流程复杂度得分（0-100）
 * @param {FlowStatistics} stats - 统计数据
 * @returns {number} 复杂度得分
 */
export function calculateComplexity(stats) {
  let score = 0

  // 节点数权重（最多 40 分）
  if (stats.totalNodes <= 10) {
    score += 40
  } else if (stats.totalNodes <= 20) {
    score += 30
  } else if (stats.totalNodes <= 30) {
    score += 20
  } else if (stats.totalNodes <= 50) {
    score += 10
  } else {
    score += 0
  }

  // 网关复杂度（最多 30 分）
  const gatewayComplexity =
    stats.exclusiveGateways * 1 +
    stats.parallelGateways * 2 +
    stats.inclusiveGateways * 2
  if (gatewayComplexity <= 3) {
    score += 30
  } else if (gatewayComplexity <= 6) {
    score += 20
  } else if (gatewayComplexity <= 10) {
    score += 10
  }

  // 流程规范性（最多 30 分）
  if (stats.startEvents === 1) score += 15
  if (stats.endEvents >= 1) score += 15

  return score
}

/**
 * 获取复杂度等级描述
 * @param {number} score - 复杂度得分
 * @returns {{ text: string, status: string }} 等级描述
 */
export function getComplexityLevel(score) {
  if (score >= 80) {
    return { text: '优秀', status: 'success' }
  } else if (score >= 60) {
    return { text: '良好', status: 'success' }
  } else if (score >= 40) {
    return { text: '一般', status: 'warning' }
  } else {
    return { text: '复杂', status: 'error' }
  }
}

/**
 * 解析 BPMN XML 并获取统计信息
 * @param {string} xml - BPMN XML 字符串
 * @returns {{ stats: FlowStatistics, complexity: number, level: Object }} 统计结果
 */
export function getFlowStatistics(xml) {
  try {
    const parser = new DOMParser()
    const xmlDoc = parser.parseFromString(xml, 'text/xml')
    const stats = countFlowElements(xmlDoc)
    const complexity = calculateComplexity(stats)
    const level = getComplexityLevel(complexity)

    return {
      stats,
      complexity,
      level,
      format: (s) => `${s}%`,
      comment: getComplexityComment(complexity)
    }
  } catch (e) {
    logger.error('统计流程失败:', e)
    return {
      stats: new FlowStatistics(),
      complexity: 0,
      level: { text: '未知', status: 'info' },
      format: (s) => `${s}%`,
      comment: '无法解析流程'
    }
  }
}

/**
 * 获取复杂度评价
 */
function getComplexityComment(score) {
  if (score >= 80) {
    return '流程结构清晰，复杂度适中'
  } else if (score >= 60) {
    return '流程结构合理，可适当简化'
  } else if (score >= 40) {
    return '流程较为复杂，建议优化结构'
  } else {
    return '流程复杂度过高，强烈建议简化'
  }
}

/**
 * 获取节点类型统计
 * @param {string} xml - BPMN XML 字符串
 * @returns {Object} 各类型节点数量
 */
export function getNodeTypeStats(xml) {
  const parser = new DOMParser()
  const xmlDoc = parser.parseFromString(xml, 'text/xml')

  const typeMap = {
    '开始事件': 'bpmn:startEvent',
    '结束事件': 'bpmn:endEvent',
    '用户任务': 'bpmn:userTask',
    '服务任务': 'bpmn:serviceTask',
    '脚本任务': 'bpmn:scriptTask',
    '排他网关': 'bpmn:exclusiveGateway',
    '并行网关': 'bpmn:parallelGateway',
    '包容网关': 'bpmn:inclusiveGateway',
    '子流程': 'bpmn:subProcess'
  }

  const result = {}
  for (const [name, tag] of Object.entries(typeMap)) {
    result[name] = xmlDoc.getElementsByTagName(tag).length
  }
  return result
}

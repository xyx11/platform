/**
 * 流程统计与健康度组合式函数
 * 封装流程统计、健康度检查、复杂度计算等功能
 */

import { ref, computed } from 'vue'
import { getFlowStatistics, calculateComplexity, getComplexityLevel } from '../utils/bpmnStatistics'
import { validateBpmn } from '../utils/bpmnValidator'

/**
 * 流程统计与健康度
 * @param {Ref<Object>} bpmnModeler - BPMN Modeler 引用
 * @returns {Object} 统计与健康度 API
 */
export function useFlowStats(bpmnModeler) {
  const stats = ref(null)
  const healthIssues = ref([])
  const isAnalyzing = ref(false)

  /**
   * 获取画布元素统计
   */
  const getElementStats = () => {
    if (!bpmnModeler?.value) return null

    const elementRegistry = bpmnModeler.value.get('elementRegistry')
    const elements = elementRegistry.getAll()

    const counts = {
      total: elements.length,
      startEvents: 0,
      endEvents: 0,
      tasks: 0,
      userTasks: 0,
      serviceTasks: 0,
      gateways: 0,
      exclusiveGateways: 0,
      parallelGateways: 0,
      inclusiveGateways: 0,
      flows: 0,
      subProcesses: 0
    }

    elements.forEach(el => {
      const type = el.type
      if (type === 'bpmn:StartEvent') counts.startEvents++
      else if (type === 'bpmn:EndEvent') counts.endEvents++
      else if (type === 'bpmn:UserTask') counts.userTasks++
      else if (type === 'bpmn:ServiceTask') counts.serviceTasks++
      else if (type?.includes('Task')) counts.tasks++
      else if (type === 'bpmn:ExclusiveGateway') counts.exclusiveGateways++
      else if (type === 'bpmn:ParallelGateway') counts.parallelGateways++
      else if (type === 'bpmn:InclusiveGateway') counts.inclusiveGateways++
      else if (type?.includes('Gateway')) counts.gateways++
      else if (type === 'bpmn:SequenceFlow') counts.flows++
      else if (type === 'bpmn:SubProcess') counts.subProcesses++
    })

    return counts
  }

  /**
   * 快速健康检查
   */
  const quickHealthCheck = () => {
    if (!bpmnModeler?.value) return { valid: false, issues: [] }

    const stats = getElementStats()
    const issues = []

    // 检查开始事件
    if (stats.startEvents === 0) {
      issues.push({ type: 'error', message: '缺少开始事件' })
    } else if (stats.startEvents > 1) {
      issues.push({ type: 'warning', message: `存在${stats.startEvents}个开始事件` })
    }

    // 检查结束事件
    if (stats.endEvents === 0) {
      issues.push({ type: 'error', message: '缺少结束事件' })
    }

    return {
      valid: issues.filter(i => i.type === 'error').length === 0,
      issues,
      stats
    }
  }

  /**
   * 详细健康检查
   */
  const detailedHealthCheck = async () => {
    if (!bpmnModeler?.value) return { valid: false, issues: [], suggestions: [] }

    isAnalyzing.value = true
    const issues = []
    const suggestions = []

    try {
      const { xml } = await bpmnModeler.value.saveXML()

      // 基础验证
      const validation = validateBpmn(xml)
      issues.push(...validation.errors.map(e => ({ type: 'error', message: e })))
      issues.push(...validation.warnings.map(w => ({ type: 'warning', message: w })))

      // 获取元素统计
      const elementRegistry = bpmnModeler.value.get('elementRegistry')
      const elements = elementRegistry.getAll()
      const flows = elements.filter(e => e.type === 'bpmn:SequenceFlow')

      // 检查用户任务
      const userTasks = elements.filter(e => e.type === 'bpmn:UserTask')
      userTasks.forEach(task => {
        const bo = task.businessObject
        if (!bo.name) {
          suggestions.push(`用户任务 "${task.id}" 缺少名称`)
        }
        if (!bo.assignee && !bo['camunda:assignee']) {
          suggestions.push(`用户任务 "${bo.name || task.id}" 未指定处理人`)
        }
      })

      // 检查服务任务
      const serviceTasks = elements.filter(e => e.type === 'bpmn:ServiceTask')
      serviceTasks.forEach(task => {
        const bo = task.businessObject
        if (!bo.name) {
          suggestions.push(`服务任务 "${task.id}" 缺少名称`)
        }
        if (!bo['camunda:class'] && !bo['camunda:delegateExpression'] && !bo['camunda:expression']) {
          suggestions.push(`服务任务 "${bo.name || task.id}" 未指定实现`)
        }
      })

      // 检查网关
      const gateways = elements.filter(e =>
        e.type === 'bpmn:ExclusiveGateway' ||
        e.type === 'bpmn:ParallelGateway' ||
        e.type === 'bpmn:InclusiveGateway'
      )
      gateways.forEach(gw => {
        const outgoing = flows.filter(f => f.source?.id === gw.id)
        if (outgoing.length === 0) {
          suggestions.push(`网关 "${gw.businessObject.name || gw.id}" 没有流出分支`)
        }
        if (outgoing.length === 1 && gw.type === 'bpmn:ExclusiveGateway') {
          suggestions.push(`排他网关 "${gw.businessObject.name || gw.id}" 只有一个流出分支`)
        }
      })

      // 检查流程链长度
      const graph = buildGraph(elements, flows)
      const longestPath = findLongestPath(graph)
      if (longestPath > 15) {
        suggestions.push(`流程链过长 (${longestPath} 个节点)，建议拆分为子流程`)
      }

      stats.value = {
        issues,
        suggestions,
        elementCount: elements.length,
        flowCount: flows.length
      }
    } catch (error) {
      console.error('健康检查失败:', error)
      issues.push({ type: 'error', message: '健康检查失败：' + error.message })
    } finally {
      isAnalyzing.value = false
    }

    return {
      valid: issues.filter(i => i.type === 'error').length === 0,
      issues,
      suggestions
    }
  }

  /**
   * 构建流程图（邻接表表示）
   */
  const buildGraph = (elements, flows) => {
    const graph = new Map()

    // 初始化所有节点
    elements.forEach(el => {
      if (!graph.has(el.id)) {
        graph.set(el.id, { incoming: [], outgoing: [] })
      }
    })

    // 添加边
    flows.forEach(flow => {
      const sourceId = flow.source?.id
      const targetId = flow.target?.id
      if (sourceId && targetId) {
        if (!graph.has(sourceId)) graph.set(sourceId, { incoming: [], outgoing: [] })
        if (!graph.has(targetId)) graph.set(targetId, { incoming: [], outgoing: [] })
        graph.get(sourceId).outgoing.push(targetId)
        graph.get(targetId).incoming.push(sourceId)
      }
    })

    return graph
  }

  /**
   * 查找最长路径（BFS）
   */
  const findLongestPath = (graph) => {
    let maxLen = 0

    for (const [nodeId, edges] of graph.entries()) {
      if (edges.incoming.length === 0) { // 从起点开始
        const queue = [[nodeId, 1]]
        const visited = new Set([nodeId])

        while (queue.length > 0) {
          const [current, dist] = queue.shift()
          maxLen = Math.max(maxLen, dist)

          const nextIds = graph.get(current)?.outgoing || []
          for (const nextId of nextIds) {
            if (!visited.has(nextId)) {
              visited.add(nextId)
              queue.push([nextId, dist + 1])
            }
          }
        }
      }
    }

    return maxLen
  }

  /**
   * 检测循环
   */
  const detectCycle = (graph) => {
    const visited = new Set()
    const recStack = new Set()

    const dfs = (nodeId) => {
      if (recStack.has(nodeId)) return true
      if (visited.has(nodeId)) return false

      visited.add(nodeId)
      recStack.add(nodeId)

      const neighbors = graph.get(nodeId)?.outgoing || []
      for (const neighbor of neighbors) {
        if (dfs(neighbor)) return true
      }

      recStack.delete(nodeId)
      return false
    }

    for (const nodeId of graph.keys()) {
      if (dfs(nodeId)) return true
    }
    return false
  }

  /**
   * 计算复杂度得分
   */
  const calculateFlowComplexity = () => {
    const elementStats = getElementStats()
    if (!elementStats) return 0
    return calculateComplexity({
      totalNodes: elementStats.total,
      userTasks: elementStats.userTasks,
      serviceTasks: elementStats.serviceTasks,
      exclusiveGateways: elementStats.exclusiveGateways,
      parallelGateways: elementStats.parallelGateways,
      inclusiveGateways: elementStats.inclusiveGateways,
      startEvents: elementStats.startEvents,
      endEvents: elementStats.endEvents
    })
  }

  /**
   * 获取复杂度等级
   */
  const getComplexity = () => {
    const score = calculateFlowComplexity()
    return getComplexityLevel(score)
  }

  /**
   * 刷新统计
   */
  const refreshStats = () => {
    stats.value = getElementStats()
  }

  return {
    // 状态
    stats,
    healthIssues,
    isAnalyzing,

    // 计算属性
    complexity: computed(() => calculateFlowComplexity()),
    complexityLevel: computed(() => getComplexity()),

    // 方法
    getElementStats,
    quickHealthCheck,
    detailedHealthCheck,
    refreshStats,
    buildGraph,
    findLongestPath,
    detectCycle
  }
}
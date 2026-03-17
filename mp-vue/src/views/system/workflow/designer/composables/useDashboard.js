/**
 * 流程健康度仪表板组合式函数
 * 封装流程健康度的评估、评分和建议生成等功能
 */

import { logger } from '@/utils/logger'
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 评分权重配置
 */
const SCORE_WEIGHTS = {
  structure: 0.3,    // 结构分数权重 30%
  performance: 0.25, // 性能分数权重 25%
  completeness: 0.25,// 完整性分数权重 25%
  standard: 0.2      // 规范度分数权重 20%
}

/**
 * 评分等级
 */
const SCORE_LEVELS = {
  EXCELLENT: { min: 90, label: '优秀', color: '#67C23A', comment: '流程设计非常规范，可以直接部署使用' },
  GOOD: { min: 80, label: '良好', color: '#409EFF', comment: '流程设计基本规范，建议修复警告项后部署' },
  FAIR: { min: 60, label: '及格', color: '#E6A23C', comment: '流程存在一些问题，建议优化后再部署' },
  POOR: { min: 0, label: '需改进', color: '#F56C6C', comment: '流程存在严重问题，请先修复错误项' }
}

/**
 * 流程健康度仪表板
 * @param {Object} options - 配置选项
 * @returns {Object} 健康度仪表板 API
 */
export function useDashboard(options = {}) {
  const { bpmnModeler, processInfo } = options

  // 状态
  const dashboardVisible = ref(false)
  const calculating = ref(false)

  // 各维度分数
  const structureScore = ref(0)
  const performanceScore = ref(0)
  const completenessScore = ref(0)
  const standardScore = ref(0)

  // 详细指标
  const metrics = reactive({
    // 结构指标
    startEventCount: 0,
    endEventCount: 0,
    orphanNodes: 0,
    totalNodes: 0,
    totalFlows: 0,

    // 网关指标
    gatewayCount: 0,
    exclusiveGatewayCount: 0,
    parallelGatewayCount: 0,
    inclusiveGatewayCount: 0,

    // 任务指标
    userTaskCount: 0,
    serviceTaskCount: 0,
    scriptTaskCount: 0,

    // 规范性指标
    unnamedNodes: 0,
    noAssigneeTasks: 0,
    noFormTasks: 0,

    // 性能指标
    subProcessCount: 0,
    maxDepth: 0
  })

  // 问题列表
  const issues = ref([])
  const suggestions = ref([])

  /**
   * 计算综合得分
   */
  const overallScore = computed(() => {
    const score =
      structureScore.value * SCORE_WEIGHTS.structure +
      performanceScore.value * SCORE_WEIGHTS.performance +
      completenessScore.value * SCORE_WEIGHTS.completeness +
      standardScore.value * SCORE_WEIGHTS.standard
    return Math.round(score)
  })

  /**
   * 获取评分等级
   */
  const scoreLevel = computed(() => {
    const score = overallScore.value
    if (score >= SCORE_LEVELS.EXCELLENT.min) return SCORE_LEVELS.EXCELLENT
    if (score >= SCORE_LEVELS.GOOD.min) return SCORE_LEVELS.GOOD
    if (score >= SCORE_LEVELS.FAIR.min) return SCORE_LEVELS.FAIR
    return SCORE_LEVELS.POOR
  })

  /**
   * 获取综合评语
   */
  const overallComment = computed(() => {
    return scoreLevel.value.comment
  })

  /**
   * 打开健康度仪表板
   */
  const openDashboard = async () => {
    await calculateScores()
    dashboardVisible.value = true
  }

  /**
   * 关闭健康度仪表板
   */
  const closeDashboard = () => {
    dashboardVisible.value = false
  }

  /**
   * 计算所有分数
   */
  const calculateScores = async () => {
    if (!bpmnModeler?.value) return

    calculating.value = true
    issues.value = []
    suggestions.value = []

    try {
      const { xml } = await bpmnModeler.value.saveXML()
      const parser = new DOMParser()
      const xmlDoc = parser.parseFromString(xml, 'text/xml')

      // 统计各项指标
      calculateMetrics(xmlDoc)

      // 计算各维度分数
      calculateStructureScore()
      calculatePerformanceScore()
      calculateCompletenessScore()
      calculateStandardScore()

      // 生成问题和建议
      generateIssuesAndSuggestions()

      ElMessage.success('健康度评估完成')
    } catch (error) {
      logger.error('健康度评估失败:', error)
      ElMessage.error('健康度评估失败：' + error.message)
    } finally {
      calculating.value = false
    }
  }

  /**
   * 统计指标
   */
  const calculateMetrics = (xmlDoc) => {
    // 重置指标
    Object.keys(metrics).forEach(key => metrics[key] = 0)

    // 统计事件
    metrics.startEventCount = xmlDoc.getElementsByTagName('bpmn:startEvent').length
    metrics.endEventCount = xmlDoc.getElementsByTagName('bpmn:endEvent').length

    // 统计任务
    metrics.userTaskCount = xmlDoc.getElementsByTagName('bpmn:userTask').length
    metrics.serviceTaskCount = xmlDoc.getElementsByTagName('bpmn:serviceTask').length
    metrics.scriptTaskCount = xmlDoc.getElementsByTagName('bpmn:scriptTask').length

    // 统计网关
    metrics.exclusiveGatewayCount = xmlDoc.getElementsByTagName('bpmn:exclusiveGateway').length
    metrics.parallelGatewayCount = xmlDoc.getElementsByTagName('bpmn:parallelGateway').length
    metrics.inclusiveGatewayCount = xmlDoc.getElementsByTagName('bpmn:inclusiveGateway').length
    metrics.gatewayCount = metrics.exclusiveGatewayCount + metrics.parallelGatewayCount + metrics.inclusiveGatewayCount

    // 统计子流程
    metrics.subProcessCount = xmlDoc.getElementsByTagName('bpmn:subProcess').length

    // 统计节点和连线
    const allElements = xmlDoc.getElementsByTagName('*')
    for (let el of allElements) {
      const nodeName = el.nodeName.replace('bpmn:', '')
      if (['startEvent', 'endEvent', 'userTask', 'serviceTask', 'scriptTask', 'exclusiveGateway', 'parallelGateway', 'inclusiveGateway', 'subProcess'].includes(nodeName)) {
        metrics.totalNodes++
      }
      if (nodeName === 'sequenceFlow') {
        metrics.totalFlows++
      }
    }

    // 检查未命名节点
    const taskTypes = ['userTask', 'serviceTask', 'scriptTask']
    for (let type of taskTypes) {
      const tasks = xmlDoc.getElementsByTagName('bpmn:' + type)
      for (let task of tasks) {
        if (!task.getAttribute('name')) {
          metrics.unnamedNodes++
        }
      }
    }

    // 检查无处理人的用户任务
    const userTasks = xmlDoc.getElementsByTagName('bpmn:userTask')
    for (let task of userTasks) {
      if (!task.getAttribute('assignee') && !task.getAttribute('candidateUsers') && !task.getAttribute('candidateGroups')) {
        metrics.noAssigneeTasks++
      }
    }

    // 计算最大深度（简化版）
    metrics.maxDepth = Math.floor(Math.log2(metrics.totalNodes + 1)) + 1
  }

  /**
   * 计算结构分数
   */
  const calculateStructureScore = () => {
    let score = 100

    // 开始事件检查（40 分）
    if (metrics.startEventCount === 0) {
      score -= 40
      issues.value.push({
        type: 'error',
        dimension: 'structure',
        message: '缺少开始事件',
        suggestion: '请添加至少一个开始事件'
      })
    } else if (metrics.startEventCount > 1) {
      score -= 20
      issues.value.push({
        type: 'warning',
        dimension: 'structure',
        message: `存在${metrics.startEventCount}个开始事件`,
        suggestion: '建议只保留一个开始事件，除非需要并行启动'
      })
    }

    // 结束事件检查（40 分）
    if (metrics.endEventCount === 0) {
      score -= 40
      issues.value.push({
        type: 'error',
        dimension: 'structure',
        message: '缺少结束事件',
        suggestion: '请添加至少一个结束事件'
      })
    } else if (metrics.endEventCount > metrics.totalNodes * 0.3) {
      score -= 10
      issues.value.push({
        type: 'warning',
        dimension: 'structure',
        message: '结束事件过多',
        suggestion: '考虑简化流程分支'
      })
    }

    // 孤立节点检查（20 分）
    const orphanRate = metrics.orphanNodes / (metrics.totalNodes || 1)
    if (orphanRate > 0.1) {
      score -= 20 * orphanRate
      issues.value.push({
        type: 'warning',
        dimension: 'structure',
        message: `存在${metrics.orphanNodes}个孤立节点`,
        suggestion: '请检查并连接所有节点'
      })
    }

    structureScore.value = Math.max(0, Math.min(100, score))
  }

  /**
   * 计算性能分数
   */
  const calculatePerformanceScore = () => {
    let score = 100

    // 节点数量检查
    if (metrics.totalNodes > 50) {
      const penalty = (metrics.totalNodes - 50) * 2
      score -= penalty
      suggestions.value.push({
        dimension: 'performance',
        message: `流程节点过多（${metrics.totalNodes}个）`,
        suggestion: '建议将复杂流程拆分为子流程'
      })
    }

    // 网关数量检查
    if (metrics.gatewayCount > 10) {
      const penalty = (metrics.gatewayCount - 10) * 5
      score -= penalty
      suggestions.value.push({
        dimension: 'performance',
        message: `网关数量过多（${metrics.gatewayCount}个）`,
        suggestion: '考虑简化条件分支逻辑'
      })
    }

    // 流程深度检查
    if (metrics.maxDepth > 10) {
      score -= (metrics.maxDepth - 10) * 5
      suggestions.value.push({
        dimension: 'performance',
        message: `流程链过长（深度${metrics.maxDepth}）`,
        suggestion: '建议优化流程结构，减少嵌套'
      })
    }

    performanceScore.value = Math.max(0, Math.min(100, score))
  }

  /**
   * 计算完整性分数
   */
  const calculateCompletenessScore = () => {
    let score = 100

    // 流程信息检查
    if (!processInfo?.value?.name) {
      score -= 30
      issues.value.push({
        type: 'warning',
        dimension: 'completeness',
        message: '流程名称未设置',
        suggestion: '请为流程设置一个有意义的名称'
      })
    }

    // 表单配置检查
    if (metrics.userTaskCount > 0) {
      // 这里可以检查是否有表单配置
      // 暂时简化处理
    }

    completenessScore.value = Math.max(0, Math.min(100, score))
  }

  /**
   * 计算规范分数
   */
  const calculateStandardScore = () => {
    let score = 100

    // 未命名节点检查
    if (metrics.unnamedNodes > 0) {
      const penalty = metrics.unnamedNodes * 5
      score -= penalty
      issues.value.push({
        type: 'warning',
        dimension: 'standard',
        message: `${metrics.unnamedNodes}个节点未命名`,
        suggestion: '请为所有节点设置有意义的名称'
      })
    }

    // 无处理人任务检查
    if (metrics.noAssigneeTasks > 0) {
      const penalty = metrics.noAssigneeTasks * 10
      score -= penalty
      issues.value.push({
        type: 'warning',
        dimension: 'standard',
        message: `${metrics.noAssigneeTasks}个用户任务未指定处理人`,
        suggestion: '请为所有用户任务配置处理人'
      })
    }

    standardScore.value = Math.max(0, Math.min(100, score))
  }

  /**
   * 生成问题和建议列表
   */
  const generateIssuesAndSuggestions = () => {
    // 基于指标生成额外建议
    if (metrics.subProcessCount === 0 && metrics.totalNodes > 30) {
      suggestions.value.push({
        dimension: 'structure',
        message: '流程较复杂但未使用子流程',
        suggestion: '考虑将相关功能分组为子流程'
      })
    }

    if (metrics.exclusiveGatewayCount > metrics.parallelGatewayCount * 3) {
      suggestions.value.push({
        dimension: 'structure',
        message: '排他网关使用较多',
        suggestion: '检查是否有可以并行执行的任务'
      })
    }
  }

  /**
   * 获取维度评分名称
   */
  const getDimensionName = (dimension) => {
    const names = {
      structure: '结构分数',
      performance: '性能分数',
      completeness: '完整性分数',
      standard: '规范分数'
    }
    return names[dimension] || dimension
  }

  /**
   * 获取问题类型图标
   */
  const getIssueIcon = (type) => {
    return type === 'error' ? 'CircleClose' : 'Warning'
  }

  /**
   * 获取问题类型颜色
   */
  const getIssueColor = (type) => {
    return type === 'error' ? '#F56C6C' : '#E6A23C'
  }

  /**
   * 重置所有分数
   */
  const resetScores = () => {
    structureScore.value = 0
    performanceScore.value = 0
    completenessScore.value = 0
    standardScore.value = 0
    issues.value = []
    suggestions.value = []
    Object.keys(metrics).forEach(key => metrics[key] = 0)
  }

  return {
    // 状态
    dashboardVisible,
    calculating,
    structureScore,
    performanceScore,
    completenessScore,
    standardScore,
    overallScore,
    scoreLevel,
    overallComment,
    metrics,
    issues,
    suggestions,

    // 方法
    openDashboard,
    closeDashboard,
    calculateScores,
    calculateMetrics,
    calculateStructureScore,
    calculatePerformanceScore,
    calculateCompletenessScore,
    calculateStandardScore,
    generateIssuesAndSuggestions,
    getDimensionName,
    getIssueIcon,
    getIssueColor,
    resetScores
  }
}

export { SCORE_WEIGHTS, SCORE_LEVELS }
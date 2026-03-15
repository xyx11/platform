/**
 * 流程模拟组合式函数
 * 封装流程执行模拟、路径追踪等功能
 */

import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 模拟速度
 */
const SIMULATION_SPEED = {
  SLOW: 'slow',
  NORMAL: 'normal',
  FAST: 'fast'
}

/**
 * 速度配置
 */
const SPEED_CONFIG = {
  [SIMULATION_SPEED.SLOW]: { delay: 1000, label: '慢速' },
  [SIMULATION_SPEED.NORMAL]: { delay: 500, label: '正常' },
  [SIMULATION_SPEED.FAST]: { delay: 200, label: '快速' }
}

/**
 * 流程模拟
 * @param {Object} options - 配置选项
 * @returns {Object} 流程模拟 API
 */
export function useSimulation(options = {}) {
  const { bpmnModeler } = options

  // 状态
  const simulationVisible = ref(false)
  const simulationSpeed = ref(SIMULATION_SPEED.NORMAL)
  const simulating = ref(false)
  const currentStep = ref(0)
  const simulationPath = ref([])
  const simulationLogs = ref([])

  /**
   * 打开模拟对话框
   */
  const openSimulation = () => {
    resetSimulation()
    simulationVisible.value = true
  }

  /**
   * 重置模拟
   */
  const resetSimulation = () => {
    simulating.value = false
    currentStep.value = 0
    simulationPath.value = []
    simulationLogs.value = []
    clearSimulationMarkers()
  }

  /**
   * 开始模拟
   */
  const startSimulation = async () => {
    if (!bpmnModeler?.value) return

    simulating.value = true
    simulationLogs.value = ['开始流程模拟...']

    try {
      // 获取流程路径
      const path = await findSimulationPath()
      if (path.length === 0) {
        ElMessage.warning('未找到可执行的路径')
        simulating.value = false
        return
      }

      simulationPath.value = path
      simulationLogs.value.push(`找到路径：${path.length} 个节点`)

      // 开始执行
      await executeSimulation(path)
    } catch (error) {
      console.error('模拟失败:', error)
      ElMessage.error('模拟失败：' + error.message)
      simulating.value = false
    }
  }

  /**
   * 查找模拟路径
   */
  const findSimulationPath = async () => {
    const elementRegistry = bpmnModeler.value.get('elementRegistry')
    const elements = elementRegistry.getAll()

    // 找到开始事件
    const startEvent = elements.find(el => el.type === 'bpmn:StartEvent')
    if (!startEvent) {
      ElMessage.warning('未找到开始事件')
      return []
    }

    const path = []
    const visited = new Set()
    let current = startEvent

    while (current && !visited.has(current.id)) {
      path.push({
        id: current.id,
        name: current.businessObject?.name || current.type,
        type: current.type
      })
      visited.add(current.id)

      // 获取流出连线
      const flows = elements.filter(el =>
        el.type === 'bpmn:SequenceFlow' && el.source?.id === current.id
      )

      if (flows.length === 0) {
        // 到达结束节点
        break
      }

      // 取第一条连线（简单模拟，不考虑条件）
      const nextElement = flows[0].target
      if (nextElement && !visited.has(nextElement.id)) {
        current = nextElement
      } else {
        break
      }
    }

    return path
  }

  /**
   * 执行模拟
   */
  const executeSimulation = async (path) => {
    const speedConfig = SPEED_CONFIG[simulationSpeed.value]
    const canvas = bpmnModeler.value.get('canvas')

    for (let i = 0; i < path.length; i++) {
      if (!simulating.value) break

      currentStep.value = i
      const node = path[i]

      // 高亮当前节点
      const element = canvas.getElementById(node.id)
      if (element) {
        canvas.addMarker(element, 'simulating')
        canvas.scrollToElement(element, 100)
      }

      simulationLogs.value.push(
        `步骤 ${i + 1}: 执行 ${node.name || node.type}`
      )

      // 等待
      await sleep(speedConfig.delay)

      // 移除高亮
      if (element) {
        canvas.removeMarker(element, 'simulating')
        canvas.addMarker(element, 'simulated')
      }
    }

    simulating.value = false
    simulationLogs.value.push('模拟完成')
    ElMessage.success('流程模拟完成')
  }

  /**
   * 下一步
   */
  const nextStep = async () => {
    if (currentStep.value >= simulationPath.value.length - 1) {
      ElMessage.info('已到达流程末尾')
      return
    }

    currentStep.value++
    const node = simulationPath.value[currentStep.value]

    const canvas = bpmnModeler.value.get('canvas')
    const element = canvas.getElementById(node.id)

    if (element) {
      canvas.addMarker(element, 'simulating')
      setTimeout(() => {
        canvas.removeMarker(element, 'simulating')
        canvas.addMarker(element, 'simulated')
      }, 500)
    }

    simulationLogs.value.push(
      `步骤 ${currentStep.value + 1}: 执行 ${node.name || node.type}`
    )
  }

  /**
   * 清除模拟标记
   */
  const clearSimulationMarkers = () => {
    if (!bpmnModeler?.value) return

    const canvas = bpmnModeler.value.get('canvas')
    const elementRegistry = bpmnModeler.value.get('elementRegistry')
    const elements = elementRegistry.getAll()

    elements.forEach(el => {
      canvas.removeMarker(el, 'simulating')
      canvas.removeMarker(el, 'simulated')
    })
  }

  /**
   * 获取速度选项
   */
  const getSpeedOptions = () => {
    return Object.entries(SPEED_CONFIG).map(([value, { label }]) => ({
      value,
      label
    }))
  }

  /**
   * 睡眠函数
   */
  const sleep = (ms) => {
    return new Promise(resolve => setTimeout(resolve, ms))
  }

  return {
    // 状态
    simulationVisible,
    simulationSpeed,
    simulating,
    currentStep,
    simulationPath,
    simulationLogs,

    // 方法
    openSimulation,
    resetSimulation,
    startSimulation,
    nextStep,
    getSpeedOptions
  }
}

export { SIMULATION_SPEED, SPEED_CONFIG }
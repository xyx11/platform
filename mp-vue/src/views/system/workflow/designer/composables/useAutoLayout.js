/**
 * 自动布局组合式函数
 * 封装流程自动布局、节点排列等功能
 */

import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 布局方向
 */
const LAYOUT_DIRECTION = {
  HORIZONTAL: 'horizontal',
  VERTICAL: 'vertical'
}

/**
 * 自动布局
 * @param {Object} options - 配置选项
 * @returns {Object} 自动布局 API
 */
export function useAutoLayout(options = {}) {
  const { bpmnModeler } = options

  // 状态
  const autoLayoutVisible = ref(false)
  const autoLayoutForm = reactive({
    direction: LAYOUT_DIRECTION.HORIZONTAL,
    spacing: 150,
    rowSpacing: 100
  })

  /**
   * 打开自动布局对话框
   */
  const openAutoLayout = () => {
    autoLayoutVisible.value = true
  }

  /**
   * 执行自动布局
   */
  const executeAutoLayout = async () => {
    if (!bpmnModeler?.value) return false

    try {
      const elementRegistry = bpmnModeler.value.get('elementRegistry')
      const modeling = bpmnModeler.value.get('modeling')
      const elements = elementRegistry.getAll()

      // 过滤出需要布局的元素（排除连线和 Process）
      const nodes = elements.filter(el =>
        el.type &&
        !el.type.includes('Flow') &&
        el.type !== 'bpmn:Process' &&
        el.type !== 'bpmn:Definitions'
      )

      if (nodes.length === 0) {
        ElMessage.warning('没有可布局的元素')
        return false
      }

      // 按类型分组
      const grouped = groupNodesByType(nodes)

      // 执行布局
      layoutNodes(grouped, autoLayoutForm)

      ElMessage.success('自动布局完成')
      autoLayoutVisible.value = false
      return true
    } catch (error) {
      console.error('自动布局失败:', error)
      ElMessage.error('自动布局失败：' + error.message)
      return false
    }
  }

  /**
   * 按类型分组节点
   */
  const groupNodesByType = (nodes) => {
    const groups = {
      startEvents: [],
      endEvents: [],
      tasks: [],
      gateways: [],
      data: [],
      subprocess: []
    }

    nodes.forEach(node => {
      const type = node.type
      if (type?.includes('StartEvent')) {
        groups.startEvents.push(node)
      } else if (type?.includes('EndEvent')) {
        groups.endEvents.push(node)
      } else if (type?.includes('Gateway')) {
        groups.gateways.push(node)
      } else if (type?.includes('SubProcess')) {
        groups.subprocess.push(node)
      } else if (type?.includes('Data')) {
        groups.data.push(node)
      } else if (type?.includes('Task')) {
        groups.tasks.push(node)
      }
    })

    return groups
  }

  /**
   * 布局节点
   */
  const layoutNodes = (grouped, options) => {
    const { direction, spacing, rowSpacing } = options
    const modeling = bpmnModeler.value.get('modeling')

    let currentX = 100
    let currentY = 100

    // 开始事件排在最前面
    if (direction === LAYOUT_DIRECTION.HORIZONTAL) {
      // 从左到右布局
      layoutRow(grouped.startEvents, currentX, currentY, spacing, modeling)
      currentX += (grouped.startEvents.length * spacing) + 50

      // 任务在中间
      layoutRow(grouped.tasks, currentX, currentY, spacing, modeling)
      currentX += (grouped.tasks.length * spacing) + 50

      // 网关在任务之后
      layoutRow(grouped.gateways, currentX, currentY, spacing, modeling)
      currentX += (grouped.gateways.length * spacing) + 50

      // 结束事件在最后
      layoutRow(grouped.endEvents, currentX, currentY, spacing, modeling)
    } else {
      // 从上到下布局
      let yOffset = 0
      layoutRow(grouped.startEvents, currentX, currentY + yOffset, spacing, modeling)
      yOffset += rowSpacing * 2

      layoutRow(grouped.tasks, currentX, currentY + yOffset, spacing, modeling)
      yOffset += rowSpacing * 3

      layoutRow(grouped.gateways, currentX, currentY + yOffset, spacing, modeling)
      yOffset += rowSpacing * 2

      layoutRow(grouped.endEvents, currentX, currentY + yOffset, spacing, modeling)
    }
  }

  /**
   * 布局一行节点
   */
  const layoutRow = (nodes, startX, startY, spacing, modeling) => {
    nodes.forEach((node, index) => {
      const x = startX + (index * spacing)
      modeling.moveElements([node], { x: x - node.x, y: startY - node.y }, node.parent)
    })
  }

  /**
   * 对齐选中元素
   */
  const alignSelected = (alignment) => {
    if (!bpmnModeler?.value) return

    const selection = bpmnModeler.value.get('selection')
    const selected = selection.get()

    if (selected.length < 2) {
      ElMessage.warning('请选择至少两个元素进行对齐')
      return
    }

    const modeling = bpmnModeler.value.get('modeling')
    const bounds = getSelectionBounds(selected)

    selected.forEach((el, index) => {
      // 第一个元素作为参考，不移动
      if (index === 0) return

      const delta = calculateAlignmentDelta(el, bounds, alignment)
      if (delta.x !== 0 || delta.y !== 0) {
        modeling.moveElements([el], delta, el.parent)
      }
    })

    ElMessage.success(`已${getAlignmentName(alignment)}`)
  }

  /**
   * 获取选中元素的边界
   */
  const getSelectionBounds = (elements) => {
    let minX = Infinity, minY = Infinity
    let maxX = -Infinity, maxY = -Infinity

    elements.forEach(el => {
      const x = el.x || 0
      const y = el.y || 0
      const width = el.width || 100
      const height = el.height || 80

      minX = Math.min(minX, x)
      minY = Math.min(minY, y)
      maxX = Math.max(maxX, x + width)
      maxY = Math.max(maxY, y + height)
    })

    return { minX, minY, maxX, maxY, width: maxX - minX, height: maxY - minY }
  }

  /**
   * 计算对齐偏移量
   */
  const calculateAlignmentDelta = (element, bounds, alignment) => {
    const x = element.x || 0
    const y = element.y || 0
    const width = element.width || 100
    const height = element.height || 80

    let deltaX = 0
    let deltaY = 0

    switch (alignment) {
      case 'alignLeft':
        deltaX = bounds.minX - x
        break
      case 'alignCenter':
        deltaX = bounds.minX + (bounds.width - width) / 2 - x
        break
      case 'alignRight':
        deltaX = bounds.maxX - (x + width)
        break
      case 'alignTop':
        deltaY = bounds.minY - y
        break
      case 'alignMiddle':
        deltaY = bounds.minY + (bounds.height - height) / 2 - y
        break
      case 'alignBottom':
        deltaY = bounds.maxY - (y + height)
        break
    }

    return { x: deltaX, y: deltaY }
  }

  /**
   * 获取对齐名称
   */
  const getAlignmentName = (alignment) => {
    const names = {
      alignLeft: '左对齐',
      alignCenter: '水平居中',
      alignRight: '右对齐',
      alignTop: '上对齐',
      alignMiddle: '垂直居中',
      alignBottom: '下对齐'
    }
    return names[alignment] || alignment
  }

  /**
   * 获取布局方向选项
   */
  const getDirectionOptions = () => {
    return [
      { value: LAYOUT_DIRECTION.HORIZONTAL, label: '从左到右' },
      { value: LAYOUT_DIRECTION.VERTICAL, label: '从上到下' }
    ]
  }

  return {
    // 状态
    autoLayoutVisible,
    autoLayoutForm,

    // 方法
    openAutoLayout,
    executeAutoLayout,
    alignSelected,
    getDirectionOptions
  }
}

export { LAYOUT_DIRECTION }
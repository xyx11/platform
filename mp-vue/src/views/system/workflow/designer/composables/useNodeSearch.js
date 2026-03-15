/**
 * 节点搜索组合式函数
 * 封装节点搜索、定位、高亮等功能
 */

import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 节点搜索
 * @param {Object} options - 配置选项
 * @returns {Object} 搜索 API
 */
export function useNodeSearch(options = {}) {
  const { bpmnModeler } = options

  // 状态
  const searchText = ref('')
  const searchResults = ref([])
  const currentIndex = ref(-1)
  const isSearching = ref(false)

  /**
   * 执行搜索
   */
  const search = (text) => {
    if (!bpmnModeler?.value || !text) {
      searchResults.value = []
      currentIndex.value = -1
      return []
    }

    isSearching.value = true
    const elementRegistry = bpmnModeler.value.get('elementRegistry')
    const elements = elementRegistry.getAll()

    const keyword = text.toLowerCase()
    const results = []

    elements.forEach(el => {
      const bo = el.businessObject
      if (!bo) return

      // 搜索 ID 和名称
      const id = bo.id || ''
      const name = bo.name || ''
      const type = bo.$type || ''

      if (
        id.toLowerCase().includes(keyword) ||
        name.toLowerCase().includes(keyword) ||
        type.toLowerCase().includes(keyword)
      ) {
        results.push({
          id: bo.id,
          name: name || '(未命名)',
          type: getElementTypeDisplayName(type),
          element: el
        })
      }
    })

    searchResults.value = results
    currentIndex.value = results.length > 0 ? 0 : -1
    isSearching.value = false

    if (results.length === 0) {
      ElMessage.info('未找到匹配的节点')
    }

    return results
  }

  /**
   * 获取元素类型显示名称
   */
  const getElementTypeDisplayName = (type) => {
    const typeMap = {
      'bpmn:StartEvent': '开始事件',
      'bpmn:EndEvent': '结束事件',
      'bpmn:UserTask': '用户任务',
      'bpmn:ServiceTask': '服务任务',
      'bpmn:ScriptTask': '脚本任务',
      'bpmn:ExclusiveGateway': '排他网关',
      'bpmn:ParallelGateway': '并行网关',
      'bpmn:InclusiveGateway': '包容网关',
      'bpmn:SubProcess': '子流程',
      'bpmn:SequenceFlow': '顺序流'
    }
    return typeMap[type] || type
  }

  /**
   * 定位到下一个结果
   */
  const findNext = () => {
    if (searchResults.value.length === 0) return null

    currentIndex.value = (currentIndex.value + 1) % searchResults.value.length
    const result = searchResults.value[currentIndex.value]
    focusElement(result.element)
    return result
  }

  /**
   * 定位到上一个结果
   */
  const findPrevious = () => {
    if (searchResults.value.length === 0) return null

    currentIndex.value = currentIndex.value <= 0
      ? searchResults.value.length - 1
      : currentIndex.value - 1

    const result = searchResults.value[currentIndex.value]
    focusElement(result.element)
    return result
  }

  /**
   * 聚焦到指定元素
   */
  const focusElement = (element) => {
    if (!bpmnModeler?.value || !element) return

    const canvas = bpmnModeler.value.get('canvas')
    const selection = bpmnModeler.value.get('selection')

    // 选中元素
    selection.select(element)

    // 滚动到元素位置
    canvas.scrollToElement(element, 50)

    // 添加高亮效果
    canvas.addMarker(element, 'highlight-search-result')

    // 2 秒后移除高亮
    setTimeout(() => {
      canvas.removeMarker(element, 'highlight-search-result')
    }, 2000)
  }

  /**
   * 定位到指定节点
   */
  const focusNode = (nodeId) => {
    if (!bpmnModeler?.value || !nodeId) return

    const elementRegistry = bpmnModeler.value.get('elementRegistry')
    const element = elementRegistry.get(nodeId)

    if (element) {
      focusElement(element)
      return true
    }

    ElMessage.warning('未找到该节点')
    return false
  }

  /**
   * 清除搜索
   */
  const clearSearch = () => {
    searchText.value = ''
    searchResults.value = []
    currentIndex.value = -1

    // 清除所有高亮
    if (bpmnModeler?.value) {
      const canvas = bpmnModeler.value.get('canvas')
      const elementRegistry = bpmnModeler.value.get('elementRegistry')
      elementRegistry.getAll().forEach(el => {
        canvas.removeMarker(el, 'highlight-search-result')
      })
    }
  }

  /**
   * 获取搜索结果统计
   */
  const searchStats = computed(() => ({
    total: searchResults.value.length,
    current: currentIndex.value >= 0 ? currentIndex.value + 1 : 0,
    hasResults: searchResults.value.length > 0
  }))

  return {
    // 状态
    searchText,
    searchResults,
    currentIndex,
    isSearching,

    // 计算属性
    searchStats,

    // 方法
    search,
    findNext,
    findPrevious,
    focusElement,
    focusNode,
    clearSearch
  }
}
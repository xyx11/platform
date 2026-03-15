/**
 * 快捷键管理组合式函数
 */

import { ref, onMounted, onBeforeUnmount } from 'vue'

/**
 * 默认快捷键配置
 */
const DEFAULT_SHORTCUTS = {
  'Ctrl-N': 'newDiagram',
  'Ctrl-O': 'openFile',
  'Ctrl-S': 'saveDiagram',
  'Ctrl-Shift-S': 'saveAsDiagram',
  'Ctrl-D': 'duplicateProcess',
  'Ctrl-Z': 'undo',
  'Ctrl-Y': 'redo',
  'Delete': 'deleteSelected',
  'Backspace': 'deleteSelected',
  '+': 'zoomIn',
  '-': 'zoomOut',
  '0': 'zoomFit',
  'Ctrl-P': 'previewDiagram',
  'Ctrl-B': 'deployDiagram',
  'Ctrl-H': 'showHelp',
  'Ctrl-F': 'searchNode',
  'F5': 'refreshDiagram'
}

/**
 * 忽略快捷键的输入元素标签
 */
const IGNORE_INPUT_TAGS = ['INPUT', 'TEXTAREA', 'SELECT']

/**
 * 快捷键管理
 * @param {Object} actionHandlers - 动作处理函数映射
 * @returns {Object} 快捷键 API
 */
export function useShortcuts(actionHandlers = {}) {
  const isRecording = ref(false)
  const recordingShortcut = ref(null)
  const customShortcuts = ref({})

  // 从 localStorage 加载自定义快捷键
  const loadCustomShortcuts = () => {
    const saved = localStorage.getItem('workflow-shortcuts')
    if (saved) {
      try {
        customShortcuts.value = JSON.parse(saved)
      } catch (e) {
        console.error('加载快捷键配置失败:', e)
      }
    }
  }

  // 保存自定义快捷键到 localStorage
  const saveCustomShortcuts = () => {
    localStorage.setItem('workflow-shortcuts', JSON.stringify(customShortcuts.value))
  }

  // 重置为默认快捷键
  const resetShortcuts = () => {
    customShortcuts.value = {}
    saveCustomShortcuts()
  }

  /**
   * 获取快捷键对应的动作
   */
  const getShortcutAction = (shortcut) => {
    // 优先使用自定义快捷键
    if (customShortcuts.value[shortcut]) {
      return customShortcuts.value[shortcut]
    }
    return DEFAULT_SHORTCUTS[shortcut]
  }

  /**
   * 解析按键组合
   */
  const parseKeyCombo = (e) => {
    const key = []
    if (e.ctrlKey || e.metaKey) key.push('Ctrl')
    if (e.shiftKey) key.push('Shift')
    if (e.altKey) key.push('Alt')
    key.push(e.key)
    return key.join('-')
  }

  /**
   * 键盘事件处理
   */
  const handleKeyDown = (e) => {
    // 忽略输入框内的快捷键
    if (IGNORE_INPUT_TAGS.includes(e.target.tagName)) {
      return
    }

    const shortcut = parseKeyCombo(e)
    const action = getShortcutAction(shortcut)

    if (action && actionHandlers[action]) {
      e.preventDefault()
      actionHandlers[action]()
    }
  }

  /**
   * 注册快捷键处理函数
   */
  const registerHandler = (action, handler) => {
    actionHandlers[action] = handler
  }

  /**
   * 设置自定义快捷键
   */
  const setCustomShortcut = (oldCombo, newCombo) => {
    // 删除旧的
    if (oldCombo && customShortcuts.value[oldCombo]) {
      const action = customShortcuts.value[oldCombo]
      delete customShortcuts.value[oldCombo]
      if (newCombo) {
        customShortcuts.value[newCombo] = action
      }
    } else if (newCombo) {
      customShortcuts.value[newCombo] = customShortcuts.value[oldCombo]
    }
    saveCustomShortcuts()
  }

  /**
   * 获取所有快捷键配置（用于显示）
   */
  const getAllShortcuts = () => {
    const all = { ...DEFAULT_SHORTCUTS }
    // 合并自定义快捷键
    Object.entries(customShortcuts.value).forEach(([combo, action]) => {
      all[combo] = action
    })
    return all
  }

  /**
   * 获取快捷键描述列表（用于设置界面）
   */
  const getShortcutList = () => {
    const actionDescriptions = {
      newDiagram: '新建流程',
      openFile: '打开文件',
      saveDiagram: '保存流程',
      saveAsDiagram: '另存为',
      duplicateProcess: '复制流程',
      undo: '撤销',
      redo: '重做',
      deleteSelected: '删除',
      zoomIn: '放大',
      zoomOut: '缩小',
      zoomFit: '适应屏幕',
      previewDiagram: '预览',
      deployDiagram: '部署',
      showHelp: '帮助',
      searchNode: '搜索节点',
      refreshDiagram: '刷新'
    }

    const merged = getAllShortcuts()
    const actionToShortcut = {}

    // 反转映射：action -> shortcut
    Object.entries(merged).forEach(([combo, action]) => {
      if (!actionToShortcut[action]) {
        actionToShortcut[action] = combo
      }
    })

    // 生成列表
    return Object.entries(actionDescriptions).map(([action, desc]) => ({
      action,
      description: desc,
      shortcut: actionToShortcut[action] || ''
    }))
  }

  onMounted(() => {
    loadCustomShortcuts()
    document.addEventListener('keydown', handleKeyDown)
  })

  onBeforeUnmount(() => {
    document.removeEventListener('keydown', handleKeyDown)
  })

  return {
    // 状态
    isRecording,
    recordingShortcut,

    // 方法
    registerHandler,
    setCustomShortcut,
    resetShortcuts,
    getAllShortcuts,
    getShortcutList,
    handleKeyDown
  }
}
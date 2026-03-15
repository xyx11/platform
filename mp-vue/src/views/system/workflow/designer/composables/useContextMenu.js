/**
 * 右键菜单组合式函数
 * 封装上下文菜单、快捷操作等功能
 */

import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 右键菜单选项
 */
const MENU_ACTIONS = {
  UNDO: 'undo',
  REDO: 'redo',
  COPY: 'copy',
  PASTE: 'paste',
  DUPLICATE: 'duplicate',
  DELETE: 'delete',
  ALIGN_LEFT: 'alignLeft',
  ALIGN_CENTER: 'alignCenter',
  ALIGN_RIGHT: 'alignRight',
  ALIGN_TOP: 'alignTop',
  ALIGN_MIDDLE: 'alignMiddle',
  ALIGN_BOTTOM: 'alignBottom',
  PROPERTIES: 'properties'
}

/**
 * 右键菜单
 * @param {Object} options - 配置选项
 * @returns {Object} 菜单 API
 */
export function useContextMenu(options = {}) {
  const { bpmnModeler, onAction } = options

  // 状态
  const contextMenuVisible = ref(false)
  const contextMenuX = ref(0)
  const contextMenuY = ref(0)
  const contextMenuTarget = ref(null)

  /**
   * 显示菜单
   */
  const showMenu = (event, target = null) => {
    event.preventDefault()
    event.stopPropagation()

    contextMenuX.value = event.clientX
    contextMenuY.value = event.clientY
    contextMenuTarget.value = target
    contextMenuVisible.value = true
  }

  /**
   * 隐藏菜单
   */
  const hideMenu = () => {
    contextMenuVisible.value = false
    contextMenuTarget.value = null
  }

  /**
   * 执行菜单动作
   */
  const executeAction = async (action) => {
    hideMenu()

    try {
      switch (action) {
        case MENU_ACTIONS.UNDO:
          if (bpmnModeler?.value) {
            bpmnModeler.value.get('commandStack').undo()
            ElMessage.success('已撤销')
          }
          break

        case MENU_ACTIONS.REDO:
          if (bpmnModeler?.value) {
            bpmnModeler.value.get('commandStack').redo()
            ElMessage.success('已重做')
          }
          break

        case MENU_ACTIONS.DELETE:
          await deleteSelected()
          break

        case MENU_ACTIONS.COPY:
          await copySelected()
          break

        case MENU_ACTIONS.PASTE:
          await pasteSelected()
          break

        case MENU_ACTIONS.DUPLICATE:
          await duplicateSelected()
          break

        case MENU_ACTIONS.ALIGN_LEFT:
        case MENU_ACTIONS.ALIGN_CENTER:
        case MENU_ACTIONS.ALIGN_RIGHT:
        case MENU_ACTIONS.ALIGN_TOP:
        case MENU_ACTIONS.ALIGN_MIDDLE:
        case MENU_ACTIONS.ALIGN_BOTTOM:
          await alignSelected(action)
          break

        case MENU_ACTIONS.PROPERTIES:
          onAction?.('properties')
          break

        default:
          onAction?.(action)
      }
    } catch (error) {
      console.error('执行菜单动作失败:', error)
      ElMessage.error('操作失败：' + error.message)
    }
  }

  /**
   * 删除选中元素
   */
  const deleteSelected = () => {
    if (!bpmnModeler?.value) return

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
   * 复制选中元素
   */
  const copySelected = async () => {
    if (!bpmnModeler?.value) return

    const selection = bpmnModeler.value.get('selection')
    const selected = selection.get()

    if (selected.length === 0) {
      ElMessage.warning('请先选择要复制的元素')
      return
    }

    // 使用 bpmn-js 的复制功能
    const clipboard = bpmnModeler.value.get('clipboard')
    const copyPaste = bpmnModeler.value.get('copyPaste')

    copyPaste.copy(selected)
    ElMessage.success('已复制')
  }

  /**
   * 粘贴元素
   */
  const pasteSelected = async () => {
    if (!bpmnModeler?.value) return

    const copyPaste = bpmnModeler.value.get('copyPaste')
    const canvas = bpmnModeler.value.get('canvas')

    try {
      const context = copyPaste.paste({
        element: canvas.getRootElement(),
        point: { x: 100, y: 100 }
      })
      ElMessage.success('已粘贴')
    } catch (error) {
      ElMessage.info('剪贴板为空')
    }
  }

  /**
   * 快速复制（复制并粘贴）
   */
  const duplicateSelected = async () => {
    if (!bpmnModeler?.value) return

    const selection = bpmnModeler.value.get('selection')
    const selected = selection.get()

    if (selected.length === 0) {
      ElMessage.warning('请先选择要复制的元素')
      return
    }

    const copyPaste = bpmnModeler.value.get('copyPaste')
    const canvas = bpmnModeler.value.get('canvas')

    // 复制
    copyPaste.copy(selected)

    // 粘贴（偏移 50 像素）
    const rootElement = canvas.getRootElement()
    copyPaste.paste({
      element: rootElement,
      point: { x: 150, y: 150 }
    })

    ElMessage.success('已复制')
  }

  /**
   * 对齐选中元素
   */
  const alignSelected = async (alignment) => {
    if (!bpmnModeler?.value) return

    const selection = bpmnModeler.value.get('selection')
    const selected = selection.get()

    if (selected.length < 2) {
      ElMessage.warning('请选择至少两个元素进行对齐')
      return
    }

    // bpmn-js 本身不支持对齐，需要手动计算位置
    ElMessage.info('对齐功能需要选中多个元素')

    // TODO: 实现对齐逻辑
    // 这里只是一个框架，实际需要对齐需要计算元素位置
  }

  /**
   * 初始化右键菜单
   */
  const initContextMenu = (container) => {
    if (!container) return

    container.addEventListener('contextmenu', (event) => {
      showMenu(event, event.target)
    })

    // 点击其他地方关闭菜单
    document.addEventListener('click', () => {
      hideMenu()
    })
  }

  /**
   * 获取菜单项配置
   */
  const getMenuItems = () => {
    return [
      { action: MENU_ACTIONS.UNDO, label: '撤销', icon: 'RefreshLeft' },
      { action: MENU_ACTIONS.REDO, label: '重做', icon: 'RefreshRight' },
      { type: 'divider' },
      { action: MENU_ACTIONS.COPY, label: '复制', icon: 'DocumentCopy' },
      { action: MENU_ACTIONS.PASTE, label: '粘贴', icon: 'Document' },
      { action: MENU_ACTIONS.DUPLICATE, label: '快速复制', icon: 'Files' },
      { type: 'divider' },
      { action: MENU_ACTIONS.DELETE, label: '删除', icon: 'Delete' },
      { type: 'divider' },
      {
        label: '对齐',
        submenu: [
          { action: MENU_ACTIONS.ALIGN_LEFT, label: '左对齐' },
          { action: MENU_ACTIONS.ALIGN_CENTER, label: '水平居中' },
          { action: MENU_ACTIONS.ALIGN_RIGHT, label: '右对齐' },
          { type: 'divider' },
          { action: MENU_ACTIONS.ALIGN_TOP, label: '上对齐' },
          { action: MENU_ACTIONS.ALIGN_MIDDLE, label: '垂直居中' },
          { action: MENU_ACTIONS.ALIGN_BOTTOM, label: '下对齐' }
        ]
      },
      { type: 'divider' },
      { action: MENU_ACTIONS.PROPERTIES, label: '属性', icon: 'Setting' }
    ]
  }

  return {
    // 状态
    contextMenuVisible,
    contextMenuX,
    contextMenuY,
    contextMenuTarget,

    // 方法
    showMenu,
    hideMenu,
    executeAction,
    initContextMenu,
    getMenuItems,
    deleteSelected,
    copySelected,
    pasteSelected,
    duplicateSelected,
    alignSelected
  }
}

export { MENU_ACTIONS }
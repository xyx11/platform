/**
 * 协作编辑组合式函数
 * 封装流程设计器的协作编辑功能，包括在线用户、编辑锁、实时同步等
 */

import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { logger } from '@/utils/logger'

/**
 * 用户状态
 */
const USER_STATUS = {
  ONLINE: 'online',
  EDITING: 'editing',
  IDLE: 'idle',
  OFFLINE: 'offline'
}

/**
 * 操作类型
 */
const OPERATION_TYPE = {
  CREATE: 'create',
  UPDATE: 'update',
  DELETE: 'delete',
  MOVE: 'move',
  RESIZE: 'resize'
}

/**
 * 协作编辑
 * @param {Object} options - 配置选项
 * @returns {Object} 协作编辑 API
 */
export function useCollaboration(options = {}) {
  const { bpmnModeler, processInfo } = options

  // 状态
  const collabVisible = ref(false)
  const connected = ref(false)
  const isLocked = ref(false)
  const lockedBy = ref(null)

  // 在线用户列表
  const onlineUsers = ref([])

  // 当前用户
  const currentUser = reactive({
    id: null,
    name: '当前用户',
    color: generateRandomColor(),
    status: USER_STATUS.ONLINE
  })

  // 协作操作历史
  const operationHistory = ref([])

  // 事件监听器
  let eventListeners = []
  let heartbeatTimer = null
  let syncTimer = null

  /**
   * 生成随机颜色
   */
  function generateRandomColor() {
    const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#A0CFF9', '#B7EB8F', '#FFE58F', '#FFC1C1']
    return colors[Math.floor(Math.random() * colors.length)]
  }

  /**
   * 打开协作对话框
   */
  const openCollaboration = () => {
    collabVisible.value = true
  }

  /**
   * 关闭协作对话框
   */
  const closeCollaboration = () => {
    collabVisible.value = false
  }

  /**
   * 初始化协作编辑
   */
  const initCollaboration = () => {
    // 模拟连接协作服务
    connectCollaboration()

    // 启动心跳
    startHeartbeat()

    // 启动自动同步
    startAutoSync()

    // 监听 BPMN 变化
    listenToModelChanges()

    ElMessage.success('协作编辑已启用')
  }

  /**
   * 连接协作服务
   */
  const connectCollaboration = () => {
    // 模拟连接
    connected.value = true

    // 模拟在线用户
    onlineUsers.value = [
      {
        id: 1,
        name: '张三',
        color: '#409EFF',
        status: USER_STATUS.EDITING,
        currentElement: 'Task_1'
      },
      {
        id: 2,
        name: '李四',
        color: '#67C23A',
        status: USER_STATUS.ONLINE,
        currentElement: null
      }
    ]

    currentUser.id = Date.now()
  }

  /**
   * 断开协作服务
   */
  const disconnectCollaboration = () => {
    connected.value = false
    stopHeartbeat()
    stopAutoSync()
    cleanupEventListeners()

    ElMessage.info('协作编辑已断开')
  }

  /**
   * 启动心跳
   */
  const startHeartbeat = () => {
    heartbeatTimer = setInterval(() => {
      // 发送心跳
      sendHeartbeat()
    }, 30000)
  }

  /**
   * 停止心跳
   */
  const stopHeartbeat = () => {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }

  /**
   * 发送心跳
   */
  const sendHeartbeat = () => {
    // 模拟发送心跳
    logger.log('发送心跳...', currentUser)
  }

  /**
   * 启动自动同步
   */
  const startAutoSync = () => {
    syncTimer = setInterval(() => {
      syncWithServer()
    }, 5000)
  }

  /**
   * 停止自动同步
   */
  const stopAutoSync = () => {
    if (syncTimer) {
      clearInterval(syncTimer)
      syncTimer = null
    }
  }

  /**
   * 与服务器同步
   */
  const syncWithServer = () => {
    if (!connected.value) return

    // 模拟同步
    logger.log('同步数据...')
  }

  /**
   * 监听模型变化
   */
  const listenToModelChanges = () => {
    if (!bpmnModeler?.value) return

    const eventBus = bpmnModeler.value.get('eventBus')

    // 监听元素创建
    const createHandler = (event) => {
      recordOperation(OPERATION_TYPE.CREATE, event.context)
      broadcastChange(OPERATION_TYPE.CREATE, event.context)
    }
    eventBus.on('shape.added', createHandler)
    eventListeners.push({ event: 'shape.added', handler: createHandler })

    // 监听元素删除
    const deleteHandler = (event) => {
      recordOperation(OPERATION_TYPE.DELETE, event.context)
      broadcastChange(OPERATION_TYPE.DELETE, event.context)
    }
    eventBus.on('shape.removed', deleteHandler)
    eventListeners.push({ event: 'shape.removed', handler: deleteHandler })

    // 监听元素移动
    const moveHandler = (event) => {
      recordOperation(OPERATION_TYPE.MOVE, event.context)
      broadcastChange(OPERATION_TYPE.MOVE, event.context)
    }
    eventBus.on('shape.move', moveHandler)
    eventListeners.push({ event: 'shape.move', handler: moveHandler })

    // 监听元素调整大小
    const resizeHandler = (event) => {
      recordOperation(OPERATION_TYPE.RESIZE, event.context)
      broadcastChange(OPERATION_TYPE.RESIZE, event.context)
    }
    eventBus.on('shape.resize', resizeHandler)
    eventListeners.push({ event: 'shape.resize', handler: resizeHandler })
  }

  /**
   * 清理事件监听器
   */
  const cleanupEventListeners = () => {
    if (!bpmnModeler?.value) return

    const eventBus = bpmnModeler.value.get('eventBus')
    eventListeners.forEach(({ event, handler }) => {
      eventBus.off(event, handler)
    })
    eventListeners = []
  }

  /**
   * 记录操作
   */
  const recordOperation = (type, context) => {
    const operation = {
      id: Date.now(),
      type,
      userId: currentUser.id,
      userName: currentUser.name,
      timestamp: new Date().toISOString(),
      context: serializeContext(context)
    }
    operationHistory.value.unshift(operation)

    // 保留最近 100 条记录
    if (operationHistory.value.length > 100) {
      operationHistory.value = operationHistory.value.slice(0, 100)
    }
  }

  /**
   * 序列化上下文
   */
  const serializeContext = (context) => {
    if (!context) return {}

    const serialized = {}
    if (context.shape) {
      serialized.elementId = context.shape.id
      serialized.elementType = context.shape.businessObject?.$type
      serialized.name = context.shape.businessObject?.name
    }
    return serialized
  }

  /**
   * 广播变更
   */
  const broadcastChange = (type, context) => {
    if (!connected.value) return

    // 模拟广播变更
    logger.log('广播变更:', type, context)
  }

  /**
   * 应用远程变更
   */
  const applyRemoteChange = (operation) => {
    if (!bpmnModeler?.value) return

    // 忽略自己的操作
    if (operation.userId === currentUser.id) return

    const modeling = bpmnModeler.value.get('modeling')
    const elementRegistry = bpmnModeler.value.get('elementRegistry')

    switch (operation.type) {
      case OPERATION_TYPE.CREATE:
        // 创建元素
        break
      case OPERATION_TYPE.DELETE:
        const element = elementRegistry.get(operation.context.elementId)
        if (element) {
          modeling.removeElements([element])
        }
        break
      case OPERATION_TYPE.MOVE:
        // 移动元素
        break
      case OPERATION_TYPE.RESIZE:
        // 调整大小
        break
    }
  }

  /**
   * 锁定流程
   */
  const lockProcess = async () => {
    if (isLocked.value) {
      ElMessage.warning('流程已被其他用户锁定')
      return false
    }

    // 模拟锁定
    isLocked.value = true
    lockedBy.value = currentUser

    ElMessage.success('流程已锁定')
    return true
  }

  /**
   * 解锁流程
   */
  const unlockProcess = async () => {
    if (!isLocked.value) {
      ElMessage.warning('流程未被锁定')
      return false
    }

    // 模拟解锁
    isLocked.value = false
    lockedBy.value = null

    ElMessage.success('流程已解锁')
    return true
  }

  /**
   * 获取在线用户数量
   */
  const getOnlineCount = () => {
    return onlineUsers.value.filter(u => u.status !== USER_STATUS.OFFLINE).length
  }

  /**
   * 获取编辑中的用户
   */
  const getEditingUsers = () => {
    return onlineUsers.value.filter(u => u.status === USER_STATUS.EDITING)
  }

  /**
   * 获取状态名称
   */
  const getStatusName = (status) => {
    const names = {
      [USER_STATUS.ONLINE]: '在线',
      [USER_STATUS.EDITING]: '编辑中',
      [USER_STATUS.IDLE]: '空闲',
      [USER_STATUS.OFFLINE]: '离线'
    }
    return names[status] || status
  }

  /**
   * 获取状态颜色
   */
  const getStatusColor = (status) => {
    const colors = {
      [USER_STATUS.ONLINE]: '#67C23A',
      [USER_STATUS.EDITING]: '#409EFF',
      [USER_STATUS.IDLE]: '#909399',
      [USER_STATUS.OFFLINE]: '#C0C4CC'
    }
    return colors[status] || '#C0C4CC'
  }

  /**
   * 获取操作类型名称
   */
  const getOperationTypeName = (type) => {
    const names = {
      [OPERATION_TYPE.CREATE]: '创建',
      [OPERATION_TYPE.UPDATE]: '更新',
      [OPERATION_TYPE.DELETE]: '删除',
      [OPERATION_TYPE.MOVE]: '移动',
      [OPERATION_TYPE.RESIZE]: '调整大小'
    }
    return names[type] || type
  }

  /**
   * 清除协作数据
   */
  const clearCollaborationData = () => {
    onlineUsers.value = []
    operationHistory.value = []
    isLocked.value = false
    lockedBy.value = null
  }

  onMounted(() => {
    initCollaboration()
  })

  onBeforeUnmount(() => {
    disconnectCollaboration()
  })

  return {
    // 状态
    collabVisible,
    connected,
    isLocked,
    lockedBy,
    onlineUsers,
    currentUser,
    operationHistory,

    // 计算
    getOnlineCount,
    getEditingUsers,
    getStatusName,
    getStatusColor,
    getOperationTypeName,

    // 方法
    openCollaboration,
    closeCollaboration,
    initCollaboration,
    disconnectCollaboration,
    lockProcess,
    unlockProcess,
    applyRemoteChange,
    clearCollaborationData
  }
}

export { USER_STATUS, OPERATION_TYPE }

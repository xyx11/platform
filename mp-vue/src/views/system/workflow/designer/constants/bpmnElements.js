/**
 * BPMN 元素配置常量
 * 定义 BPMN 元素的类型、名称、默认位置等配置
 */

/**
 * BPMN 元素配置映射表
 */
export const BPMN_ELEMENTS = {
  // 开始事件
  START_EVENT: {
    type: 'bpmn:StartEvent',
    name: '开始',
    offset: { x: 0, y: 0 }
  },
  TIMER_START: {
    type: 'bpmn:StartEvent',
    name: '定时开始',
    offset: { x: 0, y: 0 },
    eventDefinitions: [{ $type: 'bpmn:TimerEventDefinition' }]
  },
  MESSAGE_START: {
    type: 'bpmn:StartEvent',
    name: '消息开始',
    offset: { x: 0, y: 0 },
    eventDefinitions: [{ $type: 'bpmn:MessageEventDefinition' }]
  },

  // 结束事件
  END_EVENT: {
    type: 'bpmn:EndEvent',
    name: '结束',
    offset: { x: 150, y: 0 }
  },
  MESSAGE_END: {
    type: 'bpmn:EndEvent',
    name: '消息结束',
    offset: { x: 150, y: 0 },
    eventDefinitions: [{ $type: 'bpmn:MessageEventDefinition' }]
  },
  ERROR_END: {
    type: 'bpmn:EndEvent',
    name: '错误结束',
    offset: { x: 150, y: 0 },
    eventDefinitions: [{ $type: 'bpmn:ErrorEventDefinition' }]
  },
  TERMINATE_END: {
    type: 'bpmn:EndEvent',
    name: '终止结束',
    offset: { x: 150, y: 0 },
    eventDefinitions: [{ $type: 'bpmn:TerminateEventDefinition' }]
  },

  // 任务
  USER_TASK: {
    type: 'bpmn:UserTask',
    name: '用户任务',
    offset: { x: 150, y: 0 }
  },
  SERVICE_TASK: {
    type: 'bpmn:ServiceTask',
    name: '服务任务',
    offset: { x: 150, y: 0 }
  },
  SCRIPT_TASK: {
    type: 'bpmn:ScriptTask',
    name: '脚本任务',
    offset: { x: 150, y: 0 }
  },
  SEND_TASK: {
    type: 'bpmn:SendTask',
    name: '发送任务',
    offset: { x: 150, y: 0 }
  },
  RECEIVE_TASK: {
    type: 'bpmn:ReceiveTask',
    name: '接收任务',
    offset: { x: 150, y: 0 }
  },
  MANUAL_TASK: {
    type: 'bpmn:ManualTask',
    name: '手动任务',
    offset: { x: 150, y: 0 }
  },

  // 网关
  EXCLUSIVE_GATEWAY: {
    type: 'bpmn:ExclusiveGateway',
    name: '排他网关',
    offset: { x: 150, y: 0 }
  },
  PARALLEL_GATEWAY: {
    type: 'bpmn:ParallelGateway',
    name: '并行网关',
    offset: { x: 150, y: 0 }
  },
  INCLUSIVE_GATEWAY: {
    type: 'bpmn:InclusiveGateway',
    name: '包容网关',
    offset: { x: 150, y: 0 }
  },
  EVENT_GATEWAY: {
    type: 'bpmn:EventBasedGateway',
    name: '事件网关',
    offset: { x: 150, y: 0 }
  },

  // 数据
  DATA_OBJECT: {
    type: 'bpmn:DataObjectReference',
    name: '数据对象',
    offset: { x: 150, y: 100 }
  },
  DATA_STORE: {
    type: 'bpmn:DataStoreReference',
    name: '数据存储',
    offset: { x: 150, y: 100 }
  },

  // 子流程
  SUBPROCESS: {
    type: 'bpmn:SubProcess',
    name: '子流程',
    offset: { x: 0, y: 0 }
  },
  TRANSACTION: {
    type: 'bpmn:Transaction',
    name: '事务',
    offset: { x: 0, y: 0 }
  },
  EVENT_SUBPROCESS: {
    type: 'bpmn:SubProcess',
    name: '事件子流程',
    offset: { x: 0, y: 0 },
    triggeredByEvent: true
  }
}

/**
 * 操作到元素配置的映射
 */
export const ACTION_TO_ELEMENT = {
  'create-start-event': 'START_EVENT',
  'create-timer-start': 'TIMER_START',
  'create-message-start': 'MESSAGE_START',
  'create-end-event': 'END_EVENT',
  'create-message-end': 'MESSAGE_END',
  'create-error-end': 'ERROR_END',
  'create-terminate-end': 'TERMINATE_END',
  'create-user-task': 'USER_TASK',
  'create-service-task': 'SERVICE_TASK',
  'create-script-task': 'SCRIPT_TASK',
  'create-send-task': 'SEND_TASK',
  'create-receive-task': 'RECEIVE_TASK',
  'create-manual-task': 'MANUAL_TASK',
  'create-exclusive-gateway': 'EXCLUSIVE_GATEWAY',
  'create-parallel-gateway': 'PARALLEL_GATEWAY',
  'create-inclusive-gateway': 'INCLUSIVE_GATEWAY',
  'create-event-gateway': 'EVENT_GATEWAY',
  'create-data-object': 'DATA_OBJECT',
  'create-data-store': 'DATA_STORE',
  'create-subprocess': 'SUBPROCESS',
  'create-transaction': 'TRANSACTION',
  'create-event-subprocess': 'EVENT_SUBPROCESS'
}

/**
 * 操作提示信息
 */
export const ACTION_MESSAGES = {
  'create-sequence-flow': '请按住 Ctrl 键，从起始元素拖拽到目标元素创建连接',
  'create-message-flow': '请在两个元素间创建消息流',
  'create-association': '请按 Ctrl 键点击元素创建关联'
}

/**
 * 不需要成功提示的操作
 */
export const SKIP_SUCCESS_ACTIONS = ['create-sequence-flow', 'create-message-flow', 'create-association']

/**
 * 获取元素类型显示名称
 * @param {string} type - BPMN 元素类型
 * @returns {string} 中文名称
 */
export function getElementDisplayName(type) {
  const nameMap = {
    'bpmn:StartEvent': '开始事件',
    'bpmn:EndEvent': '结束事件',
    'bpmn:UserTask': '用户任务',
    'bpmn:ServiceTask': '服务任务',
    'bpmn:ScriptTask': '脚本任务',
    'bpmn:SendTask': '发送任务',
    'bpmn:ReceiveTask': '接收任务',
    'bpmn:ManualTask': '手动任务',
    'bpmn:ExclusiveGateway': '排他网关',
    'bpmn:ParallelGateway': '并行网关',
    'bpmn:InclusiveGateway': '包容网关',
    'bpmn:EventBasedGateway': '事件网关',
    'bpmn:SubProcess': '子流程',
    'bpmn:Transaction': '事务',
    'bpmn:DataObjectReference': '数据对象',
    'bpmn:DataStoreReference': '数据存储',
    'bpmn:SequenceFlow': '顺序流',
    'bpmn:MessageFlow': '消息流',
    'bpmn:Association': '关联'
  }
  return nameMap[type] || type
}

/**
 * 获取元素类型的 Tag 颜色
 * @param {string} type - BPMN 元素类型
 * @returns {string} Element Plus Tag 类型
 */
export function getElementTagType(type) {
  if (type?.includes('Event')) return 'info'
  if (type?.includes('Task')) return 'success'
  if (type?.includes('Gateway')) return 'warning'
  if (type?.includes('Flow')) return ''
  return 'info'
}
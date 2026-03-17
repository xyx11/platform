/**
 * 批量操作组合式函数
 * 封装批量设置节点属性等功能
 */

import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logger } from '@/utils/logger'

/**
 * 批量操作类型
 */
const BATCH_OPERATIONS = {
  ASSIGNEE_TYPE: 'assigneeType',
  ASSIGNEE: 'assignee',
  DOCUMENTATION: 'documentation',
  CATEGORY: 'category',
  FORM_KEY: 'formKey'
}

/**
 * 处理人类型选项
 */
const ASSIGNEE_TYPE_OPTIONS = [
  { value: 'user', label: '指定用户' },
  { value: 'deptManager', label: '部门负责人' },
  { value: 'position', label: '岗位' },
  { value: 'variable', label: '变量' }
]

/**
 * 批量操作
 * @param {Object} options - 配置选项
 * @returns {Object} 批量操作 API
 */
export function useBatchOperation(options = {}) {
  const { bpmnModeler } = options

  // 状态
  const batchVisible = ref(false)
  const batchForm = reactive({
    operation: '',
    nodeTypes: ['userTask'],
    assigneeType: '',
    assignee: '',
    documentation: '',
    category: '',
    formKey: ''
  })
  const affectedNodes = ref(0)

  /**
   * 打开批量操作对话框
   */
  const openBatchDialog = () => {
    resetBatchForm()
    batchVisible.value = true
  }

  /**
   * 重置批量表单
   */
  const resetBatchForm = () => {
    batchForm.operation = ''
    batchForm.nodeTypes = ['userTask']
    batchForm.assigneeType = ''
    batchForm.assignee = ''
    batchForm.documentation = ''
    batchForm.category = ''
    batchForm.formKey = ''
    affectedNodes.value = 0
  }

  /**
   * 获取受影响节点数量
   */
  const countAffectedNodes = async () => {
    if (!bpmnModeler?.value) return 0

    try {
      const { xml } = await bpmnModeler.value.saveXML()
      const parser = new DOMParser()
      const xmlDoc = parser.parseFromString(xml, 'text/xml')

      let count = 0
      batchForm.nodeTypes.forEach(type => {
        const elements = xmlDoc.getElementsByTagName(`bpmn:${type}`)
        count += elements.length
      })

      affectedNodes.value = count
      return count
    } catch (error) {
      logger.error('统计节点失败:', error)
      return 0
    }
  }

  /**
   * 操作类型变化时
   */
  const onBatchOperationChange = () => {
    countAffectedNodes()
  }

  /**
   * 执行批量操作
   */
  const executeBatchOperation = async () => {
    if (!batchForm.operation) {
      ElMessage.warning('请选择操作类型')
      return false
    }

    if (affectedNodes.value === 0) {
      ElMessage.warning('没有符合条件的节点')
      return false
    }

    try {
      await ElMessageBox.confirm(
        `将影响 ${affectedNodes.value} 个节点，是否继续？`,
        '批量操作',
        { type: 'warning' }
      )
    } catch {
      return false
    }

    try {
      const elementRegistry = bpmnModeler.value.get('elementRegistry')
      const modeling = bpmnModeler.value.get('modeling')
      const elements = elementRegistry.getAll()

      let updatedCount = 0

      elements.forEach(el => {
        const bo = el.businessObject
        if (!bo || !shouldUpdateNode(bo.$type)) return

        const updates = {}

        switch (batchForm.operation) {
          case BATCH_OPERATIONS.ASSIGNEE_TYPE:
            if (batchForm.assigneeType) {
              updates['camunda:assigneeType'] = batchForm.assigneeType
              updatedCount++
            }
            break

          case BATCH_OPERATIONS.ASSIGNEE:
            if (batchForm.assignee) {
              updates.assignee = batchForm.assignee
              updatedCount++
            }
            break

          case BATCH_OPERATIONS.DOCUMENTATION:
            if (batchForm.documentation) {
              updates.documentation = [{ body: batchForm.documentation }]
              updatedCount++
            }
            break

          case BATCH_OPERATIONS.CATEGORY:
            if (batchForm.category) {
              updates.category = batchForm.category
              updatedCount++
            }
            break

          case BATCH_OPERATIONS.FORM_KEY:
            if (batchForm.formKey) {
              updates['camunda:formKey'] = batchForm.formKey
              updatedCount++
            }
            break
        }

        if (Object.keys(updates).length > 0) {
          modeling.updateProperties(el, updates)
        }
      })

      ElMessage.success(`已更新 ${updatedCount} 个节点`)
      batchVisible.value = false
      return true
    } catch (error) {
      logger.error('批量操作失败:', error)
      ElMessage.error('批量操作失败：' + error.message)
      return false
    }
  }

  /**
   * 判断节点是否应该被更新
   */
  const shouldUpdateNode = (nodeType) => {
    if (!batchForm.nodeTypes || batchForm.nodeTypes.length === 0) {
      return true
    }
    return batchForm.nodeTypes.includes(nodeType)
  }

  /**
   * 获取操作类型选项
   */
  const getOperationOptions = () => {
    return [
      { value: BATCH_OPERATIONS.ASSIGNEE_TYPE, label: '批量设置处理人类型' },
      { value: BATCH_OPERATIONS.ASSIGNEE, label: '批量设置处理人' },
      { value: BATCH_OPERATIONS.DOCUMENTATION, label: '批量添加文档' },
      { value: BATCH_OPERATIONS.CATEGORY, label: '批量设置分类' },
      { value: BATCH_OPERATIONS.FORM_KEY, label: '批量设置表单键' }
    ]
  }

  /**
   * 获取节点类型选项
   */
  const getNodeTypeOptions = () => {
    return [
      { value: 'userTask', label: '用户任务' },
      { value: 'serviceTask', label: '服务任务' },
      { value: 'scriptTask', label: '脚本任务' },
      { value: 'sendTask', label: '发送任务' },
      { value: 'receiveTask', label: '接收任务' },
      { value: 'manualTask', label: '手动任务' }
    ]
  }

  /**
   * 获取处理人类型选项
   */
  const getAssigneeTypeOptions = () => {
    return ASSIGNEE_TYPE_OPTIONS
  }

  return {
    // 状态
    batchVisible,
    batchForm,
    affectedNodes,

    // 方法
    openBatchDialog,
    resetBatchForm,
    executeBatchOperation,
    onBatchOperationChange,
    countAffectedNodes,
    getOperationOptions,
    getNodeTypeOptions,
    getAssigneeTypeOptions
  }
}

export { BATCH_OPERATIONS, ASSIGNEE_TYPE_OPTIONS }

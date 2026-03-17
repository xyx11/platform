/**
 * 流程表单配置组合式函数
 * 封装表单关联、任务表单配置等功能
 */

import { logger } from '@/utils/logger'
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

/**
 * 流程表单配置
 * @param {Object} options - 配置选项
 * @returns {Object} 表单配置 API
 */
export function useFlowForms(options = {}) {
  const { bpmnModeler, processInfo } = options

  // 状态
  const formList = ref([])
  const taskList = ref([])
  const formConfig = reactive({
    startForm: '',
    tasks: []
  })
  const loading = ref(false)

  /**
   * 加载表单列表
   */
  const loadFormList = async (params = {}) => {
    loading.value = true
    try {
      const { data } = await request.get('/system/form-definition/list', {
        params: { status: 1, ...params }
      })
      // 后端返回的是 Result<List>，直接使用 data
      formList.value = Array.isArray(data) ? data : (data?.records || [])
      return formList.value
    } catch (error) {
      logger.error('加载表单列表失败:', error)
      ElMessage.error('加载表单列表失败')
      return []
    } finally {
      loading.value = false
    }
  }

  /**
   * 从 BPMN 中加载用户任务列表
   */
  const loadUserTasks = async () => {
    if (!bpmnModeler?.value) return []

    try {
      const { xml } = await bpmnModeler.value.saveXML()
      const parser = new DOMParser()
      const xmlDoc = parser.parseFromString(xml, 'text/xml')

      const userTasks = xmlDoc.getElementsByTagName('bpmn:userTask')
      const tasks = []

      for (let i = 0; i < userTasks.length; i++) {
        const task = userTasks[i]
        tasks.push({
          id: task.getAttribute('id'),
          name: task.getAttribute('name') || '未命名任务',
          formKey: '',
          assigneeType: 'user',
          assignee: '',
          selected: false
        })
      }

      taskList.value = tasks
      return tasks
    } catch (error) {
      logger.error('加载用户任务失败:', error)
      ElMessage.error('加载任务节点失败')
      return []
    }
  }

  /**
   * 保存表单配置
   */
  const saveFormConfig = async () => {
    if (!formConfig.startForm) {
      ElMessage.warning('请选择启动表单')
      return false
    }

    try {
      // 先解除旧的绑定
      await request.post('/system/workflow-form/unbind', null, {
        params: { processDefinitionKey: processInfo?.value?.id }
      })

      // 批量绑定新的配置
      const tasks = taskList.value.map(t => ({
        taskDefinitionKey: t.id,
        formKey: t.formKey,
        assigneeType: t.assigneeType,
        assignee: t.assignee
      }))

      // 逐个保存任务配置
      for (const task of tasks) {
        await request.post('/system/workflow-form/bind', {
          processDefinitionKey: processInfo?.value?.id,
          ...task
        })
      }

      ElMessage.success('表单配置已保存')
      return true
    } catch (error) {
      logger.error('保存表单配置失败:', error)
      ElMessage.error('保存表单配置失败：' + (error.response?.data?.message || error.message))
      return false
    }
  }

  /**
   * 加载表单配置
   */
  const loadFormConfig = async (processDefinitionId) => {
    if (!processDefinitionId) return null

    try {
      // 使用 workflow-form/list API 获取配置
      const { data } = await request.get('/system/workflow-form/list', {
        params: { processDefinitionKey: processDefinitionId }
      })

      if (data && Array.isArray(data)) {
        // 将列表转换为配置格式
        const tasks = data.map(item => ({
          taskDefinitionKey: item.taskDefinitionKey,
          formKey: item.formKey,
          assigneeType: item.assigneeType || 'user',
          assignee: item.assignee || ''
        }))

        // 启动表单需要从第一个任务或者额外获取
        formConfig.startForm = data[0]?.formKey || ''
        formConfig.tasks = tasks

        return {
          startForm: formConfig.startForm,
          tasks
        }
      }
      return null
    } catch (error) {
      logger.error('加载表单配置失败:', error)
      // 404 错误返回 null，不显示错误提示
      if (error.response?.status === 404) {
        return null
      }
      return null
    }
  }

  /**
   * 批量设置处理人类型
   */
  const batchSetAssigneeType = (selectedRows) => {
    const rows = selectedRows || []
    if (rows.length === 0) {
      ElMessage.warning('请先选择任务')
      return false
    }

    ElMessageBox.prompt('请输入处理人类型 (user/deptManager/position/variable)', '批量设置处理人类型', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: 'user'
    }).then(({ value }) => {
      rows.forEach(task => {
        task.assigneeType = value
      })
      ElMessage.success(`已为 ${rows.length} 个任务设置处理人类型`)
    }).catch(() => {})
    
    return true
  }

  /**
   * 批量设置处理人
   */
  const batchSetAssignee = (selectedRows) => {
    const rows = selectedRows || []
    if (rows.length === 0) {
      ElMessage.warning('请先选择任务')
      return false
    }

    ElMessageBox.prompt('请输入处理人 ID 或变量名', '批量设置处理人', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: ''
    }).then(({ value }) => {
      rows.forEach(task => {
        task.assignee = value
      })
      ElMessage.success(`已为 ${rows.length} 个任务设置处理人`)
    }).catch(() => {})
    
    return true
  }

  /**
   * 批量设置表单
   */
  const batchSetForm = (selectedRows) => {
    const rows = selectedRows || []
    if (rows.length === 0) {
      ElMessage.warning('请先选择任务')
      return false
    }

    // 需要主组件传入 formList 并处理选择逻辑
    // 这个函数需要主组件自己实现
    ElMessage.info('请在表格中直接为每个任务选择表单')
    return false
  }

  /**
   * 清空选择
   */
  const clearSelection = () => {
    taskList.value.forEach(t => {
      t.selected = false
      t.formKey = ''
      t.assignee = ''
      t.assigneeType = 'user'
    })
  }

  
  /**
   * 获取表单名称
   */
  const getFormName = (formCode) => {
    const form = formList.value.find(f => f.formCode === formCode)
    return form?.formName || formCode
  }

  return {
    // 状态
    formList,
    taskList,
    formConfig,
    loading,

    // 方法
    loadFormList,
    loadUserTasks,
    saveFormConfig,
    loadFormConfig,
    batchSetAssigneeType,
    batchSetAssignee,
    batchSetForm,
    clearSelection,
    getFormName
  }
}
/**
 * 应用已保存的表单配置到任务列表
 */
const applyFormConfig = (savedConfig) => {
  if (!savedConfig || !savedConfig.tasks) return
  
  savedConfig.tasks.forEach(savedTask => {
    const task = taskList.value.find(t => t.id === savedTask.taskDefinitionKey)
    if (task) {
      task.formKey = savedTask.formKey || ''
      task.assigneeType = savedTask.assigneeType || 'user'
      task.assignee = savedTask.assignee || ''
    }
  })
}

/**
 * 获取任务节点详细信息
 */
const getTaskDetails = () => {
  return taskList.value.map(t => ({
    ...t,
    formName: getFormName(t.formKey)
  }))
}

/**
 * 验证表单配置完整性
 */
const validateFormConfig = () => {
  const issues = []
  
  if (!formConfig.startForm) {
    issues.push({ type: 'error', message: '未选择启动表单' })
  }
  
  const unconfiguredTasks = taskList.value.filter(t => !t.formKey)
  if (unconfiguredTasks.length > 0) {
    issues.push({ 
      type: 'warning', 
      message: `${unconfiguredTasks.length} 个任务节点未关联表单` 
    })
  }
  
  const noAssigneeTasks = taskList.value.filter(t => !t.assignee && t.assigneeType === 'user')
  if (noAssigneeTasks.length > 0) {
    issues.push({ 
      type: 'warning', 
      message: `${noAssigneeTasks.length} 个任务节点未指定处理人` 
    })
  }
  
  return {
    valid: issues.filter(i => i.type === 'error').length === 0,
    issues
  }
}

export { 
  applyFormConfig,
  getTaskDetails,
  validateFormConfig
}

/**
 * 流程表单配置组合式函数
 * 封装表单关联、任务表单配置等功能
 */

import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
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
        params: { pageNum: 1, pageSize: 100, status: 1, ...params }
      })
      formList.value = data?.records || []
      return formList.value
    } catch (error) {
      console.error('加载表单列表失败:', error)
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
      console.error('加载用户任务失败:', error)
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
      const config = {
        processDefinitionId: processInfo?.value?.id,
        startForm: formConfig.startForm,
        tasks: taskList.value.map(t => ({
          taskDefinitionKey: t.id,
          formKey: t.formKey,
          assigneeType: t.assigneeType,
          assignee: t.assignee
        }))
      }

      await request.post('/system/workflow/form/config', config)
      ElMessage.success('表单配置已保存')
      return true
    } catch (error) {
      console.error('保存表单配置失败:', error)
      ElMessage.error('保存表单配置失败')
      return false
    }
  }

  /**
   * 加载表单配置
   */
  const loadFormConfig = async (processDefinitionId) => {
    if (!processDefinitionId) return null

    try {
      const { data } = await request.get(`/system/workflow/form/config/${processDefinitionId}`)
      if (data) {
        formConfig.startForm = data.startForm || ''
        formConfig.tasks = data.tasks || []
      }
      return data
    } catch (error) {
      console.error('加载表单配置失败:', error)
      return null
    }
  }

  /**
   * 批量设置处理人类型
   */
  const batchSetAssigneeType = (selectedRows, assigneeType) => {
    if (!selectedRows || selectedRows.length === 0) {
      ElMessage.warning('请先选择任务')
      return false
    }

    selectedRows.forEach(task => {
      task.assigneeType = assigneeType
    })

    ElMessage.success(`已为 ${selectedRows.length} 个任务设置处理人类型`)
    return true
  }

  /**
   * 批量设置处理人
   */
  const batchSetAssignee = (selectedRows, assignee) => {
    if (!selectedRows || selectedRows.length === 0) {
      ElMessage.warning('请先选择任务')
      return false
    }

    selectedRows.forEach(task => {
      task.assignee = assignee
    })

    ElMessage.success(`已为 ${selectedRows.length} 个任务设置处理人`)
    return true
  }

  /**
   * 批量设置表单
   */
  const batchSetForm = (selectedRows, formKey) => {
    if (!selectedRows || selectedRows.length === 0) {
      ElMessage.warning('请先选择任务')
      return false
    }

    selectedRows.forEach(task => {
      task.formKey = formKey
    })

    ElMessage.success(`已为 ${selectedRows.length} 个任务设置表单`)
    return true
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
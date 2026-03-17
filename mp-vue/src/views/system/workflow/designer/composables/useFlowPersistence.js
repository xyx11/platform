/**
 * 流程保存与部署组合式函数
 * 封装流程的保存、部署、版本管理等操作
 */

import { logger } from '@/utils/logger'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

/**
 * 流程保存与部署
 * @param {Object} options - 配置选项
 * @returns {Object} 保存与部署 API
 */
export function useFlowPersistence(options = {}) {
  const {
    bpmnModeler,
    processInfo,
    onSaved,
    onDeployed
  } = options

  // 状态
  const saving = ref(false)
  const deploying = ref(false)
  const saved = ref(false)
  const deployed = ref(false)
  const autoSaving = ref(false)
  const lastSavedTime = ref(null)
  const hasUnsavedChanges = ref(false)
  const deployedDefinitions = ref([])

  let autoSaveTimer = null

  /**
   * 保存 BPMN XML
   */
  const saveXML = async (format = true) => {
    if (!bpmnModeler?.value) {
      throw new Error('Modeler 未初始化')
    }
    return await bpmnModeler.value.saveXML({ format })
  }

  /**
   * 验证 BPMN
   */
  const validateBpmn = (xml) => {
    try {
      const parser = new DOMParser()
      const xmlDoc = parser.parseFromString(xml, 'text/xml')

      // 检查开始事件
      const startEvents = xmlDoc.getElementsByTagName('bpmn:startEvent')
      if (startEvents.length === 0) {
        return '流程必须包含至少一个开始事件'
      }
      if (startEvents.length > 1) {
        return `流程只能包含一个开始事件，当前有 ${startEvents.length} 个`
      }

      // 检查结束事件
      const endEvents = xmlDoc.getElementsByTagName('bpmn:endEvent')
      if (endEvents.length === 0) {
        return '流程必须包含至少一个结束事件'
      }

      return null
    } catch (e) {
      logger.error('BPMN 验证失败:', e)
      return 'BPMN 验证失败：' + e.message
    }
  }

  /**
   * 保存到服务器
   */
  const saveToServer = async (params) => {
    try {
      const { data } = await request.post('/system/workflow/save', {
        name: params.name,
        bpmnXml: params.bpmnXml,
        category: params.category || 'default',
        description: params.description
      })

      if (data && data.code === 200) {
        saved.value = true
        processInfo.value.id = params.id || Date.now().toString()
        processInfo.value.version = params.version || '1.0'
        onSaved?.(data)
        return true
      } else if (data && data.code === 401) {
        ElMessage.error('登录已过期，请重新登录')
        return false
      }

      saved.value = true
      onSaved?.(data)
      return true
    } catch (error) {
      logger.error('保存失败:', error)
      ElMessage.error('保存失败：' + (error.response?.data?.message || error.message))
      if (error.response?.status === 401) {
        setTimeout(() => {
          // router.push('/login')
        }, 1500)
      }
      return false
    }
  }

  /**
   * 部署到服务器
   */
  const deployToServer = async (params) => {
    try {
      const { data } = await request.post('/system/workflow/deploy', {
        name: params.name,
        bpmnXml: params.bpmnXml
      })

      if (data && data.code === 200) {
        deployed.value = true
        loadDeployedDefinitions()
        onDeployed?.(data)
        return true
      } else if (data && data.code === 401) {
        ElMessage.error('登录已过期，请重新登录')
        return false
      }

      deployed.value = true
      loadDeployedDefinitions()
      return true
    } catch (error) {
      logger.error('部署失败:', error)
      ElMessage.error('部署失败：' + (error.response?.data?.message || error.message))
      if (error.response?.status === 401) {
        setTimeout(() => {
          // router.push('/login')
        }, 1500)
      }
      return false
    }
  }

  /**
   * 执行保存
   */
  const doSave = async () => {
    const { xml } = await saveXML()

    // 自动修复 isExecutable 属性
    let fixedXml = xml
    if (xml.includes('isExecutable="false"')) {
      fixedXml = xml.replace(/isExecutable="false"/g, 'isExecutable="true"')
    }

    // 验证 BPMN
    const validationError = validateBpmn(fixedXml)
    if (validationError) {
      ElMessage.error(validationError)
      return
    }

    saving.value = true
    try {
      const result = await saveToServer({
        name: processInfo.value.name || processInfo.value.name,
        id: processInfo.value.id,
        version: processInfo.value.version,
        description: processInfo.value.description,
        category: processInfo.value.category,
        bpmnXml: fixedXml
      })
      if (result) {
        ElMessage.success('保存成功')
        lastSavedTime.value = new Date()
        hasUnsavedChanges.value = false
      }
    } catch (err) {
      ElMessage.error('保存失败：' + err.message)
    } finally {
      saving.value = false
    }
  }

  /**
   * 自动保存
   */
  const autoSave = async () => {
    if (!processInfo.value.id || !hasUnsavedChanges.value) return

    autoSaving.value = true
    try {
      const { xml } = await saveXML()
      await saveToServer({
        name: processInfo.value.name,
        id: processInfo.value.id,
        version: processInfo.value.version,
        description: processInfo.value.description,
        category: processInfo.value.category,
        bpmnXml: xml,
        autoSave: true
      })
      lastSavedTime.value = new Date()
      hasUnsavedChanges.value = false
      ElMessage.success('自动保存成功')
    } catch (err) {
      ElMessage.error('自动保存失败，但已保存到本地备份')
    } finally {
      autoSaving.value = false
    }
  }

  /**
   * 启用自动保存
   */
  const enableAutoSave = (interval = 30000) => {
    if (!bpmnModeler?.value) return

    const eventBus = bpmnModeler.value.get('eventBus')
    eventBus.on('commandStack.changed', () => {
      hasUnsavedChanges.value = true
      // 防抖自动保存
      if (autoSaveTimer) clearTimeout(autoSaveTimer)
      autoSaveTimer = setTimeout(() => {
        if (hasUnsavedChanges.value && saved.value) {
          autoSave()
        }
      }, interval)
    })
  }

  /**
   * 禁用自动保存
   */
  const disableAutoSave = () => {
    if (autoSaveTimer) {
      clearTimeout(autoSaveTimer)
      autoSaveTimer = null
    }
  }

  /**
   * 加载已部署的流程定义
   */
  const loadDeployedDefinitions = async () => {
    try {
      const { data } = await request.get('/system/workflow/definition/list', {
        params: { pageNum: 1, pageSize: 100 }
      })
      deployedDefinitions.value = data?.records || []
    } catch (error) {
      logger.error('加载流程定义失败:', error)
    }
  }

  /**
   * 部署流程
   */
  const deployDiagram = async () => {
    if (!processInfo.value.name) {
      ElMessage.warning('请先设置流程名称')
      return
    }

    deploying.value = true
    try {
      const { xml } = await saveXML()
      const result = await deployToServer({
        name: processInfo.value.name,
        bpmnXml: xml
      })
      if (result) {
        ElMessage.success('部署成功')
      }
    } catch (err) {
      ElMessage.error('部署失败：' + err.message)
    } finally {
      deploying.value = false
    }
  }

  /**
   * 清理资源
   */
  const dispose = () => {
    disableAutoSave()
  }

  return {
    // 状态
    saving,
    deploying,
    saved,
    deployed,
    autoSaving,
    lastSavedTime,
    hasUnsavedChanges,
    deployedDefinitions,

    // 方法
    doSave,
    autoSave,
    enableAutoSave,
    disableAutoSave,
    deployDiagram,
    loadDeployedDefinitions,
    saveToServer,
    deployToServer,
    dispose
  }
}

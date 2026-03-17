/**
 * 模板管理组合式函数
 * 封装流程模板的保存、加载、管理等功能
 */

import { logger } from '@/utils/logger'
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { saveTemplate, getTemplates, deleteTemplate, importTemplate as importTpl } from '../utils/storageUtils'

/**
 * 模板分类
 */
const TEMPLATE_CATEGORIES = [
  { value: 'hr', label: '人事流程' },
  { value: 'finance', label: '财务流程' },
  { value: 'admin', label: '行政流程' },
  { value: 'purchase', label: '采购流程' },
  { value: 'other', label: '其他' }
]

/**
 * 常用模板预设
 */
const COMMON_TEMPLATES = [
  {
    id: 'leave',
    name: '请假流程',
    description: '包含申请、审批、销假流程',
    category: 'hr',
    nodeCount: 5
  },
  {
    id: 'reimbursement',
    name: '报销流程',
    description: '包含申请、财务审核、打款流程',
    category: 'finance',
    nodeCount: 6
  },
  {
    id: 'purchase',
    name: '采购流程',
    description: '包含申请、比价、订单、验收流程',
    category: 'purchase',
    nodeCount: 8
  },
  {
    id: 'onboarding',
    name: '入职流程',
    description: '包含 HR 录入、部门安排、设备领取流程',
    category: 'hr',
    nodeCount: 7
  },
  {
    id: 'contract',
    name: '合同审批流程',
    description: '包含起草、法务审核、签署流程',
    category: 'admin',
    nodeCount: 5
  },
  {
    id: 'travel',
    name: '出差流程',
    description: '包含申请、审批、报销流程',
    category: 'admin',
    nodeCount: 5
  }
]

/**
 * 模板管理
 * @param {Object} options - 配置选项
 * @returns {Object} 模板管理 API
 */
export function useTemplate(options = {}) {
  const { bpmnModeler, processInfo } = options

  // 状态
  const templateVisible = ref(false)
  const templateTab = ref('save')
  const savingTemplate = ref(false)
  const templateForm = reactive({
    name: '',
    category: 'other',
    description: ''
  })
  const myTemplates = ref([])
  const templateLibVisible = ref(false)
  const templateLibTab = ref('common')

  /**
   * 打开保存模板对话框
   */
  const openSaveTemplate = () => {
    templateForm.name = (processInfo?.value?.name || '流程') + ' 模板'
    templateForm.description = ''
    templateForm.category = 'other'
    templateTab.value = 'save'
    templateVisible.value = true
  }

  /**
   * 打开导入模板对话框
   */
  const openImportTemplate = () => {
    templateTab.value = 'import'
    templateVisible.value = true
  }

  /**
   * 保存为模板
   */
  const saveAsTemplate = async () => {
    if (!templateForm.name) {
      ElMessage.warning('请输入模板名称')
      return false
    }

    if (!bpmnModeler?.value) {
      ElMessage.error('模型器未初始化')
      return false
    }

    savingTemplate.value = true
    try {
      const { xml } = await bpmnModeler.value.saveXML({ format: true })

      const templateData = {
        id: Date.now().toString(),
        name: templateForm.name,
        category: templateForm.category,
        description: templateForm.description,
        bpmnXml: xml,
        createTime: new Date().toISOString()
      }

      // 保存到本地存储
      saveTemplate(templateData)

      // 下载模板文件
      downloadTemplate(templateData)

      ElMessage.success('模板保存成功')
      templateVisible.value = false
      loadMyTemplates()
      return true
    } catch (error) {
      logger.error('保存模板失败:', error)
      ElMessage.error('保存模板失败')
      return false
    } finally {
      savingTemplate.value = false
    }
  }

  /**
   * 下载模板文件
   */
  const downloadTemplate = (templateData) => {
    const blob = new Blob([JSON.stringify(templateData, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = templateData.name + '.template.json'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  }

  /**
   * 处理模板文件导入
   */
  const handleTemplateFile = async (file) => {
    try {
      const templateData = await importTpl(file)
      if (templateData?.bpmnXml) {
        await bpmnModeler.value.importXML(templateData.bpmnXml)
        processInfo.value.name = templateData.name?.replace(' 模板', '') || '新流程'
        ElMessage.success('模板导入成功')
        templateVisible.value = false
      } else {
        ElMessage.error('无效的模板文件')
      }
    } catch (error) {
      logger.error('导入模板失败:', error)
      ElMessage.error('导入模板失败：文件格式不正确')
    }
  }

  /**
   * 加载我的模板
   */
  const loadMyTemplates = () => {
    myTemplates.value = getTemplates()
  }

  /**
   * 应用模板
   */
  const applyTemplate = async (template) => {
    try {
      await ElMessageBox.confirm(
        '应用模板将覆盖当前流程，是否继续？',
        '提示',
        { type: 'warning' }
      )
    } catch {
      return false
    }

    if (!bpmnModeler?.value) return false

    try {
      // 如果是预设模板，需要加载 BPMN
      if (template.id && COMMON_TEMPLATES.find(t => t.id === template.id)) {
        ElMessage.info('预设模板功能开发中')
        return false
      }

      await bpmnModeler.value.importXML(template.bpmnXml)
      processInfo.value.name = template.name?.replace(' 模板', '') || '新流程'
      ElMessage.success('模板应用成功')
      templateLibVisible.value = false
      return true
    } catch (error) {
      logger.error('应用模板失败:', error)
      ElMessage.error('应用模板失败')
      return false
    }
  }

  /**
   * 删除我的模板
   */
  const deleteMyTemplate = async (template) => {
    try {
      await ElMessageBox.confirm(
        `确定要删除模板 "${template.name}" 吗？`,
        '提示',
        { type: 'warning' }
      )
    } catch {
      return false
    }

    const success = deleteTemplate(template.id)
    if (success) {
      ElMessage.success('模板删除成功')
      loadMyTemplates()
    } else {
      ElMessage.error('删除失败')
    }
  }

  /**
   * 打开模板库
   */
  const openTemplateLib = () => {
    loadMyTemplates()
    templateLibTab.value = 'common'
    templateLibVisible.value = true
  }

  /**
   * 获取常用模板
   */
  const getCommonTemplates = () => {
    return COMMON_TEMPLATES
  }

  /**
   * 获取分类选项
   */
  const getCategoryOptions = () => {
    return TEMPLATE_CATEGORIES
  }

  return {
    // 状态
    templateVisible,
    templateTab,
    savingTemplate,
    templateForm,
    myTemplates,
    templateLibVisible,
    templateLibTab,

    // 方法
    openSaveTemplate,
    openImportTemplate,
    saveAsTemplate,
    handleTemplateFile,
    loadMyTemplates,
    applyTemplate,
    deleteMyTemplate,
    openTemplateLib,
    getCommonTemplates,
    getCategoryOptions
  }
}

export { TEMPLATE_CATEGORIES, COMMON_TEMPLATES }
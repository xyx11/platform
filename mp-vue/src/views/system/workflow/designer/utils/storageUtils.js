/**
 * 本地存储工具函数
 * 管理流程设计器的本地备份和模板存储
 */

const STORAGE_KEYS = {
  AUTO_BACKUP: 'workflow-auto-backup',
  TEMPLATES: 'workflow-templates',
  FORM_CONFIG: 'workflow-form-config',
  SHORTCUTS: 'workflow-shortcuts'
}

/**
 * 自动备份数据结构
 */
class BackupData {
  constructor(xml, name) {
    this.xml = xml
    this.name = name
    this.timestamp = new Date().toISOString()
    this.version = '1.0'
  }
}

/**
 * 保存自动备份
 * @param {string} xml - BPMN XML
 * @param {string} name - 流程名称
 */
export function saveAutoBackup(xml, name) {
  try {
    const backup = new BackupData(xml, name)
    localStorage.setItem(STORAGE_KEYS.AUTO_BACKUP, JSON.stringify(backup))
    console.log('自动备份完成:', backup.timestamp)
  } catch (err) {
    console.error('保存自动备份失败:', err)
  }
}

/**
 * 获取自动备份
 * @returns {BackupData|null} 备份数据
 */
export function getAutoBackup() {
  const backupStr = localStorage.getItem(STORAGE_KEYS.AUTO_BACKUP)
  if (!backupStr) return null
  try {
    return JSON.parse(backupStr)
  } catch (err) {
    console.error('解析自动备份失败:', err)
    return null
  }
}

/**
 * 清除自动备份
 */
export function clearAutoBackup() {
  localStorage.removeItem(STORAGE_KEYS.AUTO_BACKUP)
}

/**
 * 检查是否有可用的备份
 * @returns {boolean}
 */
export function hasBackup() {
  return localStorage.getItem(STORAGE_KEYS.AUTO_BACKUP) !== null
}

/**
 * 保存流程模板
 * @param {Object} template - 模板数据
 */
export function saveTemplate(template) {
  try {
    const templates = getTemplates()
    template.id = Date.now().toString()
    template.updateTime = new Date().toISOString()
    templates.push(template)
    localStorage.setItem(STORAGE_KEYS.TEMPLATES, JSON.stringify(templates))
    return template.id
  } catch (err) {
    console.error('保存模板失败:', err)
    return null
  }
}

/**
 * 获取所有模板
 * @returns {Array} 模板列表
 */
export function getTemplates() {
  try {
    const templatesStr = localStorage.getItem(STORAGE_KEYS.TEMPLATES)
    return templatesStr ? JSON.parse(templatesStr) : []
  } catch (err) {
    console.error('解析模板列表失败:', err)
    return []
  }
}

/**
 * 删除模板
 * @param {string} templateId - 模板 ID
 */
export function deleteTemplate(templateId) {
  try {
    const templates = getTemplates()
    const filtered = templates.filter(t => t.id !== templateId)
    localStorage.setItem(STORAGE_KEYS.TEMPLATES, JSON.stringify(filtered))
    return true
  } catch (err) {
    console.error('删除模板失败:', err)
    return false
  }
}

/**
 * 获取模板
 * @param {string} templateId - 模板 ID
 * @returns {Object|null} 模板数据
 */
export function getTemplate(templateId) {
  const templates = getTemplates()
  return templates.find(t => t.id === templateId) || null
}

/**
 * 更新模板
 * @param {string} templateId - 模板 ID
 * @param {Object} updates - 更新数据
 */
export function updateTemplate(templateId, updates) {
  try {
    const templates = getTemplates()
    const index = templates.findIndex(t => t.id === templateId)
    if (index === -1) return false

    templates[index] = { ...templates[index], ...updates, updateTime: new Date().toISOString() }
    localStorage.setItem(STORAGE_KEYS.TEMPLATES, JSON.stringify(templates))
    return true
  } catch (err) {
    console.error('更新模板失败:', err)
    return false
  }
}

/**
 * 导出模板到文件
 * @param {string} templateId - 模板 ID
 */
export function exportTemplate(templateId) {
  const template = getTemplate(templateId)
  if (!template) return false

  const blob = new Blob([JSON.stringify(template, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${template.name || 'template'}.json`
  a.click()
  URL.revokeObjectURL(url)
  return true
}

/**
 * 导入模板从文件
 * @param {File} file - 模板文件
 * @returns {Promise<Object>} 导入结果
 */
export function importTemplate(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (event) => {
      try {
        const template = JSON.parse(event.target.result)
        // 验证基本结构
        if (!template.name || !template.bpmnXml) {
          reject(new Error('无效的模板文件格式'))
          return
        }
        const id = saveTemplate(template)
        resolve({ id, ...template })
      } catch (err) {
        reject(new Error('解析模板文件失败：' + err.message))
      }
    }
    reader.onerror = () => reject(new Error('读取文件失败'))
    reader.readAsText(file)
  })
}

/**
 * 清除所有本地存储数据
 */
export function clearAllStorage() {
  Object.values(STORAGE_KEYS).forEach(key => {
    localStorage.removeItem(key)
  })
}

/**
 * 获取存储使用情况
 * @returns {Object} 存储统计
 */
export function getStorageStats() {
  return {
    hasBackup: hasBackup(),
    templateCount: getTemplates().length,
    storageUsed: localStorage.length
  }
}
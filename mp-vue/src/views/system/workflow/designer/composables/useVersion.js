/**
 * 版本管理组合式函数
 * 封装流程版本管理功能，包括版本历史、版本对比、版本回滚等
 */

import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 版本操作类型
 */
const VERSION_ACTION = {
  CREATE: 'create',
  DEPLOY: 'deploy',
  MODIFY: 'modify',
  ROLLBACK: 'rollback'
}

/**
 * 版本管理
 * @param {Object} options - 配置选项
 * @returns {Object} 版本管理 API
 */
export function useVersion(options = {}) {
  const { bpmnModeler, processInfo } = options

  // 状态
  const versionVisible = ref(false)
  const versionList = ref([])
  const currentVersion = ref(null)
  const comparing = ref(false)
  const compareSource = ref(null)
  const compareTarget = ref(null)
  const compareResult = ref(null)

  // 版本统计
  const versionStats = computed(() => {
    return {
      total: versionList.value.length,
      latest: versionList.value[0]?.version || '-',
      deployed: versionList.value.filter(v => v.deployed).length
    }
  })

  /**
   * 打开版本历史
   */
  const openVersionHistory = async () => {
    await loadVersions()
    versionVisible.value = true
  }

  /**
   * 关闭版本历史
   */
  const closeVersionHistory = () => {
    versionVisible.value = false
  }

  /**
   * 加载版本列表
   */
  const loadVersions = async () => {
    if (!processInfo?.value?.id) {
      // 模拟数据
      versionList.value = getSampleVersions()
      return
    }

    try {
      // 从服务器加载版本历史
      // const response = await request.get('/system/workflow/version/list', {
      //   params: { processId: processInfo.value.id }
      // })
      // versionList.value = response.data?.records || []

      // 临时使用模拟数据
      versionList.value = getSampleVersions()
    } catch (error) {
      console.error('加载版本列表失败:', error)
      ElMessage.error('加载版本列表失败')
    }
  }

  /**
   * 获取示例版本列表
   */
  const getSampleVersions = () => {
    return [
      {
        id: 3,
        version: '1.3',
        name: '添加超时处理',
        bpmnXml: '',
        deployed: false,
        action: VERSION_ACTION.MODIFY,
        userId: 1,
        userName: '张三',
        createdAt: '2024-01-15 14:30',
        remark: '添加了审批超时自动通过功能'
      },
      {
        id: 2,
        version: '1.2',
        name: '优化流程结构',
        bpmnXml: '',
        deployed: true,
        action: VERSION_ACTION.DEPLOY,
        userId: 2,
        userName: '李四',
        createdAt: '2024-01-12 10:00',
        remark: '优化了并行网关结构'
      },
      {
        id: 1,
        version: '1.1',
        name: '修复条件判断',
        bpmnXml: '',
        deployed: true,
        action: VERSION_ACTION.MODIFY,
        userId: 1,
        userName: '张三',
        createdAt: '2024-01-10 16:00',
        remark: '修复了排他网关条件判断错误'
      },
      {
        id: 0,
        version: '1.0',
        name: '初始版本',
        bpmnXml: '',
        deployed: true,
        action: VERSION_ACTION.CREATE,
        userId: 1,
        userName: '张三',
        createdAt: '2024-01-08 09:00',
        remark: '创建初始流程'
      }
    ]
  }

  /**
   * 保存新版本
   */
  const saveVersion = async (remark = '') => {
    if (!bpmnModeler?.value) return null

    try {
      const { xml } = await bpmnModeler.value.saveXML({ format: true })

      const newVersion = {
        id: Date.now(),
        version: generateVersionNumber(),
        name: processInfo?.value?.name || '未命名流程',
        bpmnXml: xml,
        deployed: false,
        action: VERSION_ACTION.MODIFY,
        userId: getCurrentUserId(),
        userName: getCurrentUserName(),
        createdAt: new Date().toLocaleString('zh-CN'),
        remark
      }

      versionList.value.unshift(newVersion)

      // 保存到本地存储
      saveVersionsToLocal()

      ElMessage.success('版本已保存')
      return newVersion
    } catch (error) {
      console.error('保存版本失败:', error)
      ElMessage.error('保存版本失败')
      return null
    }
  }

  /**
   * 生成版本号
   */
  const generateVersionNumber = () => {
    if (versionList.value.length === 0) return '1.0'

    const latest = versionList.value[0].version
    const parts = latest.split('.')
    const minor = parseInt(parts[1]) || 0
    return `${parts[0]}.${minor + 1}`
  }

  /**
   * 保存版本到本地存储
   */
  const saveVersionsToLocal = () => {
    if (!processInfo?.value?.id) return

    const key = 'workflow_versions_' + processInfo.value.id
    localStorage.setItem(key, JSON.stringify(versionList.value))
  }

  /**
   * 加载指定版本
   */
  const loadVersion = async (version) => {
    if (!bpmnModeler?.value || !version.bpmnXml) {
      ElMessage.warning('版本内容为空')
      return false
    }

    try {
      await ElMessageBox.confirm(
        `确定要加载版本 ${version.version} 吗？当前未保存的更改将丢失。`,
        '加载版本',
        { type: 'warning' }
      )
    } catch {
      return false
    }

    try {
      await bpmnModeler.value.importXML(version.bpmnXml)

      if (processInfo.value) {
        processInfo.value.version = version.version
      }

      currentVersion.value = version

      ElMessage.success('版本加载成功')
      versionVisible.value = false
      return true
    } catch (error) {
      console.error('加载版本失败:', error)
      ElMessage.error('加载版本失败')
      return false
    }
  }

  /**
   * 版本回滚
   */
  const rollbackVersion = async (version) => {
    if (!version.bpmnXml) {
      ElMessage.warning('版本内容为空')
      return false
    }

    try {
      await ElMessageBox.confirm(
        `确定要回滚到版本 ${version.version} 吗？此操作将创建一个新版本。`,
        '版本回滚',
        { type: 'warning' }
      )
    } catch {
      return false
    }

    try {
      await bpmnModeler.value.importXML(version.bpmnXml)

      // 创建回滚版本
      const rollbackVersion = {
        id: Date.now(),
        version: generateVersionNumber(),
        name: `回滚到 ${version.version}`,
        bpmnXml: version.bpmnXml,
        deployed: false,
        action: VERSION_ACTION.ROLLBACK,
        userId: getCurrentUserId(),
        userName: getCurrentUserName(),
        createdAt: new Date().toLocaleString('zh-CN'),
        remark: `回滚到版本 ${version.version}`
      }

      versionList.value.unshift(rollbackVersion)
      saveVersionsToLocal()

      if (processInfo.value) {
        processInfo.value.version = rollbackVersion.version
      }

      ElMessage.success('版本回滚成功')
      versionVisible.value = false
      return true
    } catch (error) {
      console.error('版本回滚失败:', error)
      ElMessage.error('版本回滚失败')
      return false
    }
  }

  /**
   * 删除版本
   */
  const deleteVersion = async (version) => {
    if (version.version === '1.0') {
      ElMessage.warning('不能删除初始版本')
      return false
    }

    try {
      await ElMessageBox.confirm(
        `确定要删除版本 ${version.version} 吗？`,
        '删除版本',
        { type: 'warning' }
      )
    } catch {
      return false
    }

    const index = versionList.value.findIndex(v => v.id === version.id)
    if (index === -1) return false

    versionList.value.splice(index, 1)
    saveVersionsToLocal()

    ElMessage.success('版本删除成功')
    return true
  }

  /**
   * 部署版本
   */
  const deployVersion = async (version) => {
    try {
      await ElMessageBox.confirm(
        `确定要部署版本 ${version.version} 吗？`,
        '部署版本',
        { type: 'info' }
      )
    } catch {
      return false
    }

    version.deployed = true
    version.deployedAt = new Date().toLocaleString('zh-CN')

    // 取消其他版本的部署状态
    versionList.value.forEach(v => {
      if (v.id !== version.id) {
        v.deployed = false
      }
    })

    saveVersionsToLocal()

    ElMessage.success('版本部署成功')
    return true
  }

  /**
   * 开始版本对比
   */
  const startCompare = (version) => {
    if (!compareSource.value) {
      compareSource.value = version
      ElMessage.info('请再选择一个版本进行对比')
    } else {
      compareTarget.value = version
      compareVersions()
    }
  }

  /**
   * 执行版本对比
   */
  const compareVersions = async () => {
    if (!compareSource.value || !compareTarget.value) {
      ElMessage.warning('请选择两个版本进行对比')
      return
    }

    comparing.value = true

    try {
      // 解析两个版本的 BPMN
      const sourceXml = compareSource.value.bpmnXml
      const targetXml = compareTarget.value.bpmnXml

      if (!sourceXml || !targetXml) {
        compareResult.value = {
          added: [],
          removed: [],
          modified: [],
          same: true
        }
        comparing.value = false
        return
      }

      const parser = new DOMParser()
      const sourceDoc = parser.parseFromString(sourceXml, 'text/xml')
      const targetDoc = parser.parseFromString(targetXml, 'text/xml')

      // 提取元素
      const sourceElements = extractElements(sourceDoc)
      const targetElements = extractElements(targetDoc)

      // 对比差异
      const added = targetElements.filter(t => !sourceElements.find(s => s.id === t.id))
      const removed = sourceElements.filter(s => !targetElements.find(t => t.id === s.id))
      const modified = []

      // 检查同名节点的属性变化
      targetElements.forEach(t => {
        const source = sourceElements.find(s => s.id === t.id)
        if (source && source.name !== t.name) {
          modified.push({
            id: t.id,
            oldName: source.name,
            newName: t.name,
            type: t.type
          })
        }
      })

      compareResult.value = {
        added,
        removed,
        modified,
        same: added.length === 0 && removed.length === 0 && modified.length === 0,
        sourceVersion: compareSource.value.version,
        targetVersion: compareTarget.value.version
      }

      comparing.value = false
    } catch (error) {
      console.error('版本对比失败:', error)
      ElMessage.error('版本对比失败')
      comparing.value = false
    }
  }

  /**
   * 提取 BPMN 元素
   */
  const extractElements = (xmlDoc) => {
    const elements = []
    const types = ['startEvent', 'endEvent', 'userTask', 'serviceTask', 'scriptTask', 'exclusiveGateway', 'parallelGateway', 'inclusiveGateway', 'subProcess']

    types.forEach(type => {
      const nodes = xmlDoc.getElementsByTagName('bpmn:' + type)
      for (let i = 0; i < nodes.length; i++) {
        const node = nodes[i]
        elements.push({
          id: node.getAttribute('id'),
          name: node.getAttribute('name') || '',
          type
        })
      }
    })

    return elements
  }

  /**
   * 清除对比
   */
  const clearCompare = () => {
    compareSource.value = null
    compareTarget.value = null
    compareResult.value = null
  }

  /**
   * 获取操作类型名称
   */
  const getActionName = (action) => {
    const names = {
      [VERSION_ACTION.CREATE]: '创建',
      [VERSION_ACTION.DEPLOY]: '部署',
      [VERSION_ACTION.MODIFY]: '修改',
      [VERSION_ACTION.ROLLBACK]: '回滚'
    }
    return names[action] || action
  }

  /**
   * 获取操作类型标签颜色
   */
  const getActionColor = (action) => {
    const colors = {
      [VERSION_ACTION.CREATE]: '#67C23A',
      [VERSION_ACTION.DEPLOY]: '#409EFF',
      [VERSION_ACTION.MODIFY]: '#E6A23C',
      [VERSION_ACTION.ROLLBACK]: '#909399'
    }
    return colors[action] || '#909399'
  }

  /**
   * 获取当前用户 ID
   */
  const getCurrentUserId = () => {
    return 1
  }

  /**
   * 获取当前用户名
   */
  const getCurrentUserName = () => {
    return '当前用户'
  }

  /**
   * 导出版本
   */
  const exportVersion = (version) => {
    if (!version.bpmnXml) {
      ElMessage.warning('版本内容为空')
      return
    }

    const blob = new Blob([version.bpmnXml], { type: 'application/xml' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${processInfo?.value?.name || 'workflow'}_v${version.version}.bpmn`
    a.click()
    URL.revokeObjectURL(url)

    ElMessage.success('版本导出成功')
  }

  return {
    // 状态
    versionVisible,
    versionList,
    currentVersion,
    comparing,
    compareSource,
    compareTarget,
    compareResult,
    versionStats,

    // 方法
    openVersionHistory,
    closeVersionHistory,
    loadVersions,
    saveVersion,
    loadVersion,
    rollbackVersion,
    deleteVersion,
    deployVersion,
    startCompare,
    compareVersions,
    clearCompare,
    getActionName,
    getActionColor,
    exportVersion
  }
}

export { VERSION_ACTION }
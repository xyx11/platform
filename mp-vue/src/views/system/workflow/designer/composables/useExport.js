/**
 * 导出功能组合式函数
 * 封装流程导出为 BPMN、SVG、PNG、PDF 等格式
 */

import { ref } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 导出选项
 */
const EXPORT_OPTIONS = {
  bpmn: { label: 'BPMN 2.0 XML', mimeType: 'application/xml', extension: '.bpmn' },
  svg: { label: 'SVG 图片', mimeType: 'image/svg+xml', extension: '.svg' },
  png: { label: 'PNG 图片', mimeType: 'image/png', extension: '.png' },
  pdf: { label: 'PDF 文档', mimeType: 'application/pdf', extension: '.pdf' },
  json: { label: 'JSON 模板', mimeType: 'application/json', extension: '.json' }
}

/**
 * 导出功能
 * @param {Object} options - 配置选项
 * @returns {Object} 导出 API
 */
export function useExport(options = {}) {
  const { bpmnModeler, processInfo } = options

  // 状态
  const exporting = ref(false)
  const exportOptionsVisible = ref(false)
  const exportOptions = ref({
    format: 'bpmn',
    include: ['formConfig'],
    filename: ''
  })

  /**
   * 下载文件
   */
  const downloadFile = (content, filename, type = 'application/octet-stream') => {
    const blob = new Blob([content], { type })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  }

  /**
   * 导出 BPMN XML
   */
  const exportBpmn = async () => {
    if (!bpmnModeler?.value) throw new Error('Modeler 未初始化')

    const { xml } = await bpmnModeler.value.saveXML({ format: true })
    const filename = `${processInfo?.value?.name || 'workflow'}.bpmn`
    downloadFile(xml, filename, 'application/xml')
    return xml
  }

  /**
   * 导出 SVG
   */
  const exportSvg = async () => {
    if (!bpmnModeler?.value) throw new Error('Modeler 未初始化')

    try {
      const { svg } = await bpmnModeler.value.saveSVG()
      const filename = `${processInfo?.value?.name || 'workflow'}.svg`
      downloadFile(svg, filename, 'image/svg+xml')
      return svg
    } catch (err) {
      console.error('导出 SVG 失败:', err)
      throw err
    }
  }

  /**
   * 导出 PNG
   */
  const exportPng = async () => {
    if (!bpmnModeler?.value) throw new Error('Modeler 未初始化')

    try {
      const { svg } = await bpmnModeler.value.saveSVG()

      // 创建 canvas 并绘制 SVG
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      const img = new Image()

      return new Promise((resolve, reject) => {
        img.onload = () => {
          canvas.width = img.width * 2 // 2x 分辨率
          canvas.height = img.height * 2
          ctx.scale(2, 2)
          ctx.drawImage(img, 0, 0)

          const dataUrl = canvas.toDataURL('image/png')
          const base64 = dataUrl.replace('data:image/png;base64,', '')
          const filename = `${processInfo?.value?.name || 'workflow'}.png`

          // 下载
          const a = document.createElement('a')
          a.href = dataUrl
          a.download = filename
          document.body.appendChild(a)
          a.click()
          document.body.removeChild(a)

          ElMessage.success('导出 PNG 成功')
          resolve(base64)
        }

        img.onerror = () => reject(new Error('图片加载失败'))
        img.src = 'data:image/svg+xml;base64,' + btoa(unescape(encodeURIComponent(svg)))
      })
    } catch (err) {
      console.error('导出 PNG 失败:', err)
      throw err
    }
  }

  /**
   * 导出 JSON 模板
   */
  const exportJson = async (includeOptions = {}) => {
    if (!bpmnModeler?.value) throw new Error('Modeler 未初始化')

    const { xml } = await bpmnModeler.value.saveXML({ format: true })

    const templateData = {
      name: processInfo?.value?.name || '未命名流程',
      description: processInfo?.value?.description || '',
      category: processInfo?.value?.category || '',
      bpmnXml: xml,
      createTime: new Date().toISOString(),
      version: '1.0'
    }

    // 可选包含的内容
    if (includeOptions.formConfig) {
      // templateData.formConfig = formConfig
    }

    const filename = `${processInfo?.value?.name || 'workflow'}.template.json`
    downloadFile(JSON.stringify(templateData, null, 2), filename, 'application/json')
    return templateData
  }

  /**
   * 通用导出方法
   */
  const exportToFile = async (format = 'bpmn', customOptions = {}) => {
    exporting.value = true

    try {
      let result
      switch (format) {
        case 'bpmn':
          result = await exportBpmn()
          break
        case 'svg':
          result = await exportSvg()
          break
        case 'png':
          result = await exportPng()
          break
        case 'json':
          result = await exportJson(customOptions.include)
          break
        default:
          throw new Error(`不支持的导出格式：${format}`)
      }

      ElMessage.success(`导出 ${EXPORT_OPTIONS[format].label} 成功`)
      return result
    } catch (err) {
      console.error('导出失败:', err)
      ElMessage.error('导出失败：' + err.message)
      throw err
    } finally {
      exporting.value = false
    }
  }

  /**
   * 打开导出选项对话框
   */
  const openExportDialog = (format = 'bpmn') => {
    exportOptions.value.format = format
    exportOptions.value.filename = processInfo?.value?.name || 'workflow'
    exportOptionsVisible.value = true
  }

  /**
   * 确认导出
   */
  const confirmExport = async () => {
    const { format, filename } = exportOptions.value

    if (!filename) {
      ElMessage.warning('请输入文件名')
      return
    }

    try {
      await exportToFile(format, exportOptions.value)
      exportOptionsVisible.value = false
    } catch (err) {
      // 错误已在 exportToFile 中处理
    }
  }

  /**
   * 获取导出格式选项
   */
  const getExportFormatOptions = () => {
    return Object.entries(EXPORT_OPTIONS).map(([value, { label, mimeType, extension }]) => ({
      value,
      label,
      mimeType,
      extension
    }))
  }

  return {
    // 状态
    exporting,
    exportOptionsVisible,
    exportOptions,

    // 方法
    exportBpmn,
    exportSvg,
    exportPng,
    exportJson,
    exportToFile,
    openExportDialog,
    confirmExport,
    getExportFormatOptions,
    downloadFile
  }
}
/**
 * BPMN Modeler 组合式函数
 * 封装 bpmn-js 模型器的初始化和核心操作
 */

import { ref, onMounted, onBeforeUnmount } from 'vue'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import {
  BpmnPropertiesPanelModule,
  BpmnPropertiesProviderModule,
  CamundaPlatformPropertiesProviderModule
} from 'bpmn-js-properties-panel'
import '@bpmn-io/properties-panel/assets/properties-panel.css'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css'

/**
 * 默认的 BPMN 2.0 XML - 空流程模板
 */
const DEFAULT_BPMN_XML = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
                  xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                  id="Definitions_1"
                  targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" name="新流程" isExecutable="true" camunda:versionTag="1.0.0">
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`

/**
 * BPMN Modeler 配置常量
 */
const MODELER_CONFIG = {
  palette: { visible: false }, // 使用自定义工具栏
  additionalModules: [
    BpmnPropertiesPanelModule,
    BpmnPropertiesProviderModule,
    CamundaPlatformPropertiesProviderModule
  ]
}

/**
 * 初始化并管理 BPMN Modeler
 * @param {Ref<HTMLDivElement>} canvasRef - 画布容器引用
 * @param {Ref<HTMLDivElement>} propertiesPanelRef - 属性面板容器引用
 * @returns {Object} Modeler API
 */
export function useBpmnModeler(canvasRef, propertiesPanelRef) {
  const bpmnModeler = ref(null)
  const currentZoom = ref(100)
  const isInitialized = ref(false)

  // 事件监听器引用，用于清理
  let eventListeners = []

  /**
   * 初始化 BPMN Modeler
   */
  const initModeler = () => {
    if (!canvasRef.value || !propertiesPanelRef.value) {
      console.error('画布或属性面板容器未准备好')
      return
    }

    try {
      bpmnModeler.value = new BpmnModeler({
        container: canvasRef.value,
        propertiesPanel: {
          parent: propertiesPanelRef.value
        },
        ...MODELER_CONFIG
      })

      // 加载默认图表
      bpmnModeler.value.importXML(DEFAULT_BPMN_XML, (err) => {
        if (err) {
          console.error('流程设计器初始化失败:', err)
          return
        }
      })

      // 监听视图变化更新缩放比例
      const canvas = bpmnModeler.value.get('canvas')
      const eventBus = bpmnModeler.value.get('eventBus')

      const zoomListener = ({ viewbox }) => {
        currentZoom.value = Math.round(100 / viewbox.scale)
      }
      eventBus.on('canvas.viewbox.changed', zoomListener)
      eventListeners.push({ event: 'canvas.viewbox.changed', handler: zoomListener })

      isInitialized.value = true
    } catch (error) {
      console.error('初始化 BPMN Modeler 失败:', error)
    }
  }

  /**
   * 清理 Modeler 资源
   */
  const disposeModeler = () => {
    // 移除所有事件监听
    if (bpmnModeler.value) {
      const eventBus = bpmnModeler.value.get('eventBus')
      eventListeners.forEach(({ event, handler }) => {
        eventBus.off(event, handler)
      })
      eventListeners = []

      bpmnModeler.value.destroy()
      bpmnModeler.value = null
    }
    isInitialized.value = false
  }

  /**
   * 导入 BPMN XML
   */
  const importXML = (xml) => {
    return new Promise((resolve, reject) => {
      if (!bpmnModeler.value) {
        reject(new Error('Modeler 未初始化'))
        return
      }
      bpmnModeler.value.importXML(xml, (err) => {
        if (err) reject(err)
        else resolve()
      })
    })
  }

  /**
   * 保存 BPMN XML
   */
  const saveXML = (options = {}) => {
    if (!bpmnModeler.value) {
      throw new Error('Modeler 未初始化')
    }
    return bpmnModeler.value.saveXML(options)
  }

  /**
   * 保存 SVG
   */
  const saveSVG = (options = {}) => {
    if (!bpmnModeler.value) {
      throw new Error('Modeler 未初始化')
    }
    return bpmnModeler.value.saveSVG(options)
  }

  /**
   * 撤销
   */
  const undo = () => {
    if (!bpmnModeler.value) return
    bpmnModeler.value.get('commandStack').undo()
  }

  /**
   * 重做
   */
  const redo = () => {
    if (!bpmnModeler.value) return
    bpmnModeler.value.get('commandStack').redo()
  }

  /**
   * 缩放
   */
  const zoom = (step = 0.1) => {
    if (!bpmnModeler.value) return
    const canvas = bpmnModeler.value.get('canvas')
    canvas.zoom(currentZoom.value / 100 + step)
  }

  const zoomIn = () => zoom(0.1)
  const zoomOut = () => zoom(-0.1)

  /**
   * 适应屏幕
   */
  const zoomFit = () => {
    if (!bpmnModeler.value) return
    const canvas = bpmnModeler.value.get('canvas')
    canvas.zoom('fit-viewport')
  }

  /**
   * 获取 Modeler 实例
   */
  const getModeler = () => bpmnModeler.value

  /**
   * 注册事件监听
   */
  const on = (event, handler) => {
    if (!bpmnModeler.value) return
    const eventBus = bpmnModeler.value.get('eventBus')
    eventBus.on(event, handler)
    eventListeners.push({ event, handler })
  }

  /**
   * 获取画布信息
   */
  const getCanvasInfo = () => {
    if (!bpmnModeler.value) return null
    const canvas = bpmnModeler.value.get('canvas')
    const elementRegistry = bpmnModeler.value.get('elementRegistry')

    const elements = elementRegistry.getAll()
    const processElements = elements.filter(el => el.businessObject?.$type !== 'bpmn:Definitions')

    return {
      zoom: currentZoom.value,
      elementCount: processElements.length,
      rootElement: canvas.getRootElement()
    }
  }

  return {
    // 状态
    bpmnModeler,
    currentZoom,
    isInitialized,

    // 方法
    initModeler,
    disposeModeler,
    importXML,
    saveXML,
    saveSVG,
    undo,
    redo,
    zoomIn,
    zoomOut,
    zoomFit,
    getModeler,
    on,
    getCanvasInfo
  }
}

export { DEFAULT_BPMN_XML, MODELER_CONFIG }
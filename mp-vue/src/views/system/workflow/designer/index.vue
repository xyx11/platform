<template>
  <div class="workflow-designer">
    <div class="designer-header">
      <el-button @click="newDiagram">新建</el-button>
      <el-button @click="openFile">打开</el-button>
      <el-button @click="saveDiagram">保存</el-button>
      <el-button @click="downloadDiagram">下载</el-button>
      <el-button @click="previewDiagram">预览</el-button>
      <el-button @click="deployDiagram" type="primary">部署</el-button>
    </div>
    <div class="designer-container">
      <div class="palette-container">
        <div class="palette-group">
          <div class="palette-item" data-action="create-start-event" title="开始事件">
            <svg viewBox="0 0 32 32"><circle cx="16" cy="16" r="14" fill="#28c961" stroke="#28c961" stroke-width="2"/></svg>
          </div>
          <div class="palette-item" data-action="create-end-event" title="结束事件">
            <svg viewBox="0 0 32 32"><circle cx="16" cy="16" r="14" fill="#ff3333" stroke="#ff3333" stroke-width="2"/></svg>
          </div>
          <div class="palette-item" data-action="create-user-task" title="用户任务">
            <svg viewBox="0 0 32 32"><rect x="2" y="6" width="28" height="20" rx="4" fill="#58a3d9" stroke="#58a3d9" stroke-width="2"/></svg>
          </div>
          <div class="palette-item" data-action="create-service-task" title="服务任务">
            <svg viewBox="0 0 32 32"><rect x="2" y="6" width="28" height="20" rx="4" fill="#f5c34f" stroke="#f5c34f" stroke-width="2"/></svg>
          </div>
          <div class="palette-item" data-action="create-exclusive-gateway" title="排他网关">
            <svg viewBox="0 0 32 32"><rect x="4" y="4" width="24" height="24" transform="rotate(45 16 16)" fill="#ffab45" stroke="#ffab45" stroke-width="2"/></svg>
          </div>
          <div class="palette-item" data-action="create-parallel-gateway" title="并行网关">
            <svg viewBox="0 0 32 32"><rect x="4" y="4" width="24" height="24" transform="rotate(45 16 16)" fill="#58a3d9" stroke="#58a3d9" stroke-width="2"/></svg>
          </div>
          <div class="palette-item" data-action="create-sequence-flow" title="顺序流">
            <svg viewBox="0 0 32 32"><path d="M6 16 L26 16 M20 10 L26 16 L20 22" stroke="#333" stroke-width="2" fill="none"/></svg>
          </div>
        </div>
      </div>
      <div class="canvas-container">
        <div ref="bpmnCanvas" class="bpmn-canvas"></div>
      </div>
      <div class="properties-panel-container">
        <div ref="propertiesPanel"></div>
      </div>
    </div>
  </div>
</template>

<script setup name="WorkflowDesigner">
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
import { ElMessage, ElMessageBox } from 'element-plus'

const bpmnCanvas = ref(null)
const propertiesPanel = ref(null)
const bpmnModeler = ref(null)
const currentDiagram = ref(null)
const processName = ref('')

// 默认的 BPMN 2.0 XML
const defaultBpmnXml = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" isExecutable="false">
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`

onMounted(() => {
  initBpmnModeler()
  initPalette()
})

onBeforeUnmount(() => {
  if (bpmnModeler.value) {
    bpmnModeler.value.destroy()
  }
})

const initBpmnModeler = () => {
  bpmnModeler.value = new BpmnModeler({
    container: bpmnCanvas.value,
    keyboard: {
      bindTo: window
    },
    propertiesPanel: {
      parent: propertiesPanel.value
    },
    additionalModules: [
      BpmnPropertiesPanelModule,
      BpmnPropertiesProviderModule,
      CamundaPlatformPropertiesProviderModule
    ]
  })
  // 初始化时加载空白图表
  bpmnModeler.value.importXML(defaultBpmnXml)
}

const initPalette = () => {
  const paletteContainer = document.querySelector('.palette-container')
  const paletteItems = paletteContainer.querySelectorAll('.palette-item')

  paletteItems.forEach(item => {
    item.addEventListener('click', () => {
      handlePaletteClick(item.dataset.action)
    })
  })
}

const handlePaletteClick = (action) => {
  const elementRegistry = bpmnModeler.value.get('elementRegistry')
  const modeling = bpmnModeler.value.get('modeling')
  const rootElement = elementRegistry.getRoot()

  switch (action) {
    case 'create-start-event': {
      const shape = modeling.createShape({
        type: 'bpmn:StartEvent',
        name: '开始'
      }, { x: 200, y: 200 }, rootElement)
      modeling.moveElements([shape], { x: 0, y: 0 }, rootElement)
      break
    }
    case 'create-end-event': {
      modeling.createShape({
        type: 'bpmn:EndEvent',
        name: '结束'
      }, { x: 400, y: 200 }, rootElement)
      break
    }
    case 'create-user-task': {
      modeling.createShape({
        type: 'bpmn:UserTask',
        name: '用户任务'
      }, { x: 200, y: 300 }, rootElement)
      break
    }
    case 'create-service-task': {
      modeling.createShape({
        type: 'bpmn:ServiceTask',
        name: '服务任务'
      }, { x: 400, y: 300 }, rootElement)
      break
    }
    case 'create-exclusive-gateway': {
      modeling.createShape({
        type: 'bpmn:ExclusiveGateway',
        name: '网关'
      }, { x: 300, y: 250 }, rootElement)
      break
    }
    case 'create-parallel-gateway': {
      modeling.createShape({
        type: 'bpmn:ParallelGateway',
        name: '并行网关'
      }, { x: 300, y: 250 }, rootElement)
      break
    }
    case 'create-sequence-flow': {
      // 需要在选中两个元素后创建连接
      ElMessage.info('请先选择起始元素，然后选择目标元素')
      break
    }
    default:
      break
  }
}

const newDiagram = () => {
  bpmnModeler.value.importXML(defaultBpmnXml, (err) => {
    if (err) {
      ElMessage.error('新建流程失败：' + err.message)
      return
    }
    currentDiagram.value = null
    processName.value = ''
    ElMessage.success('新建流程成功')
  })
}

const openFile = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.bpmn,.xml'
  input.onchange = (e) => {
    const file = e.target.files[0]
    const reader = new FileReader()
    reader.onload = (event) => {
      const xml = event.target.result
      try {
        bpmnModeler.value.importXML(xml)
        ElMessage.success('打开文件成功')
      } catch (err) {
        ElMessage.error('打开文件失败：' + err.message)
      }
    }
    reader.readAsText(file)
  }
  input.click()
}

const saveDiagram = async () => {
  if (!processName.value) {
    await ElMessageBox.prompt('请输入流程名称', '保存流程', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }).then(({ value }) => {
      if (value) {
        processName.value = value
      }
    }).catch(() => {
      return
    })
  }
  if (!processName.value) {
    ElMessage.warning('请输入流程名称')
    return
  }
  const { xml } = await bpmnModeler.value.saveXML({ format: true })
  try {
    await mockSaveApi({ name: processName.value, bpmnXml: xml })
    ElMessage.success('保存成功')
  } catch (err) {
    ElMessage.error('保存失败：' + err.message)
  }
}

const downloadDiagram = async () => {
  try {
    const { svg } = await bpmnModeler.value.saveSVG()
    const blob = new Blob([svg], { type: 'image/svg+xml' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = (processName.value || 'workflow') + '.svg'
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (err) {
    ElMessage.error('下载失败：' + err.message)
  }
}

const previewDiagram = async () => {
  try {
    const { svg } = await bpmnModeler.value.saveSVG()
    await ElMessageBox.alert(
      `<div style="max-height: 500px; overflow: auto;">${svg}</div>`,
      '流程预览',
      {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '关闭'
      }
    )
  } catch (err) {
    ElMessage.error('预览失败：' + err.message)
  }
}

const deployDiagram = async () => {
  if (!processName.value) {
    ElMessage.warning('请先保存流程')
    return
  }
  const { xml } = await bpmnModeler.value.saveXML({ format: true })
  try {
    await mockDeployApi({ name: processName.value, bpmnXml: xml })
    ElMessage.success('部署成功')
  } catch (err) {
    ElMessage.error('部署失败：' + err.message)
  }
}

const mockSaveApi = (params) => {
  console.log('保存流程:', params)
  return Promise.resolve({ success: true })
}

const mockDeployApi = (params) => {
  console.log('部署流程:', params)
  return Promise.resolve({ success: true })
}
</script>

<style scoped>
.workflow-designer {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.designer-header {
  padding: 10px;
  border-bottom: 1px solid #ddd;
  background: #f5f5f5;
  display: flex;
  gap: 8px;
}

.designer-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.palette-container {
  width: 60px;
  border-right: 1px solid #ddd;
  background: #fafafa;
  padding: 10px 5px;
}

.palette-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.palette-item {
  width: 48px;
  height: 48px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.palette-item:hover {
  border-color: #1e80ff;
  background: #e6f1ff;
}

.palette-item svg {
  width: 32px;
  height: 32px;
}

.canvas-container {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.bpmn-canvas {
  width: 100%;
  height: 100%;
}

.properties-panel-container {
  width: 300px;
  border-left: 1px solid #ddd;
  background: #fafafa;
  overflow: auto;
}

.properties-panel-container :deep(.bio-properties-panel) {
  background: transparent;
}

.properties-panel-container :deep(.bio-properties-panel-header) {
  background: #f5f5f5;
}
</style>
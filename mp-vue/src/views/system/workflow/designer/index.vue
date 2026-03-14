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
      <div ref="paletteContainer" class="palette-container"></div>
      <div class="canvas-container">
        <div ref="bpmnCanvas" class="bpmn-canvas"></div>
      </div>
      <div ref="propertiesPanel" class="properties-panel-container"></div>
    </div>
  </div>
</template>

<script>
const BpmnModeler = require('bpmn-js/lib/Modeler')
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'WorkflowDesigner',
  data() {
    return {
      bpmnModeler: null,
      currentDiagram: null,
      processName: ''
    }
  },
  mounted() {
    this.initBpmnModeler()
  },
  beforeUnmount() {
    if (this.bpmnModeler) {
      this.bpmnModeler.destroy()
    }
  },
  methods: {
    initBpmnModeler() {
      this.bpmnModeler = new BpmnModeler({
        container: this.$refs.bpmnCanvas,
        keyboard: {
          bindTo: window
        }
      })
    },
    newDiagram() {
      this.bpmnModeler.createNewDiagram()
      this.currentDiagram = null
      this.processName = ''
      ElMessage.success('新建流程成功')
    },
    openFile() {
      const input = document.createElement('input')
      input.type = 'file'
      input.accept = '.bpmn,.xml'
      input.onchange = (e) => {
        const file = e.target.files[0]
        const reader = new FileReader()
        reader.onload = (event) => {
          const xml = event.target.result
          try {
            this.bpmnModeler.importXML(xml)
            ElMessage.success('打开文件成功')
          } catch (err) {
            ElMessage.error('打开文件失败：' + err.message)
          }
        }
        reader.readAsText(file)
      }
      input.click()
    },
    async saveDiagram() {
      if (!this.processName) {
        await ElMessageBox.prompt('请输入流程名称', '保存流程', {
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        }).then(({ value }) => {
          if (value) {
            this.processName = value
          }
        }).catch(() => {
          return
        })
      }
      if (!this.processName) {
        ElMessage.warning('请输入流程名称')
        return
      }
      const { xml } = await this.bpmnModeler.saveXML({ format: true })
      // 调用后端 API 保存
      try {
        await this.$api?.workflow?.saveDefinition?.({
          name: this.processName,
          bpmnXml: xml
        }) || this.mockSaveApi({ name: this.processName, bpmnXml: xml })
        ElMessage.success('保存成功')
      } catch (err) {
        ElMessage.error('保存失败：' + err.message)
      }
    },
    async downloadDiagram() {
      try {
        const { svg } = await this.bpmnModeler.saveSVG()
        const blob = new Blob([svg], { type: 'image/svg+xml' })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = (this.processName || 'workflow') + '.svg'
        a.click()
        URL.revokeObjectURL(url)
        ElMessage.success('下载成功')
      } catch (err) {
        ElMessage.error('下载失败：' + err.message)
      }
    },
    async previewDiagram() {
      try {
        const { svg } = await this.bpmnModeler.saveSVG()
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
    },
    async deployDiagram() {
      if (!this.processName) {
        ElMessage.warning('请先保存流程')
        return
      }
      const { xml } = await this.bpmnModeler.saveXML({ format: true })
      try {
        await this.$api?.workflow?.deploy?.({
          name: this.processName,
          bpmnXml: xml
        }) || this.mockDeployApi({ name: this.processName, bpmnXml: xml })
        ElMessage.success('部署成功')
      } catch (err) {
        ElMessage.error('部署失败：' + err.message)
      }
    },
    mockSaveApi(params) {
      // Mock save API
      return Promise.resolve({ success: true })
    },
    mockDeployApi(params) {
      // Mock deploy API
      return Promise.resolve({ success: true })
    }
  }
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
  width: 40px;
  border-right: 1px solid #ddd;
  background: #fafafa;
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
}
</style>
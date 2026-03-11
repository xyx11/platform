# BPMN 流程设计器集成指南

## 1. 前端依赖安装

在 `mp-vue` 项目中安装 bpmn-js：

```bash
cd mp-vue
npm install bpmn-js --save
npm install diagram-js --save
```

## 2. 创建流程设计器组件

创建 `src/views/system/workflow/designer/index.vue`：

```vue
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
      <div class="palette-container"></div>
      <div class="canvas-container">
        <div ref="bpmnCanvas" class="bpmn-canvas"></div>
      </div>
      <div class="properties-panel-container"></div>
    </div>
  </div>
</template>

<script>
import { BpmnModeler } from 'bpmn-js/lib/Modeler';
import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css';

export default {
  name: 'WorkflowDesigner',
  data() {
    return {
      bpmnModeler: null,
      currentDiagram: null
    };
  },
  mounted() {
    this.initBpmnModeler();
  },
  methods: {
    initBpmnModeler() {
      this.bpmnModeler = new BpmnModeler({
        container: this.$refs.bpmnCanvas,
        keyboard: {
          bindTo: window
        }
      });
    },
    newDiagram() {
      this.bpmnModeler.createNewDiagram();
    },
    openFile() {
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = '.bpmn,.xml';
      input.onchange = (e) => {
        const file = e.target.files[0];
        const reader = new FileReader();
        reader.onload = (event) => {
          const xml = event.target.result;
          this.bpmnModeler.importXML(xml);
        };
        reader.readAsText(file);
      };
      input.click();
    },
    async saveDiagram() {
      const { xml } = await this.bpmnModeler.saveXML({ format: true });
      // 调用后端 API 保存
      this.$api.workflow.saveDefinition({
        name: this.currentDiagram?.name || '新流程',
        bpmnXml: xml
      });
      this.$message.success('保存成功');
    },
    async downloadDiagram() {
      const { svg } = await this.bpmnModeler.saveSVG();
      const blob = new Blob([svg], { type: 'image/svg+xml' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'workflow.svg';
      a.click();
      URL.revokeObjectURL(url);
    },
    previewDiagram() {
      // 打开预览对话框
    },
    async deployDiagram() {
      const { xml } = await this.bpmnModeler.saveXML({ format: true });
      await this.$api.workflow.deploy({
        name: this.currentDiagram?.name || '新流程',
        bpmnXml: xml
      });
      this.$message.success('部署成功');
    }
  }
};
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
}

.designer-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.palette-container {
  width: 40px;
  border-right: 1px solid #ddd;
}

.canvas-container {
  flex: 1;
  position: relative;
}

.bpmn-canvas {
  width: 100%;
  height: 100%;
}

.properties-panel-container {
  width: 300px;
  border-left: 1px solid #ddd;
}
</style>
```

## 3. 添加路由配置

在 `src/router/index.js` 中添加：

```javascript
{
  path: '/workflow/designer',
  name: 'WorkflowDesigner',
  component: () => import('@/views/system/workflow/designer/index.vue'),
  meta: { title: '流程设计器' }
}
```

## 4. 后端流程定义服务

创建流程定义管理接口：

```java
@PostMapping("/deploy")
@Operation(summary = "部署流程定义")
public Result<Map<String, Object>> deploy(@RequestBody Map<String, String> params) {
    String name = params.get("name");
    String bpmnXml = params.get("bpmnXml");

    Deployment deployment = repositoryService.createDeployment()
        .addStringInputStream(name + ".bpmn20.xml", new ByteArrayInputStream(bpmnXml.getBytes()))
        .name(name)
        .deploy();

    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .deploymentId(deployment.getId())
        .singleResult();

    Map<String, Object> result = new HashMap<>();
    result.put("definitionId", definition.getId());
    result.put("name", definition.getName());
    result.put("key", definition.getKey());
    result.put("version", definition.getVersion());

    return Result.success(result);
}
```

## 5. 推荐的 BPMN 元素

- **Start Event**: 开始事件
- **End Event**: 结束事件
- **User Task**: 用户任务
- **Service Task**: 服务任务
- **Exclusive Gateway**: 排他网关
- **Parallel Gateway**: 并行网关
- **Sequence Flow**: 顺序流

## 6. 流程变量配置

在用户任务中配置流程变量：

```xml
<userTask id="apply" name="提交申请" flowable:assignee="${applicant}">
  <extensionElements>
    <flowable:formData>
      <flowable:formField id="reason" label="请假事由" type="string" required="true"/>
      <flowable:formField id="days" label="请假天数" type="long" required="true"/>
    </flowable:formData>
  </extensionElements>
</userTask>
```

## 7. 与动态表单集成

流程设计器可以与动态表单设计器配合使用：
- 在用户任务中关联动态表单
- 任务办理时动态加载表单
- 表单数据作为流程变量
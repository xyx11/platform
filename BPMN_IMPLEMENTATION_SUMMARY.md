# BPMN 流程设计器实现总结

## 实现日期
2026-03-11

## 已完成功能

### 1. 前端依赖安装 ✅
```bash
cd mp-vue
npm install bpmn-js --save
npm install diagram-js --save
```

### 2. 流程设计器组件 ✅
**文件**: `mp-vue/src/views/system/workflow/designer/index.vue`

功能：
- 新建 BPMN 流程图
- 打开本地 BPMN 文件
- 保存流程定义
- 下载为 SVG
- 预览流程
- 部署到 Flowable 引擎

### 3. 路由配置 ✅
**文件**: `mp-vue/src/router/index.js`

新增路由：
- `/system/workflow-designer` - 流程设计器
- `/system/workflow-form` - 流程表单绑定
- `/system/workflow-definition` - 流程定义管理

### 4. 后端流程定义服务 ✅

#### WorkflowController.java
新增 API 端点：
- `POST /system/workflow/deploy` - 部署流程定义
- `POST /system/workflow/save` - 保存流程定义
- `GET /system/workflow/definition/list` - 获取流程定义列表
- `GET /system/workflow/definition/{id}` - 获取流程定义详情
- `DELETE /system/workflow/definition/{deploymentId}` - 删除流程定义
- `GET /system/workflow/definition/bpmn/{id}` - 获取 BPMN XML

#### WorkflowService.java / WorkflowServiceImpl.java
新增方法：
- `deployProcessDefinition()` - 部署流程
- `saveProcessDefinition()` - 保存流程
- `getProcessDefinitions()` - 查询流程列表
- `getProcessDefinition()` - 获取流程详情
- `deleteProcessDefinition()` - 删除流程
- `getProcessDefinitionBpmn()` - 获取 BPMN 内容

### 5. 流程定义管理前端页面 ✅
**文件**: `mp-vue/src/views/system/workflow-definition/index.vue`

功能：
- 流程定义列表展示（ID、名称、Key、版本、分类、状态）
- 部署新流程（支持上传 BPMN 文件或粘贴 XML）
- 查看 BPMN XML 内容
- 挂起/激活流程
- 删除流程定义

### 6. 工作流 - 表单集成 ✅

#### 后端服务
**文件**:
- `WorkflowFormService.java`
- `WorkflowFormServiceImpl.java`
- `WorkflowFormController.java`
- `WfFormBinding.java` (实体)
- `WfFormBindingMapper.java` (Mapper)

功能：
- `bindFormToProcess()` - 绑定表单到流程任务
- `getBoundForm()` - 获取绑定的表单
- `startProcessWithForm()` - 启动流程并提交表单
- `completeTaskWithForm()` - 完成任务并提交表单
- `getTaskForm()` - 获取任务表单
- `saveFormDataDraft()` - 保存表单草稿
- `getFormDataDraft()` - 获取表单草稿
- `getProcessFormHistory()` - 获取流程表单历史

#### 前端页面
**文件**: `mp-vue/src/views/system/workflow-form/index.vue`

功能：
- 流程表单绑定列表
- 绑定表单到流程任务
- 编辑/删除绑定关系

### 7. 数据库表 ✅
**文件**: `schema_enhancements.sql`

新增表：
- `wf_form_binding` - 工作流表单绑定关系表

新增菜单权限：
- 流程定义管理（菜单 ID: 33）
- 流程表单绑定（菜单 ID: 32）

## 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                      前端 Vue 应用                            │
├─────────────────┬─────────────────┬─────────────────────────┤
│  流程设计器      │  流程定义管理    │  流程表单绑定             │
│  designer/      │  definition/    │  form/                  │
│  index.vue      │  index.vue      │  index.vue              │
└────────┬────────┴────────┬────────┴──────────┬──────────────┘
         │                 │                    │
         ▼                 ▼                    ▼
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot 后端                          │
├─────────────────┬─────────────────┬─────────────────────────┤
│ Workflow        │ WorkflowForm    │ FormDefinition          │
│ Controller      │ Controller      │ Controller              │
├─────────────────┼─────────────────┼─────────────────────────┤
│ Workflow        │ WorkflowForm    │ FormDefinition          │
│ Service         │ Service         │ Service                 │
├─────────────────┼─────────────────┼─────────────────────────┤
│ Flowable        │ WfFormBinding   │ form_definition         │
│ Repository/     │ Mapper          │ Mapper                  │
│ Runtime/Task    │                 │                         │
└─────────────────┴─────────────────┴─────────────────────────┘
         │                 │                    │
         ▼                 ▼                    ▼
┌─────────────────┬─────────────────┬─────────────────────────┐
│    Database     │   Flowable      │      Redis              │
│    MySQL        │   Engine        │      Cache              │
└─────────────────┴─────────────────┴─────────────────────────┘
```

## API 端点总览

### 流程定义管理
| 方法 | 端点 | 描述 |
|------|------|------|
| POST | `/system/workflow/deploy` | 部署流程定义 |
| POST | `/system/workflow/save` | 保存流程定义 |
| GET | `/system/workflow/definition/list` | 获取流程列表 |
| GET | `/system/workflow/definition/{id}` | 获取流程详情 |
| DELETE | `/system/workflow/definition/{deploymentId}` | 删除流程 |
| GET | `/system/workflow/definition/bpmn/{id}` | 获取 BPMN XML |

### 流程实例管理
| 方法 | 端点 | 描述 |
|------|------|------|
| POST | `/system/workflow/start/{key}` | 启动流程 |
| GET | `/system/workflow/todo` | 获取待办任务 |
| POST | `/system/workflow/complete/{taskId}` | 完成任务 |
| GET | `/system/workflow/variables/{id}` | 获取流程变量 |
| DELETE | `/system/workflow/{id}` | 删除流程 |
| POST | `/system/workflow/suspend/{id}` | 挂起流程 |
| POST | `/system/workflow/activate/{id}` | 激活流程 |

### 流程表单集成
| 方法 | 端点 | 描述 |
|------|------|------|
| POST | `/system/workflow-form/bind` | 绑定表单 |
| GET | `/system/workflow-form/bound-form` | 获取绑定表单 |
| POST | `/system/workflow-form/start` | 启动流程并提交表单 |
| POST | `/system/workflow-form/complete` | 完成任务并提交表单 |
| GET | `/system/workflow-form/task-form` | 获取任务表单 |
| POST | `/system/workflow-form/draft` | 保存草稿 |
| GET | `/system/workflow-form/draft` | 获取草稿 |
| GET | `/system/workflow-form/history` | 获取历史 |
| GET | `/system/workflow-form/form-definition/{code}` | 获取表单定义 |

## 使用流程

1. **设计流程**: 使用 BPMN 流程设计器绘制流程图
2. **设计表单**: 使用表单设计器创建动态表单
3. **绑定表单**: 在流程表单绑定页面，将表单与流程任务关联
4. **部署流程**: 部署 BPMN 流程定义
5. **启动流程**: 用户填写启动表单，发起流程
6. **办理任务**: 审批人填写办理表单，完成任务
7. **查看历史**: 查看流程中的表单提交记录

## 后续优化建议

1. **流程设计器增强**
   - 添加属性面板，可配置任务节点详情
   - 支持直接从设计器绑定表单
   - 添加流程验证功能

2. **表单集成增强**
   - 支持一个任务绑定多个表单
   - 添加表单字段与流程变量映射
   - 支持表单权限控制

3. **任务办理增强**
   - 添加任务转办、委派功能
   - 支持会签、或签
   - 添加审批意见记录

4. **监控增强**
   - 添加流程实例监控面板
   - 支持流程追踪（轨迹图）
   - 添加流程统计数据
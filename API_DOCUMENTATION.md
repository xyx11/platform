# Micro Platform API 接口文档

## 概述

本文档描述了 Micro Platform 系统的所有 REST API 接口。

**基础路径**: `/system`

**认证方式**: Bearer Token (Sa-Token)

**请求头**:
```
Authorization: Bearer {token}
Content-Type: application/json
```

---

## 1. 工作流管理 API

### 1.1 启动流程

```http
POST /system/workflow/start/{processDefinitionKey}
```

**路径参数**:
- `processDefinitionKey` - 流程定义 Key

**查询参数**:
- `businessKey` - 业务主键 (必填)

**请求体**:
```json
{
  "variable1": "value1",
  "variable2": "value2"
}
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "processInstanceId": "proc_123",
    "businessKey": "1001",
    "processDefinitionId": "proc_def_456"
  }
}
```

---

### 1.2 获取待办任务

```http
GET /system/workflow/todo
```

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "taskId": "task_123",
      "taskName": "审批任务",
      "assignee": "user1",
      "createTime": "2026-03-11T10:00:00",
      "processInstanceId": "proc_123",
      "processDefinitionId": "proc_def_456"
    }
  ]
}
```

---

### 1.3 完成任务

```http
POST /system/workflow/complete/{taskId}
```

**请求体**:
```json
{
  "approved": true,
  "comment": "同意"
}
```

---

### 1.4 部署流程定义

```http
POST /system/workflow/deploy
```

**请求参数**:
- `name` - 流程名称 (必填)
- `bpmnXml` - BPMN XML 内容 (必填)

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "deploymentId": "deploy_123",
    "definitionId": "proc_def_456",
    "name": "请假流程",
    "key": "leave_process",
    "version": 1
  }
}
```

---

### 1.5 获取流程定义列表

```http
GET /system/workflow/definition/list
```

**查询参数**:
- `category` - 流程分类 (可选)

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "id": "proc_def_456",
      "name": "请假流程",
      "key": "leave_process",
      "version": 1,
      "category": "hr",
      "deploymentId": "deploy_123",
      "resourceName": "leave_process.bpmn20.xml",
      "suspended": false
    }
  ]
}
```

---

### 1.6 获取流程定义 BPMN

```http
GET /system/workflow/definition/bpmn/{processDefinitionId}
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "bpmnXml": "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bpmn:processDefinition>..."
  }
}
```

---

### 1.7 删除流程定义

```http
DELETE /system/workflow/definition/{deploymentId}
```

---

### 1.8 挂起/激活流程

```http
POST /system/workflow/suspend/{processInstanceId}
POST /system/workflow/activate/{processInstanceId}
```

---

### 1.9 获取流程变量

```http
GET /system/workflow/variables/{processInstanceId}
```

---

## 2. 工作流表单集成 API

### 2.1 绑定表单到流程

```http
POST /system/workflow-form/bind
```

**请求体**:
```json
{
  "processDefinitionKey": "leave_process",
  "taskDefinitionKey": "manager_approve",
  "formCode": "leave_form",
  "formType": 2
}
```

---

### 2.2 获取绑定的表单

```http
GET /system/workflow-form/bound-form
```

**查询参数**:
- `processDefinitionKey` - 流程定义 Key
- `taskDefinitionKey` - 任务节点 Key
- `formType` - 表单类型 (1:启动表单 2:办理表单)

---

### 2.3 启动流程并提交表单

```http
POST /system/workflow-form/start
```

**请求体**:
```json
{
  "processDefinitionKey": "leave_process",
  "businessKey": "1001",
  "formCode": "leave_form",
  "formData": {
    "reason": "事假",
    "days": 3,
    "startDate": "2026-03-15"
  }
}
```

---

### 2.4 完成任务并提交表单

```http
POST /system/workflow-form/complete
```

**请求体**:
```json
{
  "taskId": "task_123",
  "formCode": "manager_approve_form",
  "formData": {
    "approved": true,
    "comment": "同意"
  },
  "saveDraft": false
}
```

---

### 2.5 获取任务表单

```http
GET /system/workflow-form/task-form?taskId={taskId}
```

---

### 2.6 保存/获取表单草稿

```http
POST /system/workflow-form/draft
GET /system/workflow-form/draft?processInstanceId={id}&taskDefinitionKey={key}
```

---

### 2.7 获取流程表单历史

```http
GET /system/workflow-form/history?processInstanceId={id}
```

---

## 3. 表单定义管理 API

### 3.1 获取表单定义列表

```http
GET /system/form-definition/list
```

**查询参数**:
- `pageNum` - 页码 (默认 1)
- `pageSize` - 每页大小 (默认 10)
- `formName` - 表单名称 (可选)
- `category` - 分类 (可选)

---

### 3.2 获取表单定义详情

```http
GET /system/form-definition/{id}
GET /system/form-definition/code/{formCode}
```

---

### 3.3 创建表单定义

```http
POST /system/form-definition
```

**请求体**:
```json
{
  "formName": "请假申请表",
  "formCode": "leave_apply_form",
  "formType": 2,
  "formSchema": {
    "type": "object",
    "properties": {
      "reason": { "type": "string", "title": "请假事由" },
      "days": { "type": "number", "title": "请假天数" }
    }
  },
  "category": "hr",
  "remark": "用于员工请假申请"
}
```

---

### 3.4 更新表单定义

```http
PUT /system/form-definition
```

---

### 3.5 发布/停用表单

```http
POST /system/form-definition/publish/{id}
POST /system/form-definition/disable/{id}
```

---

### 3.6 删除表单定义

```http
DELETE /system/form-definition/{id}
```

---

## 4. 租户套餐管理 API

### 4.1 获取套餐列表

```http
GET /system/tenant-package/list
```

---

### 4.2 获取可用套餐

```http
GET /system/tenant-package/available
```

---

### 4.3 创建套餐

```http
POST /system/tenant-package
```

**请求体**:
```json
{
  "name": "专业版",
  "code": "professional",
  "description": "适合中小企业",
  "packageType": 3,
  "price": 299.00,
  "maxUsers": 100,
  "maxDepts": 50,
  "maxStorage": 10000,
  "status": 1,
  "sort": 3
}
```

---

### 4.4 更新套餐

```http
PUT /system/tenant-package
```

---

### 4.5 停用套餐

```http
POST /system/tenant-package/disable/{id}
```

---

### 4.6 删除套餐

```http
DELETE /system/tenant-package/{id}
```

---

## 5. 数据权限管理 API

### 5.1 获取数据权限列表

```http
GET /system/data-permission/list
```

---

### 5.2 获取角色数据权限

```http
GET /system/data-permission/role/{roleId}
```

---

### 5.3 创建数据权限规则

```http
POST /system/data-permission
```

**请求体**:
```json
{
  "roleId": 100,
  "menuId": 200,
  "tableName": "sys_user",
  "permissionType": 1,
  "ruleExpression": "#userId == data.id",
  "dataFilter": "dept_id = 100"
}
```

---

### 5.4 更新/删除数据权限

```http
PUT /system/data-permission
DELETE /system/data-permission/{id}
```

---

## 6. 在线用户管理 API

### 6.1 获取在线用户列表

```http
GET /system/online-user/list?username={username}
```

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "userId": 1,
      "token": "abc123...",
      "isCurrent": false,
      "timeout": 3600,
      "expireTime": "2026-03-11T12:00:00"
    }
  ]
}
```

---

### 6.2 踢出用户

```http
POST /system/online-user/kickout/{userId}
```

---

### 6.3 获取在线用户数量

```http
GET /system/online-user/count
```

---

## 7. 系统监控 API

### 7.1 获取服务器信息

```http
GET /system/monitor/server
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "cpuInfo": {
      "cpuNum": 8,
      "systemLoadAverage": 2.5,
      "systemCpuLoad": 0.35
    },
    "memoryInfo": {
      "total": 17179869184,
      "used": 8589934592,
      "free": 8589934592,
      "usage": 50.0
    },
    "jvmInfo": {
      "name": "G1 Young Generation",
      "version": "17.0.1",
      "startTime": "2026-03-11T08:00:00",
      "runTime": 14400,
      "home": "/Library/Java/JavaVirtualMachines/jdk-17.jdk"
    },
    "diskInfo": [
      {
        "dirName": "/",
        "total": 500000000000,
        "free": 250000000000,
        "used": 250000000000,
        "usage": 50.0
      }
    ]
  }
}
```

---

## 错误响应

所有 API 错误返回统一格式：

```json
{
  "code": 500,
  "message": "错误描述信息",
  "data": null
}
```

**常见错误码**:
- `200` - 成功
- `400` - 请求参数错误
- `401` - 未授权
- `403` - 无权限
- `404` - 资源不存在
- `500` - 服务器内部错误

---

## 权限说明

API 接口使用 `@PreAuthorize` 注解控制访问权限：

```java
@PreAuthorize("@ss.hasPermission('system:workflow:start')")
```

权限格式：`模块：功能：操作`

例如：
- `system:workflow:start` - 工作流启动
- `system:tenant-package:add` - 套餐新增
- `system:data-permission:query` - 数据权限查询
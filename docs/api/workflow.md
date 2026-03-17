# 流程定义 API

**基础路径：** `/system/workflow`

## 1. 获取已部署的流程定义列表

```http
GET /system/workflow/process-definition/list
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| processDefinitionKey | String | 否 | 流程定义 Key |
| processDefinitionName | String | 否 | 流程定义名称 |
| pageNum | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 10 |

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "id": "leave_process:1:12345",
        "key": "leave_process",
        "name": "请假流程",
        "version": 1,
        "deploymentId": "12345",
        "resourceName": "leave.bpmn20.xml",
        "suspended": false,
        "tenantId": "",
        "description": "员工请假审批流程"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

## 2. 部署流程

```http
POST /system/workflow/deploy
```

**请求体：**

```json
{
  "name": "请假流程",
  "category": "人事",
  "description": "员工请假审批流程",
  "bpmnXml": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>..."
}
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "部署成功",
  "data": {
    "deploymentId": "12345",
    "processDefinitionId": "leave_process:1:12345"
  }
}
```

---

## 3. 删除流程

```http
DELETE /system/workflow/delete?deploymentId=xxx
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| deploymentId | String | 是 | 部署 ID |

---

## 4. 获取流程定义详情

```http
GET /system/workflow/process-definition/{processDefinitionId}
```

---

## 5. 获取流程定义 BPMN

```http
GET /system/workflow/process-definition/{processDefinitionId}/bpmn
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "bpmn": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>..."
  }
}
```

---

## 6. 启动/激活流程

```http
PUT /system/workflow/process-definition/{processDefinitionId}/activate
```

---

## 7. 挂起流程

```http
PUT /system/workflow/process-definition/{processDefinitionId}/suspend
```
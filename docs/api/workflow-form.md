# 工作流表单 API

**基础路径：** `/system/workflow-form`

## 1. 获取表单绑定列表

```http
GET /system/workflow-form/list
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| processDefinitionKey | String | 否 | 流程定义 Key |
| taskDefinitionKey | String | 否 | 任务定义 Key |
| formName | String | 否 | 表单名称 |
| formKey | String | 否 | 表单 Key |
| formType | Integer | 否 | 表单类型 (1-启动表单，2-办理表单) |
| status | Integer | 否 | 状态 (0-停用，1-启用) |
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
        "bindingId": 1,
        "processDefinitionKey": "leave_process",
        "processDefinitionName": "请假流程",
        "taskDefinitionKey": "manager_approval",
        "taskName": "经理审批",
        "formKey": "leave_approval_form",
        "formName": "请假审批表",
        "formType": 2,
        "status": 1
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

## 2. 获取流程的所有表单绑定

```http
GET /system/workflow-form/process/{processDefinitionKey}
```

---

## 3. 绑定表单

```http
POST /system/workflow-form/bind
```

**请求体：**

```json
{
  "processDefinitionKey": "leave_process",
  "processDefinitionName": "请假流程",
  "taskDefinitionKey": "manager_approval",
  "taskName": "经理审批",
  "formKey": "leave_approval_form",
  "formName": "请假审批表",
  "formType": 2,
  "status": 1
}
```

---

## 4. 解除绑定

```http
POST /system/workflow-form/unbind?processDefinitionKey=xxx&taskDefinitionKey=yyy
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| processDefinitionKey | String | 是 | 流程定义 Key |
| taskDefinitionKey | String | 否 | 任务定义 Key（为空则解除整个流程的绑定） |

---

## 5. 获取可用的表单列表

```http
GET /system/workflow-form/available/{processDefinitionKey}
```

---

## 6. 获取启动表单

```http
GET /system/workflow-form/start/{processKey}
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "formKey": "leave_start_form",
    "formName": "请假申请表",
    "formSchema": {...},
    "formConfig": {...}
  }
}
```

---

## 7. 获取任务表单

```http
GET /system/workflow-form/task/{taskDefinitionKey}
```

---

## 8. 更新表单状态

```http
POST /system/workflow-form/status
```

**请求体：**

```json
{
  "bindingId": 1,
  "status": 0
}
```
# 任务管理 API

**基础路径：** `/system/workflow/task`

## 1. 获取待办任务列表

```http
GET /system/workflow/task/todo
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| processDefinitionKey | String | 否 | 流程定义 Key |
| processDefinitionName | String | 否 | 流程定义名称 |
| taskName | String | 否 | 任务名称 |
| priority | Integer | 否 | 优先级 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "rows": [
      {
        "taskId": "12345",
        "taskName": "经理审批",
        "taskDefinitionKey": "manager_approval",
        "processInstanceId": "67890",
        "processDefinitionId": "leave_process:1:11111",
        "processDefinitionName": "请假流程",
        "businessKey": "LEAVE-2024-001",
        "assignee": "lisi",
        "owner": "zhangsan",
        "priority": 50,
        "createTime": "2024-01-01 10:00:00",
        "duration": null,
        "variables": {
          "applicant": "张三",
          "leaveType": "事假",
          "days": 5
        }
      }
    ],
    "total": 1
  }
}
```

---

## 2. 获取已办任务列表

```http
GET /system/workflow/task/done
```

**请求参数：** 同待办任务列表

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "rows": [
      {
        "taskId": "12345",
        "taskName": "经理审批",
        "taskDefinitionKey": "manager_approval",
        "processInstanceId": "67890",
        "processDefinitionName": "请假流程",
        "assignee": "lisi",
        "claimTime": "2024-01-01 10:00:00",
        "endTime": "2024-01-01 11:00:00",
        "duration": 3600000
      }
    ],
    "total": 1
  }
}
```

---

## 3. 获取任务详情

```http
GET /system/workflow/task/{taskId}
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "taskId": "12345",
    "taskName": "经理审批",
    "taskDefinitionKey": "manager_approval",
    "description": "请审批员工的请假申请",
    "processInstanceId": "67890",
    "processDefinitionId": "leave_process:1:11111",
    "processDefinitionName": "请假流程",
    "businessKey": "LEAVE-2024-001",
    "assignee": "lisi",
    "owner": "zhangsan",
    "priority": 50,
    "createTime": "2024-01-01 10:00:00",
    "formKey": "leave_approval_form",
    "formName": "请假审批表",
    "variables": {
      "applicant": "张三",
      "leaveType": "事假",
      "days": 5,
      "reason": "家里有急事"
    }
  }
}
```

---

## 4. 办理任务

```http
POST /system/workflow/task/complete
```

**请求体：**

```json
{
  "taskId": "12345",
  "approved": true,
  "comment": "同意",
  "variables": {
    "approvalResult": "agree",
    "managerComment": "同意请假"
  }
}
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "nextTaskId": "23456",
    "nextTaskName": "人事备案",
    "processCompleted": false
  }
}
```

---

## 5. 退回任务

```http
POST /system/workflow/task/return
```

**请求体：**

```json
{
  "taskId": "12345",
  "targetActivityId": "start",
  "comment": "资料不全，请补充"
}
```

---

## 6. 转办任务

```http
POST /system/workflow/task/transfer
```

**请求体：**

```json
{
  "taskId": "12345",
  "targetUserId": "wangwu",
  "comment": "请帮忙处理"
}
```

---

## 7. 委派任务

```http
POST /system/workflow/task/delegate
```

**请求体：**

```json
{
  "taskId": "12345",
  "targetUserId": "wangwu",
  "comment": "暂由你处理"
}
```

---

## 8. 认领任务

```http
POST /system/workflow/task/claim
```

**请求体：**

```json
{
  "taskId": "12345",
  "userId": "lisi"
}
```

---

## 9. 获取任务表单

```http
GET /system/workflow/task/{taskId}/form
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "formKey": "leave_approval_form",
    "formName": "请假审批表",
    "formSchema": {
      "fields": [
        {
          "label": "审批结果",
          "prop": "approvalResult",
          "type": "radio",
          "options": [
            {"label": "同意", "value": "agree"},
            {"label": "拒绝", "value": "reject"}
          ]
        },
        {
          "label": "审批意见",
          "prop": "comment",
          "type": "textarea"
        }
      ]
    }
  }
}
```

---

## 10. 获取任务评论

```http
GET /system/workflow/task/{taskId}/comments
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": "comment_1",
      "taskId": "12345",
      "userId": "zhangsan",
      "userName": "张三",
      "message": "同意请假",
      "createTime": "2024-01-01 11:00:00",
      "type": "comment"
    }
  ]
}
```

---

## 11. 添加任务评论

```http
POST /system/workflow/task/{taskId}/comment
```

**请求体：**

```json
{
  "message": "请注意交接工作"
}
```

---

## 12. 获取任务历史

```http
GET /system/workflow/task/{taskId}/history
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": "history_1",
      "taskId": "12345",
      "activityId": "manager_approval",
      "activityName": "经理审批",
      "assignee": "lisi",
      "startTime": "2024-01-01 10:00:00",
      "endTime": "2024-01-01 11:00:00",
      "duration": 3600000,
      "deleteReason": null
    }
  ]
}
```
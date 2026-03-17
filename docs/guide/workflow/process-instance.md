# 流程实例

## 功能说明

流程实例管理模块用于管理运行中的流程实例，包括实例查询、挂起、激活、终止等操作。

## 主要功能

- 流程实例列表查询
- 流程实例详情查看
- 流程实例挂起/激活
- 流程实例终止
- 流程图查看
- 流转历史查询

## API 接口

### 1. 获取流程实例列表

```http
GET /system/workflow/process-instance/list
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| processDefinitionKey | String | 否 | 流程定义 Key |
| businessKey | String | 否 | 业务 Key |
| status | String | 否 | 状态（active/suspended） |
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
        "id": "12345",
        "processDefinitionId": "leave_process:1:67890",
        "processDefinitionName": "请假流程",
        "businessKey": "LEAVE-2024-001",
        "startUserId": "admin",
        "startTime": "2024-01-01 10:00:00",
        "endTime": null,
        "duration": null,
        "suspended": false,
        "activityId": "manager_approval",
        "activityName": "经理审批"
      }
    ],
    "total": 1
  }
}
```

### 2. 获取流程实例详情

```http
GET /system/workflow/process-instance/{processInstanceId}
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": "12345",
    "processDefinitionId": "leave_process:1:67890",
    "processDefinitionName": "请假流程",
    "businessKey": "LEAVE-2024-001",
    "startUserId": "admin",
    "startTime": "2024-01-01 10:00:00",
    "variables": {
      "applicant": "张三",
      "leaveType": "事假",
      "days": 5,
      "reason": "家里有急事"
    },
    "currentActivities": [
      {
        "id": "manager_approval",
        "name": "经理审批",
        "assignee": "lisi"
      }
    ]
  }
}
```

### 3. 启动流程实例

```http
POST /system/workflow/process-instance/start
```

**请求体：**

```json
{
  "processDefinitionId": "leave_process:1:67890",
  "businessKey": "LEAVE-2024-001",
  "variables": {
    "applicant": "张三",
    "leaveType": "事假",
    "days": 5,
    "reason": "家里有急事"
  }
}
```

### 4. 挂起流程实例

```http
PUT /system/workflow/process-instance/{processInstanceId}/suspend
```

### 5. 激活流程实例

```http
PUT /system/workflow/process-instance/{processInstanceId}/activate
```

### 6. 终止流程实例

```http
DELETE /system/workflow/process-instance/{processInstanceId}
```

**请求体：**

```json
{
  "reason": "流程异常终止"
}
```

### 7. 查看流程图

```http
GET /system/workflow/process-instance/{processInstanceId}/diagram
```

**响应：** 返回流程图的 PNG 或 SVG 数据

### 8. 获取流转历史

```http
GET /system/workflow/process-instance/{processInstanceId}/history
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": "history_1",
      "processInstanceId": "12345",
      "activityId": "start",
      "activityName": "开始",
      "activityType": "startEvent",
      "startTime": "2024-01-01 10:00:00",
      "endTime": "2024-01-01 10:00:01",
      "duration": 1000,
      "userId": "admin"
    },
    {
      "id": "history_2",
      "processInstanceId": "12345",
      "activityId": "manager_approval",
      "activityName": "经理审批",
      "activityType": "userTask",
      "startTime": "2024-01-01 10:00:01",
      "endTime": null,
      "duration": null,
      "assignee": "lisi"
    }
  ]
}
```

## 流程变量

### 设置流程变量

```http
PUT /system/workflow/process-instance/{processInstanceId}/variables
```

**请求体：**

```json
{
  "variables": {
    "approvalResult": "agree",
    "comment": "同意"
  }
}
```

### 获取流程变量

```http
GET /system/workflow/process-instance/{processInstanceId}/variables
```

## 流程图例

```
┌──────────────┐
│   开始        │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  提交申请     │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  经理审批     │─── 拒绝 ───► │  结束       │
└──────┬───────┘              └────────────┘
       │ 同意
       ▼
┌──────────────┐
│  人事备案     │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│   结束        │
└──────────────┘
```

## 数据表结构

Flowable 内置表：

```sql
-- 运行时流程实例表
ACT_RU_EXECUTION

-- 运行时任务表
ACT_RU_TASK

-- 运行时变量表
ACT_RU_VARIABLE

-- 历史流程实例表
ACT_HI_PROCINST

-- 历史任务表
ACT_HI_TASKINST

-- 历史变量表
ACT_HI_VARINST
```
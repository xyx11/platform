# 工作流任务管理 API 文档

## 概述

工作流任务管理模块提供了完整的任务操作功能，包括待办任务、已办任务、任务完成、转办、委派等。

---

## API 接口

### 1. 获取待办任务列表

**接口**: `GET /system/workflow/task/todo`

**权限**: 登录用户

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页大小 |

**响应示例**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "taskId": "task-123",
      "taskName": "请假审批",
      "taskKey": "leave_approval",
      "assignee": "1001",
      "owner": "1001",
      "priority": "normal",
      "createTime": "2024-03-11T10:00:00.000+08:00",
      "processInstanceId": "proc-456",
      "processDefinitionId": "def-789",
      "description": "张三的请假申请"
    }
  ]
}
```

---

### 2. 获取已办任务列表

**接口**: `GET /system/workflow/task/done`

**权限**: 登录用户

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页大小 |

**响应示例**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "taskId": "task-123",
      "taskName": "请假审批",
      "taskKey": "leave_approval",
      "assignee": "1001",
      "createTime": "2024-03-11T10:00:00.000+08:00",
      "endTime": "2024-03-11T11:00:00.000+08:00",
      "duration": 3600000,
      "description": "张三的请假申请",
      "deleteReason": "completed"
    }
  ]
}
```

---

### 3. 获取任务详情

**接口**: `GET /system/workflow/task/{taskId}`

**权限**: `system:workflow:query`

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| taskId | string | 任务 ID |

**响应示例**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "taskId": "task-123",
    "taskName": "请假审批",
    "taskKey": "leave_approval",
    "assignee": "1001",
    "priority": "normal",
    "createTime": "2024-03-11T10:00:00.000+08:00",
    "processInstanceId": "proc-456",
    "variables": {
      "applicant": "张三",
      "days": 5,
      "reason": "回家过年"
    }
  }
}
```

---

### 4. 完成任务

**接口**: `POST /system/workflow/task/{taskId}/complete`

**权限**: `system:workflow:complete`

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| taskId | string | 任务 ID |

**请求体** (可选):
```json
{
  "approved": true,
  "comment": "同意"
}
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

### 5. 转办任务

**接口**: `POST /system/workflow/task/{taskId}/transfer`

**权限**: `system:workflow:transfer`

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| taskId | string | 任务 ID |

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| newAssignee | string | 是 | 新处理人 ID |

**响应示例**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

### 6. 委派任务

**接口**: `POST /system/workflow/task/{taskId}/delegate`

**权限**: `system:workflow:delegate`

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| taskId | string | 任务 ID |

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| delegateUser | string | 是 | 被委派人 ID |

**响应示例**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

### 7. 获取任务历史

**接口**: `GET /system/workflow/task/{processInstanceId}/history`

**权限**: `system:workflow:query`

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| processInstanceId | string | 流程实例 ID |

**响应示例**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "taskId": "task-1",
      "taskName": "部门经理审批",
      "taskKey": "dept_manager_approval",
      "assignee": "1002",
      "createTime": "2024-03-11T10:00:00.000+08:00",
      "endTime": "2024-03-11T10:30:00.000+08:00",
      "duration": 1800000
    },
    {
      "taskId": "task-2",
      "taskName": "人事审批",
      "taskKey": "hr_approval",
      "assignee": "1003",
      "createTime": "2024-03-11T10:30:00.000+08:00",
      "endTime": "2024-03-11T11:00:00.000+08:00",
      "duration": 1800000
    }
  ]
}
```

---

### 8. 获取任务统计

**接口**: `GET /system/workflow/task/stats`

**权限**: 登录用户

**响应示例**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "todoCount": 5,
    "doneCount": 20,
    "highPriorityCount": 2,
    "normalPriorityCount": 3,
    "lowPriorityCount": 0
  }
}
```

---

## 权限配置

需要在系统中配置以下权限：

| 权限标识 | 说明 | 对应接口 |
|---------|------|---------|
| system:workflow:query | 流程查询权限 | 任务详情、任务历史 |
| system:workflow:complete | 完成任务权限 | 完成任务 |
| system:workflow:transfer | 转办任务权限 | 转办任务 |
| system:workflow:delegate | 委派任务权限 | 委派任务 |

---

## 菜单配置

```sql
-- 任务管理菜单
INSERT INTO sys_menu (parent_id, name, path, component, perms, type, icon, sort)
VALUES (
  (SELECT menu_id FROM sys_menu WHERE path = '/system' LIMIT 1),
  '任务管理',
  '/system/task',
  'system/task/index.vue',
  'system:workflow:query',
  1,
  'List',
  17
);
```

---

## 错误码

| 错误码 | 说明 |
|--------|------|
| 500 | 服务器内部错误 |
| 403 | 权限不足 |
| 404 | 任务不存在 |

---

## 使用示例

### JavaScript (Axios)

```javascript
import request from '@/utils/request'

// 获取待办任务
const getTodoTasks = () => {
  return request({
    url: '/system/workflow/task/todo',
    method: 'get',
    params: { pageNum: 1, pageSize: 10 }
  })
}

// 完成任务
const completeTask = (taskId, variables) => {
  return request({
    url: `/system/workflow/task/${taskId}/complete`,
    method: 'post',
    data: variables
  })
}

// 转办任务
const transferTask = (taskId, newAssignee) => {
  return request({
    url: `/system/workflow/task/${taskId}/transfer`,
    method: 'post',
    params: { newAssignee }
  })
}
```

### Java (Spring RestTemplate)

```java
// 获取任务详情
ResponseEntity<Result<Map>> response = restTemplate.getForEntity(
    "http://localhost:8080/system/workflow/task/{taskId}",
    new ParameterizedTypeReference<Result<Map>>() {},
    taskId
);

// 完成任务
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_JSON);
HttpEntity<Map<String, Object>> entity = new HttpEntity<>(variables, headers);

restTemplate.postForObject(
    "http://localhost:8080/system/workflow/task/{taskId}/complete",
    entity,
    Result.class,
    taskId
);
```

---

## 注意事项

1. **任务权限**: 用户只能操作分配给自己的任务
2. **转办 vs 委派**:
   - 转办：任务所有权转移，原处理人不再处理
   - 委派：任务临时委托给他人，原处理人仍可处理
3. **流程变量**: 完成任务时可以传递流程变量，用于后续流程判断
4. **任务历史**: 已完成的任务可以在历史记录中查询

---

更新时间：2026-03-11
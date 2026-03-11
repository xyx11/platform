# 流程实例监控实现文档

## 概述

本次实现为系统添加了完整的流程实例监控功能，包括后端 API 接口和前端监控页面。

## 功能特性

### 1. 统计看板
- 运行中流程实例数量
- 已挂起流程实例数量
- 历史完成流程数量
- 流程定义数量统计

### 2. 流程实例列表
- 显示所有运行中的流程实例
- 支持按流程定义 Key 筛选
- 支持按业务 Key 筛选
- 显示实例状态（运行中/已挂起）

### 3. 实例详情
- 流程实例基本信息
- 当前活动节点
- 启动人、启动时间等

### 4. 历史轨迹
- 时间轴展示流程活动历史
- 显示活动类型、处理人
- 显示活动耗时

### 5. 实例控制
- 挂起流程实例
- 激活流程实例

---

## 后端实现

### 接口定义 (WorkflowService.java)

新增 4 个方法：

```java
/**
 * 获取运行中的流程实例列表
 */
List<Map<String, Object>> getRunningProcessInstances(
    String processDefinitionKey,
    String businessKey
);

/**
 * 获取流程实例详情
 */
Map<String, Object> getProcessInstance(String processInstanceId);

/**
 * 获取流程历史活动轨迹
 */
List<Map<String, Object>> getProcessInstanceHistory(String processInstanceId);

/**
 * 获取流程实例统计信息
 */
Map<String, Object> getProcessInstanceStats();
```

### 服务实现 (WorkflowServiceImpl.java)

#### 1. 获取运行中流程实例列表

```java
@Override
public List<Map<String, Object>> getRunningProcessInstances(
    String processDefinitionKey, String businessKey) {

    var query = runtimeService.createProcessInstanceQuery();

    if (processDefinitionKey != null && !processDefinitionKey.isEmpty()) {
        query.processDefinitionKey(processDefinitionKey);
    }
    if (businessKey != null && !businessKey.isEmpty()) {
        query.processInstanceBusinessKey(businessKey);
    }

    List<ProcessInstance> instances = query.orderByProcessInstanceId().desc().list();

    return instances.stream().map(instance -> {
        Map<String, Object> map = new HashMap<>();
        map.put("processInstanceId", instance.getProcessInstanceId());
        map.put("processDefinitionId", instance.getProcessDefinitionId());
        map.put("processDefinitionKey", instance.getProcessDefinitionKey());
        map.put("businessKey", instance.getBusinessKey());
        map.put("isSuspended", instance.isSuspended());
        map.put("startTime", instance.getStartTime());
        map.put("startUserId", instance.getStartUserId());
        return map;
    }).collect(Collectors.toList());
}
```

#### 2. 获取流程实例详情

```java
@Override
public Map<String, Object> getProcessInstance(String processInstanceId) {
    ProcessInstance instance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();

    if (instance == null) {
        return null;
    }

    Map<String, Object> result = new HashMap<>();
    result.put("processInstanceId", instance.getProcessInstanceId());
    result.put("processDefinitionId", instance.getProcessDefinitionId());
    result.put("processDefinitionKey", instance.getProcessDefinitionKey());
    result.put("businessKey", instance.getBusinessKey());
    result.put("isSuspended", instance.isSuspended());
    result.put("startTime", instance.getStartTime());
    result.put("startUserId", instance.getStartUserId());

    // 获取当前活动
    var activities = runtimeService.getActiveActivityIds(processInstanceId);
    result.put("activeActivities", activities);

    return result;
}
```

#### 3. 获取流程历史轨迹

```java
@Override
public List<Map<String, Object>> getProcessInstanceHistory(String processInstanceId) {
    List<HistoricActivityInstance> history = historyService
        .createHistoricActivityInstanceQuery()
        .processInstanceId(processInstanceId)
        .orderByHistoricActivityInstanceStartTime()
        .asc()
        .list();

    return history.stream().map(activity -> {
        Map<String, Object> map = new HashMap<>();
        map.put("activityId", activity.getActivityId());
        map.put("activityName", activity.getActivityName());
        map.put("activityType", activity.getActivityType());
        map.put("startTime", activity.getStartTime());
        map.put("endTime", activity.getEndTime());
        map.put("duration", activity.getDurationInMillis());
        map.put("assignee", activity.getAssignee());
        return map;
    }).collect(Collectors.toList());
}
```

#### 4. 获取统计信息

```java
@Override
public Map<String, Object> getProcessInstanceStats() {
    Map<String, Object> stats = new HashMap<>();

    // 运行中的流程实例统计
    long runningCount = runtimeService.createProcessInstanceQuery().count();
    long suspendedCount = runtimeService.createProcessInstanceQuery().suspended().count();

    stats.put("runningCount", runningCount);
    stats.put("suspendedCount", suspendedCount);

    // 按流程定义分组统计
    var definitions = repositoryService.createProcessDefinitionQuery().list();
    List<Map<String, Object>> definitionStats = new ArrayList<>();

    for (var definition : definitions) {
        long count = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(definition.getKey())
                .count();

        Map<String, Object> defStat = new HashMap<>();
        defStat.put("processDefinitionKey", definition.getKey());
        defStat.put("processDefinitionName", definition.getName());
        defStat.put("count", count);
        definitionStats.add(defStat);
    }

    stats.put("definitionStats", definitionStats);

    // 历史流程实例统计
    long historicCount = historyService.createHistoricProcessInstanceQuery().count();
    stats.put("historicCount", historicCount);

    return stats;
}
```

---

## REST API 接口

### 1. 获取运行中流程实例列表

```http
GET /system/workflow/instance/list
Authorization: Bearer {token}

Query Parameters:
  - processDefinitionKey (可选): 流程定义 Key
  - businessKey (可选): 业务 Key

Response:
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "processInstanceId": "35250e0b-253c-11ef-9345-02420a0f531b",
      "processDefinitionKey": "leave-approval",
      "businessKey": "LEAVE-20240001",
      "isSuspended": false,
      "startTime": "2024-05-20T10:30:00.000+08:00",
      "startUserId": "1"
    }
  ]
}
```

### 2. 获取流程实例详情

```http
GET /system/workflow/instance/{processInstanceId}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "processInstanceId": "35250e0b-253c-11ef-9345-02420a0f531b",
    "processDefinitionId": "leave-approval:2:105",
    "processDefinitionKey": "leave-approval",
    "businessKey": "LEAVE-20240001",
    "isSuspended": false,
    "startTime": "2024-05-20T10:30:00.000+08:00",
    "startUserId": "1",
    "activeActivities": ["hr_approval"]
  }
}
```

### 3. 获取流程历史轨迹

```http
GET /system/workflow/instance/{processInstanceId}/history
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "activityId": "start",
      "activityName": "开始",
      "activityType": "startEvent",
      "startTime": "2024-05-20T10:30:00.000+08:00",
      "endTime": "2024-05-20T10:30:01.000+08:00",
      "duration": 1000
    },
    {
      "activityId": "dept_approval",
      "activityName": "部门审批",
      "activityType": "userTask",
      "startTime": "2024-05-20T10:30:01.000+08:00",
      "endTime": "2024-05-20T11:00:00.000+08:00",
      "duration": 1800000,
      "assignee": "1001"
    }
  ]
}
```

### 4. 获取统计信息

```http
GET /system/workflow/stats
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "runningCount": 15,
    "suspendedCount": 2,
    "historicCount": 128,
    "definitionStats": [
      {
        "processDefinitionKey": "leave-approval",
        "processDefinitionName": "请假审批流程",
        "count": 10
      },
      {
        "processDefinitionKey": "expense-claim",
        "processDefinitionName": "费用报销流程",
        "count": 5
      }
    ]
  }
}
```

---

## 前端实现

### 页面结构

```
process-instance/index.vue
├── 统计卡片区
│   ├── 运行中
│   ├── 已挂起
│   ├── 历史完成
│   └── 流程定义数
├── 流程实例列表
│   ├── 搜索栏 (流程定义 Key、业务 Key)
│   └── 表格
│       ├── 实例 ID
│       ├── 流程定义 Key
│       ├── 流程名称
│       ├── 业务 Key
│       ├── 状态
│       ├── 开始时间
│       ├── 启动人
│       └── 操作 (详情、轨迹、挂起/激活)
├── 详情对话框
└── 历史轨迹对话框
```

### 核心功能

#### 1. 统计数据获取

```javascript
const fetchStats = async () => {
  try {
    const { data } = await request.get('/system/workflow/stats')
    stats.value = data || {}
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}
```

#### 2. 实例列表获取

```javascript
const fetchInstanceList = async () => {
  loading.value = true
  try {
    const { data } = await request.get('/system/workflow/instance/list', {
      params: searchForm
    })
    instanceList.value = data || []
  } catch (error) {
    console.error('获取实例列表失败:', error)
    ElMessage.error('获取流程实例列表失败')
  } finally {
    loading.value = false
  }
}
```

#### 3. 挂起/激活操作

```javascript
const suspendOrActivate = async (row) => {
  const action = row.isSuspended ? '激活' : '挂起'
  const url = row.isSuspended ? '/activate' : '/suspend'

  try {
    await ElMessageBox.confirm(`确定要${action}该流程实例吗？`, '提示', {
      type: 'warning'
    })

    await request.post(`/system/workflow/${url}/${row.processInstanceId}`)
    ElMessage.success(`${action}成功`)
    fetchInstanceList()
    fetchStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${action}失败:`, error)
      ElMessage.error(`${action}失败`)
    }
  }
}
```

#### 4. 历史轨迹展示

```javascript
const viewHistory = async (row) => {
  try {
    const { data } = await request.get(
      `/system/workflow/instance/${row.processInstanceId}/history`
    )
    historyList.value = data || []
    historyVisible.value = true
  } catch (error) {
    console.error('获取历史轨迹失败:', error)
    ElMessage.error('获取历史轨迹失败')
  }
}
```

---

## 权限配置

需要在系统中配置以下权限：

| 权限标识 | 说明 |
|---------|------|
| system:workflow:query | 流程查询权限 |
| system:workflow:suspend | 挂起流程权限 |
| system:workflow:activate | 激活流程权限 |

---

## 菜单配置

```sql
-- 流程实例监控菜单
INSERT INTO sys_menu (parent_id, name, path, component, perms, type, icon, sort)
VALUES (
  (SELECT menu_id FROM sys_menu WHERE path = '/system' LIMIT 1),
  '流程实例监控',
  '/system/process-instance',
  'system/process-instance/index.vue',
  'system:workflow:query',
  1,
  'Monitor',
  16
);
```

---

## 使用场景

1. **管理员监控**: 实时查看系统中所有运行中的流程实例
2. **流程跟踪**: 查看特定流程的当前状态和历史轨迹
3. **异常处理**: 对异常流程进行挂起/激活操作
4. **统计分析**: 按流程定义统计实例数量

---

## 性能优化建议

1. **列表分页**: 当实例数量较多时，添加分页功能
2. **缓存统计**: 统计数据可添加短时缓存（如 5 秒）
3. **异步加载**: 详情和历史轨迹使用懒加载
4. **索引优化**: 确保 Flowable 相关表的索引完整

---

## 后续优化方向

1. 流程实例高级搜索（按时间范围、状态等）
2. 流程图可视化展示当前节点
3. 流程实例批量操作
4. 流程性能分析（平均处理时间等）
5. 流程实例导出功能

---

完成时间：2026-03-11
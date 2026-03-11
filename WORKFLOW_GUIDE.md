# Flowable 工作流引擎集成指南

## 1. 依赖配置

已在 `pom.xml` 中添加 Flowable 依赖：

```xml
<dependency>
    <groupId>org.flowable</groupId>
    <artifactId>flowable-spring-boot-starter</artifactId>
    <version>7.0.0</version>
</dependency>
```

## 2. 数据库表初始化

Flowable 会自动创建所需的数据库表，表前缀为 `ACT_`：

- `ACT_RE_*`: 资源库服务相关（流程定义、流程部署）
- `ACT_RU_*`: 运行时服务相关（运行中的流程、任务）
- `ACT_HI_*`: 历史服务相关（已完成的流程、任务）
- `ACT_ID_*`: 身份服务相关（用户、组）
- `ACT_GE_*`: 通用服务相关

## 3. 配置文件

在 `application.yml` 中添加 Flowable 配置：

```yaml
flowable:
  async-history-enabled: true
  db-history-used: true
  history-level: FULL
  database-schema-update: true
  check-process-definitions: false
```

## 4. 使用示例

### 4.1 启动流程

```java
Map<String, Object> variables = new HashMap<>();
variables.put("applicant", "张三");
variables.put("days", 5);

ProcessInstance instance = workflowService.startProcess(
    "leaveProcess",  // 流程定义 Key
    "leave-001",     // 业务 Key
    variables
);
```

### 4.2 获取待办任务

```java
List<Task> tasks = workflowService.getTodoTasks(userId);
```

### 4.3 完成任务

```java
Map<String, Object> variables = new HashMap<>();
variables.put("approved", true);
workflowService.completeTask(taskId, variables);
```

## 5. BPMN 流程定义示例

创建 `src/main/resources/processes/leave-process.bpmn20.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/examples">

    <process id="leaveProcess" name="请假流程">
        <startEvent id="start" name="开始"/>

        <userTask id="apply" name="提交申请" flowable:assignee="${applicant}"/>

        <userTask id="managerApprove" name="经理审批" flowable:assignee="${manager}"/>

        <userTask id="hrApprove" name="HR 审批" flowable:assignee="${hr}"/>

        <endEvent id="end" name="结束"/>

        <sequenceFlow id="flow1" sourceRef="start" targetRef="apply"/>
        <sequenceFlow id="flow2" sourceRef="apply" targetRef="managerApprove"/>
        <sequenceFlow id="flow3" sourceRef="managerApprove" targetRef="hrApprove"/>
        <sequenceFlow id="flow4" sourceRef="hrApprove" targetRef="end"/>
    </process>
</definitions>
```

## 6. API 接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/system/workflow/start/{processDefinitionKey}` | POST | 启动流程 |
| `/system/workflow/todo` | GET | 获取待办任务 |
| `/system/workflow/complete/{taskId}` | POST | 完成任务 |
| `/system/workflow/variables/{processInstanceId}` | GET | 获取流程变量 |
| `/system/workflow/{processInstanceId}` | DELETE | 删除流程 |
| `/system/workflow/suspend/{processInstanceId}` | POST | 挂起流程 |
| `/system/workflow/activate/{processInstanceId}` | POST | 激活流程 |

## 7. 注意事项

1. Flowable 会在应用启动时自动检查并创建数据库表
2. 生产环境建议将 `database-schema-update` 设置为 `false`，手动执行建表脚本
3. 流程定义文件放在 `src/main/resources/processes/` 目录下
4. 建议将流程定义与动态表单设计器结合使用
# 任务管理

## 我的待办

查看当前用户需要处理的任务。

### 查询条件

- 任务名称
- 流程名称
- 创建时间范围
- 优先级

### 任务操作

- **办理**：打开任务表单，填写审批意见
- **查看**：查看任务详情，不可编辑
- **转办**：将任务转交给其他人
- **委派**：将任务委派给其他人处理
- **退回**：退回到上一个节点
- **挂起**：暂停任务处理

## 我的已办

查看当前用户已经处理完成的任务。

## 任务查询 API

```
GET /system/workflow/task/todo
GET /system/workflow/task/done
GET /system/workflow/task/list
```

## 任务办理 API

```
POST /system/workflow/task/complete
POST /system/workflow/task/transfer
POST /system/workflow/task/delegate
POST /system/workflow/task/return
```
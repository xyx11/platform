# 表单配置

## 表单类型

### 启动表单

用于流程发起时填写数据，通常在流程开始时收集申请人信息。

### 办理表单

用于任务处理时填写数据，通常在审批环节收集审批意见。

## 配置方式

### 1. 在流程设计器中配置

1. 打开流程设计器
2. 点击「关联表单」按钮
3. 选择启动表单
4. 为每个用户任务选择办理表单
5. 保存配置

### 2. 在节点属性中配置

1. 选中用户任务节点
2. 点击「节点属性」按钮
3. 在「办理表单」下拉框中选择表单
4. 保存

## 表单绑定关系

```
workflow_form_binding 表结构：

| 字段名                  | 类型        | 说明                      |
|------------------------|------------|--------------------------|
| binding_id             | bigint     | 主键 ID                   |
| process_definition_key | varchar(64)| 流程定义 Key              |
| process_definition_name| varchar(255)| 流程定义名称             |
| task_definition_key    | varchar(64)| 任务定义 Key（启动表单为空）|
| task_name              | varchar(255)| 任务名称                 |
| form_key               | varchar(64)| 表单 Key                  |
| form_name              | varchar(255)| 表单名称                 |
| form_type              | tinyint    | 表单类型：1-启动表单，2-办理表单 |
| status                 | tinyint    | 状态：0-停用，1-启用        |
| remark                 | varchar(500)| 备注                     |
| form_schema            | text       | 表单 Schema（JSON）        |
| form_config            | text       | 表单配置（JSON）           |
```

## API 接口

详见 [工作流表单 API](/api/workflow-form)

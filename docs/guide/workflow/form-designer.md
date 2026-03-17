# 表单设计器

## 功能说明

表单设计器提供可视化的表单设计功能，支持拖拽式组件配置，可快速创建流程表单。

## 界面布局

```
┌─────────────────────────────────────────────────────────────┐
│  表单设计器                                   [保存] [预览]  │
├─────────────┬─────────────────────────────────┬─────────────┤
│  组件列表    │         画布区域                  │  属性配置    │
│             │                                 │             │
│  □ 输入框    │    ┌─────────────────┐          │  字段名      │
│  □ 文本域    │    │   表单标题       │          │  字段标签    │
│  □ 单选框    │    │                 │          │  默认值      │
│  □ 多选框    │    │   [输入框]       │          │  校验规则    │
│  □ 下拉框    │    │   [文本域]       │          │  显示隐藏    │
│  □ 日期框    │    │   [单选框]       │          │  只读禁用    │
│  □ 数字框    │    │                 │          │             │
│  □ 图片上传  │    └─────────────────┘          │             │
│  □ 文件上传  │                                 │             │
│  □ 表格      │                                 │             │
│  □ 卡片      │                                 │             │
└─────────────┴─────────────────────────────────┴─────────────┘
```

## 支持的组件

### 基础组件

| 组件 | 说明 | 配置项 |
|------|------|--------|
| 输入框 | 单行文本输入 | 占位符、默认值、最大长度、校验规则 |
| 文本域 | 多行文本输入 | 占位符、默认值、行数、最大长度 |
| 单选框 | 单项选择 | 选项列表、默认值、排列方向 |
| 多选框 | 多项选择 | 选项列表、默认值、最大选择数 |
| 下拉框 | 下拉选择 | 选项列表、默认值、是否可搜索 |
| 日期框 | 日期选择 | 日期格式、日期范围、默认值 |
| 数字框 | 数字输入 | 最小值、最大值、精度、步长 |

### 高级组件

| 组件 | 说明 | 配置项 |
|------|------|--------|
| 图片上传 | 图片上传 | 最大数量、大小限制、格式限制 |
| 文件上传 | 文件上传 | 最大数量、大小限制、格式限制 |
| 表格 | 动态表格 | 列配置、行数限制、操作列 |
| 卡片 | 内容分组 | 标题、边框、背景色 |

## 属性配置

### 通用属性

```json
{
  "label": "字段标签",
  "prop": "字段名",
  "type": "组件类型",
  "required": true,
  "hidden": false,
  "disabled": false,
  "readOnly": false,
  "placeholder": "请输入...",
  "defaultValue": "",
  "helpText": "提示信息"
}
```

### 校验规则

```json
{
  "rules": [
    {
      "required": true,
      "message": "此项为必填项",
      "trigger": "blur"
    },
    {
      "min": 2,
      "max": 20,
      "message": "长度在 2 到 20 个字符",
      "trigger": "blur"
    },
    {
      "pattern": "^[0-9]*$",
      "message": "只能输入数字",
      "trigger": "blur"
    }
  ]
}
```

## API 接口

### 1. 保存表单

```http
POST /system/form-definition
```

**请求体：**

```json
{
  "formName": "请假申请表",
  "formCode": "leave_apply_form",
  "formType": 1,
  "formSchema": {
    "fields": [
      {
        "label": "申请人",
        "prop": "applicant",
        "type": "input",
        "required": true
      },
      {
        "label": "请假类型",
        "prop": "leaveType",
        "type": "select",
        "options": [
          {"label": "事假", "value": "personal"},
          {"label": "病假", "value": "sick"}
        ]
      }
    ]
  },
  "formConfig": {},
  "status": 1
}
```

### 2. 获取表单详情

```http
GET /system/form-definition/{formId}
```

### 3. 预览表单

```http
GET /system/form-definition/{formId}/preview
```

## 表单 Schema 格式

```json
{
  "fields": [
    {
      "id": "field_1",
      "label": "姓名",
      "prop": "name",
      "type": "input",
      "required": true,
      "placeholder": "请输入姓名",
      "rules": [
        {"required": true, "message": "姓名为必填", "trigger": "blur"}
      ]
    },
    {
      "id": "field_2",
      "label": "性别",
      "prop": "sex",
      "type": "radio",
      "defaultValue": "1",
      "options": [
        {"label": "男", "value": "1"},
        {"label": "女", "value": "2"}
      ]
    }
  ],
  "config": {
    "labelWidth": 100,
    "size": "default",
    "labelPosition": "right"
  }
}
```
# 表单定义 API

**基础路径：** `/system/form-definition`

## 1. 获取表单定义列表

```http
GET /system/form-definition/list
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| formName | String | 否 | 表单名称 |
| formCode | String | 否 | 表单编码 |
| formType | Integer | 否 | 表单类型 |
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
        "formId": 1,
        "formName": "请假申请表",
        "formCode": "leave_apply_form",
        "formType": 1,
        "formSchema": {...},
        "formConfig": {...},
        "version": "1.0.0",
        "status": 1,
        "remark": "用于员工请假申请"
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

## 2. 获取表单定义详情

```http
GET /system/form-definition/{formId}
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "formId": 1,
    "formName": "请假申请表",
    "formCode": "leave_apply_form",
    "formSchema": {
      "fields": [
        {
          "label": "申请人",
          "prop": "applicant",
          "type": "input"
        },
        {
          "label": "请假类型",
          "prop": "leaveType",
          "type": "select",
          "options": [
            { "label": "事假", "value": "personal" },
            { "label": "病假", "value": "sick" }
          ]
        }
      ]
    },
    "formConfig": {...}
  }
}
```

---

## 3. 创建表单定义

```http
POST /system/form-definition
```

**请求体：**

```json
{
  "formName": "请假申请表",
  "formCode": "leave_apply_form",
  "formType": 1,
  "formSchema": {...},
  "formConfig": {...},
  "version": "1.0.0",
  "status": 1,
  "remark": "用于员工请假申请"
}
```

---

## 4. 更新表单定义

```http
PUT /system/form-definition
```

**请求体：** 同创建

---

## 5. 删除表单定义

```http
DELETE /system/form-definition/{formId}
```

---

## 6. 复制表单定义

```http
POST /system/form-definition/copy
```

**请求体：**

```json
{
  "formId": 1,
  "newFormCode": "leave_apply_form_v2",
  "newFormName": "请假申请表 v2"
}
```

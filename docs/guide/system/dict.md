# 字典管理

## 功能说明

字典管理模块用于管理系统中的静态数据，如性别、状态、类型等固定选项。

## 主要功能

- 字典类型列表查询
- 字典数据列表查询
- 新增字典类型/数据
- 编辑字典类型/数据
- 删除字典类型/数据
- 刷新字典缓存

## 字典类型 API

### 1. 获取字典类型列表

```http
GET /system/dict/type/list
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dictName | String | 否 | 字典名称 |
| dictType | String | 否 | 字典类型 |
| status | String | 否 | 状态 |
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
        "dictId": 1,
        "dictName": "用户性别",
        "dictType": "sys_user_sex",
        "status": "0",
        "remark": "用户性别列表",
        "createTime": "2024-01-01 12:00:00"
      }
    ],
    "total": 1
  }
}
```

### 2. 获取字典类型详情

```http
GET /system/dict/type/{dictId}
```

### 3. 新增字典类型

```http
POST /system/dict/type
```

**请求体：**

```json
{
  "dictName": "系统开关",
  "dictType": "sys_common_status",
  "status": "0",
  "remark": "系统通用开关"
}
```

### 4. 修改字典类型

```http
PUT /system/dict/type
```

### 5. 删除字典类型

```http
DELETE /system/dict/type/{dictIds}
```

### 6. 刷新字典缓存

```http
DELETE /system/dict/type/refreshCache
```

## 字典数据 API

### 1. 获取字典数据列表

```http
GET /system/dict/data/list
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dictType | String | 否 | 字典类型 |
| dictLabel | String | 否 | 字典标签 |
| status | String | 否 | 状态 |

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "dictCode": 1,
      "dictSort": 1,
      "dictLabel": "男",
      "dictValue": "1",
      "dictType": "sys_user_sex",
      "cssClass": "",
      "listClass": "",
      "isDefault": "Y",
      "status": "0",
      "remark": "性别男"
    },
    {
      "dictCode": 2,
      "dictSort": 2,
      "dictLabel": "女",
      "dictValue": "2",
      "dictType": "sys_user_sex",
      "cssClass": "",
      "listClass": "",
      "isDefault": "N",
      "status": "0",
      "remark": "性别女"
    }
  ]
}
```

### 2. 获取字典数据详情

```http
GET /system/dict/data/{dictCode}
```

### 3. 新增字典数据

```http
POST /system/dict/data
```

**请求体：**

```json
{
  "dictSort": 1,
  "dictLabel": "正常",
  "dictValue": "0",
  "dictType": "sys_normal_disable",
  "status": "0",
  "remark": "正常状态"
}
```

### 4. 修改字典数据

```http
PUT /system/dict/data
```

### 5. 删除字典数据

```http
DELETE /system/dict/data/{dictCodes}
```

## 系统内置字典

| 字典类型 | 字典名称 | 说明 |
|---------|---------|------|
| sys_user_sex | 用户性别 | 男/女/未知 |
| sys_normal_disable | 系统开关 | 正常/停用 |
| sys_status | 状态 | 正常/异常 |
| sys_yes_no | 是否 | 是/否 |

## 数据表结构

```sql
-- 字典类型表
CREATE TABLE sys_dict_type (
  dict_id bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  dict_name varchar(100) DEFAULT '' COMMENT '字典名称',
  dict_type varchar(100) DEFAULT '' COMMENT '字典类型',
  status char(1) DEFAULT '0' COMMENT '状态',
  create_by varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (dict_id),
  UNIQUE KEY uk_dict_type (dict_type)
);

-- 字典数据表
CREATE TABLE sys_dict_data (
  dict_code bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  dict_sort int DEFAULT '0' COMMENT '字典排序',
  dict_label varchar(100) DEFAULT '' COMMENT '字典标签',
  dict_value varchar(100) DEFAULT '' COMMENT '字典键值',
  dict_type varchar(100) DEFAULT '' COMMENT '字典类型',
  css_class varchar(100) DEFAULT NULL COMMENT '样式属性',
  list_class varchar(100) DEFAULT NULL COMMENT '表格回显样式',
  is_default char(1) DEFAULT 'N' COMMENT '是否默认',
  status char(1) DEFAULT '0' COMMENT '状态',
  create_by varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (dict_code)
);
```
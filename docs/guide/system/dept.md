# 部门管理

## 功能说明

部门管理模块用于管理系统的组织架构，支持树形结构的部门管理。

## 主要功能

- 部门列表查询（树形结构）
- 新增部门
- 编辑部门
- 删除部门
- 部门排序

## API 接口

### 1. 获取部门列表

```http
GET /system/dept/list
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| deptId | Long | 否 | 部门 ID |
| parentId | Long | 否 | 父部门 ID |
| deptName | String | 否 | 部门名称 |
| status | String | 否 | 状态 |

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "deptId": 100,
      "deptName": "总公司",
      "parentId": 0,
      "ancestors": "0",
      "orderNum": 1,
      "leader": "张三",
      "phone": "13800138000",
      "email": "test@example.com",
      "status": "0",
      "children": [
        {
          "deptId": 101,
          "deptName": "深圳分公司",
          "parentId": 100,
          "ancestors": "0,100",
          "orderNum": 1,
          "leader": "李四",
          "status": "0"
        },
        {
          "deptId": 102,
          "deptName": "北京分公司",
          "parentId": 100,
          "ancestors": "0,100",
          "orderNum": 2,
          "leader": "王五",
          "status": "0"
        }
      ]
    }
  ]
}
```

### 2. 获取部门详情

```http
GET /system/dept/{deptId}
```

### 3. 新增部门

```http
POST /system/dept
```

**请求体：**

```json
{
  "parentId": 100,
  "deptName": "研发部",
  "orderNum": 1,
  "leader": "赵六",
  "phone": "13800138001",
  "email": "dev@example.com",
  "status": "0"
}
```

### 4. 修改部门

```http
PUT /system/dept
```

### 5. 删除部门

```http
DELETE /system/dept/{deptId}
```

### 6. 获取部门树

```http
GET /system/dept/treedata
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "depts": [
      {
        "id": 100,
        "label": "总公司",
        "children": [
          {
            "id": 101,
            "label": "深圳分公司"
          }
        ]
      }
    ]
  }
}
```

## 数据表结构

```sql
CREATE TABLE sys_dept (
  dept_id bigint NOT NULL AUTO_INCREMENT COMMENT '部门 ID',
  parent_id bigint DEFAULT '0' COMMENT '父部门 ID',
  ancestors varchar(500) DEFAULT '' COMMENT '祖级列表',
  dept_name varchar(30) DEFAULT '' COMMENT '部门名称',
  order_num int DEFAULT '0' COMMENT '显示顺序',
  leader varchar(50) DEFAULT NULL COMMENT '负责人',
  phone varchar(11) DEFAULT NULL COMMENT '联系电话',
  email varchar(50) DEFAULT NULL COMMENT '邮箱',
  status char(1) DEFAULT '0' COMMENT '部门状态',
  del_flag char(1) DEFAULT '0' COMMENT '删除标志',
  create_by varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (dept_id)
);
```
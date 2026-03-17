# 角色管理

## 功能说明

角色管理模块用于管理系统角色，包括角色的增删改查、权限分配、用户分配等功能。

## 主要功能

- 角色列表查询
- 新增角色
- 编辑角色
- 删除角色
- 分配权限（菜单权限）
- 分配用户
- 数据权限配置

## API 接口

### 1. 获取角色列表

```http
GET /system/role/list
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleName | String | 否 | 角色名称 |
| roleKey | String | 否 | 角色权限字符串 |
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
        "roleId": 1,
        "roleName": "超级管理员",
        "roleKey": "admin",
        "roleSort": 1,
        "dataScope": "1",
        "status": "0",
        "flag": false,
        "createBy": "admin",
        "createTime": "2024-01-01 12:00:00",
        "remark": "超级管理员拥有所有权限",
        "menuIds": [1, 2, 3, 4, 5],
        "menuNames": ["系统管理", "用户管理", "角色管理", "菜单管理", "部门管理"]
      }
    ],
    "total": 1
  }
}
```

### 2. 获取角色详情

```http
GET /system/role/{roleId}
```

### 3. 新增角色

```http
POST /system/role
```

**请求体：**

```json
{
  "roleName": "普通用户",
  "roleKey": "common",
  "roleSort": 2,
  "dataScope": "1",
  "status": "0",
  "remark": "普通用户角色",
  "menuIds": [1, 2]
}
```

### 4. 修改角色

```http
PUT /system/role
```

### 5. 删除角色

```http
DELETE /system/role/{roleIds}
```

### 6. 修改角色状态

```http
PUT /system/role/changeStatus
```

**请求体：**

```json
{
  "roleId": 1,
  "status": "0"
}
```

### 7. 分配用户

```http
PUT /system/role/authUser/selectAll
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleId | Long | 是 | 角色 ID |
| userIds | Long[] | 是 | 用户 ID 列表 |

## 数据权限

数据权限范围：

| 范围 | 值 | 说明 |
|------|-----|------|
| 全部数据权限 | 1 | 可查看系统所有数据 |
| 自定数据权限 | 2 | 可查看自定义的数据范围 |
| 本部门数据权限 | 3 | 可查看本部门数据 |
| 本部门及以下数据权限 | 4 | 可查看本部门及下级部门数据 |
| 仅本人数据权限 | 5 | 只能查看自己创建的数据 |

## 数据表结构

```sql
CREATE TABLE sys_role (
  role_id bigint NOT NULL AUTO_INCREMENT COMMENT '角色 ID',
  role_name varchar(30) NOT NULL COMMENT '角色名称',
  role_key varchar(100) NOT NULL COMMENT '角色权限字符串',
  role_sort int NOT NULL COMMENT '显示顺序',
  data_scope char(1) DEFAULT '1' COMMENT '数据范围',
  menu_check_strictly tinyint(1) DEFAULT '1' COMMENT '菜单树选择项是否关联显示',
  dept_check_strictly tinyint(1) DEFAULT '1' COMMENT '部门树选择项是否关联显示',
  status char(1) NOT NULL COMMENT '角色状态',
  del_flag char(1) DEFAULT '0' COMMENT '删除标志',
  create_by varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (role_id)
);

-- 角色与菜单关联表
CREATE TABLE sys_role_menu (
  role_id bigint NOT NULL COMMENT '角色 ID',
  menu_id bigint NOT NULL COMMENT '菜单 ID',
  PRIMARY KEY (role_id, menu_id)
);

-- 角色与部门关联表（数据权限）
CREATE TABLE sys_role_dept (
  role_id bigint NOT NULL COMMENT '角色 ID',
  dept_id bigint NOT NULL COMMENT '部门 ID',
  PRIMARY KEY (role_id, dept_id)
);
```
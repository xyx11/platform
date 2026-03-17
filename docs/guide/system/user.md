# 用户管理

## 功能说明

用户管理模块用于管理系统用户，包括用户的增删改查、角色分配、部门分配等功能。

## 界面展示

- 用户列表
- 新增用户
- 编辑用户
- 分配角色
- 重置密码

## API 接口

### 1. 获取用户列表

```http
GET /system/user/list
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userName | String | 否 | 用户名称 |
| phonenumber | String | 否 | 手机号码 |
| status | String | 否 | 状态 |
| deptId | Long | 否 | 部门 ID |
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
        "userId": 1,
        "userName": "admin",
        "nickName": "管理员",
        "email": "admin@example.com",
        "phonenumber": "13800138000",
        "sex": "1",
        "avatar": "",
        "status": "0",
        "delFlag": "0",
        "loginIp": "127.0.0.1",
        "loginDate": "2024-01-01 12:00:00",
        "dept": {
          "deptId": 100,
          "deptName": "总公司"
        },
        "roles": [
          {
            "roleId": 1,
            "roleName": "超级管理员"
          }
        ]
      }
    ],
    "total": 1
  }
}
```

### 2. 获取用户详情

```http
GET /system/user/{userId}
```

### 3. 新增用户

```http
POST /system/user
```

**请求体：**

```json
{
  "userName": "test",
  "nickName": "测试用户",
  "password": "admin123",
  "email": "test@example.com",
  "phonenumber": "13800138001",
  "sex": "1",
  "status": "0",
  "deptId": 100,
  "roleIds": [2]
}
```

### 4. 修改用户

```http
PUT /system/user
```

### 5. 删除用户

```http
DELETE /system/user/{userIds}
```

### 6. 重置密码

```http
PUT /system/user/resetPwd
```

**请求体：**

```json
{
  "userId": 1,
  "password": "admin123"
}
```

### 7. 分配角色

```http
PUT /system/user/authRole
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户 ID |
| roleIds | Long[] | 是 | 角色 ID 列表 |

## 数据表结构

```sql
CREATE TABLE sys_user (
  user_id bigint NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
  dept_id bigint DEFAULT NULL COMMENT '部门 ID',
  user_name varchar(30) NOT NULL COMMENT '用户账号',
  nick_name varchar(30) NOT NULL COMMENT '用户昵称',
  user_type varchar(2) DEFAULT '00' COMMENT '用户类型',
  email varchar(50) DEFAULT '' COMMENT '用户邮箱',
  phonenumber varchar(11) DEFAULT '' COMMENT '手机号码',
  sex char(1) DEFAULT '0' COMMENT '用户性别',
  avatar varchar(100) DEFAULT '' COMMENT '头像地址',
  password varchar(100) DEFAULT '' COMMENT '密码',
  status char(1) DEFAULT '0' COMMENT '帐号状态',
  del_flag char(1) DEFAULT '0' COMMENT '删除标志',
  login_ip varchar(128) DEFAULT '' COMMENT '最后登录 IP',
  login_date datetime DEFAULT NULL COMMENT '最后登录时间',
  create_by varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (user_id)
);
```
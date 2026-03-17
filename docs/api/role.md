# 角色管理 API

**基础路径：** `/system/role`

## 1. 获取角色列表

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
        "remark": "超级管理员拥有所有权限"
      }
    ],
    "total": 1
  }
}
```

---

## 2. 获取角色详情

```http
GET /system/role/{roleId}
```

---

## 3. 新增角色

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

---

## 4. 修改角色

```http
PUT /system/role
```

**请求体：**

```json
{
  "roleId": 2,
  "roleName": "普通用户",
  "roleKey": "common",
  "roleSort": 2,
  "dataScope": "1",
  "status": "0",
  "remark": "普通用户角色",
  "menuIds": [1, 2]
}
```

---

## 5. 删除角色

```http
DELETE /system/role/{roleIds}
```

---

## 6. 修改角色状态

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

---

## 7. 分配数据权限

```http
PUT /system/role/dataScope
```

**请求体：**

```json
{
  "roleId": 1,
  "dataScope": "2",
  "deptIds": [100, 101, 102]
}
```

---

## 8. 获取已分配用户列表

```http
GET /system/role/authUser/allocatedList
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleId | Long | 是 | 角色 ID |
| userName | String | 否 | 用户名称 |
| phonenumber | String | 否 | 手机号码 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

---

## 9. 获取未分配用户列表

```http
GET /system/role/authUser/unallocatedList
```

---

## 10. 选择用户授权

```http
PUT /system/role/authUser/selectAll
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleId | Long | 是 | 角色 ID |
| userIds | Long[] | 是 | 用户 ID 列表 |

---

## 11. 取消用户授权

```http
PUT /system/role/authUser/cancel
```

**请求体：**

```json
{
  "roleId": 1,
  "userId": 2
}
```

---

## 12. 批量取消用户授权

```http
PUT /system/role/authUser/cancelAll
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleId | Long | 是 | 角色 ID |
| userIds | Long[] | 是 | 用户 ID 列表 |
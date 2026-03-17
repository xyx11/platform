# 用户管理 API

**基础路径：** `/system/user`

## 1. 获取用户列表

```http
GET /system/user/list
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userName | String | 否 | 用户名称 |
| nickName | String | 否 | 用户昵称 |
| phonenumber | String | 否 | 手机号码 |
| status | String | 否 | 状态 (0-正常 1-停用) |
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

---

## 2. 获取用户详情

```http
GET /system/user/{userId}
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "userId": 1,
    "userName": "admin",
    "nickName": "管理员",
    "email": "admin@example.com",
    "phonenumber": "13800138000",
    "sex": "1",
    "avatar": "",
    "status": "0",
    "deptId": 100,
    "roleIds": [1],
    "postIds": [1]
  }
}
```

---

## 3. 新增用户

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
  "postIds": [1],
  "roleIds": [2],
  "remark": "备注信息"
}
```

---

## 4. 修改用户

```http
PUT /system/user
```

**请求体：**

```json
{
  "userId": 2,
  "userName": "test",
  "nickName": "测试用户",
  "email": "test@example.com",
  "phonenumber": "13800138001",
  "sex": "1",
  "status": "0",
  "deptId": 100,
  "postIds": [1],
  "roleIds": [2],
  "remark": "备注信息"
}
```

---

## 5. 删除用户

```http
DELETE /system/user/{userIds}
```

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userIds | Long[] | 是 | 用户 ID 列表（逗号分隔） |

---

## 6. 重置密码

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

---

## 7. 分配角色

```http
PUT /system/user/authRole
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户 ID |
| roleIds | Long[] | 是 | 角色 ID 列表 |

---

## 8. 获取用户角色

```http
GET /system/user/authRole/{userId}
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "roles": [
      {"roleId": 1, "roleName": "超级管理员", "flag": true},
      {"roleId": 2, "roleName": "普通用户", "flag": false}
    ]
  }
}
```

---

## 9. 获取部门用户树

```http
GET /system/user/deptTree
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 100,
      "label": "总公司",
      "children": [
        {
          "id": 1,
          "label": "admin (管理员)",
          "type": "user"
        },
        {
          "id": 2,
          "label": "test (测试用户)",
          "type": "user"
        }
      ]
    }
  ]
}
```

---

## 10. 下载用户导入模板

```http
GET /system/user/importTemplate
```

**响应：** Excel 文件

---

## 11. 导入用户

```http
POST /system/user/import
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | Excel 文件 |
| updateSupport | Boolean | 否 | 是否更新已存在用户 |

---

## 12. 导出用户

```http
POST /system/user/export
```

**请求体：** 同列表查询参数

**响应：** Excel 文件

# API 接口速查

## 认证相关

### 登录
```
POST /auth/login
Body: { username, password, code, uuid }
```

### 登出
```
POST /auth/logout
Header: Authorization: Bearer <token>
```

### 获取验证码
```
GET /auth/captcha
```

### 刷新 Token
```
POST /auth/refresh
Header: Authorization: Bearer <token>
```

---

## 用户管理

### 用户列表
```
GET /system/user/list
Query: userName, phonenumber, status, deptId, pageNum, pageSize
```

### 用户详情
```
GET /system/user/{userId}
```

### 新增用户
```
POST /system/user
Body: { userName, nickName, password, deptId, roleIds, ... }
```

### 修改用户
```
PUT /system/user
Body: { userId, userName, nickName, deptId, roleIds, ... }
```

### 删除用户
```
DELETE /system/user/{userIds}
```

### 重置密码
```
PUT /system/user/resetPwd
Body: { userId, password }
```

### 分配角色
```
PUT /system/user/authRole
Query: userId, roleIds
```

---

## 角色管理

### 角色列表
```
GET /system/role/list
Query: roleName, roleKey, status, pageNum, pageSize
```

### 角色详情
```
GET /system/role/{roleId}
```

### 新增角色
```
POST /system/role
Body: { roleName, roleKey, roleSort, menuIds, ... }
```

### 修改角色
```
PUT /system/role
Body: { roleId, roleName, roleKey, menuIds, ... }
```

### 删除角色
```
DELETE /system/role/{roleIds}
```

### 数据权限
```
PUT /system/role/dataScope
Body: { roleId, dataScope, deptIds }
```

---

## 菜单管理

### 菜单列表
```
GET /system/menu/list
```

### 菜单详情
```
GET /system/menu/{menuId}
```

### 新增菜单
```
POST /system/menu
Body: { menuName, parentId, orderNum, menuType, path, component, ... }
```

### 修改菜单
```
PUT /system/menu
Body: { menuId, menuName, parentId, orderNum, ... }
```

### 删除菜单
```
DELETE /system/menu/{menuId}
```

---

## 部门管理

### 部门列表
```
GET /system/dept/list
Query: deptId, parentId, deptName, status
```

### 部门树
```
GET /system/dept/treedata
```

### 新增部门
```
POST /system/dept
Body: { parentId, deptName, orderNum, leader, ... }
```

### 修改部门
```
PUT /system/dept
Body: { deptId, parentId, deptName, orderNum, ... }
```

### 删除部门
```
DELETE /system/dept/{deptId}
```

---

## 字典管理

### 字典类型列表
```
GET /system/dict/type/list
Query: dictName, dictType, status
```

### 字典数据列表
```
GET /system/dict/data/list
Query: dictType, dictLabel, status
```

### 新增字典类型
```
POST /system/dict/type
Body: { dictName, dictType, status }
```

### 新增字典数据
```
POST /system/dict/data
Body: { dictType, dictLabel, dictValue, dictSort, ... }
```

---

## 工作流

### 流程定义列表
```
GET /system/workflow/process-definition/list
Query: processDefinitionKey, processDefinitionName
```

### 部署流程
```
POST /system/workflow/deploy
Body: { name, category, description, bpmnXml }
```

### 启动流程
```
POST /system/workflow/process-instance/start
Body: { processDefinitionId, businessKey, variables }
```

### 待办任务
```
GET /system/workflow/task/todo
Query: processDefinitionKey, taskName
```

### 办理任务
```
POST /system/workflow/task/complete
Body: { taskId, approved, comment, variables }
```

### 表单绑定
```
POST /system/workflow-form/bind
Body: { processDefinitionKey, taskDefinitionKey, formKey, ... }
```

---

## 通用响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```

### 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 不存在 |
| 500 | 服务器错误 |

---

## 请求头

所有需要认证的接口都需要添加：

```
Authorization: Bearer <token>
Content-Type: application/json
```

---

## 分页参数

所有列表接口支持：

```
pageNum=1    # 页码，默认 1
pageSize=10  # 每页数量，默认 10
```

---

## 排序参数

部分列表接口支持：

```
orderByField=id      # 排序字段
isAsc=asc            # asc/desc
```
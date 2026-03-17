# API 概览

## 基础信息

**基础路径：** `/system`

**认证方式：** Bearer Token

```
Authorization: Bearer <token>
```

## 响应格式

### 成功响应

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```

### 错误响应

```json
{
  "code": 401,
  "msg": "认证失败",
  "data": null
}
```

## API 分类

| 分类 | 路径前缀 | 说明 |
|------|---------|------|
| 用户管理 | /system/user | 用户 CRUD |
| 角色管理 | /system/role | 角色权限 |
| 菜单管理 | /system/menu | 菜单配置 |
| 部门管理 | /system/dept | 组织架构 |
| 岗位管理 | /system/post | 岗位配置 |
| 字典管理 | /system/dict | 数据字典 |
| 工作流 | /system/workflow | 流程管理 |
| 表单 | /system/form | 表单管理 |
| 任务 | /system/task | 任务管理 |
# 菜单管理

## 功能说明

菜单管理模块用于管理系统菜单和权限，支持树形结构展示，包括目录、菜单、按钮三种类型。

## 菜单类型

| 类型 | 说明 | 示例 |
|------|------|------|
| 目录 | 一级或二级菜单目录 | 系统管理、工作流管理 |
| 菜单 | 具体的功能页面 | 用户管理、角色管理 |
| 按钮 | 页面中的操作按钮 | 新增、编辑、删除 |

## 主要功能

- 菜单列表查询（树形结构）
- 新增菜单
- 编辑菜单
- 删除菜单
- 菜单排序

## API 接口

### 1. 获取菜单列表

```http
GET /system/menu/list
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "menuId": 1,
      "menuName": "系统管理",
      "icon": "setting",
      "orderNum": 1,
      "menuType": "M",
      "visible": "0",
      "children": [
        {
          "menuId": 100,
          "menuName": "用户管理",
          "icon": "user",
          "orderNum": 1,
          "menuType": "C",
          "path": "/system/user",
          "component": "system/user/index",
          "permission": "system:user:list",
          "children": [
            {
              "menuId": 1001,
              "menuName": "用户查询",
              "menuType": "F",
              "permission": "system:user:query"
            },
            {
              "menuId": 1002,
              "menuName": "用户新增",
              "menuType": "F",
              "permission": "system:user:add"
            }
          ]
        }
      ]
    }
  ]
}
```

### 2. 获取菜单详情

```http
GET /system/menu/{menuId}
```

### 3. 新增菜单

```http
POST /system/menu
```

**请求体：**

```json
{
  "menuName": "测试管理",
  "icon": "test",
  "orderNum": 10,
  "menuType": "M",
  "visible": "0",
  "parentId": 0
}
```

### 4. 修改菜单

```http
PUT /system/menu
```

### 5. 删除菜单

```http
DELETE /system/menu/{menuId}
```

## 菜单参数说明

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| menuName | String | 是 | 菜单名称 |
| parentId | Long | 是 | 父菜单 ID |
| orderNum | Integer | 是 | 显示顺序 |
| menuType | String | 是 | M-目录，C-菜单，F-按钮 |
| visible | String | 是 | 0-显示，1-隐藏 |
| icon | String | 否 | 菜单图标 |
| path | String | 否 | 路由地址 |
| component | String | 否 | 组件路径 |
| permission | String | 否 | 权限标识 |

## 数据表结构

```sql
CREATE TABLE sys_menu (
  menu_id bigint NOT NULL AUTO_INCREMENT COMMENT '菜单 ID',
  menu_name varchar(50) NOT NULL COMMENT '菜单名称',
  parent_id bigint DEFAULT '0' COMMENT '父菜单 ID',
  order_num int DEFAULT '0' COMMENT '显示顺序',
  path varchar(200) DEFAULT '' COMMENT '路由地址',
  component varchar(255) DEFAULT NULL COMMENT '组件路径',
  query varchar(255) DEFAULT NULL COMMENT '路由参数',
  is_frame int DEFAULT '1' COMMENT '是否为外链',
  is_cache int DEFAULT '0' COMMENT '是否缓存',
  menu_type char(1) DEFAULT '' COMMENT '菜单类型（M 目录 C 菜单 F 按钮）',
  visible char(1) DEFAULT '0' COMMENT '菜单状态',
  status char(1) DEFAULT '0' COMMENT '菜单状态',
  perms varchar(100) DEFAULT NULL COMMENT '权限标识',
  icon varchar(100) DEFAULT '#' COMMENT '菜单图标',
  create_by varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (menu_id)
);
```
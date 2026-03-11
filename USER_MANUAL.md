# Micro Platform 用户手册

## 目录

1. [系统概述](#系统概述)
2. [快速开始](#快速开始)
3. [功能模块](#功能模块)
4. [操作指南](#操作指南)
5. [常见问题](#常见问题)

---

## 系统概述

Micro Platform 是一个基于 Spring Boot 3.x 和 Vue 3 的企业级微服务平台，提供以下核心功能：

- **系统管理** - 用户、角色、菜单、部门、字典等基础管理
- **工作流引擎** - 基于 Flowable BPMN 的流程管理
- **动态表单** - JSON Schema 驱动的表单设计器
- **租户管理** - SaaS 多租户支持
- **数据权限** - 细粒度的行/列/字段级权限控制
- **系统监控** - 服务器、Redis、JVM 监控

---

## 快速开始

### 1. 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+
- Maven 3.8+

### 2. 后端启动

```bash
# 克隆项目
git clone https://github.com/xyx11/platform.git
cd micro-platform

# 导入数据库
mysql -u root -p < schema_enhancements.sql

# 修改配置
# 编辑 mp-system/src/main/resources/application-local.yml
# 配置数据库和 Redis 连接

# 启动服务
cd mp-system
mvn spring-boot:run
```

访问 Swagger: http://localhost:8082/swagger-ui.html

### 3. 前端启动

```bash
cd mp-vue
npm install
npm run dev
```

访问前端：http://localhost:5173

默认账号：`admin` / `admin123`

---

## 功能模块

### 系统管理

| 模块 | 路径 | 描述 |
|------|------|------|
| 用户管理 | /system/user | 管理系统用户 |
| 角色管理 | /system/role | 管理角色和权限 |
| 菜单管理 | /system/menu | 管理系统菜单 |
| 部门管理 | /system/dept | 管理组织架构 |
| 字典管理 | /system/dict | 管理数据字典 |
| 岗位管理 | /system/post | 管理用户岗位 |
| 参数设置 | /system/config | 系统参数配置 |
| 操作日志 | /system/log | 用户操作记录 |
| 登录日志 | /system/loginlog | 登录记录 |
| 通知公告 | /system/notice | 系统公告管理 |

### 工作流管理

| 模块 | 路径 | 描述 |
|------|------|------|
| 流程设计器 | /system/workflow-designer | BPMN 流程设计 |
| 流程定义 | /system/workflow-definition | 流程部署管理 |
| 流程表单 | /system/workflow-form | 表单与流程绑定 |

### 表单管理

| 模块 | 路径 | 描述 |
|------|------|------|
| 表单定义 | /system/form-definition | 动态表单设计 |
| 表单渲染 | /system/form-render | 表单渲染引擎 |

### 租户管理

| 模块 | 路径 | 描述 |
|------|------|------|
| 套餐管理 | /system/tenant-package | 租户套餐配置 |
| 数据权限 | /system/data-permission | 细粒度权限规则 |

### 系统监控

| 模块 | 路径 | 描述 |
|------|------|------|
| 监控总览 | /monitor/overview | 系统状态总览 |
| 在线用户 | /system/online-user | 在线用户管理 |
| 服务器监控 | /monitor/server | 服务器状态 |
| Redis 监控 | /monitor/redis | Redis 状态 |
| 缓存监控 | /monitor/cache | 缓存状态 |

---

## 操作指南

### 1. 用户管理

**新增用户**:
1. 进入 系统管理 > 用户管理
2. 点击「新增」按钮
3. 填写用户信息（用户名、昵称、部门、角色等）
4. 点击「确定」

**分配角色**:
1. 在用户列表中找到目标用户
2. 点击「编辑」
3. 在角色选择框中勾选角色
4. 保存

### 2. 流程设计

**绘制流程**:
1. 进入 系统管理 > 流程设计器
2. 点击「新建」创建空白流程
3. 从左侧面板拖拽元素到画布：
   - 开始事件
   - 用户任务
   - 网关
   - 结束事件
4. 连接各元素
5. 点击「保存」并输入流程名称
6. 点击「部署」发布流程

**绑定表单**:
1. 进入 系统管理 > 流程表单
2. 点击「绑定表单」
3. 选择流程定义 Key 和任务节点
4. 选择要绑定的表单
5. 选择表单类型（启动表单/办理表单）
6. 保存

### 3. 表单设计

**创建表单**:
1. 进入 系统管理 > 表单定义
2. 点击「新建表单」
3. 填写表单名称和编码
4. 编写 JSON Schema:
```json
{
  "type": "object",
  "properties": {
    "title": {
      "type": "string",
      "title": "标题",
      "required": true
    },
    "content": {
      "type": "string",
      "title": "内容"
    },
    "priority": {
      "type": "string",
      "title": "优先级",
      "enum": ["低", "中", "高"]
    }
  }
}
```
5. 点击「发布」

### 4. 租户套餐管理

**创建套餐**:
1. 进入 系统管理 > 套餐管理
2. 点击「新增套餐」
3. 填写套餐信息：
   - 套餐名称
   - 套餐编码
   - 套餐类型
   - 价格
   - 最大用户数
   - 最大存储空间
4. 保存

**停用套餐**:
1. 在套餐列表中找到目标套餐
2. 点击「停用」按钮
3. 确认后套餐状态变为停用

### 5. 数据权限配置

**配置行级权限**:
1. 进入 系统管理 > 数据权限规则
2. 点击「新增规则」
3. 配置规则：
   - 角色 ID
   - 表名
   - 权限类型：行级
   - 规则表达式：`#userId == data.createBy`
   - 数据过滤条件：`dept_id = #{deptId}`
4. 保存

**权限类型说明**:
- **行级**: 控制用户能看到哪些行数据
- **列级**: 控制用户能看到哪些列
- **字段级**: 控制用户能操作哪些字段

### 6. 在线用户管理

**查看在线用户**:
1. 进入 系统管理 > 在线用户
2. 可以看到所有在线用户的：
   - 用户 ID
   - Token
   - 剩余时间
   - 过期时间

**强制下线**:
1. 在在线用户列表中找到目标用户
2. 点击「强制下线」
3. 确认后用户将被踢出

---

## 常见问题

### Q1: 忘记密码怎么办？

A: 默认管理员账号为 `admin` / `admin123`。如果忘记密码，可以：
1. 在数据库中执行：
```sql
UPDATE sys_user SET password = '$2a$10$7JBTHVzq3S4E5mF4eXQzZ.6uQK8U7JxJ5QzZ5eXQzZ5eXQzZ5eXQ' WHERE username = 'admin';
```

### Q2: 流程无法启动？

A: 检查以下几点：
1. 流程是否已部署
2. 流程定义 Key 是否正确
3. 当前用户是否有启动权限
4. 流程是否处于激活状态

### Q3: 表单无法显示？

A: 检查：
1. 表单 Schema 是否是有效的 JSON
2. 表单是否已发布
3. 表单是否绑定到正确的流程任务
4. 浏览器控制台是否有错误信息

### Q4: 如何配置多租户？

A:
1. 创建租户套餐
2. 创建租户账号
3. 为租户分配套餐
4. 租户登录后自动应用套餐限制

### Q5: 数据权限不生效？

A: 检查：
1. 权限规则是否已启用
2. 规则表达式是否正确
3. 角色是否关联了权限规则
4. 查询是否使用了数据权限拦截器

---

## 技术支持

- 项目地址：https://github.com/xyx11/platform
- 问题反馈：https://github.com/xyx11/platform/issues
- 更新时间：2026-03-11
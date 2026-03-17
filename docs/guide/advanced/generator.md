# 代码生成

## 功能说明

代码生成模块根据数据库表结构自动生成 CRUD 代码，包括 Entity、Mapper、Service、Controller 和 Vue 前端代码。

## 主要功能

- 导入数据库表
- 配置生成规则
- 预览生成代码
- 下载生成代码
- 直接生成到项目

## 使用流程

### 1. 导入表

从数据库中选择要生成代码的表：

```
GET /system/tool/gen/importTable
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| tables | String | 是 | 表名列表（逗号分隔） |

### 2. 配置生成规则

编辑表的生成配置：

```
GET /system/tool/gen/edit/{genId}
```

**配置项：**

| 配置项 | 说明 | 示例 |
|--------|------|------|
| 生成模板 | 选择生成模板 | CRUD、树形表、主子表 |
| 包路径 | 生成的包名 | com.micro.platform.system |
| 模块名 | 模块名称 | system |
| 业务名 | 业务名称 | user |
| 生成功能 | 选择生成的功能 | 增删改查、导入导出 |
| 作者 | 作者名 | admin |

### 3. 预览代码

```
GET /system/tool/gen/preview/{genId}
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "java": {
      "entity": "package ...; @Data public class SysUser { ... }",
      "mapper": "package ...; public interface SysUserMapper { ... }",
      "service": "package ...; public class SysUserService { ... }",
      "controller": "package ...; @RestController public class SysUserController { ... }"
    },
    "sql": {
      "menu": "INSERT INTO sys_menu ..."
    },
    "vue": {
      "index.vue": "<template>...</template>",
      "api.js": "export function listUser() { ... }"
    }
  }
}
```

### 4. 下载代码

```
GET /system/tool/gen/download/{genId}
```

返回 ZIP 压缩包，包含所有生成的代码文件。

### 5. 直接生成到项目

```
POST /system/tool/gen/code/{genId}
```

代码直接生成到项目的对应目录中。

## 生成模板

### CRUD 模板

适用于普通单表：

```
├── entity/
│   └── SysUser.java
├── mapper/
│   └── SysUserMapper.java
├── service/
│   ├── SysUserService.java
│   └── impl/
│       └── SysUserServiceImpl.java
├── controller/
│   └── SysUserController.java
└── vue/
    └── system/
        └── user/
            └── index.vue
```

### 树形表模板

适用于部门、菜单等树形结构：

```
├── entity/
│   └── SysDept.java
├── mapper/
│   └── SysDeptMapper.java
├── service/
│   └── SysDeptService.java
└── controller/
    └── SysDeptController.java
```

### 主子表模板

适用于订单 - 订单项等关联表：

```
├── entity/
│   ├── Order.java
│   └── OrderItem.java
├── mapper/
│   ├── OrderMapper.java
│   └── OrderItemMapper.java
└── service/
    └── OrderService.java
```

## 字段类型映射

| 数据库类型 | Java 类型 | Vue 类型 |
|-----------|----------|---------|
| varchar | String | el-input |
| text | String | el-input (type=textarea) |
| int/integer | Integer | el-input-number |
| bigint | Long | el-input-number |
| decimal | BigDecimal | el-input-number |
| date/datetime | LocalDateTime | el-date-picker |
| tinyint(1) | Boolean | el-switch |

## 数据表结构

```sql
-- 代码生成业务表
CREATE TABLE gen_table (
  table_id bigint NOT NULL AUTO_INCREMENT COMMENT '表编号',
  table_name varchar(200) DEFAULT '' COMMENT '表名称',
  table_comment varchar(500) DEFAULT '' COMMENT '表描述',
  sub_table_name varchar(64) DEFAULT NULL COMMENT '关联子表的表名',
  sub_table_fk_name varchar(64) DEFAULT NULL COMMENT '子表关联的外键名',
  class_name varchar(100) DEFAULT '' COMMENT '实体类名称',
  tpl_category varchar(200) DEFAULT 'crud' COMMENT '使用的模板',
  package_name varchar(100) DEFAULT NULL COMMENT '生成包路径',
  module_name varchar(30) DEFAULT NULL COMMENT '生成模块名',
  business_name varchar(30) DEFAULT NULL COMMENT '生成业务名',
  function_name varchar(50) DEFAULT NULL COMMENT '生成功能名',
  function_author varchar(50) DEFAULT NULL COMMENT '生成功能作者',
  gen_type char(1) DEFAULT '0' COMMENT '生成代码方式',
  gen_path varchar(200) DEFAULT '/' COMMENT '生成路径',
  options varchar(1000) DEFAULT NULL COMMENT '其它生成选项',
  create_by varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (table_id)
);

-- 代码生成字段表
CREATE TABLE gen_table_column (
  column_id bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  table_id bigint DEFAULT NULL COMMENT '归属表编号',
  column_name varchar(200) DEFAULT NULL COMMENT '列名称',
  column_comment varchar(500) DEFAULT NULL COMMENT '列描述',
  column_type varchar(100) DEFAULT NULL COMMENT '列类型',
  java_type varchar(500) DEFAULT NULL COMMENT 'JAVA 类型',
  java_field varchar(200) DEFAULT NULL COMMENT 'JAVA 字段名',
  is_pk char(1) DEFAULT NULL COMMENT '是否主键',
  is_increment char(1) DEFAULT NULL COMMENT '是否自增',
  is_required char(1) DEFAULT NULL COMMENT '是否必填',
  is_insert char(1) DEFAULT NULL COMMENT '是否为插入字段',
  is_edit char(1) DEFAULT NULL COMMENT '是否编辑字段',
  is_list char(1) DEFAULT NULL COMMENT '是否列表字段',
  is_query char(1) DEFAULT NULL COMMENT '是否查询字段',
  query_type varchar(200) DEFAULT 'EQ' COMMENT '查询方式',
  html_type varchar(200) DEFAULT NULL COMMENT '显示类型',
  dict_type varchar(200) DEFAULT '' COMMENT '字典类型',
  sort int DEFAULT '0' COMMENT '排序',
  create_by varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (column_id)
);
```
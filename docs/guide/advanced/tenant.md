# 多租户

## 功能说明

多租户模块用于支持 SaaS 场景，实现数据隔离，让多个租户共享同一套系统实例。

## 租户概念

- **租户**：使用系统的组织或企业
- **租户包**：租户可用的功能套餐
- **租户管理员**：租户的超级管理员

## 数据隔离方式

| 方式 | 说明 | 优点 | 缺点 |
|------|------|------|------|
| 独立数据库 | 每个租户独立数据库 | 数据完全隔离，安全性高 | 成本高，维护复杂 |
| 独立 Schema | 每个租户独立 Schema | 数据隔离，成本适中 | 跨租户查询复杂 |
| 共享数据库 + 租户 ID | 所有租户共享数据库，通过 tenant_id 区分 | 成本低，易于维护 | 需要代码层面保证隔离 |

本项目采用**共享数据库 + 租户 ID**的方式。

## 核心表结构

### 租户表

```sql
CREATE TABLE sys_tenant (
  tenant_id bigint NOT NULL AUTO_INCREMENT COMMENT '租户 ID',
  tenant_name varchar(100) NOT NULL COMMENT '租户名称',
  tenant_code varchar(50) NOT NULL COMMENT '租户编码',
  contact_name varchar(50) DEFAULT NULL COMMENT '联系人',
  contact_phone varchar(20) DEFAULT NULL COMMENT '联系电话',
  contact_email varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  status char(1) DEFAULT '0' COMMENT '状态',
  expire_time datetime DEFAULT NULL COMMENT '过期时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (tenant_id),
  UNIQUE KEY uk_tenant_code (tenant_code)
);
```

### 租户套餐包表

```sql
CREATE TABLE sys_tenant_package (
  package_id bigint NOT NULL AUTO_INCREMENT COMMENT '套餐 ID',
  package_name varchar(100) NOT NULL COMMENT '套餐名称',
  package_code varchar(50) NOT NULL COMMENT '套餐编码',
  menu_ids varchar(1000) DEFAULT NULL COMMENT '菜单 ID 列表',
  status char(1) DEFAULT '0' COMMENT '状态',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (package_id)
);
```

### 租户与套餐关联表

```sql
CREATE TABLE sys_tenant_package_relation (
  tenant_id bigint NOT NULL COMMENT '租户 ID',
  package_id bigint NOT NULL COMMENT '套餐 ID',
  PRIMARY KEY (tenant_id, package_id)
);
```

## 租户隔离实现

### 1. MyBatis Plus 插件

```java
@Component
public class TenantLineInnerInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter) {
        // 获取当前租户 ID
        Long tenantId = TenantContextHolder.getTenantId();

        if (tenantId != null) {
            // 在 SQL 中追加租户条件
            // WHERE tenant_id = {tenantId}
        }
    }
}
```

### 2. 实体类添加租户 ID 字段

```java
@Data
public class BaseEntity {

    /**
     * 租户 ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
```

### 3. 自动填充租户 ID

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填充租户 ID
        this.strictInsertFill(metaObject, "tenantId", Long.class,
            TenantContextHolder.getTenantId());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时不更新租户 ID
    }
}
```

### 4. 租户上下文

```java
public class TenantContextHolder {

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}
```

### 5. 请求拦截器

```java
@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) {
        // 从请求头获取租户 ID
        String tenantId = request.getHeader("X-Tenant-ID");

        if (StringUtils.isNotBlank(tenantId)) {
            TenantContextHolder.setTenantId(Long.parseLong(tenantId));
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                               HttpServletResponse response,
                               Object handler,
                               Exception ex) {
        // 清理租户上下文
        TenantContextHolder.clear();
    }
}
```

## 多租户管理 API

### 1. 创建租户

```http
POST /system/tenant
```

**请求体：**

```json
{
  "tenantName": "测试企业",
  "tenantCode": "test_company",
  "contactName": "张三",
  "contactPhone": "13800138000",
  "contactEmail": "test@example.com",
  "packageId": 1,
  "expireTime": "2025-12-31 23:59:59"
}
```

### 2. 租户列表

```http
GET /system/tenant/list
```

### 3. 更新租户

```http
PUT /system/tenant
```

### 4. 删除租户

```http
DELETE /system/tenant/{tenantId}
```

## 注意事项

1. 系统管理模块（租户管理、套餐管理）不受租户隔离限制
2. 租户管理员只能管理本租户的数据
3. 租户 ID 从请求头 `X-Tenant-ID` 中获取
4. 所有业务表都需要添加 `tenant_id` 字段
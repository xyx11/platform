# 数据权限

## 功能说明

数据权限模块用于控制用户可访问的数据范围，支持按角色配置不同的数据权限。

## 数据权限范围

| 范围 | 值 | 说明 |
|------|-----|------|
| 全部数据权限 | 1 | 可查看系统所有数据 |
| 自定义数据权限 | 2 | 可查看自定义的数据范围（指定部门） |
| 本部门数据权限 | 3 | 可查看本部门数据 |
| 本部门及以下数据权限 | 4 | 可查看本部门及下级部门数据 |
| 仅本人数据权限 | 5 | 只能查看自己创建的数据 |

## 配置方式

### 1. 角色数据权限配置

在角色管理中为每个角色配置数据权限：

```
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

### 2. 数据权限注解

在 Service 方法上使用数据权限注解：

```java
@DataScope(deptAlias = "d", userAlias = "u")
public List<SysUser> selectUserList(SysUser user) {
    return baseMapper.selectUserList(user);
}
```

### 3. SQL 拼接

数据权限会自动在 SQL 后追加权限条件：

```sql
-- 原始 SQL
SELECT * FROM sys_user u

-- 追加权限后（本部门及以下）
SELECT * FROM sys_user u
LEFT JOIN sys_dept d ON u.dept_id = d.dept_id
WHERE d.dept_id IN (100, 101, 102)
```

## 实现原理

### 1. 切面处理

```java
@Aspect
@Component
public class DataScopeAspect {

    @Around("@annotation(controllerDataScope)")
    public Object doBefore(ProceedingJoinPoint point, DataScope controllerDataScope) {
        // 获取当前用户
        LoginUser user = SecurityUtils.getLoginUser();

        // 获取用户的数据权限
        String dataScope = getDataScope(user);

        // 拼接 SQL 过滤条件
        String dataScopeSql = getDataScopeSql(user, dataScope);

        // 将 SQL 条件注入到参数中
        // ...
    }
}
```

### 2. 权限 SQL 生成

```java
public String getDataScopeSql(LoginUser user, String dataScope) {
    switch (dataScope) {
        case "1":
            // 全部数据权限，不追加条件
            return "";
        case "2":
            // 自定义数据权限
            return "AND d.dept_id IN (" + getDeptIds(user) + ")";
        case "3":
            // 本部门数据权限
            return "AND d.dept_id = " + user.getDeptId();
        case "4":
            // 本部门及以下数据权限
            return "AND d.dept_id IN (" + getDeptAndChildIds(user) + ")";
        case "5":
            // 仅本人数据权限
            return "AND u.user_id = " + user.getUserId();
        default:
            return "";
    }
}
```

## 使用示例

### Controller

```java
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/list")
    @DataScope(deptAlias = "d", userAlias = "u")
    public Result<Page<SysUser>> list(SysUser user) {
        Page<SysUser> page = sysUserService.selectUserList(user);
        return Result.success(page);
    }
}
```

### Mapper XML

```xml
<select id="selectUserList" resultMap="SysUserResult">
    SELECT u.user_id, u.user_name, u.nick_name, u.dept_id,
           d.dept_name, d.leader
    FROM sys_user u
    LEFT JOIN sys_dept d ON u.dept_id = d.dept_id
    WHERE u.del_flag = '0'
    <!-- 数据权限范围 -->
    ${dataScope}
</select>
```

### Service

```java
@Service
public class SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @DataScope(deptAlias = "d", userAlias = "u")
    public Page<SysUser> selectUserList(SysUser user) {
        return sysUserMapper.selectUserList(user);
    }
}
```

## 多用户权限

当用户有多个角色时，数据权限取并集：

```java
public String getDataScope(LoginUser user) {
    List<SysRole> roles = user.getRoles();

    // 如果有任何角色是全部数据权限，直接返回
    if (roles.stream().anyMatch(r -> "1".equals(r.getDataScope()))) {
        return "1";
    }

    // 收集所有自定义数据权限的部门 ID
    Set<Long> deptIds = new HashSet<>();
    for (SysRole role : roles) {
        if ("2".equals(role.getDataScope())) {
            deptIds.addAll(role.getDeptIds());
        }
    }

    // 返回最宽松的数据权限
    // ...
}
```

## 注意事项

1. 数据权限只对有 `@DataScope` 注解的查询生效
2. 数据权限 SQL 会追加到 WHERE 条件后面，需要确保 SQL 语法正确
3. 自定义数据权限时，部门 ID 列表不能为空
4. 超级管理员（admin）默认拥有全部数据权限
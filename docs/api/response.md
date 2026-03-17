# 通用响应

## 响应格式

所有 API 接口返回统一的 JSON 格式：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

## 响应参数

| 参数 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码 |
| msg | String | 提示信息 |
| data | Object | 返回数据 |

## 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未认证或 Token 过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 成功响应

### 无数据

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

### 单个对象

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "userId": 1,
    "userName": "admin",
    "nickName": "管理员"
  }
}
```

### 列表数据

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "userId": 1,
      "userName": "admin"
    },
    {
      "userId": 2,
      "userName": "test"
    }
  ]
}
```

### 分页数据

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "rows": [
      {
        "userId": 1,
        "userName": "admin"
      },
      {
        "userId": 2,
        "userName": "test"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

## 错误响应

### 参数校验失败

```json
{
  "code": 400,
  "msg": "参数错误：userName 不能为空",
  "data": null
}
```

### 未认证

```json
{
  "code": 401,
  "msg": "登录状态已过期，请重新登录",
  "data": null
}
```

### 无权限

```json
{
  "code": 403,
  "msg": "没有操作权限",
  "data": null
}
```

### 资源不存在

```json
{
  "code": 404,
  "msg": "用户不存在",
  "data": null
}
```

### 服务器错误

```json
{
  "code": 500,
  "msg": "系统内部错误，请联系管理员",
  "data": null
}
```

## Result 工具类

```java
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> error(int code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
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
    private SysUserService userService;

    @GetMapping("/list")
    public Result<Page<SysUser>> list(SysUser user) {
        Page<SysUser> page = userService.selectUserList(user);
        return Result.success(page);
    }

    @GetMapping("/{userId}")
    public Result<SysUser> getInfo(@PathVariable Long userId) {
        SysUser user = userService.selectUserById(userId);
        return Result.success(user);
    }

    @PostMapping
    public Result<Void> add(@RequestBody SysUser user) {
        userService.add(user);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        userService.update(user);
        return Result.success();
    }

    @DeleteMapping("/{userIds}")
    public Result<Void> delete(@PathVariable Long[] userIds) {
        userService.deleteByIds(userIds);
        return Result.success();
    }
}
```
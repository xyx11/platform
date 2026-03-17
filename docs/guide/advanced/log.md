# 操作日志

## 功能说明

操作日志模块用于记录用户在系统中的所有操作行为，支持日志查询、统计和导出。

## 日志内容

| 字段 | 说明 | 示例 |
|------|------|------|
| 操作模块 | 操作所属模块 | 用户管理、角色管理 |
| 操作类型 | 增删改查等操作 | 新增、修改、删除 |
| 操作人员 | 执行操作的用户 | admin |
| 请求方法 | HTTP 方法 | GET、POST、PUT、DELETE |
| 请求 URL | 请求地址 | /system/user |
| 请求参数 | 请求携带的参数 | {"userName": "test"} |
| 返回数据 | 接口返回结果 | {"code": 200, ...} |
| 操作状态 | 成功/失败 | 1-成功，0-失败 |
| 错误信息 | 操作失败时的错误 | 异常堆栈信息 |
| 操作时间 | 执行操作的时间 | 2024-01-01 12:00:00 |
| 消耗时间 | 接口响应耗时 | 120ms |
| IP 地址 | 操作来源 IP | 192.168.1.100 |
| 操作地点 | IP 归属地 | 广东省深圳市 |

## 使用方式

### 1. 添加日志注解

```java
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @OperationLog(module = "用户管理", type = OperationType.INSERT)
    @PostMapping
    public Result<Void> add(@RequestBody SysUser user) {
        return sysUserService.add(user);
    }

    @OperationLog(module = "用户管理", type = OperationType.UPDATE)
    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        return sysUserService.update(user);
    }

    @OperationLog(module = "用户管理", type = OperationType.DELETE)
    @DeleteMapping("/{userIds}")
    public Result<Void> delete(@PathVariable Long[] userIds) {
        return sysUserService.deleteByIds(userIds);
    }
}
```

### 2. 日志注解定义

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作模块
     */
    String module();

    /**
     * 操作类型
     */
    OperationType type();

    /**
     * 是否保存请求参数
     */
    boolean saveRequestData() default true;

    /**
     * 是否保存响应数据
     */
    boolean saveResponseData() default false;
}
```

### 3. 操作类型枚举

```java
public enum OperationType {

    /**
     * 新增
     */
    INSERT,

    /**
     * 修改
     */
    UPDATE,

    /**
     * 删除
     */
    DELETE,

    /**
     * 查询
     */
    QUERY,

    /**
     * 导入
     */
    IMPORT,

    /**
     * 导出
     */
    EXPORT,

    /**
     * 授权
     */
    GRANT,

    /**
     * 其他
     */
    OTHER
}
```

## 日志记录实现

### 切面处理

```java
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        HttpServletRequest request = getRequest();
        String ip = getIp(request);
        String location = getAddress(ip);

        // 执行方法
        Object result = null;
        String status = "1";
        String errorMsg = null;

        try {
            result = point.proceed();
        } catch (Exception e) {
            status = "0";
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();

            // 构建日志对象
            OperationLogEntity log = new OperationLogEntity();
            log.setModule(operationLog.module());
            log.setType(operationLog.type().name());
            log.setMethod(request.getMethod());
            log.setUrl(request.getRequestURI());
            log.setOperator(getUsername());
            log.setDeptName(getDeptName());
            log.setOperIp(ip);
            log.setOperLocation(location);
            log.setStatus(status);
            log.setCostTime(endTime - startTime);

            // 保存请求参数
            if (operationLog.saveRequestData()) {
                log.setOperParam(getRequestData(point));
            }

            // 保存响应数据
            if (operationLog.saveResponseData()) {
                log.setJsonResult(JSON.toJSONString(result));
            }

            // 保存错误信息
            if (StringUtils.isNotBlank(errorMsg)) {
                log.setErrorMsg(errorMsg);
            }

            // 异步保存日志
            operationLogService.saveLogAsync(log);
        }

        return result;
    }
}
```

## API 接口

### 1. 获取日志列表

```http
GET /system/log/operation/list
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| module | String | 否 | 操作模块 |
| type | String | 否 | 操作类型 |
| operName | String | 否 | 操作人员 |
| status | String | 否 | 状态 |
| operTime | String | 否 | 操作时间范围 |
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
        "operId": 1,
        "title": "用户管理",
        "businessType": "INSERT",
        "method": "POST",
        "operUrl": "/system/user",
        "operIp": "192.168.1.100",
        "operLocation": "广东省深圳市",
        "operName": "admin",
        "deptName": "总公司",
        "status": "1",
        "errorMsg": null,
        "operTime": "2024-01-01 12:00:00",
        "costTime": 120
      }
    ],
    "total": 1
  }
}
```

### 2. 获取日志详情

```http
GET /system/log/operation/{operId}
```

### 3. 删除日志

```http
DELETE /system/log/operation/{operIds}
```

### 4. 清空日志

```http
DELETE /system/log/operation/clear
```

### 5. 导出日志

```http
POST /system/log/operation/export
```

## 数据表结构

```sql
CREATE TABLE sys_oper_log (
  oper_id bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  title varchar(50) DEFAULT '' COMMENT '模块标题',
  business_type int DEFAULT '0' COMMENT '业务类型',
  method varchar(100) DEFAULT '' COMMENT '方法名称',
  request_method varchar(10) DEFAULT '' COMMENT '请求方式',
  operator_type int DEFAULT '0' COMMENT '操作类别',
  oper_name varchar(50) DEFAULT '' COMMENT '操作人员',
  dept_name varchar(50) DEFAULT '' COMMENT '部门名称',
  oper_url varchar(255) DEFAULT '' COMMENT '请求 URL',
  oper_ip varchar(128) DEFAULT '' COMMENT '主机地址',
  oper_location varchar(255) DEFAULT '' COMMENT '操作地点',
  oper_param varchar(2000) DEFAULT '' COMMENT '请求参数',
  json_result varchar(2000) DEFAULT '' COMMENT '返回参数',
  status int DEFAULT '1' COMMENT '操作状态',
  error_msg varchar(2000) DEFAULT '' COMMENT '错误消息',
  oper_time datetime DEFAULT NULL COMMENT '操作时间',
  cost_time bigint DEFAULT '0' COMMENT '消耗时间',
  PRIMARY KEY (oper_id)
);
```

## IP 地址解析

```java
@Component
public class AddressUtils {

    public static String getRealAddressByIP(String ip) {
        // 使用 ip-api.com 或淘宝 IP 库
        String url = "http://ip-api.com/json/" + ip + "?lang=zh-CN";

        try {
            String response = HttpUtil.get(url);
            JSONObject json = JSON.parseObject(response);

            if (json.getInteger("status") == 1) {
                return json.getString("country") + " " +
                       json.getString("regionName") + " " +
                       json.getString("city");
            }
        } catch (Exception e) {
            // 记录异常
        }

        return "内网 IP";
    }
}
```
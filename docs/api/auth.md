# 认证说明

## 认证方式

系统采用 JWT (JSON Web Token) 进行身份认证。

## 认证流程

```
┌─────────┐      │      ┌─────────┐      │      ┌─────────┐
│  客户端  │      │      │  网关    │      │      │  认证服务 │
└────┬────┘      │      └────┬────┘      │      └────┬────┘
     │           │           │           │           │
     │  1.登录请求 │           │           │           │
     │──────────►│           │           │           │
     │           │           │           │           │
     │           │  2.转发请求 │           │           │
     │           │──────────►│           │           │
     │           │           │           │           │
     │           │           │  3.验证账号密码  │           │
     │           │           │──────────►│           │
     │           │           │           │           │
     │           │           │  4.返回 Token   │           │
     │           │           │◄──────────│           │
     │           │           │           │           │
     │           │  5.返回 Token   │           │           │
     │           │◄──────────│           │           │
     │           │           │           │           │
     │  6.返回 Token   │           │           │           │
     │◄──────────│           │           │           │
     │           │           │           │           │
     │  7.携带 Token 请求  │           │           │           │
     │──────────►│           │           │           │
     │           │           │           │           │
     │           │  8.验证 Token    │           │           │
     │           │───────────────►│           │           │
     │           │           │           │           │
     │           │  9.验证通过    │           │           │
     │           │◄───────────────│           │           │
     │           │           │           │           │
     │           │  转发请求到业务服务   │           │           │
     │           │──────────────────────►│           │
     │           │           │           │           │
     │  10.返回业务数据   │           │           │           │
     │◄──────────│           │           │           │
     │           │           │           │           │
```

## 登录接口

```http
POST /auth/login
```

**请求体：**

```json
{
  "username": "admin",
  "password": "admin123",
  "code": "1234",
  "uuid": "xxx-xxx-xxx"
}
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expires_in": 7200,
    "token_type": "Bearer"
  }
}
```

## 携带 Token

在请求头中添加 `Authorization` 字段：

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Token 结构

JWT Token 包含三部分：

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.    // Header
eyJsb2dpbl91c2VyIjp7"useridIjoxfX0.        // Payload
xxxxx                                        // Signature
```

### Header

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### Payload

```json
{
  "login_user": {
    "user_id": 1,
    "username": "admin",
    "dept_id": 100
  },
  "exp": 1704123456
}
```

## Token 配置

```yaml
security:
  jwt:
    # 密钥
    secret: "your-secret-key"
    # 过期时间（秒）
    expiration: 7200
    # Header 前缀
    header: "Bearer"
```

## 刷新 Token

```http
POST /auth/refresh
```

**请求头：**

```
Authorization: Bearer <旧 token>
```

**响应：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expires_in": 7200,
    "token_type": "Bearer"
  }
}
```

## 登出

```http
POST /auth/logout
```

**请求头：**

```
Authorization: Bearer <token>
```

## 错误码

| 状态码 | 说明 |
|--------|------|
| 401 | 未认证或 Token 过期 |
| 403 | 无权限访问 |
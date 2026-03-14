# 系统优化记录

## 已完成的优化

### 1. 操作日志异步保存
- 添加 `AsyncConfig` 异步任务配置类
- 创建 `OperationLogService` 接口和实现
- 更新 `OperationLogAspect` 使用异步服务保存日志
- 配置线程池参数（核心 5 线程，最大 10 线程，队列 100）
- 修复 common 模块 OperationLog 到 SysOperationLog 的字段映射

### 2. 忘记密码功能
- 新增 `ResetPassword.vue` 页面
- 实现手机号验证码发送
- 实现密码重置接口
- 更新登录页面添加忘记密码链接
- 添加路由 `/reset-password`

### 3. Token 自动刷新
- 后端实现 `refreshToken` 方法
- 前端 request.js 添加自动刷新逻辑
- 使用 refreshSubscribers 队列处理并发请求
- 401 响应时自动尝试刷新 token

### 4. 邮件服务集成
- SysProfileController 集成邮件发送验证码功能
- AuthService 支持密码重置时发送邮件通知
- 添加邮件服务配置示例（application.yml）

### 5. 短信服务集成
- 添加 `SmsService` 接口定义
- 添加 `SmsServiceStub` 开发环境空实现
- 实现 `AliyunSmsService` 阿里云短信服务
- 实现 `TencentSmsService` 腾讯云短信服务
- 添加 `SmsProperties` 配置属性类
- 支持通过配置切换短信服务商
- 集成示例代码和依赖说明

### 6. 个人中心增强
- 头像上传功能
- 手机号验证码修改
- 邮箱验证码修改
- 验证码倒计时控制

### 7. 工作流表单绑定
- 新增 WfFormBinding 实体和服务
- 实现表单绑定 CRUD 功能
- 前端页面集成

### 8. 导出接口统一
- 所有导出接口改为 `ResponseEntity<byte[]>` 返回格式
- 前端统一使用 blob 类型接收

### 9. 服务重构优化（最近）
- 将 `EmailNotificationService` 接口从 mp-system 移至 mp-common-core
- 修复 AuthService 编译错误和导入问题
- 优化 refreshToken 方法实现
- 统一所有模块的 EmailNotificationService 导入路径

## 生产环境配置

### 短信服务配置
在 `application.yml` 中配置：
```yaml
sms:
  enabled: true  # 启用短信服务
  provider: aliyun  # 或 tencent
  access-key-id: your-access-key-id
  access-key-secret: your-access-key-secret
  sign-name: 您的签名名称
  template-code: SMS_123456789
  aliyun-region-id: cn-hangzhou
```

### 邮件服务配置
在 `application.yml` 中配置：
```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 465
    username: your-email@example.com
    password: your-auth-code
```

## 系统功能清单

### 系统管理
- [x] 用户管理（增删改查、导入导出、角色分配）
- [x] 角色管理（增删改查、权限分配）
- [x] 菜单管理（增删改查、权限配置）
- [x] 部门管理（增删改查）
- [x] 岗位管理（增删改查、导出）
- [x] 字典管理（增删改查）
- [x] 参数设置（增删改查、导出）

### 个人中心
- [x] 个人信息修改
- [x] 密码修改
- [x] 头像上传
- [x] 手机号修改（带验证码）
- [x] 邮箱修改（带验证码）
- [x] 忘记密码（通过手机号重置）

### 系统监控
- [x] Redis 监控
- [x] 服务器监控
- [x] 缓存监控
- [x] 在线用户监控

### 系统工具
- [x] 代码生成
- [x] 表单构建
- [x] WebSocket 消息推送

### 工作流
- [x] 流程设计器
- [x] 流程定义
- [x] 流程实例监控
- [x] 任务管理
- [x] 待办事项
- [x] 表单绑定

### 日志管理
- [x] 操作日志（异步保存）
- [x] 登录日志

### 定时任务
- [x] 任务管理
- [x] 任务日志
- [x] 待办提醒定时任务

## 技术栈

- 后端：Spring Boot 3.x, MyBatis-Plus, Sa-Token, Redis
- 前端：Vue 3, Element Plus, Axios
- 数据库：MySQL 8+
- 缓存：Redis
- 工作流：Flowable
- 短信：阿里云/腾讯云（可选）
- 邮件：JavaMail（可选）

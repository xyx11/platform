# 常见问题

## 环境配置

### 1. Java 版本要求？

项目要求 **JDK 17+**，推荐使用 GraalVM 17。

Lombok 1.18.36 支持 Java 17，但不支持 Java 24。

### 2. Nacos 启动失败？

检查以下几点：
- 确保端口 8848 未被占用
- 使用单机模式启动：`bin/startup.sh -m standalone`
- 检查 logs 目录下的日志

### 3. MySQL 连接失败？

```yaml
# 检查配置
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/micro_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

确保：
- MySQL 服务已启动
- 数据库已创建
- 用户名密码正确
- 防火墙未阻止 3306 端口

## 启动问题

### 4. mp-system 启动失败？

常见原因：
1. Nacos 未启动
2. 数据库连接失败
3. Redis 未启动
4. 端口 8082 被占用

查看日志：
```bash
tail -f logs/mp-system.log
```

### 5. 前端编译错误？

```bash
# 清除缓存
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### 6. 服务注册失败？

检查 Nacos 配置：
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        username: nacos
        password: nacos
```

## 使用问题

### 7. 登录提示验证码错误？

开发环境可以关闭验证码：
```yaml
security:
  captcha:
    enabled: false
```

### 8. 流程设计器无法保存？

检查：
- BPMN XML 格式是否正确
- 是否所有节点都有必需的配置
- 浏览器控制台是否有错误

### 9. 表单设计器加载失败？

清除浏览器缓存，重新加载页面。

### 10. 如何重置 admin 密码？

```sql
UPDATE sys_user
SET password = '$2a$10$7JBmVpDKT7z.jXzQF5K5L.xYz9VJ8VJ8VJ8VJ8VJ8VJ8VJ8VJ8VJ'
WHERE user_name = 'admin';
```

新密码为：`admin123`

## 部署问题

### 11. 打包失败？

```bash
# 清理编译
mvn clean

# 跳过测试打包
mvn package -DskipTests

# 查看详细信息
mvn package -X
```

### 12. Docker 部署？

```dockerfile
FROM openjdk:17-slim

WORKDIR /app
COPY target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 性能优化

### 13. 接口响应慢？

优化建议：
1. 添加索引
2. 使用 Redis 缓存
3. 分页查询
4. 异步处理

### 14. 内存溢出？

调整 JVM 参数：
```bash
java -Xms512m -Xmx1024m -jar app.jar
```

## 其他

### 15. 如何获取日志？

```bash
# 查看实时日志
tail -f logs/mp-system.log

# 查看错误日志
grep ERROR logs/mp-system.log

# 查看最近 100 行
tail -100 logs/mp-system.log
```

### 16. 数据库表结构在哪里？

```
mp-system/src/main/resources/sql/
├── ry_20240101.sql          # 初始化 SQL
├── workflow_form_binding.sql # 工作流表单表
└── ...
```

### 17. 如何贡献代码？

1. Fork 项目
2. 创建分支 (`git checkout -b feature/your-feature`)
3. 提交代码 (`git commit -m 'Add some feature'`)
4. 推送分支 (`git push origin feature/your-feature`)
5. 创建 Pull Request
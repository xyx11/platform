# 部署运维手册

## 1. 环境要求

### 1.1 基础环境

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 推荐使用 Oracle JDK 17 或 OpenJDK 17 |
| MySQL | 8.0+ | 数据库 |
| Redis | 6.0+ | 缓存 |
| Node.js | 18+ | 前端构建 |
| Maven | 3.8+ | 构建工具 |
| Nacos | 2.2+ | 服务注册与配置中心（可选） |

### 1.2 服务器配置

| 环境 | CPU | 内存 | 磁盘 | 数量 |
|------|-----|------|------|------|
| 开发环境 | 4 核 | 8GB | 50GB | 1 |
| 测试环境 | 8 核 | 16GB | 100GB | 1 |
| 生产环境 | 16 核 | 32GB | 200GB | 2+ |

---

## 2. 安装步骤

### 2.1 安装 JDK 17

```bash
# CentOS/RHEL
yum install java-17-openjdk-devel

# Ubuntu/Debian
apt install openjdk-17-jdk

# 验证安装
java -version
```

### 2.2 安装 MySQL 8.0

```bash
# CentOS/RHEL
yum install mysql-server

# Ubuntu/Debian
apt install mysql-server

# 初始化
mysql_secure_installation

# 创建数据库
mysql -u root -p
CREATE DATABASE micro_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2.3 安装 Redis 6.0

```bash
# CentOS/RHEL
yum install redis

# Ubuntu/Debian
apt install redis-server

# 启动
systemctl start redis
systemctl enable redis
```

### 2.4 安装 Nacos（可选）

```bash
# 下载
wget https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.tar.gz

# 解压
tar -xzf nacos-server-2.2.3.tar.gz
cd nacos/bin

# 启动（单机模式）
sh startup.sh -m standalone

# 访问控制台
http://localhost:8848/nacos
# 默认账号密码：nacos/nacos
```

---

## 3. 数据库初始化

```bash
# 导入基础表结构
mysql -u root -p micro_platform < schema_base.sql

# 导入增强功能表
mysql -u root -p micro_platform < schema_enhancements.sql

# 导入工作流表
mysql -u root -p micro_platform < schema_workflow.sql

# 导入表单设计器表
mysql -u root -p micro_platform < schema_form_designer.sql

# 导入消息表
mysql -u root -p micro_platform < schema_message.sql

# 导入租户表
mysql -u root -p micro_platform < schema_tenant.sql

# 导入审计日志表
mysql -u root -p micro_platform < schema_audit_log.sql
```

---

## 4. 后端部署

### 4.1 开发环境部署

```bash
# 克隆项目
git clone https://github.com/xyx11/platform.git
cd micro-platform

# 修改配置
# 编辑 mp-system/src/main/resources/application-local.yml
# 配置数据库和 Redis 连接

# 构建
mvn clean package -DskipTests

# 启动
java -jar mp-system/target/mp-system-1.0.0.jar --spring.profiles.active=local
```

### 4.2 生产环境部署

```bash
# 创建应用目录
mkdir -p /opt/micro-platform
cd /opt/micro-platform

# 上传 jar 包
scp mp-system/target/mp-system-1.0.0.jar user@server:/opt/micro-platform/

# 创建配置文件
vim application-prod.yml
```

**application-prod.yml**:
```yaml
server:
  port: 8080

spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://db-host:3306/micro_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
          username: db_user
          password: db_password
  redis:
    host: redis-host
    port: 6379
    password: redis_password

sa-token:
  token-prefix: Bearer
  timeout: 86400

logging:
  level:
    root: INFO
    com.micro.platform: INFO
  file:
    name: /opt/micro-platform/logs/application.log
```

**创建启动脚本**:
```bash
vim start.sh
```

```bash
#!/bin/bash
APP_NAME=mp-system-1.0.0.jar
APP_DIR=/opt/micro-platform

cd $APP_DIR

# 检查是否已运行
PID=$(ps -ef | grep $APP_NAME | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "Application is already running (PID: $PID)"
    exit 1
fi

# 启动应用
nohup java -Xms2g -Xmx2g -XX:+UseG1GC \
    -jar $APP_NAME \
    --spring.profiles.active=prod \
    > /dev/null 2>&1 &

echo "Application starting..."
sleep 3
PID=$(ps -ef | grep $APP_NAME | grep -v grep | awk '{print $2}')
echo "Application started (PID: $PID)"
```

```bash
# 添加执行权限
chmod +x start.sh

# 启动
./start.sh
```

---

## 5. 前端部署

### 5.1 开发环境

```bash
cd mp-vue

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 访问
http://localhost:5173
```

### 5.2 生产环境

```bash
cd mp-vue

# 修改 API 地址
# 编辑 vite.config.js 中的 target 配置

# 构建
npm run build

# 部署到 Nginx
dist/ 目录部署到 Nginx
```

**Nginx 配置**:
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /var/www/micro-platform;
    index index.html;

    # 前端静态文件
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # WebSocket 代理
    location /ws-endpoint {
        proxy_pass http://localhost:8080/ws-endpoint;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
}
```

---

## 6. 监控与维护

### 6.1 日志管理

**日志位置**:
- 应用日志：`/opt/micro-platform/logs/application.log`
- 系统日志：`/var/log/messages` 或 `/var/log/syslog`

**日志轮转** (/etc/logrotate.d/micro-platform):
```
/opt/micro-platform/logs/*.log {
    daily
    rotate 30
    missingok
    notifempty
    compress
    delaycompress
    copytruncate
}
```

### 6.2 健康检查

```bash
# 检查应用是否运行
curl http://localhost:8080/actuator/health

# 检查数据库连接
curl http://localhost:8080/actuator/health/db

# 检查 Redis 连接
curl http://localhost:8080/actuator/health/redis
```

### 6.3 性能监控

**JVM 监控**:
```bash
# 查看堆内存
jstat -gc <pid> 1000

# 查看线程
jstack <pid>

# 查看内存 dump
jmap -dump:format=b,file=heap.hprof <pid>
```

**慢查询日志**:
```sql
-- 开启慢查询
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;
```

---

## 7. 备份与恢复

### 7.1 数据库备份

```bash
# 全量备份
mysqldump -u root -p micro_platform > backup_$(date +%Y%m%d).sql

# 压缩备份
mysqldump -u root -p micro_platform | gzip > backup_$(date +%Y%m%d).sql.gz

# 定时备份（crontab）
0 2 * * * mysqldump -u root -p micro_platform | gzip > /backup/db_$(date +\%Y\%m\%d).sql.gz
```

### 7.2 数据库恢复

```bash
# 恢复数据
mysql -u root -p micro_platform < backup_20260311.sql

# 恢复压缩备份
gunzip < backup_20260311.sql.gz | mysql -u root -p micro_platform
```

---

## 8. 常见问题

### 8.1 应用无法启动

```bash
# 检查端口占用
netstat -tlnp | grep 8080

# 检查日志
tail -f /opt/micro-platform/logs/application.log

# 检查 JVM 参数
java -Xmx 参数是否合理
```

### 8.2 数据库连接失败

```bash
# 检查 MySQL 服务
systemctl status mysqld

# 检查连接数
mysql -u root -p -e "SHOW PROCESSLIST;"

# 检查防火墙
firewall-cmd --list-ports
```

### 8.3 Redis 连接失败

```bash
# 检查 Redis 服务
systemctl status redis

# 测试连接
redis-cli ping

# 检查配置
redis-cli CONFIG GET bind
```

### 8.4 内存溢出

```bash
# 增加堆内存
-Xms4g -Xmx4g

# 使用 G1 收集器
-XX:+UseG1GC

# 添加 OOM 导出
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/opt/micro-platform/logs/heap.hprof
```

---

## 9. 扩缩容

### 9.1 水平扩展

1. 部署多个应用实例
2. 使用 Nginx 负载均衡
3. 配置 Session 共享（Redis）

**Nginx 负载均衡配置**:
```nginx
upstream backend {
    server 192.168.1.10:8080;
    server 192.168.1.11:8080;
    server 192.168.1.12:8080;
}

server {
    location / {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 9.2 垂直扩展

- 增加 CPU 核心数
- 增加内存
- 使用 SSD 磁盘

---

## 10. 版本升级

```bash
# 1. 备份当前版本
cp /opt/micro-platform/mp-system-1.0.0.jar /opt/micro-platform/backup/

# 2. 停止应用
pkill -f mp-system-1.0.0.jar

# 3. 备份数据库
mysqldump -u root -p micro_platform > upgrade_backup.sql

# 4. 上传新版本
scp mp-system-1.1.0.jar user@server:/opt/micro-platform/

# 5. 执行数据库迁移脚本
mysql -u root -p micro_platform < upgrade_1.0_to_1.1.sql

# 6. 启动新版本
cd /opt/micro-platform
nohup java -jar mp-system-1.1.0.jar --spring.profiles.active=prod &

# 7. 验证
curl http://localhost:8080/actuator/health
```

---

**文档版本**: v1.0
**更新时间**: 2026-03-11
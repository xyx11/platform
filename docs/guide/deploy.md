# 部署指南

## 生产环境部署

### 1. 服务器要求

| 配置 | 最低要求 | 推荐配置 |
|------|---------|---------|
| CPU | 2 核 | 4 核 |
| 内存 | 4GB | 8GB |
| 磁盘 | 20GB | 50GB |
| 网络 | 10Mbps | 100Mbps |

### 2. 环境准备

```bash
# 安装 JDK 17
sudo yum install java-17-openjdk -y

# 安装 MySQL 8.0
sudo yum install mysql-server -y

# 安装 Redis
sudo yum install redis -y

# 安装 Nginx
sudo yum install nginx -y
```

### 3. Nacos 部署

```bash
# 下载 Nacos
wget https://github.com/alibaba/nacos/releases/download/v2.2.3/nacos-server-2.2.3.tar.gz
tar -xzf nacos-server-2.2.3.tar.gz
cd nacos

# 修改配置
cd conf
cp application.properties.example application.properties

# 集群模式配置
# 修改 application.properties 中的数据库配置
spring.datasource.platform=mysql
db.num=1
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true
db.user=nacos
db.password=nacos

# 集群节点配置
cd ../conf
cp cluster.conf.example cluster.conf
# 编辑 cluster.conf，添加节点 IP

# 启动
bin/startup.sh
```

### 4. 后端打包部署

```bash
# 打包
cd /Users/xieyunxing/micro-platform
mvn clean package -DskipTests -P prod

# 创建部署目录
mkdir -p /opt/micro-platform
cp mp-gateway/target/mp-gateway.jar /opt/micro-platform/
cp mp-auth/target/mp-auth.jar /opt/micro-platform/
cp mp-system/target/mp-system.jar /opt/micro-platform/
cp mp-generator/target/mp-generator.jar /opt/micro-platform/
```

### 5. 创建 systemd 服务

```bash
# mp-gateway 服务
sudo vi /etc/systemd/system/mp-gateway.service
```

```ini
[Unit]
Description=Micro Platform Gateway
After=syslog.target network.target

[Service]
User=root
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod /opt/micro-platform/mp-gateway.jar
ExecStop=/bin/kill -15 $MAINPID
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# 启用服务
sudo systemctl daemon-reload
sudo systemctl enable mp-gateway
sudo systemctl start mp-gateway

# 查看状态
sudo systemctl status mp-gateway
```

### 6. 前端部署

```bash
# 打包前端
cd mp-vue
npm run build:prod

# 配置 Nginx
sudo vi /etc/nginx/conf.d/micro-platform.conf
```

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /Users/xieyunxing/micro-platform/mp-vue/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /prod-api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

```bash
# 重启 Nginx
sudo nginx -s reload
```

### 7. Docker 部署

```dockerfile
# Dockerfile
FROM openjdk:17-slim

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'

services:
  nacos:
    image: nacos/nacos-server:v2.2.3
    environment:
      - MODE=standalone
    ports:
      - "8848:8848"

  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=root
      - MYSQL_DATABASE=micro_platform
    ports:
      - "3306:3306"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  mp-gateway:
    build: ./mp-gateway
    ports:
      - "8080:8080"
    depends_on:
      - nacos

  mp-auth:
    build: ./mp-auth
    ports:
      - "8081:8081"
    depends_on:
      - nacos

  mp-system:
    build: ./mp-system
    ports:
      - "8082:8082"
    depends_on:
      - nacos
```

```bash
# 启动
docker-compose up -d
```

### 8. 日志配置

```yaml
# application-prod.yml
logging:
  level:
    root: INFO
    com.micro.platform.system: INFO
  file:
    name: /opt/logs/mp-system.log
    max-size: 100MB
    max-history: 30
```

### 9. 性能优化

**JVM 参数优化：**

```bash
JAVA_OPTS="-Xms1024m -Xmx2048m -Xss512k -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

**MySQL 优化：**

```sql
-- 查看慢查询
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;

-- 添加索引
CREATE INDEX idx_user_name ON sys_user(user_name);
CREATE INDEX idx_dept_id ON sys_user(dept_id);
```

**Redis 优化：**

```bash
# 配置最大内存
maxmemory 2gb
maxmemory-policy allkeys-lru
```

### 10. 监控告警

**Spring Boot Admin:**

```xml
<!-- 添加依赖 -->
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-client</artifactId>
</dependency>
```

```yaml
# 配置
spring:
  boot:
    admin:
      client:
        url: http://admin-server:8088
```

**Prometheus + Grafana:**

```yaml
# 添加指标暴露
management:
  endpoints:
    web:
      exposure:
        include: prometheus,health,metrics
  metrics:
    export:
      prometheus:
        enabled: true
```

## 常见问题

### 服务启动失败

查看日志：
```bash
tail -f /opt/logs/mp-system.log
```

### 端口被占用

```bash
# 查看占用端口的进程
lsof -i :8080

# 终止进程
kill -9 <pid>
```

### 数据库连接失败

```bash
# 检查 MySQL 状态
systemctl status mysqld

# 检查连接配置
mysql -u root -p -e "SHOW GRANTS FOR 'micro_platform'@'%';"
```
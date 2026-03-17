# 环境配置

## 必需环境

| 环境 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 推荐 GraalVM 17 |
| MySQL | 8.0+ | 数据库 |
| Redis | 6.0+ | 缓存 |
| Node.js | 18+ | 前端运行时 |
| Maven | 3.8+ | 构建工具 |
| Nacos | 2.2+ | 服务注册与配置中心 |

## 环境安装

### 1. JDK 17

```bash
# macOS
brew install openjdk@17

# 配置 JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### 2. MySQL 8.0

```bash
# macOS
brew install mysql@8.0

# 启动 MySQL
brew services start mysql

# 初始化数据库
mysql -u root -p
CREATE DATABASE micro_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 3. Redis

```bash
# macOS
brew install redis
brew services start redis

# 验证
redis-cli ping
# 返回 PONG 表示成功
```

### 4. Node.js

```bash
# 使用 nvm 安装
nvm install 18
nvm use 18

# 验证
node -v
npm -v
```

### 5. Maven

```bash
# macOS
brew install maven

# 验证
mvn -v
```

### 6. Nacos

```bash
# 下载 Nacos
wget https://github.com/alibaba/nacos/releases/download/v2.2.3/nacos-server-2.2.3.tar.gz

# 解压
tar -xzf nacos-server-2.2.3.tar.gz
cd nacos

# 单机模式启动
bin/startup.sh -m standalone

# 访问控制台
# http://localhost:8848/nacos
# 默认账号：nacos / nacos
```

## 配置文件

### application-local.yml

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/micro_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

  redis:
    host: 127.0.0.1
    port: 6379
    password:
    database: 0

  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        username: nacos
        password: nacos
```

## 验证安装

```bash
# 检查 Java
java -version

# 检查 MySQL
mysql --version

# 检查 Redis
redis-cli --version

# 检查 Node
node -v

# 检查 Maven
mvn -version
```
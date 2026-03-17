# 快速开始

## 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+
- Maven 3.8+

## 安装步骤

### 1. 克隆项目

```bash
git clone https://github.com/xyx11/platform.git
cd platform
```

### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE micro_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

# 导入 SQL 脚本
mysql -u root -p micro_platform < sql/ry_20240101.sql
```

### 3. 启动后端服务

```bash
# 使用启动脚本
./start.sh

# 或者分别启动各个服务
cd mp-gateway && mvn spring-boot:run
cd mp-auth && mvn spring-boot:run
cd mp-system && mvn spring-boot:run
cd mp-generator && mvn spring-boot:run
```

### 4. 启动前端服务

```bash
cd mp-vue
npm install
npm run dev
```

### 5. 访问系统

- 前端地址：http://localhost:3001
- 网关地址：http://localhost:8080
- 默认账号：admin / admin123

## 常见问题

### 服务启动失败

检查 Nacos 是否启动：
```bash
# 启动 Nacos
cd nacos/bin
./startup.sh -m standalone
```

### 前端编译错误

清除缓存后重新安装依赖：
```bash
rm -rf node_modules package-lock.json
npm install
```
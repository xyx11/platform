#!/bin/bash

# 微服务启动脚本

echo "========================================"
echo "   启动 micro-platform 微服务"
echo "========================================"

# 设置 Maven 路径
MVN=/opt/homebrew/bin/mvn
PROFILES="-Dspring.profiles.active=local"

# 项目根目录
BASE_DIR="/Users/xieyunxing/micro-platform"

cd $BASE_DIR

# 1. 启动 mp-auth (端口 8081)
echo "启动 mp-auth..."
$MVN spring-boot:run -pl mp-auth $PROFILES > logs/mp-auth.log 2>&1 &
echo "  -> mp-auth 启动中 (端口 8081)"
sleep 3

# 2. 启动 mp-system (端口 8082)
echo "启动 mp-system..."
$MVN spring-boot:run -pl mp-system $PROFILES > logs/mp-system.log 2>&1 &
echo "  -> mp-system 启动中 (端口 8082)"
sleep 3

# 3. 启动 mp-generator (端口 8083)
echo "启动 mp-generator..."
$MVN spring-boot:run -pl mp-generator $PROFILES > logs/mp-generator.log 2>&1 &
echo "  -> mp-generator 启动中 (端口 8083)"
sleep 3

# 4. 启动 mp-job (端口 8084)
echo "启动 mp-job..."
$MVN spring-boot:run -pl mp-job $PROFILES > logs/mp-job.log 2>&1 &
echo "  -> mp-job 启动中 (端口 8084)"
sleep 3

# 5. 启动 mp-gateway (端口 8080)
echo "启动 mp-gateway..."
$MVN spring-boot:run -pl mp-gateway $PROFILES > logs/mp-gateway.log 2>&1 &
echo "  -> mp-gateway 启动中 (端口 8080)"

echo "========================================"
echo "   所有服务启动完成!"
echo "========================================"
echo ""
echo "服务列表:"
echo "  - mp-gateway:  http://localhost:8080"
echo "  - mp-auth:     http://localhost:8081"
echo "  - mp-system:   http://localhost:8082"
echo "  - mp-generator:http://localhost:8083"
echo "  - mp-job:      http://localhost:8084"
echo ""
echo "Swagger 地址:"
echo "  - http://localhost:8081/doc.html"
echo "  - http://localhost:8082/doc.html"
echo ""
echo "查看日志：tail -f logs/mp-*.log"
echo "停止服务：pkill -f 'spring.profiles.active=local'"
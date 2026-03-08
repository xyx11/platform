#!/bin/bash

# 微服务启动脚本

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
MVN=/opt/homebrew/bin/mvn
NACOS_HOME="/Users/xieyunxing/Downloads/nacos"
NACOS_SERVER="127.0.0.1:8848"
BASE_DIR="/Users/xieyunxing/micro-platform"
LOGS_DIR="$BASE_DIR/logs"
XXL_JOB_HOME="/Users/xieyunxing/Downloads/xxl-job-2.4.1"
XXL_JOB_PORT="8888"
VUE_PORT="3001"

# 服务配置：名称 - 模块 - 端口
declare -a SERVICES=(
    "mp-auth:mp-auth:8081"
    "mp-system:mp-system:8082"
    "mp-generator:mp-generator:8083"
    "mp-job:mp-job:8084"
    "mp-gateway:mp-gateway:8080"
)

print_header() {
    echo -e "${BLUE}"
    echo "========================================"
    echo "   启动 micro-platform 微服务"
    echo "========================================"
    echo -e "${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

print_warn() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# 检查端口是否被占用
check_port() {
    local port=$1
    if lsof -i :$port &>/dev/null; then
        return 0  # 端口被占用
    fi
    return 1  # 端口空闲
}

# 检查服务健康状态
check_health() {
    local port=$1
    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        local status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/ 2>/dev/null)
        if [ "$status" = "200" ] || [ "$status" = "404" ]; then
            return 0
        fi
        sleep 1
        attempt=$((attempt + 1))
    done
    return 1
}

# 清理已存在的服务进程
cleanup_existing() {
    local port=$1
    local pid=$(lsof -ti :$port 2>/dev/null)
    if [ -n "$pid" ]; then
        echo -n "  清理端口 $port 的进程 (PID: $pid)... "
        kill $pid 2>/dev/null
        sleep 1
        print_success "已清理"
    fi
}

# 主流程
cd $BASE_DIR

print_header

# 确保日志目录存在
mkdir -p $LOGS_DIR
mkdir -p /Users/xieyunxing/applogs/xxl-job/jobhandler

# 检查并启动 Nacos
echo -e "${BLUE}检查 Nacos 服务...${NC}"
if curl -s -o /dev/null -w "%{http_code}" http://$NACOS_SERVER/nacos/ | grep -q "200"; then
    print_success "Nacos 已在运行 ($NACOS_SERVER)"
else
    print_info "Nacos 未运行，正在启动..."
    if [ -d "$NACOS_HOME" ]; then
        $NACOS_HOME/bin/startup.sh -m standalone > /dev/null 2>&1
        sleep 5
        if curl -s -o /dev/null -w "%{http_code}" http://$NACOS_SERVER/nacos/ | grep -q "200"; then
            print_success "Nacos 启动完成"
        else
            print_warn "Nacos 可能启动失败，请检查"
        fi
    else
        print_error "Nacos 目录不存在，请检查配置"
    fi
fi
echo ""

# 启动 XXL-Job Admin
echo -e "${BLUE}检查 XXL-Job 调度中心...${NC}"
if curl -s -o /dev/null -w "%{http_code}" http://localhost:$XXL_JOB_PORT/xxl-job-admin/ | grep -qE "200|302"; then
    print_success "XXL-Job Admin 已在运行 (端口 $XXL_JOB_PORT)"
else
    print_info "XXL-Job Admin 未运行，正在启动..."
    if [ -d "$XXL_JOB_HOME" ]; then
        nohup java -Dserver.port=$XXL_JOB_PORT -jar $XXL_JOB_HOME/xxl-job-admin/target/xxl-job-admin-2.4.1.jar > $LOGS_DIR/xxl-job-admin.log 2>&1 &
        sleep 8
        if curl -s -o /dev/null -w "%{http_code}" http://localhost:$XXL_JOB_PORT/xxl-job-admin/ | grep -qE "200|302"; then
            print_success "XXL-Job Admin 启动完成"
        else
            print_warn "XXL-Job Admin 可能启动失败，请查看日志"
        fi
    else
        print_warn "XXL-Job 目录不存在，跳过启动"
    fi
fi
echo ""

# 停止旧服务
echo -e "${BLUE}检查旧服务进程...${NC}"
for service in "${SERVICES[@]}"; do
    IFS=':' read -r name module port <<< "$service"
    cleanup_existing $port
done
print_success "旧服务清理完成"
echo ""

# 启动服务
echo -e "${BLUE}启动微服务...${NC}"
echo ""

for service in "${SERVICES[@]}"; do
    IFS=':' read -r name module port <<< "$service"

    if check_port $port; then
        print_warn "$name 端口 $port 已被占用，跳过"
        continue
    fi

    echo -n "  启动 $name (端口 $port)... "
    $MVN spring-boot:run -pl $module -Dspring-boot.run.profiles=local > $LOGS_DIR/$name.log 2>&1 &
    echo -e "${GREEN}已启动${NC}"
    sleep 2
done

echo ""
echo -e "${BLUE}等待服务初始化...${NC}"
sleep 5

# 服务健康检查
echo -e "${BLUE}服务健康检查...${NC}"
echo ""

for service in "${SERVICES[@]}"; do
    IFS=':' read -r name module port <<< "$service"
    echo -n "  $name (:$port): "
    if check_port $port; then
        if check_health $port; then
            print_success "运行正常"
        else
            print_warn "进程运行但健康检查超时，请查看日志"
        fi
    else
        print_error "未启动"
    fi
done

echo ""

# 启动前端 Vue 应用
echo -e "${BLUE}检查前端 Vue 应用...${NC}"
VUE_PID=$(lsof -ti :$VUE_PORT 2>/dev/null)
if [ -n "$VUE_PID" ]; then
    print_success "前端应用已在运行 (端口 $VUE_PORT)"
else
    if [ -d "$BASE_DIR/mp-vue" ]; then
        echo -n "  启动前端应用 (端口 $VUE_PORT)... "
        cd $BASE_DIR/mp-vue
        nohup npm run dev > $LOGS_DIR/mp-vue.log 2>&1 &
        sleep 8
        if lsof -i :$VUE_PORT &>/dev/null; then
            print_success "已启动"
        else
            print_warn "可能启动失败，请查看日志"
        fi
        cd $BASE_DIR
    else
        print_warn "mp-vue 目录不存在，跳过启动"
    fi
fi

echo ""
echo "========================================"
echo -e "${GREEN}   所有服务启动完成!${NC}"
echo "========================================"
echo ""
echo "服务列表:"
echo "  - mp-gateway:   http://localhost:8080"
echo "  - mp-auth:      http://localhost:8081"
echo "  - mp-system:    http://localhost:8082"
echo "  - mp-generator: http://localhost:8083"
echo "  - mp-job:       http://localhost:8084"
echo "  - xxl-job:      http://localhost:$XXL_JOB_PORT/xxl-job-admin"
echo "  - mp-vue:       http://localhost:$VUE_PORT"
echo ""
echo "Swagger 地址:"
echo "  - http://localhost:8081/doc.html"
echo "  - http://localhost:8082/doc.html"
echo ""
echo "XXL-Job 调度中心:"
echo "  - 地址：http://localhost:$XXL_JOB_PORT/xxl-job-admin"
echo "  - 账号：admin / 123456"
echo ""
echo "常用命令:"
echo "  查看日志：tail -f logs/mp-auth.log"
echo "  停止服务：pkill -f 'spring-boot.run.profiles=local'"
echo "  停止 Nacos: $NACOS_HOME/bin/shutdown.sh"
echo "  停止前端：pkill -f 'npm run dev'"
echo ""
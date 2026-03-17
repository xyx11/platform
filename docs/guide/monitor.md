# 系统监控

## 功能说明

系统监控模块提供对服务器、JVM、Redis、数据库等资源的实时监控。

## 监控指标

### 1. CPU 监控

| 指标 | 说明 | 告警阈值 |
|------|------|---------|
| CPU 使用率 | 当前 CPU 使用百分比 | > 80% |
| CPU 核心数 | 可用处理器核心数 | - |
| 系统负载 | 1 分钟/5 分钟/15 分钟负载 | > CPU 核心数 |

### 2. 内存监控

| 指标 | 说明 | 告警阈值 |
|------|------|---------|
| 总内存 | 系统总内存 | - |
| 已用内存 | 当前已使用内存 | > 80% |
| 可用内存 | 剩余可用内存 | < 20% |
| 使用率 | 内存使用百分比 | > 80% |

### 3. 磁盘监控

| 指标 | 说明 | 告警阈值 |
|------|------|---------|
| 总容量 | 磁盘总大小 | - |
| 已使用 | 已使用容量 | > 80% |
| 可用容量 | 剩余可用容量 | < 20% |
| 使用率 | 磁盘使用百分比 | > 80% |

### 4. JVM 监控

| 指标 | 说明 | 告警阈值 |
|------|------|---------|
| JVM 版本 | Java 运行时版本 | - |
| 启动时间 | JVM 启动时间 | - |
| 运行时长 | JVM 运行时长 | - |
| 堆内存使用 | 堆内存使用情况 | > 80% |
| GC 次数 | 垃圾回收次数 | 突增 |
| GC 时间 | 垃圾回收耗时 | > 1s |

### 5. Redis 监控

| 指标 | 说明 | 告警阈值 |
|------|------|---------|
| 连接数 | 当前连接客户端数 | > 1000 |
| 内存使用 | Redis 内存使用 | > 80% |
| 命中率 | 缓存命中百分比 | < 90% |
| QPS | 每秒查询数 | 突增 |
| 键数量 | 存储的键总数 | - |

### 6. 数据库监控

| 指标 | 说明 | 告警阈值 |
|------|------|---------|
| 连接数 | 数据库连接数 | > 80% |
| 慢查询 | 执行缓慢的 SQL | > 10 个/分钟 |
| TPS | 每秒事务数 | - |
| QPS | 每秒查询数 | - |

## API 接口

### 1. 获取服务器信息

```http
GET /system/monitor/server
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "cpuInfo": {
      "cpuNum": 8,
      "cpuUsage": 45.5,
      "cpuModel": "Intel(R) Xeon(R) CPU"
    },
    "memoryInfo": {
      "totalMemory": 16384,
      "usedMemory": 8192,
      "freeMemory": 8192,
      "memoryUsage": 50.0
    },
    "jvmInfo": {
      "jvmVersion": "17.0.1",
      "startTime": "2024-01-01 10:00:00",
      "uptime": 3600000,
      "heapMax": 4096,
      "heapUsed": 2048,
      "gcCount": 100,
      "gcTime": 5000
    },
    "systemInfo": {
      "osName": "Linux",
      "osArch": "amd64",
      "osVersion": "5.4.0",
      "systemLoad": 2.5
    },
    "diskInfo": {
      "path": "/",
      "total": 500,
      "used": 200,
      "free": 300,
      "usage": 40.0
    }
  }
}
```

### 2. 获取 Redis 信息

```http
GET /system/monitor/redis
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "version": "7.0.0",
    "uptime": "100 天 5:30:20",
    "connectedClients": "50",
    "usedMemory": "1.2G",
    "usedMemoryPeak": "1.5G",
    "usedMemoryRatio": "60.0%",
    "totalCommandsProcessed": "1000000",
    "instantaneousOpsPerSec": "5000",
    "keyspaceHits": "900000",
    "keyspaceMisses": "100000",
    "dbInfo": [
      {
        "dbName": "db0",
        "keysCount": 1000
      }
    ]
  }
}
```

### 3. 获取数据库连接池信息

```http
GET /system/monitor/datasource
```

### 4. 获取线程池信息

```http
GET /system/monitor/threadpool
```

### 5. 获取系统日志

```http
GET /system/monitor/log
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| lines | Integer | 否 | 行数，默认 100 |
| level | String | 否 | 日志级别 |

## 在线监测

### 1. 服务监控

```http
GET /system/monitor/services
```

查看各微服务运行状态：

| 服务 | 状态 | 响应时间 | 内存 |
|------|------|---------|------|
| mp-gateway | ● 正常 | 5ms | 256MB |
| mp-auth | ● 正常 | 10ms | 512MB |
| mp-system | ● 正常 | 8ms | 1024MB |
| mp-generator | ● 正常 | 12ms | 256MB |

### 2. 缓存监控

```http
GET /system/monitor/cache
```

查看缓存使用情况：

- 缓存命中率
- 缓存键数量
- 缓存大小

### 3. 消息队列监控

```http
GET /system/monitor/mq
```

## 告警配置

### 1. 告警规则

```yaml
monitor:
  alert:
    cpu:
      threshold: 80
      duration: 5m
    memory:
      threshold: 80
      duration: 5m
    disk:
      threshold: 85
      duration: 10m
    jvm:
      heap-threshold: 80
      gc-time-threshold: 1000ms
```

### 2. 告警通知

支持多种通知方式：

- 邮件通知
- 短信通知
- 企业微信
- 钉钉
- 飞书

### 3. 告警历史

```http
GET /system/monitor/alert/history
```

## 数据表结构

```sql
-- 系统监控记录表
CREATE TABLE sys_monitor (
  id bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  type varchar(50) NOT NULL COMMENT '监控类型',
  cpu_usage decimal(5,2) DEFAULT NULL COMMENT 'CPU 使用率',
  memory_usage decimal(5,2) DEFAULT NULL COMMENT '内存使用率',
  disk_usage decimal(5,2) DEFAULT NULL COMMENT '磁盘使用率',
  jvm_heap_usage decimal(5,2) DEFAULT NULL COMMENT 'JVM 堆使用率',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  INDEX idx_type_time (type, create_time)
);

-- 告警记录表
CREATE TABLE sys_alert_log (
  id bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  alert_type varchar(50) NOT NULL COMMENT '告警类型',
  alert_level varchar(20) NOT NULL COMMENT '告警级别',
  alert_content text COMMENT '告警内容',
  alert_status char(1) DEFAULT '0' COMMENT '状态',
  handle_user varchar(64) DEFAULT NULL COMMENT '处理人',
  handle_time datetime DEFAULT NULL COMMENT '处理时间',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id)
);
```

## 优化建议

### 1. 性能优化

- 启用 Redis 缓存
- 配置连接池参数
- 优化 SQL 查询
- 添加数据库索引

### 2. 容量规划

- 监控增长趋势
- 预测资源需求
- 提前扩容

### 3. 故障排查

```bash
# 查看系统负载
uptime

# 查看内存使用
free -h

# 查看磁盘使用
df -h

# 查看进程
top

# 查看网络连接
netstat -an | grep ESTABLISHED | wc -l
```
# 系统通知模块

## 概述

系统提供多种通知方式，包括：
- 站内消息通知
- WebSocket 实时推送
- 邮件通知
- 短信通知

## 数据库设计

### 通知消息表

```sql
CREATE TABLE sys_notice_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    title VARCHAR(200) NOT NULL COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    notice_type VARCHAR(32) NOT NULL COMMENT '通知类型（1 通知 2 公告）',
    priority VARCHAR(10) DEFAULT 'normal' COMMENT '优先级（low/normal/high/urgent）',
    sender_id BIGINT COMMENT '发送人 ID',
    sender_name VARCHAR(64) COMMENT '发送人姓名',
    target_type VARCHAR(32) DEFAULT 'all' COMMENT '目标类型（all/user/role/dept）',
    target_ids VARCHAR(500) COMMENT '目标 ID 集合（逗号分隔）',
    status VARCHAR(2) DEFAULT '0' COMMENT '状态（0 草稿 1 已发布 2 已撤回）',
    publish_time DATETIME COMMENT '发布时间',
    expire_time DATETIME COMMENT '过期时间',
    is_push BIT DEFAULT 0 COMMENT '是否推送（1 是 0 否）',
    push_status VARCHAR(10) DEFAULT 'pending' COMMENT '推送状态（pending/success/failed）',
    read_count INT DEFAULT 0 COMMENT '阅读人数',
    unread_count INT DEFAULT 0 COMMENT '未读人数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by VARCHAR(64) COMMENT '创建者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by VARCHAR(64) COMMENT '更新者',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_notice_type (notice_type),
    INDEX idx_status (status),
    INDEX idx_publish_time (publish_time),
    INDEX idx_target_type (target_type)
) COMMENT '系统通知消息表';
```

### 用户消息表

```sql
CREATE TABLE sys_user_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    message_id BIGINT NOT NULL COMMENT '消息 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    is_read BIT DEFAULT 0 COMMENT '是否已读',
    read_time DATETIME COMMENT '阅读时间',
    is_deleted BIT DEFAULT 0 COMMENT '是否删除',
    delete_time DATETIME COMMENT '删除时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_message_user (message_id, user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time)
) COMMENT '用户消息表';
```

## 通知类型

### 系统通知类型

| 类型 | 编码 | 说明 |
|------|------|------|
| 系统通知 | SYSTEM | 系统升级、维护通知 |
| 安全通知 | SECURITY | 登录异常、密码修改等 |
| 业务通知 | BUSINESS | 审批、待办等业务通知 |
| 活动通知 | ACTIVITY | 系统活动、培训通知 |

### 消息优先级

| 优先级 | 编码 | 说明 | 推送方式 |
|--------|------|------|----------|
| 低 | low | 一般消息 | 站内信 |
| 普通 | normal | 普通消息 | 站内信 + 角标 |
| 高 | high | 重要消息 | 站内信 + 弹窗 |
| 紧急 | urgent | 紧急消息 | 全量推送 + 短信 |

## 消息发送

### 发送通知消息

```java
@Autowired
private SysNoticeService noticeService;

@Autowired
private WebSocketNotificationService notificationService;

// 发送系统通知
NoticeMessage message = new NoticeMessage();
message.setTitle("系统维护通知");
message.setContent("系统将于今晚 23:00-24:00 进行维护升级...");
message.setNoticeType("NOTICE");
message.setPriority("high");
message.setTargetType("all");
message.setPublishTime(LocalDateTime.now());
noticeService.publishNotice(message);

// 推送给特定用户
List<Long> userIds = Arrays.asList(1L, 2L, 3L);
noticeService.sendToUsers(message, userIds);

// 推送给特定角色
noticeService.sendToRoles(message, Arrays.asList("admin", "manager"));

// 推送给特定部门
noticeService.sendToDepts(message, Arrays.asList(100L, 101L));
```

### WebSocket 实时推送

```java
@Autowired
private WebSocketNotificationService notificationService;

// 发送消息给指定用户
notificationService.sendToUser(userId, "您有一条新的待办事项", data);

// 发送消息给所有在线用户
notificationService.sendToAll("系统通知", data);

// 发送消息给指定 Topic
notificationService.sendToTopic("/topic/notice", noticeData);

// 发送用户消息
UserMessage userMessage = new UserMessage();
userMessage.setUserId(userId);
userMessage.setTitle("审批提醒");
userMessage.setContent("您有一个请假申请需要审批");
notificationService.sendUserMessage(userMessage);
```

### 批量发送

```java
// 批量发送通知
List<Long> userIds = getUserIdsByDept(100L);
noticeService.batchSend(message, userIds);

// 异步发送
noticeService.asyncSend(message, userIds);

// 定时发送
noticeService.scheduleSend(message, userIds, "0 0 9 * * ?");
```

## 消息管理

### 查询消息列表

```java
// 查询我的消息
Page<UserMessageVO> messages = noticeService.getMyMessages(userId, pageNum, pageSize);

// 查询未读消息
List<UserMessageVO> unread = noticeService.getUnreadMessages(userId);

// 查询已读消息
Page<UserMessageVO> read = noticeService.getReadMessages(userId, pageNum, pageSize);
```

### 标记已读

```java
// 标记单条消息已读
noticeService.markAsRead(messageId, userId);

// 批量标记已读
List<Long> messageIds = ...;
noticeService.markBatchAsRead(messageIds, userId);

// 标记全部已读
noticeService.markAllAsRead(userId);
```

### 删除消息

```java
// 删除单条消息
noticeService.deleteMessage(messageId, userId);

// 批量删除
noticeService.batchDelete(messageIds, userId);

// 清空消息
noticeService.clearMessages(userId);
```

### 消息统计

```java
// 获取未读数量
int unreadCount = noticeService.getUnreadCount(userId);

// 获取消息统计
MessageStats stats = noticeService.getMessageStats(userId);
// stats.getTotal() - 总消息数
// stats.getUnread() - 未读数
// stats.getToday() - 今日消息
```

## 消息模板

### 定义消息模板

```java
public enum MessageTemplate {

    // 审批通知
    APPROVAL_NOTIFY(
        "审批通知",
        "您有一个${businessType}申请需要审批",
        "/workflow/task/handle/${taskId}"
    ),

    // 审批结果
    APPROVAL_RESULT(
        "审批结果",
        "您的${businessType}申请已${result}",
        "/workflow/process/${processInstanceId}"
    ),

    // 系统通知
    SYSTEM_NOTICE(
        "系统通知",
        "${noticeContent}",
        "/system/notice/${noticeId}"
    ),

    // 任务分配
    TASK_ASSIGNED(
        "任务分配",
        "您被分配了一个新任务：${taskName}",
        "/system/task/${taskId}"
    ),

    // 密码修改
    PASSWORD_CHANGED(
        "密码修改提醒",
        "您的密码已于${modifyTime}修改成功",
        "/profile"
    ),

    // 账号锁定
    ACCOUNT_LOCKED(
        "账号锁定通知",
        "您的账号因${reason}已被锁定",
        "/auth/unlock"
    );

    private final String name;
    private final String template;
    private final String redirectUrl;
}
```

### 使用模板发送消息

```java
// 使用模板发送
Map<String, String> params = new HashMap<>();
params.put("businessType", "请假");
params.put("taskName", "张三的请假申请");
params.put("taskId", taskId.toString());

noticeService.sendByTemplate(
    userId,
    MessageTemplate.TASK_ASSIGNED,
    params
);
```

## 消息推送配置

### WebSocket 配置

```yaml
websocket:
  enabled: true
  path: /ws
  heartbeat: 30000
  max-sessions: 10000
  allowed-origins: "*"
```

### 推送策略

```yaml
push:
  # 在线用户推送策略
  online:
    enabled: true
    retry-count: 3
    retry-interval: 1000

  # 离线用户推送策略
  offline:
    enabled: true
    # 离线消息保留时间（天）
    retention-days: 30
    # 是否聚合消息
    aggregate: true
    # 聚合时间窗口（分钟）
    aggregate-window: 5

  # 邮件通知
  email:
    enabled: true
    # 高优先级才发送邮件
    min-priority: high

  # 短信通知
  sms:
    enabled: false
    # 仅紧急消息发送短信
    min-priority: urgent
```

## 消息处理

### 消息处理器

```java
@Component
public class MessageHandler {

    @Autowired
    private NoticeService noticeService;

    /**
     * 处理审批通过消息
     */
    @Async
    public void handleApprovalPass(Long taskId, Long userId) {
        // 1. 保存消息
        UserMessage message = new UserMessage();
        message.setUserId(userId);
        message.setTitle("审批通过");
        message.setContent("您的申请已通过审批");
        message.setMessageType("BUSINESS");
        noticeService.saveMessage(message);

        // 2. 推送消息
        notificationService.sendToUser(userId, "审批通过", message);

        // 3. 记录日志
        log.info("发送审批通过消息给用户：{}", userId);
    }

    /**
     * 处理任务分配消息
     */
    @Async
    public void handleTaskAssigned(Long taskId, Long userId) {
        // 实现任务分配消息处理
    }
}
```

### 消息接收端

```javascript
// WebSocket 连接
const ws = new WebSocket('ws://localhost:8080/ws')

ws.onopen = () => {
  console.log('WebSocket 已连接')
  // 发送认证
  ws.send(JSON.stringify({
    type: 'auth',
    token: getToken()
  }))
}

ws.onmessage = (event) => {
  const message = JSON.parse(event.data)
  console.log('收到消息:', message)

  // 显示通知
  if (message.type === 'NOTICE') {
    showNotification(message.title, message.content)
    // 播放提示音
    playNotificationSound()
    // 更新角标
    updateBadgeCount()
  }
}

ws.onerror = (error) => {
  console.error('WebSocket 错误:', error)
}

ws.onclose = () => {
  console.log('WebSocket 已关闭')
  // 尝试重连
  reconnect()
}
```

## 前端实现

### 消息组件

```vue
<template>
  <div class="message-box">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0">
      <el-dropdown trigger="click">
        <el-icon><Bell /></el-icon>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>
              <div class="message-header">
                <span>消息中心</span>
                <el-button link @click="markAllAsRead">全部已读</el-button>
              </div>
            </el-dropdown-item>
            <el-dropdown-item v-for="msg in messages" :key="msg.id">
              <div class="message-item" :class="{ unread: !msg.isRead }">
                <div class="message-title">{{ msg.title }}</div>
                <div class="message-content">{{ msg.content }}</div>
                <div class="message-time">{{ formatTime(msg.createTime) }}</div>
              </div>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </el-badge>
  </div>
</template>

<script setup>
import { useMessageStore } from '@/store/message'

const messageStore = useMessageStore()
const unreadCount = computed(() => messageStore.unreadCount)
const messages = computed(() => messageStore.messages)

const markAllAsRead = () => {
  messageStore.markAllAsRead()
}
</script>
```

### 消息 Store

```javascript
// store/message.js
export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0)
  const messages = ref([])
  const ws = ref(null)

  // 连接 WebSocket
  const connectWebSocket = () => {
    ws.value = new WebSocket(wsUrl)
    ws.value.onmessage = (event) => {
      const message = JSON.parse(event.data)
      handleMessage(message)
    }
  }

  // 处理消息
  const handleMessage = (message) => {
    if (message.type === 'UNREAD_COUNT') {
      unreadCount.value = message.data
    } else if (message.type === 'NEW_MESSAGE') {
      messages.value.unshift(message.data)
      unreadCount.value++
      showNotification(message.data.title, message.data.content)
    }
  }

  // 标记已读
  const markAsRead = (messageId) => {
    api.markAsRead(messageId).then(() => {
      unreadCount.value--
    })
  }

  const markAllAsRead = () => {
    api.markAllAsRead().then(() => {
      unreadCount.value = 0
    })
  }

  return {
    unreadCount,
    messages,
    connectWebSocket,
    markAsRead,
    markAllAsRead
  }
})
```

## 最佳实践

### 1. 消息去重

```java
public void sendMessage(Long userId, String title, String content) {
    // 检查最近是否发送过相同消息
    UserMessage lastMessage = messageMapper.findLastMessage(userId, title, content);
    if (lastMessage != null &&
        System.currentTimeMillis() - lastMessage.getCreateTime().getTime() < 60000) {
        log.warn("消息重复，跳过发送");
        return;
    }
    // 发送消息
}
```

### 2. 消息聚合

```java
public void sendBatchMessages(List<Long> userIds, NoticeMessage message) {
    // 按部门聚合
    Map<Long, List<Long>> deptUsers = groupByDept(userIds);
    for (Map.Entry<Long, List<Long>> entry : deptUsers.entrySet()) {
        NoticeMessage deptMessage = clone(message);
        deptMessage.setContent(message.getContent() +
            "（共 " + entry.getValue().size() + " 人）");
        noticeService.send(deptMessage, entry.getValue());
    }
}
```

### 3. 离线消息处理

```java
@Scheduled(fixedRate = 60000)
public void processOfflineMessages() {
    // 获取所有离线用户
    List<Long> offlineUsers = getOfflineUsers();

    // 检查是否有未推送的离线消息
    for (Long userId : offlineUsers) {
        List<UserMessage> messages = messageMapper.findUnreadMessages(userId);
        if (!messages.isEmpty()) {
            // 发送邮件通知
            emailService.sendDigestEmail(userId, messages);
        }
    }
}
```

### 4. 消息推送限流

```java
@Service
public class MessageRateLimiter {

    private final Cache<String, RateLimiter> limiters =
        Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    public boolean trySend(String userId) {
        RateLimiter limiter = limiters.get(userId, k ->
            RateLimiter.create(10, 1, TimeUnit.MINUTES));
        return limiter.tryAcquire();
    }
}
```
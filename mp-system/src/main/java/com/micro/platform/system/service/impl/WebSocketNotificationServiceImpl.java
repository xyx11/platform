package com.micro.platform.system.service.impl;

import com.micro.platform.system.service.WebSocketNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket 通知服务实现
 */
@Service
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketNotificationServiceImpl.class);

    private final SimpMessageSendingOperations messagingTemplate;
    private final SimpUserRegistry simpUserRegistry;

    public WebSocketNotificationServiceImpl(SimpMessageSendingOperations messagingTemplate,
                                            SimpUserRegistry simpUserRegistry) {
        this.messagingTemplate = messagingTemplate;
        this.simpUserRegistry = simpUserRegistry;
    }

    @Override
    public void sendTodoNotification(Long userId, String title, String message, String type) {
        log.info("发送待办通知给用户：{}, 标题：{}, 类型：{}", userId, title, type);

        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("type", type);
        notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 发送到用户特定的队列
        String destination = "/queue/notification." + userId;
        try {
            messagingTemplate.convertAndSend(destination, notification);
        } catch (Exception e) {
            log.error("发送通知失败：{}", destination, e);
        }
    }

    @Override
    public void sendWorkflowTaskNotification(Long userId, String taskId, String taskName, String action) {
        log.info("发送工作流任务通知给用户：{}, 任务：{}, 操作：{}", userId, taskId, action);

        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("taskId", taskId);
        notification.put("taskName", taskName);
        notification.put("action", action);
        notification.put("type", "WORKFLOW_TASK");
        notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 发送到用户特定的队列
        String destination = "/queue/notification." + userId;
        try {
            messagingTemplate.convertAndSend(destination, notification);
        } catch (Exception e) {
            log.error("发送工作流通知失败：{}", destination, e);
        }
    }

    @Override
    public void sendSystemNotification(Long userId, String title, String message) {
        log.info("发送系统通知给用户：{}, 标题：{}", userId, title);

        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("type", "SYSTEM");
        notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 发送到用户特定的队列
        String destination = "/queue/notification." + userId;
        try {
            messagingTemplate.convertAndSend(destination, notification);
        } catch (Exception e) {
            log.error("发送系统通知失败：{}", destination, e);
        }
    }

    @Override
    public void broadcastNotification(String title, String message, String type) {
        log.info("广播通知：{}, 类型：{}", title, type);

        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("message", message);
        notification.put("type", type);
        notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 广播到所有订阅的用户
        try {
            messagingTemplate.convertAndSend("/topic/notification", notification);
        } catch (Exception e) {
            log.error("广播通知失败", e);
        }
    }
}
package com.micro.platform.system.service;

import com.micro.platform.system.entity.WebSocketMessage;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WebSocket 消息推送服务
 */
@Service
public class WebSocketMessageService {

    private final SimpMessageSendingOperations messagingTemplate;

    public WebSocketMessageService(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 发送消息给指定用户
     */
    public void sendToUser(Long userId, WebSocketMessage message) {
        if (message.getId() == null) {
            message.setId(UUID.randomUUID().toString());
        }
        if (message.getSendTime() == null) {
            message.setSendTime(LocalDateTime.now());
        }
        if (message.getReceiverId() == null) {
            message.setReceiverId(userId);
        }
        if (message.getIsRead() == null) {
            message.setIsRead(false);
        }

        // 发送到用户专属队列
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/messages",
            message
        );
    }

    /**
     * 发送系统通知
     */
    public void sendSystemNotification(Long userId, String title, String content) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType(1);
        message.setTitle(title);
        message.setContent(content);
        sendToUser(userId, message);
    }

    /**
     * 发送待办提醒
     */
    public void sendTodoReminder(Long userId, String title, String content, Object data) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType(2);
        message.setTitle(title);
        message.setContent(content);
        message.setData(data);
        sendToUser(userId, message);
    }

    /**
     * 发送消息通知
     */
    public void sendMessageNotification(Long userId, String title, String content) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType(3);
        message.setTitle(title);
        message.setContent(content);
        sendToUser(userId, message);
    }

    /**
     * 发送预警提醒
     */
    public void sendAlertNotification(Long userId, String title, String content) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType(4);
        message.setTitle(title);
        message.setContent(content);
        sendToUser(userId, message);
    }

    /**
     * 广播消息给所有订阅的用户
     */
    public void broadcast(String destination, Object message) {
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * 广播系统通知给所有用户
     */
    public void broadcastSystemNotification(String title, String content) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType(1);
        message.setTitle(title);
        message.setContent(content);
        broadcast("/topic/system-notifications", message);
    }
}
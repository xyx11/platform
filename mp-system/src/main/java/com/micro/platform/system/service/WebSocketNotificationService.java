package com.micro.platform.system.service;

/**
 * WebSocket 通知服务
 */
public interface WebSocketNotificationService {

    /**
     * 发送待办事项通知
     */
    void sendTodoNotification(Long userId, String title, String message, String type);

    /**
     * 发送工作流任务通知
     */
    void sendWorkflowTaskNotification(Long userId, String taskId, String taskName, String action);

    /**
     * 发送系统消息通知
     */
    void sendSystemNotification(Long userId, String title, String message);

    /**
     * 广播通知给所有在线用户
     */
    void broadcastNotification(String title, String message, String type);
}
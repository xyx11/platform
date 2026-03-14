package com.micro.platform.common.core.service;

import java.util.List;

/**
 * 邮件通知服务
 */
public interface EmailNotificationService {

    /**
     * 发送待办事项提醒邮件
     */
    void sendTodoReminderEmail(String to, String title, String content, String dueTime);

    /**
     * 发送工作流任务通知邮件
     */
    void sendWorkflowTaskEmail(String to, String taskName, String action, String processName);

    /**
     * 发送简单邮件
     */
    void sendSimpleEmail(String to, String subject, String content);

    /**
     * 批量发送邮件
     */
    void batchSendEmail(List<String> recipients, String subject, String content);
}
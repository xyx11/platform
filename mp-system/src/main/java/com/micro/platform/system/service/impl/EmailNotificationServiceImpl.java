package com.micro.platform.system.service.impl;

import com.micro.platform.common.core.service.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;

/**
 * 邮件通知服务实现
 */
@Service
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public EmailNotificationServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendTodoReminderEmail(String to, String title, String content, String dueTime) {
        log.info("发送待办提醒邮件到：{}, 标题：{}, 截止时间：{}", to, title, dueTime);

        String subject = "[待办提醒] " + title;
        String htmlContent = buildTodoReminderHtml(title, content, dueTime);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("待办提醒邮件发送成功：{}", to);
        } catch (MessagingException e) {
            log.error("发送待办提醒邮件失败：{}", to, e);
        }
    }

    @Override
    @Async
    public void sendWorkflowTaskEmail(String to, String taskName, String action, String processName) {
        log.info("发送工作流任务邮件到：{}, 任务：{}, 操作：{}", to, taskName, action);

        String subject = "[工作流通知] " + taskName + " - " + action;
        String htmlContent = buildWorkflowTaskHtml(taskName, action, processName);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("工作流任务邮件发送成功：{}", to);
        } catch (MessagingException e) {
            log.error("发送工作流任务邮件失败：{}", to, e);
        }
    }

    @Override
    @Async
    public void sendSimpleEmail(String to, String subject, String content) {
        log.info("发送简单邮件到：{}, 主题：{}", to, subject);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("简单邮件发送成功：{}", to);
        } catch (Exception e) {
            log.error("发送简单邮件失败：{}", to, e);
        }
    }

    @Override
    @Async
    public void batchSendEmail(List<String> recipients, String subject, String content) {
        log.info("批量发送邮件，收件人数量：{}", recipients.size());

        for (String to : recipients) {
            try {
                sendSimpleEmail(to, subject, content);
            } catch (Exception e) {
                log.error("批量发送邮件失败，收件人：{}", to, e);
            }
        }
    }

    /**
     * 构建待办提醒邮件 HTML
     */
    private String buildTodoReminderHtml(String title, String content, String dueTime) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #4CAF50; color: white; padding: 15px; border-radius: 5px 5px 0 0; }" +
                ".content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }" +
                ".footer { background-color: #f1f1f1; padding: 10px; text-align: center; font-size: 12px; }" +
                ".deadline { color: #f44336; font-weight: bold; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'><h2>待办事项提醒</h2></div>" +
                "<div class='content'>" +
                "<p><strong>标题：</strong>" + title + "</p>" +
                "<p><strong>内容：</strong>" + content + "</p>" +
                "<p class='deadline'>截止时间：" + dueTime + "</p>" +
                "<p>请及时处理您的待办事项。</p>" +
                "</div>" +
                "<div class='footer'>此邮件由系统自动发送，请勿回复。</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * 构建工作流任务邮件 HTML
     */
    private String buildWorkflowTaskHtml(String taskName, String action, String processName) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #2196F3; color: white; padding: 15px; border-radius: 5px 5px 0 0; }" +
                ".content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }" +
                ".footer { background-color: #f1f1f1; padding: 10px; text-align: center; font-size: 12px; }" +
                ".action { color: #2196F3; font-weight: bold; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'><h2>工作流任务通知</h2></div>" +
                "<div class='content'>" +
                "<p><strong>任务名称：</strong>" + taskName + "</p>" +
                "<p><strong>操作流程：</strong>" + processName + "</p>" +
                "<p class='action'>操作类型：" + action + "</p>" +
                "<p>请及时处理您的工作流任务。</p>" +
                "</div>" +
                "<div class='footer'>此邮件由系统自动发送，请勿回复。</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
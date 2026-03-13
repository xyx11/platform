package com.micro.platform.system.controller;

import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.service.EmailNotificationService;
import com.micro.platform.system.service.WebSocketNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知管理控制器
 */
@Tag(name = "通知管理", description = "WebSocket 和邮件通知")
@RestController
@RequestMapping("/system/notification")
public class NotificationController {

    private final WebSocketNotificationService webSocketNotificationService;

    @Autowired(required = false)
    private EmailNotificationService emailNotificationService;

    public NotificationController(WebSocketNotificationService webSocketNotificationService) {
        this.webSocketNotificationService = webSocketNotificationService;
    }

    @Operation(summary = "发送测试 WebSocket 通知")
    @PreAuthorize("hasAuthority('system:notification:send')")
    @PostMapping("/websocket/test")
    public Result<Void> sendWebSocketTest() {
        Long userId = SecurityUtil.getUserId();
        webSocketNotificationService.sendTodoNotification(
                userId,
                "测试通知",
                "这是一条测试 WebSocket 通知消息",
                "TEST"
        );
        return Result.success();
    }

    @Operation(summary = "发送测试邮件")
    @PreAuthorize("hasAuthority('system:notification:send')")
    @PostMapping("/email/test")
    public Result<Void> sendEmailTest(@RequestParam String email) {
        if (emailNotificationService == null) {
            return Result.error("邮件服务未启用");
        }
        emailNotificationService.sendSimpleEmail(
                email,
                "测试邮件",
                "这是一条测试邮件通知，用于验证邮件发送功能是否正常。"
        );
        return Result.success();
    }

    @Operation(summary = "手动触发待办提醒")
    @PreAuthorize("hasAuthority('system:notification:send')")
    @PostMapping("/todo/remind")
    public Result<Void> sendTodoReminder(@RequestParam Long todoId) {
        // 这里可以根据待办 ID 获取详情并发送通知
        // 简化处理，发送通用提醒
        Long userId = SecurityUtil.getUserId();
        webSocketNotificationService.sendTodoNotification(
                userId,
                "待办提醒",
                "您有待处理的事项，请及时处理",
                "REMINDER"
        );
        return Result.success();
    }

    @Operation(summary = "获取通知配置")
    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("websocketEnabled", true);
        config.put("emailEnabled", emailNotificationService != null);
        config.put("reminderTime", "09:00");
        return Result.success(config);
    }
}
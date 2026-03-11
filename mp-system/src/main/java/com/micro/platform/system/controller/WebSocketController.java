package com.micro.platform.system.controller;

import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.service.WebSocketMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket 消息推送 Controller
 */
@RestController
@RequestMapping("/system/ws")
@Tag(name = "WebSocket 消息推送")
public class WebSocketController {

    private final WebSocketMessageService messageService;

    public WebSocketController(WebSocketMessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    @Operation(summary = "发送消息给指定用户")
    @PreAuthorize("@ss.hasPermission('system:ws:send')")
    public Result<Void> sendToUser(@RequestParam Long userId,
                                    @RequestParam String title,
                                    @RequestParam String content,
                                    @RequestParam(required = false) Integer type) {
        if (type == null) {
            type = 3; // 默认消息通知
        }
        final Integer messageType = type;
        messageService.sendToUser(userId, new com.micro.platform.system.entity.WebSocketMessage() {{
            setType(messageType);
            setTitle(title);
            setContent(content);
        }});
        return Result.success();
    }

    @PostMapping("/send/system")
    @Operation(summary = "发送系统通知")
    @PreAuthorize("@ss.hasPermission('system:ws:send')")
    public Result<Void> sendSystemNotification(@RequestParam Long userId,
                                                @RequestParam String title,
                                                @RequestParam String content) {
        messageService.sendSystemNotification(userId, title, content);
        return Result.success();
    }

    @PostMapping("/send/todo")
    @Operation(summary = "发送待办提醒")
    @PreAuthorize("@ss.hasPermission('system:ws:send')")
    public Result<Void> sendTodoReminder(@RequestParam Long userId,
                                          @RequestParam String title,
                                          @RequestParam String content) {
        messageService.sendTodoReminder(userId, title, content, null);
        return Result.success();
    }

    @PostMapping("/send/alert")
    @Operation(summary = "发送预警提醒")
    @PreAuthorize("@ss.hasPermission('system:ws:send')")
    public Result<Void> sendAlertNotification(@RequestParam Long userId,
                                               @RequestParam String title,
                                               @RequestParam String content) {
        messageService.sendAlertNotification(userId, title, content);
        return Result.success();
    }

    @PostMapping("/broadcast")
    @Operation(summary = "广播消息")
    @PreAuthorize("@ss.hasPermission('system:ws:broadcast')")
    public Result<Void> broadcast(@RequestParam String title,
                                   @RequestParam String content) {
        messageService.broadcastSystemNotification(title, content);
        return Result.success();
    }

    @GetMapping("/endpoint")
    @Operation(summary = "获取 WebSocket 端点信息")
    public Result<Map<String, Object>> getEndpointInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("endpoint", "/ws");
        info.put("topicPrefix", "/topic");
        info.put("queuePrefix", "/queue");
        info.put("userPrefix", "/user");
        info.put("appPrefix", "/app");
        Long userId = SecurityUtil.getUserId();
        if (userId != null) {
            info.put("currentUserId", userId);
            info.put("userQueueDestination", "/user/" + userId + "/queue/messages");
        }
        return Result.success(info);
    }
}
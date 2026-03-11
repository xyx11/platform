package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.PageResult;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysMessage;
import com.micro.platform.system.service.SysMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息中心 Controller
 */
@RestController
@RequestMapping("/system/message")
@Tag(name = "消息中心管理")
public class SysMessageController {

    private final SysMessageService messageService;

    public SysMessageController(SysMessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/list")
    @Operation(summary = "查询消息列表")
    @PreAuthorize("@ss.hasPermission('system:message:list')")
    @OperationLog(module = "消息中心", type = OperationType.SELECT)
    public Result<PageResult<SysMessage>> list(SysMessage message,
                                                @RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysMessage> page = messageService.selectMessagePage(message, pageNum, pageSize);
        PageResult<SysMessage> result = PageResult.build(page);
        return Result.success(result);
    }

    @GetMapping("/my-messages")
    @Operation(summary = "获取我的消息")
    @OperationLog(module = "消息中心", type = OperationType.SELECT)
    public Result<PageResult<SysMessage>> myMessages(@RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtil.getUserId();
        Page<SysMessage> page = messageService.selectUserMessages(userId, pageNum, pageSize);
        PageResult<SysMessage> result = PageResult.build(page);
        return Result.success(result);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读消息数量")
    public Result<Integer> unreadCount() {
        Long userId = SecurityUtil.getUserId();
        int count = messageService.countUnreadMessages(userId);
        return Result.success(count);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取消息详情")
    @PreAuthorize("@ss.hasPermission('system:message:query')")
    @OperationLog(module = "消息中心", type = OperationType.SELECT)
    public Result<SysMessage> get(@PathVariable Long id) {
        SysMessage message = messageService.getById(id);
        return Result.success(message);
    }

    @PostMapping("/send")
    @Operation(summary = "发送消息")
    @PreAuthorize("@ss.hasPermission('system:message:add')")
    @OperationLog(module = "消息中心", type = OperationType.INSERT)
    public Result<Void> send(@RequestBody SysMessage message) {
        messageService.sendMessage(message);
        return Result.success();
    }

    @PostMapping("/batch-send")
    @Operation(summary = "批量发送消息")
    @PreAuthorize("@ss.hasPermission('system:message:add')")
    @OperationLog(module = "消息中心", type = OperationType.INSERT)
    public Result<Void> batchSend(@RequestBody Map<String, Object> params) {
        SysMessage message = (SysMessage) params.get("message");
        @SuppressWarnings("unchecked")
        List<Long> receiverIds = (List<Long>) params.get("receiverIds");
        messageService.batchSendMessage(message, receiverIds);
        return Result.success();
    }

    @PostMapping("/mark-read/{id}")
    @Operation(summary = "标记消息为已读")
    @OperationLog(module = "消息中心", type = OperationType.UPDATE)
    public Result<Void> markRead(@PathVariable Long id) {
        Long userId = SecurityUtil.getUserId();
        messageService.markAsRead(id, userId);
        return Result.success();
    }

    @PostMapping("/batch-mark-read")
    @Operation(summary = "批量标记消息为已读")
    @OperationLog(module = "消息中心", type = OperationType.UPDATE)
    public Result<Void> batchMarkRead(@RequestBody List<Long> messageIds) {
        Long userId = SecurityUtil.getUserId();
        messageService.batchMarkAsRead(messageIds, userId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除消息")
    @PreAuthorize("@ss.hasPermission('system:message:remove')")
    @OperationLog(module = "消息中心", type = OperationType.DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.getUserId();
        messageService.deleteMessage(id, userId);
        return Result.success();
    }

    @PostMapping("/withdraw/{id}")
    @Operation(summary = "撤回消息")
    @PreAuthorize("@ss.hasPermission('system:message:withdraw')")
    @OperationLog(module = "消息中心", type = OperationType.UPDATE)
    public Result<Void> withdraw(@PathVariable Long id) {
        messageService.withdrawMessage(id);
        return Result.success();
    }

    @GetMapping("/stats")
    @Operation(summary = "获取消息统计")
    @OperationLog(module = "消息中心", type = OperationType.SELECT)
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = messageService.getMessageStats();
        return Result.success(stats);
    }
}
package com.micro.platform.system.controller;

import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.SysTaskComment;
import com.micro.platform.system.service.SysTaskCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务评论控制器
 */
@Tag(name = "任务评论", description = "任务评论管理")
@RestController
@RequestMapping("/system/todo/comment")
public class SysTaskCommentController {

    private final SysTaskCommentService sysTaskCommentService;

    public SysTaskCommentController(SysTaskCommentService sysTaskCommentService) {
        this.sysTaskCommentService = sysTaskCommentService;
    }

    @Operation(summary = "获取任务评论列表")
    @GetMapping("/list")
    public Result<List<SysTaskComment>> list(@RequestParam Long todoId) {
        return Result.success(sysTaskCommentService.getCommentsByTodoId(todoId));
    }

    @Operation(summary = "获取评论数量")
    @GetMapping("/count")
    public Result<Integer> count(@RequestParam Long todoId) {
        return Result.success(sysTaskCommentService.getCommentCount(todoId));
    }

    @Operation(summary = "添加评论")
    @OperationLog(module = "任务评论", type = OperationType.INSERT, description = "添加评论")
    @PreAuthorize("hasAuthority('system:todo:edit')")
    @PostMapping
    public Result<SysTaskComment> add(@RequestBody SysTaskComment comment) {
        return Result.success(sysTaskCommentService.addComment(comment));
    }

    @Operation(summary = "修改评论")
    @OperationLog(module = "任务评论", type = OperationType.UPDATE, description = "修改评论")
    @PreAuthorize("hasAuthority('system:todo:edit')")
    @PutMapping
    public Result<Void> edit(@RequestBody SysTaskComment comment) {
        sysTaskCommentService.updateComment(comment);
        return Result.success();
    }

    @Operation(summary = "删除评论")
    @OperationLog(module = "任务评论", type = OperationType.DELETE, description = "删除评论")
    @PreAuthorize("hasAuthority('system:todo:remove')")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        sysTaskCommentService.deleteComment(id);
        return Result.success();
    }
}
package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.SysTodoRecycleBin;
import com.micro.platform.system.service.SysTodoRecycleBinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 任务回收站控制器
 */
@Tag(name = "任务回收站", description = "任务回收站管理")
@RestController
@RequestMapping("/system/todo/recycle-bin")
public class SysTodoRecycleBinController {

    private final SysTodoRecycleBinService sysTodoRecycleBinService;

    public SysTodoRecycleBinController(SysTodoRecycleBinService sysTodoRecycleBinService) {
        this.sysTodoRecycleBinService = sysTodoRecycleBinService;
    }

    @Operation(summary = "获取回收站列表")
    @GetMapping("/list")
    public Result<Page<SysTodoRecycleBin>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(sysTodoRecycleBinService.getRecycleBinPage(pageNum, pageSize));
    }

    @Operation(summary = "移动到回收站")
    @OperationLog(module = "任务回收站", type = OperationType.DELETE, description = "移动到回收站")
    @PreAuthorize("hasAuthority('system:todo:remove')")
    @PostMapping("/move/{todoId}")
    public Result<Void> moveToRecycleBin(@PathVariable Long todoId) {
        sysTodoRecycleBinService.moveToRecycleBin(todoId);
        return Result.success();
    }

    @Operation(summary = "恢复待办")
    @OperationLog(module = "任务回收站", type = OperationType.UPDATE, description = "恢复待办")
    @PreAuthorize("hasAuthority('system:todo:edit')")
    @PostMapping("/recover/{recycleId}")
    public Result<Void> recoverTodo(@PathVariable Long recycleId) {
        sysTodoRecycleBinService.recoverTodo(recycleId);
        return Result.success();
    }

    @Operation(summary = "永久删除")
    @OperationLog(module = "任务回收站", type = OperationType.DELETE, description = "永久删除")
    @PreAuthorize("hasAuthority('system:todo:remove')")
    @DeleteMapping("/{recycleId}")
    public Result<Void> deletePermanently(@PathVariable Long recycleId) {
        sysTodoRecycleBinService.deletePermanently(recycleId);
        return Result.success();
    }

    @Operation(summary = "清空回收站")
    @OperationLog(module = "任务回收站", type = OperationType.DELETE, description = "清空回收站")
    @PreAuthorize("hasAuthority('system:todo:remove')")
    @DeleteMapping("/clear")
    public Result<Void> clearRecycleBin() {
        sysTodoRecycleBinService.clearRecycleBin();
        return Result.success();
    }
}
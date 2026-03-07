package com.micro.platform.job.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.job.entity.SysJobLog;
import com.micro.platform.job.service.SysJobLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 定时任务日志控制器
 */
@Tag(name = "定时任务日志", description = "定时任务日志管理")
@RestController
@RequestMapping("/system/job-log")
public class SysJobLogController {

    private final SysJobLogService sysJobLogService;

    public SysJobLogController(SysJobLogService sysJobLogService) {
        this.sysJobLogService = sysJobLogService;
    }

    @Operation(summary = "获取定时任务日志列表")
    @PreAuthorize("hasAuthority('system:job:query')")
    @GetMapping("/list")
    public Result<Page<SysJobLog>> list(SysJobLog jobLog,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysJobLog> page = sysJobLogService.selectJobLogPage(jobLog, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取定时任务日志详情")
    @PreAuthorize("hasAuthority('system:job:query')")
    @GetMapping("/{id}")
    public Result<SysJobLog> get(@PathVariable Long id) {
        return Result.success(sysJobLogService.getById(id));
    }

    @Operation(summary = "删除定时任务日志")
    @PreAuthorize("hasAuthority('system:job:remove')")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        sysJobLogService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "清空定时任务日志")
    @PreAuthorize("hasAuthority('system:job:remove')")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        sysJobLogService.clean();
        return Result.success();
    }
}
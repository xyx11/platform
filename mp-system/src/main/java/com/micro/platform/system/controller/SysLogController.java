package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.system.entity.SysOperationLog;
import com.micro.platform.system.service.SysOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 */
@Tag(name = "操作日志", description = "操作日志管理")
@RestController
@RequestMapping("/system/log")
public class SysLogController {

    private final SysOperationLogService sysOperationLogService;

    public SysLogController(SysOperationLogService sysOperationLogService) {
        this.sysOperationLogService = sysOperationLogService;
    }

    @Operation(summary = "获取操作日志列表")
    @PreAuthorize("hasAuthority('system:log:query')")
    @GetMapping("/list")
    public Result<Page<SysOperationLog>> list(SysOperationLog log,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysOperationLog> page = sysOperationLogService.selectOperationLogPage(log, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取操作日志详情")
    @PreAuthorize("hasAuthority('system:log:query')")
    @GetMapping("/{id}")
    public Result<SysOperationLog> get(@PathVariable Long id) {
        return Result.success(sysOperationLogService.getById(id));
    }

    @Operation(summary = "删除操作日志")
    @PreAuthorize("hasAuthority('system:log:remove')")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        sysOperationLogService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "清空操作日志")
    @PreAuthorize("hasAuthority('system:log:remove')")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        sysOperationLogService.clean();
        return Result.success();
    }
}
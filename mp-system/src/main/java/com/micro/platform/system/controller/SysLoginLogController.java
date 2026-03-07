package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.system.entity.SysLoginLog;
import com.micro.platform.system.service.SysLoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 登录日志控制器
 */
@Tag(name = "登录日志", description = "登录日志管理")
@RestController
@RequestMapping("/system/loginlog")
public class SysLoginLogController {

    private final SysLoginLogService sysLoginLogService;

    public SysLoginLogController(SysLoginLogService sysLoginLogService) {
        this.sysLoginLogService = sysLoginLogService;
    }

    @Operation(summary = "获取登录日志列表")
    @PreAuthorize("hasAuthority('system:loginlog:query')")
    @GetMapping("/list")
    public Result<Page<SysLoginLog>> list(SysLoginLog log,
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysLoginLog> page = sysLoginLogService.selectLoginLogPage(log, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "删除登录日志")
    @PreAuthorize("hasAuthority('system:loginlog:remove')")
    @DeleteMapping("/{logId}")
    public Result<Void> remove(@PathVariable Long logId) {
        sysLoginLogService.removeById(logId);
        return Result.success();
    }

    @Operation(summary = "清空登录日志")
    @PreAuthorize("hasAuthority('system:loginlog:remove')")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        sysLoginLogService.clean();
        return Result.success();
    }
}
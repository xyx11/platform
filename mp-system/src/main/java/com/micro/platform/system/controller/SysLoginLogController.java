package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.SysLoginLog;
import com.micro.platform.system.service.SysLoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import java.net.URLEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @Operation(summary = "批量删除登录日志")
    @OperationLog(module = "登录日志", type = OperationType.DELETE, description = "批量删除登录日志")
    @PreAuthorize("hasAuthority('system:loginlog:remove')")
    @DeleteMapping("/batch")
    public Result<Void> batchRemove(@RequestBody List<Long> logIds) {
        sysLoginLogService.removeByIds(logIds);
        return Result.success();
    }

    @Operation(summary = "清空登录日志")
    @OperationLog(module = "登录日志", type = OperationType.CLEAN, description = "清空登录日志")
    @PreAuthorize("hasAuthority('system:loginlog:remove')")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        sysLoginLogService.clean();
        return Result.success();
    }

    @Operation(summary = "导出登录日志")
    @OperationLog(module = "登录日志", type = OperationType.EXPORT, description = "导出登录日志")
    @PreAuthorize("hasAuthority('system:loginlog:query')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(SysLoginLog log) throws Exception {
        byte[] data = sysLoginLogService.exportLoginLog(log);
        String fileName = URLEncoder.encode("登录日志", "UTF-8").replaceAll("\\\\+", "%20");
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @Operation(summary = "获取登录日志统计信息")
    @PreAuthorize("hasAuthority('system:loginlog:query')")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(sysLoginLogService.getLoginLogStats());
    }
}
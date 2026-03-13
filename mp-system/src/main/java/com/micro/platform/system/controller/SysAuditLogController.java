package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.PageResult;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.SysAuditLog;
import com.micro.platform.system.service.SysAuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 审计日志 Controller
 */
@RestController
@RequestMapping("/system/audit-log")
@Tag(name = "审计日志管理")
public class SysAuditLogController {

    private final SysAuditLogService auditLogService;

    public SysAuditLogController(SysAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/list")
    @Operation(summary = "查询审计日志列表")
    @PreAuthorize("@ss.hasPermission('system:audit-log:list')")
    @OperationLog(module = "审计日志", type = OperationType.QUERY)
    public Result<PageResult<SysAuditLog>> list(SysAuditLog auditLog,
                                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysAuditLog> page = auditLogService.selectAuditLogPage(auditLog, pageNum, pageSize);
        PageResult<SysAuditLog> result = PageResult.build(page);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取审计日志详情")
    @PreAuthorize("@ss.hasPermission('system:audit-log:query')")
    @OperationLog(module = "审计日志", type = OperationType.QUERY)
    public Result<SysAuditLog> get(@PathVariable Long id) {
        SysAuditLog auditLog = auditLogService.getById(id);
        return Result.success(auditLog);
    }

    @GetMapping("/table/{tableName}/record/{recordId}")
    @Operation(summary = "查询指定记录的操作历史")
    @PreAuthorize("@ss.hasPermission('system:audit-log:query')")
    @OperationLog(module = "审计日志", type = OperationType.QUERY)
    public Result<List<SysAuditLog>> getByTableAndRecord(@PathVariable String tableName,
                                                          @PathVariable Long recordId) {
        List<SysAuditLog> logs = auditLogService.selectAuditLogByTable(tableName, recordId);
        return Result.success(logs);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户的操作日志")
    @PreAuthorize("@ss.hasPermission('system:audit-log:query')")
    @OperationLog(module = "审计日志", type = OperationType.QUERY)
    public Result<List<SysAuditLog>> getByUser(@PathVariable Long userId,
                                                @RequestParam(required = false) Integer limit) {
        List<SysAuditLog> logs = auditLogService.selectAuditLogByUser(userId, limit);
        return Result.success(logs);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取审计统计信息")
    @PreAuthorize("@ss.hasPermission('system:audit-log:query')")
    @OperationLog(module = "审计日志", type = OperationType.QUERY)
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = auditLogService.getAuditStats();
        return Result.success(stats);
    }

    @DeleteMapping("/{ids}")
    @Operation(summary = "删除审计日志")
    @PreAuthorize("@ss.hasPermission('system:audit-log:remove')")
    @OperationLog(module = "审计日志", type = OperationType.DELETE)
    public Result<Void> delete(@PathVariable List<Long> ids) {
        auditLogService.batchDelete(ids);
        return Result.success();
    }

    @DeleteMapping("/clear")
    @Operation(summary = "清空审计日志")
    @PreAuthorize("@ss.hasPermission('system:audit-log:clear')")
    @OperationLog(module = "审计日志", type = OperationType.CLEAN)
    public Result<Void> clear() {
        auditLogService.clear();
        return Result.success();
    }
}
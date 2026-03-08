package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.SysCommandLog;
import com.micro.platform.system.service.SysCommandLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 命令执行控制器
 */
@Tag(name = "命令执行", description = "命令执行管理")
@RestController
@RequestMapping("/system/command")
public class SysCommandController {

    private final SysCommandLogService sysCommandLogService;

    public SysCommandController(SysCommandLogService sysCommandLogService) {
        this.sysCommandLogService = sysCommandLogService;
    }

    @Operation(summary = "获取命令执行记录列表")
    @PreAuthorize("hasAuthority('system:command:query')")
    @GetMapping("/list")
    public Result<Page<SysCommandLog>> list(SysCommandLog log,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysCommandLog> page = sysCommandLogService.selectCommandLogPage(log, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取命令执行记录详情")
    @PreAuthorize("hasAuthority('system:command:query')")
    @GetMapping("/{id}")
    public Result<SysCommandLog> get(@PathVariable Long id) {
        return Result.success(sysCommandLogService.getById(id));
    }

    @Operation(summary = "执行命令")
    @OperationLog(module = "命令执行", type = OperationType.OTHER, description = "执行命令")
    @PreAuthorize("hasAuthority('system:command:execute')")
    @PostMapping("/execute")
    public Result<String> execute(@RequestBody Map<String, String> params) {
        String commandType = params.get("commandType");
        String command = params.get("command");
        try {
            String result = sysCommandLogService.executeCommand(commandType, command);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("命令执行失败：" + e.getMessage());
        }
    }

    @Operation(summary = "删除命令执行记录")
    @OperationLog(module = "命令执行", type = OperationType.DELETE, description = "删除命令执行记录")
    @PreAuthorize("hasAuthority('system:command:remove')")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        sysCommandLogService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "批量删除命令执行记录")
    @OperationLog(module = "命令执行", type = OperationType.DELETE, description = "批量删除命令执行记录")
    @PreAuthorize("hasAuthority('system:command:remove')")
    @DeleteMapping("/batch")
    public Result<Void> batchRemove(@RequestBody List<Long> ids) {
        sysCommandLogService.removeByIds(ids);
        return Result.success();
    }

    @Operation(summary = "清空命令执行记录")
    @OperationLog(module = "命令执行", type = OperationType.CLEAN, description = "清空命令执行记录")
    @PreAuthorize("hasAuthority('system:command:remove')")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        sysCommandLogService.clean();
        return Result.success();
    }
}
package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysConfig;
import com.micro.platform.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 参数配置控制器
 */
@Tag(name = "参数配置", description = "参数配置管理")
@RestController
@RequestMapping("/system/config")
public class SysConfigController {

    private final SysConfigService sysConfigService;

    public SysConfigController(SysConfigService sysConfigService) {
        this.sysConfigService = sysConfigService;
    }

    @Operation(summary = "获取参数配置列表")
    @PreAuthorize("hasAuthority('system:config:query')")
    @GetMapping("/list")
    public Result<Page<SysConfig>> list(SysConfig config,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysConfig> page = sysConfigService.selectConfigPage(config, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取参数配置详情")
    @PreAuthorize("hasAuthority('system:config:query')")
    @GetMapping("/{configId}")
    public Result<SysConfig> get(@PathVariable Long configId) {
        return Result.success(sysConfigService.getById(configId));
    }

    @Operation(summary = "新增参数配置")
    @OperationLog(module = "参数配置", type = OperationType.CREATE, description = "新增参数配置")
    @PreAuthorize("hasAuthority('system:config:add')")
    @PostMapping
    public Result<Void> add(@RequestBody SysConfig config) {
        config.setCreateBy(SecurityUtil.getUserId());
        sysConfigService.saveConfig(config);
        return Result.success();
    }

    @Operation(summary = "修改参数配置")
    @OperationLog(module = "参数配置", type = OperationType.UPDATE, description = "修改参数配置")
    @PreAuthorize("hasAuthority('system:config:edit')")
    @PutMapping
    public Result<Void> update(@RequestBody SysConfig config) {
        config.setUpdateBy(SecurityUtil.getUserId());
        sysConfigService.updateConfig(config);
        return Result.success();
    }

    @Operation(summary = "删除参数配置")
    @OperationLog(module = "参数配置", type = OperationType.DELETE, description = "删除参数配置")
    @PreAuthorize("hasAuthority('system:config:remove')")
    @DeleteMapping("/{configId}")
    public Result<Void> remove(@PathVariable Long configId) {
        sysConfigService.removeById(configId);
        return Result.success();
    }

    @Operation(summary = "批量删除参数配置")
    @OperationLog(module = "参数配置", type = OperationType.DELETE, description = "批量删除参数配置")
    @PreAuthorize("hasAuthority('system:config:remove')")
    @DeleteMapping("/batch")
    public Result<Void> batchRemove(@RequestBody List<Long> configIds) {
        sysConfigService.removeByIds(configIds);
        return Result.success();
    }

    @Operation(summary = "刷新参数配置缓存")
    @OperationLog(module = "参数配置", type = OperationType.CLEAN, description = "刷新参数配置缓存")
    @PreAuthorize("hasAuthority('system:config:remove')")
    @DeleteMapping("/refreshCache")
    public Result<Void> refreshCache() {
        sysConfigService.refreshCache();
        return Result.success();
    }

    @Operation(summary = "根据键名获取参数值")
    @GetMapping("/configKey/{configKey}")
    public Result<SysConfig> getByKey(@PathVariable String configKey) {
        return Result.success(sysConfigService.selectConfigByKey(configKey));
    }
}
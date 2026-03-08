package com.micro.platform.job.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.job.entity.SysJob;
import com.micro.platform.job.service.SysJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 定时任务控制器
 */
@Tag(name = "定时任务", description = "定时任务管理")
@RestController
@RequestMapping("/system/job")
public class SysJobController {

    private final SysJobService sysJobService;

    public SysJobController(SysJobService sysJobService) {
        this.sysJobService = sysJobService;
    }

    @Operation(summary = "获取定时任务列表")
    @PreAuthorize("hasAuthority('system:job:query')")
    @GetMapping("/list")
    public Result<Page<SysJob>> list(SysJob job,
                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysJob> page = sysJobService.selectJobPage(job, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取定时任务详情")
    @PreAuthorize("hasAuthority('system:job:query')")
    @GetMapping("/{id}")
    public Result<SysJob> get(@PathVariable Long id) {
        return Result.success(sysJobService.getById(id));
    }

    @Operation(summary = "新增定时任务")
    @PreAuthorize("hasAuthority('system:job:add')")
    @PostMapping
    public Result<Void> add(@RequestBody SysJob job) {
        sysJobService.save(job);
        return Result.success();
    }

    @Operation(summary = "修改定时任务")
    @PreAuthorize("hasAuthority('system:job:edit')")
    @PutMapping
    public Result<Void> edit(@RequestBody SysJob job) {
        sysJobService.updateById(job);
        return Result.success();
    }

    @Operation(summary = "批量删除定时任务")
    @OperationLog(module = "定时任务", type = OperationType.DELETE, description = "批量删除定时任务")
    @PreAuthorize("hasAuthority('system:job:remove')")
    @DeleteMapping("/batch")
    public Result<Void> batchRemove(@RequestBody List<Long> ids) {
        sysJobService.batchDelete(ids);
        return Result.success();
    }

    @Operation(summary = "删除定时任务")
    @OperationLog(module = "定时任务", type = OperationType.DELETE, description = "删除定时任务")
    @PreAuthorize("hasAuthority('system:job:remove')")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        sysJobService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "立即执行一次任务")
    @PreAuthorize("hasAuthority('system:job:edit')")
    @PostMapping("/{id}/run")
    public Result<Void> run(@PathVariable Long id) {
        sysJobService.run(id);
        return Result.success();
    }

    @Operation(summary = "启动定时任务")
    @PreAuthorize("hasAuthority('system:job:edit')")
    @PostMapping("/{id}/start")
    public Result<Void> start(@PathVariable Long id) {
        sysJobService.start(id);
        return Result.success();
    }

    @Operation(summary = "停止定时任务")
    @PreAuthorize("hasAuthority('system:job:edit')")
    @PostMapping("/{id}/stop")
    public Result<Void> stop(@PathVariable Long id) {
        sysJobService.stop(id);
        return Result.success();
    }

    @Operation(summary = "导出定时任务")
    @PreAuthorize("hasAuthority('system:job:query')")
    @GetMapping("/export")
    public void export(HttpServletResponse response, SysJob job) {
        sysJobService.exportJob(response, job);
    }

    @Operation(summary = "获取任务统计信息")
    @PreAuthorize("hasAuthority('system:job:query')")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(sysJobService.getJobStats());
    }
}
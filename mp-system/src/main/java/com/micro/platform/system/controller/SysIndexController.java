package com.micro.platform.system.controller;

import com.micro.platform.common.core.result.Result;
import com.micro.platform.system.service.SysIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 首页统计控制器
 */
@Tag(name = "首页统计", description = "首页统计数据")
@RestController
@RequestMapping("/system/index")
public class SysIndexController {

    private final SysIndexService sysIndexService;

    public SysIndexController(SysIndexService sysIndexService) {
        this.sysIndexService = sysIndexService;
    }

    @Operation(summary = "获取统计数据")
    @PreAuthorize("hasAuthority('system:index:query')")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(sysIndexService.getStatistics());
    }

    @Operation(summary = "获取访问趋势")
    @PreAuthorize("hasAuthority('system:index:query')")
    @GetMapping("/visitTrend")
    public Result<Map<String, Object>> getVisitTrend() {
        return Result.success(sysIndexService.getVisitTrend());
    }

    @Operation(summary = "获取用户分布")
    @PreAuthorize("hasAuthority('system:index:query')")
    @GetMapping("/userDistribution")
    public Result<Map<String, Object>> getUserDistribution() {
        return Result.success(sysIndexService.getUserDistribution());
    }

    @Operation(summary = "获取热门操作统计")
    @PreAuthorize("hasAuthority('system:index:query')")
    @GetMapping("/hotOperations")
    public Result<Map<String, Object>> getHotOperations() {
        return Result.success(((com.micro.platform.system.service.impl.SysIndexServiceImpl)sysIndexService).getHotOperations());
    }

    @Operation(summary = "获取公告列表")
    @GetMapping("/noticeList")
    public Result<List<Map<String, Object>>> getNoticeList() {
        return Result.success(((com.micro.platform.system.service.impl.SysIndexServiceImpl)sysIndexService).getNoticeList());
    }
}
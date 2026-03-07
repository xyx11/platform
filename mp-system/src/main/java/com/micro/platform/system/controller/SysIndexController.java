package com.micro.platform.system.controller;

import com.micro.platform.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 首页统计控制器
 */
@Tag(name = "首页统计", description = "首页统计数据")
@RestController
@RequestMapping("/system/index")
public class SysIndexController {

    @Operation(summary = "获取统计数据")
    @PreAuthorize("hasAuthority('system:index:query')")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        // TODO: 替换为实际的 Service 查询
        statistics.put("userCount", 1234);
        statistics.put("roleCount", 56);
        statistics.put("menuCount", 234);
        statistics.put("logCount", 8901);
        statistics.put("deptCount", 12);
        statistics.put("postCount", 24);
        statistics.put("noticeCount", 18);
        statistics.put("jobCount", 8);
        return Result.success(statistics);
    }

    @Operation(summary = "获取访问趋势")
    @PreAuthorize("hasAuthority('system:index:query')")
    @GetMapping("/visitTrend")
    public Result<Map<String, Object>> getVisitTrend() {
        Map<String, Object> trend = new HashMap<>();
        trend.put("dates", new String[]{"周一", "周二", "周三", "周四", "周五", "周六", "周日"});
        trend.put("values", new Integer[]{820, 932, 901, 934, 1290, 1330, 1320});
        return Result.success(trend);
    }

    @Operation(summary = "获取用户分布")
    @PreAuthorize("hasAuthority('system:index:query')")
    @GetMapping("/userDistribution")
    public Result<Map<String, Object>> getUserDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        distribution.put("data", new Object[]{
            Map.of("value", 1048, "name", "搜索引擎"),
            Map.of("value", 735, "name", "直接访问"),
            Map.of("value", 580, "name", "邮件营销"),
            Map.of("value", 484, "name", "联盟广告"),
            Map.of("value", 300, "name", "视频广告")
        });
        return Result.success(distribution);
    }
}

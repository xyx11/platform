package com.micro.platform.system.service.impl;

import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysLoginLog;
import com.micro.platform.system.mapper.SysLoginLogMapper;
import com.micro.platform.system.service.SysIndexService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 首页统计服务实现
 */
@Service
public class SysIndexServiceImpl extends ServiceImplX<SysLoginLogMapper, SysLoginLog> implements SysIndexService {

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        // 这里可以根据实际需要统计各个表的数据
        // 由于是分库分表场景，暂时返回示例数据
        statistics.put("userCount", 1234);
        statistics.put("roleCount", 56);
        statistics.put("menuCount", 234);
        statistics.put("logCount", 8901);
        statistics.put("deptCount", 12);
        statistics.put("postCount", 24);
        statistics.put("noticeCount", 18);
        statistics.put("jobCount", 8);
        return statistics;
    }

    @Override
    public Map<String, Object> getVisitTrend() {
        Map<String, Object> trend = new HashMap<>();

        // 获取最近 7 天的日期
        List<String> dates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            dates.add(LocalDate.now().minusDays(i).format(formatter));
        }
        trend.put("dates", dates);

        // 模拟访问数据（实际应该从登录日志中统计）
        Random random = new Random();
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            values.add(800 + random.nextInt(600));
        }
        trend.put("values", values);

        return trend;
    }

    @Override
    public Map<String, Object> getUserDistribution() {
        Map<String, Object> distribution = new HashMap<>();

        List<Map<String, Object>> data = new ArrayList<>();
        data.add(createDistributionItem(1048, "搜索引擎"));
        data.add(createDistributionItem(735, "直接访问"));
        data.add(createDistributionItem(580, "邮件营销"));
        data.add(createDistributionItem(484, "联盟广告"));
        data.add(createDistributionItem(300, "视频广告"));

        distribution.put("data", data);
        return distribution;
    }

    private Map<String, Object> createDistributionItem(int value, String name) {
        Map<String, Object> item = new HashMap<>();
        item.put("value", value);
        item.put("name", name);
        return item;
    }
}
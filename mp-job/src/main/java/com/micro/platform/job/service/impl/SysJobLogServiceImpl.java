package com.micro.platform.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.job.entity.SysJobLog;
import com.micro.platform.job.mapper.SysJobLogMapper;
import com.micro.platform.job.service.SysJobLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定时任务日志服务实现
 */
@Service
public class SysJobLogServiceImpl extends ServiceImplX<SysJobLogMapper, SysJobLog> implements SysJobLogService {

    @Override
    public Page<SysJobLog> selectJobLogPage(SysJobLog jobLog, Integer pageNum, Integer pageSize) {
        Page<SysJobLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysJobLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(jobLog.getJobId() != null, SysJobLog::getJobId, jobLog.getJobId())
                .like(StringUtils.hasText(jobLog.getJobName()), SysJobLog::getJobName, jobLog.getJobName())
                .eq(StringUtils.hasText(jobLog.getJobGroup()), SysJobLog::getJobGroup, jobLog.getJobGroup())
                .eq(jobLog.getStatus() != null, SysJobLog::getStatus, jobLog.getStatus())
                .orderByDesc(SysJobLog::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public void clean() {
        baseMapper.clean();
    }

    @Override
    public void batchRemove(String ids) {
        if (StringUtils.hasText(ids)) {
            List<Long> idList = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            removeByIds(idList);
        }
    }

    @Override
    public Map<String, Object> getJobLogStats() {
        Map<String, Object> stats = new HashMap<>();

        // 统计日志总数
        long totalLogCount = baseMapper.selectCount(null);
        stats.put("totalLogCount", totalLogCount);

        // 统计今日执行次数
        LambdaQueryWrapper<SysJobLog> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(SysJobLog::getCreateTime, LocalDate.now().atStartOfDay());
        long todayExecuteCount = baseMapper.selectCount(todayWrapper);
        stats.put("todayExecuteCount", todayExecuteCount);

        // 统计今日成功次数
        LambdaQueryWrapper<SysJobLog> todaySuccessWrapper = new LambdaQueryWrapper<>();
        todaySuccessWrapper.ge(SysJobLog::getCreateTime, LocalDate.now().atStartOfDay())
                .eq(SysJobLog::getStatus, 0);
        long todaySuccessCount = baseMapper.selectCount(todaySuccessWrapper);
        stats.put("todaySuccessCount", todaySuccessCount);

        // 统计今日失败次数
        LambdaQueryWrapper<SysJobLog> todayFailWrapper = new LambdaQueryWrapper<>();
        todayFailWrapper.ge(SysJobLog::getCreateTime, LocalDate.now().atStartOfDay())
                .eq(SysJobLog::getStatus, 1);
        long todayFailCount = baseMapper.selectCount(todayFailWrapper);
        stats.put("todayFailCount", todayFailCount);

        // 计算今日成功率
        double successRate = todayExecuteCount > 0 ? (double) todaySuccessCount / todayExecuteCount * 100 : 0;
        stats.put("successRate", String.format("%.2f", successRate));

        // 统计最近 7 天每天的执行次数
        Map<String, Integer> trendData = new HashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LambdaQueryWrapper<SysJobLog> dateWrapper = new LambdaQueryWrapper<>();
            dateWrapper.between(SysJobLog::getCreateTime,
                    date.atStartOfDay(),
                    date.atTime(java.time.LocalTime.MAX));
            long count = baseMapper.selectCount(dateWrapper);
            trendData.put(date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")), (int) count);
        }
        stats.put("trendData", trendData);

        return stats;
    }
}
package com.micro.platform.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysLoginLog;
import com.micro.platform.system.mapper.SysLoginLogMapper;
import com.micro.platform.system.service.SysLoginLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录日志服务实现
 */
@Service
public class SysLoginLogServiceImpl extends ServiceImplX<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    @Override
    public Page<SysLoginLog> selectLoginLogPage(SysLoginLog log, Integer pageNum, Integer pageSize) {
        Page<SysLoginLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(log.getUsername()), SysLoginLog::getUsername, log.getUsername())
                .eq(log.getStatus() != null, SysLoginLog::getStatus, log.getStatus())
                .eq(log.getUserId() != null, SysLoginLog::getUserId, log.getUserId())
                .like(StringUtils.hasText(log.getIp()), SysLoginLog::getIp, log.getIp())
                .ge(log.getLoginTime() != null, SysLoginLog::getLoginTime, log.getLoginTime())
                .orderByDesc(SysLoginLog::getLoginTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public void clean() {
        baseMapper.clean();
    }

    @Override
    public byte[] exportLoginLog(SysLoginLog log) {
        try {
            LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(StringUtils.hasText(log.getUsername()), SysLoginLog::getUsername, log.getUsername())
                    .eq(log.getStatus() != null, SysLoginLog::getStatus, log.getStatus())
                    .eq(log.getUserId() != null, SysLoginLog::getUserId, log.getUserId())
                    .like(StringUtils.hasText(log.getIp()), SysLoginLog::getIp, log.getIp())
                    .ge(log.getLoginTime() != null, SysLoginLog::getLoginTime, log.getLoginTime())
                    .orderByDesc(SysLoginLog::getLoginTime);
            List<SysLoginLog> list = list(wrapper);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            EasyExcel.write(os, SysLoginLog.class)
                    .sheet("登录日志")
                    .doWrite(list);
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出登录日志失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getLoginLogStats() {
        Map<String, Object> stats = new HashMap<>();

        // 统计日志总数
        long totalLogCount = baseMapper.selectCount(null);
        stats.put("totalLogCount", totalLogCount);

        // 统计今日登录次数
        LambdaQueryWrapper<SysLoginLog> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(SysLoginLog::getLoginTime, LocalDate.now().atStartOfDay());
        long todayLoginCount = baseMapper.selectCount(todayWrapper);
        stats.put("todayLoginCount", todayLoginCount);

        // 统计今日成功登录次数
        LambdaQueryWrapper<SysLoginLog> todaySuccessWrapper = new LambdaQueryWrapper<>();
        todaySuccessWrapper.ge(SysLoginLog::getLoginTime, LocalDate.now().atStartOfDay())
                .eq(SysLoginLog::getStatus, 1);
        long todaySuccessCount = baseMapper.selectCount(todaySuccessWrapper);
        stats.put("todaySuccessCount", todaySuccessCount);

        // 统计今日失败登录次数
        LambdaQueryWrapper<SysLoginLog> todayFailWrapper = new LambdaQueryWrapper<>();
        todayFailWrapper.ge(SysLoginLog::getLoginTime, LocalDate.now().atStartOfDay())
                .eq(SysLoginLog::getStatus, 0);
        long todayFailCount = baseMapper.selectCount(todayFailWrapper);
        stats.put("todayFailCount", todayFailCount);

        // 计算今日成功率
        double successRate = todayLoginCount > 0 ? (double) todaySuccessCount / todayLoginCount * 100 : 0;
        stats.put("successRate", String.format("%.2f", successRate));

        // 统计最近 7 天每天的登录次数
        Map<String, Integer> trendData = new HashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LambdaQueryWrapper<SysLoginLog> dateWrapper = new LambdaQueryWrapper<>();
            dateWrapper.between(SysLoginLog::getLoginTime,
                    date.atStartOfDay(),
                    date.atTime(java.time.LocalTime.MAX));
            long count = baseMapper.selectCount(dateWrapper);
            trendData.put(date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")), (int) count);
        }
        stats.put("trendData", trendData);

        // 统计登录用户数（去重）
        // 注意：需要数据库支持，这里简化处理
        stats.put("uniqueUserCount", 0);

        return stats;
    }
}
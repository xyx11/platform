package com.micro.platform.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysOperationLog;
import com.micro.platform.system.mapper.SysOperationLogMapper;
import com.micro.platform.system.service.SysOperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志服务实现
 */
@Service
public class SysOperationLogServiceImpl extends ServiceImplX<SysOperationLogMapper, SysOperationLog> implements SysOperationLogService {

    @Override
    public Page<SysOperationLog> selectOperationLogPage(SysOperationLog log, Integer pageNum, Integer pageSize) {
        Page<SysOperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(log.getTitle()), SysOperationLog::getTitle, log.getTitle())
                .like(StringUtils.hasText(log.getOperName()), SysOperationLog::getOperName, log.getOperName())
                .eq(log.getBusinessType() != null, SysOperationLog::getBusinessType, log.getBusinessType())
                .eq(log.getStatus() != null, SysOperationLog::getStatus, log.getStatus())
                .ge(log.getOperTime() != null, SysOperationLog::getOperTime, log.getOperTime())
                .orderByDesc(SysOperationLog::getOperTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public void clean() {
        baseMapper.clean();
    }

    @Override
    public byte[] exportOperationLog(SysOperationLog log) {
        try {
            List<SysOperationLog> list = selectOperationLogList(log);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            EasyExcel.write(os, SysOperationLog.class)
                    .sheet("操作日志")
                    .doWrite(list);
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出操作日志失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getOperationLogStats() {
        Map<String, Object> stats = new HashMap<>();

        // 统计日志总数
        long totalLogCount = baseMapper.selectCount(null);
        stats.put("totalLogCount", totalLogCount);

        // 统计今日操作次数
        LambdaQueryWrapper<SysOperationLog> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(SysOperationLog::getOperTime, LocalDate.now().atStartOfDay());
        long todayOperCount = baseMapper.selectCount(todayWrapper);
        stats.put("todayOperCount", todayOperCount);

        // 统计今日成功次数
        LambdaQueryWrapper<SysOperationLog> todaySuccessWrapper = new LambdaQueryWrapper<>();
        todaySuccessWrapper.ge(SysOperationLog::getOperTime, LocalDate.now().atStartOfDay())
                .eq(SysOperationLog::getStatus, 0);
        long todaySuccessCount = baseMapper.selectCount(todaySuccessWrapper);
        stats.put("todaySuccessCount", todaySuccessCount);

        // 统计今日失败次数
        LambdaQueryWrapper<SysOperationLog> todayFailWrapper = new LambdaQueryWrapper<>();
        todayFailWrapper.ge(SysOperationLog::getOperTime, LocalDate.now().atStartOfDay())
                .eq(SysOperationLog::getStatus, 1);
        long todayFailCount = baseMapper.selectCount(todayFailWrapper);
        stats.put("todayFailCount", todayFailCount);

        // 计算今日成功率
        double successRate = todayOperCount > 0 ? (double) todaySuccessCount / todayOperCount * 100 : 0;
        stats.put("successRate", String.format("%.2f", successRate));

        // 统计最近 7 天每天的操作次数
        Map<String, Integer> trendData = new HashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LambdaQueryWrapper<SysOperationLog> dateWrapper = new LambdaQueryWrapper<>();
            dateWrapper.between(SysOperationLog::getOperTime,
                    date.atStartOfDay(),
                    date.atTime(java.time.LocalTime.MAX));
            long count = baseMapper.selectCount(dateWrapper);
            trendData.put(date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")), (int) count);
        }
        stats.put("trendData", trendData);

        return stats;
    }

    /**
     * 查询操作日志列表
     */
    private List<SysOperationLog> selectOperationLogList(SysOperationLog log) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(log.getTitle()), SysOperationLog::getTitle, log.getTitle())
                .like(StringUtils.hasText(log.getOperName()), SysOperationLog::getOperName, log.getOperName())
                .eq(log.getBusinessType() != null, SysOperationLog::getBusinessType, log.getBusinessType())
                .eq(log.getStatus() != null, SysOperationLog::getStatus, log.getStatus())
                .ge(log.getOperTime() != null, SysOperationLog::getOperTime, log.getOperTime())
                .orderByDesc(SysOperationLog::getOperTime);
        return baseMapper.selectList(wrapper);
    }
}
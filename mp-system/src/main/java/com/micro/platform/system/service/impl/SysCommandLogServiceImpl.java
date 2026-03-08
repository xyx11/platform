package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysCommandLog;
import com.micro.platform.system.mapper.SysCommandLogMapper;
import com.micro.platform.system.service.SysCommandLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 命令执行记录服务实现类
 */
@Service
public class SysCommandLogServiceImpl extends ServiceImplX<SysCommandLogMapper, SysCommandLog> implements SysCommandLogService {

    @Override
    public Page<SysCommandLog> selectCommandLogPage(SysCommandLog log, Integer pageNum, Integer pageSize) {
        Page<SysCommandLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysCommandLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(log.getCommandType()), SysCommandLog::getCommandType, log.getCommandType())
                .like(StringUtils.hasText(log.getExecuteName()), SysCommandLog::getExecuteName, log.getExecuteName())
                .eq(log.getStatus() != null, SysCommandLog::getStatus, log.getStatus())
                .orderByDesc(SysCommandLog::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public String executeCommand(String commandType, String command) {
        SysCommandLog commandLog = new SysCommandLog();
        commandLog.setCommandType(commandType);
        commandLog.setCommandContent(command);
        commandLog.setExecuteTimeStr(LocalDateTime.now());

        long startTime = System.currentTimeMillis();
        Process process = null;

        try {
            // 根据操作系统类型执行命令
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                // Windows 系统
                process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", command});
            } else {
                // Linux/Unix 系统
                process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            }

            // 读取命令输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 读取错误输出
            StringBuilder error = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line).append("\n");
                }
            }

            // 等待命令执行完成（设置超时时间）
            boolean completed = process.waitFor(30, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                commandLog.setStatus(0);
                commandLog.setErrorMsg("命令执行超时");
                commandLog.setCommandResult("命令执行超时，已强制终止");
            } else {
                int exitCode = process.exitValue();
                if (exitCode == 0) {
                    commandLog.setStatus(1);
                    commandLog.setCommandResult(output.toString());
                } else {
                    commandLog.setStatus(0);
                    commandLog.setErrorMsg(error.toString());
                    commandLog.setCommandResult("命令执行失败，退出码：" + exitCode);
                }
            }

        } catch (IOException | InterruptedException e) {
            commandLog.setStatus(0);
            commandLog.setErrorMsg(e.getMessage());
            commandLog.setCommandResult("命令执行异常：" + e.getMessage());
            if (process != null) {
                process.destroyForcibly();
            }
        } finally {
            // 记录执行时间
            long endTime = System.currentTimeMillis();
            commandLog.setExecuteTime(endTime - startTime);

            // 保存记录
            baseMapper.insert(commandLog);
        }

        return commandLog.getCommandResult();
    }

    @Override
    public void clean() {
        baseMapper.delete(new LambdaQueryWrapper<>());
    }

    @Override
    public Map<String, Object> getCommandLogStats() {
        Map<String, Object> stats = new HashMap<>();

        // 统计记录总数
        long totalCount = baseMapper.selectCount(null);
        stats.put("totalCount", totalCount);

        // 统计今日执行次数
        LambdaQueryWrapper<SysCommandLog> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(SysCommandLog::getCreateTime, LocalDate.now().atStartOfDay());
        long todayCount = baseMapper.selectCount(todayWrapper);
        stats.put("todayCount", todayCount);

        // 统计今日成功次数
        LambdaQueryWrapper<SysCommandLog> todaySuccessWrapper = new LambdaQueryWrapper<>();
        todaySuccessWrapper.ge(SysCommandLog::getCreateTime, LocalDate.now().atStartOfDay())
                .eq(SysCommandLog::getStatus, 1);
        long todaySuccessCount = baseMapper.selectCount(todaySuccessWrapper);
        stats.put("todaySuccessCount", todaySuccessCount);

        // 统计今日失败次数
        LambdaQueryWrapper<SysCommandLog> todayFailWrapper = new LambdaQueryWrapper<>();
        todayFailWrapper.ge(SysCommandLog::getCreateTime, LocalDate.now().atStartOfDay())
                .eq(SysCommandLog::getStatus, 0);
        long todayFailCount = baseMapper.selectCount(todayFailWrapper);
        stats.put("todayFailCount", todayFailCount);

        // 计算今日成功率
        double successRate = todayCount > 0 ? (double) todaySuccessCount / todayCount * 100 : 0;
        stats.put("successRate", String.format("%.2f", successRate));

        // 统计最近 7 天每天的执行次数
        Map<String, Integer> trendData = new HashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LambdaQueryWrapper<SysCommandLog> dateWrapper = new LambdaQueryWrapper<>();
            dateWrapper.between(SysCommandLog::getCreateTime,
                    date.atStartOfDay(),
                    date.atTime(java.time.LocalTime.MAX));
            long count = baseMapper.selectCount(dateWrapper);
            trendData.put(date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")), (int) count);
        }
        stats.put("trendData", trendData);

        // 统计平均执行时间
        LambdaQueryWrapper<SysCommandLog> todayWithTimeWrapper = new LambdaQueryWrapper<>();
        todayWithTimeWrapper.ge(SysCommandLog::getCreateTime, LocalDate.now().atStartOfDay())
                .isNotNull(SysCommandLog::getExecuteTime);
        List<SysCommandLog> todayLogs = list(todayWithTimeWrapper);
        if (!todayLogs.isEmpty()) {
            double avgExecuteTime = todayLogs.stream()
                    .mapToLong(SysCommandLog::getExecuteTime)
                    .average()
                    .orElse(0.0);
            stats.put("avgExecuteTime", Math.round(avgExecuteTime) + "ms");
        } else {
            stats.put("avgExecuteTime", "0ms");
        }

        return stats;
    }
}
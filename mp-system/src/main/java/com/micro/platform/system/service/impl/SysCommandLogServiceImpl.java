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
import java.time.LocalDateTime;
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
}
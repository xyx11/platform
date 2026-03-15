package com.micro.platform.system.service.impl;

import com.micro.platform.common.log.entity.OperationLog;
import com.micro.platform.common.log.service.OperationLogService;
import com.micro.platform.system.entity.SysOperationLog;
import com.micro.platform.system.mapper.SysOperationLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {

    private static final Logger log = LoggerFactory.getLogger(OperationLogServiceImpl.class);

    private final SysOperationLogMapper sysOperationLogMapper;

    public OperationLogServiceImpl(SysOperationLogMapper sysOperationLogMapper) {
        this.sysOperationLogMapper = sysOperationLogMapper;
    }

    // 最大日志长度限制（避免超出数据库 TEXT 字段限制）
    private static final int MAX_LOG_LENGTH = 60000;

    @Override
    @Async("operationLogExecutor")
    public void saveLog(OperationLog operationLog) {
        try {
            // 将 common 模块的 OperationLog 转换为 system 模块的 SysOperationLog
            SysOperationLog sysOperationLog = new SysOperationLog();
            BeanUtils.copyProperties(operationLog, sysOperationLog);

            // 特殊字段处理，并进行长度限制
            if (operationLog.getRequestParams() != null) {
                String params = truncateIfNeeded(operationLog.getRequestParams(), MAX_LOG_LENGTH);
                sysOperationLog.setOperParam(params);
            }
            if (operationLog.getResponseResult() != null) {
                String result = truncateIfNeeded(operationLog.getResponseResult(), MAX_LOG_LENGTH);
                sysOperationLog.setJsonResult(result);
            }
            if (operationLog.getOperationIp() != null) {
                sysOperationLog.setOperIp(operationLog.getOperationIp());
            }
            if (operationLog.getOperationLocation() != null) {
                sysOperationLog.setOperLocation(operationLog.getOperationLocation());
            }
            if (operationLog.getOperatorName() != null) {
                sysOperationLog.setOperName(operationLog.getOperatorName());
            }
            // SysOperationLog 没有 operatorId 字段，跳过
            if (operationLog.getRequestMethod() != null) {
                sysOperationLog.setRequestMethod(operationLog.getRequestMethod());
            }
            if (operationLog.getRequestUrl() != null) {
                sysOperationLog.setOperUrl(operationLog.getRequestUrl());
            }
            if (operationLog.getModule() != null) {
                sysOperationLog.setTitle(operationLog.getModule());
            }
            if (operationLog.getDescription() != null) {
                sysOperationLog.setMethod(operationLog.getDescription());
            }
            if (operationLog.getCreateTime() != null) {
                sysOperationLog.setOperTime(operationLog.getCreateTime());
            }
            if (operationLog.getExecuteTime() != null) {
                sysOperationLog.setCostTime(operationLog.getExecuteTime().intValue());
            }
            
            sysOperationLogMapper.insert(sysOperationLog);
            log.debug("操作日志保存成功：{} - {}", operationLog.getModule(), operationLog.getDescription());
        } catch (Exception e) {
            log.error("操作日志保存失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 截断字符串到指定长度（避免超出数据库字段限制）
     */
    private String truncateIfNeeded(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() > maxLength) {
            log.warn("日志内容超出限制，已截断：原长度={}, 限制长度={}", value.length(), maxLength);
            return value.substring(0, maxLength);
        }
        return value;
    }
}
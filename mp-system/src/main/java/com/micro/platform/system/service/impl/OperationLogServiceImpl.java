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

    @Override
    @Async("operationLogExecutor")
    public void saveLog(OperationLog operationLog) {
        try {
            // 将 common 模块的 OperationLog 转换为 system 模块的 SysOperationLog
            SysOperationLog sysOperationLog = new SysOperationLog();
            BeanUtils.copyProperties(operationLog, sysOperationLog);
            
            // 特殊字段处理
            if (operationLog.getRequestParams() != null) {
                sysOperationLog.setOperParam(operationLog.getRequestParams());
            }
            if (operationLog.getResponseResult() != null) {
                sysOperationLog.setJsonResult(operationLog.getResponseResult());
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
            if (operationLog.getOperatorId() != null) {
                sysOperationLog.setOperId(operationLog.getOperatorId());
            }
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
}
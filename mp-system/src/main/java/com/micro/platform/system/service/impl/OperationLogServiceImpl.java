package com.micro.platform.system.service.impl;

import com.micro.platform.common.log.entity.OperationLog;
import com.micro.platform.common.log.service.OperationLogService;
import com.micro.platform.system.mapper.SysOperationLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现
 */
@Service
public class OperationLogServiceImpl implements CommonOperationLogService {

    private static final Logger log = LoggerFactory.getLogger(OperationLogServiceImpl.class);

    private final SysOperationLogMapper sysOperationLogMapper;

    public OperationLogServiceImpl(SysOperationLogMapper sysOperationLogMapper) {
        this.sysOperationLogMapper = sysOperationLogMapper;
    }

    @Override
    @Async("operationLogExecutor")
    public void saveLog(OperationLog operationLog) {
        try {
            sysOperationLogMapper.insert(operationLog);
            log.debug("操作日志保存成功：{} - {}", operationLog.getModule(), operationLog.getDescription());
        } catch (Exception e) {
            log.error("操作日志保存失败：{}", e.getMessage(), e);
        }
    }
}
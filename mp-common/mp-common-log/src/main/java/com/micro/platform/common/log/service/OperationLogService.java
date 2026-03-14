package com.micro.platform.common.log.service;

import com.micro.platform.common.log.entity.OperationLog;

/**
 * 操作日志服务接口（定义在 common 模块，避免循环依赖）
 */
public interface OperationLogService {

    /**
     * 保存操作日志
     */
    void saveLog(OperationLog operationLog);
}
package com.micro.platform.common.log.handler;

import com.micro.platform.common.log.entity.AuditLogEntity;

/**
 * 审计日志事件处理器
 * 
 * 由具体模块（如 mp-system）实现，用于保存审计日志
 */
public interface AuditLogEventHandler {

    /**
     * 处理审计日志
     */
    void onAuditLog(AuditLogEntity auditLog);
}

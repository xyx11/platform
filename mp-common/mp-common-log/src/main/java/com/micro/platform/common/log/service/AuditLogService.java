package com.micro.platform.common.log.service;

import com.micro.platform.common.log.entity.AuditLogEntity;

/**
 * 审计日志服务接口
 */
public interface AuditLogService {

    /**
     * 保存审计日志
     */
    void save(AuditLogEntity auditLog);
}

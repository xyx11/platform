package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysAuditLog;

import java.util.List;
import java.util.Map;

/**
 * 审计日志服务接口
 */
public interface SysAuditLogService extends IServiceX<SysAuditLog> {

    /**
     * 分页查询审计日志
     */
    Page<SysAuditLog> selectAuditLogPage(SysAuditLog auditLog, Integer pageNum, Integer pageSize);

    /**
     * 查询指定表格的审计日志
     */
    List<SysAuditLog> selectAuditLogByTable(String tableName, Long recordId);

    /**
     * 查询用户的操作日志
     */
    List<SysAuditLog> selectAuditLogByUser(Long userId, Integer limit);

    /**
     * 删除审计日志
     */
    void batchDelete(List<Long> ids);

    /**
     * 清空审计日志
     */
    void clear();

    /**
     * 获取审计统计信息
     */
    Map<String, Object> getAuditStats();
}
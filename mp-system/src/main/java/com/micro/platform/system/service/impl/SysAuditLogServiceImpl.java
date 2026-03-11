package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysAuditLog;
import com.micro.platform.system.mapper.SysAuditLogMapper;
import com.micro.platform.system.service.SysAuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 审计日志服务实现
 */
@Service
public class SysAuditLogServiceImpl extends ServiceImplX<SysAuditLogMapper, SysAuditLog> implements SysAuditLogService {

    @Override
    public Page<SysAuditLog> selectAuditLogPage(SysAuditLog auditLog, Integer pageNum, Integer pageSize) {
        Page<SysAuditLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysAuditLog> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(auditLog.getModule() != null && !auditLog.getModule().isEmpty(), SysAuditLog::getModule, auditLog.getModule())
                .eq(auditLog.getOperationType() != null, SysAuditLog::getOperationType, auditLog.getOperationType())
                .eq(auditLog.getTableName() != null && !auditLog.getTableName().isEmpty(), SysAuditLog::getTableName, auditLog.getTableName())
                .eq(auditLog.getOperatorId() != null, SysAuditLog::getOperatorId, auditLog.getOperatorId())
                .eq(auditLog.getStatus() != null, SysAuditLog::getStatus, auditLog.getStatus())
                .like(auditLog.getDescription() != null && !auditLog.getDescription().isEmpty(), SysAuditLog::getDescription, auditLog.getDescription())
                .orderByDesc(SysAuditLog::getCreateTime);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SysAuditLog> selectAuditLogByTable(String tableName, Long recordId) {
        LambdaQueryWrapper<SysAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAuditLog::getTableName, tableName)
                .eq(recordId != null, SysAuditLog::getRecordId, recordId)
                .orderByDesc(SysAuditLog::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<SysAuditLog> selectAuditLogByUser(Long userId, Integer limit) {
        Page<SysAuditLog> page = new Page<>(1, limit != null ? limit : 10);
        LambdaQueryWrapper<SysAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAuditLog::getOperatorId, userId)
                .orderByDesc(SysAuditLog::getCreateTime);
        return baseMapper.selectPage(page, wrapper).getRecords();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            this.removeByIds(ids);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clear() {
        this.remove(new LambdaQueryWrapper<>());
    }

    @Override
    public Map<String, Object> getAuditStats() {
        Map<String, Object> stats = new HashMap<>();

        // 总日志数
        long totalCount = baseMapper.selectCount(null);
        stats.put("totalCount", totalCount);

        // 成功日志数
        long successCount = baseMapper.selectCount(new LambdaQueryWrapper<SysAuditLog>().eq(SysAuditLog::getStatus, 1));
        stats.put("successCount", successCount);

        // 失败日志数
        long failCount = baseMapper.selectCount(new LambdaQueryWrapper<SysAuditLog>().eq(SysAuditLog::getStatus, 0));
        stats.put("failCount", failCount);

        // 按操作类型统计
        List<Map<String, Object>> operationTypeStats = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            long count = baseMapper.selectCount(new LambdaQueryWrapper<SysAuditLog>().eq(SysAuditLog::getOperationType, i));
            Map<String, Object> stat = new HashMap<>();
            stat.put("type", i);
            stat.put("count", count);
            operationTypeStats.add(stat);
        }
        stats.put("operationTypeStats", operationTypeStats);

        // 按模块统计
        List<Map<String, Object>> moduleStats = new ArrayList<>();
        // 这里可以按模块分组统计，简化处理
        stats.put("moduleStats", moduleStats);

        return stats;
    }
}
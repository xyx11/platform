package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计日志实体
 *
 * 用于记录系统中敏感数据的变更历史，支持数据追踪和审计
 */
@TableName("sys_audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模块名称（如：用户管理、角色管理）
     */
    private String module;

    /**
     * 操作类型
     * 1-新增 2-修改 3-删除 4-查询 5-导入 6-导出 7-其他
     */
    private Integer operationType;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 记录 ID
     */
    private Long recordId;

    /**
     * 变更前数据（JSON）
     */
    @TableField(exist = false)
    private String beforeData;

    /**
     * 变更后数据（JSON）
     */
    @TableField(exist = false)
    private String afterData;

    /**
     * 变更字段（JSON，记录具体变更的字段）
     */
    private String changeFields;

    /**
     * 操作人 ID
     */
    private Long operatorId;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 部门 ID
     */
    private Long deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 操作 IP
     */
    private String operIp;

    /**
     * 操作地点
     */
    private String operLocation;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求 URL
     */
    private String requestUrl;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 执行时长（毫秒）
     */
    private Long executeTime;

    /**
     * 操作状态（0-失败 1-成功）
     */
    private Integer status;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 租户 ID（多租户场景）
     */
    private Long tenantId;
}
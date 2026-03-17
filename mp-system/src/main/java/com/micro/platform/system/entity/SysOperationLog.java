package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统日志实体
 */
@TableName("sys_operation_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模块标题
     */
    private String title;

    /**
     * 业务类型
     */
    private Integer businessType;

    /**
     * 方法名称
     */
    private String method;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 操作类别
     */
    private Integer operatorType;

    /**
     * 操作人员
     */
    private String operName;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 请求 URL
     */
    private String operUrl;

    /**
     * 主机地址
     */
    private String operIp;

    /**
     * 操作地点
     */
    private String operLocation;

    /**
     * 请求参数
     */
    private String operParam;

    /**
     * 返回参数
     */
    private String jsonResult;

    /**
     * 操作状态 (0:正常 1:异常)
     */
    private Integer status;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 操作时间
     */
    private LocalDateTime operTime;

    /**
     * 消耗时间
     */
    private Integer costTime;
}
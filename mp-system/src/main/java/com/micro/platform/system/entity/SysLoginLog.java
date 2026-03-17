package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统登录日志实体
 */
@TableName("sys_login_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysLoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 登录日志 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long logId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 登录状态 (0:失败 1:成功)
     */
    private Integer status;

    /**
     * 登录 IP 地址
     */
    private String ip;

    /**
     * 登录地点
     */
    private String location;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 提示消息
     */
    private String msg;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
}
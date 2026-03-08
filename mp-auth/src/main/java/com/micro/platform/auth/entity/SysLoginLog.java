package com.micro.platform.auth.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统登录日志实体
 */
@TableName("sys_login_log")
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

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
}
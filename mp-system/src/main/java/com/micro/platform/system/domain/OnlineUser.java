package com.micro.platform.system.domain;

import lombok.Data;
import java.io.Serializable;

/**
 * 在线用户
 */
@Data
public class OnlineUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Token 会话标识
     */
    private String token;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 登录 IP 地址
     */
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 登录时间
     */
    private String loginTime;

    /**
     * 在线时长（分钟）
     */
    private Long onlineDuration;

    /**
     * 角色标识
     */
    private String roleKey;

    /**
     * 角色名称
     */
    private String roleName;
}
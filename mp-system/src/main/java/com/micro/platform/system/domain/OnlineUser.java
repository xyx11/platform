package com.micro.platform.system.domain;

import java.io.Serializable;

/**
 * 在线用户
 */
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getIpaddr() {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr) {
        this.ipaddr = ipaddr;
    }

    public String getLoginLocation() {
        return loginLocation;
    }

    public void setLoginLocation(String loginLocation) {
        this.loginLocation = loginLocation;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public Long getOnlineDuration() {
        return onlineDuration;
    }

    public void setOnlineDuration(Long onlineDuration) {
        this.onlineDuration = onlineDuration;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
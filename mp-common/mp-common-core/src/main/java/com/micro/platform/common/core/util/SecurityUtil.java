package com.micro.platform.common.core.util;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.micro.platform.common.core.entity.LoginUser;

/**
 * 安全工具类
 */
public class SecurityUtil {

    /**
     * 登录
     */
    public static void login(Long userId) {
        StpUtil.login(userId);
    }

    /**
     * 登录（带超时）
     */
    public static void login(Long userId, long timeout) {
        StpUtil.login(userId, timeout);
    }

    /**
     * 登录（带配置）
     */
    public static void login(Long userId, SaLoginModel saLoginModel) {
        StpUtil.login(userId, saLoginModel);
    }

    /**
     * 注销
     */
    public static void logout() {
        StpUtil.logout();
    }

    /**
     * 判断是否已登录
     */
    public static boolean isLogin() {
        return StpUtil.isLogin();
    }

    /**
     * 获取当前登录用户 ID
     */
    public static Long getUserId() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        Object id = StpUtil.getLoginId();
        return id == null ? null : Long.valueOf(id.toString());
    }

    /**
     * 获取当前登录用户信息
     */
    public static LoginUser getLoginUser() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        SaSession session = StpUtil.getSession();
        return (LoginUser) session.get("loginUser");
    }

    /**
     * 设置登录用户信息
     */
    public static void setLoginUser(LoginUser loginUser) {
        SaSession session = StpUtil.getSession();
        session.set("loginUser", loginUser);
    }

    /**
     * 获取当前登录用户名
     */
    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getUsername();
    }

    /**
     * 检查是否有权限
     */
    public static boolean hasPermission(String permission) {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return loginUser.getPermissions().contains(permission) ||
                loginUser.getRoleCodes().contains("admin");
    }

    /**
     * 检查是否有任意一个权限
     */
    public static boolean hasAnyPermission(String... permissions) {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        for (String permission : permissions) {
            if (loginUser.getPermissions().contains(permission) ||
                    loginUser.getRoleCodes().contains("admin")) {
                return true;
            }
        }
        return false;
    }
}
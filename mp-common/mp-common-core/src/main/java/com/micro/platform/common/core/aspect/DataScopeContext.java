package com.micro.platform.common.core.aspect;

import com.micro.platform.common.security.entity.LoginUser;

/**
 * 数据权限上下文
 *
 * 使用 ThreadLocal 存储数据权限相关信息
 */
public class DataScopeContext {

    private static final ThreadLocal<String> DEPT_ALIAS = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ALIAS = new ThreadLocal<>();
    private static final ThreadLocal<LoginUser> LOGIN_USER = new ThreadLocal<>();

    public static void setDeptAlias(String deptAlias) {
        DEPT_ALIAS.set(deptAlias);
    }

    public static String getDeptAlias() {
        return DEPT_ALIAS.get();
    }

    public static void setUserAlias(String userAlias) {
        USER_ALIAS.set(userAlias);
    }

    public static String getUserAlias() {
        return USER_ALIAS.get();
    }

    public static void setLoginUser(LoginUser loginUser) {
        LOGIN_USER.set(loginUser);
    }

    public static LoginUser getLoginUser() {
        return LOGIN_USER.get();
    }

    public static void clear() {
        DEPT_ALIAS.remove();
        USER_ALIAS.remove();
        LOGIN_USER.remove();
    }
}
package com.micro.platform.common.core.interceptor;

/**
 * 租户上下文
 *
 * 使用 ThreadLocal 存储当前租户信息
 */
public class TenantContext {

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    public static void setCurrentTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static Long getCurrentTenantId() {
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}
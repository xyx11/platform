package com.micro.platform.job.config;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限认证接口实现
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private final JdbcTemplate jdbcTemplate;

    public StpInterfaceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 从数据库加载用户权限
        String sql = "SELECT DISTINCT m.permission FROM sys_user_role ur " +
                     "JOIN sys_role r ON ur.role_id = r.role_id " +
                     "JOIN sys_role_menu rm ON r.role_id = rm.role_id " +
                     "JOIN sys_menu m ON rm.menu_id = m.menu_id " +
                     "WHERE ur.user_id = ? AND m.permission IS NOT NULL AND m.permission != ''";
        List<String> result = jdbcTemplate.queryForList(sql, String.class, loginId);
        return result != null ? result : new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 从数据库加载用户角色
        String sql = "SELECT r.role_code FROM sys_user_role ur " +
                     "JOIN sys_role r ON ur.role_id = r.role_id " +
                     "WHERE ur.user_id = ?";
        List<String> result = jdbcTemplate.queryForList(sql, String.class, loginId);
        return result != null ? result : new ArrayList<>();
    }
}
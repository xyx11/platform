package com.micro.platform.common.core.util;

import com.micro.platform.common.core.enums.DataScopeType;
import com.micro.platform.common.security.entity.LoginUser;
import org.springframework.util.StringUtils;

/**
 * 数据权限工具类
 */
public class DataScopeUtil {

    /**
     * 获取数据范围 SQL 过滤条件
     *
     * @param loginUser 登录用户信息
     * @param deptAlias 部门别名
     * @param userAlias 用户别名
     * @return SQL 过滤条件
     */
    public static String getDataScope(LoginUser loginUser, String deptAlias, String userAlias) {
        if (loginUser == null) {
            return "";
        }

        // 超级管理员拥有全部数据权限
        if (loginUser.getRoleCodes() != null && loginUser.getRoleCodes().contains("admin")) {
            return "";
        }

        Integer dataScope = loginUser.getDataScope();
        if (dataScope == null) {
            dataScope = DataScopeType.SELF.getCode();
        }

        StringBuilder sqlSb = new StringBuilder();

        switch (DataScopeType.getByCode(dataScope)) {
            case ALL:
                // 全部数据，不添加过滤条件
                sqlSb.append("");
                break;
            case DEPT_AND_CHILD:
                // 本部门及以下
                sqlSb.append(" AND (")
                        .append(deptAlias).append(".id = ").append(loginUser.getDeptId())
                        .append(" OR ")
                        .append(deptAlias).append(".parent_id = ").append(loginUser.getDeptId())
                        .append(")");
                break;
            case DEPT_ONLY:
                // 仅本部门
                sqlSb.append(" AND ").append(deptAlias).append(".id = ").append(loginUser.getDeptId());
                break;
            case SELF:
                // 仅本人
                sqlSb.append(" AND ").append(userAlias).append(".id = ").append(loginUser.getUserId());
                break;
            case CUSTOM:
                // 自定义数据权限（按角色）
                if (loginUser.getRoleIds() != null && !loginUser.getRoleIds().isEmpty()) {
                    sqlSb.append(" AND EXISTS (SELECT 1 FROM sys_user_role sur ")
                            .append("WHERE sur.user_id = ").append(userAlias).append(".id ")
                            .append("AND sur.role_id IN (").append(String.join(",", loginUser.getRoleIds().toString().replaceAll("\\[|\\]", ""))).append("))");
                } else {
                    sqlSb.append(" AND 1=0 ");
                }
                break;
            default:
                sqlSb.append("");
        }

        return sqlSb.toString();
    }

    /**
     * 获取数据范围 SQL 过滤条件（简化版，仅根据部门 ID）
     */
    public static String getDataScopeSimple(LoginUser loginUser, String deptAlias, String userAlias, String deptIds) {
        if (!StringUtils.hasText(deptIds)) {
            return getDataScope(loginUser, deptAlias, userAlias);
        }

        StringBuilder sqlSb = new StringBuilder();
        sqlSb.append(" AND (");

        if (deptAlias.contains(".")) {
            sqlSb.append(deptAlias).append(".id");
        } else {
            sqlSb.append(deptAlias).append(".dept_id");
        }
        sqlSb.append(" IN (").append(deptIds).append(")");

        sqlSb.append(" OR ");

        if (userAlias.contains(".")) {
            sqlSb.append(userAlias).append(".id");
        } else {
            sqlSb.append(userAlias).append(".user_id");
        }
        sqlSb.append(" = ").append(loginUser.getUserId());

        sqlSb.append(")");

        return sqlSb.toString();
    }
}
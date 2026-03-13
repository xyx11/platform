package com.micro.platform.common.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.micro.platform.common.core.aspect.DataScopeContext;
import com.micro.platform.common.core.enums.DataScopeType;
import com.micro.platform.common.core.service.IDataScopeService;
import com.micro.platform.common.core.entity.LoginUser;
import com.micro.platform.common.core.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据权限服务实现
 */
@Service
public class DataScopeServiceImpl implements IDataScopeService {

    @Override
    public Wrapper getDataScopeWrapper(LoginUser loginUser, String deptAlias, String userAlias) {
        QueryWrapper<Object> wrapper = new QueryWrapper<>();

        if (loginUser == null) {
            return wrapper;
        }

        // 超级管理员拥有全部数据权限
        if (loginUser.getRoleCodes() != null && loginUser.getRoleCodes().contains("admin")) {
            return wrapper;
        }

        Integer dataScope = loginUser.getDataScope();
        if (dataScope == null) {
            dataScope = DataScopeType.SELF.getCode();
        }

        switch (DataScopeType.getByCode(dataScope)) {
            case ALL:
                // 全部数据，不添加过滤条件
                break;
            case DEPT_AND_CHILD:
                // 本部门及以下（需要递归查询子部门）
                wrapper.and(w -> w.eq(deptAlias + ".id", loginUser.getDeptId())
                        .or().eq(deptAlias + ".parent_id", loginUser.getDeptId()));
                break;
            case DEPT_ONLY:
                // 仅本部门
                wrapper.eq(deptAlias + ".id", loginUser.getDeptId());
                break;
            case SELF:
                // 仅本人
                wrapper.eq(userAlias + ".id", loginUser.getUserId());
                break;
            case CUSTOM:
                // 自定义数据权限（按角色）
                if (loginUser.getRoleIds() != null && !loginUser.getRoleIds().isEmpty()) {
                    wrapper.inSql(userAlias + ".id", "SELECT user_id FROM sys_user_role WHERE role_id IN ("
                            + String.join(",", loginUser.getRoleIds().stream().map(String::valueOf).toArray(String[]::new)) + ")");
                } else {
                    // 没有角色权限，查询空结果
                    wrapper.eq(userAlias + ".id", -1);
                }
                break;
            default:
                wrapper.eq(userAlias + ".id", loginUser.getUserId());
        }

        return wrapper;
    }

    @Override
    public Wrapper getCurrentUserDataScope(String deptAlias, String userAlias) {
        LoginUser loginUser = SecurityUtil.getLoginUser();
        return getDataScopeWrapper(loginUser, deptAlias, userAlias);
    }
}

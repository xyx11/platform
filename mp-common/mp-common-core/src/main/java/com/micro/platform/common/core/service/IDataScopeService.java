package com.micro.platform.common.core.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.micro.platform.common.security.entity.LoginUser;

/**
 * 数据权限服务接口
 */
public interface IDataScopeService {

    /**
     * 获取数据权限过滤条件
     *
     * @param loginUser 登录用户
     * @param deptAlias 部门别名
     * @param userAlias 用户别名
     * @return 过滤条件
     */
    Wrapper getDataScopeWrapper(LoginUser loginUser, String deptAlias, String userAlias);

    /**
     * 获取当前用户的数据权限过滤条件
     *
     * @param deptAlias 部门别名
     * @param userAlias 用户别名
     * @return 过滤条件
     */
    Wrapper getCurrentUserDataScope(String deptAlias, String userAlias);
}
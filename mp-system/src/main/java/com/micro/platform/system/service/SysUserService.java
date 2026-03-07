package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysUser;

/**
 * 用户服务接口
 */
public interface SysUserService extends IServiceX<SysUser> {

    /**
     * 分页查询用户列表
     */
    Page<SysUser> selectUserPage(SysUser user, Integer pageNum, Integer pageSize);

    /**
     * 保存用户（包含角色关联）
     */
    void saveUser(SysUser user);

    /**
     * 更新用户（包含角色关联）
     */
    void updateUser(SysUser user);

    /**
     * 重置密码
     */
    void resetPassword(Long userId, String password);
}
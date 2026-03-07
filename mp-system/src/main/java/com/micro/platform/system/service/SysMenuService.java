package com.micro.platform.system.service;

import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysMenu;

import java.util.List;
import java.util.Set;

/**
 * 菜单服务接口
 */
public interface SysMenuService extends IServiceX<SysMenu> {

    /**
     * 获取菜单树
     */
    List<SysMenu> getMenuTree();

    /**
     * 根据用户 ID 获取菜单权限
     */
    List<SysMenu> getMenusByUserId(Long userId);

    /**
     * 获取用户的菜单权限标识
     */
    Set<String> getMenuPermissions(Long userId);
}

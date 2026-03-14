package com.micro.platform.system.service;

import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysMenu;

import java.util.List;
import java.util.Map;
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

    /**
     * 导出菜单数据
     */
    byte[] exportMenu(SysMenu menu);

    /**
     * 查询菜单列表（支持条件查询）
     */
    List<SysMenu> selectMenuList(SysMenu menu);

    /**
     * 获取菜单统计信息
     */
    Map<String, Object> getMenuStats();

    /**
     * 获取菜单树形结构（包含按钮）
     */
    List<Map<String, Object>> getMenuTreeWithButtons();

    /**
     * 获取指定角色的菜单权限列表
     */
    List<Map<String, Object>> getMenusByRoleId(Long roleId);
}
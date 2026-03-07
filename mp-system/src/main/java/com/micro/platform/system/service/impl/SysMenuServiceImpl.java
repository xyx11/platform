package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysMenu;
import com.micro.platform.system.mapper.SysMenuMapper;
import com.micro.platform.system.service.SysMenuService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Service
public class SysMenuServiceImpl extends ServiceImplX<SysMenuMapper, SysMenu> implements SysMenuService {

    @Override
    public List<SysMenu> getMenuTree() {
        List<SysMenu> menus = list(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getType, 1, 2)
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSort));
        return buildMenuTree(menus, 0L);
    }

    @Override
    public List<SysMenu> getMenusByUserId(Long userId) {
        // 超级管理员拥有所有菜单权限
        if (userId == 1L) {
            return getMenuTree();
        }
        List<SysMenu> menus = baseMapper.selectMenusByUserId(userId);
        return buildMenuTree(menus, 0L);
    }

    @Override
    public Set<String> getMenuPermissions(Long userId) {
        if (userId == 1L) {
            return Collections.singleton("*:*:*");
        }
        List<SysMenu> menus = baseMapper.selectMenusByUserId(userId);
        return menus.stream()
                .map(SysMenu::getPermission)
                .filter(Objects::nonNull)
                .filter(permission -> !permission.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * 构建菜单树
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .peek(menu -> menu.setChildren(buildMenuTree(menus, menu.getMenuId())))
                .collect(Collectors.toList());
    }
}
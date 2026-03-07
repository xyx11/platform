package com.micro.platform.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysMenu;
import com.micro.platform.system.mapper.SysMenuMapper;
import com.micro.platform.system.service.SysMenuService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
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

    @Override
    public List<SysMenu> selectMenuList(SysMenu menu) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(menu.getMenuName()), SysMenu::getMenuName, menu.getMenuName())
                .eq(menu.getStatus() != null, SysMenu::getStatus, menu.getStatus())
                .eq(menu.getType() != null, SysMenu::getType, menu.getType())
                .orderByAsc(SysMenu::getSort);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public void exportMenu(HttpServletResponse response, SysMenu menu) {
        try {
            List<SysMenu> list = selectMenuList(menu);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("菜单数据", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), SysMenu.class)
                    .sheet("菜单数据")
                    .doWrite(list);
        } catch (Exception e) {
            throw new RuntimeException("导出菜单数据失败：" + e.getMessage());
        }
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
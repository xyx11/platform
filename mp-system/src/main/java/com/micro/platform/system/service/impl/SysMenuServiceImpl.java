package com.micro.platform.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysMenu;
import com.micro.platform.system.mapper.SysMenuMapper;
import com.micro.platform.system.service.SysMenuService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
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
    public byte[] exportMenu(SysMenu menu) {
        try {
            List<SysMenu> list = selectMenuList(menu);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            EasyExcel.write(os, SysMenu.class)
                    .sheet("菜单数据")
                    .doWrite(list);
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出菜单数据失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getMenuStats() {
        Map<String, Object> stats = new HashMap<>();

        // 统计菜单总数
        long totalMenuCount = baseMapper.selectCount(null);
        stats.put("totalMenuCount", totalMenuCount);

        // 统计目录数量
        long directoryCount = baseMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getType, 1));
        stats.put("directoryCount", directoryCount);

        // 统计菜单数量
        long menuCount = baseMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getType, 2));
        stats.put("menuCount", menuCount);

        // 统计按钮数量
        long buttonCount = baseMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getType, 3));
        stats.put("buttonCount", buttonCount);

        // 统计正常状态的菜单
        long activeMenuCount = baseMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus, 1));
        stats.put("activeMenuCount", activeMenuCount);

        // 统计禁用的菜单
        long disabledMenuCount = baseMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus, 0));
        stats.put("disabledMenuCount", disabledMenuCount);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getMenuTreeWithButtons() {
        List<SysMenu> menus = list(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getType, 1, 2, 3)
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSort));

        List<SysMenu> treeMenus = buildMenuTree(menus, 0L);

        // 转换为 Map 结构
        return menusToTreeMaps(treeMenus);
    }

    @Override
    public List<Map<String, Object>> getMenusByRoleId(Long roleId) {
        List<SysMenu> menus = baseMapper.selectMenusByRoleId(roleId);
        List<SysMenu> treeMenus = buildMenuTree(menus, 0L);
        return menusToTreeMaps(treeMenus);
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

    /**
     * 将菜单列表转换为树形 Map 结构
     */
    private List<Map<String, Object>> menusToTreeMaps(List<SysMenu> menus) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysMenu menu : menus) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", menu.getMenuId());
            map.put("parentId", menu.getParentId());
            map.put("label", menu.getMenuName());
            map.put("type", menu.getType());
            map.put("icon", menu.getIcon());
            map.put("path", menu.getPath());
            map.put("component", menu.getComponent());
            map.put("permission", menu.getPermission());
            map.put("sort", menu.getSort());
            map.put("visible", menu.getVisible());
            map.put("status", menu.getStatus());

            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                map.put("children", menusToTreeMaps(menu.getChildren()));
            }
            result.add(map);
        }
        return result;
    }
}
package com.micro.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.micro.platform.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单 Mapper 接口
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户 ID 查询菜单权限
     */
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    /**
     * 获取菜单树
     */
    List<SysMenu> selectMenuTree();

    /**
     * 根据角色 ID 查询菜单
     */
    List<SysMenu> selectMenusByRoleId(@Param("roleId") Long roleId);
}

package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysRole;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Set;

/**
 * 角色服务接口
 */
public interface SysRoleService extends IServiceX<SysRole> {

    /**
     * 分页查询角色列表
     */
    Page<SysRole> selectRolePage(SysRole role, Integer pageNum, Integer pageSize);

    /**
     * 根据用户 ID 查询角色 ID 列表
     */
    List<Long> selectRoleIdsByUserId(Long userId);

    /**
     * 根据用户 ID 查询角色标识列表
     */
    Set<String> selectRoleCodesByUserId(Long userId);

    /**
     * 分配菜单权限
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 根据角色 ID 查询菜单 ID 列表
     */
    List<Long> selectMenusByRoleId(Long roleId);

    /**
     * 批量删除角色
     */
    void batchDelete(List<Long> ids);

    /**
     * 导出角色数据
     */
    void exportRole(HttpServletResponse response, SysRole role);

    /**
     * 查询角色列表（支持条件查询）
     */
    List<SysRole> selectRoleList(SysRole role);
}
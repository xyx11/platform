package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysDataPermission;

import java.util.List;

/**
 * 数据权限规则服务接口
 */
public interface SysDataPermissionService extends IServiceX<SysDataPermission> {

    /**
     * 分页查询数据权限规则
     */
    Page<SysDataPermission> selectPermissionPage(SysDataPermission permission, Integer pageNum, Integer pageSize);

    /**
     * 获取角色的数据权限规则
     */
    List<SysDataPermission> selectByRoleId(Long roleId);

    /**
     * 获取角色在指定表上的数据权限规则
     */
    List<SysDataPermission> selectByRoleIdAndTable(Long roleId, String tableName);

    /**
     * 创建数据权限规则
     */
    void createPermission(SysDataPermission permission);

    /**
     * 更新数据权限规则
     */
    void updatePermission(SysDataPermission permission);

    /**
     * 删除数据权限规则
     */
    void deletePermission(Long id);
}
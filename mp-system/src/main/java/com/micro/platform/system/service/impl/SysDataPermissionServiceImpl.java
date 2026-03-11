package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysDataPermission;
import com.micro.platform.system.mapper.SysDataPermissionMapper;
import com.micro.platform.system.service.SysDataPermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据权限规则服务实现
 */
@Service
public class SysDataPermissionServiceImpl extends ServiceImplX<SysDataPermissionMapper, SysDataPermission> implements SysDataPermissionService {

    @Override
    public Page<SysDataPermission> selectPermissionPage(SysDataPermission permission, Integer pageNum, Integer pageSize) {
        Page<SysDataPermission> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDataPermission> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(permission.getRoleId() != null, SysDataPermission::getRoleId, permission.getRoleId())
                .eq(permission.getMenuId() != null, SysDataPermission::getMenuId, permission.getMenuId())
                .eq(permission.getPermissionType() != null, SysDataPermission::getPermissionType, permission.getPermissionType())
                .like(permission.getTableName() != null && !permission.getTableName().isEmpty(),
                        SysDataPermission::getTableName, permission.getTableName())
                .eq(permission.getStatus() != null, SysDataPermission::getStatus, permission.getStatus())
                .orderByDesc(SysDataPermission::getCreateTime);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SysDataPermission> selectByRoleId(Long roleId) {
        LambdaQueryWrapper<SysDataPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDataPermission::getRoleId, roleId)
                .eq(SysDataPermission::getStatus, 1);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<SysDataPermission> selectByRoleIdAndTable(Long roleId, String tableName) {
        LambdaQueryWrapper<SysDataPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDataPermission::getRoleId, roleId)
                .eq(SysDataPermission::getTableName, tableName)
                .eq(SysDataPermission::getStatus, 1);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPermission(SysDataPermission permission) {
        this.save(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermission(SysDataPermission permission) {
        this.updateById(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long id) {
        this.removeById(id);
    }
}
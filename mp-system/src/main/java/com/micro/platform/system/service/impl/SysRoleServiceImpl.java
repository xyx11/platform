package com.micro.platform.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysRole;
import com.micro.platform.system.entity.SysUser;
import com.micro.platform.system.mapper.SysRoleMapper;
import com.micro.platform.system.mapper.SysUserMapper;
import com.micro.platform.system.service.SysRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 */
@Service
public class SysRoleServiceImpl extends ServiceImplX<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysUserMapper sysUserMapper;

    public SysRoleServiceImpl(SysRoleMapper sysRoleMapper, SysUserMapper sysUserMapper) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public Page<SysRole> selectRolePage(SysRole role, Integer pageNum, Integer pageSize) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(role.getRoleName() != null, SysRole::getRoleName, role.getRoleName())
                .like(role.getRoleCode() != null, SysRole::getRoleCode, role.getRoleCode())
                .eq(role.getStatus() != null, SysRole::getStatus, role.getStatus())
                .orderByAsc(SysRole::getSort);
        Page<SysRole> resultPage = baseMapper.selectPage(page, wrapper);

        // 填充用户数量
        for (SysRole r : resultPage.getRecords()) {
            r.setUserCount(getRoleUserCount(r.getRoleId()));
        }

        return resultPage;
    }

    @Override
    public List<Long> selectRoleIdsByUserId(Long userId) {
        return baseMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    public Set<String> selectRoleCodesByUserId(Long userId) {
        List<Long> roleIds = selectRoleIdsByUserId(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return Set.of();
        }
        List<SysRole> roles = baseMapper.selectBatchIds(roleIds);
        return roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 先删除原有的关联
        baseMapper.deleteRoleMenus(roleId);
        // 再插入新的关联
        if (!CollectionUtils.isEmpty(menuIds)) {
            baseMapper.batchInsertRoleMenus(roleId, menuIds);
        }
    }

    @Override
    public List<Long> selectMenusByRoleId(Long roleId) {
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public List<SysRole> selectRoleList(SysRole role) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(role.getRoleName() != null, SysRole::getRoleName, role.getRoleName())
                .like(role.getRoleCode() != null, SysRole::getRoleCode, role.getRoleCode())
                .eq(role.getStatus() != null, SysRole::getStatus, role.getStatus())
                .orderByAsc(SysRole::getSort);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            this.removeByIds(ids);
        }
    }

    @Override
    public byte[] exportRole(SysRole role) {
        try {
            List<SysRole> list = selectRoleList(role);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            EasyExcel.write(os, SysRole.class)
                    .sheet("角色数据")
                    .doWrite(list);
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出角色数据失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getRoleStats(Long roleId) {
        Map<String, Object> stats = new HashMap<>();

        if (roleId == null) {
            // 统计所有角色
            long totalRoleCount = baseMapper.selectCount(null);
            long activeRoleCount = baseMapper.selectCount(new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, 1));
            stats.put("totalRoleCount", totalRoleCount);
            stats.put("activeRoleCount", activeRoleCount);

            // 统计每个角色的用户数量
            List<SysRole> roles = selectRoleList(new SysRole());
            List<Map<String, Object>> roleStats = new ArrayList<>();
            for (SysRole r : roles) {
                Map<String, Object> roleStat = new HashMap<>();
                roleStat.put("roleId", r.getRoleId());
                roleStat.put("roleName", r.getRoleName());
                roleStat.put("roleCode", r.getRoleCode());
                roleStat.put("userCount", getRoleUserCount(r.getRoleId()));
                roleStats.add(roleStat);
            }
            stats.put("roleStats", roleStats);
        } else {
            // 统计指定角色
            SysRole r = getById(roleId);
            if (r == null) {
                throw new BusinessException("角色不存在");
            }

            stats.put("roleId", roleId);
            stats.put("roleName", r.getRoleName());
            stats.put("roleCode", r.getRoleCode());
            stats.put("description", r.getDescription());
            stats.put("dataScope", r.getDataScope());
            stats.put("userCount", getRoleUserCount(roleId));
            stats.put("status", r.getStatus());
            stats.put("sort", r.getSort());
        }

        return stats;
    }

    @Override
    public int getRoleUserCount(Long roleId) {
        if (roleId == null) {
            return 0;
        }
        // 查询拥有该角色的用户数量
        return sysRoleMapper.countUsersByRoleId(roleId);
    }

    @Override
    public List<Map<String, Object>> getRoleUsers(Long roleId) {
        List<Map<String, Object>> users = new ArrayList<>();
        if (roleId == null) {
            return users;
        }

        // 查询拥有该角色的用户列表
        List<SysUser> userList = sysUserMapper.selectUsersByRoleId(roleId);
        for (SysUser user : userList) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", user.getUserId());
            userMap.put("username", user.getUsername());
            userMap.put("nickname", user.getNickname());
            userMap.put("phone", user.getPhone());
            userMap.put("email", user.getEmail());
            userMap.put("status", user.getStatus());
            users.add(userMap);
        }
        return users;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUsers(Long roleId, List<Long> userIds) {
        // 检查角色是否存在
        SysRole role = getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 先删除原有的用户角色关联
        sysUserMapper.deleteUserRoleBatch(roleId);

        // 再插入新的关联
        if (!CollectionUtils.isEmpty(userIds)) {
            sysUserMapper.batchInsertUserRole(roleId, userIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUsers(Long roleId, List<Long> userIds) {
        // 检查角色是否存在
        SysRole role = getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 从角色移除用户
        if (!CollectionUtils.isEmpty(userIds)) {
            sysUserMapper.batchDeleteUserRole(roleId, userIds);
        }
    }
}
package com.micro.platform.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysRole;
import com.micro.platform.system.mapper.SysRoleMapper;
import com.micro.platform.system.service.SysRoleService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 */
@Service
public class SysRoleServiceImpl extends ServiceImplX<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;

    public SysRoleServiceImpl(SysRoleMapper sysRoleMapper) {
        this.sysRoleMapper = sysRoleMapper;
    }

    @Override
    public Page<SysRole> selectRolePage(SysRole role, Integer pageNum, Integer pageSize) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(role.getRoleName() != null, SysRole::getRoleName, role.getRoleName())
                .like(role.getRoleCode() != null, SysRole::getRoleCode, role.getRoleCode())
                .eq(role.getStatus() != null, SysRole::getStatus, role.getStatus())
                .orderByAsc(SysRole::getSort);
        return baseMapper.selectPage(page, wrapper);
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
    public void exportRole(HttpServletResponse response, SysRole role) {
        try {
            List<SysRole> list = selectRoleList(role);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("角色数据", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), SysRole.class)
                    .sheet("角色数据")
                    .doWrite(list);
        } catch (Exception e) {
            throw new RuntimeException("导出角色数据失败：" + e.getMessage());
        }
    }
}
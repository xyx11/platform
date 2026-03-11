package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.core.interceptor.TenantContext;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysTenant;
import com.micro.platform.system.mapper.SysTenantMapper;
import com.micro.platform.system.service.SysTenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 租户服务实现
 */
@Service
public class SysTenantServiceImpl extends ServiceImplX<SysTenantMapper, SysTenant> implements SysTenantService {

    @Override
    public Page<SysTenant> selectTenantPage(SysTenant tenant, Integer pageNum, Integer pageSize) {
        Page<SysTenant> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(tenant.getName() != null && !tenant.getName().isEmpty(), SysTenant::getName, tenant.getName())
                .like(tenant.getCode() != null && !tenant.getCode().isEmpty(), SysTenant::getCode, tenant.getCode())
                .eq(tenant.getStatus() != null, SysTenant::getStatus, tenant.getStatus())
                .orderByDesc(SysTenant::getCreateTime);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public SysTenant selectByCode(String code) {
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenant::getCode, code);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTenant(SysTenant tenant) {
        // 检查编码是否已存在
        SysTenant existTenant = selectByCode(tenant.getCode());
        if (existTenant != null) {
            throw new BusinessException("租户编码已存在");
        }

        tenant.setStatus(1);
        this.save(tenant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTenant(SysTenant tenant) {
        SysTenant existTenant = getById(tenant.getId());
        if (existTenant == null) {
            throw new BusinessException("租户不存在");
        }

        // 如果修改了编码，检查新编码是否已被使用
        if (!existTenant.getCode().equals(tenant.getCode())) {
            SysTenant codeTenant = selectByCode(tenant.getCode());
            if (codeTenant != null && !codeTenant.getId().equals(tenant.getId())) {
                throw new BusinessException("租户编码已存在");
            }
        }

        this.updateById(tenant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTenant(Long id) {
        this.removeById(id);
    }

    @Override
    public void switchTenant(Long tenantId) {
        SysTenant tenant = getById(tenantId);
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }

        if (tenant.getStatus() == 0) {
            throw new BusinessException("租户已禁用");
        }

        if (tenant.getExpireTime() != null && tenant.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("租户已过期");
        }

        // 设置当前租户
        TenantContext.setCurrentTenantId(tenantId);
    }
}
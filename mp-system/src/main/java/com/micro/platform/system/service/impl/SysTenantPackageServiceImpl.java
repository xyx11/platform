package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysTenantPackage;
import com.micro.platform.system.mapper.SysTenantPackageMapper;
import com.micro.platform.system.service.SysTenantPackageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 租户套餐服务实现
 */
@Service
public class SysTenantPackageServiceImpl extends ServiceImplX<SysTenantPackageMapper, SysTenantPackage> implements SysTenantPackageService {

    @Override
    public Page<SysTenantPackage> selectPackagePage(SysTenantPackage pkg, Integer pageNum, Integer pageSize) {
        Page<SysTenantPackage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysTenantPackage> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(pkg.getName() != null && !pkg.getName().isEmpty(), SysTenantPackage::getName, pkg.getName())
                .like(pkg.getCode() != null && !pkg.getCode().isEmpty(), SysTenantPackage::getCode, pkg.getCode())
                .eq(pkg.getPackageType() != null, SysTenantPackage::getPackageType, pkg.getPackageType())
                .eq(pkg.getStatus() != null, SysTenantPackage::getStatus, pkg.getStatus())
                .orderByAsc(SysTenantPackage::getSort);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public SysTenantPackage selectByCode(String code) {
        LambdaQueryWrapper<SysTenantPackage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenantPackage::getCode, code);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPackage(SysTenantPackage pkg) {
        // 检查编码是否已存在
        SysTenantPackage exist = selectByCode(pkg.getCode());
        if (exist != null) {
            throw new BusinessException("套餐编码已存在");
        }

        pkg.setStatus(1);
        this.save(pkg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePackage(SysTenantPackage pkg) {
        SysTenantPackage exist = getById(pkg.getId());
        if (exist == null) {
            throw new BusinessException("套餐不存在");
        }

        // 如果修改了编码，检查新编码是否已被使用
        if (!exist.getCode().equals(pkg.getCode())) {
            SysTenantPackage codePkg = selectByCode(pkg.getCode());
            if (codePkg != null && !codePkg.getId().equals(pkg.getId())) {
                throw new BusinessException("套餐编码已存在");
            }
        }

        this.updateById(pkg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disablePackage(Long id) {
        SysTenantPackage pkg = getById(id);
        if (pkg == null) {
            throw new BusinessException("套餐不存在");
        }

        pkg.setStatus(0);
        this.updateById(pkg);
    }

    @Override
    public List<SysTenantPackage> selectAvailablePackages() {
        LambdaQueryWrapper<SysTenantPackage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenantPackage::getStatus, 1)
                .orderByAsc(SysTenantPackage::getSort);
        return baseMapper.selectList(wrapper);
    }
}
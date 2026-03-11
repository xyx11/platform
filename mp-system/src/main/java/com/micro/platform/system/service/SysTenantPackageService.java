package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysTenantPackage;

import java.util.List;

/**
 * 租户套餐服务接口
 */
public interface SysTenantPackageService extends IServiceX<SysTenantPackage> {

    /**
     * 分页查询套餐列表
     */
    Page<SysTenantPackage> selectPackagePage(SysTenantPackage pkg, Integer pageNum, Integer pageSize);

    /**
     * 根据编码查询套餐
     */
    SysTenantPackage selectByCode(String code);

    /**
     * 创建套餐
     */
    void createPackage(SysTenantPackage pkg);

    /**
     * 更新套餐
     */
    void updatePackage(SysTenantPackage pkg);

    /**
     * 停用套餐
     */
    void disablePackage(Long id);

    /**
     * 获取可用套餐列表
     */
    List<SysTenantPackage> selectAvailablePackages();
}
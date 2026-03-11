package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysTenant;

/**
 * 租户服务接口
 */
public interface SysTenantService extends IServiceX<SysTenant> {

    /**
     * 分页查询租户列表
     */
    Page<SysTenant> selectTenantPage(SysTenant tenant, Integer pageNum, Integer pageSize);

    /**
     * 根据编码查询租户
     */
    SysTenant selectByCode(String code);

    /**
     * 创建租户
     */
    void createTenant(SysTenant tenant);

    /**
     * 更新租户
     */
    void updateTenant(SysTenant tenant);

    /**
     * 删除租户
     */
    void deleteTenant(Long id);

    /**
     * 切换租户
     */
    void switchTenant(Long tenantId);
}
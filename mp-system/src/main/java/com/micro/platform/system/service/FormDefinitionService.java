package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.FormDefinition;
import com.micro.platform.system.entity.FormData;

import java.util.Map;

/**
 * 表单定义服务接口
 */
public interface FormDefinitionService extends IServiceX<FormDefinition> {

    /**
     * 分页查询表单定义列表
     */
    Page<FormDefinition> selectFormDefinitionPage(FormDefinition formDefinition, Integer pageNum, Integer pageSize);

    /**
     * 根据编码查询表单定义
     */
    FormDefinition selectByCode(String formCode);

    /**
     * 创建表单定义
     */
    void createFormDefinition(FormDefinition formDefinition);

    /**
     * 更新表单定义
     */
    void updateFormDefinition(FormDefinition formDefinition);

    /**
     * 发布表单
     */
    void publishForm(Long id);

    /**
     * 停用表单
     */
    void disableForm(Long id);
}
package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.micro.platform.system.entity.FormDefinition;

/**
 * 表单定义服务接口
 */
public interface FormDefinitionService extends IService<FormDefinition> {

    /**
     * 根据编码查询表单
     * @param formCode 表单编码
     * @return 表单定义
     */
    FormDefinition selectByCode(String formCode);
}

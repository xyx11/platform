package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.micro.platform.system.entity.FormDefinition;
import com.micro.platform.system.mapper.FormDefinitionMapper;
import com.micro.platform.system.service.FormDefinitionService;
import org.springframework.stereotype.Service;

/**
 * 表单定义服务实现
 */
@Service
public class FormDefinitionServiceImpl extends ServiceImpl<FormDefinitionMapper, FormDefinition> implements FormDefinitionService {

    @Override
    public FormDefinition selectByCode(String formCode) {
        LambdaQueryWrapper<FormDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormDefinition::getFormCode, formCode)
               .eq(FormDefinition::getDelFlag, 0);
        return this.getOne(wrapper);
    }
}

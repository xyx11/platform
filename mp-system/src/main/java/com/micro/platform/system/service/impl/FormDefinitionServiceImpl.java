package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.FormDefinition;
import com.micro.platform.system.mapper.FormDefinitionMapper;
import com.micro.platform.system.service.FormDefinitionService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 表单定义服务实现
 */
@Service
public class FormDefinitionServiceImpl extends ServiceImplX<FormDefinitionMapper, FormDefinition> implements FormDefinitionService {

    @Override
    public Page<FormDefinition> selectFormDefinitionPage(FormDefinition formDefinition, Integer pageNum, Integer pageSize) {
        Page<FormDefinition> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FormDefinition> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(formDefinition.getFormName() != null && !formDefinition.getFormName().isEmpty(),
                        FormDefinition::getFormName, formDefinition.getFormName())
                .like(formDefinition.getFormCode() != null && !formDefinition.getFormCode().isEmpty(),
                        FormDefinition::getFormCode, formDefinition.getFormCode())
                .eq(formDefinition.getFormType() != null, FormDefinition::getFormType, formDefinition.getFormType())
                .eq(formDefinition.getStatus() != null, FormDefinition::getStatus, formDefinition.getStatus())
                .orderByDesc(FormDefinition::getCreateTime);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Cacheable(value = "formDefinitions", key = "#formCode", unless = "#result == null")
    public FormDefinition selectByCode(String formCode) {
        LambdaQueryWrapper<FormDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormDefinition::getFormCode, formCode);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "formDefinitions", key = "#formDefinition.formCode")
    public void createFormDefinition(FormDefinition formDefinition) {
        // 检查编码是否已存在
        FormDefinition existForm = selectByCode(formDefinition.getFormCode());
        if (existForm != null) {
            throw new BusinessException("表单编码已存在");
        }

        formDefinition.setStatus(0); // 草稿状态
        formDefinition.setVersion(1);

        Long userId = SecurityUtil.getUserId();
        if (userId != null) {
            formDefinition.setCreateBy(userId);
            formDefinition.setCreateByName(SecurityUtil.getUsername());
        }

        this.save(formDefinition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "formDefinitions", key = "#formDefinition.formCode")
    public void updateFormDefinition(FormDefinition formDefinition) {
        FormDefinition existForm = getById(formDefinition.getId());
        if (existForm == null) {
            throw new BusinessException("表单不存在");
        }

        // 如果修改了编码，检查新编码是否已被使用
        if (!existForm.getFormCode().equals(formDefinition.getFormCode())) {
            FormDefinition codeForm = selectByCode(formDefinition.getFormCode());
            if (codeForm != null && !codeForm.getId().equals(formDefinition.getId())) {
                throw new BusinessException("表单编码已存在");
            }
        }

        formDefinition.setUpdateTime(LocalDateTime.now());
        Long userId = SecurityUtil.getUserId();
        if (userId != null) {
            formDefinition.setUpdateBy(userId);
            formDefinition.setUpdateByName(SecurityUtil.getUsername());
        }

        this.updateById(formDefinition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "formDefinitions", key = "#id")
    public void publishForm(Long id) {
        FormDefinition form = getById(id);
        if (form == null) {
            throw new BusinessException("表单不存在");
        }

        form.setStatus(1); // 发布状态
        form.setUpdateTime(LocalDateTime.now());
        this.updateById(form);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "formDefinitions", key = "#id")
    public void disableForm(Long id) {
        FormDefinition form = getById(id);
        if (form == null) {
            throw new BusinessException("表单不存在");
        }

        form.setStatus(2); // 停用状态
        form.setUpdateTime(LocalDateTime.now());
        this.updateById(form);
    }
}
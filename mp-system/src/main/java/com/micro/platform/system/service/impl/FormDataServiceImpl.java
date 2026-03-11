package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.FormData;
import com.micro.platform.system.mapper.FormDataMapper;
import com.micro.platform.system.service.FormDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 表单数据服务实现
 */
@Service
public class FormDataServiceImpl extends ServiceImplX<FormDataMapper, FormData> implements FormDataService {

    private final ObjectMapper objectMapper;

    public FormDataServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Page<FormData> selectFormDataPage(Long formId, Integer pageNum, Integer pageSize) {
        Page<FormData> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FormData> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(formId != null, FormData::getFormId, formId)
                .orderByDesc(FormData::getCreateTime);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public FormData getFormDataDetail(Long id) {
        return this.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitFormData(Long formId, Map<String, Object> data) {
        try {
            FormData formData = new FormData();
            formData.setFormId(formId);
            formData.setFormData(objectMapper.writeValueAsString(data));
            formData.setStatus(1); // 已提交
            formData.setSubmitTime(LocalDateTime.now());

            Long userId = SecurityUtil.getUserId();
            if (userId != null) {
                formData.setSubmitterId(userId);
                formData.setSubmitterName(SecurityUtil.getUsername());
            }

            this.save(formData);
        } catch (JsonProcessingException e) {
            throw new BusinessException("表单数据序列化失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFormData(Long formId, Map<String, Object> data) {
        try {
            // 检查是否存在草稿
            LambdaQueryWrapper<FormData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FormData::getFormId, formId)
                    .eq(FormData::getStatus, 0); // 草稿状态

            Long userId = SecurityUtil.getUserId();
            if (userId != null) {
                wrapper.eq(FormData::getSubmitterId, userId);
            }

            FormData existDraft = this.getOne(wrapper);

            if (existDraft != null) {
                // 更新草稿
                existDraft.setFormData(objectMapper.writeValueAsString(data));
                existDraft.setUpdateTime(LocalDateTime.now());
                this.updateById(existDraft);
            } else {
                // 新建草稿
                FormData formData = new FormData();
                formData.setFormId(formId);
                formData.setFormData(objectMapper.writeValueAsString(data));
                formData.setStatus(0); // 草稿状态

                if (userId != null) {
                    formData.setSubmitterId(userId);
                    formData.setSubmitterName(SecurityUtil.getUsername());
                }

                this.save(formData);
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException("表单数据序列化失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditFormData(Long id, Integer status, String remark) {
        FormData formData = this.getById(id);
        if (formData == null) {
            throw new BusinessException("表单数据不存在");
        }

        formData.setStatus(status);
        formData.setRemark(remark);
        formData.setUpdateTime(LocalDateTime.now());
        this.updateById(formData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFormData(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            this.removeByIds(ids);
        }
    }
}
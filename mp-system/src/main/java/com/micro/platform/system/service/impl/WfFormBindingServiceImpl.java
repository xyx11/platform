package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.WfFormBinding;
import com.micro.platform.system.mapper.WfFormBindingMapper;
import com.micro.platform.system.service.WfFormBindingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 工作流表单绑定服务实现
 */
@Service
public class WfFormBindingServiceImpl extends ServiceImpl<WfFormBindingMapper, WfFormBinding> implements WfFormBindingService {

    @Override
    public Page<WfFormBinding> selectFormBindingPage(WfFormBinding formBinding, Integer pageNum, Integer pageSize) {
        Page<WfFormBinding> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WfFormBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(formBinding.getProcessDefinitionKey()), WfFormBinding::getProcessDefinitionKey, formBinding.getProcessDefinitionKey())
                .eq(StringUtils.hasText(formBinding.getTaskDefinitionKey()), WfFormBinding::getTaskDefinitionKey, formBinding.getTaskDefinitionKey())
                .eq(formBinding.getFormType() != null, WfFormBinding::getFormType, formBinding.getFormType())
                .eq(formBinding.getStatus() != null, WfFormBinding::getStatus, formBinding.getStatus())
                .eq(WfFormBinding::getDeleted, 0)
                .orderByDesc(WfFormBinding::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindForm(WfFormBinding formBinding) {
        // 检查是否已存在绑定
        LambdaQueryWrapper<WfFormBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfFormBinding::getProcessDefinitionKey, formBinding.getProcessDefinitionKey())
                .eq(WfFormBinding::getTaskDefinitionKey, formBinding.getTaskDefinitionKey())
                .eq(WfFormBinding::getFormType, formBinding.getFormType())
                .eq(WfFormBinding::getDeleted, 0);
        WfFormBinding existing = baseMapper.selectOne(wrapper);

        if (existing != null) {
            // 更新现有绑定
            existing.setFormCode(formBinding.getFormCode());
            existing.setFormName(formBinding.getFormName());
            existing.setStatus(1);
            existing.setUpdateBy(SecurityUtil.getUserId());
            existing.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(existing);
        } else {
            // 新增绑定
            formBinding.setBindingKey(generateBindingKey(formBinding.getProcessDefinitionKey(), formBinding.getTaskDefinitionKey(), formBinding.getFormType()));
            formBinding.setStatus(1);
            formBinding.setCreateBy(SecurityUtil.getUserId());
            formBinding.setCreateTime(LocalDateTime.now());
            formBinding.setDeleted(0);
            baseMapper.insert(formBinding);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindForm(String processDefinitionKey, String taskDefinitionKey, Integer formType) {
        LambdaQueryWrapper<WfFormBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfFormBinding::getProcessDefinitionKey, processDefinitionKey)
                .eq(WfFormBinding::getTaskDefinitionKey, taskDefinitionKey)
                .eq(WfFormBinding::getFormType, formType)
                .eq(WfFormBinding::getDeleted, 0);
        WfFormBinding existing = baseMapper.selectOne(wrapper);

        if (existing != null) {
            baseMapper.deleteById(existing.getId());
        }
    }

    @Override
    public List<Map<String, Object>> getAvailableForms(String processDefinitionKey) {
        // 获取所有已发布的表单定义
        LambdaQueryWrapper<WfFormBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfFormBinding::getProcessDefinitionKey, processDefinitionKey)
                .eq(WfFormBinding::getStatus, 1)
                .eq(WfFormBinding::getDeleted, 0);
        List<WfFormBinding> bindings = baseMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (WfFormBinding binding : bindings) {
            Map<String, Object> map = new HashMap<>();
            map.put("formCode", binding.getFormCode());
            map.put("formName", binding.getFormName());
            map.put("formType", binding.getFormType());
            map.put("taskDefinitionKey", binding.getTaskDefinitionKey());
            result.add(map);
        }
        return result;
    }

    @Override
    public WfFormBinding getTaskFormBinding(String processDefinitionKey, String taskDefinitionKey, Integer formType) {
        LambdaQueryWrapper<WfFormBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfFormBinding::getProcessDefinitionKey, processDefinitionKey)
                .eq(WfFormBinding::getTaskDefinitionKey, taskDefinitionKey)
                .eq(WfFormBinding::getFormType, formType)
                .eq(WfFormBinding::getStatus, 1)
                .eq(WfFormBinding::getDeleted, 0);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public List<WfFormBinding> getProcessFormBindings(String processDefinitionKey) {
        LambdaQueryWrapper<WfFormBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfFormBinding::getProcessDefinitionKey, processDefinitionKey)
                .eq(WfFormBinding::getStatus, 1)
                .eq(WfFormBinding::getDeleted, 0)
                .orderByDesc(WfFormBinding::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 生成绑定 Key
     */
    private String generateBindingKey(String processDefinitionKey, String taskDefinitionKey, Integer formType) {
        return processDefinitionKey + ":" + taskDefinitionKey + ":" + formType;
    }
}
package com.micro.platform.system.service.impl;

import com.micro.platform.system.entity.WorkflowFormBinding;
import com.micro.platform.system.mapper.WorkflowFormBindingMapper;
import com.micro.platform.system.service.WorkflowFormService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 工作流表单服务实现
 */
@Service
public class WorkflowFormServiceImpl implements WorkflowFormService {

    private final WorkflowFormBindingMapper workflowFormBindingMapper;

    public WorkflowFormServiceImpl(WorkflowFormBindingMapper workflowFormBindingMapper) {
        this.workflowFormBindingMapper = workflowFormBindingMapper;
    }

    @Override
    public List<WorkflowFormBinding> getFormBindings(String processDefinitionKey, String taskDefinitionKey) {
        return workflowFormBindingMapper.selectBindings(processDefinitionKey, taskDefinitionKey);
    }

    @Override
    public List<WorkflowFormBinding> getProcessBindings(String processDefinitionKey) {
        return workflowFormBindingMapper.selectProcessBindings(processDefinitionKey);
    }

    @Override
    public void bindForm(WorkflowFormBinding binding) {
        if (binding.getProcessDefinitionKey() == null || binding.getFormKey() == null) {
            throw new IllegalArgumentException("流程定义 Key 和表单 Key 不能为空");
        }

        // 如果是启动表单
        if (binding.getTaskDefinitionKey() == null || binding.getTaskDefinitionKey().isEmpty()) {
            binding.setFormType(1); // 启动表单
            workflowFormBindingMapper.insert(binding);
        } else {
            // 任务表单
            binding.setFormType(2); // 办理表单
            // 先删除旧的绑定
            workflowFormBindingMapper.deleteByTaskKey(binding.getProcessDefinitionKey(), binding.getTaskDefinitionKey());
            workflowFormBindingMapper.insert(binding);
        }
    }

    @Override
    public void unbindForm(String processDefinitionKey, String taskDefinitionKey) {
        if (taskDefinitionKey == null || taskDefinitionKey.isEmpty()) {
            // 解除整个流程的绑定
            workflowFormBindingMapper.deleteByProcessKey(processDefinitionKey);
        } else {
            // 解除特定任务的绑定
            workflowFormBindingMapper.deleteByTaskKey(processDefinitionKey, taskDefinitionKey);
        }
    }

    @Override
    public List<Map<String, Object>> getAvailableForms(String processDefinitionKey) {
        // 这里应该从表单定义表中查询可用的表单
        // 暂时返回空列表，后续需要实现表单定义表
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getStartForm(String processKey) {
        WorkflowFormBinding binding = workflowFormBindingMapper.selectStartForm(processKey);
        if (binding == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("formKey", binding.getFormKey());
        result.put("formName", binding.getFormName());
        result.put("formContent", binding.getFormSchema());
        return result;
    }

    @Override
    public Map<String, Object> getTaskForm(String taskDefinitionKey) {
        WorkflowFormBinding binding = workflowFormBindingMapper.selectTaskForm(taskDefinitionKey);
        if (binding == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("formKey", binding.getFormKey());
        result.put("formName", binding.getFormName());
        result.put("formContent", binding.getFormSchema());
        return result;
    }
}

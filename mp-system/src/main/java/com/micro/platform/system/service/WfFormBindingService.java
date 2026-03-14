package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.micro.platform.system.entity.WfFormBinding;

import java.util.List;
import java.util.Map;

/**
 * 工作流表单绑定服务接口
 */
public interface WfFormBindingService extends IService<WfFormBinding> {

    /**
     * 分页查询表单绑定列表
     */
    Page<WfFormBinding> selectFormBindingPage(WfFormBinding formBinding, Integer pageNum, Integer pageSize);

    /**
     * 绑定表单
     */
    void bindForm(WfFormBinding formBinding);

    /**
     * 解除绑定
     */
    void unbindForm(String processDefinitionKey, String taskDefinitionKey, Integer formType);

    /**
     * 获取流程定义可用的表单列表
     */
    List<Map<String, Object>> getAvailableForms(String processDefinitionKey);

    /**
     * 获取任务节点的表单绑定
     */
    WfFormBinding getTaskFormBinding(String processDefinitionKey, String taskDefinitionKey, Integer formType);

    /**
     * 获取流程的所有表单绑定
     */
    List<WfFormBinding> getProcessFormBindings(String processDefinitionKey);
}
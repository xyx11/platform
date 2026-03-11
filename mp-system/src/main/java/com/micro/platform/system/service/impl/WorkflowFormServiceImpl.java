package com.micro.platform.system.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.system.entity.FormData;
import com.micro.platform.system.entity.WfFormBinding;
import com.micro.platform.system.mapper.FormDataMapper;
import com.micro.platform.system.mapper.WfFormBindingMapper;
import com.micro.platform.system.service.FormDefinitionService;
import com.micro.platform.system.service.WorkflowFormService;
import com.micro.platform.system.entity.FormDefinition;
import com.micro.platform.system.service.WorkflowService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 流程表单集成服务实现
 */
@Service
public class WorkflowFormServiceImpl implements WorkflowFormService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowFormServiceImpl.class);

    private final WorkflowService workflowService;
    private final FormDefinitionService formDefinitionService;
    private final FormDataMapper formDataMapper;
    private final WfFormBindingMapper wfFormBindingMapper;
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    public WorkflowFormServiceImpl(WorkflowService workflowService,
                                   FormDefinitionService formDefinitionService,
                                   FormDataMapper formDataMapper,
                                   WfFormBindingMapper wfFormBindingMapper,
                                   RuntimeService runtimeService,
                                   TaskService taskService) {
        this.workflowService = workflowService;
        this.formDefinitionService = formDefinitionService;
        this.formDataMapper = formDataMapper;
        this.wfFormBindingMapper = wfFormBindingMapper;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindFormToProcess(String processDefinitionKey, String taskDefinitionKey,
                                   String formCode, Integer formType) {
        // 获取表单定义
        FormDefinition formDefinition = formDefinitionService.selectByCode(formCode);
        if (formDefinition == null) {
            throw new BusinessException("表单不存在");
        }

        // 生成绑定关系 Key
        String bindingKey = getBindingKey(processDefinitionKey, taskDefinitionKey, formType);

        // 检查是否已存在绑定关系
        LambdaQueryWrapper<WfFormBinding> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WfFormBinding::getBindingKey, bindingKey);
        WfFormBinding existing = wfFormBindingMapper.selectOne(queryWrapper);

        if (existing != null) {
            // 更新现有绑定
            existing.setFormCode(formCode);
            existing.setFormName(formDefinition.getFormName());
            existing.setFormType(formType);
            existing.setUpdateTime(LocalDateTime.now());
            wfFormBindingMapper.updateById(existing);
        } else {
            // 创建新绑定
            WfFormBinding binding = new WfFormBinding();
            binding.setProcessDefinitionKey(processDefinitionKey);
            binding.setTaskDefinitionKey(taskDefinitionKey);
            binding.setFormCode(formCode);
            binding.setFormName(formDefinition.getFormName());
            binding.setFormType(formType);
            binding.setBindingKey(bindingKey);
            binding.setStatus(1);
            wfFormBindingMapper.insert(binding);
        }

        log.info("表单绑定：流程={}，任务={}，表单={}", processDefinitionKey, taskDefinitionKey, formCode);
    }

    @Override
    public FormDefinition getBoundForm(String processDefinitionKey, String taskDefinitionKey,
                                        Integer formType) {
        // 从数据库获取绑定关系
        String bindingKey = getBindingKey(processDefinitionKey, taskDefinitionKey, formType);

        LambdaQueryWrapper<WfFormBinding> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WfFormBinding::getBindingKey, bindingKey)
                   .eq(WfFormBinding::getStatus, 1);

        WfFormBinding binding = wfFormBindingMapper.selectOne(queryWrapper);
        if (binding == null) {
            return null;
        }

        // 根据表单编码获取表单定义
        return formDefinitionService.selectByCode(binding.getFormCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessInstance startProcessWithForm(String processDefinitionKey, String businessKey,
                                                 String formCode, Map<String, Object> formData) {
        // 1. 获取表单定义
        FormDefinition formDefinition = formDefinitionService.selectByCode(formCode);
        if (formDefinition == null) {
            throw new BusinessException("表单不存在");
        }

        // 2. 准备流程变量
        Map<String, Object> variables = new HashMap<>();

        // 3. 将表单数据转换为流程变量
        if (formData != null) {
            // 将表单数据扁平化，作为流程变量
            for (Map.Entry<String, Object> entry : formData.entrySet()) {
                variables.put(entry.getKey(), entry.getValue());
            }

            // 保存完整的表单数据 JSON
            variables.put("formData", JSONUtil.toJsonStr(formData));
            variables.put("formCode", formCode);
        }

        // 4. 启动流程
        ProcessInstance processInstance = workflowService.startProcess(
                processDefinitionKey, businessKey, variables);

        // 5. 保存表单提交记录
        saveFormDataRecord(processInstance.getId(), formCode, formData, 1);

        log.info("启动流程并提交表单：processInstanceId={}, formCode={}",
                processInstance.getId(), formCode);

        return processInstance;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTaskWithForm(String taskId, String formCode,
                                      Map<String, Object> formData, Boolean saveDraft) {
        // 1. 获取任务信息
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        String processInstanceId = task.getProcessInstanceId();
        String taskDefinitionKey = task.getTaskDefinitionKey();

        // 2. 如果是保存草稿，不完成任务
        if (Boolean.TRUE.equals(saveDraft)) {
            saveFormDataDraft(processInstanceId, taskDefinitionKey, formCode, formData);
            log.info("保存任务表单草稿：taskId={}, processInstanceId={}", taskId, processInstanceId);
            return;
        }

        // 3. 准备任务完成变量
        Map<String, Object> variables = new HashMap<>();

        // 4. 将表单数据转换为流程变量
        if (formData != null) {
            for (Map.Entry<String, Object> entry : formData.entrySet()) {
                variables.put(entry.getKey(), entry.getValue());
            }
            variables.put("formData", JSONUtil.toJsonStr(formData));
            variables.put("formCode", formCode);
        }

        // 5. 完成任务
        workflowService.completeTask(taskId, variables);

        // 6. 保存表单提交记录
        saveFormDataRecord(processInstanceId, formCode, formData, 2);

        log.info("完成任务并提交表单：taskId={}, processInstanceId={}", taskId, processInstanceId);
    }

    @Override
    public FormDefinition getTaskForm(String taskId) {
        // 1. 获取任务信息
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        String processDefinitionKey = task.getProcessDefinitionKey();
        String taskDefinitionKey = task.getTaskDefinitionKey();

        // 2. 获取绑定的表单（办理表单类型）
        FormDefinition formDefinition = getBoundForm(processDefinitionKey, taskDefinitionKey, 2);

        if (formDefinition != null) {
            return formDefinition;
        }

        // 3. 如果没有绑定办理表单，尝试获取启动表单
        formDefinition = getBoundForm(processDefinitionKey, taskDefinitionKey, 1);
        if (formDefinition != null) {
            return formDefinition;
        }

        // 4. 如果都没有，返回 null
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFormDataDraft(String processInstanceId, String taskDefinitionKey,
                                   String formCode, Map<String, Object> formData) {
        FormData draft = new FormData();
        draft.setProcessInstanceId(processInstanceId);
        draft.setFormCode(formCode);
        draft.setFormData(JSONUtil.toJsonStr(formData));
        draft.setStatus(0); // 草稿状态
        draft.setCreateTime(LocalDateTime.now());

        // 检查是否已有草稿
        LambdaQueryWrapper<FormData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FormData::getProcessInstanceId, processInstanceId)
                   .eq(FormData::getFormCode, formCode)
                   .eq(FormData::getStatus, 0);
        FormData existingDraft = formDataMapper.selectOne(queryWrapper);

        if (existingDraft != null) {
            draft.setId(existingDraft.getId());
            draft.setUpdateTime(LocalDateTime.now());
            formDataMapper.updateById(draft);
        } else {
            formDataMapper.insert(draft);
        }
    }

    @Override
    public FormData getFormDataDraft(String processInstanceId, String taskDefinitionKey) {
        LambdaQueryWrapper<FormData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FormData::getProcessInstanceId, processInstanceId)
                   .eq(FormData::getStatus, 0)
                   .orderByDesc(FormData::getCreateTime)
                   .last("LIMIT 1");

        return formDataMapper.selectOne(queryWrapper);
    }

    @Override
    public List<FormData> getProcessFormHistory(String processInstanceId) {
        LambdaQueryWrapper<FormData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FormData::getProcessInstanceId, processInstanceId)
                   .eq(FormData::getStatus, 1) // 只查询已提交的
                   .orderByAsc(FormData::getSubmitTime);

        return formDataMapper.selectList(queryWrapper);
    }

    /**
     * 生成绑定关系的 Key
     */
    private String getBindingKey(String processDefinitionKey, String taskDefinitionKey, Integer formType) {
        return String.format("%s:%s:%s", processDefinitionKey, taskDefinitionKey, formType);
    }

    /**
     * 保存表单数据记录
     */
    private void saveFormDataRecord(String processInstanceId, String formCode,
                                    Map<String, Object> formData, Integer submitType) {
        FormData formDataEntity = new FormData();
        formDataEntity.setProcessInstanceId(processInstanceId);
        formDataEntity.setFormCode(formCode);
        formDataEntity.setFormData(JSONUtil.toJsonStr(formData));
        formDataEntity.setStatus(1); // 已提交
        formDataEntity.setSubmitTime(LocalDateTime.now());
        formDataEntity.setSubmitType(submitType); // 1-启动表单 2-办理表单

        formDataMapper.insert(formDataEntity);
    }
}
package com.micro.platform.system.service.impl;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.system.service.WorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.micro.platform.system.entity.FormDefinition;
import com.micro.platform.system.entity.WorkflowFormBinding;
import com.micro.platform.system.mapper.FormDefinitionMapper;
import com.micro.platform.system.mapper.WorkflowFormBindingMapper;

/**
 * 工作流服务实现
 */
@Service
public class WorkflowServiceImpl implements WorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowServiceImpl.class);

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;
    private final WorkflowFormBindingMapper workflowFormBindingMapper;
    private final FormDefinitionMapper formDefinitionMapper;

    public WorkflowServiceImpl(RuntimeService runtimeService,
                               TaskService taskService,
                               HistoryService historyService,
                               RepositoryService repositoryService,
                               WorkflowFormBindingMapper workflowFormBindingMapper,
                               FormDefinitionMapper formDefinitionMapper) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.historyService = historyService;
        this.repositoryService = repositoryService;
        this.workflowFormBindingMapper = workflowFormBindingMapper;
        this.formDefinitionMapper = formDefinitionMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessInstance startProcess(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
        log.info("启动流程：{}, businessKey: {}", processDefinitionKey, businessKey);
        return runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessInstance startProcessById(String processDefinitionId, String businessKey, Map<String, Object> variables) {
        log.info("启动流程实例，processDefinitionId: {}, businessKey: {}", processDefinitionId, businessKey);
        return runtimeService.startProcessInstanceById(processDefinitionId, businessKey, variables);
    }

    @Override
    public List<Task> getTodoTasks(String userId) {
        return taskService.createTaskQuery()
                .taskAssignee(userId)
                .orderByTaskPriority()
                .desc()
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(String taskId, Map<String, Object> variables) {
        log.info("完成任务：{}", taskId);
        taskService.complete(taskId, variables);
    }

    @Override
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        return runtimeService.getVariables(processInstanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setProcessVariables(String processInstanceId, Map<String, Object> variables) {
        runtimeService.setVariables(processInstanceId, variables);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProcess(String processInstanceId, String deleteReason) {
        log.info("删除流程实例：{}, 原因：{}", processInstanceId, deleteReason);
        runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void suspendProcess(String processInstanceId) {
        log.info("挂起流程实例：{}", processInstanceId);
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateProcess(String processInstanceId) {
        log.info("激活流程实例：{}", processInstanceId);
        runtimeService.activateProcessInstanceById(processInstanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void suspendProcessDefinition(String processDefinitionId) {
        log.info("挂起流程定义：{}", processDefinitionId);
        repositoryService.suspendProcessDefinitionById(processDefinitionId, true, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateProcessDefinition(String processDefinitionId) {
        log.info("激活流程定义：{}", processDefinitionId);
        repositoryService.activateProcessDefinitionById(processDefinitionId, true, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "processDefinitions", allEntries = true)
    public Map<String, Object> deployProcessDefinition(String name, String bpmnXml) {
        log.info("部署流程定义：{}", name);
        ByteArrayInputStream bais = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));
        Deployment deployment = repositoryService.createDeployment()
                .addInputStream(name + ".bpmn20.xml", bais)
                .name(name)
                .deploy();

        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();

        Map<String, Object> result = new HashMap<>();
        result.put("deploymentId", deployment.getId());
        result.put("definitionId", definition.getId());
        result.put("name", definition.getName());
        result.put("key", definition.getKey());
        result.put("version", definition.getVersion());

        log.info("流程定义部署成功：{} (v{})", definition.getName(), definition.getVersion());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "processDefinitions", allEntries = true)
    public void saveProcessDefinition(String name, String bpmnXml, String category) {
        log.info("保存流程定义：{}, 分类：{}", name, category);
        ByteArrayInputStream bais = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));
        Deployment deployment = repositoryService.createDeployment()
                .addInputStream(name + ".bpmn20.xml", bais)
                .name(name)
                .category(category)
                .deploy();

        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();

        if (definition != null && category != null) {
            repositoryService.setProcessDefinitionCategory(definition.getId(), category);
        }
        log.info("流程定义保存成功：{}", name);
    }

    @Override
    @Cacheable(value = "processDefinitions", key = "#category ?: 'all'", unless = "#result == null")
    public Map<String, Object> getProcessDefinitions(String category) {
        log.debug("获取流程定义列表，分类：{}", category);
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

        if (category != null && !category.isEmpty()) {
            query.processDefinitionCategory(category);
        }

        List<ProcessDefinition> definitions = query.orderByProcessDefinitionVersion().desc().list();

        List<Map<String, Object>> records = definitions.stream().map(definition -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", definition.getId());
            map.put("name", definition.getName());
            map.put("key", definition.getKey());
            map.put("version", definition.getVersion());
            map.put("category", definition.getCategory());
            map.put("deploymentId", definition.getDeploymentId());
            map.put("resourceName", definition.getResourceName());
            map.put("description", definition.getDescription());
            map.put("suspended", definition.isSuspended());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", records.size());
        return result;
    }

    @Override
    @Cacheable(value = "processDefinitions", key = "#processDefinitionId", unless = "#result == null")
    public Map<String, Object> getProcessDefinition(String processDefinitionId) {
        log.debug("获取流程定义详情：{}", processDefinitionId);
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();

        if (definition == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", definition.getId());
        result.put("name", definition.getName());
        result.put("key", definition.getKey());
        result.put("version", definition.getVersion());
        result.put("category", definition.getCategory());
        result.put("deploymentId", definition.getDeploymentId());
        result.put("resourceName", definition.getResourceName());
        result.put("description", definition.getDescription());
        result.put("suspended", definition.isSuspended());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "processDefinitions", allEntries = true)
    public void deleteProcessDefinition(String deploymentId) {
        log.info("删除流程定义，部署 ID: {}", deploymentId);
        repositoryService.deleteDeployment(deploymentId, true);
        log.info("流程定义删除成功：{}", deploymentId);
    }

    @Override
    public String getProcessDefinitionBpmn(String processDefinitionId) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();

        if (definition == null) {
            throw new BusinessException("流程定义不存在");
        }

        String resourceName = definition.getResourceName();
        try (InputStream inputStream = repositoryService.getResourceAsStream(definition.getDeploymentId(), resourceName)) {
            if (inputStream != null) {
                byte[] bytes = inputStream.readAllBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("读取 BPMN 失败：{}", e.getMessage());
            throw new RuntimeException("读取 BPMN 失败：" + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> getRunningProcessInstances(String processDefinitionKey, String businessKey) {
        log.debug("获取运行中流程实例列表");
        var query = runtimeService.createProcessInstanceQuery();

        if (processDefinitionKey != null && !processDefinitionKey.isEmpty()) {
            query.processDefinitionKey(processDefinitionKey);
        }
        if (businessKey != null && !businessKey.isEmpty()) {
            query.processInstanceBusinessKey(businessKey);
        }

        List<ProcessInstance> instances = query.orderByProcessInstanceId().desc().list();

        return instances.stream().map(this::convertProcessInstanceToMap).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getProcessInstance(String processInstanceId) {
        log.debug("获取流程实例详情：{}", processInstanceId);
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (instance == null) {
            throw new BusinessException("流程实例不存在");
        }

        Map<String, Object> result = convertProcessInstanceToMap(instance);

        // 获取当前活动
        var activities = runtimeService.getActiveActivityIds(processInstanceId);
        result.put("activeActivities", activities);

        return result;
    }

    @Override
    public List<Map<String, Object>> getProcessInstanceHistory(String processInstanceId) {
        log.debug("获取流程历史轨迹：{}", processInstanceId);
        List<HistoricActivityInstance> history = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();

        return history.stream().map(activity -> {
            Map<String, Object> map = new HashMap<>();
            map.put("activityId", activity.getActivityId());
            map.put("activityName", activity.getActivityName());
            map.put("activityType", activity.getActivityType());
            map.put("startTime", activity.getStartTime());
            map.put("endTime", activity.getEndTime());
            map.put("duration", activity.getDurationInMillis());
            map.put("assignee", activity.getAssignee());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getProcessInstanceStats() {
        log.debug("获取流程实例统计信息");
        Map<String, Object> stats = new HashMap<>();

        // 运行中的流程实例统计
        long runningCount = runtimeService.createProcessInstanceQuery().count();
        long suspendedCount = runtimeService.createProcessInstanceQuery().suspended().count();

        stats.put("runningCount", runningCount);
        stats.put("suspendedCount", suspendedCount);

        // 按流程定义分组统计
        var definitions = repositoryService.createProcessDefinitionQuery().list();
        List<Map<String, Object>> definitionStats = new ArrayList<>();

        for (var definition : definitions) {
            long count = runtimeService.createProcessInstanceQuery()
                    .processDefinitionKey(definition.getKey())
                    .count();

            Map<String, Object> defStat = new HashMap<>();
            defStat.put("processDefinitionKey", definition.getKey());
            defStat.put("processDefinitionName", definition.getName());
            defStat.put("count", count);
            definitionStats.add(defStat);
        }

        stats.put("definitionStats", definitionStats);

        // 历史流程实例统计
        long historicCount = historyService.createHistoricProcessInstanceQuery().count();
        stats.put("historicCount", historicCount);

        return stats;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFormConfig(Map<String, Object> config) {
        log.info("保存表单配置：{}", config);
        String processDefinitionId = (String) config.get("processDefinitionId");
        String startForm = (String) config.get("startForm");

        // Get process definition to get the key
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();

        if (definition == null) {
            throw new BusinessException("流程定义不存在");
        }

        // Delete old bindings
        workflowFormBindingMapper.deleteByProcessKey(definition.getKey());

        // Save start form binding
        WorkflowFormBinding startBinding = new WorkflowFormBinding();
        startBinding.setProcessDefinitionKey(definition.getKey());
        startBinding.setProcessDefinitionName(definition.getName());
        startBinding.setTaskDefinitionKey(null);
        startBinding.setFormKey(startForm);
        startBinding.setFormType(1); // 启动表单
        workflowFormBindingMapper.insert(startBinding);

        // Save task form bindings
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) config.get("tasks");
        if (tasks != null) {
            for (Map<String, Object> task : tasks) {
                String taskKey = (String) task.get("taskDefinitionKey");
                String formKey = (String) task.get("formKey");
                if (formKey != null && !formKey.isEmpty()) {
                    WorkflowFormBinding taskBinding = new WorkflowFormBinding();
                    taskBinding.setProcessDefinitionKey(definition.getKey());
                    taskBinding.setProcessDefinitionName(definition.getName());
                    taskBinding.setTaskDefinitionKey(taskKey);
                    taskBinding.setTaskName((String) task.get("taskName"));
                    taskBinding.setFormKey(formKey);
                    taskBinding.setFormType(2); // 办理表单
                    workflowFormBindingMapper.insert(taskBinding);
                }
            }
        }

        log.info("表单配置保存成功");
    }

    @Override
    public Map<String, Object> getStartForm(String processKey) {
        log.info("获取启动表单：{}", processKey);
        WorkflowFormBinding binding = workflowFormBindingMapper.selectStartForm(processKey);
        if (binding == null) {
            return new HashMap<>();
        }

        // 从 form_definition 表获取表单 schema
        FormDefinition formDefinition = formDefinitionMapper.selectByCode(binding.getFormKey());

        Map<String, Object> result = new HashMap<>();
        result.put("formKey", binding.getFormKey());
        result.put("formName", binding.getFormName() != null ? binding.getFormName() : (formDefinition != null ? formDefinition.getFormName() : ""));
        result.put("formContent", formDefinition != null ? formDefinition.getFormSchema() : binding.getFormSchema());
        return result;
    }

    @Override
    public Map<String, Object> getTaskForm(String taskDefinitionKey) {
        log.info("获取任务表单：{}", taskDefinitionKey);
        WorkflowFormBinding binding = workflowFormBindingMapper.selectTaskForm(taskDefinitionKey);
        if (binding == null) {
            return new HashMap<>();
        }

        // 从 form_definition 表获取表单 schema
        FormDefinition formDefinition = formDefinitionMapper.selectByCode(binding.getFormKey());

        Map<String, Object> result = new HashMap<>();
        result.put("formKey", binding.getFormKey());
        result.put("formName", binding.getFormName() != null ? binding.getFormName() : (formDefinition != null ? formDefinition.getFormName() : ""));
        result.put("formContent", formDefinition != null ? formDefinition.getFormSchema() : binding.getFormSchema());
        return result;
    }

    /**
     * 转换流程实例为 Map
     */
    private Map<String, Object> convertProcessInstanceToMap(ProcessInstance instance) {
        Map<String, Object> map = new HashMap<>();
        map.put("processInstanceId", instance.getProcessInstanceId());
        map.put("processDefinitionId", instance.getProcessDefinitionId());
        map.put("processDefinitionKey", instance.getProcessDefinitionKey());
        map.put("businessKey", instance.getBusinessKey());
        map.put("isSuspended", instance.isSuspended());
        map.put("startTime", instance.getStartTime());
        map.put("startUserId", instance.getStartUserId());
        return map;
    }
}
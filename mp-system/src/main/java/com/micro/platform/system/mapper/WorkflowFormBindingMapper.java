package com.micro.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.micro.platform.system.entity.WorkflowFormBinding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工作流表单绑定 Mapper
 */
@Mapper
public interface WorkflowFormBindingMapper extends BaseMapper<WorkflowFormBinding> {

    /**
     * 查询表单绑定列表
     */
    @Select("SELECT a.*, b.form_schema, b.form_config FROM workflow_form_binding a LEFT JOIN form_definition b ON a.form_key = b.form_code WHERE a.del_flag = 0 " +
            "<if test='processDefinitionKey != null and processDefinitionKey != \"\"'>" +
            "AND process_definition_key = #{processDefinitionKey}" +
            "</if>" +
            "<if test='taskDefinitionKey != null and taskDefinitionKey != \"\"'>" +
            "AND task_definition_key = #{taskDefinitionKey}" +
            "</if>" +
            "ORDER BY form_type, task_name")
    List<WorkflowFormBinding> selectBindings(@Param("processDefinitionKey") String processDefinitionKey,
                                              @Param("taskDefinitionKey") String taskDefinitionKey);

    /**
     * 查询流程的所有表单绑定
     */
    @Select("SELECT a.*, b.form_schema, b.form_config FROM workflow_form_binding a LEFT JOIN form_definition b ON a.form_key = b.form_code WHERE a.del_flag = 0 " +
            "AND process_definition_key = #{processDefinitionKey} " +
            "ORDER BY form_type, task_name")
    List<WorkflowFormBinding> selectProcessBindings(@Param("processDefinitionKey") String processDefinitionKey);

    /**
     * 查询启动表单
     */
    @Select("SELECT a.*, b.form_schema, b.form_config FROM workflow_form_binding a LEFT JOIN form_definition b ON a.form_key = b.form_code WHERE a.del_flag = 0 " +
            "AND process_definition_key = #{processKey} " +
            "AND form_type = 1 " +
            "LIMIT 1")
    WorkflowFormBinding selectStartForm(@Param("processKey") String processKey);

    /**
     * 查询任务表单
     */
    @Select("SELECT a.*, b.form_schema, b.form_config FROM workflow_form_binding a LEFT JOIN form_definition b ON a.form_key = b.form_code WHERE a.del_flag = 0 " +
            "AND task_definition_key = #{taskDefinitionKey} " +
            "AND form_type = 2 " +
            "LIMIT 1")
    WorkflowFormBinding selectTaskForm(@Param("taskDefinitionKey") String taskDefinitionKey);

    /**
     * 删除指定任务的绑定
     */
    @Select("UPDATE workflow_form_binding SET del_flag = 1 " +
            "WHERE process_definition_key = #{processDefinitionKey} " +
            "AND task_definition_key = #{taskDefinitionKey}")
    void deleteByTaskKey(@Param("processDefinitionKey") String processDefinitionKey,
                         @Param("taskDefinitionKey") String taskDefinitionKey);

    /**
     * 删除指定流程的所有绑定
     */
    @Select("UPDATE workflow_form_binding SET del_flag = 1 " +
            "WHERE process_definition_key = #{processDefinitionKey}")
    void deleteByProcessKey(@Param("processDefinitionKey") String processDefinitionKey);
}

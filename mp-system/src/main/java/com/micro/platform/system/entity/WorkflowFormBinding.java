package com.micro.platform.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工作流表单绑定实体
 */
@TableName("workflow_form_binding")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowFormBinding implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "binding_id", type = IdType.AUTO)
    private Long bindingId;

    /**
     * 流程定义 Key
     */
    @TableField("process_definition_key")
    private String processDefinitionKey;

    /**
     * 流程定义名称
     */
    @TableField("process_definition_name")
    private String processDefinitionName;

    /**
     * 任务定义 Key（启动表单为空）
     */
    @TableField("task_definition_key")
    private String taskDefinitionKey;

    /**
     * 任务名称
     */
    @TableField("task_name")
    private String taskName;

    /**
     * 表单 Key
     */
    @TableField("form_key")
    private String formKey;

    /**
     * 表单名称
     */
    @TableField("form_name")
    private String formName;

    /**
     * 表单类型：1-启动表单，2-办理表单
     */
    @TableField("form_type")
    private Integer formType;

    /**
     * 状态：0-停用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 表单 Schema（JSON）
     */
    @TableField("form_schema")
    private String formSchema;

    /**
     * 表单 Config（JSON）
     */
    @TableField("form_config")
    private String formConfig;

    /**
     * 创建人
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
}
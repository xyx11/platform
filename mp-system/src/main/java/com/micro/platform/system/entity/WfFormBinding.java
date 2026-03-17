package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工作流表单绑定关系实体
 */
@TableName("wf_form_binding")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WfFormBinding implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 绑定 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 流程定义 Key
     */
    private String processDefinitionKey;

    /**
     * 流程定义 ID
     */
    private String processDefinitionId;

    /**
     * 任务节点 Key
     */
    private String taskDefinitionKey;

    /**
     * 表单编码
     */
    private String formCode;

    /**
     * 表单名称
     */
    private String formName;

    /**
     * 表单类型 (1:启动表单 2:办理表单)
     */
    private Integer formType;

    /**
     * 绑定关系唯一标识
     */
    private String bindingKey;

    /**
     * 状态 (0:停用 1:启用)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建者 ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新者 ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标志 (0:正常 1:删除)
     */
    @TableLogic
    private Integer deleted;
}
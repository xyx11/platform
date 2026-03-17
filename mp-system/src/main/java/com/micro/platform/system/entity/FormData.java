package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 动态表单数据实体
 *
 * 用于存储表单提交的数据
 */
@TableName("form_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 表单 ID
     */
    private Long formId;

    /**
     * 表单编码
     */
    private String formCode;

    /**
     * 表单数据 JSON
     */
    private String formData;

    /**
     * 提交者 ID
     */
    private Long submitterId;

    /**
     * 提交者名称
     */
    private String submitterName;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 状态 (0:草稿 1:已提交 2:已审核 3:已驳回)
     */
    private Integer status;

    /**
     * 提交类型 (1:启动表单 2:办理表单)
     */
    private Integer submitType;

    /**
     * 流程实例 ID（流程表单使用）
     */
    private Long processInstanceId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

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

    /**
     * 租户 ID
     */
    private Long tenantId;
}
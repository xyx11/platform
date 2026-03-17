package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;
import java.util.Date;

/**
 * 表单定义实体
 */
@TableName("form_definition")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormDefinition {

    @TableId(value = "id", type = IdType.AUTO)
    private Long formId;

    private String formName;

    private String formCode;

    private Integer formType;

    private String content;

    private String formSchema;

    private String formConfig;

    private String externalUrl;

    private String version;

    private Integer status;

    private String remark;

    private String dataSourceConfig;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    private Integer delFlag;
    
    // 别名方法，兼容不同调用方式
}

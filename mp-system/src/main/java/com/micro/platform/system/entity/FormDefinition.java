package com.micro.platform.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.util.Date;

/**
 * 表单定义实体
 */
@TableName("form_definition")
public class FormDefinition {

    @TableId(type = IdType.AUTO)
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

    public Long getFormId() { return formId; }
    public void setFormId(Long formId) { this.formId = formId; }
    
    // 别名方法，兼容不同调用方式
    public Long getId() { return formId; }
    public void setId(Long id) { this.formId = id; }
    
    public String getFormName() { return formName; }
    public void setFormName(String formName) { this.formName = formName; }
    public String getFormCode() { return formCode; }
    public void setFormCode(String formCode) { this.formCode = formCode; }
    public Integer getFormType() { return formType; }
    public void setFormType(Integer formType) { this.formType = formType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getFormSchema() { return formSchema; }
    public void setFormSchema(String formSchema) { this.formSchema = formSchema; }
    public String getFormConfig() { return formConfig; }
    public void setFormConfig(String formConfig) { this.formConfig = formConfig; }
    public String getExternalUrl() { return externalUrl; }
    public void setExternalUrl(String externalUrl) { this.externalUrl = externalUrl; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getDataSourceConfig() { return dataSourceConfig; }
    public void setDataSourceConfig(String dataSourceConfig) { this.dataSourceConfig = dataSourceConfig; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public Integer getDelFlag() { return delFlag; }
    public void setDelFlag(Integer delFlag) { this.delFlag = delFlag; }
}

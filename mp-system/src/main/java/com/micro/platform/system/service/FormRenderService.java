package com.micro.platform.system.service;

import java.util.Map;

/**
 * 表单渲染服务接口
 */
public interface FormRenderService {

    /**
     * 渲染表单
     *
     * @param formCode 表单编码
     * @return 表单渲染配置
     */
    Map<String, Object> renderForm(String formCode);

    /**
     * 渲染表单字段
     *
     * @param formCode 表单编码
     * @return 表单字段配置
     */
    Map<String, Object> renderFormFields(String formCode);

    /**
     * 验证表单数据
     *
     * @param formCode 表单编码
     * @param formData 表单数据
     * @return 验证结果
     */
    Map<String, Object> validateFormData(String formCode, Map<String, Object> formData);

    /**
     * 获取表单 schema
     *
     * @param formCode 表单编码
     * @return 表单 schema
     */
    String getFormSchema(String formCode);
}
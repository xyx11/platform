package com.micro.platform.system.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.system.entity.FormDefinition;
import com.micro.platform.system.service.FormDefinitionService;
import com.micro.platform.system.service.FormRenderService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 表单渲染服务实现
 */
@Service
public class FormRenderServiceImpl implements FormRenderService {

    private final FormDefinitionService formDefinitionService;
    private final ObjectMapper objectMapper;

    public FormRenderServiceImpl(FormDefinitionService formDefinitionService, ObjectMapper objectMapper) {
        this.formDefinitionService = formDefinitionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> renderForm(String formCode) {
        FormDefinition form = formDefinitionService.selectByCode(formCode);
        if (form == null) {
            throw new BusinessException("表单不存在");
        }

        if (form.getStatus() != 1) {
            throw new BusinessException("表单未发布或已停用");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("formId", form.getFormId());
        result.put("formName", form.getFormName());
        result.put("formCode", form.getFormCode());
        result.put("formType", form.getFormType());

        try {
            // 解析表单配置
            if (form.getFormConfig() != null) {
                Map<String, Object> config = objectMapper.readValue(
                    form.getFormConfig(),
                    new TypeReference<Map<String, Object>>() {}
                );
                result.put("config", config);
            }

            // 解析表单字段
            if (form.getFormSchema() != null) {
                Map<String, Object> schema = objectMapper.readValue(
                    form.getFormSchema(),
                    new TypeReference<Map<String, Object>>() {}
                );
                result.put("schema", schema);
            }

            // 解析数据源配置
            if (form.getDataSourceConfig() != null) {
                Map<String, Object> dataSource = objectMapper.readValue(
                    form.getDataSourceConfig(),
                    new TypeReference<Map<String, Object>>() {}
                );
                result.put("dataSource", dataSource);
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException("表单配置解析失败：" + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> renderFormFields(String formCode) {
        Map<String, Object> form = renderForm(formCode);
        Object schema = form.get("schema");

        Map<String, Object> result = new HashMap<>();
        result.put("formCode", form.get("formCode"));
        result.put("formName", form.get("formName"));

        if (schema instanceof Map) {
            Map<String, Object> schemaMap = (Map<String, Object>) schema;
            Object fields = schemaMap.get("fields");
            result.put("fields", fields);
        }

        return result;
    }

    @Override
    public Map<String, Object> validateFormData(String formCode, Map<String, Object> formData) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("errors", new ArrayList<>());

        try {
            Map<String, Object> form = renderForm(formCode);
            Object schemaObj = form.get("schema");

            if (schemaObj instanceof Map) {
                Map<String, Object> schemaMap = (Map<String, Object>) schemaObj;
                Object fieldsObj = schemaMap.get("fields");

                if (fieldsObj instanceof List) {
                    List<Map<String, Object>> fields = (List<Map<String, Object>>) fieldsObj;
                    List<Map<String, String>> errors = new ArrayList<>();

                    for (Map<String, Object> field : fields) {
                        String fieldName = (String) field.get("name");
                        String fieldLabel = (String) field.get("label");
                        String type = (String) field.get("type");
                        Boolean required = (Boolean) field.get("required");

                        // 必填验证
                        if (Boolean.TRUE.equals(required)) {
                            Object value = formData.get(fieldName);
                            if (value == null || "".equals(value.toString().trim())) {
                                Map<String, String> error = new HashMap<>();
                                error.put("field", fieldName);
                                error.put("message", fieldLabel + " 不能为空");
                                errors.add(error);
                                result.put("valid", false);
                            }
                        }

                        // 类型验证
                        Object value = formData.get(fieldName);
                        if (value != null && !"".equals(value.toString().trim())) {
                            if ("email".equals(type)) {
                                if (!isValidEmail(value.toString())) {
                                    Map<String, String> error = new HashMap<>();
                                    error.put("field", fieldName);
                                    error.put("message", fieldLabel + " 格式不正确");
                                    errors.add(error);
                                    result.put("valid", false);
                                }
                            } else if ("number".equals(type)) {
                                try {
                                    Double.parseDouble(value.toString());
                                } catch (NumberFormatException e) {
                                    Map<String, String> error = new HashMap<>();
                                    error.put("field", fieldName);
                                    error.put("message", fieldLabel + " 必须是数字");
                                    errors.add(error);
                                    result.put("valid", false);
                                }
                            }
                        }
                    }

                    result.put("errors", errors);
                }
            }
        } catch (BusinessException e) {
            result.put("valid", false);
            List<Map<String, String>> errors = new ArrayList<>();
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            errors.add(error);
            result.put("errors", errors);
        }

        return result;
    }

    @Override
    public String getFormSchema(String formCode) {
        FormDefinition form = formDefinitionService.selectByCode(formCode);
        if (form == null) {
            throw new BusinessException("表单不存在");
        }
        return form.getFormSchema();
    }

    /**
     * 验证邮箱格式
     */
    private boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(regex);
    }
}
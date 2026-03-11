package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.FormData;

import java.util.List;
import java.util.Map;

/**
 * 表单数据服务接口
 */
public interface FormDataService extends IServiceX<FormData> {

    /**
     * 分页查询表单数据
     */
    Page<FormData> selectFormDataPage(Long formId, Integer pageNum, Integer pageSize);

    /**
     * 获取表单数据详情
     */
    FormData getFormDataDetail(Long id);

    /**
     * 提交表单数据
     */
    void submitFormData(Long formId, Map<String, Object> data);

    /**
     * 保存表单数据（草稿）
     */
    void saveFormData(Long formId, Map<String, Object> data);

    /**
     * 审核表单数据
     */
    void auditFormData(Long id, Integer status, String remark);

    /**
     * 删除表单数据
     */
    void deleteFormData(List<Long> ids);
}
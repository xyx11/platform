package com.micro.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.micro.platform.system.entity.FormDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 表单定义 Mapper 接口
 */
@Mapper
public interface FormDefinitionMapper extends BaseMapper<FormDefinition> {

    /**
     * 根据表单编码查询
     */
    @Select("SELECT * FROM form_definition WHERE form_code = #{formCode} AND del_flag = 0 LIMIT 1")
    FormDefinition selectByCode(String formCode);
}
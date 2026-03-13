package com.micro.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.micro.platform.system.entity.SysTodoTag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 待办事项标签 Mapper 接口
 */
@Mapper
public interface SysTodoTagMapper extends BaseMapper<SysTodoTag> {

    /**
     * 获取用户的标签列表
     */
    List<SysTodoTag> selectByUserId(Long userId);
}
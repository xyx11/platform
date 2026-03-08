package com.micro.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.micro.platform.system.entity.SysTodo;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * 待办事项 Mapper 接口
 */
@Mapper
public interface SysTodoMapper extends BaseMapper<SysTodo> {

    /**
     * 获取待办事项统计信息
     */
    Map<String, Object> getTodoStats(Long userId);
}
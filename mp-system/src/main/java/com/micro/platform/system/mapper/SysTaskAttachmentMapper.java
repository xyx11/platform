package com.micro.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.micro.platform.system.entity.SysTaskAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务附件 Mapper 接口
 */
@Mapper
public interface SysTaskAttachmentMapper extends BaseMapper<SysTaskAttachment> {

    /**
     * 根据任务 ID 获取附件列表
     */
    List<SysTaskAttachment> selectAttachmentsByTodoId(@Param("todoId") Long todoId);

    /**
     * 获取附件总数
     */
    int selectCountByTodoId(@Param("todoId") Long todoId);
}
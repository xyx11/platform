package com.micro.platform.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.job.entity.SysJobLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 定时任务日志 Mapper 接口
 */
@Mapper
public interface SysJobLogMapper extends BaseMapper<SysJobLog> {

    /**
     * 分页查询任务日志列表
     */
    Page<SysJobLog> selectPage(@Param("page") Page<SysJobLog> page, @Param("jobLog") SysJobLog jobLog);

    /**
     * 清空任务日志
     */
    void clean();
}
package com.micro.platform.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.job.entity.SysJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 定时任务 Mapper 接口
 */
@Mapper
public interface SysJobMapper extends BaseMapper<SysJob> {

    /**
     * 分页查询任务列表
     */
    Page<SysJob> selectPage(@Param("page") Page<SysJob> page, @Param("job") SysJob job);
}
package com.micro.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.system.entity.SysLoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 登录日志 Mapper 接口
 */
@Mapper
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {

    /**
     * 分页查询登录日志列表
     */
    Page<SysLoginLog> selectPage(@Param("page") Page<SysLoginLog> page, @Param("log") SysLoginLog log);

    /**
     * 清空登录日志
     */
    void clean();
}
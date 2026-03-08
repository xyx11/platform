package com.micro.platform.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.micro.platform.auth.entity.SysLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志 Mapper 接口
 */
@Mapper
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {

}
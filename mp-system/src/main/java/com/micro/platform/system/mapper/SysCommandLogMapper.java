package com.micro.platform.system.mapper;

import com.micro.platform.common.core.mapper.BaseMapperX;
import com.micro.platform.system.entity.SysCommandLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 命令执行记录 Mapper
 */
@Mapper
public interface SysCommandLogMapper extends BaseMapperX<SysCommandLog> {
}
package com.micro.platform.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.micro.platform.auth.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

}
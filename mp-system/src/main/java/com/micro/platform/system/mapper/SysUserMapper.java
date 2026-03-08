package com.micro.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.micro.platform.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     */
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户
     */
    SysUser selectByPhone(@Param("phone") String phone);

    /**
     * 根据邮箱查询用户
     */
    SysUser selectByEmail(@Param("email") String email);

    /**
     * 查询用户的角色 ID 列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的权限标识列表
     */
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 根据角色 ID 查询用户列表
     */
    List<SysUser> selectUsersByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量删除用户角色关联
     */
    int deleteUserRoleBatch(@Param("roleId") Long roleId);

    /**
     * 批量插入用户角色关联
     */
    int batchInsertUserRole(@Param("roleId") Long roleId, @Param("userIds") List<Long> userIds);

    /**
     * 批量删除用户角色关联（指定用户）
     */
    int batchDeleteUserRole(@Param("roleId") Long roleId, @Param("userIds") List<Long> userIds);
}
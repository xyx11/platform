package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysUser;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface SysUserService extends IServiceX<SysUser> {

    /**
     * 分页查询用户列表
     */
    Page<SysUser> selectUserPage(SysUser user, Integer pageNum, Integer pageSize);

    /**
     * 保存用户（包含角色关联）
     */
    void saveUser(SysUser user);

    /**
     * 更新用户（包含角色关联）
     */
    void updateUser(SysUser user);

    /**
     * 重置密码
     */
    void resetPassword(Long userId, String password);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 导出用户数据
     */
    byte[] exportUser(SysUser user);

    /**
     * 下载导入模板
     */
    byte[] downloadTemplate();

    /**
     * 导入用户数据
     */
    Map<String, Object> importUser(MultipartFile file);

    /**
     * 上传头像
     */
    String uploadAvatar(MultipartFile file, Long userId);

    /**
     * 获取用户详情（包含部门等信息）
     */
    Map<String, Object> getUserDetail(Long userId);

    /**
     * 获取用户统计信息
     */
    Map<String, Object> getUserStats(Long userId);

    /**
     * 获取用户的角色 ID 列表
     */
    Map<String, Object> getUserRoles(Long userId);

    /**
     * 分配角色到用户
     */
    void assignRoles(Long userId, Long[] roleIds);

    /**
     * 批量新增用户
     */
    Map<String, Object> batchSaveUser(java.util.List<SysUser> users);

    /**
     * 批量重置密码
     */
    void batchResetPassword(java.util.List<Long> userIds, String password);

    /**
     * 解锁用户（解除登录禁用）
     */
    void unlockUser(Long userId);

    /**
     * 批量解锁用户
     */
    void batchUnlockUsers(java.util.List<Long> userIds);

    /**
     * 修改用户状态
     */
    void updateStatus(Long userId, Integer status);
}
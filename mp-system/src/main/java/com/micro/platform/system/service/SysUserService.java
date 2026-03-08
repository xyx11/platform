package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysUser;
import jakarta.servlet.http.HttpServletResponse;
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
    void exportUser(HttpServletResponse response, SysUser user);

    /**
     * 下载导入模板
     */
    void downloadTemplate(HttpServletResponse response);

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
}
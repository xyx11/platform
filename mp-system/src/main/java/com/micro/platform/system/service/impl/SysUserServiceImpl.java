package com.micro.platform.system.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysDept;
import com.micro.platform.system.entity.SysUser;
import com.micro.platform.system.mapper.SysDeptMapper;
import com.micro.platform.system.mapper.SysUserMapper;
import com.micro.platform.system.service.SysUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户服务实现类
 */
@Service
public class SysUserServiceImpl extends ServiceImplX<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysDeptMapper sysDeptMapper;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    public SysUserServiceImpl(SysUserMapper sysUserMapper, SysDeptMapper sysDeptMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysDeptMapper = sysDeptMapper;
    }

    @Override
    public Page<SysUser> selectUserPage(SysUser user, Integer pageNum, Integer pageSize) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(user.getUsername()), SysUser::getUsername, user.getUsername())
                .like(StringUtils.hasText(user.getPhone()), SysUser::getPhone, user.getPhone())
                .like(StringUtils.hasText(user.getEmail()), SysUser::getEmail, user.getEmail())
                .eq(user.getStatus() != null, SysUser::getStatus, user.getStatus())
                .eq(user.getDeptId() != null, SysUser::getDeptId, user.getDeptId())
                .orderByDesc(SysUser::getCreateTime);
        Page<SysUser> resultPage = baseMapper.selectPage(page, wrapper);

        // 填充角色名称
        for (SysUser u : resultPage.getRecords()) {
            List<Long> roleIds = sysUserMapper.selectRoleIdsByUserId(u.getUserId());
            u.setRoleIds(roleIds);
        }

        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUser(SysUser user) {
        // 检查用户名是否存在
        SysUser existUser = baseMapper.selectByUsername(user.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        // 加密密码
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        }
        baseMapper.insert(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUser user) {
        baseMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String password) {
        SysUser user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        baseMapper.updateById(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!new BCryptPasswordEncoder().matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        updateById(user);
    }

    @Override
    public void exportUser(HttpServletResponse response, SysUser user) {
        try {
            List<SysUser> list = selectUserList(user);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("用户数据", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), SysUser.class)
                    .sheet("用户数据")
                    .doWrite(list);
        } catch (Exception e) {
            throw new RuntimeException("导出用户数据失败：" + e.getMessage());
        }
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("用户导入模板", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            List<SysUser> template = new ArrayList<>();
            SysUser demo = new SysUser();
            demo.setUsername("demo");
            demo.setNickname("演示用户");
            demo.setPassword("admin123");
            demo.setPhone("13800138000");
            demo.setEmail("demo@example.com");
            demo.setGender(1);
            demo.setStatus(1);
            template.add(demo);

            EasyExcel.write(response.getOutputStream(), SysUser.class)
                    .sheet("模板")
                    .doWrite(template);
        } catch (Exception e) {
            throw new RuntimeException("下载模板失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> importUser(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<SysUser> users = EasyExcel.read(file.getInputStream())
                    .head(SysUser.class)
                    .sheet()
                    .doReadSync();

            int success = 0;
            int failed = 0;
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            for (SysUser user : users) {
                try {
                    // 检查用户名是否存在
                    SysUser existUser = baseMapper.selectByUsername(user.getUsername());
                    if (existUser != null) {
                        failed++;
                        continue;
                    }

                    // 加密密码
                    String password = user.getPassword();
                    if (!StringUtils.hasText(password)) {
                        password = "admin123"; // 默认密码
                    }
                    user.setPassword(encoder.encode(password));
                    user.setCreateTime(LocalDateTime.now());
                    user.setDeleted(0);

                    baseMapper.insert(user);
                    success++;
                } catch (Exception e) {
                    failed++;
                }
            }

            result.put("success", success);
            result.put("failed", failed);
        } catch (Exception e) {
            throw new BusinessException("导入用户数据失败：" + e.getMessage());
        }
        return result;
    }

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 检查文件大小（不超过 2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException("头像大小不能超过 2MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只能上传图片文件");
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String fileName = "avatar/" + userId + "/" + IdUtil.fastSimpleUUID() + extension;

        // 创建上传目录
        File destDir = new File(uploadPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        // 保存文件
        File destFile = new File(uploadPath + "/" + fileName);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new BusinessException("上传失败：" + e.getMessage());
        }

        // 更新用户头像
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        String avatarUrl = "/uploads/" + fileName;
        user.setAvatar(avatarUrl);
        updateById(user);

        return avatarUrl;
    }

    @Override
    public Map<String, Object> getUserDetail(Long userId) {
        Map<String, Object> result = new HashMap<>();
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        result.put("user", user);

        // 获取部门信息
        if (user.getDeptId() != null) {
            SysDept dept = sysDeptMapper.selectById(user.getDeptId());
            result.put("dept", dept);
        }

        // 获取角色信息
        List<Long> roleIds = sysUserMapper.selectRoleIdsByUserId(userId);
        result.put("roleIds", roleIds);

        return result;
    }

    @Override
    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        if (userId == null) {
            // 统计所有用户
            long totalUserCount = baseMapper.selectCount(null);
            long activeUserCount = baseMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1));
            long inactiveUserCount = baseMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 0));
            stats.put("totalUserCount", totalUserCount);
            stats.put("activeUserCount", activeUserCount);
            stats.put("inactiveUserCount", inactiveUserCount);

            // 按性别统计
            long maleCount = baseMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getGender, 1));
            long femaleCount = baseMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getGender, 0));
            long unknownCount = baseMapper.selectCount(new LambdaQueryWrapper<SysUser>().isNull(SysUser::getGender));
            stats.put("maleCount", maleCount);
            stats.put("femaleCount", femaleCount);
            stats.put("unknownCount", unknownCount);

            // 按部门统计用户数
            List<Map<String, Object>> deptStats = new ArrayList<>();
            // 这里简化处理，实际应该联表查询统计每个部门的用户数
            stats.put("deptStats", deptStats);
        } else {
            // 统计指定用户
            SysUser user = getById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            stats.put("userId", userId);
            stats.put("username", user.getUsername());
            stats.put("nickname", user.getNickname());
            stats.put("status", user.getStatus());
        }

        return stats;
    }

    @Override
    public Map<String, Object> getUserRoles(Long userId) {
        Map<String, Object> result = new HashMap<>();
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        List<Long> roleIds = sysUserMapper.selectRoleIdsByUserId(userId);
        result.put("userId", userId);
        result.put("roleIds", roleIds);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, Long[] roleIds) {
        // 检查用户是否存在
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 先删除原有的用户角色关联
        sysUserMapper.deleteUserRoleByUserId(userId);

        // 再插入新的关联
        if (roleIds != null && roleIds.length > 0) {
            sysUserMapper.batchInsertUserRoleByUserId(userId, Arrays.asList(roleIds));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchSaveUser(List<SysUser> users) {
        Map<String, Object> result = new HashMap<>();
        int success = 0;
        int failed = 0;
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        for (SysUser user : users) {
            try {
                // 检查用户名是否存在
                SysUser existUser = baseMapper.selectByUsername(user.getUsername());
                if (existUser != null) {
                    failed++;
                    continue;
                }

                // 加密密码
                String password = user.getPassword();
                if (!StringUtils.hasText(password)) {
                    password = "admin123"; // 默认密码
                }
                user.setPassword(encoder.encode(password));
                user.setCreateTime(LocalDateTime.now());
                user.setDeleted(0);

                baseMapper.insert(user);
                success++;
            } catch (Exception e) {
                failed++;
            }
        }

        result.put("success", success);
        result.put("failed", failed);
        return result;
    }

    @Override
    public void batchResetPassword(List<Long> userIds, String password) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(password);
        for (Long userId : userIds) {
            LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(SysUser::getUserId, userId)
                    .set(SysUser::getPassword, encodedPassword);
            baseMapper.update(null, wrapper);
        }
    }

    @Override
    public void unlockUser(Long userId) {
        // 解锁用户 - 检查用户状态
        SysUser user = getById(userId);
        if (user != null && user.getStatus() == 0) {
            // 用户被禁用，这里仅作为预留接口
            // 实际解锁需要管理员手动修改用户状态为正常
        }
    }

    @Override
    public void batchUnlockUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            unlockUser(userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long userId, Integer status) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        updateById(user);
    }

    /**
     * 查询用户列表
     */
    private List<SysUser> selectUserList(SysUser user) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(user.getUsername()), SysUser::getUsername, user.getUsername())
                .like(StringUtils.hasText(user.getPhone()), SysUser::getPhone, user.getPhone())
                .like(StringUtils.hasText(user.getEmail()), SysUser::getEmail, user.getEmail())
                .eq(user.getStatus() != null, SysUser::getStatus, user.getStatus())
                .orderByDesc(SysUser::getCreateTime);
        return baseMapper.selectList(wrapper);
    }
}

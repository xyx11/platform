package com.micro.platform.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysUser;
import com.micro.platform.system.mapper.SysUserMapper;
import com.micro.platform.system.service.SysUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.util.*;

/**
 * 用户服务实现类
 */
@Service
public class SysUserServiceImpl extends ServiceImplX<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;

    public SysUserServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public Page<SysUser> selectUserPage(SysUser user, Integer pageNum, Integer pageSize) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(user.getUsername()), SysUser::getUsername, user.getUsername())
                .like(StringUtils.hasText(user.getPhone()), SysUser::getPhone, user.getPhone())
                .like(StringUtils.hasText(user.getEmail()), SysUser::getEmail, user.getEmail())
                .eq(user.getStatus() != null, SysUser::getStatus, user.getStatus())
                .orderByDesc(SysUser::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
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
                    user.setCreateTime(new Date());
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
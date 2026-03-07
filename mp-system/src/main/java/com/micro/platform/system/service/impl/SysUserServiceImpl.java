package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysUser;
import com.micro.platform.system.mapper.SysUserMapper;
import com.micro.platform.system.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
        // TODO: 需要加密密码
        baseMapper.insert(user);
        // TODO: 需要保存用户角色关联
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUser user) {
        // TODO: 需要更新用户角色关联
        baseMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String password) {
        SysUser user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // TODO: 需要加密密码
        user.setPassword(password);
        baseMapper.updateById(user);
    }
}
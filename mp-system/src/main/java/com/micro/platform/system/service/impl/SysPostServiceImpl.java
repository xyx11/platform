package com.micro.platform.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.system.entity.SysPost;
import com.micro.platform.system.entity.SysUser;
import com.micro.platform.system.mapper.SysPostMapper;
import com.micro.platform.system.mapper.SysUserMapper;
import com.micro.platform.system.service.SysPostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * 岗位服务实现类
 */
@Service
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements SysPostService {

    private final SysUserMapper sysUserMapper;

    public SysPostServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public Page<SysPost> selectPostPage(SysPost post, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(post.getPostCode() != null && !post.getPostCode().isEmpty(), SysPost::getPostCode, post.getPostCode())
               .like(StringUtils.hasText(post.getPostName()), SysPost::getPostName, post.getPostName())
               .eq(post.getStatus() != null, SysPost::getStatus, post.getStatus())
               .orderByAsc(SysPost::getPostSort);
        Page<SysPost> page = page(new Page<>(pageNum, pageSize), wrapper);

        // 填充用户数量
        for (SysPost p : page.getRecords()) {
            p.setUserCount(getPostUserCount(p.getPostId()));
        }

        return page;
    }

    @Override
    public List<SysPost> selectNormalPosts() {
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPost::getStatus, 1)
               .orderByAsc(SysPost::getPostSort);
        return list(wrapper);
    }

    @Override
    public byte[] exportPost(SysPost post) {
        try {
            List<SysPost> list = selectNormalPosts();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            EasyExcel.write(os, SysPost.class)
                    .sheet("岗位数据")
                    .doWrite(list);
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出岗位数据失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getPostStats(Long postId) {
        Map<String, Object> stats = new HashMap<>();

        if (postId == null) {
            // 统计所有岗位
            long totalPostCount = baseMapper.selectCount(null);
            long activePostCount = baseMapper.selectCount(new LambdaQueryWrapper<SysPost>().eq(SysPost::getStatus, 1));
            stats.put("totalPostCount", totalPostCount);
            stats.put("activePostCount", activePostCount);

            // 统计总用户数（有岗位的用户）
            LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.isNotNull(SysUser::getDeptId); // 假设有部门的用户都有岗位
            stats.put("totalUserWithPost", sysUserMapper.selectCount(userWrapper));

            // 统计每个岗位的用户数量
            List<SysPost> posts = selectNormalPosts();
            List<Map<String, Object>> postStats = new ArrayList<>();
            for (SysPost p : posts) {
                Map<String, Object> postStat = new HashMap<>();
                postStat.put("postId", p.getPostId());
                postStat.put("postName", p.getPostName());
                postStat.put("postCode", p.getPostCode());
                postStat.put("userCount", getPostUserCount(p.getPostId()));
                postStats.add(postStat);
            }
            stats.put("postStats", postStats);
        } else {
            // 统计指定岗位
            SysPost post = getById(postId);
            if (post == null) {
                throw new BusinessException("岗位不存在");
            }

            stats.put("postId", postId);
            stats.put("postName", post.getPostName());
            stats.put("postCode", post.getPostCode());
            stats.put("userCount", getPostUserCount(postId));
            stats.put("status", post.getStatus());
            stats.put("remark", post.getRemark());
        }

        return stats;
    }

    @Override
    public int getPostUserCount(Long postId) {
        if (postId == null) {
            return 0;
        }
        // 这里假设用户表有 post_id 字段，实际项目中可能需要用户 - 岗位关联表
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        // 由于当前用户表没有 post_id，这里返回 0
        // 实际项目中应该有关联表 sys_user_post
        return 0;
    }

    @Override
    public List<Map<String, Object>> getPostUsers(Long postId) {
        List<Map<String, Object>> users = new ArrayList<>();
        // 实际项目中应该从用户 - 岗位关联表查询
        return users;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUsers(Long postId, List<Long> userIds) {
        // 检查岗位是否存在
        SysPost post = getById(postId);
        if (post == null) {
            throw new BusinessException("岗位不存在");
        }

        // 实际项目中应该添加到用户 - 岗位关联表
        // 这里仅提供接口框架
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUsers(Long postId, List<Long> userIds) {
        // 实际项目中应该从用户 - 岗位关联表删除
        // 这里仅提供接口框架
    }
}
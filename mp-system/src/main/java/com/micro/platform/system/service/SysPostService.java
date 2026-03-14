package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.micro.platform.system.entity.SysPost;

import java.util.List;
import java.util.Map;

/**
 * 岗位服务接口
 */
public interface SysPostService extends IService<SysPost> {

    /**
     * 查询岗位列表
     */
    Page<SysPost> selectPostPage(SysPost post, Integer pageNum, Integer pageSize);

    /**
     * 查询所有正常岗位
     */
    List<SysPost> selectNormalPosts();

    /**
     * 导出岗位数据
     */
    byte[] exportPost(SysPost post);

    /**
     * 获取岗位统计信息
     */
    Map<String, Object> getPostStats(Long postId);

    /**
     * 获取岗位用户数量
     */
    int getPostUserCount(Long postId);

    /**
     * 获取岗位用户列表
     */
    List<Map<String, Object>> getPostUsers(Long postId);

    /**
     * 分配用户到岗位
     */
    void assignUsers(Long postId, List<Long> userIds);

    /**
     * 从岗位移除用户
     */
    void removeUsers(Long postId, List<Long> userIds);
}
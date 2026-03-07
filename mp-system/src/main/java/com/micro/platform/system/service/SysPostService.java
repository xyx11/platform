package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.micro.platform.system.entity.SysPost;

import java.util.List;

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
}
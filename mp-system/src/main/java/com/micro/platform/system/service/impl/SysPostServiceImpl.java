package com.micro.platform.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.micro.platform.system.entity.SysPost;
import com.micro.platform.system.mapper.SysPostMapper;
import com.micro.platform.system.service.SysPostService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.util.List;

/**
 * 岗位服务实现类
 */
@Service
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements SysPostService {

    @Override
    public Page<SysPost> selectPostPage(SysPost post, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(post.getPostCode() != null && !post.getPostCode().isEmpty(), SysPost::getPostCode, post.getPostCode())
               .like(StringUtils.hasText(post.getPostName()), SysPost::getPostName, post.getPostName())
               .eq(post.getStatus() != null, SysPost::getStatus, post.getStatus())
               .orderByAsc(SysPost::getPostSort);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<SysPost> selectNormalPosts() {
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPost::getStatus, 1)
               .orderByAsc(SysPost::getPostSort);
        return list(wrapper);
    }

    @Override
    public void exportPost(HttpServletResponse response, SysPost post) {
        try {
            List<SysPost> list = selectNormalPosts();

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("岗位数据", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), SysPost.class)
                    .sheet("岗位数据")
                    .doWrite(list);
        } catch (Exception e) {
            throw new RuntimeException("导出岗位数据失败：" + e.getMessage());
        }
    }
}
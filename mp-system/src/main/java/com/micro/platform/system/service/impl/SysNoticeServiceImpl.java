package com.micro.platform.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysNotice;
import com.micro.platform.system.mapper.SysNoticeMapper;
import com.micro.platform.system.service.SysNoticeService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.util.List;

/**
 * 通知公告服务实现
 */
@Service
public class SysNoticeServiceImpl extends ServiceImplX<SysNoticeMapper, SysNotice> implements SysNoticeService {

    @Override
    public Page<SysNotice> selectNoticePage(SysNotice notice, Integer pageNum, Integer pageSize) {
        Page<SysNotice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(notice.getNoticeTitle()), SysNotice::getNoticeTitle, notice.getNoticeTitle())
                .eq(StringUtils.hasText(notice.getNoticeType()), SysNotice::getNoticeType, notice.getNoticeType())
                .eq(notice.getStatus() != null, SysNotice::getStatus, notice.getStatus())
                .orderByDesc(SysNotice::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            this.removeByIds(ids);
        }
    }

    @Override
    public void exportNotice(HttpServletResponse response, SysNotice notice) {
        try {
            LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(StringUtils.hasText(notice.getNoticeTitle()), SysNotice::getNoticeTitle, notice.getNoticeTitle())
                    .eq(StringUtils.hasText(notice.getNoticeType()), SysNotice::getNoticeType, notice.getNoticeType())
                    .eq(notice.getStatus() != null, SysNotice::getStatus, notice.getStatus())
                    .orderByDesc(SysNotice::getCreateTime);
            List<SysNotice> list = baseMapper.selectList(wrapper);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("通知公告", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), SysNotice.class)
                    .sheet("通知公告")
                    .doWrite(list);
        } catch (Exception e) {
            throw new RuntimeException("导出通知公告失败：" + e.getMessage());
        }
    }
}
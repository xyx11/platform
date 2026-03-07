package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysNotice;
import com.micro.platform.system.mapper.SysNoticeMapper;
import com.micro.platform.system.service.SysNoticeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
}
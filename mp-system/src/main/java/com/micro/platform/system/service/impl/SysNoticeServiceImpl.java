package com.micro.platform.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysNotice;
import com.micro.platform.system.entity.SysNoticeUser;
import com.micro.platform.system.mapper.SysNoticeMapper;
import com.micro.platform.system.mapper.SysNoticeUserMapper;
import com.micro.platform.system.mapper.SysUserMapper;
import com.micro.platform.system.service.SysNoticeService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知公告服务实现
 */
@Service
public class SysNoticeServiceImpl extends ServiceImplX<SysNoticeMapper, SysNotice> implements SysNoticeService {

    private final SysNoticeUserMapper sysNoticeUserMapper;
    private final SysUserMapper sysUserMapper;

    public SysNoticeServiceImpl(SysNoticeUserMapper sysNoticeUserMapper, SysUserMapper sysUserMapper) {
        this.sysNoticeUserMapper = sysNoticeUserMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public Page<SysNotice> selectNoticePage(SysNotice notice, Integer pageNum, Integer pageSize) {
        Page<SysNotice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(notice.getNoticeTitle()), SysNotice::getNoticeTitle, notice.getNoticeTitle())
                .eq(StringUtils.hasText(notice.getNoticeType()), SysNotice::getNoticeType, notice.getNoticeType())
                .eq(notice.getStatus() != null, SysNotice::getStatus, notice.getStatus())
                .orderByDesc(SysNotice::getCreateTime);
        Page<SysNotice> resultPage = baseMapper.selectPage(page, wrapper);

        // 填充已读未读人数
        for (SysNotice n : resultPage.getRecords()) {
            int[] status = getReadStatus(n.getNoticeId());
            n.setReadCount(status[0]);
            n.setUnreadCount(status[1]);
        }

        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            // 删除公告
            this.removeByIds(ids);
            // 删除相关的阅读记录
            for (Long id : ids) {
                LambdaQueryWrapper<SysNoticeUser> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysNoticeUser::getNoticeId, id);
                sysNoticeUserMapper.delete(wrapper);
            }
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long noticeId, Long userId) {
        // 检查公告是否存在
        SysNotice notice = getById(noticeId);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }

        // 查询是否已存在记录
        LambdaQueryWrapper<SysNoticeUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNoticeUser::getNoticeId, noticeId)
                .eq(SysNoticeUser::getUserId, userId);
        SysNoticeUser noticeUser = sysNoticeUserMapper.selectOne(wrapper);

        if (noticeUser != null) {
            // 更新为已读
            noticeUser.setReadStatus(1);
            noticeUser.setReadTime(LocalDateTime.now());
            sysNoticeUserMapper.updateById(noticeUser);
        } else {
            // 创建阅读记录
            noticeUser = new SysNoticeUser();
            noticeUser.setNoticeId(noticeId);
            noticeUser.setUserId(userId);
            noticeUser.setReadStatus(1);
            noticeUser.setReadTime(LocalDateTime.now());
            noticeUser.setCreateTime(LocalDateTime.now());
            sysNoticeUserMapper.insert(noticeUser);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchMarkAsRead(List<Long> noticeIds, Long userId) {
        for (Long noticeId : noticeIds) {
            markAsRead(noticeId, userId);
        }
    }

    @Override
    public Page<SysNotice> getUnreadNotices(Long userId, Integer pageNum, Integer pageSize) {
        Page<SysNotice> page = new Page<>(pageNum, pageSize);

        // 查询用户未读的公告 ID 列表
        LambdaQueryWrapper<SysNoticeUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysNoticeUser::getUserId, userId)
                .eq(SysNoticeUser::getReadStatus, 1)
                .select(SysNoticeUser::getNoticeId);
        List<Long> readNoticeIds = sysNoticeUserMapper.selectList(userWrapper)
                .stream()
                .map(SysNoticeUser::getNoticeId)
                .toList();

        // 查询未读公告
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getStatus, 1); // 只查询正常的公告

        // 如果用户已读了一些公告，则排除这些公告
        if (!readNoticeIds.isEmpty()) {
            wrapper.notIn(SysNotice::getNoticeId, readNoticeIds);
        }

        // 排除定时发布但未到时间的公告
        wrapper.and(w -> w.isNull(SysNotice::getTimingPublish)
                .or().eq(SysNotice::getTimingPublish, 0)
                .or().lt(SysNotice::getPublishTime, LocalDateTime.now()));

        wrapper.orderByDesc(SysNotice::getCreateTime);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<SysNotice> getReadNotices(Long userId, Integer pageNum, Integer pageSize) {
        Page<SysNotice> page = new Page<>(pageNum, pageSize);

        // 查询用户已读的公告 ID 列表
        LambdaQueryWrapper<SysNoticeUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysNoticeUser::getUserId, userId)
                .eq(SysNoticeUser::getReadStatus, 1)
                .select(SysNoticeUser::getNoticeId);
        List<Long> readNoticeIds = sysNoticeUserMapper.selectList(userWrapper)
                .stream()
                .map(SysNoticeUser::getNoticeId)
                .toList();

        if (readNoticeIds.isEmpty()) {
            page.setRecords(List.of());
            return page;
        }

        // 查询已读公告
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysNotice::getNoticeId, readNoticeIds)
                .orderByDesc(SysNotice::getCreateTime);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public int[] getReadStatus(Long noticeId) {
        int readCount = sysNoticeUserMapper.countRead(noticeId);
        int unreadCount = sysNoticeUserMapper.countUnread(noticeId);
        return new int[]{readCount, unreadCount};
    }

    @Override
    public int countUserUnread(Long userId) {
        return sysNoticeUserMapper.countUserUnread(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkTimedPublish() {
        // 查询所有定时发布但未发布的公告
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getTimingPublish, 1)
                .eq(SysNotice::getStatus, 0) // 未发布状态
                .le(SysNotice::getPublishTime, LocalDateTime.now());

        List<SysNotice> notices = baseMapper.selectList(wrapper);
        for (SysNotice notice : notices) {
            // 更新状态为已发布
            notice.setStatus(1);
            baseMapper.updateById(notice);
        }
    }

    @Override
    public Map<String, Object> getNoticeStats() {
        Map<String, Object> stats = new HashMap<>();

        // 统计公告总数
        long totalCount = baseMapper.selectCount(null);
        stats.put("totalCount", totalCount);

        // 统计今日新增公告数
        LambdaQueryWrapper<SysNotice> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(SysNotice::getCreateTime, LocalDate.now().atStartOfDay());
        long todayCount = baseMapper.selectCount(todayWrapper);
        stats.put("todayCount", todayCount);

        // 统计已发布和未发布的公告数
        LambdaQueryWrapper<SysNotice> publishedWrapper = new LambdaQueryWrapper<>();
        publishedWrapper.eq(SysNotice::getStatus, 1);
        long publishedCount = baseMapper.selectCount(publishedWrapper);
        stats.put("publishedCount", publishedCount);

        LambdaQueryWrapper<SysNotice> unPublishedWrapper = new LambdaQueryWrapper<>();
        unPublishedWrapper.eq(SysNotice::getStatus, 0);
        long unPublishedCount = baseMapper.selectCount(unPublishedWrapper);
        stats.put("unPublishedCount", unPublishedCount);

        // 统计不同类型公告数量
        LambdaQueryWrapper<SysNotice> noticeWrapper = new LambdaQueryWrapper<>();
        noticeWrapper.eq(SysNotice::getNoticeType, "1"); // 通知
        long noticeCount = baseMapper.selectCount(noticeWrapper);
        stats.put("noticeCount", noticeCount);

        LambdaQueryWrapper<SysNotice> announcementWrapper = new LambdaQueryWrapper<>();
        announcementWrapper.eq(SysNotice::getNoticeType, "2"); // 公告
        long announcementCount = baseMapper.selectCount(announcementWrapper);
        stats.put("announcementCount", announcementCount);

        // 统计最近 7 天每天的新增公告数
        Map<String, Integer> trendData = new HashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LambdaQueryWrapper<SysNotice> dateWrapper = new LambdaQueryWrapper<>();
            dateWrapper.between(SysNotice::getCreateTime,
                    date.atStartOfDay(),
                    date.atTime(java.time.LocalTime.MAX));
            long count = baseMapper.selectCount(dateWrapper);
            trendData.put(date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")), (int) count);
        }
        stats.put("trendData", trendData);

        return stats;
    }
}
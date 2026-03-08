package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysNotice;
import com.micro.platform.system.entity.SysNoticeUser;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

/**
 * 通知公告服务接口
 */
public interface SysNoticeService extends IServiceX<SysNotice> {

    /**
     * 分页查询公告列表
     */
    Page<SysNotice> selectNoticePage(SysNotice notice, Integer pageNum, Integer pageSize);

    /**
     * 批量删除公告
     */
    void batchDelete(List<Long> ids);

    /**
     * 导出公告数据
     */
    void exportNotice(HttpServletResponse response, SysNotice notice);

    /**
     * 标记公告为已读
     */
    void markAsRead(Long noticeId, Long userId);

    /**
     * 批量标记公告为已读
     */
    void batchMarkAsRead(List<Long> noticeIds, Long userId);

    /**
     * 获取用户的未读公告列表
     */
    Page<SysNotice> getUnreadNotices(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户的已读公告列表
     */
    Page<SysNotice> getReadNotices(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取公告的已读人数和未读人数
     */
    int[] getReadStatus(Long noticeId);

    /**
     * 获取用户的未读公告数量
     */
    int countUserUnread(Long userId);

    /**
     * 定时发布检查
     */
    void checkTimedPublish();

    /**
     * 获取通知公告统计信息
     */
    Map<String, Object> getNoticeStats();
}
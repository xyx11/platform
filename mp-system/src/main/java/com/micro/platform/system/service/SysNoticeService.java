package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysNotice;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

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
}
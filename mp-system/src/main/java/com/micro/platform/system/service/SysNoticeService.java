package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysNotice;

/**
 * 通知公告服务接口
 */
public interface SysNoticeService extends IServiceX<SysNotice> {

    /**
     * 分页查询公告列表
     */
    Page<SysNotice> selectNoticePage(SysNotice notice, Integer pageNum, Integer pageSize);
}
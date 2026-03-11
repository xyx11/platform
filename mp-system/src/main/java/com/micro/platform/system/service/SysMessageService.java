package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysMessage;

import java.util.List;
import java.util.Map;

/**
 * 消息服务接口
 */
public interface SysMessageService extends IServiceX<SysMessage> {

    /**
     * 分页查询消息列表
     */
    Page<SysMessage> selectMessagePage(SysMessage message, Integer pageNum, Integer pageSize);

    /**
     * 获取用户的消息列表
     */
    Page<SysMessage> selectUserMessages(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户的未读消息数量
     */
    int countUnreadMessages(Long userId);

    /**
     * 发送消息
     */
    void sendMessage(SysMessage message);

    /**
     * 批量发送消息
     */
    void batchSendMessage(SysMessage message, List<Long> receiverIds);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long messageId, Long userId);

    /**
     * 批量标记消息为已读
     */
    void batchMarkAsRead(List<Long> messageIds, Long userId);

    /**
     * 删除消息
     */
    void deleteMessage(Long messageId, Long userId);

    /**
     * 撤回消息
     */
    void withdrawMessage(Long messageId);

    /**
     * 获取消息统计信息
     */
    Map<String, Object> getMessageStats();
}
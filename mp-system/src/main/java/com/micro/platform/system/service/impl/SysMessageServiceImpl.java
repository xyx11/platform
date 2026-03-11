package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.common.security.entity.LoginUser;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysMessage;
import com.micro.platform.system.entity.SysMessageReceiver;
import com.micro.platform.system.mapper.SysMessageMapper;
import com.micro.platform.system.mapper.SysMessageReceiverMapper;
import com.micro.platform.system.service.SysMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 消息服务实现
 */
@Service
public class SysMessageServiceImpl extends ServiceImplX<SysMessageMapper, SysMessage> implements SysMessageService {

    private final SysMessageReceiverMapper messageReceiverMapper;

    public SysMessageServiceImpl(SysMessageMapper sysMessageMapper,
                                 SysMessageReceiverMapper messageReceiverMapper) {
        this.messageReceiverMapper = messageReceiverMapper;
    }

    @Override
    public Page<SysMessage> selectMessagePage(SysMessage message, Integer pageNum, Integer pageSize) {
        Page<SysMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(message.getMessageType() != null, SysMessage::getMessageType, message.getMessageType())
                .eq(message.getLevel() != null, SysMessage::getLevel, message.getLevel())
                .eq(message.getSenderId() != null, SysMessage::getSenderId, message.getSenderId())
                .eq(message.getSendStatus() != null, SysMessage::getSendStatus, message.getSendStatus())
                .like(message.getTitle() != null && !message.getTitle().isEmpty(), SysMessage::getTitle, message.getTitle())
                .orderByDesc(SysMessage::getCreateTime);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<SysMessage> selectUserMessages(Long userId, Integer pageNum, Integer pageSize) {
        Page<SysMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();

        // 查询发给当前用户的消息（包括群发消息）
        wrapper.and(w -> w.eq(SysMessage::getReceiverId, userId)
                .or().isNull(SysMessage::getReceiverId)
                .or().eq(SysMessage::getReceiverId, 0L));

        wrapper.orderByDesc(SysMessage::getCreateTime);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public int countUnreadMessages(Long userId) {
        return baseMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(SysMessage message) {
        LoginUser loginUser = SecurityUtil.getLoginUser();
        if (loginUser != null) {
            message.setSenderId(loginUser.getUserId());
            message.setSenderName(loginUser.getUsername());
        }

        message.setSendStatus(1); // 已发送
        message.setSendTime(LocalDateTime.now());
        message.setIsRead(0); // 未读

        // 保存消息
        this.save(message);

        // 如果是群发消息，需要创建接收者记录
        if (message.getReceiverId() == null || message.getReceiverId() == 0) {
            // 群发消息，创建接收者记录
            // 这里简化处理，实际项目中应该根据部门、角色等批量创建
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSendMessage(SysMessage message, List<Long> receiverIds) {
        LoginUser loginUser = SecurityUtil.getLoginUser();
        if (loginUser != null) {
            message.setSenderId(loginUser.getUserId());
            message.setSenderName(loginUser.getUsername());
        }

        message.setSendStatus(1);
        message.setSendTime(LocalDateTime.now());

        // 保存消息
        this.save(message);

        // 批量创建接收者记录
        if (receiverIds != null && !receiverIds.isEmpty()) {
            for (Long receiverId : receiverIds) {
                SysMessageReceiver receiver = new SysMessageReceiver();
                receiver.setMessageId(message.getId());
                receiver.setReceiverId(receiverId);
                receiver.setIsRead(0);
                messageReceiverMapper.insert(receiver);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long messageId, Long userId) {
        // 更新消息的已读状态
        SysMessage message = this.getById(messageId);
        if (message != null && message.getIsRead() == 0) {
            message.setIsRead(1);
            message.setReadTime(LocalDateTime.now());
            this.updateById(message);
        }

        // 更新接收者的已读状态
        LambdaQueryWrapper<SysMessageReceiver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessageReceiver::getMessageId, messageId)
                .eq(SysMessageReceiver::getReceiverId, userId);
        SysMessageReceiver receiver = messageReceiverMapper.selectOne(wrapper);
        if (receiver != null && receiver.getIsRead() == 0) {
            receiver.setIsRead(1);
            receiver.setReadTime(LocalDateTime.now());
            messageReceiverMapper.updateById(receiver);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchMarkAsRead(List<Long> messageIds, Long userId) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        // 更新消息的已读状态
        for (Long messageId : messageIds) {
            markAsRead(messageId, userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long messageId, Long userId) {
        // 逻辑删除消息
        this.removeById(messageId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawMessage(Long messageId) {
        SysMessage message = this.getById(messageId);
        if (message != null) {
            message.setSendStatus(2); // 已撤回
            this.updateById(message);
        }
    }

    @Override
    public Map<String, Object> getMessageStats() {
        Map<String, Object> stats = new HashMap<>();

        LoginUser loginUser = SecurityUtil.getLoginUser();
        if (loginUser == null) {
            return stats;
        }

        // 总消息数
        long totalCount = baseMapper.selectCount(null);
        stats.put("totalCount", totalCount);

        // 未读消息数
        int unreadCount = countUnreadMessages(loginUser.getUserId());
        stats.put("unreadCount", unreadCount);

        // 已读消息数
        stats.put("readCount", totalCount - unreadCount);

        // 按类型统计
        Map<Integer, Long> typeStats = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            int type = i;
            long count = baseMapper.selectCount(new LambdaQueryWrapper<SysMessage>().eq(SysMessage::getMessageType, type));
            typeStats.put(type, count);
        }
        stats.put("typeStats", typeStats);

        return stats;
    }
}
package com.micro.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.micro.platform.system.entity.SysMessageReceiver;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 消息接收者 Mapper 接口
 */
@Mapper
public interface SysMessageReceiverMapper extends BaseMapper<SysMessageReceiver> {

    /**
     * 批量插入消息接收者
     */
    int batchInsert(@Param("messageId") Long messageId, @Param("receiverIds") Long[] receiverIds);
}
package com.micro.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.micro.platform.system.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息 Mapper 接口
 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {

    /**
     * 获取用户的未读消息数量
     */
    int countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 获取用户的消息列表
     */
    List<SysMessage> selectMessageByUserId(@Param("userId") Long userId);
}
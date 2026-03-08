package com.micro.platform.system.mapper;

import com.micro.platform.common.core.mapper.BaseMapperX;
import com.micro.platform.system.entity.SysNoticeUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 通知公告阅读记录 Mapper
 */
@Mapper
public interface SysNoticeUserMapper extends BaseMapperX<SysNoticeUser> {

    /**
     * 统计公告已读人数
     */
    @Select("SELECT COUNT(*) FROM sys_notice_user WHERE notice_id = #{noticeId} AND read_status = 1")
    Integer countRead(@Param("noticeId") Long noticeId);

    /**
     * 统计公告未读人数
     */
    @Select("SELECT COUNT(*) FROM sys_notice_user WHERE notice_id = #{noticeId} AND read_status = 0")
    Integer countUnread(@Param("noticeId") Long noticeId);

    /**
     * 统计用户未读公告数
     */
    @Select("SELECT COUNT(*) FROM sys_notice_user WHERE user_id = #{userId} AND read_status = 0")
    Integer countUserUnread(@Param("userId") Long userId);
}
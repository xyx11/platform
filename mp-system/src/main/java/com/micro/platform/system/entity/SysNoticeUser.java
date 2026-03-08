package com.micro.platform.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知公告阅读记录实体
 */
@Tag(name = "公告阅读记录", description = "公告阅读记录实体")
@TableName("sys_notice_user")
public class SysNoticeUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Parameter(description = "主键 ID")
    private Long id;

    /**
     * 公告 ID
     */
    @Parameter(description = "公告 ID")
    private Long noticeId;

    /**
     * 用户 ID
     */
    @Parameter(description = "用户 ID")
    private Long userId;

    /**
     * 阅读状态 (0:未读 1:已读)
     */
    @Parameter(description = "阅读状态")
    private Integer readStatus;

    /**
     * 阅读时间
     */
    @Parameter(description = "阅读时间")
    private LocalDateTime readTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Parameter(description = "创建时间")
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getNoticeId() { return noticeId; }
    public void setNoticeId(Long noticeId) { this.noticeId = noticeId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getReadStatus() { return readStatus; }
    public void setReadStatus(Integer readStatus) { this.readStatus = readStatus; }

    public LocalDateTime getReadTime() { return readTime; }
    public void setReadTime(LocalDateTime readTime) { this.readTime = readTime; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
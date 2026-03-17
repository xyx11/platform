package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内消息实体
 */
@TableName("sys_message")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 消息类型
     * 1-系统消息 2-通知消息 3-待办消息 4-预警消息
     */
    private Integer messageType;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息级别
     * 1-普通 2-重要 3-紧急
     */
    private Integer level;

    /**
     * 发送者 ID
     */
    private Long senderId;

    /**
     * 发送者名称
     */
    private String senderName;

    /**
     * 接收者 ID (0 表示所有人)
     */
    private Long receiverId;

    /**
     * 接收者部门 ID
     */
    private Long receiverDeptId;

    /**
     * 接收者角色 ID
     */
    private Long receiverRoleId;

    /**
     * 是否已读 (0:未读 1:已读)
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 定时发送时间
     */
    private LocalDateTime scheduleTime;

    /**
     * 发送状态 (0:草稿 1:已发送 2:已撤回)
     */
    private Integer sendStatus;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 创建者 ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新者 ID
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标志 (0:正常 1:删除)
     */
    @TableLogic
    private Integer deleted;

    /**
     * 租户 ID
     */
    private Long tenantId;
}
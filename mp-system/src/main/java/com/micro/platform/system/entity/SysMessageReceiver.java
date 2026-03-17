package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息接收者实体
 *
 * 用于记录消息的发送和接收状态，支持已读未读统计
 */
@TableName("sys_message_receiver")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysMessageReceiver implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 消息 ID
     */
    private Long messageId;

    /**
     * 接收者 ID
     */
    private Long receiverId;

    /**
     * 接收者名称
     */
    private String receiverName;

    /**
     * 接收者部门 ID
     */
    private Long deptId;

    /**
     * 读取状态 (0:未读 1:已读)
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
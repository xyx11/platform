package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * WebSocket 消息实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息 ID
     */
    private String id;

    /**
     * 消息类型
     * 1-系统通知 2-待办提醒 3-消息通知 4-预警提醒
     */
    private Integer type;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 接收者 ID
     */
    private Long receiverId;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 额外数据
     */
    private Object data;
}
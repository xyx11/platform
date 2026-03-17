package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务评论实体类
 */
@TableName("sys_task_comment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysTaskComment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论 ID
     */
    @TableId(value = "comment_id", type = IdType.ASSIGN_ID)
    private Long commentId;

    /**
     * 任务 ID（待办 ID）
     */
    private Long todoId;

    /**
     * 父评论 ID
     */
    private Long parentId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 评论内容
     */
    private String commentContent;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 删除标志
     */
    @TableLogic
    private Integer deleted;
}

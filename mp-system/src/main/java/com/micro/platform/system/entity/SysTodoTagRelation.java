package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 待办事项与标签关联表
 */
@TableName("sys_todo_tag_relation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysTodoTagRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联 ID
     */
    @TableId(value = "relation_id", type = IdType.ASSIGN_ID)
    private Long relationId;

    /**
     * 待办 ID
     */
    private Long todoId;

    /**
     * 标签 ID
     */
    private Long tagId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
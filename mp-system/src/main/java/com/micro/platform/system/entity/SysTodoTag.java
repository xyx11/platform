package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 待办事项标签实体类
 */
@TableName("sys_todo_tag")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysTodoTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签 ID
     */
    @TableId(value = "tag_id", type = IdType.ASSIGN_ID)
    private Long tagId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签颜色
     */
    private String tagColor;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建者 ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新者 ID
     */
    private Long updateBy;

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
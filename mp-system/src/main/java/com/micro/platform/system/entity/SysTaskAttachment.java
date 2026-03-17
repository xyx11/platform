package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务附件实体类
 */
@TableName("sys_task_attachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysTaskAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 附件 ID
     */
    @TableId(value = "attachment_id", type = IdType.ASSIGN_ID)
    private Long attachmentId;

    /**
     * 任务 ID（待办 ID）
     */
    private Long todoId;

    /**
     * 附件名称
     */
    private String attachmentName;

    /**
     * 附件路径
     */
    private String attachmentPath;

    /**
     * 附件类型
     */
    private String attachmentType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 上传用户 ID
     */
    private Long uploadUserId;

    /**
     * 上传用户名称
     */
    private String uploadUserName;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 删除标志
     */
    @TableLogic
    private Integer deleted;
}
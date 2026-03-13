package com.micro.platform.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务附件实体类
 */
@TableName("sys_task_attachment")
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

    public Long getAttachmentId() { return attachmentId; }
    public void setAttachmentId(Long attachmentId) { this.attachmentId = attachmentId; }
    public Long getTodoId() { return todoId; }
    public void setTodoId(Long todoId) { this.todoId = todoId; }
    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }
    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }
    public String getAttachmentType() { return attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public Long getUploadUserId() { return uploadUserId; }
    public void setUploadUserId(Long uploadUserId) { this.uploadUserId = uploadUserId; }
    public String getUploadUserName() { return uploadUserName; }
    public void setUploadUserName(String uploadUserName) { this.uploadUserName = uploadUserName; }
    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
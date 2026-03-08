package com.micro.platform.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知公告实体
 */
@Tag(name = "通知公告", description = "通知公告实体")
@TableName("sys_notice")
public class SysNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公告 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Parameter(description = "公告 ID")
    private Long noticeId;

    /**
     * 公告标题
     */
    @Parameter(description = "公告标题")
    private String noticeTitle;

    /**
     * 公告类型 (1:通知 2:公告)
     */
    @Parameter(description = "公告类型")
    private String noticeType;

    /**
     * 公告内容
     */
    @Parameter(description = "公告内容")
    private String noticeContent;

    /**
     * 状态 (0:关闭 1:正常)
     */
    @Parameter(description = "状态")
    private Integer status;

    /**
     * 发布时间
     */
    @Parameter(description = "发布时间")
    private LocalDateTime publishTime;

    /**
     * 是否定时发布 (0:否 1:是)
     */
    @Parameter(description = "是否定时发布")
    private Integer timingPublish;

    /**
     * 创建者 ID
     */
    @Parameter(description = "创建者 ID")
    private Long createBy;

    /**
     * 创建者名称
     */
    @Parameter(description = "创建者名称")
    private String createByName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Parameter(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新者 ID
     */
    @Parameter(description = "更新者 ID")
    private Long updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Parameter(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    @Parameter(description = "备注")
    private String remark;

    /**
     * 删除标志
     */
    @TableLogic
    @Parameter(description = "删除标志")
    private Integer deleted;

    // 扩展字段（非数据库字段）
    /**
     * 未读人数
     */
    @TableField(exist = false)
    @Parameter(description = "未读人数")
    private Integer unreadCount;

    /**
     * 已读人数
     */
    @TableField(exist = false)
    @Parameter(description = "已读人数")
    private Integer readCount;

    public Long getNoticeId() { return noticeId; }
    public void setNoticeId(Long noticeId) { this.noticeId = noticeId; }

    public String getNoticeTitle() { return noticeTitle; }
    public void setNoticeTitle(String noticeTitle) { this.noticeTitle = noticeTitle; }

    public String getNoticeType() { return noticeType; }
    public void setNoticeType(String noticeType) { this.noticeType = noticeType; }

    public String getNoticeContent() { return noticeContent; }
    public void setNoticeContent(String noticeContent) { this.noticeContent = noticeContent; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }

    public Integer getTimingPublish() { return timingPublish; }
    public void setTimingPublish(Integer timingPublish) { this.timingPublish = timingPublish; }

    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }

    public String getCreateByName() { return createByName; }
    public void setCreateByName(String createByName) { this.createByName = createByName; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

    public Integer getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Integer unreadCount) { this.unreadCount = unreadCount; }

    public Integer getReadCount() { return readCount; }
    public void setReadCount(Integer readCount) { this.readCount = readCount; }
}
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
 * 通知公告实体
 */
@Tag(name = "通知公告", description = "通知公告实体")
@TableName("sys_notice")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
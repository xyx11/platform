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
 * 命令执行记录实体
 */
@Tag(name = "命令执行记录", description = "命令执行记录实体")
@TableName("sys_command_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysCommandLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 命令 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Parameter(description = "命令 ID")
    private Long commandId;

    /**
     * 命令类型
     */
    @Parameter(description = "命令类型")
    private String commandType;

    /**
     * 命令内容
     */
    @Parameter(description = "命令内容")
    private String commandContent;

    /**
     * 命令结果
     */
    @Parameter(description = "命令结果")
    private String commandResult;

    /**
     * 执行时长（毫秒）
     */
    @Parameter(description = "执行时长")
    private Long executeTime;

    /**
     * 执行者 ID
     */
    @Parameter(description = "执行者 ID")
    private Long executeBy;

    /**
     * 执行者名称
     */
    @Parameter(description = "执行者名称")
    private String executeName;

    /**
     * 执行时间
     */
    @Parameter(description = "执行时间")
    private LocalDateTime executeTimeStr;

    /**
     * 状态：0-失败 1-成功
     */
    @Parameter(description = "状态")
    private Integer status;

    /**
     * 错误消息
     */
    @Parameter(description = "错误消息")
    private String errorMsg;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Parameter(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 删除标志
     */
    @TableLogic
    @Parameter(description = "删除标志")
    private Integer deleted;
}
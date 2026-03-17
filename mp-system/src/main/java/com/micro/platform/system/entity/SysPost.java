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
 * 系统岗位实体
 */
@Tag(name = "岗位管理", description = "岗位实体")
@TableName("sys_post")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysPost implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 岗位 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Parameter(description = "岗位 ID")
    private Long postId;

    /**
     * 岗位编码
     */
    @Parameter(description = "岗位编码")
    private String postCode;

    /**
     * 岗位名称
     */
    @Parameter(description = "岗位名称")
    private String postName;

    /**
     * 岗位排序
     */
    @Parameter(description = "岗位排序")
    private Integer postSort;

    /**
     * 状态 (0:禁用 1:正常)
     */
    @Parameter(description = "状态")
    private Integer status;

    /**
     * 备注
     */
    @Parameter(description = "备注")
    private String remark;

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
     * 删除标志 (0:正常 1:删除)
     */
    @TableLogic
    @Parameter(description = "删除标志")
    private Integer deleted;

    // 扩展字段（非数据库字段）
    /**
     * 岗位用户数量
     */
    @TableField(exist = false)
    @Parameter(description = "岗位用户数量")
    private Integer userCount;
}
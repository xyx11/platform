package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统角色实体
 */
@TableName("sys_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 数据权限范围 (1:全部数据 2:本部门及以下 3:本部门 4:仅本人)
     */
    private Integer dataScope;

    /**
     * 状态 (0:禁用 1:正常)
     */
    private Integer status;

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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新者 ID
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标志 (0:正常 1:删除)
     */
    @TableLogic
    private Integer deleted;

    /**
     * 用户数量（非数据库字段）
     */
    @TableField(exist = false)
    private Integer userCount;
}
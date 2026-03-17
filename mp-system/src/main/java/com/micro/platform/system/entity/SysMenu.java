package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统菜单实体
 */
@TableName("sys_menu")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 菜单 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long menuId;

    /**
     * 父菜单 ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 菜单类型 (1:目录 2:菜单 3:按钮)
     */
    private Integer type;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否外链 (0:是 1:否)
     */
    private Integer isFrame;

    /**
     * 是否缓存 (0:缓存 1:不缓存)
     */
    private Integer isCache;

    /**
     * 是否显示 (0:隐藏 1:显示)
     */
    private Integer visible;

    /**
     * 状态 (0:禁用 1:正常)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

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
     * 子菜单
     */
    @TableField(exist = false)
    private List<SysMenu> children;
    public List<SysMenu> getChildren() { return children; }
}
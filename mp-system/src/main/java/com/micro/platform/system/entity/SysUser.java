package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户实体
 */
@TableName("sys_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long userId;

    /**
     * 部门 ID
     */
    private Long deptId;

    /**
     * 部门名称（非数据库字段）
     */
    @TableField(exist = false)
    private String deptName;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别 (0:女 1:男)
     */
    private Integer gender;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 状态 (0:禁用 1:正常)
     */
    private Integer status;

    /**
     * 最后登录 IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    private LocalDateTime loginDate;

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
     * 角色 ID 列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Long> roleIds;

    public List<Long> getRoleIds() {
        return roleIds;
    }
}
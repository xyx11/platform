package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典类型实体
 */
@TableName("sys_dict_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysDictType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字典主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long dictId;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典类型
     */
    private String dictType;

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

}

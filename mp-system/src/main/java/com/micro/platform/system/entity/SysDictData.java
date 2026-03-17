package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典数据实体
 */
@TableName("sys_dict_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysDictData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字典主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long dictCode;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 字典标签
     */
    private String dictLabel;

    /**
     * 字典键值
     */
    private String dictValue;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态 (0:禁用 1:正常)
     */
    private Integer status;

    /**
     * 样式属性
     */
    private String cssClass;

    /**
     * 表格回显样式
     */
    private String listClass;

    /**
     * 是否默认 (0:是 1:否)
     */
    private Integer isDefault;

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

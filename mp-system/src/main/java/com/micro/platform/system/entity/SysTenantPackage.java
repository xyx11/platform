package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租户套餐实体
 *
 * 用于 SaaS 商业化场景，定义不同套餐的权益和价格
 */
@TableName("sys_tenant_package")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysTenantPackage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 套餐 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 套餐名称
     */
    private String name;

    /**
     * 套餐编码
     */
    private String code;

    /**
     * 套餐描述
     */
    private String description;

    /**
     * 套餐类型
     * 1-免费版 2-基础版 3-专业版 4-企业版 5-定制版
     */
    private Integer packageType;

    /**
     * 价格（元/月）
     */
    private BigDecimal price;

    /**
     * 最大用户数
     */
    private Integer maxUsers;

    /**
     * 最大部门数
     */
    private Integer maxDepts;

    /**
     * 最大数据存储空间（MB）
     */
    private Integer maxStorage;

    /**
     * 可用功能模块（JSON 格式）
     */
    private String features;

    /**
     * 状态 (0:停用 1:启用)
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

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
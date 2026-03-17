package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统文件实体
 */
@TableName("sys_file")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long fileId;

    private String fileName;

    private String originalName;

    private String fileExt;

    private Long fileSize;

    private String fileType;

    private String fileUrl;

    private Long createBy;

    private String createByName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}

package com.micro.platform.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 待办事项实体类
 */
@TableName("sys_todo")
public class SysTodo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 待办 ID
     */
    @TableId(value = "todo_id", type = IdType.ASSIGN_ID)
    private Long todoId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 待办标题
     */
    private String todoTitle;

    /**
     * 待办内容
     */
    private String todoContent;

    /**
     * 待办类型 (1=工作 2=会议 3=提醒 4=其他)
     */
    private String todoType;

    /**
     * 优先级 (1=紧急重要 2=重要 3=一般 4=次要)
     */
    private Integer priority;

    /**
     * 状态 (0=待处理 1=已完成 2=已取消)
     */
    private Integer status;

    /**
     * 计划完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planTime;

    /**
     * 实际完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualTime;

    /**
     * 创建者 ID
     */
    private Long createBy;

    /**
     * 创建者名称
     */
    private String createByName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新者 ID
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 删除标志
     */
    @TableLogic
    private Integer deleted;

    public Long getTodoId() { return todoId; }
    public void setTodoId(Long todoId) { this.todoId = todoId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTodoTitle() { return todoTitle; }
    public void setTodoTitle(String todoTitle) { this.todoTitle = todoTitle; }
    public String getTodoContent() { return todoContent; }
    public void setTodoContent(String todoContent) { this.todoContent = todoContent; }
    public String getTodoType() { return todoType; }
    public void setTodoType(String todoType) { this.todoType = todoType; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getPlanTime() { return planTime; }
    public void setPlanTime(LocalDateTime planTime) { this.planTime = planTime; }
    public LocalDateTime getActualTime() { return actualTime; }
    public void setActualTime(LocalDateTime actualTime) { this.actualTime = actualTime; }
    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }
    public String getCreateByName() { return createByName; }
    public void setCreateByName(String createByName) { this.createByName = createByName; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
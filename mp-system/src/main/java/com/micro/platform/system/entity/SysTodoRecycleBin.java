package com.micro.platform.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务回收站实体类
 */
@TableName("sys_todo_recycle_bin")
public class SysTodoRecycleBin implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 回收站 ID
     */
    @TableId(value = "recycle_id", type = IdType.ASSIGN_ID)
    private Long recycleId;

    /**
     * 待办 ID
     */
    private Long todoId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 待办标题（冗余存储，方便查看）
     */
    private String todoTitle;

    /**
     * 待办内容（冗余存储）
     */
    private String todoContent;

    /**
     * 删除人 ID
     */
    private Long deleteBy;

    /**
     * 删除人名称
     */
    private String deleteByName;

    /**
     * 删除时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deleteTime;

    /**
     * 恢复时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recoverTime;

    /**
     * 是否已恢复 (0=未恢复 1=已恢复)
     */
    private Integer isRecover;

    public Long getRecycleId() { return recycleId; }
    public void setRecycleId(Long recycleId) { this.recycleId = recycleId; }
    public Long getTodoId() { return todoId; }
    public void setTodoId(Long todoId) { this.todoId = todoId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTodoTitle() { return todoTitle; }
    public void setTodoTitle(String todoTitle) { this.todoTitle = todoTitle; }
    public String getTodoContent() { return todoContent; }
    public void setTodoContent(String todoContent) { this.todoContent = todoContent; }
    public Long getDeleteBy() { return deleteBy; }
    public void setDeleteBy(Long deleteBy) { this.deleteBy = deleteBy; }
    public String getDeleteByName() { return deleteByName; }
    public void setDeleteByName(String deleteByName) { this.deleteByName = deleteByName; }
    public LocalDateTime getDeleteTime() { return deleteTime; }
    public void setDeleteTime(LocalDateTime deleteTime) { this.deleteTime = deleteTime; }
    public LocalDateTime getRecoverTime() { return recoverTime; }
    public void setRecoverTime(LocalDateTime recoverTime) { this.recoverTime = recoverTime; }
    public Integer getIsRecover() { return isRecover; }
    public void setIsRecover(Integer isRecover) { this.isRecover = isRecover; }
}
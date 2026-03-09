package com.micro.platform.system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 部门 DTO
 */
public class DeptDTO {

    /**
     * 部门 ID
     */
    private Long deptId;

    /**
     * 父部门 ID
     */
    private Long parentId;

    /**
     * 部门名称
     */
    @Size(min = 2, max = 50, message = "部门名称长度在 2 到 50 个字符")
    private String deptName;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    @Pattern(regexp = "^$|1[3-9]\\d{9}$|^0\\d{2,3}-?\\d{7,8}$", message = "请输入正确的手机号码或座机号码")
    private String phone;

    /**
     * 邮箱
     */
    @Pattern(regexp = "^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "请输入正确的邮箱地址")
    private String email;

    /**
     * 状态 (0:禁用 1:正常)
     */
    private Integer status;

    // Getters and Setters
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public String getLeader() { return leader; }
    public void setLeader(String leader) { this.leader = leader; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
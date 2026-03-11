package com.micro.platform.common.core.enums;

/**
 * 数据范围枚举
 */
public enum DataScopeType {

    /**
     * 全部数据权限
     */
    ALL(1, "全部数据"),

    /**
     * 本部门及以下数据权限
     */
    DEPT_AND_CHILD(2, "本部门及以下"),

    /**
     * 仅本部门数据权限
     */
    DEPT_ONLY(3, "仅本部门"),

    /**
     * 仅本人数据权限
     */
    SELF(4, "仅本人"),

    /**
     * 按角色自定义数据权限
     */
    CUSTOM(5, "自定义");

    private final int code;
    private final String desc;

    DataScopeType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static DataScopeType getByCode(int code) {
        for (DataScopeType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return SELF;
    }
}
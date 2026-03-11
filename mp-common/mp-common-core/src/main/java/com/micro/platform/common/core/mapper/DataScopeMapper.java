package com.micro.platform.common.core.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据权限 Mapper 基接口
 */
public interface DataScopeMapper {

    /**
     * 分页查询（带数据权限）
     *
     * @param wrapper 查询条件
     * @return 数据列表
     */
    <T> List<T> selectPageDataScope(@Param(Constants.WRAPPER) Wrapper<T> wrapper);

    /**
     * 查询列表（带数据权限）
     *
     * @param wrapper 查询条件
     * @return 数据列表
     */
    <T> List<T> selectListDataScope(@Param(Constants.WRAPPER) Wrapper<T> wrapper);
}
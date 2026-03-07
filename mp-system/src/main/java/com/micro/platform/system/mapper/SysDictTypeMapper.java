package com.micro.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.system.entity.SysDictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典类型 Mapper 接口
 */
@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {

    /**
     * 分页查询字典类型列表
     */
    Page<SysDictType> selectPage(@Param("page") Page<SysDictType> page, @Param("dict") SysDictType dict);

    /**
     * 查询所有正常的字典类型
     */
    List<SysDictType> selectAllNormal();
}
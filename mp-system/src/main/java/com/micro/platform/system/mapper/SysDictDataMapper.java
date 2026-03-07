package com.micro.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.system.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典数据 Mapper 接口
 */
@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    /**
     * 分页查询字典数据列表
     */
    Page<SysDictData> selectPage(@Param("page") Page<SysDictData> page, @Param("dict") SysDictData dict);

    /**
     * 根据字典类型查询字典数据
     */
    List<SysDictData> selectByDictType(@Param("dictType") String dictType);
}
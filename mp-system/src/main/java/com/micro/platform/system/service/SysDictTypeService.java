package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysDictType;

import java.util.List;

/**
 * 字典类型服务接口
 */
public interface SysDictTypeService extends IServiceX<SysDictType> {

    /**
     * 分页查询字典类型列表
     */
    Page<SysDictType> selectDictTypePage(SysDictType dictType, Integer pageNum, Integer pageSize);

    /**
     * 查询所有字典类型列表
     */
    List<SysDictType> selectDictTypeList(SysDictType dictType);
}

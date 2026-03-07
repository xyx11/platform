package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysDictData;

import java.util.List;

/**
 * 字典数据服务接口
 */
public interface SysDictDataService extends IServiceX<SysDictData> {

    /**
     * 分页查询字典数据列表
     */
    Page<SysDictData> selectDictDataPage(SysDictData dictData, Integer pageNum, Integer pageSize);

    /**
     * 根据字典类型查询字典数据列表
     */
    List<SysDictData> selectDictDataByType(String dictType);
}

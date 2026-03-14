package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysDictType;

import java.util.List;
import java.util.Map;

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

    /**
     * 导出字典类型数据
     */
    byte[] exportDictType(SysDictType dictType);

    /**
     * 刷新字典缓存
     */
    void refreshCache();

    /**
     * 删除字典类型缓存
     */
    void clearCache(String dictType);

    /**
     * 获取字典类型及其数据（树形结构）
     */
    List<Map<String, Object>> getDictTree();

    /**
     * 统计字典类型使用数量
     */
    Map<String, Long> getDictStats();
}
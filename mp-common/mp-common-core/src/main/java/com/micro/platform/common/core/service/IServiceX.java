package com.micro.platform.common.core.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 基础 Service 接口
 *
 * @param <T> 实体类型
 */
public interface IServiceX<T> extends IService<T> {

    /**
     * 根据 ID 查询
     */
    T getById(Serializable id);

    /**
     * 分页查询
     */
    Page<T> page(Page<T> page);

    /**
     * 列表查询
     */
    List<T> list();

    /**
     * 批量保存
     */
    boolean saveBatch(Collection<T> entityList, int batchSize);

    /**
     * 批量更新
     */
    boolean updateBatchById(Collection<T> entityList);
}

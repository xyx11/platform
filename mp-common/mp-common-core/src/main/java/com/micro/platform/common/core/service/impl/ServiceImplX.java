package com.micro.platform.common.core.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.micro.platform.common.core.service.IServiceX;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 基础 Service 实现类
 *
 * @param <M> Mapper 类型
 * @param <T> 实体类型
 */
public class ServiceImplX<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements IServiceX<T> {

    @Override
    public T getById(Serializable id) {
        return super.getById(id);
    }

    @Override
    public Page<T> page(Page<T> page) {
        return super.page(page);
    }

    @Override
    public List<T> list() {
        return super.list();
    }

    @Override
    public boolean saveBatch(Collection<T> entityList, int batchSize) {
        return super.saveBatch(entityList, batchSize);
    }

    @Override
    public boolean updateBatchById(Collection<T> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return true;
        }
        return super.updateBatchById(entityList);
    }
}

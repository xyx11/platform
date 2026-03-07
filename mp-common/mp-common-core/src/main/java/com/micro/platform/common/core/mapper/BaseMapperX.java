package com.micro.platform.common.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Collection;
import java.util.List;

/**
 * 基础 Mapper 接口
 *
 * @param <T> 实体类型
 */
public interface BaseMapperX<T> extends BaseMapper<T> {

    /**
     * 批量插入
     */
    default int insertBatch(Collection<T> list) {
        int size = list.size();
        int i = 0;
        int batch = 100;
        while (i < size) {
            int batchCount = Math.min(batch, size - i);
            List<T> batchList = list.stream().skip(i).limit(batchCount).toList();
            insertBatchSomeColumn(batchList);
            i += batch;
        }
        return i;
    }

    /**
     * 批量插入部分字段
     */
    int insertBatchSomeColumn(Collection<T> list);

    /**
     * 根据 ID 批量更新
     */
    int updateBatchById(Collection<T> list);
}
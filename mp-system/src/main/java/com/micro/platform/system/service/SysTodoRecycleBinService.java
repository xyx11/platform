package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysTodoRecycleBin;

/**
 * 任务回收站服务接口
 */
public interface SysTodoRecycleBinService extends IServiceX<SysTodoRecycleBin> {

    /**
     * 删除待办到回收站
     */
    void moveToRecycleBin(Long todoId);

    /**
     * 恢复待办
     */
    void recoverTodo(Long recycleId);

    /**
     * 永久删除
     */
    void deletePermanently(Long recycleId);

    /**
     * 清空回收站
     */
    void clearRecycleBin();

    /**
     * 分页获取回收站列表
     */
    Page<SysTodoRecycleBin> getRecycleBinPage(Integer pageNum, Integer pageSize);
}
package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysFile;

import java.util.List;

public interface SysFileService extends IServiceX<SysFile> {
    Page<SysFile> selectFilePage(Integer pageNum, Integer pageSize);
    void removeFile(Long fileId);
    void batchRemove(List<Long> fileIds);
}

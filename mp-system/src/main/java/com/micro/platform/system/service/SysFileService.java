package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface SysFileService extends IServiceX<SysFile> {
    Page<SysFile> selectFilePage(Integer pageNum, Integer pageSize);
    SysFile uploadFile(MultipartFile file) throws IOException;
    void removeFile(Long fileId);
    void batchRemove(List<Long> fileIds);
}
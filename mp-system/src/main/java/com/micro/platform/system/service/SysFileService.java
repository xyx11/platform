package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SysFileService extends IServiceX<SysFile> {
    Page<SysFile> selectFilePage(Integer pageNum, Integer pageSize);
    SysFile uploadFile(MultipartFile file) throws IOException;
    void removeFile(Long fileId);
    void batchRemove(List<Long> fileIds);

    /**
     * 根据条件查询文件列表
     */
    Page<SysFile> selectFilePage(SysFile file, Integer pageNum, Integer pageSize);

    /**
     * 获取文件统计信息
     */
    Map<String, Object> getFileStats();

    /**
     * 导出文件数据
     */
    byte[] exportFile(SysFile file);
}
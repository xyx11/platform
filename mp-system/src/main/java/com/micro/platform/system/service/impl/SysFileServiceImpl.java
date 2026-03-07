package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysFile;
import com.micro.platform.system.mapper.SysFileMapper;
import com.micro.platform.system.service.SysFileService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class SysFileServiceImpl extends ServiceImplX<SysFileMapper, SysFile> implements SysFileService {

    @Override
    public Page<SysFile> selectFilePage(Integer pageNum, Integer pageSize) {
        Page<SysFile> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysFile::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public void removeFile(Long fileId) {
        SysFile sysFile = getById(fileId);
        if (sysFile != null) {
            // 删除物理文件
            String tempPath = System.getProperty("java.io.tmpdir") + "/" + sysFile.getFileName();
            new File(tempPath).delete();
            // 删除数据库记录
            removeById(fileId);
        }
    }

    @Override
    public void batchRemove(List<Long> fileIds) {
        for (Long fileId : fileIds) {
            removeFile(fileId);
        }
    }
}

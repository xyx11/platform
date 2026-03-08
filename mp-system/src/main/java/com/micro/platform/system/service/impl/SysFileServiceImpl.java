package com.micro.platform.system.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysFile;
import com.micro.platform.system.mapper.SysFileMapper;
import com.micro.platform.system.service.SysFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class SysFileServiceImpl extends ServiceImplX<SysFileMapper, SysFile> implements SysFileService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public Page<SysFile> selectFilePage(Integer pageNum, Integer pageSize) {
        Page<SysFile> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysFile::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public SysFile uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String fileName = DateUtil.format(new Date(), "yyyyMMdd") + "/" + IdUtil.fastSimpleUUID() + extension;

        // 创建上传目录
        File destDir = new File(uploadPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        // 保存文件
        File destFile = new File(uploadPath + "/" + fileName);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        file.transferTo(destFile);

        // 保存数据库
        SysFile sysFile = new SysFile();
        sysFile.setFileName(fileName);
        sysFile.setOriginalName(originalFilename);
        sysFile.setFileExt(extension.replace(".", ""));
        sysFile.setFileSize(file.getSize());
        sysFile.setFileType(file.getContentType());
        sysFile.setFileUrl("/uploads/" + fileName);
        sysFile.setCreateBy(SecurityUtil.getUserId());

        save(sysFile);
        return sysFile;
    }

    @Override
    public void removeFile(Long fileId) {
        SysFile sysFile = getById(fileId);
        if (sysFile != null) {
            // 删除物理文件
            File tempFile = new File(uploadPath + "/" + sysFile.getFileName());
            if (tempFile.exists()) {
                tempFile.delete();
            }
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
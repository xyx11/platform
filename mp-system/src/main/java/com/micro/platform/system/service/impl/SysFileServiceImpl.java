package com.micro.platform.system.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysFile;
import com.micro.platform.system.mapper.SysFileMapper;
import com.micro.platform.system.service.SysFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Override
    public Page<SysFile> selectFilePage(SysFile file, Integer pageNum, Integer pageSize) {
        Page<SysFile> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(file.getOriginalName()), SysFile::getOriginalName, file.getOriginalName())
                .like(StringUtils.hasText(file.getFileExt()), SysFile::getFileExt, file.getFileExt())
                .eq(file.getFileType() != null && !file.getFileType().isEmpty(), SysFile::getFileType, file.getFileType())
                .orderByDesc(SysFile::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> getFileStats() {
        Map<String, Object> stats = new HashMap<>();

        // 统计文件总数
        long totalFileCount = baseMapper.selectCount(null);
        stats.put("totalFileCount", totalFileCount);

        // 统计总文件大小（字节）
        List<SysFile> allFiles = list();
        long totalSize = allFiles.stream().mapToLong(SysFile::getFileSize).sum();
        stats.put("totalSize", totalSize);
        stats.put("totalSizeFormatted", formatFileSize(totalSize));

        // 统计不同扩展名的文件数量
        Map<String, Long> extCount = allFiles.stream()
                .collect(Collectors.groupingBy(SysFile::getFileExt, Collectors.counting()));
        stats.put("extCount", extCount);

        // 最近上传的文件数（最近 7 天）
        long recentFileCount = allFiles.stream()
                .filter(f -> f.getCreateTime() != null &&
                        System.currentTimeMillis() - f.getCreateTime().atZone(java.time.ZoneId.systemDefault())
                                .toInstant().toEpochMilli() < TimeUnit.DAYS.toMillis(7))
                .count();
        stats.put("recentFileCount", recentFileCount);

        return stats;
    }

    @Override
    public byte[] exportFile(SysFile file) {
        try {
            LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(StringUtils.hasText(file.getOriginalName()), SysFile::getOriginalName, file.getOriginalName())
                    .like(StringUtils.hasText(file.getFileExt()), SysFile::getFileExt, file.getFileExt())
                    .eq(file.getFileType() != null && !file.getFileType().isEmpty(), SysFile::getFileType, file.getFileType())
                    .orderByDesc(SysFile::getCreateTime);
            List<SysFile> list = baseMapper.selectList(wrapper);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            EasyExcel.write(os, SysFile.class)
                    .sheet("文件管理")
                    .doWrite(list);
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出文件数据失败：" + e.getMessage());
        }
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
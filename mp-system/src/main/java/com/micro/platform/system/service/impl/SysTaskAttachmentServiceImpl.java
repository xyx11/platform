package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysTaskAttachment;
import com.micro.platform.system.mapper.SysTaskAttachmentMapper;
import com.micro.platform.system.service.SysTaskAttachmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 任务附件服务实现
 */
@Service
public class SysTaskAttachmentServiceImpl implements SysTaskAttachmentService {

    private static final Logger log = LoggerFactory.getLogger(SysTaskAttachmentServiceImpl.class);

    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/micro-platform/uploads/attachments/";

    private final SysTaskAttachmentMapper attachmentMapper;

    public SysTaskAttachmentServiceImpl(SysTaskAttachmentMapper attachmentMapper) {
        this.attachmentMapper = attachmentMapper;
    }

    @Override
    public List<SysTaskAttachment> getAttachmentsByTodoId(Long todoId) {
        return attachmentMapper.selectAttachmentsByTodoId(todoId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysTaskAttachment uploadAttachment(Long todoId, MultipartFile file) {
        try {
            // 创建上传目录（按日期分类）
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadPath = Paths.get(UPLOAD_DIR, dateDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName != null && originalFileName.contains(".")
                    ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
            String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

            // 保存文件
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath);

            // 保存数据库记录
            SysTaskAttachment attachment = new SysTaskAttachment();
            attachment.setTodoId(todoId);
            attachment.setAttachmentName(originalFileName);
            attachment.setAttachmentPath(dateDir + "/" + fileName);
            attachment.setAttachmentType(file.getContentType());
            attachment.setFileSize(file.getSize());
            attachment.setUploadUserId(SecurityUtil.getUserId());
            attachment.setUploadUserName(SecurityUtil.getUsername());
            attachment.setDownloadCount(0);
            attachment.setCreateTime(java.time.LocalDateTime.now());
            attachment.setDeleted(0);

            attachmentMapper.insert(attachment);
            log.info("上传附件：{}, 大小：{} bytes", originalFileName, file.getSize());
            return attachment;
        } catch (IOException e) {
            log.error("上传附件失败", e);
            throw new BusinessException("上传附件失败：" + e.getMessage());
        }
    }

    @Override
    public byte[] downloadAttachment(Long attachmentId) {
        SysTaskAttachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            throw new BusinessException("附件不存在");
        }

        try {
            Path filePath = Paths.get(UPLOAD_DIR, attachment.getAttachmentPath());
            byte[] content = Files.readAllBytes(filePath);

            // 增加下载次数
            attachment.setDownloadCount(attachment.getDownloadCount() + 1);
            attachmentMapper.updateById(attachment);

            return content;
        } catch (IOException e) {
            log.error("下载附件失败", e);
            throw new BusinessException("下载附件失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(Long attachmentId) {
        SysTaskAttachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            throw new BusinessException("附件不存在");
        }

        // 删除文件
        try {
            Path filePath = Paths.get(UPLOAD_DIR, attachment.getAttachmentPath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            log.warn("删除物理文件失败：{}", attachment.getAttachmentPath(), e);
        }

        // 删除数据库记录
        attachmentMapper.deleteById(attachmentId);
        log.info("删除附件：{}", attachmentId);
    }

    @Override
    public int getAttachmentCount(Long todoId) {
        return attachmentMapper.selectCountByTodoId(todoId);
    }
}
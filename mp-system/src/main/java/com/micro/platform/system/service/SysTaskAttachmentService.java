package com.micro.platform.system.service;

import com.micro.platform.system.entity.SysTaskAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 任务附件服务接口
 */
public interface SysTaskAttachmentService {

    /**
     * 获取任务附件列表
     */
    List<SysTaskAttachment> getAttachmentsByTodoId(Long todoId);

    /**
     * 上传附件
     */
    SysTaskAttachment uploadAttachment(Long todoId, MultipartFile file);

    /**
     * 下载附件
     */
    byte[] downloadAttachment(Long attachmentId);

    /**
     * 删除附件
     */
    void deleteAttachment(Long attachmentId);

    /**
     * 获取附件数量
     */
    int getAttachmentCount(Long todoId);
}
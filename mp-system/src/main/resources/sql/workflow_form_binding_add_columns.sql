-- 为 workflow_form_binding 表添加 status 和 remark 字段
-- 执行时间：2026-03-16

-- 添加 status 字段（状态：0-停用，1-启用）
ALTER TABLE `workflow_form_binding`
ADD COLUMN `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-停用，1-启用' AFTER `form_type`;

-- 添加 remark 字段（备注）
ALTER TABLE `workflow_form_binding`
ADD COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '备注' AFTER `status`;

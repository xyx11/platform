-- 更新 workflow_form_binding 表结构
-- 执行日期：2026-03-17
-- 说明：添加 form_config 字段用于存储表单配置 JSON

-- 检查字段是否存在，不存在则添加
SELECT COUNT(*) INTO @cnt FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'workflow_form_binding'
AND COLUMN_NAME = 'form_config';

SET @sql = IF(@cnt = 0,
  'ALTER TABLE `workflow_form_binding` ADD COLUMN `form_config` text COMMENT ''表单配置（JSON）'' AFTER `form_schema`',
  'SELECT ''Column form_config already exists'' AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证结果
DESC workflow_form_binding;
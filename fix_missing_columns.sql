-- 修复缺失的字段
-- 执行时间：2026-03-08
-- 使用方法：mysql -u root -p database_name < fix_missing_columns.sql

-- 1. 修复 sys_post 表 - 添加 create_by_name 和 update_by_name 字段
ALTER TABLE `sys_post`
ADD COLUMN IF NOT EXISTS `create_by_name` varchar(50) DEFAULT NULL COMMENT '创建者名称' AFTER `create_by`;

ALTER TABLE `sys_post`
ADD COLUMN IF NOT EXISTS `update_by_name` varchar(50) DEFAULT NULL COMMENT '更新者名称' AFTER `update_by`;

-- 2. 修复 sys_notice 表 - 添加 publish_time、timing_publish 和 create_by_name 字段
ALTER TABLE `sys_notice`
ADD COLUMN IF NOT EXISTS `publish_time` datetime DEFAULT NULL COMMENT '发布时间' AFTER `status`;

ALTER TABLE `sys_notice`
ADD COLUMN IF NOT EXISTS `timing_publish` int DEFAULT '0' COMMENT '是否定时发布 (0:否 1:是)' AFTER `publish_time`;

ALTER TABLE `sys_notice`
ADD COLUMN IF NOT EXISTS `create_by_name` varchar(50) DEFAULT NULL COMMENT '创建者名称' AFTER `create_by`;

-- 3. 修复 sys_user 表 - 添加 dept_name 字段
ALTER TABLE `sys_user`
ADD COLUMN IF NOT EXISTS `dept_name` varchar(50) DEFAULT NULL COMMENT '部门名称' AFTER `dept_id`;

-- 4. 修复 sys_menu 表 - 添加 is_iframe 字段
ALTER TABLE `sys_menu`
ADD COLUMN IF NOT EXISTS `is_iframe` int DEFAULT '0' COMMENT '是否外链 iframe (0:否 1:是)' AFTER `is_frame`;

-- 5. 修复 sys_role 表 - 添加 data_scope 字段
ALTER TABLE `sys_role`
ADD COLUMN IF NOT EXISTS `data_scope` int DEFAULT '1' COMMENT '数据权限范围' AFTER `description`;
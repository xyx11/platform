-- 通知公告阅读记录表
CREATE TABLE IF NOT EXISTS `sys_notice_user` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `notice_id` bigint NOT NULL COMMENT '公告 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `read_status` int DEFAULT '0' COMMENT '阅读状态 (0:未读 1:已读)',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notice_user` (`notice_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_notice_id` (`notice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知公告阅读记录表';

-- 修改 sys_notice 表，添加定时发布字段
ALTER TABLE `sys_notice`
ADD COLUMN `publish_time` datetime DEFAULT NULL COMMENT '发布时间' AFTER `status`,
ADD COLUMN `timing_publish` int DEFAULT '0' COMMENT '是否定时发布 (0:否 1:是)' AFTER `remark`;
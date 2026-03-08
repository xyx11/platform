-- 修复 sys_notice_user 表缺失问题
-- 问题：通知公告列表查询失败，提示表不存在

CREATE TABLE IF NOT EXISTS `sys_notice_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `notice_id` BIGINT NOT NULL COMMENT '公告 ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `read_status` TINYINT DEFAULT 0 COMMENT '阅读状态：0=未读，1=已读',
  `read_time` DATETIME COMMENT '阅读时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_notice_user` (`notice_id`, `user_id`),
  KEY `idx_user_read` (`user_id`, `read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知公告阅读记录表';
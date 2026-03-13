-- 命令执行记录表
CREATE TABLE IF NOT EXISTS `sys_command_log` (
  `command_id` bigint NOT NULL COMMENT '命令 ID',
  `command_type` varchar(50) DEFAULT NULL COMMENT '命令类型',
  `command_content` text DEFAULT NULL COMMENT '命令内容',
  `command_result` text DEFAULT NULL COMMENT '命令结果',
  `execute_time` bigint DEFAULT NULL COMMENT '执行时长（毫秒）',
  `execute_by` bigint DEFAULT NULL COMMENT '执行者 ID',
  `execute_name` varchar(50) DEFAULT NULL COMMENT '执行者名称',
  `execute_time_str` datetime DEFAULT NULL COMMENT '执行时间',
  `status` int DEFAULT '0' COMMENT '状态：0-失败 1-成功',
  `error_msg` text DEFAULT NULL COMMENT '错误消息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`command_id`),
  KEY `idx_execute_by` (`execute_by`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='命令执行记录表';

-- 待办事项表
CREATE TABLE IF NOT EXISTS `sys_todo` (
  `todo_id` bigint NOT NULL COMMENT '待办 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `todo_title` varchar(200) DEFAULT NULL COMMENT '待办标题',
  `todo_content` text DEFAULT NULL COMMENT '待办内容',
  `todo_type` varchar(50) DEFAULT NULL COMMENT '待办类型 (1=工作 2=会议 3=提醒 4=其他)',
  `priority` int DEFAULT '3' COMMENT '优先级 (1=紧急重要 2=重要 3=一般 4=次要)',
  `status` int DEFAULT '0' COMMENT '状态 (0=待处理 1=已完成 2=已取消)',
  `plan_time` datetime DEFAULT NULL COMMENT '计划完成时间',
  `actual_time` datetime DEFAULT NULL COMMENT '实际完成时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_by_name` varchar(50) DEFAULT NULL COMMENT '创建者名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  PRIMARY KEY (`todo_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_todo_type` (`todo_type`),
  KEY `idx_priority` (`priority`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='待办事项表';

-- 插入示例数据
INSERT INTO `sys_todo` (`todo_id`, `user_id`, `todo_title`, `todo_content`, `todo_type`, `priority`, `status`, `plan_time`, `create_time`, `create_by_name`) VALUES
(1, 1, '完成系统需求分析', '编写系统需求文档，包括功能需求和非功能需求', '1', 2, 1, '2024-01-15 18:00:00', NOW(), '管理员'),
(2, 1, '参加项目周会', '每周一下午 3 点项目组例会', '2', 3, 0, '2024-01-20 15:00:00', NOW(), '管理员'),
(3, 1, '代码审查', '审查团队成员提交的代码', '1', 3, 0, '2024-01-22 12:00:00', NOW(), '管理员')
ON DUPLICATE KEY UPDATE `todo_title`=`todo_title`;
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

-- 待办事项标签表
CREATE TABLE IF NOT EXISTS `sys_todo_tag` (
  `tag_id` bigint NOT NULL COMMENT '标签 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `tag_name` varchar(50) DEFAULT NULL COMMENT '标签名称',
  `tag_color` varchar(20) DEFAULT NULL COMMENT '标签颜色',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`tag_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='待办事项标签表';

-- 待办事项与标签关联表
CREATE TABLE IF NOT EXISTS `sys_todo_tag_relation` (
  `relation_id` bigint NOT NULL COMMENT '关联 ID',
  `todo_id` bigint NOT NULL COMMENT '待办 ID',
  `tag_id` bigint NOT NULL COMMENT '标签 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`relation_id`),
  KEY `idx_todo_id` (`todo_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='待办事项与标签关联表';

-- 待办事项回收站表
CREATE TABLE IF NOT EXISTS `sys_todo_recycle_bin` (
  `recycle_id` bigint NOT NULL COMMENT '回收站 ID',
  `todo_id` bigint NOT NULL COMMENT '待办 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `todo_title` varchar(200) DEFAULT NULL COMMENT '待办标题',
  `todo_content` text DEFAULT NULL COMMENT '待办内容',
  `delete_by` bigint DEFAULT NULL COMMENT '删除人 ID',
  `delete_by_name` varchar(50) DEFAULT NULL COMMENT '删除人名称',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `recover_time` datetime DEFAULT NULL COMMENT '恢复时间',
  `is_recover` int DEFAULT '0' COMMENT '是否已恢复 (0=未恢复 1=已恢复)',
  PRIMARY KEY (`recycle_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_todo_id` (`todo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='待办事项回收站表';

-- 待办事项评论表
CREATE TABLE IF NOT EXISTS `sys_task_comment` (
  `comment_id` bigint NOT NULL COMMENT '评论 ID',
  `todo_id` bigint NOT NULL COMMENT '待办 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `content` text DEFAULT NULL COMMENT '评论内容',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论 ID',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_by_name` varchar(50) DEFAULT NULL COMMENT '创建者名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`comment_id`),
  KEY `idx_todo_id` (`todo_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='待办事项评论表';

-- 待办事项附件表
CREATE TABLE IF NOT EXISTS `sys_task_attachment` (
  `attachment_id` bigint NOT NULL COMMENT '附件 ID',
  `todo_id` bigint NOT NULL COMMENT '待办 ID',
  `attachment_name` varchar(255) DEFAULT NULL COMMENT '附件名称',
  `attachment_path` varchar(500) DEFAULT NULL COMMENT '附件路径',
  `attachment_type` varchar(100) DEFAULT NULL COMMENT '附件类型',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小 (字节)',
  `upload_user_id` bigint DEFAULT NULL COMMENT '上传人 ID',
  `upload_user_name` varchar(50) DEFAULT NULL COMMENT '上传人名称',
  `download_count` int DEFAULT '0' COMMENT '下载次数',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`attachment_id`),
  KEY `idx_todo_id` (`todo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='待办事项附件表';
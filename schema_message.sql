-- ============================================
-- 消息中心表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_message` (
  `id` bigint NOT NULL COMMENT '消息 ID',
  `message_type` int DEFAULT NULL COMMENT '消息类型 (1:系统消息 2:通知消息 3:待办消息 4:预警消息)',
  `title` varchar(200) DEFAULT NULL COMMENT '消息标题',
  `content` text DEFAULT NULL COMMENT '消息内容',
  `level` int DEFAULT NULL COMMENT '消息级别 (1:普通 2:重要 3:紧急)',
  `sender_id` bigint DEFAULT NULL COMMENT '发送者 ID',
  `sender_name` varchar(50) DEFAULT NULL COMMENT '发送者名称',
  `receiver_id` bigint DEFAULT NULL COMMENT '接收者 ID (0 表示所有人)',
  `receiver_dept_id` bigint DEFAULT NULL COMMENT '接收者部门 ID',
  `receiver_role_id` bigint DEFAULT NULL COMMENT '接收者角色 ID',
  `is_read` int DEFAULT '0' COMMENT '是否已读 (0:未读 1:已读)',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `schedule_time` datetime DEFAULT NULL COMMENT '定时发送时间',
  `send_status` int DEFAULT '0' COMMENT '发送状态 (0:草稿 1:已发送 2:已撤回)',
  `send_time` datetime DEFAULT NULL COMMENT '发送时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户 ID',
  PRIMARY KEY (`id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_send_status` (`send_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- ============================================
-- 消息接收者表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_message_receiver` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `message_id` bigint NOT NULL COMMENT '消息 ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者 ID',
  `receiver_name` varchar(50) DEFAULT NULL COMMENT '接收者名称',
  `dept_id` bigint DEFAULT NULL COMMENT '部门 ID',
  `is_read` int DEFAULT '0' COMMENT '读取状态 (0:未读 1:已读)',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_message_id` (`message_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息接收者表';

-- ============================================
-- 消息中心菜单权限
-- ============================================
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `status`) VALUES
(24, 1, '消息中心', '/system/message', 'system/message/index', 'system:message:list', 2, 'Bell', 14, 1),
(2401, 24, '消息查询', '', '', 'system:message:query', 3, '', 1, 1, NOW()),
(2402, 24, '发送消息', '', '', 'system:message:add', 3, '', 2, 1, NOW()),
(2403, 24, '消息删除', '', '', 'system:message:remove', 3, '', 3, 1, NOW()),
(2404, 24, '撤回消息', '', '', 'system:message:withdraw', 3, '', 4, 1, NOW())
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- 重新关联超级管理员角色和所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu ON DUPLICATE KEY UPDATE role_id=role_id;
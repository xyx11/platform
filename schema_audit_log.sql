-- ============================================
-- 审计日志表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_audit_log` (
  `id` bigint NOT NULL COMMENT '日志 ID',
  `module` varchar(50) DEFAULT NULL COMMENT '模块名称',
  `operation_type` int DEFAULT NULL COMMENT '操作类型 (1:新增 2:修改 3:删除 4:查询 5:导入 6:导出 7:其他)',
  `description` varchar(255) DEFAULT NULL COMMENT '操作描述',
  `table_name` varchar(50) DEFAULT NULL COMMENT '表名',
  `record_id` bigint DEFAULT NULL COMMENT '记录 ID',
  `before_data` text DEFAULT NULL COMMENT '变更前数据 (JSON)',
  `after_data` text DEFAULT NULL COMMENT '变更后数据 (JSON)',
  `change_fields` text DEFAULT NULL COMMENT '变更字段 (JSON)',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人 ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人名称',
  `dept_id` bigint DEFAULT NULL COMMENT '部门 ID',
  `dept_name` varchar(50) DEFAULT NULL COMMENT '部门名称',
  `oper_ip` varchar(50) DEFAULT NULL COMMENT '操作 IP',
  `oper_location` varchar(100) DEFAULT NULL COMMENT '操作地点',
  `request_method` varchar(20) DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求 URL',
  `request_params` text DEFAULT NULL COMMENT '请求参数',
  `execute_time` bigint DEFAULT NULL COMMENT '执行时长（毫秒）',
  `status` int DEFAULT NULL COMMENT '操作状态（0-失败 1-成功）',
  `error_msg` text DEFAULT NULL COMMENT '错误消息',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_table_name` (`table_name`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- ============================================
-- 审计日志菜单权限
-- ============================================
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `status`) VALUES
(23, 1, '审计日志', '/system/audit-log', 'system/audit-log/index', 'system:audit-log:list', 2, 'Document', 13, 1),
(2301, 23, '审计日志查询', '', '', 'system:audit-log:query', 3, '', 1, 1, NOW()),
(2302, 23, '审计日志删除', '', '', 'system:audit-log:remove', 3, '', 2, 1, NOW()),
(2303, 23, '审计日志清空', '', '', 'system:audit-log:clear', 3, '', 3, 1, NOW())
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- 重新关联超级管理员角色和所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu ON DUPLICATE KEY UPDATE role_id=role_id;
-- ============================================
-- 动态表单设计器 - 表单定义表
-- ============================================
CREATE TABLE IF NOT EXISTS `form_definition` (
  `id` bigint NOT NULL COMMENT '表单 ID',
  `form_name` varchar(100) DEFAULT NULL COMMENT '表单名称',
  `form_code` varchar(50) DEFAULT NULL COMMENT '表单编码',
  `form_type` int DEFAULT NULL COMMENT '表单类型 (1:普通表单 2:流程表单 3:调查表单)',
  `form_config` text DEFAULT NULL COMMENT '表单配置 JSON',
  `form_schema` text DEFAULT NULL COMMENT '表单 schema JSON',
  `data_source_config` text DEFAULT NULL COMMENT '数据源配置 JSON',
  `status` int DEFAULT '0' COMMENT '状态 (0:草稿 1:发布 2:停用)',
  `version` int DEFAULT '1' COMMENT '版本号',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_by_name` varchar(50) DEFAULT NULL COMMENT '创建者名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_by_name` varchar(50) DEFAULT NULL COMMENT '更新者名称',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户 ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_form_code` (`form_code`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表单定义表';

-- ============================================
-- 动态表单设计器 - 表单数据表
-- ============================================
CREATE TABLE IF NOT EXISTS `form_data` (
  `id` bigint NOT NULL COMMENT '数据 ID',
  `form_id` bigint NOT NULL COMMENT '表单 ID',
  `form_code` varchar(50) DEFAULT NULL COMMENT '表单编码',
  `form_data` text DEFAULT NULL COMMENT '表单数据 JSON',
  `submitter_id` bigint DEFAULT NULL COMMENT '提交者 ID',
  `submitter_name` varchar(50) DEFAULT NULL COMMENT '提交者名称',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `status` int DEFAULT '0' COMMENT '状态 (0:草稿 1:已提交 2:已审核 3:已驳回)',
  `process_instance_id` bigint DEFAULT NULL COMMENT '流程实例 ID',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户 ID',
  PRIMARY KEY (`id`),
  KEY `idx_form_id` (`form_id`),
  KEY `idx_submitter_id` (`submitter_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表单数据表';

-- ============================================
-- 动态表单设计器菜单权限
-- ============================================
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `status`) VALUES
(26, 1, '表单设计器', '/system/form-designer', 'system/form-designer/index', 'system:form-designer:list', 2, 'DesignServices', 16, 1),
(2601, 26, '表单定义查询', '', '', 'system:form-definition:query', 3, '', 1, 1, NOW()),
(2602, 26, '表单定义新增', '', '', 'system:form-definition:add', 3, '', 2, 1, NOW()),
(2603, 26, '表单定义修改', '', '', 'system:form-definition:edit', 3, '', 3, 1, NOW()),
(2604, 26, '表单定义删除', '', '', 'system:form-definition:remove', 3, '', 4, 1, NOW()),
(2605, 26, '表单定义发布', '', '', 'system:form-definition:publish', 3, '', 5, 1, NOW()),
(2606, 26, '表单数据查询', '', '', 'system:form-data:query', 3, '', 6, 1, NOW()),
(2607, 26, '表单数据提交', '', '', 'system:form-data:submit', 3, '', 7, 1, NOW()),
(2608, 26, '表单数据审核', '', '', 'system:form-data:audit', 3, '', 8, 1, NOW()),
(2609, 26, '表单数据删除', '', '', 'system:form-data:remove', 3, '', 9, 1, NOW())
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- 重新关联超级管理员角色和所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu ON DUPLICATE KEY UPDATE role_id=role_id;
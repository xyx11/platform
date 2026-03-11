-- ============================================
-- WebSocket 消息推送菜单权限
-- ============================================
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `is_frame`, `is_cache`, `visible`, `status`, `remark`, `create_time`) VALUES
(28, 1, '消息推送', '/system/websocket', 'system/websocket/index', 'system:websocket:list', 2, 'Connection', 18, 0, 0, 1, 1, NULL, NOW()),
(2801, 28, '发送消息', '', '', 'system:ws:send', 3, '', 1, 0, 0, 1, 1, NULL, NOW()),
(2802, 28, '广播消息', '', '', 'system:ws:broadcast', 3, '', 2, 0, 0, 1, 1, NULL, NOW())
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- ============================================
-- 租户套餐管理表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_tenant_package` (
  `id` bigint NOT NULL COMMENT '套餐 ID',
  `name` varchar(100) DEFAULT NULL COMMENT '套餐名称',
  `code` varchar(50) DEFAULT NULL COMMENT '套餐编码',
  `description` varchar(255) DEFAULT NULL COMMENT '套餐描述',
  `package_type` int DEFAULT NULL COMMENT '套餐类型 (1:免费版 2:基础版 3:专业版 4:企业版 5:定制版)',
  `price` decimal(10,2) DEFAULT NULL COMMENT '价格（元/月）',
  `max_users` int DEFAULT NULL COMMENT '最大用户数',
  `max_depts` int DEFAULT NULL COMMENT '最大部门数',
  `max_storage` int DEFAULT NULL COMMENT '最大存储空间（MB）',
  `features` text DEFAULT NULL COMMENT '可用功能模块（JSON）',
  `status` int DEFAULT '1' COMMENT '状态 (0:停用 1:启用)',
  `sort` int DEFAULT '0' COMMENT '排序',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户套餐表';

-- ============================================
-- 细粒度数据权限规则表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_data_permission` (
  `id` bigint NOT NULL COMMENT '权限 ID',
  `role_id` bigint NOT NULL COMMENT '角色 ID',
  `menu_id` bigint DEFAULT NULL COMMENT '菜单 ID',
  `table_name` varchar(50) DEFAULT NULL COMMENT '表名',
  `permission_type` int DEFAULT NULL COMMENT '权限类型 (1:行级 2:列级 3:字段级)',
  `rule_expression` varchar(500) DEFAULT NULL COMMENT '规则表达式（SpEL）',
  `allowed_columns` text DEFAULT NULL COMMENT '允许的字段',
  `denied_columns` text DEFAULT NULL COMMENT '禁止的字段',
  `data_filter` varchar(1000) DEFAULT NULL COMMENT '数据过滤条件',
  `status` int DEFAULT '1' COMMENT '状态 (0:停用 1:启用)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  PRIMARY KEY (`id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_table_name` (`table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据权限规则表';

-- ============================================
-- 租户套餐管理菜单权限
-- ============================================
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `is_frame`, `is_cache`, `visible`, `status`, `remark`, `create_time`) VALUES
(29, 1, '套餐管理', '/system/tenant-package', 'system/tenant-package/index', 'system:tenant-package:list', 2, 'Package', 19, 0, 0, 1, 1, NULL, NOW()),
(2901, 29, '套餐查询', '', '', 'system:tenant-package:query', 3, '', 1, 0, 0, 1, 1, NULL, NOW()),
(2902, 29, '套餐新增', '', '', 'system:tenant-package:add', 3, '', 2, 0, 0, 1, 1, NULL, NOW()),
(2903, 29, '套餐修改', '', '', 'system:tenant-package:edit', 3, '', 3, 0, 0, 1, 1, NULL, NOW()),
(2904, 29, '套餐删除', '', '', 'system:tenant-package:remove', 3, '', 4, 0, 0, 1, 1, NULL, NOW()),
(2905, 29, '套餐停用', '', '', 'system:tenant-package:disable', 3, '', 5, 0, 0, 1, 1, NULL, NOW())
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- ============================================
-- 细粒度数据权限菜单权限
-- ============================================
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `is_frame`, `is_cache`, `visible`, `status`, `remark`, `create_time`) VALUES
(30, 1, '数据权限规则', '/system/data-permission', 'system/data-permission/index', 'system:data-permission:list', 2, 'Lock', 20, 0, 0, 1, 1, NULL, NOW()),
(3001, 30, '规则查询', '', '', 'system:data-permission:query', 3, '', 1, 0, 0, 1, 1, NULL, NOW()),
(3002, 30, '规则新增', '', '', 'system:data-permission:add', 3, '', 2, 0, 0, 1, 1, NULL, NOW()),
(3003, 30, '规则修改', '', '', 'system:data-permission:edit', 3, '', 3, 0, 0, 1, 1, NULL, NOW()),
(3004, 30, '规则删除', '', '', 'system:data-permission:remove', 3, '', 4, 0, 0, 1, 1, NULL, NOW())
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- ============================================
-- 表单渲染菜单权限
-- ============================================
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `is_frame`, `is_cache`, `visible`, `status`, `remark`, `create_time`) VALUES
(31, 1, '表单渲染', '/system/form-render', 'system/form-render/index', 'system:form-render:list', 2, 'TextFields', 21, 0, 0, 1, 1, NULL, NOW()),
(3101, 31, '表单查询', '', '', 'system:form-render:query', 3, '', 1, 0, 0, 1, 1, NULL, NOW()),
(3102, 31, '表单验证', '', '', 'system:form-render:validate', 3, '', 2, 0, 0, 1, 1, NULL, NOW())
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- 重新关联超级管理员角色和所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu ON DUPLICATE KEY UPDATE role_id=role_id;

-- ============================================
-- 插入默认租户套餐数据
-- ============================================
INSERT INTO `sys_tenant_package` (`id`, `name`, `code`, `description`, `package_type`, `price`, `max_users`, `max_depts`, `max_storage`, `features`, `status`, `sort`) VALUES
(1, '免费版', 'free', '适合个人用户，基础功能免费使用', 1, 0.00, 5, 3, 100, '{"user":true,"dept":true,"dict":true}', 1, 1),
(2, '基础版', 'basic', '适合小团队，提供基础协作功能', 2, 99.00, 20, 10, 1000, '{"user":true,"dept":true,"dict":true,"message":true,"form":true}', 1, 2),
(3, '专业版', 'professional', '适合中小企业，完整的功能套件', 3, 299.00, 100, 50, 10000, '{"all":true}', 1, 3),
(4, '企业版', 'enterprise', '适合大型企业，支持定制开发', 4, 999.00, 9999, 9999, 102400, '{"all":true,"custom":true}', 1, 4)
ON DUPLICATE KEY UPDATE `name`=`name`;

-- ============================================
-- 工作流表单绑定关系表
-- ============================================
CREATE TABLE IF NOT EXISTS `wf_form_binding` (
  `id` bigint NOT NULL COMMENT '绑定 ID',
  `process_definition_key` varchar(100) DEFAULT NULL COMMENT '流程定义 Key',
  `process_definition_id` varchar(64) DEFAULT NULL COMMENT '流程定义 ID',
  `task_definition_key` varchar(100) DEFAULT NULL COMMENT '任务节点 Key',
  `form_code` varchar(50) DEFAULT NULL COMMENT '表单编码',
  `form_name` varchar(200) DEFAULT NULL COMMENT '表单名称',
  `form_type` int DEFAULT NULL COMMENT '表单类型 (1:启动表单 2:办理表单)',
  `binding_key` varchar(255) DEFAULT NULL COMMENT '绑定关系唯一标识',
  `status` int DEFAULT '1' COMMENT '状态 (0:停用 1:启用)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_binding_key` (`binding_key`),
  KEY `idx_process_key` (`process_definition_key`),
  KEY `idx_task_key` (`task_definition_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流表单绑定关系表';

-- ============================================
-- 工作流表单绑定菜单权限
-- ============================================
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `is_frame`, `is_cache`, `visible`, `status`, `remark`, `create_time`) VALUES
(32, 21, '流程表单', '/system/workflow-form', 'system/workflow-form/index', 'system:workflow-form:list', 2, 'FileText', 22, 0, 0, 1, 1, NULL, NOW()),
(3201, 32, '表单绑定', '', '', 'system:workflow-form:bind', 3, '', 1, 0, 0, 1, 1, NULL, NOW()),
(3202, 32, '表单查询', '', '', 'system:workflow-form:query', 3, '', 2, 0, 0, 1, 1, NULL, NOW()),
(3203, 32, '启动流程', '', '', 'system:workflow-form:start', 3, '', 3, 0, 0, 1, 1, NULL, NOW()),
(3204, 32, '完成任务', '', '', 'system:workflow-form:complete', 3, '', 4, 0, 0, 1, 1, NULL, NOW()),
(3205, 32, '草稿管理', '', '', 'system:workflow-form:draft', 3, '', 5, 0, 0, 1, 1, NULL, NOW())
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- ============================================
-- 工作流流程定义管理菜单权限
-- ============================================
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `is_frame`, `is_cache`, `visible`, `status`, `remark`, `create_time`) VALUES
(33, 21, '流程定义', '/system/workflow-definition', 'system/workflow-definition/index', 'system:workflow-definition:list', 2, 'Flowchart', 23, 0, 0, 1, 1, NULL, NOW()),
(3301, 33, '流程查询', '', '', 'system:workflow-definition:query', 3, '', 1, 0, 0, 1, 1, NULL, NOW()),
(3302, 33, '流程部署', '', '', 'system:workflow-definition:deploy', 3, '', 2, 0, 0, 1, 1, NULL, NOW()),
(3303, 33, '流程删除', '', '', 'system:workflow-definition:delete', 3, '', 3, 0, 0, 1, 1, NULL, NOW())
ON DUPLICATE KEY UPDATE menu_name=menu_name;
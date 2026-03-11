-- ============================================
-- 租户表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_tenant` (
  `id` bigint NOT NULL COMMENT '租户 ID',
  `name` varchar(100) DEFAULT NULL COMMENT '租户名称',
  `code` varchar(50) DEFAULT NULL COMMENT '租户编码',
  `contact` varchar(50) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` int DEFAULT '1' COMMENT '租户状态 (0:禁用 1:正常)',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户表';

-- ============================================
-- 插入默认租户数据
-- ============================================
INSERT INTO `sys_tenant` (`id`, `name`, `code`, `contact`, `phone`, `email`, `status`, `create_time`) VALUES
(1, '默认租户', 'default', '管理员', '13800138000', 'admin@example.com', 1, NOW())
ON DUPLICATE KEY UPDATE `name`=`name`;

-- ============================================
-- 租户管理菜单权限
-- ============================================
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `status`) VALUES
(25, 1, '租户管理', '/system/tenant', 'system/tenant/index', 'system:tenant:list', 2, 'Team', 15, 1),
(2501, 25, '租户查询', '', '', 'system:tenant:query', 3, '', 1, 1, NOW()),
(2502, 25, '租户新增', '', '', 'system:tenant:add', 3, '', 2, 1, NOW()),
(2503, 25, '租户修改', '', '', 'system:tenant:edit', 3, '', 3, 1, NOW()),
(2504, 25, '租户删除', '', '', 'system:tenant:remove', 3, '', 4, 1, NOW())
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- 重新关联超级管理员角色和所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu ON DUPLICATE KEY UPDATE role_id=role_id;
-- ============================================
-- 工作流引擎菜单权限
-- ============================================
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `status`) VALUES
(27, 1, '工作流管理', '/system/workflow', 'system/workflow/index', 'system:workflow:list', 2, 'FlowChart', 17, 1),
(2701, 27, '流程定义查询', '', '', 'system:workflow:query', 3, '', 1, 1, NOW()),
(2702, 27, '启动流程', '', '', 'system:workflow:start', 3, '', 2, 1, NOW()),
(2703, 27, '完成任务', '', '', 'system:workflow:complete', 3, '', 3, 1, NOW()),
(2704, 27, '流程删除', '', '', 'system:workflow:delete', 3, '', 4, 1, NOW()),
(2705, 27, '流程挂起', '', '', 'system:workflow:suspend', 3, '', 5, 1, NOW()),
(2706, 27, '流程激活', '', '', 'system:workflow:activate', 3, '', 6, 1, NOW())
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- 重新关联超级管理员角色和所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu ON DUPLICATE KEY UPDATE role_id=role_id;
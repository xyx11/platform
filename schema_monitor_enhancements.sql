-- ============================================
-- 在线用户管理菜单权限
-- ============================================
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `is_frame`, `is_cache`, `visible`, `status`, `remark`, `create_time`) VALUES
(32, 1, '在线用户', '/system/online-user', 'system/online-user/index', 'system:online-user:list', 2, 'User', 22, 0, 0, 1, 1, NULL, NOW()),
(3201, 32, '用户查询', '', '', 'system:online-user:query', 3, '', 1, 0, 0, 1, 1, NULL, NOW()),
(3202, 32, '强制下线', '', '', 'system:online-user:kickout', 3, '', 2, 0, 0, 1, 1, NULL, NOW())
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- ============================================
-- 服务器监控增强菜单权限
-- ============================================
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `is_frame`, `is_cache`, `visible`, `status`, `remark`, `create_time`) VALUES
(33, 1, '服务器监控', '/system/server-monitor', 'system/monitor/server', 'system:monitor:info', 2, 'Server', 23, 0, 0, 1, 1, NULL, NOW())
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- 重新关联超级管理员角色和所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu ON DUPLICATE KEY UPDATE role_id=role_id;
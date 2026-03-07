-- 创建系统用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门 ID',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `password` varchar(100) DEFAULT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `gender` int DEFAULT NULL COMMENT '性别 (0:女 1:男)',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` int DEFAULT NULL COMMENT '状态 (0:禁用 1:正常)',
  `login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录 IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 创建角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `role_id` bigint NOT NULL COMMENT '角色 ID',
  `role_name` varchar(50) DEFAULT NULL COMMENT '角色名称',
  `role_code` varchar(50) DEFAULT NULL COMMENT '角色编码',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  `data_scope` int DEFAULT NULL COMMENT '数据权限范围',
  `status` int DEFAULT NULL COMMENT '状态 (0:禁用 1:正常)',
  `sort` int DEFAULT NULL COMMENT '排序',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- 创建菜单表
CREATE TABLE IF NOT EXISTS `sys_menu` (
  `menu_id` bigint NOT NULL COMMENT '菜单 ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父菜单 ID',
  `menu_name` varchar(50) DEFAULT NULL COMMENT '菜单名称',
  `path` varchar(255) DEFAULT NULL COMMENT '路由地址',
  `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
  `permission` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `type` int DEFAULT NULL COMMENT '菜单类型 (1:目录 2:菜单 3:按钮)',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标',
  `sort` int DEFAULT NULL COMMENT '排序',
  `is_frame` int DEFAULT NULL COMMENT '是否外链 (0:是 1:否)',
  `is_cache` int DEFAULT NULL COMMENT '是否缓存 (0:缓存 1:不缓存)',
  `visible` int DEFAULT NULL COMMENT '是否显示 (0:隐藏 1:显示)',
  `status` int DEFAULT NULL COMMENT '状态 (0:禁用 1:正常)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统菜单表';

-- 创建部门表
CREATE TABLE IF NOT EXISTS `sys_dept` (
  `dept_id` bigint NOT NULL COMMENT '部门 ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父部门 ID',
  `dept_name` varchar(50) DEFAULT NULL COMMENT '部门名称',
  `sort` int DEFAULT NULL COMMENT '排序',
  `leader` varchar(50) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` int DEFAULT NULL COMMENT '状态 (0:禁用 1:正常)',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统部门表';

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `role_id` bigint NOT NULL COMMENT '角色 ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 创建角色菜单关联表
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色 ID',
  `menu_id` bigint NOT NULL COMMENT '菜单 ID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- 创建字典类型表
CREATE TABLE IF NOT EXISTS `sys_dict_type` (
  `dict_id` bigint NOT NULL COMMENT '字典主键',
  `dict_name` varchar(50) DEFAULT NULL COMMENT '字典名称',
  `dict_type` varchar(50) DEFAULT NULL COMMENT '字典类型',
  `status` int DEFAULT NULL COMMENT '状态 (0:禁用 1:正常)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  PRIMARY KEY (`dict_id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

-- 创建字典数据表
CREATE TABLE IF NOT EXISTS `sys_dict_data` (
  `dict_code` bigint NOT NULL COMMENT '字典主键',
  `dict_type` varchar(50) DEFAULT NULL COMMENT '字典类型',
  `dict_label` varchar(50) DEFAULT NULL COMMENT '字典标签',
  `dict_value` varchar(100) DEFAULT NULL COMMENT '字典值',
  `sort` int DEFAULT NULL COMMENT '排序',
  `status` int DEFAULT NULL COMMENT '状态 (0:禁用 1:正常)',
  `css_class` varchar(100) DEFAULT NULL COMMENT '样式属性',
  `list_class` varchar(100) DEFAULT NULL COMMENT '表格回显样式',
  `is_default` int DEFAULT NULL COMMENT '是否默认 (0:是 1:否)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  PRIMARY KEY (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典数据表';

-- 创建参数配置表
CREATE TABLE IF NOT EXISTS `sys_config` (
  `config_id` bigint NOT NULL COMMENT '参数主键',
  `config_name` varchar(50) DEFAULT NULL COMMENT '参数名称',
  `config_key` varchar(50) DEFAULT NULL COMMENT '参数键名',
  `config_value` varchar(255) DEFAULT NULL COMMENT '参数键值',
  `config_type` int DEFAULT NULL COMMENT '系统内置 (0:是 1:否)',
  `status` int DEFAULT NULL COMMENT '状态 (0:禁用 1:正常)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='参数配置表';

-- 创建操作日志表
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
  `id` bigint NOT NULL COMMENT '日志 ID',
  `module` varchar(50) DEFAULT NULL COMMENT '模块名称',
  `operation_type` varchar(50) DEFAULT NULL COMMENT '操作类型',
  `description` varchar(255) DEFAULT NULL COMMENT '操作描述',
  `request_method` varchar(20) DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求 URL',
  `request_params` text DEFAULT NULL COMMENT '请求参数',
  `response_result` text DEFAULT NULL COMMENT '响应结果',
  `operation_ip` varchar(50) DEFAULT NULL COMMENT '操作 IP',
  `operation_location` varchar(100) DEFAULT NULL COMMENT '操作地点',
  `browser` varchar(100) DEFAULT NULL COMMENT '浏览器类型',
  `os` varchar(100) DEFAULT NULL COMMENT '操作系统',
  `execute_time` bigint DEFAULT NULL COMMENT '执行时长（毫秒）',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人 ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人名称',
  `dept_id` bigint DEFAULT NULL COMMENT '部门 ID',
  `dept_name` varchar(50) DEFAULT NULL COMMENT '部门名称',
  `status` int DEFAULT NULL COMMENT '状态：0-失败 1-成功',
  `error_msg` text DEFAULT NULL COMMENT '错误消息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_module` (`module`),
  KEY `idx_operator` (`operator_name`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 插入默认管理员用户 (密码：admin123 使用 BCrypt 加密)
INSERT INTO `sys_user` (`user_id`, `username`, `password`, `nickname`, `status`, `create_time`)
VALUES (1, 'admin', '$2a$10$7JBWZbZfQ7K7ZqJ8X9X9X9X9X9X9X9X9X9X9X9X9X9X9X9X9X9X9X', '管理员', 1, NOW())
ON DUPLICATE KEY UPDATE `username`=`username`;

-- 插入默认角色
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_code`, `status`, `create_time`)
VALUES
(1, '超级管理员', 'admin', 1, NOW()),
(2, '普通角色', 'common', 1, NOW())
ON DUPLICATE KEY UPDATE `role_code`=`role_code`;

-- 插入默认菜单
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `status`, `sort`, `create_time`) VALUES
-- 系统管理目录
(1, 0, '系统管理', '/system', 'layout', '', 1, 'Setting', 1, 1, NOW()),
-- 用户管理
(2, 1, '用户管理', '/system/user', 'system/user/index', 'system:user:list', 2, 'User', 1, 1, NOW()),
(101, 2, '用户查询', '', '', 'system:user:query', 3, '', 1, 1, NOW()),
(102, 2, '用户新增', '', '', 'system:user:add', 3, '', 1, 2, NOW()),
(103, 2, '用户修改', '', '', 'system:user:edit', 3, '', 1, 3, NOW()),
(104, 2, '用户删除', '', '', 'system:user:remove', 3, '', 1, 4, NOW()),
(105, 2, '重置密码', '', '', 'system:user:resetPwd', 3, '', 1, 5, NOW()),
-- 角色管理
(3, 1, '角色管理', '/system/role', 'system/role/index', 'system:role:list', 2, 'Avatar', 1, 2, NOW()),
(201, 3, '角色查询', '', '', 'system:role:query', 3, '', 1, 1, NOW()),
(202, 3, '角色新增', '', '', 'system:role:add', 3, '', 1, 2, NOW()),
(203, 3, '角色修改', '', '', 'system:role:edit', 3, '', 1, 3, NOW()),
(204, 3, '角色删除', '', '', 'system:role:remove', 3, '', 1, 4, NOW()),
-- 菜单管理
(4, 1, '菜单管理', '/system/menu', 'system/menu/index', 'system:menu:list', 2, 'Menu', 1, 3, NOW()),
(301, 4, '菜单查询', '', '', 'system:menu:query', 3, '', 1, 1, NOW()),
(302, 4, '菜单新增', '', '', 'system:menu:add', 3, '', 1, 2, NOW()),
(303, 4, '菜单修改', '', '', 'system:menu:edit', 3, '', 1, 3, NOW()),
(304, 4, '菜单删除', '', '', 'system:menu:remove', 3, '', 1, 4, NOW()),
-- 部门管理
(5, 1, '部门管理', '/system/dept', 'system/dept/index', 'system:dept:list', 2, 'OfficeBuilding', 1, 4, NOW()),
(401, 5, '部门查询', '', '', 'system:dept:query', 3, '', 1, 1, NOW()),
(402, 5, '部门新增', '', '', 'system:dept:add', 3, '', 1, 2, NOW()),
(403, 5, '部门修改', '', '', 'system:dept:edit', 3, '', 1, 3, NOW()),
(404, 5, '部门删除', '', '', 'system:dept:remove', 3, '', 1, 4, NOW()),
-- 字典管理
(6, 1, '字典管理', '/system/dict', 'system/dict/index', 'system:dict:list', 2, 'Collection', 1, 5, NOW()),
(501, 6, '字典查询', '', '', 'system:dict:query', 3, '', 1, 1, NOW()),
(502, 6, '字典新增', '', '', 'system:dict:add', 3, '', 1, 2, NOW()),
(503, 6, '字典修改', '', '', 'system:dict:edit', 3, '', 1, 3, NOW()),
(504, 6, '字典删除', '', '', 'system:dict:remove', 3, '', 1, 4, NOW()),
-- 参数配置
(7, 1, '参数设置', '/system/config', 'system/config/index', 'system:config:list', 2, 'Tools', 1, 6, NOW()),
(601, 7, '参数查询', '', '', 'system:config:query', 3, '', 1, 1, NOW()),
(602, 7, '参数新增', '', '', 'system:config:add', 3, '', 1, 2, NOW()),
(603, 7, '参数修改', '', '', 'system:config:edit', 3, '', 1, 3, NOW()),
(604, 7, '参数删除', '', '', 'system:config:remove', 3, '', 1, 4, NOW()),
-- 日志管理
(8, 1, '日志管理', '/system/log', 'system/log/index', 'system:log:list', 2, 'Document', 1, 7, NOW()),
(701, 8, '日志查询', '', '', 'system:log:query', 3, '', 1, 1, NOW()),
(702, 8, '日志删除', '', '', 'system:log:remove', 3, '', 1, 2, NOW()),
(703, 8, '日志清空', '', '', 'system:log:clear', 3, '', 1, 3, NOW())
ON DUPLICATE KEY UPDATE `menu_name`=`menu_name`;

-- 关联管理员和超级管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1) ON DUPLICATE KEY UPDATE `user_id`=`user_id`;

-- 关联超级管理员角色和所有菜单
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, menu_id FROM sys_menu ON DUPLICATE KEY UPDATE `role_id`=`role_id`;

-- 插入字典类型
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_time`) VALUES
(1, '系统是否', 'sys_normal_disable', 1, NOW()),
(2, '用户性别', 'sys_user_sex', 1, NOW()),
(3, '系统开关', 'sys_on_off', 1, NOW())
ON DUPLICATE KEY UPDATE `dict_name`=`dict_name`;

-- 插入字典数据
INSERT INTO `sys_dict_data` (`dict_code`, `dict_type`, `dict_label`, `dict_value`, `sort`, `status`, `create_time`) VALUES
(1, 'sys_normal_disable', '正常', '1', 1, 1, NOW()),
(2, 'sys_normal_disable', '停用', '0', 2, 1, NOW()),
(3, 'sys_user_sex', '男', '1', 1, 1, NOW()),
(4, 'sys_user_sex', '女', '0', 2, 1, NOW()),
(5, 'sys_on_off', '开', 'true', 1, 1, NOW()),
(6, 'sys_on_off', '关', 'false', 2, 1, NOW())
ON DUPLICATE KEY UPDATE `dict_label`=`dict_label`;

-- 插入参数配置
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `status`, `create_time`) VALUES
(1, '主框架页 - 默认皮肤样式', 'skin.index.style', 'skin-blue', 1, 1, NOW()),
(2, '用户管理 - 账号初始密码', 'user.password.init', '123456', 1, 1, NOW()),
(3, '主框架页 - 侧边栏主题', 'skin.index.theme', 'theme-dark', 1, 1, NOW())
ON DUPLICATE KEY UPDATE `config_name`=`config_name`;
-- 创建岗位表
CREATE TABLE IF NOT EXISTS `sys_post` (
  `post_id` bigint NOT NULL COMMENT '岗位 ID',
  `post_code` varchar(50) DEFAULT NULL COMMENT '岗位编码',
  `post_name` varchar(50) DEFAULT NULL COMMENT '岗位名称',
  `post_sort` int DEFAULT NULL COMMENT '岗位排序',
  `status` int DEFAULT NULL COMMENT '状态 (0:禁用 1:正常)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  PRIMARY KEY (`post_id`),
  UNIQUE KEY `uk_post_code` (`post_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统岗位表';

-- 插入岗位数据
INSERT INTO `sys_post` (`post_id`, `post_code`, `post_name`, `post_sort`, `status`, `create_time`) VALUES
(1, 'ceo', '董事长', 1, 1, NOW()),
(2, 'manager', '总经理', 2, 1, NOW()),
(3, 'hr', '人力资源', 3, 1, NOW()),
(4, 'tech', '技术岗位', 4, 1, NOW()),
(5, 'finance', '财务岗位', 5, 1, NOW()),
(6, 'admin', '行政岗位', 6, 1, NOW())
ON DUPLICATE KEY UPDATE `post_code`=`post_code`;

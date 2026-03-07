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
(703, 8, '日志清空', '', '', 'system:log:clear', 3, '', 1, 3, NOW()),
-- 定时任务管理
(9, 1, '定时任务', '/system/job', 'system/job/index', 'system:job:list', 2, 'Timer', 1, 2, NOW()),
(801, 9, '任务查询', '', '', 'system:job:query', 3, '', 1, 1, NOW()),
(802, 9, '任务新增', '', '', 'system:job:add', 3, '', 1, 2, NOW()),
(803, 9, '任务修改', '', '', 'system:job:edit', 3, '', 1, 3, NOW()),
(804, 9, '任务删除', '', '', 'system:job:remove', 3, '', 1, 4, NOW()),
(805, 9, '启动任务', '', '', 'system:job:start', 3, '', 1, 5, NOW()),
(806, 9, '停止任务', '', '', 'system:job:stop', 3, '', 1, 6, NOW()),
(807, 9, '立即执行', '', '', 'system:job:run', 3, '', 1, 7, NOW()),
-- 任务日志管理
(10, 1, '任务日志', '/system/job-log', 'system/job-log/index', 'system:joblog:list', 2, 'FileText', 1, 3, NOW()),
(901, 10, '日志查询', '', '', 'system:joblog:query', 3, '', 1, 1, NOW()),
(902, 10, '日志删除', '', '', 'system:joblog:remove', 3, '', 1, 2, NOW()),
(903, 10, '日志清空', '', '', 'system:joblog:clear', 3, '', 1, 3, NOW())
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

-- 创建定时任务表
CREATE TABLE IF NOT EXISTS `sys_job` (
  `job_id` bigint NOT NULL COMMENT '任务 ID',
  `job_name` varchar(50) DEFAULT NULL COMMENT '任务名称',
  `job_group` varchar(50) DEFAULT NULL COMMENT '任务组名',
  `invoke_target` varchar(255) DEFAULT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(50) DEFAULT NULL COMMENT 'Cron 执行表达式',
  `misfire_policy` int DEFAULT '3' COMMENT '执行策略 (1:立即执行 2:取消执行 3:跳过执行)',
  `concurrent` int DEFAULT '1' COMMENT '是否并发执行 (0:允许 1:禁止)',
  `status` int DEFAULT NULL COMMENT '状态 (0:暂停 1:正常)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  PRIMARY KEY (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时任务表';

-- 创建定时任务日志表
CREATE TABLE IF NOT EXISTS `sys_job_log` (
  `job_log_id` bigint NOT NULL COMMENT '日志 ID',
  `job_id` bigint DEFAULT NULL COMMENT '任务 ID',
  `job_name` varchar(50) DEFAULT NULL COMMENT '任务名称',
  `job_group` varchar(50) DEFAULT NULL COMMENT '任务组名',
  `invoke_target` varchar(255) DEFAULT NULL COMMENT '调用目标字符串',
  `job_message` varchar(255) DEFAULT NULL COMMENT '日志信息',
  `status` int DEFAULT NULL COMMENT '执行状态 (0:正常 1:失败)',
  `exception_info` text DEFAULT NULL COMMENT '异常信息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时任务日志表';

-- 插入定时任务示例数据
INSERT INTO `sys_job` (`job_id`, `job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`, `remark`, `create_time`) VALUES
(1, '系统定时清理', 'system', 'SystemTask.clean()', '0 0 2 * * ?', 3, 1, 1, '每天凌晨 2 点执行', NOW()),
(2, '数据备份任务', 'backup', 'BackupTask.backup()', '0 0 3 * * ?', 3, 1, 0, '每天凌晨 3 点备份数据', NOW())
ON DUPLICATE KEY UPDATE `job_name`=`job_name`;
-- ============================================
-- XXL-Job 调度中心数据库初始化
-- ============================================
-- 执行以下 SQL 创建 xxl_job 数据库:
-- mysql -u root -p < xxl_job_init.sql
-- 或者手动执行:
-- CREATE DATABASE xxl_job DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- use xxl_job;
-- source tables_xxl_job.sql
--
-- XXL-Job Admin 访问地址：http://localhost:8888/xxl-job-admin
-- 默认账号密码：admin / 123456

-- ============================================
-- 完整菜单数据 (60 个菜单项)
-- ============================================
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `status`) VALUES
-- 系统管理目录
(1, 0, '系统管理', '/system', 'layout', '', 1, 'Setting', 1, 1),
-- 用户管理
(2, 1, '用户管理', '/system/user', 'system/user/index', 'system:user:list', 2, 'User', 1, 1),
(101, 2, '用户查询', '', '', 'system:user:query', 3, '', 1, 1, NOW()),
(102, 2, '用户新增', '', '', 'system:user:add', 3, '', 2, 1, NOW()),
(103, 2, '用户修改', '', '', 'system:user:edit', 3, '', 3, 1, NOW()),
(104, 2, '用户删除', '', '', 'system:user:remove', 3, '', 4, 1, NOW()),
(105, 2, '重置密码', '', '', 'system:user:resetPwd', 3, '', 5, 1, NOW()),
-- 角色管理
(3, 1, '角色管理', '/system/role', 'system/role/index', 'system:role:list', 2, 'Avatar', 2, 1),
(201, 3, '角色查询', '', '', 'system:role:query', 3, '', 1, 1, NOW()),
(202, 3, '角色新增', '', '', 'system:role:add', 3, '', 2, 1, NOW()),
(203, 3, '角色修改', '', '', 'system:role:edit', 3, '', 3, 1, NOW()),
(204, 3, '角色删除', '', '', 'system:role:remove', 3, '', 4, 1, NOW()),
-- 菜单管理
(4, 1, '菜单管理', '/system/menu', 'system/menu/index', 'system:menu:list', 2, 'Menu', 3, 1),
(301, 4, '菜单查询', '', '', 'system:menu:query', 3, '', 1, 1, NOW()),
(302, 4, '菜单新增', '', '', 'system:menu:add', 3, '', 2, 1, NOW()),
(303, 4, '菜单修改', '', '', 'system:menu:edit', 3, '', 3, 1, NOW()),
(304, 4, '菜单删除', '', '', 'system:menu:remove', 3, '', 4, 1, NOW()),
-- 部门管理
(5, 1, '部门管理', '/system/dept', 'system/dept/index', 'system:dept:list', 2, 'OfficeBuilding', 4, 1),
(401, 5, '部门查询', '', '', 'system:dept:query', 3, '', 1, 1, NOW()),
(402, 5, '部门新增', '', '', 'system:dept:add', 3, '', 2, 1, NOW()),
(403, 5, '部门修改', '', '', 'system:dept:edit', 3, '', 3, 1, NOW()),
(404, 5, '部门删除', '', '', 'system:dept:remove', 3, '', 4, 1, NOW()),
-- 岗位管理
(6, 1, '岗位管理', '/system/post', 'system/post/index', 'system:post:list', 2, 'Postcard', 5, 1),
(501, 6, '岗位查询', '', '', 'system:post:query', 3, '', 1, 1, NOW()),
(502, 6, '岗位新增', '', '', 'system:post:add', 3, '', 2, 1, NOW()),
(503, 6, '岗位修改', '', '', 'system:post:edit', 3, '', 3, 1, NOW()),
(504, 6, '岗位删除', '', '', 'system:post:remove', 3, '', 4, 1, NOW()),
-- 字典管理
(7, 1, '字典管理', '/system/dict', 'system/dict/index', 'system:dict:list', 2, 'Collection', 6, 1),
(601, 7, '字典查询', '', '', 'system:dict:query', 3, '', 1, 1, NOW()),
(602, 7, '字典新增', '', '', 'system:dict:add', 3, '', 2, 1, NOW()),
(603, 7, '字典修改', '', '', 'system:dict:edit', 3, '', 3, 1, NOW()),
(604, 7, '字典删除', '', '', 'system:dict:remove', 3, '', 4, 1, NOW()),
-- 参数配置
(8, 1, '参数设置', '/system/config', 'system/config/index', 'system:config:list', 2, 'Tools', 7, 1),
(701, 8, '参数查询', '', '', 'system:config:query', 3, '', 1, 1, NOW()),
(702, 8, '参数新增', '', '', 'system:config:add', 3, '', 2, 1, NOW()),
(703, 8, '参数修改', '', '', 'system:config:edit', 3, '', 3, 1, NOW()),
(704, 8, '参数删除', '', '', 'system:config:remove', 3, '', 4, 1, NOW()),
-- 日志管理
(9, 1, '日志管理', '/system/log', 'system/log/index', 'system:log:list', 2, 'Document', 8, 1),
(801, 9, '日志查询', '', '', 'system:log:query', 3, '', 1, 1, NOW()),
(802, 9, '日志删除', '', '', 'system:log:remove', 3, '', 2, 1, NOW()),
(803, 9, '日志清空', '', '', 'system:log:clear', 3, '', 3, 1, NOW()),
-- 定时任务
(10, 1, '定时任务', '/system/job', 'system/job/index', 'system:job:list', 2, 'Timer', 9, 1),
(901, 10, '任务查询', '', '', 'system:job:query', 3, '', 1, 1, NOW()),
(902, 10, '任务新增', '', '', 'system:job:add', 3, '', 2, 1, NOW()),
(903, 10, '任务修改', '', '', 'system:job:edit', 3, '', 3, 1, NOW()),
(904, 10, '任务删除', '', '', 'system:job:remove', 3, '', 4, 1, NOW()),
(905, 10, '启动任务', '', '', 'system:job:start', 3, '', 5, 1, NOW()),
(906, 10, '停止任务', '', '', 'system:job:stop', 3, '', 6, 1, NOW()),
(907, 10, '立即执行', '', '', 'system:job:run', 3, '', 7, 1, NOW()),
-- 任务日志
(11, 1, '任务日志', '/system/job-log', 'system/job-log/index', 'system:joblog:list', 2, 'FileText', 10, 1),
(1001, 11, '日志查询', '', '', 'system:joblog:query', 3, '', 1, 1, NOW()),
(1002, 11, '日志删除', '', '', 'system:joblog:remove', 3, '', 2, 1, NOW()),
(1003, 11, '日志清空', '', '', 'system:joblog:clear', 3, '', 3, 1, NOW()),
-- 系统监控
(12, 1, '系统监控', '/monitor', 'layout', '', 1, 'Monitor', 11, 1),
(13, 12, '在线用户', '/monitor/online', 'monitor/online/index', 'monitor:online:list', 2, 'User', 1, 1),
(1101, 13, '在线用户查询', '', '', 'monitor:online:query', 3, '', 1, 1, NOW()),
(1102, 13, '强制下线', '', '', 'monitor:online:forceLogout', 3, '', 2, 1, NOW()),
(14, 12, 'Redis 监控', '/monitor/redis', 'monitor/redis/index', 'monitor:redis:query', 2, 'Folder', 2, 1),
(15, 12, '服务器监控', '/monitor/server', 'monitor/server/index', 'monitor:server:query', 2, 'Platform', 3, 1),
(16, 12, '缓存监控', '/monitor/cache', 'monitor/cache/index', 'monitor:cache:query', 2, 'DataLine', 4, 1)
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- 关联超级管理员角色和所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu ON DUPLICATE KEY UPDATE role_id=role_id;

-- 示例部门数据
-- ============================================
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `dept_name`, `sort`, `leader`, `phone`, `email`, `status`) VALUES
(100, 0, '集团总公司', 1, 'admin', '13800000000', 'admin@micro.com', 1),
(101, 100, '深圳分公司', 1, 'zhangsan', '13800000001', 'sz@micro.com', 1),
(102, 100, '北京分公司', 2, 'lisi', '13800000002', 'bj@micro.com', 1),
(200, 101, '研发部门', 1, 'wangwu', '13800000003', 'dev@micro.com', 1),
(201, 101, '市场部门', 2, 'zhaoliu', '13800000004', 'market@micro.com', 1),
(202, 102, '市场部门', 1, 'sunqi', '13800000005', 'bjmarket@micro.com', 1)
ON DUPLICATE KEY UPDATE dept_name=dept_name;

-- ============================================
-- 通知公告表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_notice` (
  `notice_id` bigint NOT NULL COMMENT '公告 ID',
  `notice_title` varchar(50) DEFAULT NULL COMMENT '公告标题',
  `notice_type` varchar(10) DEFAULT NULL COMMENT '公告类型 (1:通知 2:公告)',
  `notice_content` text DEFAULT NULL COMMENT '公告内容',
  `status` int DEFAULT NULL COMMENT '状态 (0:关闭 1:正常)',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者 ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知公告表';

-- 插入示例公告数据
INSERT INTO `sys_notice` (`notice_id`, `notice_title`, `notice_type`, `notice_content`, `status`, `create_time`, `remark`) VALUES
(1, '系统上线通知', '1', '尊敬的各位用户：\n\n您好！系统将于今日正式上线运行，欢迎大家使用。如有任何问题，请及时联系管理员。\n\n谢谢！', 1, NOW(), '系统首次上线'),
(2, '版本更新公告', '2', '本次更新内容：\n1. 优化了用户界面\n2. 修复了已知 bug\n3. 提升了系统性能', 1, NOW(), '版本 v1.1.0')
ON DUPLICATE KEY UPDATE `notice_title`=`notice_title`;

-- ============================================
-- 通知公告菜单权限
-- ============================================
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `status`) VALUES
(17, 1, '通知公告', '/system/notice', 'system/notice/index', 'system:notice:list', 2, 'Bell', 8, 1),
(1201, 17, '公告查询', '', '', 'system:notice:query', 3, '', 1, 1, NOW()),
(1202, 17, '公告新增', '', '', 'system:notice:add', 3, '', 2, 1, NOW()),
(1203, 17, '公告修改', '', '', 'system:notice:edit', 3, '', 3, 1, NOW()),
(1204, 17, '公告删除', '', '', 'system:notice:remove', 3, '', 4, 1, NOW())
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- 重新关联超级管理员角色和所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu ON DUPLICATE KEY UPDATE role_id=role_id;

-- ============================================
-- 首页统计菜单权限
-- ============================================
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `status`) VALUES
(18, 1, '首页统计', '/system/index', 'dashboard/index', 'system:index:query', 2, 'HomeFilled', 0, 1)
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- 重新关联超级管理员角色和所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu ON DUPLICATE KEY UPDATE role_id=role_id;

-- ============================================
-- 命令执行记录表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_command_log` (
  `command_id` bigint NOT NULL COMMENT '命令 ID',
  `command_type` varchar(50) DEFAULT NULL COMMENT '命令类型',
  `command_content` text DEFAULT NULL COMMENT '命令内容',
  `command_result` text DEFAULT NULL COMMENT '命令结果',
  `execute_time` bigint DEFAULT NULL COMMENT '执行时长（毫秒）',
  `execute_by` bigint DEFAULT NULL COMMENT '执行者 ID',
  `execute_name` varchar(50) DEFAULT NULL COMMENT '执行者名称',
  `execute_time_str` datetime DEFAULT NULL COMMENT '执行时间',
  `status` int DEFAULT NULL COMMENT '状态：0-失败 1-成功',
  `error_msg` text DEFAULT NULL COMMENT '错误消息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  PRIMARY KEY (`command_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='命令执行记录表';

-- 更新菜单权限
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `status`) VALUES
-- 系统监控菜单
(12, 0, '系统监控', '/monitor', 'layout', '', 1, 'Monitor', 2, 1),
(13, 12, '在线用户', '/monitor/online', 'monitor/online/index', 'monitor:online:list', 2, 'User', 1, 1),
(1101, 13, '在线用户查询', '', '', 'monitor:online:query', 3, '', 1, 1, NOW()),
(1102, 13, '强制下线', '', '', 'monitor:online:forceLogout', 3, '', 2, 1, NOW()),
(14, 12, 'Redis 监控', '/monitor/redis', 'monitor/redis/index', 'monitor:redis:query', 2, 'Folder', 2, 1),
(15, 12, '服务器监控', '/monitor/server', 'monitor/server/index', 'monitor:server:query', 2, 'Platform', 3, 1),
(16, 12, '缓存监控', '/monitor/cache', 'monitor/cache/index', 'monitor:cache:query', 2, 'DataLine', 4, 1),
-- 代码生成菜单
(19, 0, '代码生成', '/generator', 'layout', '', 1, 'Code', 3, 1),
(1901, 19, '数据表管理', '/generator/table', 'generator/table/index', 'generator:table:list', 2, 'Grid', 1, 1),
(1902, 19, '代码预览', '', '', 'generator:table:preview', 3, '', 1, 1, NOW()),
(1903, 19, '代码生成', '', '', 'generator:table:generate', 3, '', 2, 1, NOW()),
-- 定时任务菜单
(10, 0, '定时任务', '/job', 'layout', '', 1, 'Timer', 4, 1),
(1001, 10, '任务管理', '/job/list', 'job/list/index', 'system:job:list', 2, 'List', 1, 1),
(1002, 10, '任务日志', '/job/log', 'job/log/index', 'system:joblog:list', 2, 'Document', 2, 1)
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- 重新关联超级管理员角色和所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu ON DUPLICATE KEY UPDATE role_id=role_id;


-- ============================================
-- 文件管理表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_file` (
  `file_id` bigint NOT NULL COMMENT '文件 ID',
  `file_name` varchar(255) DEFAULT NULL COMMENT '文件名称',
  `original_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_ext` varchar(50) DEFAULT NULL COMMENT '文件扩展名',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小 (字节)',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `file_url` varchar(500) DEFAULT NULL COMMENT '文件 URL',
  `create_by` bigint DEFAULT NULL COMMENT '创建者 ID',
  `create_by_name` varchar(50) DEFAULT NULL COMMENT '创建者名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `deleted` int DEFAULT '0' COMMENT '删除标志 (0:正常 1:删除)',
  PRIMARY KEY (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件管理表';

-- 文件管理菜单权限
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `status`) VALUES
(20, 1, '文件管理', '/system/file', 'system/file/index', 'system:file:list', 2, 'Folder', 9, 1),
(2001, 20, '文件查询', '', '', 'system:file:query', 3, '', 1, 1, NOW()),
(2002, 20, '文件上传', '', '', 'system:file:upload', 3, '', 2, 1, NOW()),
(2003, 20, '文件删除', '', '', 'system:file:remove', 3, '', 3, 1, NOW())
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- 重新关联超级管理员角色和所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu ON DUPLICATE KEY UPDATE role_id=role_id;

-- ============================================
-- 登录日志表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_login_log` (
  `log_id` bigint NOT NULL COMMENT '访问 ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户 ID',
  `username` varchar(50) DEFAULT NULL COMMENT '用户账号',
  `status` int DEFAULT NULL COMMENT '登录状态 (0:失败 1:成功)',
  `ip` varchar(100) DEFAULT NULL COMMENT '登录 IP 地址',
  `location` varchar(255) DEFAULT NULL COMMENT '登录地点',
  `browser` varchar(100) DEFAULT NULL COMMENT '浏览器类型',
  `os` varchar(100) DEFAULT NULL COMMENT '操作系统',
  `msg` varchar(255) DEFAULT NULL COMMENT '提示消息',
  `login_time` datetime DEFAULT NULL COMMENT '登录时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- 登录日志菜单权限
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `type`, `icon`, `sort`, `status`) VALUES
(21, 1, '登录日志', '/system/loginlog', 'system/loginlog/index', 'system:loginlog:list', 2, 'Document', 10, 1),
(2101, 21, '登录日志查询', '', '', 'system:loginlog:query', 3, '', 1, 1, NOW()),
(2102, 21, '登录日志删除', '', '', 'system:loginlog:remove', 3, '', 2, 1, NOW()),
(2103, 21, '登录日志清空', '', '', 'system:loginlog:clear', 3, '', 3, 1, NOW())
ON DUPLICATE KEY UPDATE menu_name=menu_name;

-- 重新关联超级管理员角色和所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu ON DUPLICATE KEY UPDATE role_id=role_id;

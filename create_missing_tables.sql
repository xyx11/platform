-- 创建缺失的 sys_login_log 和 sys_file 表
-- 执行时间：2026-03-08

-- 创建登录日志表
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

-- 创建文件管理表
CREATE TABLE IF NOT EXISTS `sys_file` (
  `file_id` bigint NOT NULL COMMENT '文件 ID',
  `file_name` varchar(100) DEFAULT NULL COMMENT '文件名',
  `original_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_ext` varchar(50) DEFAULT NULL COMMENT '文件扩展名',
  `file_path` varchar(500) DEFAULT NULL COMMENT '文件路径',
  `file_url` varchar(500) DEFAULT NULL COMMENT '文件 URL',
  `create_by` bigint DEFAULT NULL COMMENT '上传用户 ID',
  `create_by_name` varchar(50) DEFAULT NULL COMMENT '上传用户名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `file_type` varchar(100) DEFAULT NULL COMMENT '文件类型 (MIME)',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小 (字节)',
  `upload_user` bigint DEFAULT NULL COMMENT '上传用户 ID',
  `upload_time` datetime DEFAULT NULL COMMENT '上传时间',
  `status` int DEFAULT '1' COMMENT '状态 (0:禁用 1:正常)',
  `deleted` int DEFAULT '0' COMMENT '逻辑删除标记',
  PRIMARY KEY (`file_id`),
  KEY `idx_upload_user` (`upload_user`),
  KEY `idx_upload_time` (`upload_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件管理表';

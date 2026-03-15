-- ----------------------------
-- Table structure for form_definition
-- ----------------------------
DROP TABLE IF EXISTS `form_definition`;
CREATE TABLE `form_definition` (
  `form_id` bigint NOT NULL AUTO_INCREMENT COMMENT '表单 ID',
  `form_name` varchar(255) NOT NULL COMMENT '表单名称',
  `form_code` varchar(64) NOT NULL COMMENT '表单标识（唯一编码）',
  `form_type` tinyint NOT NULL DEFAULT '1' COMMENT '表单类型：1-普通表单，2-动态表单，3-外部表单',
  `content` text COMMENT '表单内容（HTML 或 JSON）',
  `form_schema` text COMMENT '表单 schema（JSON）',
  `form_config` text COMMENT '表单配置（JSON）',
  `external_url` varchar(512) DEFAULT NULL COMMENT '外部表单 URL',
  `version` varchar(32) DEFAULT '1.0' COMMENT '版本号',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志：0-正常，1-删除',
  PRIMARY KEY (`form_id`),
  UNIQUE KEY `uk_form_code` (`form_code`),
  KEY `idx_form_name` (`form_name`),
  KEY `idx_status` (`status`),
  KEY `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='表单定义表';

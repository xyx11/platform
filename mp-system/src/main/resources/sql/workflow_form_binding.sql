-- ----------------------------
-- Table structure for workflow_form_binding
-- ----------------------------
DROP TABLE IF EXISTS `workflow_form_binding`;
CREATE TABLE `workflow_form_binding` (
  `binding_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `process_definition_key` varchar(64) NOT NULL COMMENT '流程定义 Key',
  `process_definition_name` varchar(255) DEFAULT NULL COMMENT '流程定义名称',
  `task_definition_key` varchar(64) DEFAULT NULL COMMENT '任务定义 Key（启动表单为空）',
  `task_name` varchar(255) DEFAULT NULL COMMENT '任务名称',
  `form_key` varchar(64) NOT NULL COMMENT '表单 Key',
  `form_name` varchar(255) DEFAULT NULL COMMENT '表单名称',
  `form_type` tinyint NOT NULL DEFAULT '1' COMMENT '表单类型：1-启动表单，2-办理表单',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-停用，1-启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `form_schema` text COMMENT '表单 Schema（JSON）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '逻辑删除标志：0-正常，1-删除',
  PRIMARY KEY (`binding_id`),
  KEY `idx_process_key` (`process_definition_key`),
  KEY `idx_task_key` (`task_definition_key`),
  KEY `idx_form_key` (`form_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工作流表单绑定表';
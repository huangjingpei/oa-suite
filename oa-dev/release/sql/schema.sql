-- ==============================================================================
-- 现代企业协同办公系统 (OA Suite Modernization) 数据库 DDL 脚本
-- 适用数据库: MySQL 8.0+
-- 字符集: utf8mb4 / 排序规则: utf8mb4_0900_ai_ci
-- 顶级包名: com.graddu.oa
-- 规范版本: M1-20261027
-- ==============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------------------------
-- 1. 系统与组织架构模块 (oa_sys_*)
-- ------------------------------------------------------------------------------

-- 1.1 部门表
DROP TABLE IF EXISTS `oa_sys_dept`;
CREATE TABLE `oa_sys_dept` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '部门ID',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父部门ID (0为根部门)',
  `dept_code` VARCHAR(64) NOT NULL COMMENT '部门编码',
  `dept_name` VARCHAR(100) NOT NULL COMMENT '部门名称',
  `leader` VARCHAR(64) DEFAULT NULL COMMENT '部门负责人姓名/工号',
  `phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
  `email` VARCHAR(64) DEFAULT NULL COMMENT '部门邮箱',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (1正常, 0停用)',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人工号/用户名',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 (0正常, 1已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统部门组织架构表';

-- 1.2 用户表
DROP TABLE IF EXISTS `oa_sys_user`;
CREATE TABLE `oa_sys_user` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `username` VARCHAR(64) NOT NULL COMMENT '登录账号',
  `real_name` VARCHAR(64) NOT NULL COMMENT '真实姓名',
  `password` VARCHAR(128) NOT NULL COMMENT '密码哈希 (BCrypt/Sa-Token加密)',
  `salt` VARCHAR(64) DEFAULT NULL COMMENT '密码盐',
  `work_no` VARCHAR(64) DEFAULT NULL COMMENT '工号',
  `dept_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '所属部门ID',
  `post_name` VARCHAR(64) DEFAULT NULL COMMENT '岗位职务',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '电子邮箱',
  `phone` VARCHAR(32) DEFAULT NULL COMMENT '手机号码',
  `gender` TINYINT DEFAULT 1 COMMENT '性别 (1男, 2女, 0未知)',
  `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (1正常, 0冻结/停用)',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 (0正常, 1已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户/员工表';

-- 1.3 角色表
DROP TABLE IF EXISTS `oa_sys_role`;
CREATE TABLE `oa_sys_role` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `role_code` VARCHAR(64) NOT NULL COMMENT '角色标识 (如 ROLE_ADMIN, ROLE_HR)',
  `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (1正常, 0停用)',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 (0正常, 1已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色表';

-- 1.4 用户角色关联表
DROP TABLE IF EXISTS `oa_sys_user_role`;
CREATE TABLE `oa_sys_user_role` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户与角色关联表';

-- 1.5 系统菜单与操作权限表
DROP TABLE IF EXISTS `oa_sys_permission`;
CREATE TABLE `oa_sys_permission` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父权限ID (0为根目录)',
  `name` VARCHAR(64) NOT NULL COMMENT '权限/菜单名称',
  `perms` VARCHAR(100) DEFAULT NULL COMMENT '权限标识编码 (如 hr:leave:apply, sys:user:add)',
  `menu_type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型 (1:目录, 2:菜单, 3:按钮操作)',
  `icon` VARCHAR(64) DEFAULT NULL COMMENT '图标样式',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '显示排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (1正常, 0停用)',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 (0正常, 1已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统菜单与操作权限表';

-- 1.6 角色与权限关联表
DROP TABLE IF EXISTS `oa_sys_role_permission`;
CREATE TABLE `oa_sys_role_permission` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色与权限关联表';

-- 1.5 字典类型表
DROP TABLE IF EXISTS `oa_sys_dict_type`;
CREATE TABLE `oa_sys_dict_type` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '字典类型ID',
  `dict_name` VARCHAR(100) NOT NULL COMMENT '字典名称',
  `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型编码',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (1正常, 0停用)',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

-- 1.6 字典数据项表
DROP TABLE IF EXISTS `oa_sys_dict_data`;
CREATE TABLE `oa_sys_dict_data` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '字典项ID',
  `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型编码',
  `dict_label` VARCHAR(100) NOT NULL COMMENT '字典标签 (展示名称)',
  `dict_value` VARCHAR(100) NOT NULL COMMENT '字典键值',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (1正常, 0停用)',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据项表';

-- ------------------------------------------------------------------------------
-- 2. 轻量自研工作流引擎模块 (oa_wf_*)
-- 替代原系统前端直接读写 pr_flow, pr_log, pr_log_history, bs_free_process
-- ------------------------------------------------------------------------------

-- 2.1 流程定义表
DROP TABLE IF EXISTS `oa_wf_definition`;
CREATE TABLE `oa_wf_definition` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '流程定义ID',
  `proc_key` VARCHAR(64) NOT NULL COMMENT '流程标识Key (如 PROC_LEAVE, PROC_SEAL)',
  `proc_name` VARCHAR(100) NOT NULL COMMENT '流程名称',
  `business_table` VARCHAR(64) NOT NULL COMMENT '绑定的业务单据表名',
  `proc_mode` VARCHAR(32) NOT NULL DEFAULT 'FREE' COMMENT '流程模式 (FREE自由流程, FIXED预设流程)',
  `route_nodes_json` JSON DEFAULT NULL COMMENT '预设审批节点定义JSON (用于固定流程)',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (1启用, 0停用)',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_proc_key_version` (`proc_key`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作流流程定义表';

-- 2.2 流程运行实例表
DROP TABLE IF EXISTS `oa_wf_instance`;
CREATE TABLE `oa_wf_instance` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '流程实例ID',
  `proc_def_id` BIGINT UNSIGNED NOT NULL COMMENT '流程定义ID',
  `proc_key` VARCHAR(64) NOT NULL COMMENT '流程标识Key',
  `business_table` VARCHAR(64) NOT NULL COMMENT '关联业务表',
  `business_id` BIGINT UNSIGNED NOT NULL COMMENT '关联业务单据ID',
  `title` VARCHAR(255) NOT NULL COMMENT '流程实例标题/摘要',
  `initiator_username` VARCHAR(64) NOT NULL COMMENT '发起人工号/账号',
  `initiator_name` VARCHAR(64) NOT NULL COMMENT '发起人真实姓名',
  `dept_name` VARCHAR(100) DEFAULT NULL COMMENT '发起人部门',
  `current_node_name` VARCHAR(100) DEFAULT NULL COMMENT '当前节点名称',
  `current_assignees` VARCHAR(500) DEFAULT NULL COMMENT '当前待办审批人用户名列表 (逗号分隔)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (0草稿, 1审批中, 2已完成/同意, 3已驳回, 4已作废/撤销)',
  `start_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发起时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_business` (`business_table`, `business_id`),
  KEY `idx_initiator` (`initiator_username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作流运行实例表';

-- 2.3 流程待办任务表 (替代 pr_log)
DROP TABLE IF EXISTS `oa_wf_task`;
CREATE TABLE `oa_wf_task` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
  `inst_id` BIGINT UNSIGNED NOT NULL COMMENT '流程实例ID',
  `node_key` VARCHAR(64) NOT NULL COMMENT '节点编码',
  `node_name` VARCHAR(100) NOT NULL COMMENT '节点名称 (如 部门经理审批)',
  `node_seq` INT NOT NULL DEFAULT 1 COMMENT '审批节点序号',
  `assignee_username` VARCHAR(64) NOT NULL COMMENT '任务指派审批人账号',
  `assignee_name` VARCHAR(64) DEFAULT NULL COMMENT '指派审批人真实姓名',
  `task_type` VARCHAR(32) NOT NULL DEFAULT 'APPROVE' COMMENT '任务类型 (APPROVE普通审批, SIGN加签, COUNTERSIGN会签, NOTIFY知会)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '任务状态 (1待处理, 2已完成, 3已转办/转历史)',
  `claim_time` DATETIME DEFAULT NULL COMMENT '签收时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '任务生成时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_inst_id` (`inst_id`),
  KEY `idx_assignee_status` (`assignee_username`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作流待办任务表';

-- 2.4 流程审批履历/历史日志表 (替代 pr_log_history)
DROP TABLE IF EXISTS `oa_wf_task_history`;
CREATE TABLE `oa_wf_task_history` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '履历ID',
  `inst_id` BIGINT UNSIGNED NOT NULL COMMENT '流程实例ID',
  `task_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联的任务ID',
  `node_name` VARCHAR(100) NOT NULL COMMENT '节点名称',
  `node_seq` INT NOT NULL DEFAULT 1 COMMENT '节点序号',
  `operator_username` VARCHAR(64) NOT NULL COMMENT '操作人账号',
  `operator_name` VARCHAR(64) NOT NULL COMMENT '操作人姓名',
  `action` VARCHAR(32) NOT NULL COMMENT '操作动作 (SUBMIT提交, AGREE同意, REJECT驳回, ADDSIGN加签, NOTIFY知会, CANCEL撤回)',
  `opinion` TEXT DEFAULT NULL COMMENT '审批意见/处理说明',
  `target_assignee` VARCHAR(255) DEFAULT NULL COMMENT '转办/加签目标人 (若有)',
  `files` VARCHAR(1000) DEFAULT NULL COMMENT '审批过程附件JSON/URL',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_inst_id` (`inst_id`),
  KEY `idx_operator` (`operator_username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作流审批履历历史表';

-- 2.5 自由流程动态流转路由表 (替代 bs_free_process)
DROP TABLE IF EXISTS `oa_wf_free_route`;
CREATE TABLE `oa_wf_free_route` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `inst_id` BIGINT UNSIGNED NOT NULL COMMENT '流程实例ID',
  `audit_chain_json` JSON NOT NULL COMMENT '完整动态审批人链条JSON [{seq:1, user:"admin"}, ...]',
  `notify_chain_json` JSON DEFAULT NULL COMMENT '知会人列表JSON',
  `add_sign_json` JSON DEFAULT NULL COMMENT '动态加签记录JSON',
  `current_seq` INT NOT NULL DEFAULT 1 COMMENT '当前流转到的序号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inst_id` (`inst_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作流自由流转路由状态表';

-- ------------------------------------------------------------------------------
-- 3. 人事与考勤业务模块 (oa_hr_*)
-- 规范化原 bs_leave, bs_attendance*, bs_overtime, bs_egress
-- ------------------------------------------------------------------------------

-- 3.1 请假申请表 (替代 bs_leave)
DROP TABLE IF EXISTS `oa_hr_leave`;
CREATE TABLE `oa_hr_leave` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '请假单ID',
  `leave_no` VARCHAR(64) NOT NULL COMMENT '请假流水单号 (如 QJ202610270001)',
  `applicant_username` VARCHAR(64) NOT NULL COMMENT '申请人账号',
  `applicant_name` VARCHAR(64) NOT NULL COMMENT '申请人姓名',
  `dept_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '申请部门ID',
  `dept_name` VARCHAR(100) DEFAULT NULL COMMENT '部门名称',
  `leave_type` VARCHAR(32) NOT NULL COMMENT '请假类型字典 (ANNUAL年假, SICK病假, PERSONAL事假, MARRIAGE婚假, MATERNITY产假)',
  `start_time` DATETIME NOT NULL COMMENT '请假起始时间',
  `end_time` DATETIME NOT NULL COMMENT '请假结束时间',
  `total_hours` DECIMAL(5,1) NOT NULL DEFAULT '0.0' COMMENT '请假总时长(小时)',
  `total_days` DECIMAL(5,2) NOT NULL DEFAULT '0.0' COMMENT '请假总天数(折算)',
  `reason` VARCHAR(1000) NOT NULL COMMENT '请假原因说明',
  `files` VARCHAR(1000) DEFAULT NULL COMMENT '证明材料/附件文件URL',
  `proc_inst_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联流程实例ID',
  `proc_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态 (0草稿, 1审核中, 2已通过, 3已驳回, 4已作废)',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_leave_no` (`leave_no`),
  KEY `idx_applicant` (`applicant_username`),
  KEY `idx_proc_inst` (`proc_inst_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='人事请假申请单表';

-- 3.2 加班申请表 (替代 bs_overtime)
DROP TABLE IF EXISTS `oa_hr_overtime`;
CREATE TABLE `oa_hr_overtime` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '加班单ID',
  `overtime_no` VARCHAR(64) NOT NULL COMMENT '加班单流水号',
  `applicant_username` VARCHAR(64) NOT NULL COMMENT '申请人账号',
  `applicant_name` VARCHAR(64) NOT NULL COMMENT '申请人姓名',
  `dept_name` VARCHAR(100) DEFAULT NULL COMMENT '部门名称',
  `start_time` DATETIME NOT NULL COMMENT '加班起始时间',
  `end_time` DATETIME NOT NULL COMMENT '加班结束时间',
  `total_hours` DECIMAL(5,1) NOT NULL DEFAULT '0.0' COMMENT '加班工时(小时)',
  `reason` VARCHAR(1000) NOT NULL COMMENT '加班工作内容及事由',
  `files` VARCHAR(1000) DEFAULT NULL COMMENT '附件文件URL',
  `proc_inst_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联流程实例ID',
  `proc_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态 (0草稿, 1审核中, 2已通过, 3已驳回, 4已作废)',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_overtime_no` (`overtime_no`),
  KEY `idx_applicant` (`applicant_username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='人事加班申请单表';

-- 3.3 外出报备申请表 (替代 bs_egress)
DROP TABLE IF EXISTS `oa_hr_egress`;
CREATE TABLE `oa_hr_egress` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '外出单ID',
  `egress_no` VARCHAR(64) NOT NULL COMMENT '外出流水号',
  `applicant_username` VARCHAR(64) NOT NULL COMMENT '申请人账号',
  `applicant_name` VARCHAR(64) NOT NULL COMMENT '申请人姓名',
  `dept_name` VARCHAR(100) DEFAULT NULL COMMENT '部门名称',
  `start_time` DATETIME NOT NULL COMMENT '外出开始时间',
  `end_time` DATETIME NOT NULL COMMENT '外出预计结束时间',
  `location` VARCHAR(500) NOT NULL COMMENT '外出地点/拜访单位',
  `companions` VARCHAR(500) DEFAULT NULL COMMENT '随行人员名单',
  `reason` VARCHAR(1000) NOT NULL COMMENT '外出事由',
  `total_hours` DECIMAL(5,1) DEFAULT '0.0' COMMENT '外出工时(小时)',
  `files` VARCHAR(1000) DEFAULT NULL COMMENT '相关证明或文件URL',
  `proc_inst_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联流程实例ID',
  `proc_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态 (0草稿, 1审核中, 2已通过, 3已驳回, 4已作废)',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_egress_no` (`egress_no`),
  KEY `idx_applicant` (`applicant_username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='人事外出报备申请单表';

-- 3.4 考勤异常补卡申请单 (替代 bs_attendance)
DROP TABLE IF EXISTS `oa_hr_attendance_repair`;
CREATE TABLE `oa_hr_attendance_repair` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '补卡申请ID',
  `repair_no` VARCHAR(64) NOT NULL COMMENT '补卡流水号',
  `applicant_username` VARCHAR(64) NOT NULL COMMENT '申请人账号',
  `applicant_name` VARCHAR(64) NOT NULL COMMENT '申请人姓名',
  `dept_name` VARCHAR(100) DEFAULT NULL COMMENT '部门名称',
  `reason` VARCHAR(1000) NOT NULL COMMENT '异常说明/补卡原因',
  `files` VARCHAR(1000) DEFAULT NULL COMMENT '证明附件URL',
  `total_count` INT NOT NULL DEFAULT 1 COMMENT '补卡次数',
  `proc_inst_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联流程实例ID',
  `proc_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态 (0草稿, 1审核中, 2已通过, 3已驳回, 4已作废)',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_repair_no` (`repair_no`),
  KEY `idx_applicant` (`applicant_username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考勤补卡申请单主表';

-- 3.5 考勤补卡明细项表 (替代 bs_attendance_details)
DROP TABLE IF EXISTS `oa_hr_attendance_repair_detail`;
CREATE TABLE `oa_hr_attendance_repair_detail` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '明细项ID',
  `repair_id` BIGINT UNSIGNED NOT NULL COMMENT '关联补卡申请单ID',
  `repair_date` DATE NOT NULL COMMENT '补卡日期',
  `repair_type` VARCHAR(32) NOT NULL COMMENT '补卡类型 (CHECK_IN上班卡, CHECK_OUT下班卡, ALL全天漏卡)',
  `original_sign_time` DATETIME DEFAULT NULL COMMENT '原打卡时间 (若有)',
  `repair_sign_time` DATETIME NOT NULL COMMENT '应修正打卡时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '明细原因说明',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_repair_id` (`repair_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考勤补卡申请明细项表';

-- 3.6 移动打卡/考勤流水记录表 (支撑移动端打卡及统计)
DROP TABLE IF EXISTS `oa_hr_clock_record`;
CREATE TABLE `oa_hr_clock_record` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '打卡流水ID',
  `username` VARCHAR(64) NOT NULL COMMENT '打卡员工账号',
  `real_name` VARCHAR(64) NOT NULL COMMENT '员工姓名',
  `dept_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '部门ID',
  `clock_time` DATETIME NOT NULL COMMENT '打卡时间',
  `clock_type` VARCHAR(32) NOT NULL COMMENT '打卡类型 (ON上班打卡, OFF下班打卡)',
  `device_type` VARCHAR(32) DEFAULT 'MOBILE' COMMENT '设备 (MOBILE手机端, WEB网页端, HARDWARE考勤机)',
  `location_address` VARCHAR(500) DEFAULT NULL COMMENT '打卡地理位置名称',
  `latitude` DECIMAL(10,6) DEFAULT NULL COMMENT '打卡经度',
  `longitude` DECIMAL(10,6) DEFAULT NULL COMMENT '打卡纬度',
  `status` VARCHAR(32) NOT NULL DEFAULT 'NORMAL' COMMENT '考勤判定 (NORMAL正常, LATE迟到, EARLY早退, OUTSIDE外勤打卡)',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录入库时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_clock_time` (`username`, `clock_time`),
  KEY `idx_clock_time` (`clock_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工日常考勤打卡流水表';

-- 3.7 员工月度工资条表 (替代 BS_SALARY)
DROP TABLE IF EXISTS `oa_hr_salary`;
CREATE TABLE `oa_hr_salary` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `salary_month` VARCHAR(10) NOT NULL COMMENT '薪酬月份 (如 2026-10)',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '员工用户ID',
  `username` VARCHAR(64) NOT NULL COMMENT '员工工号/账号',
  `real_name` VARCHAR(64) NOT NULL COMMENT '真实姓名',
  `dept_name` VARCHAR(100) DEFAULT NULL COMMENT '所属部门',
  `post_name` VARCHAR(64) DEFAULT NULL COMMENT '职务岗位',
  `base_salary` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '基本薪资',
  `post_salary` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '岗位工资/津贴',
  `performance_salary` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '绩效奖金',
  `overtime_salary` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '加班工时补贴',
  `bonus` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '全勤/专项激励奖',
  `gross_salary` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '应发总计 (Gross)',
  `pension_fee` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '个人社保五险',
  `housing_fund` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '个人住房公积金',
  `tax_fee` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '个人所得税扣缴',
  `other_deduction` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '迟到旷工/其他扣除',
  `net_salary` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '税后实发金额 (Net Pay)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '发放状态 (1已发放, 0核算中)',
  `is_confirmed` TINYINT NOT NULL DEFAULT 0 COMMENT '员工签收状态 (0待签收, 1已核对签收)',
  `confirm_time` DATETIME DEFAULT NULL COMMENT '签收确认时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '发放备注说明',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '核算人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '核算时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_month` (`username`, `salary_month`),
  KEY `idx_month` (`salary_month`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工月度工资条与薪酬核算表';

-- ------------------------------------------------------------------------------
-- 4. 行政公文与印章管理模块 (oa_adm_*)
-- 规范化原 bs_seal_normal, bs_seal_contract, bs_seal_declare, bs_redhead, bs_record_borrow
-- ------------------------------------------------------------------------------

-- 4.1 企业用印申请表 (统一合并公章、合同章、申报章)
DROP TABLE IF EXISTS `oa_adm_seal_apply`;
CREATE TABLE `oa_adm_seal_apply` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '用印申请ID',
  `apply_no` VARCHAR(64) NOT NULL COMMENT '用印流水单号 (如 YZ202610270001)',
  `applicant_username` VARCHAR(64) NOT NULL COMMENT '申请人账号',
  `applicant_name` VARCHAR(64) NOT NULL COMMENT '申请人姓名',
  `dept_name` VARCHAR(100) DEFAULT NULL COMMENT '部门名称',
  `agent_name` VARCHAR(64) DEFAULT NULL COMMENT '经办人姓名',
  `seal_type` VARCHAR(32) NOT NULL COMMENT '印章类型 (COMMON公章, CONTRACT合同章, LEGAL法人章, FINANCE财务章, DECLARE申报章)',
  `file_name` VARCHAR(255) NOT NULL COMMENT '需盖章文件名称',
  `seal_copies` INT NOT NULL DEFAULT 1 COMMENT '用印份数',
  `purpose` VARCHAR(1000) NOT NULL COMMENT '用印事由及详细说明',
  `files` VARCHAR(1000) DEFAULT NULL COMMENT '盖章材料电子档附件URL',
  `contract_amount` DECIMAL(12,2) DEFAULT NULL COMMENT '关联关联合同金额(合同用印时使用)',
  `counterparty` VARCHAR(255) DEFAULT NULL COMMENT '相对方/合作单位(合同用印时使用)',
  `proc_inst_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联流程实例ID',
  `proc_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态 (0草稿, 1审核中, 2已通过, 3已驳回, 4已作废)',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_applicant` (`applicant_username`),
  KEY `idx_proc_inst` (`proc_inst_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业用印审批申请表';

-- 4.2 红头文件/政务公文下发表 (替代 bs_redhead)
DROP TABLE IF EXISTS `oa_adm_redhead_doc`;
CREATE TABLE `oa_adm_redhead_doc` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '公文ID',
  `doc_no` VARCHAR(100) NOT NULL COMMENT '发文字号 (如 启航发〔2026〕01号)',
  `title` VARCHAR(255) NOT NULL COMMENT '公文标题',
  `doc_type` VARCHAR(32) NOT NULL COMMENT '公文类型 (DECISION决议, NOTICE通知, CIRCULAR通报, ORDER命令)',
  `security_level` VARCHAR(32) DEFAULT 'NORMAL' COMMENT '密级 (NORMAL普件, SECRET秘密, CONFIDENTIAL机密)',
  `issuer_dept` VARCHAR(100) NOT NULL COMMENT '发文部门/拟稿部门',
  `drafter_name` VARCHAR(64) NOT NULL COMMENT '拟稿人姓名',
  `signer_name` VARCHAR(64) DEFAULT NULL COMMENT '签发人姓名',
  `target_zone` VARCHAR(500) DEFAULT NULL COMMENT '发布受众范围 (全员, 指定部门等)',
  `content` MEDIUMTEXT NOT NULL COMMENT '公文正文内容 (富文本/HTML)',
  `files` VARCHAR(1000) DEFAULT NULL COMMENT '公文红头扫描件/正式附件PDF',
  `publish_date` DATE DEFAULT NULL COMMENT '印发/发布日期',
  `proc_inst_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联流程实例ID',
  `proc_status` TINYINT NOT NULL DEFAULT 0 COMMENT '公文签发审批状态 (0拟稿, 1审批中, 2已签发/生效, 3已退回)',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doc_no` (`doc_no`),
  KEY `idx_publish_date` (`publish_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='红头公文发文管理表';

-- 4.3 档案借阅申请表 (替代 bs_record_borrow)
DROP TABLE IF EXISTS `oa_adm_record_borrow`;
CREATE TABLE `oa_adm_record_borrow` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '借阅ID',
  `borrow_no` VARCHAR(64) NOT NULL COMMENT '借阅申请流水号',
  `applicant_username` VARCHAR(64) NOT NULL COMMENT '申请人账号',
  `applicant_name` VARCHAR(64) NOT NULL COMMENT '借阅人姓名',
  `dept_name` VARCHAR(100) DEFAULT NULL COMMENT '借阅人部门',
  `record_title` VARCHAR(255) NOT NULL COMMENT '借阅档案/卷册名称',
  `borrow_type` VARCHAR(32) NOT NULL DEFAULT 'READ_ONLY' COMMENT '借阅形式 (READ_ONLY查阅, BORROW_PHYSICAL实体外借, COPY复印)',
  `copies` INT NOT NULL DEFAULT 1 COMMENT '借阅/复印卷数/份数',
  `start_time` DATETIME NOT NULL COMMENT '计划借出时间',
  `expected_return_time` DATETIME NOT NULL COMMENT '预计归还时间',
  `actual_return_time` DATETIME DEFAULT NULL COMMENT '实际归还时间',
  `purpose` VARCHAR(1000) NOT NULL COMMENT '借阅事由及用途说明',
  `return_status` TINYINT NOT NULL DEFAULT 0 COMMENT '归还状态 (0未借出, 1借出中, 2已归还, 3逾期)',
  `files` VARCHAR(1000) DEFAULT NULL COMMENT '审批附件URL',
  `proc_inst_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联流程实例ID',
  `proc_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态 (0草稿, 1审核中, 2已通过, 3已驳回, 4已作废)',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_borrow_no` (`borrow_no`),
  KEY `idx_applicant` (`applicant_username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业档案与资料借阅申请表';

-- ------------------------------------------------------------------------------
-- 5. 协同信息与工作台模块 (oa_collab_*)
-- 规范化原 bs_announce, bs_news, bs_comments, bs_dynamic
-- ------------------------------------------------------------------------------

-- 5.1 通知与公告表 (替代 bs_announce)
DROP TABLE IF EXISTS `oa_collab_notice`;
CREATE TABLE `oa_collab_notice` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '公告ID',
  `title` VARCHAR(255) NOT NULL COMMENT '公告标题',
  `notice_type` VARCHAR(32) NOT NULL COMMENT '类型 (NOTICE公告, NEWS新闻, SAFETY安全提醒, HOLIDAY休假通知)',
  `content` MEDIUMTEXT NOT NULL COMMENT '正文内容',
  `files` VARCHAR(1000) DEFAULT NULL COMMENT '附件文件URL',
  `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶 (1置顶, 0普通)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (1已发布, 0草稿, 2已下线)',
  `publisher_name` VARCHAR(64) NOT NULL COMMENT '发布人姓名',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览阅读次数',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_publish_time` (`publish_time`),
  KEY `idx_notice_type` (`notice_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业协同通知与公告表';

-- 5.2 通告与公文已读/未读状态追踪表 (替代 sys_announcement_send)
DROP TABLE IF EXISTS `oa_collab_notice_read`;
CREATE TABLE `oa_collab_notice_read` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `notice_id` BIGINT UNSIGNED NOT NULL COMMENT '通告/公文ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '员工用户ID',
  `username` VARCHAR(64) NOT NULL COMMENT '员工账号/工号',
  `real_name` VARCHAR(64) NOT NULL COMMENT '员工姓名',
  `read_status` TINYINT NOT NULL DEFAULT 1 COMMENT '已读状态 (1已读, 0未读)',
  `read_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次查阅时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notice_user` (`notice_id`, `username`),
  KEY `idx_notice_id` (`notice_id`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通告与公文阅读状态追踪表';

-- 5.3 业务流转过程评论与讨论留言表 (替代 bs_comments)
DROP TABLE IF EXISTS `oa_collab_comment`;
CREATE TABLE `oa_collab_comment` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '留言ID',
  `business_table` VARCHAR(64) NOT NULL COMMENT '关联业务表',
  `business_id` BIGINT UNSIGNED NOT NULL COMMENT '关联业务单据ID',
  `inst_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联流程实例ID',
  `author_username` VARCHAR(64) NOT NULL COMMENT '留言人账号',
  `author_name` VARCHAR(64) NOT NULL COMMENT '留言人姓名',
  `content` VARCHAR(1000) NOT NULL COMMENT '讨论/沟通内容',
  `files` VARCHAR(1000) DEFAULT NULL COMMENT '附件图片/文档URL',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发表时间',
  PRIMARY KEY (`id`),
  KEY `idx_business` (`business_table`, `business_id`),
  KEY `idx_inst_id` (`inst_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务与流程协同沟通留言表';

-- ------------------------------------------------------------------------------
-- 6. 企微/钉钉/飞书 待办审批提醒机器人配置表 (oa_sys_notify_config)
-- ------------------------------------------------------------------------------
DROP TABLE IF EXISTS `oa_sys_notify_config`;
CREATE TABLE `oa_sys_notify_config` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `platform` VARCHAR(32) NOT NULL COMMENT '平台类型 (WECHAT_WORK企业微信, DINGTALK钉钉, FEISHU飞书, CUSTOM自定义)',
  `name` VARCHAR(64) NOT NULL COMMENT '机器人名称 (如 集团审批中心企微提醒群)',
  `webhook_url` VARCHAR(512) NOT NULL COMMENT 'Webhook URL 地址',
  `secret_key` VARCHAR(256) DEFAULT NULL COMMENT '安全签名密钥(钉钉/飞书加签)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态 (1启用, 0禁用)',
  `notify_events` VARCHAR(255) DEFAULT 'TASK_ASSIGNED,PROCESS_FINISHED,NOTICE_PUBLISHED' COMMENT '订阅事件(TASK_ASSIGNED待办到达, PROCESS_FINISHED办结通知, NOTICE_PUBLISHED公文发布)',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注说明',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微/钉钉/飞书机器人消息推送配置表';

SET FOREIGN_KEY_CHECKS = 1;

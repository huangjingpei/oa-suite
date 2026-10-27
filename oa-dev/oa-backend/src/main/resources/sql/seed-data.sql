-- ==============================================================================
-- 现代企业协同办公系统 (OA Suite Modernization) 基础种子数据脚本
-- 适用数据库: MySQL 8.0+
-- 顶级包名: com.graddu.oa
-- 规范版本: M1-20261027
-- ==============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------------------------
-- 1. 系统部门初始数据
-- ------------------------------------------------------------------------------
DELETE FROM `oa_sys_dept`;
INSERT INTO `oa_sys_dept` (`id`, `parent_id`, `dept_code`, `dept_name`, `leader`, `phone`, `email`, `sort_order`, `status`, `create_by`) VALUES
(1001, 0, 'DEPT_CORP', '集团总部', 'admin', '010-88880001', 'corp@graddu.com', 1, 1, 'system'),
(1002, 1001, 'DEPT_EXEC', '总经办', 'admin', '010-88880002', 'exec@graddu.com', 2, 1, 'system'),
(1003, 1001, 'DEPT_HR_ADM', '综合行政与人事部', 'hr_mgr', '010-88880003', 'hr@graddu.com', 3, 1, 'system'),
(1004, 1001, 'DEPT_DEV', '技术研发中心', 'dev_mgr', '010-88880004', 'dev@graddu.com', 4, 1, 'system'),
(1005, 1001, 'DEPT_MKT', '市场与运营部', 'mkt_mgr', '010-88880005', 'mkt@graddu.com', 5, 1, 'system'),
(1006, 1001, 'DEPT_FIN', '财务管理部', 'fin_mgr', '010-88880006', 'fin@graddu.com', 6, 1, 'system');

-- ------------------------------------------------------------------------------
-- 2. 系统角色初始数据
-- ------------------------------------------------------------------------------
DELETE FROM `oa_sys_role`;
INSERT INTO `oa_sys_role` (`id`, `role_code`, `role_name`, `description`, `sort_order`, `status`, `create_by`) VALUES
(2001, 'ROLE_ADMIN', '超级管理员', '拥有系统全模块配置、数据查看与最高管理权限', 1, 1, 'system'),
(2002, 'ROLE_HR', '人事专员', '负责员工入转调离、请假与考勤统计、排班管理', 2, 1, 'system'),
(2003, 'ROLE_ADM', '行政管理专员', '负责印章管理、用印审批、档案借阅与红头文件印发', 3, 1, 'system'),
(2004, 'ROLE_DEPT_LEADER', '部门负责人', '负责本部门员工单据的第一级审批与流转', 4, 1, 'system'),
(2005, 'ROLE_EMPLOYEE', '普通员工', '具备基础个人工作台、单据发起与协同沟通权限', 5, 1, 'system');

-- ------------------------------------------------------------------------------
-- 3. 系统初始用户数据
-- 默认密码均为: 123456 (BCrypt哈希: $2a$10$Rt0wSOsmcllHK7qQRKNLoOIrrF8xYx6b3cEJioWYu.hxL58.eTHmy)
-- ------------------------------------------------------------------------------
DELETE FROM `oa_sys_user`;
INSERT INTO `oa_sys_user` (`id`, `username`, `real_name`, `password`, `salt`, `work_no`, `dept_id`, `post_name`, `email`, `phone`, `gender`, `status`, `create_by`) VALUES
(3001, 'admin', '系统管理员', '$2a$10$Rt0wSOsmcllHK7qQRKNLoOIrrF8xYx6b3cEJioWYu.hxL58.eTHmy', 'salt123', 'OA0001', 1002, '技术总监/总管', 'admin@graddu.com', '13800000001', 1, 1, 'system'),
(3002, 'hr_mgr', '李人事', '$2a$10$Rt0wSOsmcllHK7qQRKNLoOIrrF8xYx6b3cEJioWYu.hxL58.eTHmy', 'salt123', 'OA0002', 1003, '人事主管', 'hr@graddu.com', '13800000002', 2, 1, 'system'),
(3003, 'adm_mgr', '赵行政', '$2a$10$Rt0wSOsmcllHK7qQRKNLoOIrrF8xYx6b3cEJioWYu.hxL58.eTHmy', 'salt123', 'OA0003', 1003, '印章档案专员', 'adm@graddu.com', '13800000003', 2, 1, 'system'),
(3004, 'dev_mgr', '王主管', '$2a$10$Rt0wSOsmcllHK7qQRKNLoOIrrF8xYx6b3cEJioWYu.hxL58.eTHmy', 'salt123', 'OA0004', 1004, '研发一部经理', 'dev_mgr@graddu.com', '13800000004', 1, 1, 'system'),
(3005, 'zhangsan', '张三', '$2a$10$Rt0wSOsmcllHK7qQRKNLoOIrrF8xYx6b3cEJioWYu.hxL58.eTHmy', 'salt123', 'OA0101', 1004, 'Java开发工程师', 'zhangsan@graddu.com', '13800000101', 1, 1, 'system'),
(3006, 'lisi', '李四', '$2a$10$Rt0wSOsmcllHK7qQRKNLoOIrrF8xYx6b3cEJioWYu.hxL58.eTHmy', 'salt123', 'OA0102', 1004, '前端开发工程师', 'lisi@graddu.com', '13800000102', 1, 1, 'system');

-- ------------------------------------------------------------------------------
-- 4. 用户与角色关联初始数据
-- ------------------------------------------------------------------------------
DELETE FROM `oa_sys_user_role`;
INSERT INTO `oa_sys_user_role` (`id`, `user_id`, `role_id`) VALUES
(4001, 3001, 2001), -- admin -> 超管
(4002, 3002, 2002), -- hr_mgr -> 人事专员
(4003, 3002, 2004), -- hr_mgr -> 部门负责人
(4004, 3003, 2003), -- adm_mgr -> 行政管理
(4005, 3004, 2004), -- dev_mgr -> 部门负责人
(4006, 3005, 2005), -- zhangsan -> 员工
(4007, 3006, 2005); -- lisi -> 员工

-- ------------------------------------------------------------------------------
-- 4.1 系统权限与菜单初始数据
-- ------------------------------------------------------------------------------
DELETE FROM `oa_sys_permission`;
INSERT INTO `oa_sys_permission` (`id`, `parent_id`, `name`, `perms`, `menu_type`, `icon`, `sort_order`, `status`, `create_by`) VALUES
-- 1. 工作台审批中枢
(1000, 0, '工作台审批中枢', 'wf', 1, 'DashboardOutlined', 1, 1, 'system'),
(1010, 1000, '待办任务审批', 'wf:pending:view', 2, 'ClockCircleOutlined', 1, 1, 'system'),
(1011, 1010, '审批通过', 'wf:task:approve', 3, NULL, 1, 1, 'system'),
(1012, 1010, '审批驳回', 'wf:task:reject', 3, NULL, 2, 1, 'system'),
(1013, 1010, '审批加签', 'wf:task:sign', 3, NULL, 3, 1, 'system'),
(1020, 1000, '已办流转追踪', 'wf:handled:view', 2, 'CheckCircleOutlined', 2, 1, 'system'),
(1030, 1000, '我发起的单据', 'wf:mine:view', 2, 'SendOutlined', 3, 1, 'system'),
(1031, 1030, '撤回单据', 'wf:task:cancel', 3, NULL, 1, 1, 'system'),

-- 2. 人事考勤中心
(2000, 0, '人事考勤中心', 'hr', 1, 'TeamOutlined', 2, 1, 'system'),
(2010, 2000, '员工请假台账', 'hr:leave:view', 2, 'CalendarOutlined', 1, 1, 'system'),
(2011, 2010, '提交请假申请', 'hr:leave:apply', 3, NULL, 1, 1, 'system'),
(2012, 2010, '请假排版打印', 'hr:leave:print', 3, NULL, 2, 1, 'system'),
(2020, 2000, '考勤打卡记录', 'hr:attendance:view', 2, 'FieldTimeOutlined', 2, 1, 'system'),
(2021, 2020, '在线打卡', 'hr:attendance:clock', 3, NULL, 1, 1, 'system'),
(2022, 2020, '发起漏卡补卡', 'hr:attendance:repair', 3, NULL, 2, 1, 'system'),

-- 3. 行政综合中心
(3000, 0, '行政综合中心', 'adm', 1, 'BankOutlined', 3, 1, 'system'),
(3010, 3000, '企业用印审批', 'adm:seal:view', 2, 'SafetyCertificateOutlined', 1, 1, 'system'),
(3011, 3010, '发起用印申请', 'adm:seal:apply', 3, NULL, 1, 1, 'system'),
(3012, 3010, '用印单据打印', 'adm:seal:print', 3, NULL, 2, 1, 'system'),
(3020, 3000, '红头公文拟发', 'adm:redhead:view', 2, 'FileTextOutlined', 2, 1, 'system'),
(3021, 3020, '公文起草拟发', 'adm:redhead:draft', 3, NULL, 1, 1, 'system'),
(3022, 3020, '红头公文套打', 'adm:redhead:print', 3, NULL, 2, 1, 'system'),
(3030, 3000, '档案资料借阅', 'adm:borrow:view', 2, 'FolderOpenOutlined', 3, 1, 'system'),
(3031, 3030, '发起借阅申请', 'adm:borrow:apply', 3, NULL, 1, 1, 'system'),
(3032, 3030, '确认归还核销', 'adm:borrow:return', 3, NULL, 2, 1, 'system'),

-- 4. 系统组织管理
(4000, 0, '系统组织管理', 'sys', 1, 'SettingOutlined', 4, 1, 'system'),
(4010, 4000, '部门架构管理', 'sys:dept:view', 2, 'ApartmentOutlined', 1, 1, 'system'),
(4011, 4010, '新增部门', 'sys:dept:add', 3, NULL, 1, 1, 'system'),
(4012, 4010, '修改部门', 'sys:dept:edit', 3, NULL, 2, 1, 'system'),
(4013, 4010, '删除部门', 'sys:dept:delete', 3, NULL, 3, 1, 'system'),
(4020, 4000, '员工用户管理', 'sys:user:view', 2, 'UserOutlined', 2, 1, 'system'),
(4021, 4020, '新建员工', 'sys:user:add', 3, NULL, 1, 1, 'system'),
(4022, 4020, '编辑员工', 'sys:user:edit', 3, NULL, 2, 1, 'system'),
(4023, 4020, '启停员工', 'sys:user:status', 3, NULL, 3, 1, 'system'),
(4024, 4020, '重置密码', 'sys:user:reset', 3, NULL, 4, 1, 'system'),
(4025, 4020, '注销员工', 'sys:user:delete', 3, NULL, 5, 1, 'system'),
(4030, 4000, '角色权限体系', 'sys:role:view', 2, 'IdcardOutlined', 3, 1, 'system'),
(4031, 4030, '新增角色', 'sys:role:add', 3, NULL, 1, 1, 'system'),
(4032, 4030, '编辑角色', 'sys:role:edit', 3, NULL, 2, 1, 'system'),
(4033, 4030, '分配权限', 'sys:role:auth', 3, NULL, 3, 1, 'system'),
(4034, 4030, '删除角色', 'sys:role:delete', 3, NULL, 4, 1, 'system'),
(4040, 4000, '系统数据字典', 'sys:dict:view', 2, 'BookOutlined', 4, 1, 'system'),
(4041, 4040, '维护字典', 'sys:dict:save', 3, NULL, 1, 1, 'system'),
(4042, 4040, '删除字典', 'sys:dict:delete', 3, NULL, 2, 1, 'system');

-- ------------------------------------------------------------------------------
-- 4.2 角色与权限关联初始数据
-- ------------------------------------------------------------------------------
DELETE FROM `oa_sys_role_permission`;
-- 给超级管理员 (2001) 赋予全量权限
INSERT INTO `oa_sys_role_permission` (`role_id`, `permission_id`)
SELECT 2001, id FROM `oa_sys_permission`;

-- 给人事主管 (2002) 赋予工作台与人事中心权限
INSERT INTO `oa_sys_role_permission` (`role_id`, `permission_id`) VALUES
(2002, 1000), (2002, 1010), (2002, 1011), (2002, 1012), (2002, 1013), (2002, 1020), (2002, 1030), (2002, 1031),
(2002, 2000), (2002, 2010), (2002, 2011), (2002, 2012), (2002, 2020), (2002, 2021), (2002, 2022);

-- 给行政专员 (2003) 赋予工作台与行政中心权限
INSERT INTO `oa_sys_role_permission` (`role_id`, `permission_id`) VALUES
(2003, 1000), (2003, 1010), (2003, 1011), (2003, 1012), (2003, 1013), (2003, 1020), (2003, 1030), (2003, 1031),
(2003, 3000), (2003, 3010), (2003, 3011), (2003, 3012), (2003, 3020), (2003, 3021), (2003, 3022), (2003, 3030), (2003, 3031), (2003, 3032);

-- 给普通员工 (2005) 赋予日常填报与发起权限
INSERT INTO `oa_sys_role_permission` (`role_id`, `permission_id`) VALUES
(2005, 1000), (2005, 1020), (2005, 1030), (2005, 1031),
(2005, 2000), (2005, 2010), (2005, 2011), (2005, 2020), (2005, 2021), (2005, 2022),
(2005, 3000), (2005, 3010), (2005, 3011), (2005, 3020), (2005, 3030), (2005, 3031);

-- ------------------------------------------------------------------------------
-- 5. 数据字典初始数据 (字典类型 + 字典键值)
-- ------------------------------------------------------------------------------
DELETE FROM `oa_sys_dict_type`;
INSERT INTO `oa_sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `remark`, `create_by`) VALUES
(5001, '请假类型', 'oa_leave_type', 1, '人事考勤请假类别', 'system'),
(5002, '印章类型', 'oa_seal_type', 1, '行政公章用印类别', 'system'),
(5003, '公文类型', 'oa_redhead_type', 1, '政务红头公文发文类别', 'system'),
(5004, '流程审批状态', 'oa_proc_status', 1, '通用审批流单据状态', 'system'),
(5005, '打卡判定状态', 'oa_clock_status', 1, '日常考勤流水判定', 'system');

DELETE FROM `oa_sys_dict_data`;
INSERT INTO `oa_sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `sort_order`, `status`, `create_by`) VALUES
-- 请假类型
(5101, 'oa_leave_type', '年假', 'ANNUAL', 1, 1, 'system'),
(5102, 'oa_leave_type', '事假', 'PERSONAL', 2, 1, 'system'),
(5103, 'oa_leave_type', '病假', 'SICK', 3, 1, 'system'),
(5104, 'oa_leave_type', '调休', 'REST', 4, 1, 'system'),
(5105, 'oa_leave_type', '婚假', 'MARRIAGE', 5, 1, 'system'),
(5106, 'oa_leave_type', '产假/陪产假', 'MATERNITY', 6, 1, 'system'),
(5107, 'oa_leave_type', '丧假', 'FUNERAL', 7, 1, 'system'),

-- 印章类型
(5201, 'oa_seal_type', '企业公章', 'COMMON', 1, 1, 'system'),
(5202, 'oa_seal_type', '合同专用章', 'CONTRACT', 2, 1, 'system'),
(5203, 'oa_seal_type', '法定代表人名章', 'LEGAL', 3, 1, 'system'),
(5204, 'oa_seal_type', '财务专用章', 'FINANCE', 4, 1, 'system'),
(5205, 'oa_seal_type', '资质与申报专用章', 'DECLARE', 5, 1, 'system'),

-- 公文类型
(5301, 'oa_redhead_type', '重要决议', 'DECISION', 1, 1, 'system'),
(5302, 'oa_redhead_type', '行政通知', 'NOTICE', 2, 1, 'system'),
(5303, 'oa_redhead_type', '通报批评/表扬', 'CIRCULAR', 3, 1, 'system'),
(5304, 'oa_redhead_type', '业务指令', 'ORDER', 4, 1, 'system'),

-- 流程审批状态
(5401, 'oa_proc_status', '草稿待发', '0', 1, 1, 'system'),
(5402, 'oa_proc_status', '流转审批中', '1', 2, 1, 'system'),
(5403, 'oa_proc_status', '审批通过', '2', 3, 1, 'system'),
(5404, 'oa_proc_status', '审批驳回', '3', 4, 1, 'system'),
(5405, 'oa_proc_status', '撤销作废', '4', 5, 1, 'system'),

-- 考勤打卡状态
(5501, 'oa_clock_status', '正常打卡', 'NORMAL', 1, 1, 'system'),
(5502, 'oa_clock_status', '迟到', 'LATE', 2, 1, 'system'),
(5503, 'oa_clock_status', '早退', 'EARLY', 3, 1, 'system'),
(5504, 'oa_clock_status', '外勤打卡', 'OUTSIDE', 4, 1, 'system');

-- ------------------------------------------------------------------------------
-- 6. 基础工作流定义初始化 (预置自由流程与标准单据绑定)
-- ------------------------------------------------------------------------------
DELETE FROM `oa_wf_definition`;
INSERT INTO `oa_wf_definition` (`id`, `proc_key`, `proc_name`, `business_table`, `proc_mode`, `version`, `status`, `remark`, `create_by`) VALUES
(6001, 'PROC_LEAVE', '员工请假审批流程', 'oa_hr_leave', 'FREE', 1, 1, '支持自由指定部门主管、分管领导，支持加签/会签', 'system'),
(6002, 'PROC_SEAL', '企业印章用印审批流程', 'oa_adm_seal_apply', 'FREE', 1, 1, '包含公章、合同章用印审批流', 'system'),
(6003, 'PROC_OVERTIME', '加班申请审批流程', 'oa_hr_overtime', 'FREE', 1, 1, '部门主管审核与人事备案', 'system'),
(6004, 'PROC_EGRESS', '员工因公外出报备流程', 'oa_hr_egress', 'FREE', 1, 1, '外出审批与考勤免卡流转', 'system'),
(6005, 'PROC_REPAIR', '考勤异常补卡申请流程', 'oa_hr_attendance_repair', 'FREE', 1, 1, '漏打卡、设备异常补卡申请', 'system'),
(6006, 'PROC_BORROW', '企业档案借阅审批流程', 'oa_adm_record_borrow', 'FREE', 1, 1, '资料与档案实体查阅及借出审批', 'system'),
(6007, 'PROC_REDHEAD', '红头公文拟稿签发流程', 'oa_adm_redhead_doc', 'FREE', 1, 1, '公文拟办、核稿与领导会签签发', 'system');

-- ------------------------------------------------------------------------------
-- 7. 协同通知初始化演示数据
-- ------------------------------------------------------------------------------
DELETE FROM `oa_collab_notice`;
INSERT INTO `oa_collab_notice` (`id`, `title`, `notice_type`, `content`, `is_top`, `status`, `publisher_name`, `publish_time`, `view_count`, `create_by`) VALUES
(7001, '关于新一代企业协同OA平台正式启动升级改造的通知', 'NOTICE', '<p>各位同仁：</p><p>为全面提升企业办公效能与移动审批体验，公司正式启动新一代数字化协同办公系统（OA-Suite Modernization）的研发与升级工作。系统采用现代企业级架构全面重构，支持动态加签、会签、多端打卡与高保真打印单据等核心能力。敬请期待！</p>', 1, 1, '总经办', NOW(), 128, 'system');

SET FOREIGN_KEY_CHECKS = 1;

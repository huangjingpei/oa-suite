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

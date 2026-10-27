/**
 * 业务领域强类型契约定义 (HR & ADM)
 */

// ==================== 1. 请假管理 (HR Leave) ====================

export type LeaveType = 'ANNUAL' | 'PERSONAL' | 'SICK' | 'REST' | 'MARRIAGE' | 'MATERNITY' | 'FUNERAL';

export interface LeaveApplyDTO {
  leaveType: LeaveType;
  startTime: string;
  endTime: string;
  totalHours: number;
  totalDays: number;
  reason: string;
  files?: string;
  isDraft?: boolean;
  auditUsernames?: string[];
  notifyUsernames?: string[];
}

export interface LeaveVO {
  id: number;
  leaveNo: string;
  applicantUsername: string;
  applicantName: string;
  deptId?: number;
  deptName?: string;
  leaveType: LeaveType;
  startTime: string;
  endTime: string;
  totalHours: number;
  totalDays: number;
  reason: string;
  files?: string;
  procInstId?: number;
  procStatus: number; // 0草稿, 1审核中, 2通过, 3驳回, 4作废
  createTime: string;
}

// ==================== 2. 考勤打卡与补卡 (HR Attendance) ====================

export interface ClockInDTO {
  clockType: 'ON' | 'OFF';
  deviceType?: 'MOBILE' | 'WEB';
  locationAddress?: string;
  latitude?: number;
  longitude?: number;
  remark?: string;
}

export interface ClockRecordVO {
  id: number;
  username: string;
  realName: string;
  deptName?: string;
  clockType: 'ON' | 'OFF';
  clockTime: string;
  deviceType?: string;
  locationAddress?: string;
  status: number; // 0正常, 1迟到, 2早退, 3外勤
  remark?: string;
}

export interface RepairDetailItem {
  repairDate: string;
  repairType: 'CHECK_IN' | 'CHECK_OUT' | 'ALL';
  originalSignTime?: string;
  repairSignTime: string;
  remark?: string;
}

export interface AttendanceRepairDTO {
  reason: string;
  files?: string;
  isDraft?: boolean;
  auditUsernames?: string[];
  details: RepairDetailItem[];
}

export interface AttendanceRepairVO {
  id: number;
  repairNo: string;
  applicantUsername: string;
  applicantName: string;
  deptName?: string;
  reason: string;
  files?: string;
  totalCount: number;
  procInstId?: number;
  procStatus: number;
  createTime: string;
  details?: RepairDetailItem[];
}

// ==================== 3. 企业用印管理 (ADM Seal) ====================

export type SealType = 'COMMON' | 'CONTRACT' | 'LEGAL' | 'FINANCE' | 'DECLARE';

export interface SealApplyDTO {
  sealType: SealType;
  fileName: string;
  sealCopies: number;
  purpose: string;
  agentName?: string;
  files?: string;
  contractAmount?: number;
  counterparty?: string;
  isDraft?: boolean;
  auditUsernames?: string[];
}

export interface SealApplyVO {
  id: number;
  applyNo: string;
  applicantUsername: string;
  applicantName: string;
  deptName?: string;
  agentName?: string;
  sealType: SealType;
  fileName: string;
  sealCopies: number;
  purpose: string;
  files?: string;
  contractAmount?: number;
  counterparty?: string;
  procInstId?: number;
  procStatus: number;
  createTime: string;
}

// ==================== 4. 红头公文拟发 (ADM Redhead) ====================

export type DocType = 'DECISION' | 'NOTICE' | 'CIRCULAR' | 'ORDER';
export type SecurityLevel = 'NORMAL' | 'SECRET' | 'CONFIDENTIAL';

export interface RedheadDocDTO {
  docNo: string;
  title: string;
  docType: DocType;
  securityLevel?: SecurityLevel;
  signerName?: string;
  targetZone?: string;
  content: string;
  files?: string;
  publishDate?: string;
  isDraft?: boolean;
  auditUsernames?: string[];
}

export interface RedheadDocVO {
  id: number;
  docNo: string;
  title: string;
  docType: DocType;
  securityLevel: SecurityLevel;
  issuerDept?: string;
  drafterName?: string;
  signerName?: string;
  targetZone?: string;
  content: string;
  files?: string;
  publishDate?: string;
  procInstId?: number;
  procStatus: number;
  createTime: string;
}

// ==================== 5. 档案借阅申请 (ADM Record Borrow) ====================

export type BorrowType = 'READ_ONLY' | 'BORROW_PHYSICAL' | 'COPY';

export interface RecordBorrowDTO {
  recordTitle: string;
  borrowType?: BorrowType;
  copies?: number;
  startTime: string;
  expectedReturnTime: string;
  purpose: string;
  files?: string;
  isDraft?: boolean;
  auditUsernames?: string[];
}

export interface RecordBorrowVO {
  id: number;
  borrowNo: string;
  applicantUsername: string;
  applicantName: string;
  deptName?: string;
  recordTitle: string;
  borrowType: BorrowType;
  copies: number;
  startTime: string;
  expectedReturnTime: string;
  actualReturnTime?: string;
  purpose: string;
  returnStatus: number; // 0未借出, 1借出中, 2已归还, 3逾期
  files?: string;
  procInstId?: number;
  procStatus: number;
  createTime: string;
}

// ==================== 6. 加班申报 (HR Overtime) ====================

export interface OvertimeApplyDTO {
  startTime: string;
  endTime: string;
  totalHours: number;
  reason: string;
  files?: string;
  isDraft?: boolean;
  auditUsernames?: string[];
  notifyUsernames?: string[];
}

export interface OvertimeVO {
  id: number;
  overtimeNo: string;
  applicantUsername: string;
  applicantName: string;
  deptName?: string;
  startTime: string;
  endTime: string;
  totalHours: number;
  reason: string;
  files?: string;
  procInstId?: number;
  procStatus: number;
  createTime: string;
}

// ==================== 7. 外出公干/外勤报备 (HR Egress) ====================

export interface EgressApplyDTO {
  startTime: string;
  endTime: string;
  location: string;
  companions?: string;
  totalHours: number;
  reason: string;
  files?: string;
  isDraft?: boolean;
  auditUsernames?: string[];
  notifyUsernames?: string[];
}

export interface EgressVO {
  id: number;
  egressNo: string;
  applicantUsername: string;
  applicantName: string;
  deptName?: string;
  startTime: string;
  endTime: string;
  location: string;
  companions?: string;
  totalHours: number;
  reason: string;
  files?: string;
  procInstId?: number;
  procStatus: number;
  createTime: string;
}

// ==================== 8. 月度工资条 (HR Salary) ====================

export interface SalaryVerifyDTO {
  password: string;
}

export interface SalaryVO {
  id: number;
  salaryMonth: string;
  userId: number;
  username: string;
  realName: string;
  deptName?: string;
  postName?: string;
  baseSalary: number;
  postSalary: number;
  performanceSalary: number;
  overtimeSalary: number;
  bonus: number;
  grossSalary: number;
  pensionFee: number;
  housingFund: number;
  taxFee: number;
  otherDeduction: number;
  netSalary: number;
  status: number;
  isConfirmed: number;
  confirmTime?: string;
  remark?: string;
  createTime: string;
}

// ==================== 9. 公文通告与已读追踪 (Collab Notice & Read Tracking) ====================

export interface NoticeSaveDTO {
  id?: number;
  title: string;
  category?: string;
  priority?: string;
  content: string;
  targetScope?: string;
  targetDepts?: string;
  targetUsers?: string;
  files?: string;
  status?: number;
}

export interface NoticeVO {
  id: number;
  title: string;
  category: string;
  priority: string;
  content: string;
  targetScope: string;
  targetDepts?: string;
  targetUsers?: string;
  files?: string;
  status: number;
  publisherUsername: string;
  publisherName: string;
  publishTime?: string;
  isRead: boolean;
  readCount: number;
  totalCount: number;
  readRate: number;
  createTime: string;
}

export interface UserReadItem {
  userId: number;
  username: string;
  realName: string;
  deptName?: string;
  readTime?: string;
}

export interface NoticeReadStatsVO {
  noticeId: number;
  title: string;
  totalUsers: number;
  readCount: number;
  unreadCount: number;
  readRate: number;
  readUsers: UserReadItem[];
  unreadUsers: UserReadItem[];
}

import request from '@/utils/request';
import type { R, PageResult } from '@/types/api';
import type {
  LeaveApplyDTO,
  LeaveVO,
  ClockInDTO,
  ClockRecordVO,
  AttendanceRepairDTO,
  AttendanceRepairVO,
  SealApplyDTO,
  SealApplyVO,
  RedheadDocDTO,
  RedheadDocVO,
  RecordBorrowDTO,
  RecordBorrowVO,
  OvertimeApplyDTO,
  OvertimeVO,
  EgressApplyDTO,
  EgressVO,
  SalaryVerifyDTO,
  SalaryVO,
  NoticeSaveDTO,
  NoticeVO,
  NoticeReadStatsVO
} from '@/types/business';

// ==================== 1. 请假业务 (HR Leave) ====================

export function applyLeaveApi(data: LeaveApplyDTO) {
  return request.post<any, R<number>>('/hr/leave/apply', data);
}

export function getLeaveDetailApi(id: number) {
  return request.get<any, R<LeaveVO>>(`/hr/leave/detail/${id}`);
}

export function getMyLeavesApi(params: { pageNum?: number; pageSize?: number }) {
  return request.get<any, R<PageResult<LeaveVO>>>('/hr/leave/my', { params });
}

// ==================== 2. 考勤业务 (HR Attendance) ====================

export function clockInApi(data: ClockInDTO) {
  return request.post<any, R<boolean>>('/hr/attendance/clock-in', data);
}

export function getMyClockRecordsApi(params: { pageNum?: number; pageSize?: number }) {
  return request.get<any, R<PageResult<ClockRecordVO>>>('/hr/attendance/my-records', { params });
}

export function applyRepairApi(data: AttendanceRepairDTO) {
  return request.post<any, R<number>>('/hr/attendance/repair/apply', data);
}

export function getRepairDetailApi(id: number) {
  return request.get<any, R<AttendanceRepairVO>>(`/hr/attendance/repair/detail/${id}`);
}

export function getMyRepairsApi(params: { pageNum?: number; pageSize?: number }) {
  return request.get<any, R<PageResult<AttendanceRepairVO>>>('/hr/attendance/repair/my', { params });
}

// ==================== 3. 用印业务 (ADM Seal) ====================

export function applySealApi(data: SealApplyDTO) {
  return request.post<any, R<number>>('/adm/seal/apply', data);
}

export function getSealDetailApi(id: number) {
  return request.get<any, R<SealApplyVO>>(`/adm/seal/detail/${id}`);
}

export function getMySealsApi(params: { pageNum?: number; pageSize?: number }) {
  return request.get<any, R<PageResult<SealApplyVO>>>('/adm/seal/my', { params });
}

// ==================== 4. 红头公文 (ADM Redhead) ====================

export function draftRedheadApi(data: RedheadDocDTO) {
  return request.post<any, R<number>>('/adm/redhead/draft', data);
}

export function getRedheadDetailApi(id: number) {
  return request.get<any, R<RedheadDocVO>>(`/adm/redhead/detail/${id}`);
}

export function getListRedheadApi(params: { pageNum?: number; pageSize?: number }) {
  return request.get<any, R<PageResult<RedheadDocVO>>>('/adm/redhead/list', { params });
}

// ==================== 5. 档案借阅 (ADM Borrow) ====================

export function applyBorrowApi(data: RecordBorrowDTO) {
  return request.post<any, R<number>>('/adm/borrow/apply', data);
}

export function getBorrowDetailApi(id: number) {
  return request.get<any, R<RecordBorrowVO>>(`/adm/borrow/detail/${id}`);
}

export function getMyBorrowsApi(params: { pageNum?: number; pageSize?: number }) {
  return request.get<any, R<PageResult<RecordBorrowVO>>>('/adm/borrow/my', { params });
}

// ==================== 6. 文件上传 ====================

export function uploadFileApi(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post<any, R<string>>('/common/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}

// ==================== 7. 加班申报 (HR Overtime) ====================

export function applyOvertimeApi(data: OvertimeApplyDTO) {
  return request.post<any, R<number>>('/hr/overtime/apply', data);
}

export function getOvertimeDetailApi(id: number) {
  return request.get<any, R<OvertimeVO>>(`/hr/overtime/detail/${id}`);
}

export function getMyOvertimesApi(params: { pageNum?: number; pageSize?: number }) {
  return request.get<any, R<PageResult<OvertimeVO>>>('/hr/overtime/my', { params });
}

// ==================== 8. 外出公干/外勤报备 (HR Egress) ====================

export function applyEgressApi(data: EgressApplyDTO) {
  return request.post<any, R<number>>('/hr/egress/apply', data);
}

export function getEgressDetailApi(id: number) {
  return request.get<any, R<EgressVO>>(`/hr/egress/detail/${id}`);
}

export function getMyEgressesApi(params: { pageNum?: number; pageSize?: number }) {
  return request.get<any, R<PageResult<EgressVO>>>('/hr/egress/my', { params });
}

// ==================== 9. 月度工资条 (HR Salary) ====================

export function verifySalaryPasswordApi(data: SalaryVerifyDTO) {
  return request.post<any, R<boolean>>('/hr/salary/verify-pwd', data);
}

export function getMySalariesApi(params: { month?: string; pageNum?: number; pageSize?: number }) {
  return request.get<any, R<PageResult<SalaryVO>>>('/hr/salary/my', { params });
}

export function getSalaryDetailApi(id: number) {
  return request.get<any, R<SalaryVO>>(`/hr/salary/detail/${id}`);
}

export function confirmSalaryApi(id: number) {
  return request.put<any, R<boolean>>(`/hr/salary/confirm/${id}`);
}

// ==================== 10. 公文通告与已读追踪 (Collab Notice & Read Tracking) ====================

export function pageNoticesApi(params: { keyword?: string; category?: string; pageNum?: number; pageSize?: number }) {
  return request.get<any, R<PageResult<NoticeVO>>>('/collab/notice/page', { params });
}

export function getNoticeDetailApi(id: number) {
  return request.get<any, R<NoticeVO>>(`/collab/notice/detail/${id}`);
}

export function getNoticeReadStatsApi(id: number) {
  return request.get<any, R<NoticeReadStatsVO>>(`/collab/notice/read-stats/${id}`);
}

export function saveNoticeApi(data: NoticeSaveDTO) {
  return request.post<any, R<number>>('/collab/notice', data);
}

export function deleteNoticeApi(id: number) {
  return request.delete<any, R<boolean>>(`/collab/notice/${id}`);
}

export function remindUnreadUsersApi(id: number) {
  return request.post<any, R<string>>(`/collab/notice/remind/${id}`);
}

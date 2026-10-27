import request from '@/utils/request';
import type { R, PageResult } from '@/types/api';
import type {
  TaskVO,
  ProcessHistoryVO,
  ProcessStartDTO,
  ProcessApproveDTO,
  ProcessRejectDTO,
  ProcessAddSignDTO
} from '@/types/workflow';

export function startProcessApi(data: ProcessStartDTO) {
  return request.post<R<number>>('/wf/process/start', data);
}

export function approveTaskApi(data: ProcessApproveDTO) {
  return request.post<R<void>>('/wf/process/approve', data);
}

export function rejectTaskApi(data: ProcessRejectDTO) {
  return request.post<R<void>>('/wf/process/reject', data);
}

export function addSignTaskApi(data: ProcessAddSignDTO) {
  return request.post<R<void>>('/wf/process/add-sign', data);
}

export function cancelProcessApi(instId: number, reason?: string) {
  return request.post<R<void>>(`/wf/process/cancel/${instId}`, null, {
    params: { reason }
  });
}

export function getPendingTasksApi(pageNum = 1, pageSize = 10) {
  return request.get<R<PageResult<TaskVO>>>('/wf/process/pending', {
    params: { pageNum, pageSize }
  });
}

export function getHandledTasksApi(pageNum = 1, pageSize = 10) {
  return request.get<R<PageResult<TaskVO>>>('/wf/process/handled', {
    params: { pageNum, pageSize }
  });
}

export function getMySubmittedApi(pageNum = 1, pageSize = 10) {
  return request.get<R<PageResult<TaskVO>>>('/wf/process/my-submitted', {
    params: { pageNum, pageSize }
  });
}

export function getProcessHistoryApi(instId: number) {
  return request.get<R<ProcessHistoryVO[]>>(`/wf/process/history/${instId}`);
}

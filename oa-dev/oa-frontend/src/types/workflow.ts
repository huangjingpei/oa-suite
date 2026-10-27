export interface TaskVO {
  taskId: number;
  instId: number;
  procKey: string;
  procName?: string;
  title: string;
  initiatorUsername: string;
  initiatorName: string;
  deptName?: string;
  nodeName: string;
  nodeSeq?: number;
  taskType: 'APPROVE' | 'SIGN' | 'COUNTERSIGN' | 'NOTIFY';
  taskStatus: number; // 1待处理, 2已完成
  instStatus: number; // 0草稿, 1审批中, 2通过, 3驳回, 4作废
  businessTable: string;
  businessId: number;
  createTime: string;
}

export interface ProcessHistoryVO {
  id: number;
  instId: number;
  nodeName: string;
  nodeSeq: number;
  operatorUsername: string;
  operatorName: string;
  action: 'SUBMIT' | 'AGREE' | 'REJECT' | 'ADDSIGN' | 'NOTIFY' | 'CANCEL';
  opinion?: string;
  targetAssignee?: string;
  files?: string;
  createTime: string;
}

export interface ProcessStartDTO {
  procKey: string;
  businessTable: string;
  businessId: number;
  title: string;
  auditUsernames: string[];
  notifyUsernames?: string[];
}

export interface ProcessApproveDTO {
  taskId: number;
  opinion?: string;
  addSignUsername?: string;
  notifyUsername?: string;
}

export interface ProcessRejectDTO {
  taskId: number;
  opinion: string;
}

export interface ProcessAddSignDTO {
  taskId: number;
  targetUsername: string;
  opinion?: string;
}

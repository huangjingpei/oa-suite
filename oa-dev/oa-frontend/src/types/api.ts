export interface R<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export interface PageResult<T = any> {
  records: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

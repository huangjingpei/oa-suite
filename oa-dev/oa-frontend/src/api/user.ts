import request from '@/utils/request';
import type { R } from '@/types/api';
import type { UserVO, DeptNode } from '@/types/user';

export function listUserOptionsApi() {
  return request.get<R<UserVO[]>>('/sys/user/options');
}

export function listDeptTreeApi() {
  return request.get<R<DeptNode[]>>('/sys/dept/tree');
}

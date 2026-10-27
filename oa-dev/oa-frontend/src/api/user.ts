import request from '@/utils/request';
import type { R } from '@/types/api';
import type { UserVO, DeptNode } from '@/types/user';

export function listUserOptionsApi() {
  return request.get<any, R<UserVO[]>>('/sys/user/options');
}

export const getUserListApi = listUserOptionsApi;

export function listDeptTreeApi() {
  return request.get<any, R<DeptNode[]>>('/sys/dept/tree');
}

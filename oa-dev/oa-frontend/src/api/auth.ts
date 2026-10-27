import request from '@/utils/request';
import type { R } from '@/types/api';
import type { LoginDTO, LoginVO, UserVO } from '@/types/user';

export function loginApi(data: LoginDTO) {
  return request.post<R<LoginVO>>('/auth/login', data);
}

export function logoutApi() {
  return request.post<R<void>>('/auth/logout');
}

export function getProfileApi() {
  return request.get<R<UserVO>>('/auth/profile');
}

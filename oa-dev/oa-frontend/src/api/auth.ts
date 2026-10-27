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

export interface PasswordUpdateDTO {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ProfileUpdateDTO {
  realName?: string;
  phone?: string;
  email?: string;
  gender?: number;
  avatar?: string;
}

export function updatePasswordApi(data: PasswordUpdateDTO) {
  return request.put<any, R<boolean>>('/auth/password', data);
}

export function updateProfileApi(data: ProfileUpdateDTO) {
  return request.put<any, R<boolean>>('/auth/profile', data);
}


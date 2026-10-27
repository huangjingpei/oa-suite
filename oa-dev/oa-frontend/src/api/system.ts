import request from '@/utils/request';
import type { R, PageResult } from '@/types/api';
import type { UserVO } from '@/types/user';

// ==================== 1. 部门组织架构 (Dept) ====================

export interface DeptItem {
  id?: number;
  parentId?: number;
  deptCode: string;
  deptName: string;
  leader?: string;
  phone?: string;
  email?: string;
  sortOrder?: number;
  status?: number;
  children?: DeptItem[];
}

export function getDeptTreeApi() {
  return request.get<any, R<DeptItem[]>>('/sys/dept/tree');
}

export function createDeptApi(data: DeptItem) {
  return request.post<any, R<boolean>>('/sys/dept', data);
}

export function updateDeptApi(data: DeptItem) {
  return request.put<any, R<boolean>>('/sys/dept', data);
}

export function deleteDeptApi(id: number) {
  return request.delete<any, R<boolean>>(`/sys/dept/${id}`);
}

// ==================== 2. 员工用户管理 (User) ====================

export interface UserCreateParams {
  username: string;
  realName: string;
  password?: string;
  workNo?: string;
  deptId?: number;
  postName?: string;
  email?: string;
  phone?: string;
  gender?: number;
  roleIds?: number[];
}

export interface UserUpdateParams {
  id: number;
  realName?: string;
  workNo?: string;
  deptId?: number;
  postName?: string;
  email?: string;
  phone?: string;
  gender?: number;
  status?: number;
  roleIds?: number[];
}

export function getUserPageApi(params: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  deptId?: number;
  status?: number;
}) {
  return request.get<any, R<PageResult<UserVO>>>('/sys/user/page', { params });
}

export function createUserApi(data: UserCreateParams) {
  return request.post<any, R<boolean>>('/sys/user', data);
}

export function updateUserApi(data: UserUpdateParams) {
  return request.put<any, R<boolean>>('/sys/user', data);
}

export function resetUserPasswordApi(id: number) {
  return request.put<any, R<boolean>>(`/sys/user/reset-password/${id}`);
}

export function changeUserStatusApi(id: number, status: number) {
  return request.put<any, R<boolean>>(`/sys/user/status/${id}`, null, { params: { status } });
}

export function deleteUserApi(id: number) {
  return request.delete<any, R<boolean>>(`/sys/user/${id}`);
}

export function getAddressBookApi(params?: { deptId?: number; keyword?: string }) {
  return request.get<any, R<UserVO[]>>('/sys/user/address-book', { params });
}

// ==================== 3. 角色与权限体系 (Role) ====================

export interface RoleItem {
  id?: number;
  roleCode: string;
  roleName: string;
  description?: string;
  sortOrder?: number;
  status?: number;
}

export function listRolesApi() {
  return request.get<any, R<RoleItem[]>>('/sys/role/list');
}

export function createRoleApi(data: RoleItem) {
  return request.post<any, R<boolean>>('/sys/role', data);
}

export function updateRoleApi(data: RoleItem) {
  return request.put<any, R<boolean>>('/sys/role', data);
}

export function deleteRoleApi(id: number) {
  return request.delete<any, R<boolean>>(`/sys/role/${id}`);
}

// ==================== 4. 数据字典中心 (Dict) ====================

export interface DictTypeItem {
  id?: number;
  dictName: string;
  dictType: string;
  status?: number;
  remark?: string;
  createTime?: string;
}

export interface DictDataItem {
  id?: number;
  dictType: string;
  dictLabel: string;
  dictValue: string;
  sortOrder?: number;
  status?: number;
  remark?: string;
}

export function listDictTypesApi() {
  return request.get<any, R<DictTypeItem[]>>('/sys/dict/type/list');
}

export function createDictTypeApi(data: DictTypeItem) {
  return request.post<any, R<boolean>>('/sys/dict/type', data);
}

export function updateDictTypeApi(data: DictTypeItem) {
  return request.put<any, R<boolean>>('/sys/dict/type', data);
}

export function deleteDictTypeApi(id: number) {
  return request.delete<any, R<boolean>>(`/sys/dict/type/${id}`);
}

export function listDictDataByTypeApi(dictType: string) {
  return request.get<any, R<DictDataItem[]>>(`/sys/dict/data/type/${dictType}`);
}

export function createDictDataApi(data: DictDataItem) {
  return request.post<any, R<boolean>>('/sys/dict/data', data);
}

export function updateDictDataApi(data: DictDataItem) {
  return request.put<any, R<boolean>>('/sys/dict/data', data);
}

export function deleteDictDataApi(id: number) {
  return request.delete<any, R<boolean>>(`/sys/dict/data/${id}`);
}

// ==================== 5. 系统菜单与操作权限 (Permission) ====================

export interface PermissionTreeItem {
  id: number;
  parentId: number;
  name: string;
  perms?: string;
  menuType: number; // 1: 目录, 2: 菜单, 3: 按钮
  icon?: string;
  sortOrder: number;
  status: number;
  children?: PermissionTreeItem[];
}

export interface RolePermissionAssignDTO {
  roleId: number;
  permissionIds: number[];
}

export function getPermissionTreeApi() {
  return request.get<any, R<PermissionTreeItem[]>>('/sys/permission/tree');
}

export function getRolePermissionIdsApi(roleId: number) {
  return request.get<any, R<number[]>>(`/sys/permission/role/${roleId}`);
}

export function assignRolePermissionsApi(data: RolePermissionAssignDTO) {
  return request.post<any, R<boolean>>('/sys/permission/assign', data);
}


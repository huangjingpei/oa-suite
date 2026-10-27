export interface LoginDTO {
  username: string;
  password: string;
}

export interface LoginVO {
  token: string;
  tokenName: string;
  userId: number;
  username: string;
  realName: string;
  avatar?: string;
  deptId?: number;
  deptName?: string;
  roles: string[];
  permissions?: string[];
}

export interface UserVO {
  id: number;
  username: string;
  realName: string;
  workNo?: string;
  deptId?: number;
  deptName?: string;
  postName?: string;
  email?: string;
  phone?: string;
  gender?: number;
  avatar?: string;
  status: number;
  createTime?: string;
  roleIds?: number[];
  roleNames?: string[];
  permissions?: string[];
}

export interface DeptNode {
  id: number;
  parentId: number;
  deptCode: string;
  deptName: string;
  leader?: string;
  phone?: string;
  email?: string;
  sortOrder: number;
  status: number;
}

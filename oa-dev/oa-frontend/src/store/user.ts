import { defineStore } from 'pinia';
import { loginApi, logoutApi, getProfileApi } from '@/api/auth';
import type { LoginDTO, UserVO } from '@/types/user';

interface UserState {
  token: string;
  userInfo: UserVO | null;
  roles: string[];
  permissions: string[];
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('oa_token') || '',
    userInfo: JSON.parse(localStorage.getItem('oa_user') || 'null'),
    roles: JSON.parse(localStorage.getItem('oa_roles') || '[]'),
    permissions: JSON.parse(localStorage.getItem('oa_perms') || '[]')
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    displayName: (state) => state.userInfo?.realName || state.userInfo?.username || '用户',
    hasPermission: (state) => (perm: string) => {
      if (state.roles.includes('ROLE_ADMIN') || state.permissions.includes('*')) return true;
      return state.permissions.includes(perm);
    }
  },
  actions: {
    async login(dto: LoginDTO) {
      const res = await loginApi(dto);
      const data = res.data.data;
      this.token = data.token;
      this.roles = data.roles || [];
      this.permissions = data.permissions || [];
      localStorage.setItem('oa_token', data.token);
      localStorage.setItem('oa_roles', JSON.stringify(this.roles));
      localStorage.setItem('oa_perms', JSON.stringify(this.permissions));

      await this.fetchProfile();
      return data;
    },
    async fetchProfile() {
      try {
        const res = await getProfileApi();
        this.userInfo = res.data.data;
        if (this.userInfo?.permissions) {
          this.permissions = this.userInfo.permissions;
          localStorage.setItem('oa_perms', JSON.stringify(this.permissions));
        }
        localStorage.setItem('oa_user', JSON.stringify(this.userInfo));
      } catch (e) {
        console.error('获取个人信息失败', e);
      }
    },
    async logout() {
      try {
        await logoutApi();
      } catch (ignored) {
      } finally {
        this.token = '';
        this.userInfo = null;
        this.roles = [];
        this.permissions = [];
        localStorage.removeItem('oa_token');
        localStorage.removeItem('oa_user');
        localStorage.removeItem('oa_roles');
        localStorage.removeItem('oa_perms');
      }
    }
  }
});

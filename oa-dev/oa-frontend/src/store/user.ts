import { defineStore } from 'pinia';
import { loginApi, logoutApi, getProfileApi } from '@/api/auth';
import type { LoginDTO, UserVO } from '@/types/user';

interface UserState {
  token: string;
  userInfo: UserVO | null;
  roles: string[];
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('oa_token') || '',
    userInfo: JSON.parse(localStorage.getItem('oa_user') || 'null'),
    roles: []
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    displayName: (state) => state.userInfo?.realName || state.userInfo?.username || '用户'
  },
  actions: {
    async login(dto: LoginDTO) {
      const res = await loginApi(dto);
      const data = res.data.data;
      this.token = data.token;
      this.roles = data.roles || [];
      localStorage.setItem('oa_token', data.token);

      await this.fetchProfile();
      return data;
    },
    async fetchProfile() {
      try {
        const res = await getProfileApi();
        this.userInfo = res.data.data;
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
        localStorage.removeItem('oa_token');
        localStorage.removeItem('oa_user');
      }
    }
  }
});

import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { message } from 'ant-design-vue';
import type { R } from '@/types/api';

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('oa_token');
    if (token) {
      config.headers['Authorization'] = token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<R>) => {
    const res = response.data;
    // 后端返回标准 ResultCode.SUCCESS = 200
    if (res.code === 200) {
      const payload: any = res;
      payload.status = response.status;
      payload.headers = response.headers;
      // 兼容按 response.data.data 访问
      if (res.data && typeof res.data === 'object' && !('data' in (res.data as any))) {
        try {
          Object.defineProperty(res.data, 'data', {
            get() {
              return res.data;
            },
            configurable: true,
            enumerable: false
          });
        } catch (_) {}
      }
      return payload;
    }

    // 401 认证过期
    if (res.code === 401) {
      message.error(res.message || '登录已过期，请重新登录');
      localStorage.removeItem('oa_token');
      localStorage.removeItem('oa_user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
      return Promise.reject(new Error(res.message || 'Error'));
    }

    message.error(res.message || '系统业务处理异常');
    return Promise.reject(new Error(res.message || 'Error'));
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      message.error('登录状态已失效，请重新登录');
      localStorage.removeItem('oa_token');
      localStorage.removeItem('oa_user');
      window.location.href = '/login';
    } else {
      message.error(error.message || '网络连接超时或后端服务未就绪');
    }
    return Promise.reject(error);
  }
);

export default request;

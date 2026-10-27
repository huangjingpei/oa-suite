import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import BasicLayout from '@/layouts/BasicLayout.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '企业登录门户 - OA Suite' }
  },
  {
    path: '/',
    component: BasicLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台与审批中枢' }
      },
      // 业务中心
      {
        path: 'business/leave',
        name: 'LeaveManagement',
        component: () => import('@/views/business/leave/index.vue'),
        meta: { title: '员工请假与假期台账' }
      },
      {
        path: 'business/attendance',
        name: 'AttendanceManagement',
        component: () => import('@/views/business/attendance/index.vue'),
        meta: { title: '考勤打卡与漏卡补卡' }
      },
      {
        path: 'business/seal',
        name: 'SealManagement',
        component: () => import('@/views/business/seal/index.vue'),
        meta: { title: '企业印章使用审批' }
      },
      {
        path: 'business/redhead',
        name: 'RedheadManagement',
        component: () => import('@/views/business/redhead/index.vue'),
        meta: { title: '红头公文拟发与大厅' }
      },
      {
        path: 'business/borrow',
        name: 'BorrowManagement',
        component: () => import('@/views/business/borrow/index.vue'),
        meta: { title: '档案资料借阅台账' }
      },
      // 系统组织管理
      {
        path: 'system/dept',
        name: 'DeptManagement',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '组织架构与部门管理' }
      },
      {
        path: 'system/user',
        name: 'UserManagement',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '员工与用户管理' }
      },
      {
        path: 'system/role',
        name: 'RoleManagement',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色与权限体系 (RBAC)' }
      },
      {
        path: 'system/dict',
        name: 'DictManagement',
        component: () => import('@/views/system/dict/index.vue'),
        meta: { title: '系统数据字典中心' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 全局路由守卫
router.beforeEach((to, _from, next) => {
  if (to.meta.title) {
    document.title = `${to.meta.title} | 盛泰协同办公平台`;
  }
  const token = localStorage.getItem('oa_token');
  if (to.path !== '/login' && !token) {
    next('/login');
  } else if (to.path === '/login' && token) {
    next('/dashboard');
  } else {
    next();
  }
});

export default router;

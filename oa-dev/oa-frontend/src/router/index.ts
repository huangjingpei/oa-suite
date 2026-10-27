import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import BasicLayout from '@/layouts/BasicLayout.vue';
import MobileLayout from '@/layouts/MobileLayout.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '企业登录门户 - OA Suite' }
  },
  // 电脑端后台路由 (PC Web)
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
      {
        path: 'business/salary',
        name: 'SalaryManagement',
        component: () => import('@/views/business/salary/index.vue'),
        meta: { title: '月度工资条安全中心' }
      },
      // 系统组织管理
      {
        path: 'system/address',
        name: 'AddressBook',
        component: () => import('@/views/system/address/index.vue'),
        meta: { title: '企业全员通讯录' }
      },
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
  // 移动端 H5 专版路由 (Mobile H5)
  {
    path: '/m',
    component: MobileLayout,
    redirect: '/m/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'MobileDashboard',
        component: () => import('@/views/mobile/dashboard/index.vue'),
        meta: { title: '移动协同工作台' }
      },
      {
        path: 'clock',
        name: 'MobileClock',
        component: () => import('@/views/mobile/clock/index.vue'),
        meta: { title: '移动考勤打卡' }
      },
      {
        path: 'todo',
        name: 'MobileTodo',
        component: () => import('@/views/mobile/todo/index.vue'),
        meta: { title: '移动待办审批' }
      },
      {
        path: 'profile',
        name: 'MobileProfile',
        component: () => import('@/views/mobile/profile/index.vue'),
        meta: { title: '个人信息中心' }
      },
      {
        path: 'apply/leave',
        name: 'MobileApplyLeave',
        component: () => import('@/views/mobile/apply/leave.vue'),
        meta: { title: '移动快速请假' }
      },
      {
        path: 'apply/seal',
        name: 'MobileApplySeal',
        component: () => import('@/views/mobile/apply/seal.vue'),
        meta: { title: '移动快速用印' }
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

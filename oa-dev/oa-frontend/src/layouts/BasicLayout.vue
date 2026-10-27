<template>
  <a-layout class="basic-layout">
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      theme="light"
      class="layout-sider"
    >
      <div class="logo">
        <span class="logo-icon">🏢</span>
        <span v-if="!collapsed" class="logo-text">盛泰 OA 协同平台</span>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="inline"
        theme="light"
        :items="menuItems"
        @click="handleMenuClick"
      />
    </a-layout-sider>

    <a-layout>
      <a-layout-header class="layout-header">
        <div class="header-left">
          <component
            :is="collapsed ? MenuUnfoldOutlined : MenuFoldOutlined"
            class="trigger-icon"
            @click="() => (collapsed = !collapsed)"
          />
          <a-breadcrumb class="breadcrumb">
            <a-breadcrumb-item>企业协同平台</a-breadcrumb-item>
            <a-breadcrumb-item>工作台与审批中枢</a-breadcrumb-item>
          </a-breadcrumb>
        </div>

        <div class="header-right">
          <a-button type="link" size="small" @click="router.push('/m/dashboard')">
            <MobileOutlined /> 移动端H5视图
          </a-button>
          <a-tag v-if="userStore.userInfo?.deptName" color="blue">
            {{ userStore.userInfo?.deptName }}
          </a-tag>
          <a-dropdown>
            <span class="user-action">
              <a-avatar style="background-color: #1677ff; margin-right: 8px">
                {{ userStore.displayName.charAt(0) }}
              </a-avatar>
              <span class="username">{{ userStore.displayName }}</span>
            </span>
            <template #overlay>
              <a-menu>
                <a-menu-item key="user-info" disabled>
                  工号：{{ userStore.userInfo?.workNo || 'OA0000' }}
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" @click="handleLogout">
                  <LogoutOutlined /> 退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <a-layout-content class="layout-content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, h, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/store/user';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DashboardOutlined,
  FileDoneOutlined,
  CalendarOutlined,
  ClockCircleOutlined,
  AuditOutlined,
  ReadOutlined,
  SettingOutlined,
  ApartmentOutlined,
  TeamOutlined,
  SafetyCertificateOutlined,
  BookOutlined,
  MobileOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const collapsed = ref<boolean>(false);
const selectedKeys = ref<string[]>(['dashboard']);

const menuItems = ref([
  {
    key: 'dashboard',
    icon: () => h(DashboardOutlined),
    label: '协同工作台'
  },
  {
    key: 'leave',
    icon: () => h(CalendarOutlined),
    label: '请假与假期台账'
  },
  {
    key: 'attendance',
    icon: () => h(ClockCircleOutlined),
    label: '考勤打卡与补卡'
  },
  {
    key: 'seal',
    icon: () => h(AuditOutlined),
    label: '企业用印审批'
  },
  {
    key: 'redhead',
    icon: () => h(FileDoneOutlined),
    label: '红头公文发文'
  },
  {
    key: 'borrow',
    icon: () => h(ReadOutlined),
    label: '档案资料借阅'
  },
  {
    key: 'system',
    icon: () => h(SettingOutlined),
    label: '组织与系统管理',
    children: [
      {
        key: 'dept',
        icon: () => h(ApartmentOutlined),
        label: '部门架构维护'
      },
      {
        key: 'user',
        icon: () => h(TeamOutlined),
        label: '员工用户管理'
      },
      {
        key: 'role',
        icon: () => h(SafetyCertificateOutlined),
        label: '角色权限体系'
      },
      {
        key: 'dict',
        icon: () => h(BookOutlined),
        label: '数据字典中心'
      }
    ]
  }
]);

watch(
  () => route.path,
  (path) => {
    if (path.includes('business/leave')) selectedKeys.value = ['leave'];
    else if (path.includes('business/attendance')) selectedKeys.value = ['attendance'];
    else if (path.includes('business/seal')) selectedKeys.value = ['seal'];
    else if (path.includes('business/redhead')) selectedKeys.value = ['redhead'];
    else if (path.includes('business/borrow')) selectedKeys.value = ['borrow'];
    else if (path.includes('system/dept')) selectedKeys.value = ['dept'];
    else if (path.includes('system/user')) selectedKeys.value = ['user'];
    else if (path.includes('system/role')) selectedKeys.value = ['role'];
    else if (path.includes('system/dict')) selectedKeys.value = ['dict'];
    else selectedKeys.value = ['dashboard'];
  },
  { immediate: true }
);

const handleMenuClick = ({ key }: { key: string }) => {
  if (key === 'dashboard') {
    router.push('/dashboard');
  } else if (key === 'leave') {
    router.push('/business/leave');
  } else if (key === 'attendance') {
    router.push('/business/attendance');
  } else if (key === 'seal') {
    router.push('/business/seal');
  } else if (key === 'redhead') {
    router.push('/business/redhead');
  } else if (key === 'borrow') {
    router.push('/business/borrow');
  } else if (key === 'dept') {
    router.push('/system/dept');
  } else if (key === 'user') {
    router.push('/system/user');
  } else if (key === 'role') {
    router.push('/system/role');
  } else if (key === 'dict') {
    router.push('/system/dict');
  }
};

const handleLogout = async () => {
  await userStore.logout();
  message.success('已安全退出登录');
  router.push('/login');
};
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
}

.layout-sider {
  box-shadow: 2px 0 8px 0 rgba(29, 35, 41, 0.05);
  border-right: 1px solid #f0f0f0;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #1f1f1f;
  white-space: nowrap;
}

.layout-header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.trigger-icon {
  font-size: 18px;
  cursor: pointer;
  transition: color 0.3s;
}

.trigger-icon:hover {
  color: #1677ff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-action {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.username {
  font-size: 14px;
  color: #333;
}

.layout-content {
  margin: 16px;
  padding: 0;
  min-height: 280px;
}
</style>

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
        <span v-if="!collapsed" class="logo-text">启航 OA 协同平台</span>
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
                <a-menu-item key="profile" @click="openProfileModal">
                  <UserOutlined /> 个人基本资料
                </a-menu-item>
                <a-menu-item key="password" @click="openPasswordModal">
                  <KeyOutlined /> 修改登录密码
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

    <!-- 个人基本资料修改弹窗 -->
    <a-modal
      v-model:open="profileModalVisible"
      title="个人基本资料设置"
      width="520px"
      :confirm-loading="savingProfile"
      @ok="handleSaveProfile"
    >
      <a-form layout="vertical" style="margin-top: 16px">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="登录账号">
              <a-input :value="userStore.userInfo?.username" disabled />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="员工工号">
              <a-input :value="userStore.userInfo?.workNo || '未分配'" disabled />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="所属部门">
              <a-input :value="userStore.userInfo?.deptName || '未归属'" disabled />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="岗位职务">
              <a-input :value="userStore.userInfo?.postName || '标准职级'" disabled />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="真实姓名" required>
          <a-input v-model:value="profileForm.realName" placeholder="请输入您的真实姓名" />
        </a-form-item>

        <a-form-item label="联系电话">
          <a-input v-model:value="profileForm.phone" placeholder="请输入11位手机号码" />
        </a-form-item>

        <a-form-item label="电子邮箱">
          <a-input v-model:value="profileForm.email" placeholder="请输入企业邮箱或常用邮箱" />
        </a-form-item>

        <a-form-item label="性别">
          <a-radio-group v-model:value="profileForm.gender">
            <a-radio :value="1">男</a-radio>
            <a-radio :value="2">女</a-radio>
            <a-radio :value="0">保密</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 修改个人密码弹窗 -->
    <a-modal
      v-model:open="passwordModalVisible"
      title="修改个人登录密码"
      width="460px"
      :confirm-loading="savingPassword"
      @ok="handleSavePassword"
    >
      <a-alert
        message="安全须知"
        description="新密码长度需在6-32位之间，请勿与原密码相同；修改成功后请牢记新密码。"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />
      <a-form layout="vertical">
        <a-form-item label="当前原密码" required>
          <a-input-password
            v-model:value="passwordForm.oldPassword"
            placeholder="请输入当前生效的原登录密码"
          />
        </a-form-item>

        <a-form-item label="设置新密码" required>
          <a-input-password
            v-model:value="passwordForm.newPassword"
            placeholder="请输入6-32位的新密码"
          />
        </a-form-item>

        <a-form-item label="确认新密码" required>
          <a-input-password
            v-model:value="passwordForm.confirmPassword"
            placeholder="请再次输入新密码"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, reactive, h, watch } from 'vue';
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
  LogoutOutlined,
  UserOutlined,
  KeyOutlined,
  ContactsOutlined,
  DollarOutlined,
  NotificationOutlined,
  RobotOutlined
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { updatePasswordApi, updateProfileApi } from '@/api/auth';

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
    key: 'salary',
    icon: () => h(DollarOutlined),
    label: '我的月度工资条'
  },
  {
    key: 'notice',
    icon: () => h(NotificationOutlined),
    label: '公文通告与追踪'
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
      },
      {
        key: 'notify',
        icon: () => h(RobotOutlined),
        label: '消息提醒机器人'
      }
    ]
  },
  {
    key: 'address',
    icon: () => h(ContactsOutlined),
    label: '企业全员通讯录'
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
    else if (path.includes('business/salary')) selectedKeys.value = ['salary'];
    else if (path.includes('business/notice')) selectedKeys.value = ['notice'];
    else if (path.includes('system/address')) selectedKeys.value = ['address'];
    else if (path.includes('system/dept')) selectedKeys.value = ['dept'];
    else if (path.includes('system/user')) selectedKeys.value = ['user'];
    else if (path.includes('system/role')) selectedKeys.value = ['role'];
    else if (path.includes('system/dict')) selectedKeys.value = ['dict'];
    else if (path.includes('system/notify')) selectedKeys.value = ['notify'];
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
  } else if (key === 'salary') {
    router.push('/business/salary');
  } else if (key === 'notice') {
    router.push('/business/notice');
  } else if (key === 'address') {
    router.push('/system/address');
  } else if (key === 'dept') {
    router.push('/system/dept');
  } else if (key === 'user') {
    router.push('/system/user');
  } else if (key === 'role') {
    router.push('/system/role');
  } else if (key === 'dict') {
    router.push('/system/dict');
  } else if (key === 'notify') {
    router.push('/system/notify');
  }
};

// 个人资料设置
const profileModalVisible = ref(false);
const savingProfile = ref(false);
const profileForm = reactive({
  realName: '',
  phone: '',
  email: '',
  gender: 1
});

const openProfileModal = () => {
  profileForm.realName = userStore.userInfo?.realName || '';
  profileForm.phone = userStore.userInfo?.phone || '';
  profileForm.email = userStore.userInfo?.email || '';
  profileForm.gender = userStore.userInfo?.gender ?? 1;
  profileModalVisible.value = true;
};

const handleSaveProfile = async () => {
  if (!profileForm.realName.trim()) {
    message.warning('真实姓名不能为空');
    return;
  }
  try {
    savingProfile.value = true;
    const res = await updateProfileApi(profileForm);
    if (res.code === 200) {
      message.success('个人基本资料已成功更新');
      await userStore.fetchProfile();
      profileModalVisible.value = false;
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  } finally {
    savingProfile.value = false;
  }
};

// 修改密码
const passwordModalVisible = ref(false);
const savingPassword = ref(false);
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const openPasswordModal = () => {
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  passwordModalVisible.value = true;
};

const handleSavePassword = async () => {
  if (!passwordForm.oldPassword) {
    message.warning('请输入当前原密码');
    return;
  }
  if (!passwordForm.newPassword || passwordForm.newPassword.length < 6) {
    message.warning('新密码长度不能少于6位');
    return;
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.warning('两次输入的新密码不一致');
    return;
  }
  if (passwordForm.oldPassword === passwordForm.newPassword) {
    message.warning('新密码不能与当前原密码相同');
    return;
  }
  try {
    savingPassword.value = true;
    const res = await updatePasswordApi(passwordForm);
    if (res.code === 200) {
      message.success('密码修改成功，请牢记您的新密码');
      passwordModalVisible.value = false;
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  } finally {
    savingPassword.value = false;
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

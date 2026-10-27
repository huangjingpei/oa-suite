<template>
  <div class="mobile-profile-page">
    <!-- 用户个人名片卡 -->
    <div class="profile-card">
      <a-avatar :size="64" style="background-color: #1677ff; font-size: 24px">
        {{ userStore.displayName.charAt(0) }}
      </a-avatar>
      <div class="profile-name">{{ userStore.displayName }}</div>
      <div class="profile-dept">{{ userStore.userInfo?.deptName || '启航协同' }} · {{ userStore.userInfo?.postName || '员工' }}</div>
      <div class="profile-workno">工号：{{ userStore.userInfo?.workNo || 'OA1001' }}</div>
    </div>

    <!-- 功能列表 -->
    <a-card :bordered="false" class="menu-card" style="margin-top: 14px">
      <a-list item-layout="horizontal">
        <a-list-item class="menu-item" @click="router.push('/dashboard')">
          <a-list-item-meta title="💻 切换至电脑端完整版工作台" description="进入支持高保真打印与大屏审批的总控台" />
          <RightOutlined />
        </a-list-item>

        <a-list-item class="menu-item" @click="router.push('/m/apply/leave')">
          <a-list-item-meta title="📝 移动端发起请假申请" description="快速发起事假、病假、年假等" />
          <RightOutlined />
        </a-list-item>

        <a-list-item class="menu-item" @click="router.push('/m/apply/seal')">
          <a-list-item-meta title="📑 移动端发起用印申请" description="快速加盖企业公章与合同章" />
          <RightOutlined />
        </a-list-item>

        <a-list-item class="menu-item" @click="router.push('/m/clock')">
          <a-list-item-meta title="⏱️ 移动定位考勤打卡" description="手机快速签到打卡记录" />
          <RightOutlined />
        </a-list-item>
      </a-list>
    </a-card>

    <!-- 退出登录按钮 -->
    <div class="logout-wrapper">
      <a-button danger block size="large" @click="handleLogout">
        <template #icon><LogoutOutlined /></template>
        安全退出登录
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { message } from 'ant-design-vue';
import {
  RightOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue';

const router = useRouter();
const userStore = useUserStore();

const handleLogout = async () => {
  await userStore.logout();
  message.success('已安全退出');
  router.push('/login');
};
</script>

<style scoped>
.mobile-profile-page {
  padding-bottom: 20px;
}

.profile-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px 16px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.profile-name {
  font-size: 18px;
  font-weight: 600;
  color: #1f1f1f;
  margin-top: 10px;
}

.profile-dept {
  font-size: 13px;
  color: #666;
  margin-top: 4px;
}

.profile-workno {
  font-size: 12px;
  color: #999;
  font-family: Consolas, monospace;
  margin-top: 4px;
}

.menu-card {
  border-radius: 12px;
}

.menu-item {
  cursor: pointer;
  padding: 12px 0;
}

.menu-item:active {
  background-color: #fafafa;
}

.logout-wrapper {
  margin-top: 24px;
}
</style>

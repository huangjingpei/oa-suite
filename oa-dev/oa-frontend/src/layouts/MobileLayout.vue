<template>
  <div class="mobile-wrapper">
    <div class="mobile-container">
      <!-- 移动端顶部导航栏 -->
      <header class="mobile-header">
        <div class="header-left">
          <LeftOutlined v-if="showBackButton" class="back-btn" @click="goBack" />
          <span class="header-title">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <a-tag color="blue" style="margin-right: 0">
            {{ userStore.userInfo?.deptName || '盛泰协同' }}
          </a-tag>
        </div>
      </header>

      <!-- 移动端视图内容主区域 -->
      <main class="mobile-content">
        <router-view />
      </main>

      <!-- 移动端底部 TabBar 导航栏 -->
      <footer v-if="showTabBar" class="mobile-tabbar">
        <div
          class="tab-item"
          :class="{ active: currentTab === 'dashboard' }"
          @click="navigate('/m/dashboard')"
        >
          <HomeOutlined class="tab-icon" />
          <span class="tab-label">工作台</span>
        </div>

        <div
          class="tab-item"
          :class="{ active: currentTab === 'clock' }"
          @click="navigate('/m/clock')"
        >
          <ClockCircleOutlined class="tab-icon" />
          <span class="tab-label">打卡考勤</span>
        </div>

        <div
          class="tab-item"
          :class="{ active: currentTab === 'todo' }"
          @click="navigate('/m/todo')"
        >
          <a-badge :count="pendingCount" :offset="[6, -4]" size="small">
            <CheckCircleOutlined class="tab-icon" />
          </a-badge>
          <span class="tab-label">待办审批</span>
        </div>

        <div
          class="tab-item"
          :class="{ active: currentTab === 'profile' }"
          @click="navigate('/m/profile')"
        >
          <UserOutlined class="tab-icon" />
          <span class="tab-label">我的信息</span>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/store/user';
import {
  HomeOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  UserOutlined,
  LeftOutlined
} from '@ant-design/icons-vue';
import { getPendingTasksApi } from '@/api/workflow';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const pendingCount = ref<number>(0);

const pageTitle = computed(() => {
  return (route.meta?.title as string) || '移动端协同办公';
});

const showBackButton = computed(() => {
  return route.path !== '/m/dashboard' &&
         route.path !== '/m/clock' &&
         route.path !== '/m/todo' &&
         route.path !== '/m/profile';
});

const showTabBar = computed(() => {
  return route.path === '/m/dashboard' ||
         route.path === '/m/clock' ||
         route.path === '/m/todo' ||
         route.path === '/m/profile';
});

const currentTab = computed(() => {
  if (route.path.includes('/m/clock')) return 'clock';
  if (route.path.includes('/m/todo')) return 'todo';
  if (route.path.includes('/m/profile')) return 'profile';
  return 'dashboard';
});

const navigate = (path: string) => {
  router.push(path);
};

const goBack = () => {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push('/m/dashboard');
  }
};

onMounted(async () => {
  try {
    const res: any = await getPendingTasksApi(1, 5);
    pendingCount.value = res?.data?.total || res?.data?.data?.total || 0;
  } catch {}
});
</script>

<style scoped>
.mobile-wrapper {
  min-height: 100vh;
  background-color: #f0f2f5;
  display: flex;
  justify-content: center;
}

.mobile-container {
  width: 100%;
  max-width: 480px;
  min-height: 100vh;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.08);
  position: relative;
}

.mobile-header {
  height: 50px;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.back-btn {
  font-size: 16px;
  cursor: pointer;
  color: #333;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f1f1f;
}

.mobile-content {
  flex: 1;
  padding: 14px;
  padding-bottom: 70px;
  background-color: #f7f8fa;
  overflow-y: auto;
}

.mobile-tabbar {
  position: fixed;
  bottom: 0;
  width: 100%;
  max-width: 480px;
  height: 56px;
  background: #ffffff;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  z-index: 100;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  color: #8c8c8c;
  transition: all 0.2s;
  flex: 1;
}

.tab-item.active {
  color: #1677ff;
}

.tab-icon {
  font-size: 18px;
  margin-bottom: 2px;
}

.tab-label {
  font-size: 11px;
}
</style>

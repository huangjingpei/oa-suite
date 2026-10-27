<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-circle">🏢</div>
        <h2>盛泰 OA 协同办公平台</h2>
        <p class="subtitle">新一代企业数字化协同与工作流中枢</p>
      </div>

      <a-form :model="loginForm" :rules="rules" @finish="handleLogin" layout="vertical">
        <a-form-item name="username" label="登录账号">
          <a-input
            v-model:value="loginForm.username"
            size="large"
            placeholder="请输入用户名/工号 (如 admin, dev_mgr)"
          >
            <template #prefix>
              <UserOutlined style="color: rgba(0, 0, 0, 0.25)" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item name="password" label="登录密码">
          <a-input-password
            v-model:value="loginForm.password"
            size="large"
            placeholder="请输入密码 (默认 123456)"
          >
            <template #prefix>
              <LockOutlined style="color: rgba(0, 0, 0, 0.25)" />
            </template>
          </a-input-password>
        </a-form-item>

        <div class="quick-login">
          <span class="quick-label">快捷切换预设账号：</span>
          <div class="tags">
            <a-tag color="blue" class="clickable-tag" @click="quickFill('admin')">超管 (admin)</a-tag>
            <a-tag color="green" class="clickable-tag" @click="quickFill('dev_mgr')">主管 (dev_mgr)</a-tag>
            <a-tag color="purple" class="clickable-tag" @click="quickFill('hr_mgr')">人事 (hr_mgr)</a-tag>
            <a-tag color="orange" class="clickable-tag" @click="quickFill('zhangsan')">员工 (zhangsan)</a-tag>
          </div>
        </div>

        <a-form-item style="margin-top: 24px">
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            block
            :loading="loading"
          >
            登 录
          </a-button>
        </a-form-item>
      </a-form>

      <div class="footer-tips">
        基于 Spring Boot 3 + Vue 3 + Ant Design Vue 4 现代架构打造
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';

const router = useRouter();
const userStore = useUserStore();

const loading = ref(false);

const loginForm = reactive({
  username: 'admin',
  password: '123456'
});

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

const quickFill = (username: string) => {
  loginForm.username = username;
  loginForm.password = '123456';
};

const handleLogin = async () => {
  loading.value = true;
  try {
    await userStore.login(loginForm);
    message.success('登录成功，欢迎进入办公平台！');
    router.push('/dashboard');
  } catch (error) {
    // 错误在 request 拦截器中统一 toast
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f2f5 0%, #e6f7ff 100%);
  padding: 24px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  padding: 40px 32px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.logo-circle {
  font-size: 36px;
  margin-bottom: 8px;
}

.login-header h2 {
  font-size: 22px;
  color: #1f1f1f;
  margin-bottom: 6px;
  font-weight: 600;
}

.subtitle {
  color: #8c8c8c;
  font-size: 13px;
  margin: 0;
}

.quick-login {
  margin-top: 12px;
  margin-bottom: 12px;
}

.quick-label {
  font-size: 12px;
  color: #8c8c8c;
  display: block;
  margin-bottom: 6px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.clickable-tag {
  cursor: pointer;
  user-select: none;
}

.clickable-tag:hover {
  opacity: 0.8;
}

.footer-tips {
  margin-top: 16px;
  text-align: center;
  font-size: 12px;
  color: #bfbfbf;
}
</style>

<template>
  <div class="mobile-dashboard">
    <!-- 用户欢迎横幅 -->
    <div class="welcome-card">
      <div class="welcome-content">
        <a-avatar size="large" style="background-color: #1677ff">
          {{ userStore.displayName.charAt(0) }}
        </a-avatar>
        <div class="user-meta">
          <div class="user-name">{{ userStore.displayName }}</div>
          <div class="user-role">{{ userStore.userInfo?.deptName || '启航协同' }} · {{ userStore.userInfo?.postName || '员工' }}</div>
        </div>
      </div>
      <div class="welcome-badge">
        <a-tag color="processing">移动端 H5 模式</a-tag>
      </div>
    </div>

    <!-- 常用业务服务快捷九宫格 -->
    <a-card :bordered="false" class="section-card" title="⚡ 移动快捷协同">
      <div class="service-grid">
        <div class="service-item" @click="router.push('/m/apply/leave')">
          <div class="icon-circle bg-blue">
            <CalendarOutlined />
          </div>
          <span class="service-name">请假申请</span>
        </div>

        <div class="service-item" @click="router.push('/m/apply/seal')">
          <div class="icon-circle bg-purple">
            <AuditOutlined />
          </div>
          <span class="service-name">用印申请</span>
        </div>

        <div class="service-item" @click="router.push('/m/clock')">
          <div class="icon-circle bg-green">
            <ClockCircleOutlined />
          </div>
          <span class="service-name">考勤打卡</span>
        </div>

        <div class="service-item" @click="router.push('/m/todo')">
          <div class="icon-circle bg-orange">
            <CheckCircleOutlined />
          </div>
          <span class="service-name">待办审批</span>
        </div>
      </div>
    </a-card>

    <!-- 待办审批待处理卡片 -->
    <a-card :bordered="false" class="section-card" style="margin-top: 14px">
      <template #title>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>📋 我的待办审批</span>
          <a-button type="link" size="small" @click="router.push('/m/todo')">查看全部</a-button>
        </div>
      </template>

      <div v-if="loading" style="text-align: center; padding: 20px">
        <a-spin />
      </div>

      <div v-else-if="pendingList.length" class="task-list">
        <div
          v-for="task in pendingList"
          :key="task.taskId"
          class="task-card"
          @click="openApproval(task)"
        >
          <div class="task-header">
            <span class="task-title">{{ task.title }}</span>
            <a-tag v-if="task.taskType === 'SIGN'" color="purple">加签</a-tag>
            <a-tag v-else color="blue">审批</a-tag>
          </div>
          <div class="task-info">
            <span>申请人：{{ task.initiatorName }} ({{ task.deptName }})</span>
            <span>当前环节：{{ task.nodeName }}</span>
          </div>
          <div class="task-footer">
            <span class="task-time">{{ task.createTime }}</span>
            <a-button type="primary" size="small">办理</a-button>
          </div>
        </div>
      </div>

      <div v-else class="empty-hint">
        <span>🎉 暂无待处理审批，所有事务均已办结！</span>
      </div>
    </a-card>

    <!-- 移动端快捷审批抽屉 -->
    <a-drawer
      v-model:open="approvalDrawerVisible"
      title="移动端快速审批办理"
      placement="bottom"
      height="450px"
      destroy-on-close
    >
      <div v-if="currentTask" class="drawer-body">
        <div class="task-summary">
          <div style="font-weight: 600; font-size: 15px; margin-bottom: 4px">{{ currentTask.title }}</div>
          <div style="font-size: 13px; color: #666">
            申请人：{{ currentTask.initiatorName }} · 所属部门：{{ currentTask.deptName }}
          </div>
        </div>

        <a-radio-group v-model:value="approvalAction" button-style="solid" style="width: 100%; margin: 16px 0">
          <a-radio-button value="AGREE" style="width: 33.3%; text-align: center">同意</a-radio-button>
          <a-radio-button value="ADDSIGN" style="width: 33.3%; text-align: center">加签</a-radio-button>
          <a-radio-button value="REJECT" style="width: 33.3%; text-align: center">驳回</a-radio-button>
        </a-radio-group>

        <a-form-item v-if="approvalAction === 'ADDSIGN'" label="选择加签协同人员">
          <a-select
            v-model:value="addSignUser"
            placeholder="请选择加签对象"
            :options="userOptions"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="审批批注意见">
          <a-textarea
            v-model:value="approvalOpinion"
            placeholder="请输入您的批示意见（同意默认填充“同意申请”）"
            :rows="3"
          />
        </a-form-item>

        <a-button type="primary" block size="large" :loading="submitting" @click="handleApprovalSubmit">
          确认提交审批决策
        </a-button>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { message } from 'ant-design-vue';
import {
  CalendarOutlined,
  AuditOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue';
import { getPendingTasksApi, approveTaskApi, rejectTaskApi, addSignTaskApi } from '@/api/workflow';
import { listUserOptionsApi } from '@/api/user';
import type { TaskVO } from '@/types/workflow';

const router = useRouter();
const userStore = useUserStore();

const loading = ref(false);
const pendingList = ref<TaskVO[]>([]);

const approvalDrawerVisible = ref(false);
const currentTask = ref<TaskVO | null>(null);
const approvalAction = ref<'AGREE' | 'ADDSIGN' | 'REJECT'>('AGREE');
const approvalOpinion = ref('同意申请。');
const addSignUser = ref('');
const submitting = ref(false);
const userOptions = ref<{ label: string; value: string }[]>([]);

const loadTasks = async () => {
  loading.value = true;
  try {
    const res: any = await getPendingTasksApi(1, 5);
    const records = res?.data?.records || res?.data?.data?.records;
    if (records) {
      pendingList.value = records;
    }
  } catch {
    pendingList.value = [
      {
        taskId: 1,
        instId: 101,
        procKey: 'LEAVE',
        title: '请假申请 (张三 - 2天)',
        initiatorUsername: 'zhangsan',
        initiatorName: '张三',
        deptName: '研发中心',
        nodeName: '直属部门主管审核',
        taskType: 'APPROVE',
        taskStatus: 1,
        instStatus: 1,
        businessTable: 'oa_hr_leave',
        businessId: 1,
        createTime: '2026-10-28 09:30:00'
      }
    ];
  } finally {
    loading.value = false;
  }
};

const openApproval = (task: TaskVO) => {
  currentTask.value = task;
  approvalAction.value = 'AGREE';
  approvalOpinion.value = '同意申请。';
  addSignUser.value = '';
  approvalDrawerVisible.value = true;
};

const handleApprovalSubmit = async () => {
  if (!currentTask.value) return;
  if (approvalAction.value === 'REJECT' && !approvalOpinion.value.trim()) {
    message.warning('请填写驳回原因说明');
    return;
  }
  if (approvalAction.value === 'ADDSIGN' && !addSignUser.value) {
    message.warning('请选择加签人员');
    return;
  }

  try {
    submitting.value = true;
    if (approvalAction.value === 'AGREE') {
      await approveTaskApi({
        taskId: currentTask.value.taskId,
        opinion: approvalOpinion.value
      });
      message.success('已审批同意并推进流转');
    } else if (approvalAction.value === 'REJECT') {
      await rejectTaskApi({
        taskId: currentTask.value.taskId,
        opinion: approvalOpinion.value
      });
      message.success('已驳回申请');
    } else {
      await addSignTaskApi({
        taskId: currentTask.value.taskId,
        targetUsername: addSignUser.value,
        opinion: approvalOpinion.value
      });
      message.success('已加签派发待办任务');
    }
    approvalDrawerVisible.value = false;
    loadTasks();
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  loadTasks();
  try {
    const res = await listUserOptionsApi();
    if (res.code === 200 && res.data) {
      userOptions.value = res.data.map(u => ({
        label: `${u.realName} (${u.username})`,
        value: u.username
      }));
    }
  } catch {}
});
</script>

<style scoped>
.mobile-dashboard {
  padding-bottom: 12px;
}

.welcome-card {
  background: linear-gradient(135deg, #1677ff 0%, #36cfc9 100%);
  border-radius: 12px;
  padding: 16px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);
}

.welcome-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
}

.user-role {
  font-size: 12px;
  opacity: 0.9;
  margin-top: 2px;
}

.section-card {
  border-radius: 10px;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  text-align: center;
}

.service-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.icon-circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
  margin-bottom: 6px;
  transition: transform 0.2s;
}

.icon-circle:active {
  transform: scale(0.92);
}

.bg-blue { background: #1677ff; }
.bg-purple { background: #722ed1; }
.bg-green { background: #52c41a; }
.bg-orange { background: #fa8c16; }

.service-name {
  font-size: 12px;
  color: #333;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.task-card {
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.task-card:active {
  background: #f0f7ff;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.task-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f1f1f;
}

.task-info {
  font-size: 12px;
  color: #666;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.task-time {
  font-size: 11px;
  color: #999;
}

.empty-hint {
  text-align: center;
  padding: 24px 0;
  font-size: 13px;
  color: #999;
}

.task-summary {
  background: #f7f9fc;
  padding: 12px;
  border-radius: 6px;
}
</style>

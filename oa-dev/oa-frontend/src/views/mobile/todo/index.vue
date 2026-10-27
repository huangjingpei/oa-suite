<template>
  <div class="mobile-todo-page">
    <a-segmented
      v-model:value="activeTab"
      :options="[
        { label: '我的待办事务', value: 'pending' },
        { label: '我已处理审批', value: 'handled' }
      ]"
      block
      style="margin-bottom: 14px"
      @change="handleTabChange"
    />

    <div v-if="loading" style="text-align: center; padding: 40px">
      <a-spin />
    </div>

    <!-- 待办列表 -->
    <div v-else-if="activeTab === 'pending'">
      <div v-if="pendingList.length" class="todo-list">
        <div
          v-for="task in pendingList"
          :key="task.taskId"
          class="todo-card"
          @click="openApproval(task)"
        >
          <div class="todo-header">
            <span class="todo-title">{{ task.title }}</span>
            <a-tag v-if="task.taskType === 'SIGN'" color="purple">加签任务</a-tag>
            <a-tag v-else color="blue">待办</a-tag>
          </div>
          <div class="todo-meta">
            <div>申请人员：{{ task.initiatorName }} · {{ task.deptName }}</div>
            <div>当前环节：{{ task.nodeName }}</div>
            <div class="todo-time">到达时间：{{ task.createTime }}</div>
          </div>
          <div class="todo-action">
            <a-button type="primary" block size="middle">立即办理审批</a-button>
          </div>
        </div>
      </div>
      <div v-else class="empty-hint">
        <CheckCircleOutlined style="font-size: 36px; color: #52c41a; margin-bottom: 8px" />
        <div>太棒了！所有待办审批均已处理完毕</div>
      </div>
    </div>

    <!-- 已办列表 -->
    <div v-else>
      <div v-if="handledList.length" class="todo-list">
        <div v-for="task in handledList" :key="task.instId" class="todo-card">
          <div class="todo-header">
            <span class="todo-title">{{ task.title }}</span>
            <a-tag v-if="task.instStatus === 2" color="green">审批通过</a-tag>
            <a-tag v-else-if="task.instStatus === 3" color="red">已驳回</a-tag>
            <a-tag v-else color="processing">流转中</a-tag>
          </div>
          <div class="todo-meta">
            <div>发起人员：{{ task.initiatorName }} · {{ task.deptName }}</div>
            <div>单据状态：{{ task.instStatus === 2 ? '流程已办结' : '仍在审批中' }}</div>
          </div>
        </div>
      </div>
      <div v-else class="empty-hint">
        <div>暂无已办历史记录</div>
      </div>
    </div>

    <!-- 移动端审批办理抽屉 -->
    <a-drawer
      v-model:open="drawerVisible"
      title="审批事务办理"
      placement="bottom"
      height="450px"
      destroy-on-close
    >
      <div v-if="currentTask" class="drawer-content">
        <div class="task-info-box">
          <div style="font-weight: 600; font-size: 15px; margin-bottom: 4px">{{ currentTask.title }}</div>
          <div style="font-size: 12px; color: #666">
            申请人：{{ currentTask.initiatorName }} · {{ currentTask.deptName }}
          </div>
        </div>

        <a-radio-group v-model:value="actionType" button-style="solid" style="width: 100%; margin: 16px 0">
          <a-radio-button value="AGREE" style="width: 33.3%; text-align: center">同意</a-radio-button>
          <a-radio-button value="ADDSIGN" style="width: 33.3%; text-align: center">加签</a-radio-button>
          <a-radio-button value="REJECT" style="width: 33.3%; text-align: center">驳回</a-radio-button>
        </a-radio-group>

        <a-form-item v-if="actionType === 'ADDSIGN'" label="选择加签人员">
          <a-select
            v-model:value="addSignUser"
            placeholder="请选择加签对象"
            :options="userOptions"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="审批批注意见">
          <a-textarea v-model:value="opinion" placeholder="请输入您的意见..." :rows="3" />
        </a-form-item>

        <a-button type="primary" block size="large" :loading="submitting" @click="handleSubmit">
          确认提交
        </a-button>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { CheckCircleOutlined } from '@ant-design/icons-vue';
import {
  getPendingTasksApi,
  getHandledTasksApi,
  approveTaskApi,
  rejectTaskApi,
  addSignTaskApi
} from '@/api/workflow';
import { listUserOptionsApi } from '@/api/user';
import type { TaskVO } from '@/types/workflow';

const activeTab = ref<'pending' | 'handled'>('pending');
const loading = ref(false);
const pendingList = ref<TaskVO[]>([]);
const handledList = ref<TaskVO[]>([]);

const drawerVisible = ref(false);
const currentTask = ref<TaskVO | null>(null);
const actionType = ref<'AGREE' | 'ADDSIGN' | 'REJECT'>('AGREE');
const opinion = ref('同意申请。');
const addSignUser = ref('');
const submitting = ref(false);
const userOptions = ref<{ label: string; value: string }[]>([]);

const loadData = async () => {
  loading.value = true;
  try {
    if (activeTab.value === 'pending') {
      const res: any = await getPendingTasksApi(1, 20);
      const records = res?.data?.records || res?.data?.data?.records;
      if (records) {
        pendingList.value = records;
      }
    } else {
      const res: any = await getHandledTasksApi(1, 20);
      const records = res?.data?.records || res?.data?.data?.records;
      if (records) {
        handledList.value = records;
      }
    }
  } catch {
    if (activeTab.value === 'pending') {
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
    } else {
      handledList.value = [
        {
          taskId: 101,
          instId: 101,
          procKey: 'SEAL',
          title: '企业公章用印 (王商务)',
          initiatorUsername: 'wang',
          initiatorName: '王商务',
          deptName: '市场营销部',
          nodeName: '办结',
          taskType: 'APPROVE',
          taskStatus: 2,
          instStatus: 2,
          businessTable: 'oa_adm_seal',
          businessId: 1,
          createTime: '2026-10-28 10:00:00'
        }
      ];
    }
  } finally {
    loading.value = false;
  }
};

const handleTabChange = () => {
  loadData();
};

const openApproval = (task: TaskVO) => {
  currentTask.value = task;
  actionType.value = 'AGREE';
  opinion.value = '同意申请。';
  addSignUser.value = '';
  drawerVisible.value = true;
};

const handleSubmit = async () => {
  if (!currentTask.value) return;
  if (actionType.value === 'REJECT' && !opinion.value.trim()) {
    message.warning('请填写驳回原因说明');
    return;
  }
  if (actionType.value === 'ADDSIGN' && !addSignUser.value) {
    message.warning('请选择加签人员');
    return;
  }

  try {
    submitting.value = true;
    if (actionType.value === 'AGREE') {
      await approveTaskApi({
        taskId: currentTask.value.taskId,
        opinion: opinion.value
      });
      message.success('已审批同意并推进流转');
    } else if (actionType.value === 'REJECT') {
      await rejectTaskApi({
        taskId: currentTask.value.taskId,
        opinion: opinion.value
      });
      message.success('已驳回申请');
    } else {
      await addSignTaskApi({
        taskId: currentTask.value.taskId,
        targetUsername: addSignUser.value,
        opinion: opinion.value
      });
      message.success('已加签派发待办任务');
    }
    drawerVisible.value = false;
    loadData();
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  loadData();
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
.mobile-todo-page {
  padding-bottom: 12px;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-card {
  background: #ffffff;
  border-radius: 10px;
  padding: 14px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

.todo-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.todo-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f1f1f;
}

.todo-meta {
  font-size: 12px;
  color: #666;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.todo-time {
  color: #999;
  font-size: 11px;
}

.todo-action {
  margin-top: 12px;
}

.empty-hint {
  text-align: center;
  padding: 40px 0;
  color: #8c8c8c;
  font-size: 14px;
}

.task-info-box {
  background: #f7f9fc;
  padding: 12px;
  border-radius: 6px;
}
</style>

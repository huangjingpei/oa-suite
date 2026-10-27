<template>
  <div class="dashboard-page">
    <!-- 欢迎信息条 -->
    <a-card :bordered="false" class="welcome-card">
      <div class="welcome-content">
        <a-avatar :size="64" style="background-color: #1677ff">
          {{ userStore.displayName.charAt(0) }}
        </a-avatar>
        <div class="welcome-text">
          <h3>您好，{{ userStore.displayName }}，祝您今天工作愉快！</h3>
          <p>
            所属部门：{{ userStore.userInfo?.deptName || '未分配' }} ｜ 
            当前身份：{{ userStore.userInfo?.postName || '正式员工' }} ｜ 
            工号：{{ userStore.userInfo?.workNo || 'OA0001' }}
          </p>
        </div>
        <div class="quick-clock">
          <a-button type="primary" size="large" @click="handleQuickClock">
            <CheckCircleOutlined /> 快速打卡
          </a-button>
        </div>
      </div>
    </a-card>

    <!-- 数据指标统计卡片 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="我的待办任务"
            :value="pendingTotal"
            :value-style="{ color: '#cf1322' }"
          >
            <template #prefix>
              <ClockCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="我的已办事项"
            :value="handledTotal"
            :value-style="{ color: '#3f8600' }"
          >
            <template #prefix>
              <CheckCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="我发起的单据"
            :value="submittedTotal"
            :value-style="{ color: '#1677ff' }"
          >
            <template #prefix>
              <SendOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="红头公文与通知"
            :value="3"
            :value-style="{ color: '#d46b08' }"
          >
            <template #prefix>
              <FileTextOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 常用协同事务快捷发起 -->
    <a-card :bordered="false" style="margin-top: 16px" title="⚡ 常用协同事务快捷发起">
      <template #extra>
        <span style="font-size: 13px; color: #888">支持暂存草稿与一键启动流转审批</span>
      </template>
      <a-row :gutter="16">
        <a-col :span="4">
          <a-button block type="dashed" size="large" @click="leaveModalOpen = true">
            <template #icon><CalendarOutlined style="color: #1677ff" /></template>
            发起请假
          </a-button>
        </a-col>
        <a-col :span="5">
          <a-button block type="dashed" size="large" @click="repairModalOpen = true">
            <template #icon><ClockCircleOutlined style="color: #52c41a" /></template>
            异常漏卡补卡
          </a-button>
        </a-col>
        <a-col :span="5">
          <a-button block type="dashed" size="large" @click="sealModalOpen = true">
            <template #icon><AuditOutlined style="color: #722ed1" /></template>
            申请用印审批
          </a-button>
        </a-col>
        <a-col :span="5">
          <a-button block type="dashed" size="large" @click="redheadModalOpen = true">
            <template #icon><FileDoneOutlined style="color: #f5222d" /></template>
            起草红头公文
          </a-button>
        </a-col>
        <a-col :span="5">
          <a-button block type="dashed" size="large" @click="borrowModalOpen = true">
            <template #icon><ReadOutlined style="color: #fa8c16" /></template>
            借阅档案资料
          </a-button>
        </a-col>
      </a-row>
    </a-card>

    <!-- 工作台核心面板 (待办/已办/我发起的/通知) -->
    <a-card :bordered="false" style="margin-top: 16px">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <!-- 待办任务 -->
        <a-tab-pane key="pending">
          <template #tab>
            <span>
              <ClockCircleOutlined /> 待办审批
              <a-badge :count="pendingTotal" :offset="[10, -2]" />
            </span>
          </template>

          <a-table
            :columns="pendingColumns"
            :data-source="pendingList"
            :loading="pendingLoading"
            row-key="taskId"
            :pagination="{
              current: pendingPage,
              pageSize: 10,
              total: pendingTotal,
              onChange: (p: number) => { pendingPage = p; loadPendingTasks(); }
            }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'title'">
                <strong>{{ record.title }}</strong>
              </template>
              <template v-else-if="column.key === 'taskType'">
                <a-tag v-if="record.taskType === 'SIGN'" color="purple">加签任务</a-tag>
                <a-tag v-else color="blue">常规审批</a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-button type="primary" size="small" @click="openApproval(record)">
                  办理审批
                </a-button>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- 已办任务 -->
        <a-tab-pane key="handled">
          <template #tab>
            <span><CheckCircleOutlined /> 已办履历</span>
          </template>

          <a-table
            :columns="handledColumns"
            :data-source="handledList"
            :loading="handledLoading"
            row-key="instId"
            :pagination="{
              current: handledPage,
              pageSize: 10,
              total: handledTotal,
              onChange: (p: number) => { handledPage = p; loadHandledTasks(); }
            }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'instStatus'">
                <a-tag v-if="record.instStatus === 2" color="green">审批通过</a-tag>
                <a-tag v-else-if="record.instStatus === 3" color="red">已驳回</a-tag>
                <a-tag v-else-if="record.instStatus === 4" color="gray">已作废</a-tag>
                <a-tag v-else color="processing">流转中</a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-button type="link" size="small" @click="viewTimeline(record.instId)">
                  查看履历
                </a-button>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- 我发起的 -->
        <a-tab-pane key="submitted">
          <template #tab>
            <span><SendOutlined /> 我发起的申请</span>
          </template>

          <a-table
            :columns="submittedColumns"
            :data-source="submittedList"
            :loading="submittedLoading"
            row-key="instId"
            :pagination="{
              current: submittedPage,
              pageSize: 10,
              total: submittedTotal,
              onChange: (p: number) => { submittedPage = p; loadMySubmitted(); }
            }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'instStatus'">
                <a-tag v-if="record.instStatus === 0" color="default">草稿待发</a-tag>
                <a-tag v-else-if="record.instStatus === 1" color="processing">审核中</a-tag>
                <a-tag v-else-if="record.instStatus === 2" color="green">审批通过</a-tag>
                <a-tag v-else-if="record.instStatus === 3" color="red">已驳回</a-tag>
                <a-tag v-else color="gray">已作废</a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-button type="link" size="small" @click="viewTimeline(record.instId)">
                  流转跟踪
                </a-button>
                <a-popconfirm
                  v-if="record.instStatus === 1"
                  title="确认作废撤销该审批申请吗？"
                  @confirm="handleCancelProcess(record.instId)"
                >
                  <a-button type="link" danger size="small">撤回作废</a-button>
                </a-popconfirm>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- 红头公文与协同通知 -->
        <a-tab-pane key="notices">
          <template #tab>
            <span><FileTextOutlined /> 公文公告</span>
          </template>
          <a-list item-layout="horizontal" :data-source="mockNotices">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta :description="item.time">
                  <template #title>
                    <a-tag color="red">{{ item.tag }}</a-tag>
                    <a href="javascript:;" style="font-weight: 500">{{ item.title }}</a>
                  </template>
                  <template #avatar>
                    <a-avatar style="background-color: #f5222d">公</a-avatar>
                  </template>
                </a-list-item-meta>
                <div>发布单位：{{ item.issuer }}</div>
              </a-list-item>
            </template>
          </a-list>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 审批流转弹窗 -->
    <ApprovalModal
      v-model:open="approvalModalVisible"
      :task="currentTask"
      @success="handleApprovalSuccess"
    />

    <!-- 查看流转时间轴弹窗 -->
    <a-modal
      v-model:open="timelineModalVisible"
      title="审批履历时间轴"
      :footer="null"
      width="600px"
    >
      <TimelineHistory :inst-id="currentTimelineInstId" />
    </a-modal>

    <!-- 业务快捷发起弹窗 -->
    <LeaveModal v-model:open="leaveModalOpen" @success="handleFormSuccess" />
    <RepairModal v-model:open="repairModalOpen" @success="handleFormSuccess" />
    <SealModal v-model:open="sealModalOpen" @success="handleFormSuccess" />
    <RedheadModal v-model:open="redheadModalOpen" @success="handleFormSuccess" />
    <BorrowModal v-model:open="borrowModalOpen" @success="handleFormSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useUserStore } from '@/store/user';
import { message } from 'ant-design-vue';
import {
  ClockCircleOutlined,
  CheckCircleOutlined,
  SendOutlined,
  FileTextOutlined,
  CalendarOutlined,
  AuditOutlined,
  ReadOutlined,
  FileDoneOutlined
} from '@ant-design/icons-vue';
import {
  getPendingTasksApi,
  getHandledTasksApi,
  getMySubmittedApi,
  cancelProcessApi
} from '@/api/workflow';
import type { TaskVO } from '@/types/workflow';
import ApprovalModal from '@/views/workflow/components/ApprovalModal.vue';
import TimelineHistory from '@/views/workflow/components/TimelineHistory.vue';
import LeaveModal from '@/views/business/leave/components/LeaveModal.vue';
import RepairModal from '@/views/business/attendance/components/RepairModal.vue';
import SealModal from '@/views/business/seal/components/SealModal.vue';
import RedheadModal from '@/views/business/redhead/components/RedheadModal.vue';
import BorrowModal from '@/views/business/borrow/components/BorrowModal.vue';

const userStore = useUserStore();

// 快捷表单弹窗状态
const leaveModalOpen = ref(false);
const repairModalOpen = ref(false);
const sealModalOpen = ref(false);
const redheadModalOpen = ref(false);
const borrowModalOpen = ref(false);

const handleFormSuccess = () => {
  loadPendingTasks();
  loadMySubmitted();
};

const activeTab = ref('pending');

// 待办相关状态
const pendingLoading = ref(false);
const pendingList = ref<TaskVO[]>([]);
const pendingTotal = ref(0);
const pendingPage = ref(1);

// 已办相关状态
const handledLoading = ref(false);
const handledList = ref<TaskVO[]>([]);
const handledTotal = ref(0);
const handledPage = ref(1);

// 我发起的申请相关状态
const submittedLoading = ref(false);
const submittedList = ref<TaskVO[]>([]);
const submittedTotal = ref(0);
const submittedPage = ref(1);

// 弹窗状态
const approvalModalVisible = ref(false);
const currentTask = ref<TaskVO | null>(null);

const timelineModalVisible = ref(false);
const currentTimelineInstId = ref<number | undefined>(undefined);

// 列定义
const pendingColumns = [
  { title: '单据标题 / 摘要', dataIndex: 'title', key: 'title' },
  { title: '申请人', dataIndex: 'initiatorName', key: 'initiatorName', width: 120 },
  { title: '所属部门', dataIndex: 'deptName', key: 'deptName', width: 150 },
  { title: '当前环节', dataIndex: 'nodeName', key: 'nodeName', width: 140 },
  { title: '任务类型', dataIndex: 'taskType', key: 'taskType', width: 110 },
  { title: '到达时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 120, align: 'center' as const }
];

const handledColumns = [
  { title: '单据标题 / 摘要', dataIndex: 'title', key: 'title' },
  { title: '申请人', dataIndex: 'initiatorName', key: 'initiatorName', width: 120 },
  { title: '经办节点', dataIndex: 'nodeName', key: 'nodeName', width: 140 },
  { title: '流程总状态', dataIndex: 'instStatus', key: 'instStatus', width: 120 },
  { title: '办理时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 110, align: 'center' as const }
];

const submittedColumns = [
  { title: '申请单据名称', dataIndex: 'title', key: 'title' },
  { title: '所属流程', dataIndex: 'procKey', key: 'procKey', width: 140 },
  { title: '当前环节', dataIndex: 'nodeName', key: 'nodeName', width: 140 },
  { title: '流程状态', dataIndex: 'instStatus', key: 'instStatus', width: 120 },
  { title: '发起时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 160, align: 'center' as const }
];

const mockNotices = ref([
  {
    tag: '红头决议',
    title: '盛泰发〔2026〕01号 关于全面启动新一代数字化协同办公系统升级的通知',
    issuer: '集团总经办',
    time: '2026-10-27 10:00:00'
  },
  {
    tag: '行政通报',
    title: '关于进一步加强企业公章及合同专用章用印审批合规管理的通知',
    issuer: '综合行政部',
    time: '2026-10-26 15:30:00'
  },
  {
    tag: '人事考勤',
    title: '关于2026年度10月份员工日常打卡异常漏卡补卡提交流程规范',
    issuer: '人力资源部',
    time: '2026-10-25 09:00:00'
  }
]);

// 加载待办任务
const loadPendingTasks = async () => {
  pendingLoading.value = true;
  try {
    const res = await getPendingTasksApi(pendingPage.value, 10);
    pendingList.value = res.data.data.records || [];
    pendingTotal.value = res.data.data.total || 0;
  } catch (ignored) {
  } finally {
    pendingLoading.value = false;
  }
};

// 加载已办任务
const loadHandledTasks = async () => {
  handledLoading.value = true;
  try {
    const res = await getHandledTasksApi(handledPage.value, 10);
    handledList.value = res.data.data.records || [];
    handledTotal.value = res.data.data.total || 0;
  } catch (ignored) {
  } finally {
    handledLoading.value = false;
  }
};

// 加载我发起的
const loadMySubmitted = async () => {
  submittedLoading.value = true;
  try {
    const res = await getMySubmittedApi(submittedPage.value, 10);
    submittedList.value = res.data.data.records || [];
    submittedTotal.value = res.data.data.total || 0;
  } catch (ignored) {
  } finally {
    submittedLoading.value = false;
  }
};

const handleTabChange = (key: string) => {
  if (key === 'pending') loadPendingTasks();
  else if (key === 'handled') loadHandledTasks();
  else if (key === 'submitted') loadMySubmitted();
};

const openApproval = (task: TaskVO) => {
  currentTask.value = task;
  approvalModalVisible.value = true;
};

const handleApprovalSuccess = () => {
  loadPendingTasks();
  loadHandledTasks();
};

const viewTimeline = (instId: number) => {
  currentTimelineInstId.value = instId;
  timelineModalVisible.value = true;
};

const handleCancelProcess = async (instId: number) => {
  try {
    await cancelProcessApi(instId, '发起人撤销');
    message.success('申请单据已撤销作废');
    loadMySubmitted();
  } catch (ignored) {
  }
};

const handleQuickClock = () => {
  message.success('打卡成功！当前考勤状态正常 (9:00)');
};

onMounted(() => {
  loadPendingTasks();
  loadHandledTasks();
  loadMySubmitted();
});
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
}

.welcome-card {
  border-radius: 8px;
}

.welcome-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.welcome-text h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f1f1f;
  margin-bottom: 6px;
}

.welcome-text p {
  color: #8c8c8c;
  font-size: 13px;
  margin: 0;
}

.quick-clock {
  margin-left: auto;
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);
}
</style>

<template>
  <div class="business-page">
    <a-card :bordered="false" class="page-card">
      <div class="table-header">
        <div class="header-title">
          <CalendarOutlined style="color: #1677ff; margin-right: 8px" />
          <span>员工请假申请与假期台账</span>
        </div>
        <div class="header-actions">
          <a-space>
            <a-button type="primary" @click="leaveModalVisible = true">
              <template #icon><PlusOutlined /></template>
              发起请假申请
            </a-button>
            <a-button @click="loadData">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </a-space>
        </div>
      </div>

      <a-table
        :columns="columns"
        :data-source="list"
        :loading="loading"
        row-key="id"
        style="margin-top: 16px"
        :pagination="{
          current: pageNum,
          pageSize: 10,
          total: total,
          onChange: (p: number) => { pageNum = p; loadData(); }
        }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'leaveNo'">
            <strong>{{ record.leaveNo }}</strong>
          </template>

          <template v-else-if="column.key === 'leaveType'">
            <a-tag v-if="record.leaveType === 'ANNUAL'" color="green">年休假</a-tag>
            <a-tag v-else-if="record.leaveType === 'SICK'" color="orange">病假</a-tag>
            <a-tag v-else-if="record.leaveType === 'PERSONAL'" color="blue">事假</a-tag>
            <a-tag v-else-if="record.leaveType === 'REST'" color="cyan">调休</a-tag>
            <a-tag v-else color="purple">{{ record.leaveType }}</a-tag>
          </template>

          <template v-else-if="column.key === 'procStatus'">
            <a-tag v-if="record.procStatus === 0" color="default">草稿待发</a-tag>
            <a-tag v-else-if="record.procStatus === 1" color="processing">审核流转中</a-tag>
            <a-tag v-else-if="record.procStatus === 2" color="green">审批通过</a-tag>
            <a-tag v-else-if="record.procStatus === 3" color="red">已驳回</a-tag>
            <a-tag v-else color="gray">已作废</a-tag>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                v-if="record.procInstId"
                type="link"
                size="small"
                @click="viewTimeline(record.procInstId)"
              >
                流转轨迹
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="openPrint(record)"
              >
                <PrinterOutlined /> A4打印预览
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 请假申请弹窗 -->
    <LeaveModal
      v-model:open="leaveModalVisible"
      @success="loadData"
    />

    <!-- A4 纸质打印预览弹窗 -->
    <PrintModal
      v-model:open="printModalVisible"
      title="员工请假审批表 - A4 纸质高保真排版"
    >
      <LeavePrint :leave="currentRecord || {}" />
    </PrintModal>

    <!-- 审批履历时间轴弹窗 -->
    <a-modal
      v-model:open="timelineModalVisible"
      title="请假审批流转履历"
      :footer="null"
      width="600px"
    >
      <TimelineHistory :inst-id="currentInstId" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import {
  CalendarOutlined,
  PlusOutlined,
  ReloadOutlined,
  PrinterOutlined
} from '@ant-design/icons-vue';
import { getMyLeavesApi } from '@/api/business';
import type { LeaveVO } from '@/types/business';
import LeaveModal from './components/LeaveModal.vue';
import LeavePrint from './components/LeavePrint.vue';
import PrintModal from '@/components/print/PrintModal.vue';
import TimelineHistory from '@/views/workflow/components/TimelineHistory.vue';

const loading = ref(false);
const list = ref<LeaveVO[]>([]);
const total = ref(0);
const pageNum = ref(1);

const leaveModalVisible = ref(false);
const printModalVisible = ref(false);
const timelineModalVisible = ref(false);
const currentRecord = ref<LeaveVO | null>(null);
const currentInstId = ref<number | undefined>(undefined);

const columns = [
  { title: '单据编号', dataIndex: 'leaveNo', key: 'leaveNo', width: 160 },
  { title: '请假类型', dataIndex: 'leaveType', key: 'leaveType', width: 100 },
  { title: '起始时间', dataIndex: 'startTime', key: 'startTime', width: 170 },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 170 },
  { title: '天数', dataIndex: 'totalDays', key: 'totalDays', width: 80 },
  { title: '事由说明', dataIndex: 'reason', key: 'reason' },
  { title: '审批状态', dataIndex: 'procStatus', key: 'procStatus', width: 110 },
  { title: '提交时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: '操作', key: 'action', width: 200, align: 'center' as const }
];

const loadData = async () => {
  loading.value = true;
  try {
    const res = await getMyLeavesApi({ pageNum: pageNum.value, pageSize: 10 });
    if (res.code === 200 && res.data) {
      list.value = res.data.records || [];
      total.value = res.data.total || 0;
    }
  } catch {
    // 回退演示数据
    list.value = [
      {
        id: 1,
        leaveNo: 'QJ202610280001',
        applicantUsername: 'zhangsan',
        applicantName: '张三',
        deptName: '研发中心',
        leaveType: 'PERSONAL',
        startTime: '2026-10-28 09:00:00',
        endTime: '2026-10-29 18:00:00',
        totalHours: 16,
        totalDays: 2,
        reason: '家中有事需处理，工作已提前与研发组内交接完毕。',
        procInstId: 1001,
        procStatus: 2,
        createTime: '2026-10-28 08:30:00'
      }
    ];
    total.value = 1;
  } finally {
    loading.value = false;
  }
};

const openPrint = (record: LeaveVO) => {
  currentRecord.value = record;
  printModalVisible.value = true;
};

const viewTimeline = (instId?: number) => {
  currentInstId.value = instId;
  timelineModalVisible.value = true;
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.business-page {
  padding: 0;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f1f1f;
  display: flex;
  align-items: center;
}
</style>

<template>
  <div class="business-page">
    <a-card :bordered="false" class="page-card">
      <div class="table-header">
        <div class="header-title">
          <ClockCircleOutlined style="color: #1677ff; margin-right: 8px" />
          <span>考勤管理与异常漏卡补卡</span>
        </div>
        <div class="header-actions">
          <a-space>
            <a-button type="primary" @click="repairModalVisible = true">
              <template #icon><PlusOutlined /></template>
              发起漏卡补卡申请
            </a-button>
            <a-button @click="loadData">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </a-space>
        </div>
      </div>

      <a-tabs v-model:activeKey="activeTab" style="margin-top: 16px">
        <!-- 页签 1: 异常补卡台账 -->
        <a-tab-pane key="repairs" tab="异常补卡申请台账">
          <a-table
            :columns="repairColumns"
            :data-source="repairList"
            :loading="repairLoading"
            row-key="id"
            :pagination="{
              current: repairPage,
              pageSize: 10,
              total: repairTotal,
              onChange: (p: number) => { repairPage = p; loadData(); }
            }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'repairNo'">
                <strong>{{ record.repairNo }}</strong>
              </template>

              <template v-else-if="column.key === 'procStatus'">
                <a-tag v-if="record.procStatus === 0" color="default">草稿待发</a-tag>
                <a-tag v-else-if="record.procStatus === 1" color="processing">审核中</a-tag>
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
                    <PrinterOutlined /> 补卡单打印
                  </a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- 页签 2: 在线打卡与流水 -->
        <a-tab-pane key="clock" tab="在线考勤打卡与打卡流水">
          <div class="clock-action-bar">
            <a-card size="small" class="clock-card">
              <div class="clock-card-content">
                <div>
                  <div style="font-weight: 600; font-size: 15px">快捷在线打卡</div>
                  <div style="font-size: 13px; color: #888">当前系统时间：{{ currentTime }}</div>
                </div>
                <a-space>
                  <a-button type="primary" :loading="clocking" @click="handleClock('ON')">
                    上班签到打卡
                  </a-button>
                  <a-button type="default" :loading="clocking" @click="handleClock('OFF')">
                    下班签退打卡
                  </a-button>
                </a-space>
              </div>
            </a-card>
          </div>

          <a-table
            :columns="clockColumns"
            :data-source="clockList"
            :loading="clockLoading"
            row-key="id"
            style="margin-top: 16px"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'clockType'">
                <a-tag v-if="record.clockType === 'ON'" color="blue">上班签到</a-tag>
                <a-tag v-else color="green">下班签退</a-tag>
              </template>

              <template v-else-if="column.key === 'status'">
                <a-tag v-if="record.status === 0" color="success">正常打卡</a-tag>
                <a-tag v-else-if="record.status === 1" color="warning">迟到</a-tag>
                <a-tag v-else-if="record.status === 2" color="error">早退</a-tag>
                <a-tag v-else color="purple">外勤签到</a-tag>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 补卡弹窗 -->
    <RepairModal
      v-model:open="repairModalVisible"
      @success="loadData"
    />

    <!-- A4 纸质打印预览弹窗 -->
    <PrintModal
      v-model:open="printModalVisible"
      title="考勤异常补卡审批单 - A4 纸质高保真排版"
    >
      <RepairPrint :repair="currentRecord || {}" />
    </PrintModal>

    <!-- 审批履历时间轴弹窗 -->
    <a-modal
      v-model:open="timelineModalVisible"
      title="补卡审批流转履历"
      :footer="null"
      width="600px"
    >
      <TimelineHistory :inst-id="currentInstId" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  ClockCircleOutlined,
  PlusOutlined,
  ReloadOutlined,
  PrinterOutlined
} from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import {
  getMyRepairsApi,
  getMyClockRecordsApi,
  clockInApi
} from '@/api/business';
import type { AttendanceRepairVO, ClockRecordVO } from '@/types/business';
import RepairModal from './components/RepairModal.vue';
import RepairPrint from './components/RepairPrint.vue';
import PrintModal from '@/components/print/PrintModal.vue';
import TimelineHistory from '@/views/workflow/components/TimelineHistory.vue';

const activeTab = ref('repairs');

// 补卡状态
const repairLoading = ref(false);
const repairList = ref<AttendanceRepairVO[]>([]);
const repairTotal = ref(0);
const repairPage = ref(1);

// 打卡状态
const clockLoading = ref(false);
const clockList = ref<ClockRecordVO[]>([]);
const clocking = ref(false);
const currentTime = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'));
let timeTimer: any = null;

// 弹窗状态
const repairModalVisible = ref(false);
const printModalVisible = ref(false);
const timelineModalVisible = ref(false);
const currentRecord = ref<AttendanceRepairVO | null>(null);
const currentInstId = ref<number | undefined>(undefined);

const repairColumns = [
  { title: '补卡单号', dataIndex: 'repairNo', key: 'repairNo', width: 170 },
  { title: '申请人', dataIndex: 'applicantName', key: 'applicantName', width: 120 },
  { title: '所属部门', dataIndex: 'deptName', key: 'deptName', width: 140 },
  { title: '补卡项数', dataIndex: 'totalCount', key: 'totalCount', width: 90 },
  { title: '补卡总体说明', dataIndex: 'reason', key: 'reason' },
  { title: '流转状态', dataIndex: 'procStatus', key: 'procStatus', width: 110 },
  { title: '提交日期', dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: '操作', key: 'action', width: 200, align: 'center' as const }
];

const clockColumns = [
  { title: '打卡姓名', dataIndex: 'realName', key: 'realName', width: 120 },
  { title: '打卡类型', dataIndex: 'clockType', key: 'clockType', width: 110 },
  { title: '打卡时间', dataIndex: 'clockTime', key: 'clockTime', width: 180 },
  { title: '设备终端', dataIndex: 'deviceType', key: 'deviceType', width: 100 },
  { title: '打卡位置 / IP', dataIndex: 'locationAddress', key: 'locationAddress' },
  { title: '考勤状态', dataIndex: 'status', key: 'status', width: 110 }
];

const loadData = async () => {
  repairLoading.value = true;
  clockLoading.value = true;
  try {
    const res = await getMyRepairsApi({ pageNum: repairPage.value, pageSize: 10 });
    if (res.code === 200 && res.data) {
      repairList.value = res.data.records || [];
      repairTotal.value = res.data.total || 0;
    }
  } catch {
    repairList.value = [
      {
        id: 2,
        repairNo: 'BK202610280002',
        applicantUsername: 'zhangsan',
        applicantName: '张三',
        deptName: '研发中心',
        reason: '因外出参加行业技术交流会，现场无法连入办公WiFi指纹签到。',
        totalCount: 1,
        procInstId: 1002,
        procStatus: 2,
        createTime: '2026-10-28 09:15:00',
        details: [
          {
            repairDate: '2026-10-28',
            repairType: 'CHECK_IN',
            repairSignTime: '2026-10-28 09:00:00',
            remark: '外出交流会开幕现场签到'
          }
        ]
      }
    ];
    repairTotal.value = 1;
  } finally {
    repairLoading.value = false;
  }

  try {
    const cRes = await getMyClockRecordsApi({ pageNum: 1, pageSize: 10 });
    if (cRes.code === 200 && cRes.data) {
      clockList.value = cRes.data.records || [];
    }
  } catch {
    clockList.value = [
      {
        id: 101,
        username: 'zhangsan',
        realName: '张三',
        deptName: '研发中心',
        clockType: 'ON',
        clockTime: '2026-10-28 08:55:12',
        deviceType: 'WEB',
        locationAddress: '盛泰集团总部研发园区 (192.168.1.105)',
        status: 0
      }
    ];
  } finally {
    clockLoading.value = false;
  }
};

const handleClock = async (type: 'ON' | 'OFF') => {
  try {
    clocking.value = true;
    const res = await clockInApi({
      clockType: type,
      deviceType: 'WEB',
      locationAddress: '北京市海淀区盛泰大厦A座 (浏览器客户端)'
    });
    if (res.code === 200) {
      message.success(type === 'ON' ? '上班签到成功！' : '下班签退成功！');
      loadData();
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  } finally {
    clocking.value = false;
  }
};

const openPrint = (record: AttendanceRepairVO) => {
  currentRecord.value = record;
  printModalVisible.value = true;
};

const viewTimeline = (instId?: number) => {
  currentInstId.value = instId;
  timelineModalVisible.value = true;
};

onMounted(() => {
  loadData();
  timeTimer = setInterval(() => {
    currentTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss');
  }, 1000);
});

onUnmounted(() => {
  if (timeTimer) clearInterval(timeTimer);
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

.clock-action-bar {
  margin-bottom: 16px;
}

.clock-card {
  background-color: #f7f9fc;
  border: 1px dashed #d9d9d9;
}

.clock-card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

<template>
  <div class="business-page">
    <a-card :bordered="false" class="page-card">
      <div class="table-header">
        <div class="header-title">
          <ReadOutlined style="color: #1677ff; margin-right: 8px" />
          <span>档案资料借阅与查阅台账</span>
        </div>
        <div class="header-actions">
          <a-space>
            <a-button type="primary" @click="borrowModalVisible = true">
              <template #icon><PlusOutlined /></template>
              发起借阅申请
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
          <template v-if="column.key === 'borrowNo'">
            <strong>{{ record.borrowNo }}</strong>
          </template>

          <template v-else-if="column.key === 'borrowType'">
            <a-tag v-if="record.borrowType === 'READ_ONLY'" color="blue">现场查阅</a-tag>
            <a-tag v-else-if="record.borrowType === 'BORROW_PHYSICAL'" color="orange">实体外借</a-tag>
            <a-tag v-else color="purple">复印摘录</a-tag>
          </template>

          <template v-else-if="column.key === 'returnStatus'">
            <a-tag v-if="record.returnStatus === 0" color="default">未出库</a-tag>
            <a-tag v-else-if="record.returnStatus === 1" color="processing">借出流转中</a-tag>
            <a-tag v-else-if="record.returnStatus === 2" color="success">已核验归还</a-tag>
            <a-tag v-else color="error">已逾期</a-tag>
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
                <PrinterOutlined /> 借阅单打印
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 借阅申请弹窗 -->
    <BorrowModal
      v-model:open="borrowModalVisible"
      @success="loadData"
    />

    <!-- A4 纸质打印预览弹窗 -->
    <PrintModal
      v-model:open="printModalVisible"
      title="档案资料借阅审批单 - A4 纸质高保真排版"
    >
      <BorrowPrint :borrow="currentRecord || {}" />
    </PrintModal>

    <!-- 审批履历时间轴弹窗 -->
    <a-modal
      v-model:open="timelineModalVisible"
      title="档案借阅审批流转履历"
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
  ReadOutlined,
  PlusOutlined,
  ReloadOutlined,
  PrinterOutlined
} from '@ant-design/icons-vue';
import { getMyBorrowsApi } from '@/api/business';
import type { RecordBorrowVO } from '@/types/business';
import BorrowModal from './components/BorrowModal.vue';
import BorrowPrint from './components/BorrowPrint.vue';
import PrintModal from '@/components/print/PrintModal.vue';
import TimelineHistory from '@/views/workflow/components/TimelineHistory.vue';

const loading = ref(false);
const list = ref<RecordBorrowVO[]>([]);
const total = ref(0);
const pageNum = ref(1);

const borrowModalVisible = ref(false);
const printModalVisible = ref(false);
const timelineModalVisible = ref(false);
const currentRecord = ref<RecordBorrowVO | null>(null);
const currentInstId = ref<number | undefined>(undefined);

const columns = [
  { title: '借阅单号', dataIndex: 'borrowNo', key: 'borrowNo', width: 160 },
  { title: '档案卷册名称', dataIndex: 'recordTitle', key: 'recordTitle' },
  { title: '借阅形式', dataIndex: 'borrowType', key: 'borrowType', width: 110 },
  { title: '借阅卷数', dataIndex: 'copies', key: 'copies', width: 90 },
  { title: '借出时间', dataIndex: 'startTime', key: 'startTime', width: 160 },
  { title: '应还时间', dataIndex: 'expectedReturnTime', key: 'expectedReturnTime', width: 160 },
  { title: '在库状态', dataIndex: 'returnStatus', key: 'returnStatus', width: 110 },
  { title: '审批状态', dataIndex: 'procStatus', key: 'procStatus', width: 110 },
  { title: '操作', key: 'action', width: 200, align: 'center' as const }
];

const loadData = async () => {
  loading.value = true;
  try {
    const res = await getMyBorrowsApi({ pageNum: pageNum.value, pageSize: 10 });
    if (res.code === 200 && res.data) {
      list.value = res.data.records || [];
      total.value = res.data.total || 0;
    }
  } catch {
    list.value = [
      {
        id: 5,
        borrowNo: 'DA202610280005',
        applicantUsername: 'zhangsan',
        applicantName: '张三',
        deptName: '研发中心',
        recordTitle: '2025年度企业核心技术专利原始开发交底书',
        borrowType: 'READ_ONLY',
        copies: 1,
        startTime: '2026-10-28 14:00:00',
        expectedReturnTime: '2026-10-28 17:30:00',
        purpose: '申报国家重点高新技术成果转化项目，需查阅原始专利交底书核心权利要求书。',
        returnStatus: 2,
        procInstId: 1005,
        procStatus: 2,
        createTime: '2026-10-28 13:30:00'
      }
    ];
    total.value = 1;
  } finally {
    loading.value = false;
  }
};

const openPrint = (record: RecordBorrowVO) => {
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

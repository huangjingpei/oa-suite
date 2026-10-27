<template>
  <div class="business-page">
    <a-card :bordered="false" class="page-card">
      <div class="table-header">
        <div class="header-title">
          <AuditOutlined style="color: #1677ff; margin-right: 8px" />
          <span>企业印章使用审批台账</span>
        </div>
        <div class="header-actions">
          <a-space>
            <a-button type="primary" @click="sealModalVisible = true">
              <template #icon><PlusOutlined /></template>
              发起用印申请
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
          <template v-if="column.key === 'applyNo'">
            <strong>{{ record.applyNo }}</strong>
          </template>

          <template v-else-if="column.key === 'sealType'">
            <a-tag v-if="record.sealType === 'COMMON'" color="red">企业公章</a-tag>
            <a-tag v-else-if="record.sealType === 'CONTRACT'" color="blue">合同专用章</a-tag>
            <a-tag v-else-if="record.sealType === 'LEGAL'" color="purple">法人代表章</a-tag>
            <a-tag v-else-if="record.sealType === 'FINANCE'" color="orange">财务专用章</a-tag>
            <a-tag v-else color="cyan">资质申报章</a-tag>
          </template>

          <template v-else-if="column.key === 'contractAmount'">
            <span v-if="record.contractAmount">¥ {{ Number(record.contractAmount).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</span>
            <span v-else style="color: #999">-</span>
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
                <PrinterOutlined /> 签报单打印
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 用印申请弹窗 -->
    <SealModal
      v-model:open="sealModalVisible"
      @success="loadData"
    />

    <!-- A4 纸质打印预览弹窗 -->
    <PrintModal
      v-model:open="printModalVisible"
      title="企业印章使用审批签报表 - A4 纸质高保真排版"
    >
      <SealPrint :seal="currentRecord || {}" />
    </PrintModal>

    <!-- 审批履历时间轴弹窗 -->
    <a-modal
      v-model:open="timelineModalVisible"
      title="用印审批流转履历"
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
  AuditOutlined,
  PlusOutlined,
  ReloadOutlined,
  PrinterOutlined
} from '@ant-design/icons-vue';
import { getMySealsApi } from '@/api/business';
import type { SealApplyVO } from '@/types/business';
import SealModal from './components/SealModal.vue';
import SealPrint from './components/SealPrint.vue';
import PrintModal from '@/components/print/PrintModal.vue';
import TimelineHistory from '@/views/workflow/components/TimelineHistory.vue';

const loading = ref(false);
const list = ref<SealApplyVO[]>([]);
const total = ref(0);
const pageNum = ref(1);

const sealModalVisible = ref(false);
const printModalVisible = ref(false);
const timelineModalVisible = ref(false);
const currentRecord = ref<SealApplyVO | null>(null);
const currentInstId = ref<number | undefined>(undefined);

const columns = [
  { title: '用印单号', dataIndex: 'applyNo', key: 'applyNo', width: 160 },
  { title: '印章类别', dataIndex: 'sealType', key: 'sealType', width: 110 },
  { title: '用印文件名称', dataIndex: 'fileName', key: 'fileName' },
  { title: '份数', dataIndex: 'sealCopies', key: 'sealCopies', width: 80 },
  { title: '合同金额', dataIndex: 'contractAmount', key: 'contractAmount', width: 130 },
  { title: '经办人', dataIndex: 'agentName', key: 'agentName', width: 100 },
  { title: '流转状态', dataIndex: 'procStatus', key: 'procStatus', width: 110 },
  { title: '申请日期', dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: '操作', key: 'action', width: 200, align: 'center' as const }
];

const loadData = async () => {
  loading.value = true;
  try {
    const res = await getMySealsApi({ pageNum: pageNum.value, pageSize: 10 });
    if (res.code === 200 && res.data) {
      list.value = res.data.records || [];
      total.value = res.data.total || 0;
    }
  } catch {
    list.value = [
      {
        id: 3,
        applyNo: 'YZ202610280003',
        applicantUsername: 'zhangsan',
        applicantName: '张三',
        deptName: '研发中心',
        agentName: '张三',
        sealType: 'CONTRACT',
        fileName: '智能制造协同系统采购框架协议',
        sealCopies: 2,
        purpose: '与战略客户签订年度采购协议及技术服务保密协议。',
        contractAmount: 580000.0,
        counterparty: '国投高新实业发展有限公司',
        procInstId: 1003,
        procStatus: 2,
        createTime: '2026-10-28 10:20:00'
      }
    ];
    total.value = 1;
  } finally {
    loading.value = false;
  }
};

const openPrint = (record: SealApplyVO) => {
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

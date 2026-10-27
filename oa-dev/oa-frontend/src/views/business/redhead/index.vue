<template>
  <div class="business-page">
    <a-card :bordered="false" class="page-card">
      <div class="table-header">
        <div class="header-title">
          <FileDoneOutlined style="color: #e53935; margin-right: 8px" />
          <span>红头公文拟发与全员公文大厅</span>
        </div>
        <div class="header-actions">
          <a-space>
            <a-button type="primary" danger @click="redheadModalVisible = true">
              <template #icon><PlusOutlined /></template>
              起草拟办公文
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
          <template v-if="column.key === 'docNo'">
            <span style="font-family: SimHei; font-weight: bold; color: #e53935">{{ record.docNo }}</span>
          </template>

          <template v-else-if="column.key === 'title'">
            <a href="javascript:;" style="font-weight: 500; color: #1f1f1f" @click="openPrint(record)">
              {{ record.title }}
            </a>
          </template>

          <template v-else-if="column.key === 'docType'">
            <a-tag v-if="record.docType === 'NOTICE'" color="red">通知</a-tag>
            <a-tag v-else-if="record.docType === 'DECISION'" color="purple">决议</a-tag>
            <a-tag v-else-if="record.docType === 'CIRCULAR'" color="orange">通报</a-tag>
            <a-tag v-else color="blue">指令</a-tag>
          </template>

          <template v-else-if="column.key === 'securityLevel'">
            <a-tag v-if="record.securityLevel === 'CONFIDENTIAL'" color="error">★ 机密</a-tag>
            <a-tag v-else-if="record.securityLevel === 'SECRET'" color="warning">★ 秘密</a-tag>
            <a-tag v-else color="default">普件</a-tag>
          </template>

          <template v-else-if="column.key === 'procStatus'">
            <a-tag v-if="record.procStatus === 0" color="default">草稿待发</a-tag>
            <a-tag v-else-if="record.procStatus === 1" color="processing">会签审核中</a-tag>
            <a-tag v-else-if="record.procStatus === 2" color="green">已正式印发</a-tag>
            <a-tag v-else-if="record.procStatus === 3" color="red">已退回</a-tag>
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
                会签轨迹
              </a-button>
              <a-button
                type="link"
                danger
                size="small"
                @click="openPrint(record)"
              >
                <PrinterOutlined /> 红头排版预览/打印
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 公文拟办弹窗 -->
    <RedheadModal
      v-model:open="redheadModalVisible"
      @success="loadData"
    />

    <!-- 红头公文 A4 纸质高保真排版打印弹窗 -->
    <PrintModal
      v-model:open="printModalVisible"
      title="正式红头公文排版与套红印章预览"
    >
      <RedheadPrint :doc="currentRecord || {}" />
    </PrintModal>

    <!-- 审批履历时间轴弹窗 -->
    <a-modal
      v-model:open="timelineModalVisible"
      title="红头公文会签与签发履历"
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
  FileDoneOutlined,
  PlusOutlined,
  ReloadOutlined,
  PrinterOutlined
} from '@ant-design/icons-vue';
import { getListRedheadApi } from '@/api/business';
import type { RedheadDocVO } from '@/types/business';
import RedheadModal from './components/RedheadModal.vue';
import RedheadPrint from './components/RedheadPrint.vue';
import PrintModal from '@/components/print/PrintModal.vue';
import TimelineHistory from '@/views/workflow/components/TimelineHistory.vue';

const loading = ref(false);
const list = ref<RedheadDocVO[]>([]);
const total = ref(0);
const pageNum = ref(1);

const redheadModalVisible = ref(false);
const printModalVisible = ref(false);
const timelineModalVisible = ref(false);
const currentRecord = ref<RedheadDocVO | null>(null);
const currentInstId = ref<number | undefined>(undefined);

const columns = [
  { title: '发文字号', dataIndex: 'docNo', key: 'docNo', width: 170 },
  { title: '公文标题', dataIndex: 'title', key: 'title' },
  { title: '公文类型', dataIndex: 'docType', key: 'docType', width: 90 },
  { title: '密级', dataIndex: 'securityLevel', key: 'securityLevel', width: 90 },
  { title: '签发人', dataIndex: 'signerName', key: 'signerName', width: 100 },
  { title: '发布状态', dataIndex: 'procStatus', key: 'procStatus', width: 110 },
  { title: '印发日期', dataIndex: 'publishDate', key: 'publishDate', width: 130 },
  { title: '操作', key: 'action', width: 230, align: 'center' as const }
];

const loadData = async () => {
  loading.value = true;
  try {
    const res = await getListRedheadApi({ pageNum: pageNum.value, pageSize: 10 });
    if (res.code === 200 && res.data) {
      list.value = res.data.records || [];
      total.value = res.data.total || 0;
    }
  } catch {
    list.value = [
      {
        id: 4,
        docNo: '启航发〔2026〕18号',
        title: '关于印发 2026 年度科技创新与协同办公数字化转型实施纲要的通知',
        docType: 'NOTICE',
        securityLevel: 'NORMAL',
        issuerDept: '启航智慧协同科技有限公司',
        drafterName: '行政部',
        signerName: '王董事长',
        targetZone: '集团各中心、各部门、直属分公司',
        content: '为深入贯彻落实数字化转型战略布局，全面提升集团协同研发、规范化行政流转与智能敏捷办公水平，经集团董事会与总裁办公会联合研究决定，特制定本纲要。',
        publishDate: '2026-10-28',
        procInstId: 1004,
        procStatus: 2,
        createTime: '2026-10-28 11:00:00'
      }
    ];
    total.value = 1;
  } finally {
    loading.value = false;
  }
};

const openPrint = (record: RedheadDocVO) => {
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

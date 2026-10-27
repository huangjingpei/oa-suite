<template>
  <div class="timeline-container">
    <a-spin :spinning="loading">
      <a-empty v-if="historyList.length === 0" description="暂无审批记录" />
      <a-timeline v-else mode="left">
        <a-timeline-item
          v-for="item in historyList"
          :key="item.id"
          :color="getActionColor(item.action)"
        >
          <template #dot>
            <component :is="getActionIcon(item.action)" />
          </template>
          <div class="timeline-item-content">
            <div class="timeline-header">
              <span class="node-name">{{ item.nodeName }}</span>
              <a-tag :color="getActionColor(item.action)">{{ getActionText(item.action) }}</a-tag>
              <span class="time">{{ item.createTime }}</span>
            </div>
            <div class="operator-info">
              操作人：<strong>{{ item.operatorName }}</strong> ({{ item.operatorUsername }})
              <span v-if="item.targetAssignee" class="target-user">
                ➔ 指派给：{{ item.targetAssignee }}
              </span>
            </div>
            <div v-if="item.opinion" class="opinion-box">
              批注意见：{{ item.opinion }}
            </div>
          </div>
        </a-timeline-item>
      </a-timeline>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { getProcessHistoryApi } from '@/api/workflow';
import type { ProcessHistoryVO } from '@/types/workflow';
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  UserAddOutlined,
  SendOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue';

const props = defineProps<{
  instId?: number;
}>();

const loading = ref(false);
const historyList = ref<ProcessHistoryVO[]>([]);

const loadHistory = async () => {
  if (!props.instId) {
    historyList.value = [];
    return;
  }
  loading.value = true;
  try {
    const res: any = await getProcessHistoryApi(props.instId);
    historyList.value = Array.isArray(res?.data) ? res.data : (res?.data?.data || []);
  } catch (e) {
    console.error('加载审批履历失败', e);
  } finally {
    loading.value = false;
  }
};

watch(() => props.instId, () => {
  loadHistory();
});

onMounted(() => {
  loadHistory();
});

const getActionColor = (action: string) => {
  switch (action) {
    case 'AGREE':
      return 'green';
    case 'REJECT':
      return 'red';
    case 'ADDSIGN':
      return 'purple';
    case 'SUBMIT':
      return 'blue';
    case 'CANCEL':
      return 'gray';
    default:
      return 'blue';
  }
};

const getActionText = (action: string) => {
  switch (action) {
    case 'AGREE':
      return '同意通过';
    case 'REJECT':
      return '审批驳回';
    case 'ADDSIGN':
      return '加签流转';
    case 'SUBMIT':
      return '提交发起';
    case 'CANCEL':
      return '已作废';
    default:
      return action;
  }
};

const getActionIcon = (action: string) => {
  switch (action) {
    case 'AGREE':
      return CheckCircleOutlined;
    case 'REJECT':
      return CloseCircleOutlined;
    case 'ADDSIGN':
      return UserAddOutlined;
    case 'SUBMIT':
      return SendOutlined;
    default:
      return ClockCircleOutlined;
  }
};
</script>

<style scoped>
.timeline-container {
  padding: 16px 8px;
  max-height: 480px;
  overflow-y: auto;
}

.timeline-item-content {
  font-size: 13px;
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.node-name {
  font-weight: 600;
  color: #262626;
}

.time {
  font-size: 12px;
  color: #8c8c8c;
  margin-left: auto;
}

.operator-info {
  color: #595959;
  margin-bottom: 4px;
}

.target-user {
  margin-left: 8px;
  color: #722ed1;
  font-weight: 500;
}

.opinion-box {
  background: #f5f5f5;
  border-left: 3px solid #1677ff;
  padding: 6px 12px;
  border-radius: 4px;
  color: #262626;
  margin-top: 6px;
}
</style>

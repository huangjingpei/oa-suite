<template>
  <a-modal
    v-model:open="visible"
    :title="'审批流转处理 - ' + (task?.title || '')"
    width="680px"
    :confirm-loading="submitting"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-tabs v-model:activeKey="activeTab">
      <a-tab-pane key="action" tab="审批办理">
        <a-descriptions :column="2" bordered size="small" style="margin-bottom: 20px">
          <a-descriptions-item label="单据标题" :span="2">
            <strong>{{ task?.title }}</strong>
          </a-descriptions-item>
          <a-descriptions-item label="发起人">
            {{ task?.initiatorName }} ({{ task?.initiatorUsername }})
          </a-descriptions-item>
          <a-descriptions-item label="申请部门">
            {{ task?.deptName || '未分配' }}
          </a-descriptions-item>
          <a-descriptions-item label="当前审批节点">
            <a-tag color="processing">{{ task?.nodeName }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="到达时间">
            {{ task?.createTime }}
          </a-descriptions-item>
        </a-descriptions>

        <a-form layout="vertical">
          <a-form-item label="审批决断" required>
            <a-radio-group v-model:value="actionType" button-style="solid">
              <a-radio-button value="AGREE">
                <CheckOutlined /> 同意通过
              </a-radio-button>
              <a-radio-button value="REJECT">
                <CloseOutlined /> 审批驳回
              </a-radio-button>
              <a-radio-button value="ADDSIGN">
                <UserAddOutlined /> 动态加签
              </a-radio-button>
            </a-radio-group>
          </a-form-item>

          <!-- 加签时选择目标加签人 -->
          <a-form-item
            v-if="actionType === 'ADDSIGN'"
            label="指派加签审批人"
            required
          >
            <a-select
              v-model:value="addSignUsername"
              placeholder="请选择加签流转的员工"
              style="width: 100%"
              show-search
            >
              <a-select-option
                v-for="u in userOptions"
                :key="u.username"
                :value="u.username"
              >
                {{ u.realName }} ({{ u.postName || u.username }}) - {{ u.deptName || '部门' }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="批注意见说明" :required="actionType === 'REJECT'">
            <a-textarea
              v-model:value="opinion"
              :rows="4"
              :placeholder="actionType === 'REJECT' ? '请务必输入驳回原因...' : '请输入审批意见批注 (如：情况属实，同意申请)'"
            />
          </a-form-item>
        </a-form>
      </a-tab-pane>

      <a-tab-pane key="timeline" tab="审批历史履历">
        <TimelineHistory :inst-id="task?.instId" />
      </a-tab-pane>
    </a-tabs>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { CheckOutlined, CloseOutlined, UserAddOutlined } from '@ant-design/icons-vue';
import { approveTaskApi, rejectTaskApi, addSignTaskApi } from '@/api/workflow';
import { listUserOptionsApi } from '@/api/user';
import type { TaskVO } from '@/types/workflow';
import type { UserVO } from '@/types/user';
import TimelineHistory from './TimelineHistory.vue';

const props = defineProps<{
  open: boolean;
  task: TaskVO | null;
}>();

const emit = defineEmits<{
  (e: 'update:open', val: boolean): void;
  (e: 'success'): void;
}>();

const visible = ref(props.open);
const activeTab = ref('action');
const actionType = ref<'AGREE' | 'REJECT' | 'ADDSIGN'>('AGREE');
const opinion = ref('');
const addSignUsername = ref('');
const submitting = ref(false);
const userOptions = ref<UserVO[]>([]);

watch(() => props.open, (val) => {
  visible.value = val;
  if (val) {
    activeTab.value = 'action';
    actionType.value = 'AGREE';
    opinion.value = '';
    addSignUsername.value = '';
  }
});

watch(visible, (val) => {
  emit('update:open', val);
});

onMounted(async () => {
  try {
    const res = await listUserOptionsApi();
    userOptions.value = res.data.data || [];
  } catch (ignored) {
  }
});

const handleCancel = () => {
  visible.value = false;
};

const handleSubmit = async () => {
  if (!props.task) return;

  if (actionType.value === 'REJECT' && !opinion.value.trim()) {
    message.warning('请填写驳回原因说明');
    return;
  }
  if (actionType.value === 'ADDSIGN' && !addSignUsername.value) {
    message.warning('请选择需要加签的审批人');
    return;
  }

  submitting.value = true;
  try {
    if (actionType.value === 'AGREE') {
      await approveTaskApi({
        taskId: props.task.taskId,
        opinion: opinion.value || '同意通过'
      });
      message.success('已同意并流转该审批');
    } else if (actionType.value === 'REJECT') {
      await rejectTaskApi({
        taskId: props.task.taskId,
        opinion: opinion.value
      });
      message.success('已驳回该审批申请');
    } else if (actionType.value === 'ADDSIGN') {
      await addSignTaskApi({
        taskId: props.task.taskId,
        targetUsername: addSignUsername.value,
        opinion: opinion.value || '加签流转'
      });
      message.success('加签成功');
    }

    visible.value = false;
    emit('success');
  } catch (error) {
    // 统一提示已在 request 拦截器中处理
  } finally {
    submitting.value = false;
  }
};
</script>

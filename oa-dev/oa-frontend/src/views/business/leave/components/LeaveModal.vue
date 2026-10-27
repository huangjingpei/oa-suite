<template>
  <a-modal
    v-model:open="visible"
    title="发起请假申请"
    width="680px"
    destroy-on-close
    :confirm-loading="submitting"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      layout="vertical"
    >
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="请假类别" name="leaveType">
            <a-select v-model:value="formData.leaveType" placeholder="请选择假期类别">
              <a-select-option value="PERSONAL">事假</a-select-option>
              <a-select-option value="SICK">病假</a-select-option>
              <a-select-option value="ANNUAL">年休假</a-select-option>
              <a-select-option value="REST">调休假</a-select-option>
              <a-select-option value="MARRIAGE">婚假</a-select-option>
              <a-select-option value="MATERNITY">产假 / 陪产假</a-select-option>
              <a-select-option value="FUNERAL">丧假</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>

        <a-col :span="12">
          <a-form-item label="请假时间范围" name="timeRange">
            <a-range-picker
              v-model:value="timeRange"
              show-time
              format="YYYY-MM-DD HH:mm"
              style="width: 100%"
              @change="handleTimeChange"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="请假折算天数" name="totalDays">
            <a-input-number
              v-model:value="formData.totalDays"
              :min="0.5"
              :step="0.5"
              addon-after="天"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>

        <a-col :span="12">
          <a-form-item label="总工时（小时）" name="totalHours">
            <a-input-number
              v-model:value="formData.totalHours"
              :min="1"
              :step="1"
              addon-after="小时"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="请假事由" name="reason">
        <a-textarea
          v-model:value="formData.reason"
          placeholder="请详细说明请假原因及工作交接情况"
          :rows="3"
          show-count
          :maxlength="300"
        />
      </a-form-item>

      <a-form-item label="自选用印/审批人（选填，未指定时默认按岗位主管分配）">
        <a-select
          v-model:value="selectedApprovers"
          mode="multiple"
          placeholder="可按顺序添加指定审批人员"
          :options="userOptions"
          style="width: 100%"
        />
      </a-form-item>
    </a-form>

    <template #footer>
      <a-space>
        <a-button @click="visible = false">取消</a-button>
        <a-button :loading="submitting" @click="handleSubmit(true)">暂存为草稿</a-button>
        <a-button type="primary" :loading="submitting" @click="handleSubmit(false)">立即提交发起审批</a-button>
      </a-space>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import type { FormInstance, Rule } from 'ant-design-vue/es/form';
import { message } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import { applyLeaveApi } from '@/api/business';
import { getUserListApi } from '@/api/user';
import type { LeaveApplyDTO, LeaveType } from '@/types/business';

const props = defineProps<{
  open: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:open', val: boolean): void;
  (e: 'success'): void;
}>();

const visible = computed({
  get: () => props.open,
  set: (val: boolean) => emit('update:open', val)
});

const formRef = ref<FormInstance>();
const submitting = ref(false);
const timeRange = ref<[Dayjs, Dayjs] | null>(null);
const selectedApprovers = ref<string[]>([]);
const userOptions = ref<{ label: string; value: string }[]>([]);

const formData = reactive<{
  leaveType: LeaveType;
  startTime: string;
  endTime: string;
  totalDays: number;
  totalHours: number;
  reason: string;
}>({
  leaveType: 'PERSONAL',
  startTime: '',
  endTime: '',
  totalDays: 1,
  totalHours: 8,
  reason: ''
});

const formRules: Record<string, Rule[]> = {
  leaveType: [{ required: true, message: '请选择请假类别', trigger: 'change' }],
  reason: [{ required: true, message: '请输入请假事由', trigger: 'blur' }]
};

const handleTimeChange = (dates: [Dayjs, Dayjs] | null) => {
  if (dates && dates[0] && dates[1]) {
    formData.startTime = dates[0].format('YYYY-MM-DD HH:mm:ss');
    formData.endTime = dates[1].format('YYYY-MM-DD HH:mm:ss');
    const diffHours = Math.max(1, Math.round(dates[1].diff(dates[0], 'hour', true)));
    formData.totalHours = diffHours;
    formData.totalDays = Number((diffHours / 8).toFixed(1)) || 1;
  }
};

const handleCancel = () => {
  formRef.value?.resetFields();
};

const handleSubmit = async (isDraft: boolean) => {
  try {
    if (!isDraft) {
      await formRef.value?.validate();
      if (!formData.startTime || !formData.endTime) {
        message.warning('请选择请假起止时间范围');
        return;
      }
    }

    submitting.value = true;
    const payload: LeaveApplyDTO = {
      ...formData,
      isDraft,
      auditUsernames: selectedApprovers.value.length ? selectedApprovers.value : undefined
    };

    const res = await applyLeaveApi(payload);
    if (res.code === 200) {
      message.success(isDraft ? '已暂存请假草稿' : '请假申请已提交并成功启动流转');
      visible.value = false;
      emit('success');
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  try {
    const res = await getUserListApi();
    if (res.code === 200 && res.data) {
      userOptions.value = res.data.map((u: any) => ({
        label: `${u.realName} (${u.username} - ${u.deptName || '部门'})`,
        value: u.username
      }));
    }
  } catch {}
});
</script>

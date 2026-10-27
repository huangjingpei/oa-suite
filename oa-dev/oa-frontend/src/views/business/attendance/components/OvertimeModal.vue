<template>
  <a-modal
    v-model:open="visible"
    title="发起员工加班申报单"
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
        <a-col :span="16">
          <a-form-item label="加班时间范围" name="timeRange">
            <a-range-picker
              v-model:value="timeRange"
              show-time
              format="YYYY-MM-DD HH:mm"
              style="width: 100%"
              @change="handleTimeChange"
            />
          </a-form-item>
        </a-col>

        <a-col :span="8">
          <a-form-item label="核算加班工时" name="totalHours">
            <a-input-number
              v-model:value="formData.totalHours"
              :min="0.5"
              :step="0.5"
              addon-after="小时"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="加班工作内容及任务交付说明" name="reason">
        <a-textarea
          v-model:value="formData.reason"
          placeholder="请详细说明因何紧急业务需加班、工作事项安排及预期交付成果"
          :rows="4"
          show-count
          :maxlength="400"
        />
      </a-form-item>

      <a-form-item label="指定流程审批人（选填，未指定时默认按直属部门主管流转）">
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
import { applyOvertimeApi } from '@/api/business';
import { getUserListApi } from '@/api/user';
import type { OvertimeApplyDTO } from '@/types/business';

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
  startTime: string;
  endTime: string;
  totalHours: number;
  reason: string;
}>({
  startTime: '',
  endTime: '',
  totalHours: 4,
  reason: ''
});

const formRules: Record<string, Rule[]> = {
  reason: [{ required: true, message: '请输入加班工作内容及事由', trigger: 'blur' }]
};

const handleTimeChange = (dates: [Dayjs, Dayjs] | null) => {
  if (dates && dates[0] && dates[1]) {
    formData.startTime = dates[0].format('YYYY-MM-DD HH:mm:ss');
    formData.endTime = dates[1].format('YYYY-MM-DD HH:mm:ss');
    const diffHours = Math.round(dates[1].diff(dates[0], 'minute') / 30) / 2;
    if (diffHours > 0) {
      formData.totalHours = diffHours;
    }
  } else {
    formData.startTime = '';
    formData.endTime = '';
  }
};

const handleCancel = () => {
  visible.value = false;
};

const handleSubmit = async (isDraft: boolean) => {
  try {
    await formRef.value?.validate();
    if (!formData.startTime || !formData.endTime) {
      message.warning('请选择加班起止时间范围');
      return;
    }

    submitting.value = true;
    const dto: OvertimeApplyDTO = {
      startTime: formData.startTime,
      endTime: formData.endTime,
      totalHours: formData.totalHours,
      reason: formData.reason,
      isDraft,
      auditUsernames: selectedApprovers.value.length > 0 ? selectedApprovers.value : undefined
    };

    const res = await applyOvertimeApi(dto);
    if (res.code === 200) {
      message.success(isDraft ? '加班草稿已保存' : '加班申报单已提交流转');
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
    if (res.data) {
      userOptions.value = res.data.map(u => ({
        label: `${u.realName} (${u.deptName || '未归属'} - ${u.workNo || u.username})`,
        value: u.username
      }));
    }
  } catch (ignored) {
  }
});
</script>

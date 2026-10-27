<template>
  <div class="mobile-apply-page">
    <a-card :bordered="false" class="apply-card">
      <a-form layout="vertical">
        <a-form-item label="请假类别" required>
          <a-select v-model:value="formData.leaveType" style="width: 100%">
            <a-select-option value="PERSONAL">事假</a-select-option>
            <a-select-option value="SICK">病假</a-select-option>
            <a-select-option value="ANNUAL">年休假</a-select-option>
            <a-select-option value="REST">调休假</a-select-option>
            <a-select-option value="MARRIAGE">婚假</a-select-option>
            <a-select-option value="MATERNITY">产假/陪产假</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="开始时间" required>
          <a-date-picker
            v-model:value="startDate"
            show-time
            format="YYYY-MM-DD HH:mm"
            style="width: 100%"
            placeholder="选择请假起始时刻"
          />
        </a-form-item>

        <a-form-item label="结束时间" required>
          <a-date-picker
            v-model:value="endDate"
            show-time
            format="YYYY-MM-DD HH:mm"
            style="width: 100%"
            placeholder="选择请假截止时刻"
          />
        </a-form-item>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="请假天数">
              <a-input-number v-model:value="formData.totalDays" :min="0.5" :step="0.5" addon-after="天" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="总工时">
              <a-input-number v-model:value="formData.totalHours" :min="1" :step="1" addon-after="小时" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="请假详细事由" required>
          <a-textarea v-model:value="formData.reason" placeholder="说明请假原因及工作交接情况" :rows="3" />
        </a-form-item>

        <div style="margin-top: 24px">
          <a-button type="primary" block size="large" :loading="submitting" @click="handleSubmit">
            立即提交请假申请
          </a-button>
        </div>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { Dayjs } from 'dayjs';
import { applyLeaveApi } from '@/api/business';
import type { LeaveApplyDTO, LeaveType } from '@/types/business';

const router = useRouter();
const submitting = ref(false);
const startDate = ref<Dayjs | null>(null);
const endDate = ref<Dayjs | null>(null);

const formData = reactive<{
  leaveType: LeaveType;
  totalDays: number;
  totalHours: number;
  reason: string;
}>({
  leaveType: 'PERSONAL',
  totalDays: 1,
  totalHours: 8,
  reason: ''
});

const handleSubmit = async () => {
  if (!startDate.value || !endDate.value) {
    message.warning('请选择请假起止时间');
    return;
  }
  if (!formData.reason.trim()) {
    message.warning('请填写请假事由');
    return;
  }

  try {
    submitting.value = true;
    const payload: LeaveApplyDTO = {
      leaveType: formData.leaveType,
      startTime: startDate.value.format('YYYY-MM-DD HH:mm:ss'),
      endTime: endDate.value.format('YYYY-MM-DD HH:mm:ss'),
      totalDays: formData.totalDays,
      totalHours: formData.totalHours,
      reason: formData.reason,
      isDraft: false
    };

    const res = await applyLeaveApi(payload);
    if (res.code === 200) {
      message.success('请假申请已提交成功！');
      router.push('/m/dashboard');
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.apply-card {
  border-radius: 12px;
}
</style>

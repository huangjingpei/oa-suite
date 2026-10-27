<template>
  <a-modal
    v-model:open="visible"
    title="发起考勤异常漏卡补卡申请"
    width="760px"
    destroy-on-close
    :confirm-loading="submitting"
  >
    <a-form layout="vertical">
      <a-form-item label="总体异常原因说明" required>
        <a-textarea
          v-model:value="reason"
          placeholder="请简要说明漏打卡原因（如：因公外出驻场、机房设备故障无法指纹签到等）"
          :rows="2"
        />
      </a-form-item>

      <div style="margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center">
        <span style="font-weight: 600; color: #333">补卡明细清单</span>
        <a-button type="dashed" size="small" @click="addDetailItem">
          + 添加补卡明细行
        </a-button>
      </div>

      <a-table
        :columns="columns"
        :data-source="details"
        :pagination="false"
        size="small"
        bordered
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'repairDate'">
            <a-date-picker
              v-model:value="record.dateVal"
              value-format="YYYY-MM-DD"
              placeholder="选择漏卡日期"
              style="width: 100%"
            />
          </template>

          <template v-else-if="column.key === 'repairType'">
            <a-select v-model:value="record.repairType" style="width: 100%">
              <a-select-option value="CHECK_IN">上班卡</a-select-option>
              <a-select-option value="CHECK_OUT">下班卡</a-select-option>
              <a-select-option value="ALL">全天</a-select-option>
            </a-select>
          </template>

          <template v-else-if="column.key === 'repairSignTime'">
            <a-time-picker
              v-model:value="record.timeVal"
              format="HH:mm:ss"
              value-format="HH:mm:ss"
              placeholder="应打卡时间"
              style="width: 100%"
            />
          </template>

          <template v-else-if="column.key === 'remark'">
            <a-input v-model:value="record.remark" placeholder="事由及证明人" />
          </template>

          <template v-else-if="column.key === 'action'">
            <a-button
              type="link"
              danger
              size="small"
              :disabled="details.length <= 1"
              @click="removeDetailItem(index)"
            >
              删除
            </a-button>
          </template>
        </template>
      </a-table>

      <a-form-item label="指定审批人（选填）" style="margin-top: 16px">
        <a-select
          v-model:value="selectedApprovers"
          mode="multiple"
          placeholder="可指定直属主管审批"
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
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import { applyRepairApi } from '@/api/business';
import { getUserListApi } from '@/api/user';
import type { AttendanceRepairDTO } from '@/types/business';

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

const submitting = ref(false);
const reason = ref('');
const selectedApprovers = ref<string[]>([]);
const userOptions = ref<{ label: string; value: string }[]>([]);

interface FormDetailRow {
  dateVal: string;
  repairType: 'CHECK_IN' | 'CHECK_OUT' | 'ALL';
  timeVal: string;
  remark: string;
}

const details = ref<FormDetailRow[]>([
  {
    dateVal: dayjs().format('YYYY-MM-DD'),
    repairType: 'CHECK_IN',
    timeVal: '09:00:00',
    remark: ''
  }
]);

const columns = [
  { title: '漏打卡日期', key: 'repairDate', width: 150 },
  { title: '补卡班次', key: 'repairType', width: 110 },
  { title: '应修正时间', key: 'repairSignTime', width: 130 },
  { title: '说明 / 证明人', key: 'remark' },
  { title: '操作', key: 'action', width: 70, align: 'center' as const }
];

const addDetailItem = () => {
  details.value.push({
    dateVal: dayjs().format('YYYY-MM-DD'),
    repairType: 'CHECK_OUT',
    timeVal: '18:00:00',
    remark: ''
  });
};

const removeDetailItem = (index: number) => {
  details.value.splice(index, 1);
};

const handleSubmit = async (isDraft: boolean) => {
  if (!isDraft && !reason.value.trim()) {
    message.warning('请输入补卡异常原因说明');
    return;
  }

  for (const item of details.value) {
    if (!item.dateVal || !item.timeVal) {
      message.warning('补卡明细的日期与时间均不能为空');
      return;
    }
  }

  try {
    submitting.value = true;
    const payload: AttendanceRepairDTO = {
      reason: reason.value,
      isDraft,
      auditUsernames: selectedApprovers.value.length ? selectedApprovers.value : undefined,
      details: details.value.map(d => ({
        repairDate: d.dateVal,
        repairType: d.repairType,
        repairSignTime: `${d.dateVal} ${d.timeVal}`,
        remark: d.remark
      }))
    };

    const res = await applyRepairApi(payload);
    if (res.code === 200) {
      message.success(isDraft ? '已暂存补卡草稿' : '补卡申请提交成功，已启动流程流转');
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

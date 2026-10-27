<template>
  <a-modal
    v-model:open="visible"
    title="发起档案资料借阅申请"
    width="680px"
    destroy-on-close
    :confirm-loading="submitting"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      layout="vertical"
    >
      <a-form-item label="档案/卷册名称" name="recordTitle">
        <a-input v-model:value="formData.recordTitle" placeholder="请输入档案全称或卷号（如：2025年度财务审计底稿）" />
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="借阅方式" name="borrowType">
            <a-select v-model:value="formData.borrowType">
              <a-select-option value="READ_ONLY">现场查阅（限阅览室）</a-select-option>
              <a-select-option value="BORROW_PHYSICAL">实体外借出库</a-select-option>
              <a-select-option value="COPY">复印 / 摘录留存</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="借阅卷数 / 份数" name="copies">
            <a-input-number v-model:value="formData.copies" :min="1" :step="1" style="width: 100%" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="借阅时间跨度" required>
        <a-range-picker
          v-model:value="timeRange"
          show-time
          format="YYYY-MM-DD HH:mm"
          style="width: 100%"
          @change="handleTimeChange"
        />
      </a-form-item>

      <a-form-item label="借阅事由与用途说明" name="purpose">
        <a-textarea
          v-model:value="formData.purpose"
          placeholder="请说明查阅目的及借阅合理性"
          :rows="3"
        />
      </a-form-item>

      <a-form-item label="指定部门主管/档案专管员（选填）">
        <a-select
          v-model:value="selectedApprovers"
          mode="multiple"
          placeholder="可指定审批人员"
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
import { Dayjs } from 'dayjs';
import { applyBorrowApi } from '@/api/business';
import { getUserListApi } from '@/api/user';
import type { RecordBorrowDTO, BorrowType } from '@/types/business';

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
  recordTitle: string;
  borrowType: BorrowType;
  copies: number;
  startTime: string;
  expectedReturnTime: string;
  purpose: string;
}>({
  recordTitle: '',
  borrowType: 'READ_ONLY',
  copies: 1,
  startTime: '',
  expectedReturnTime: '',
  purpose: ''
});

const formRules: Record<string, Rule[]> = {
  recordTitle: [{ required: true, message: '请输入档案/卷册名称', trigger: 'blur' }],
  purpose: [{ required: true, message: '请输入借阅事由', trigger: 'blur' }]
};

const handleTimeChange = (dates: [Dayjs, Dayjs] | null) => {
  if (dates && dates[0] && dates[1]) {
    formData.startTime = dates[0].format('YYYY-MM-DD HH:mm:ss');
    formData.expectedReturnTime = dates[1].format('YYYY-MM-DD HH:mm:ss');
  }
};

const handleSubmit = async (isDraft: boolean) => {
  try {
    if (!isDraft) {
      await formRef.value?.validate();
      if (!formData.startTime || !formData.expectedReturnTime) {
        message.warning('请选择计划借出与预计归还时间');
        return;
      }
    }

    submitting.value = true;
    const payload: RecordBorrowDTO = {
      ...formData,
      isDraft,
      auditUsernames: selectedApprovers.value.length ? selectedApprovers.value : undefined
    };

    const res = await applyBorrowApi(payload);
    if (res.code === 200) {
      message.success(isDraft ? '已暂存借阅草稿' : '档案借阅申请提交成功，已启动流程');
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

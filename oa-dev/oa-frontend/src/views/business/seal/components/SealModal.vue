<template>
  <a-modal
    v-model:open="visible"
    title="发起企业用印申请"
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
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="印章类型" name="sealType">
            <a-select v-model:value="formData.sealType">
              <a-select-option value="COMMON">企业公章</a-select-option>
              <a-select-option value="CONTRACT">合同专用章</a-select-option>
              <a-select-option value="LEGAL">法人代表名章</a-select-option>
              <a-select-option value="FINANCE">财务专用章</a-select-option>
              <a-select-option value="DECLARE">资质申报章</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>

        <a-col :span="12">
          <a-form-item label="盖章经办人" name="agentName">
            <a-input v-model:value="formData.agentName" placeholder="经办人姓名（默认为申请人本人）" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="16">
          <a-form-item label="用印文件名称" name="fileName">
            <a-input v-model:value="formData.fileName" placeholder="例如：2026年度业务采购框架合同" />
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <a-form-item label="用印份数" name="sealCopies">
            <a-input-number v-model:value="formData.sealCopies" :min="1" :step="1" style="width: 100%" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row v-if="formData.sealType === 'CONTRACT'" :gutter="16">
        <a-col :span="12">
          <a-form-item label="关联合同金额（元）">
            <a-input-number
              v-model:value="formData.contractAmount"
              :min="0"
              style="width: 100%"
              placeholder="请输入合同金额"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="相对方合作单位">
            <a-input v-model:value="formData.counterparty" placeholder="对方公司企业全称" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="用印用途及背景说明" name="purpose">
        <a-textarea
          v-model:value="formData.purpose"
          placeholder="请说明文件签署背景、对方合作资质及印章使用必要性"
          :rows="3"
        />
      </a-form-item>

      <a-form-item label="审批人账号选择（选填）">
        <a-select
          v-model:value="selectedApprovers"
          mode="multiple"
          placeholder="可指定部门领导与法务合规人员"
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
import { applySealApi } from '@/api/business';
import { getUserListApi } from '@/api/user';
import type { SealApplyDTO, SealType } from '@/types/business';

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
const selectedApprovers = ref<string[]>([]);
const userOptions = ref<{ label: string; value: string }[]>([]);

const formData = reactive<{
  sealType: SealType;
  fileName: string;
  sealCopies: number;
  agentName: string;
  contractAmount?: number;
  counterparty: string;
  purpose: string;
}>({
  sealType: 'COMMON',
  fileName: '',
  sealCopies: 2,
  agentName: '',
  contractAmount: undefined,
  counterparty: '',
  purpose: ''
});

const formRules: Record<string, Rule[]> = {
  sealType: [{ required: true, message: '请选择印章类型', trigger: 'change' }],
  fileName: [{ required: true, message: '请输入用印文件名称', trigger: 'blur' }],
  purpose: [{ required: true, message: '请输入用印用途及背景说明', trigger: 'blur' }]
};

const handleSubmit = async (isDraft: boolean) => {
  try {
    if (!isDraft) {
      await formRef.value?.validate();
    }

    submitting.value = true;
    const payload: SealApplyDTO = {
      ...formData,
      isDraft,
      auditUsernames: selectedApprovers.value.length ? selectedApprovers.value : undefined
    };

    const res = await applySealApi(payload);
    if (res.code === 200) {
      message.success(isDraft ? '已暂存用印草稿' : '用印申请提交成功，已启动流转审批');
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

<template>
  <a-modal
    v-model:open="visible"
    title="红头公文拟稿与拟发"
    width="780px"
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
          <a-form-item label="发文字号" name="docNo">
            <a-input v-model:value="formData.docNo" placeholder="例如：盛泰发〔2026〕05号" />
          </a-form-item>
        </a-col>
        <a-col :span="6">
          <a-form-item label="公文类型" name="docType">
            <a-select v-model:value="formData.docType">
              <a-select-option value="NOTICE">通知</a-select-option>
              <a-select-option value="DECISION">决定 / 决议</a-select-option>
              <a-select-option value="CIRCULAR">通报</a-select-option>
              <a-select-option value="ORDER">工作指令</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="6">
          <a-form-item label="公文密级" name="securityLevel">
            <a-select v-model:value="formData.securityLevel">
              <a-select-option value="NORMAL">普通公件</a-select-option>
              <a-select-option value="SECRET">内部秘密</a-select-option>
              <a-select-option value="CONFIDENTIAL">机密文件</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="公文标题" name="title">
        <a-input v-model:value="formData.title" placeholder="请输入公文标准标题（如：关于xxx的通知）" />
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="主送单位 / 受众范围" name="targetZone">
            <a-input v-model:value="formData.targetZone" placeholder="例如：各中心、直属分公司、全体员工" />
          </a-form-item>
        </a-col>
        <a-col :span="6">
          <a-form-item label="签发人">
            <a-input v-model:value="formData.signerName" placeholder="签发领导姓名" />
          </a-form-item>
        </a-col>
        <a-col :span="6">
          <a-form-item label="成文 / 印发日期">
            <a-date-picker v-model:value="publishDate" value-format="YYYY-MM-DD" style="width: 100%" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="公文正文内容" name="content">
        <a-textarea
          v-model:value="formData.content"
          placeholder="请输入公文正文内容（支持分段）..."
          :rows="6"
          show-count
        />
      </a-form-item>

      <a-form-item label="指定签发/会签人员（选填）">
        <a-select
          v-model:value="selectedApprovers"
          mode="multiple"
          placeholder="可指定会签与签发负责人"
          :options="userOptions"
          style="width: 100%"
        />
      </a-form-item>
    </a-form>

    <template #footer>
      <a-space>
        <a-button @click="visible = false">取消</a-button>
        <a-button :loading="submitting" @click="handleSubmit(true)">暂存为草稿</a-button>
        <a-button type="primary" :loading="submitting" @click="handleSubmit(false)">立即提交发起签发流</a-button>
      </a-space>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import type { FormInstance, Rule } from 'ant-design-vue/es/form';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import { draftRedheadApi } from '@/api/business';
import { getUserListApi } from '@/api/user';
import type { RedheadDocDTO, DocType, SecurityLevel } from '@/types/business';

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
const publishDate = ref<string>(dayjs().format('YYYY-MM-DD'));
const selectedApprovers = ref<string[]>([]);
const userOptions = ref<{ label: string; value: string }[]>([]);

const formData = reactive<{
  docNo: string;
  title: string;
  docType: DocType;
  securityLevel: SecurityLevel;
  targetZone: string;
  signerName: string;
  content: string;
}>({
  docNo: '盛泰发〔2026〕' + Math.floor(Math.random() * 89 + 10) + '号',
  title: '',
  docType: 'NOTICE',
  securityLevel: 'NORMAL',
  targetZone: '集团各中心、各部门、直属分公司',
  signerName: '王董事长',
  content: ''
});

const formRules: Record<string, Rule[]> = {
  docNo: [{ required: true, message: '请输入发文字号', trigger: 'blur' }],
  title: [{ required: true, message: '请输入公文标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文内容', trigger: 'blur' }]
};

const handleSubmit = async (isDraft: boolean) => {
  try {
    if (!isDraft) {
      await formRef.value?.validate();
    }

    submitting.value = true;
    const payload: RedheadDocDTO = {
      ...formData,
      publishDate: publishDate.value,
      isDraft,
      auditUsernames: selectedApprovers.value.length ? selectedApprovers.value : undefined
    };

    const res = await draftRedheadApi(payload);
    if (res.code === 200) {
      message.success(isDraft ? '已暂存拟稿公文' : '公文已提交并成功启动会签与签发流程');
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

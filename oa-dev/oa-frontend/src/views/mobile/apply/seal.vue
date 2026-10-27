<template>
  <div class="mobile-apply-page">
    <a-card :bordered="false" class="apply-card">
      <a-form layout="vertical">
        <a-form-item label="印章类别" required>
          <a-select v-model:value="formData.sealType" style="width: 100%">
            <a-select-option value="COMMON">企业公章</a-select-option>
            <a-select-option value="CONTRACT">合同专用章</a-select-option>
            <a-select-option value="LEGAL">法人代表名章</a-select-option>
            <a-select-option value="FINANCE">财务专用章</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="用印文件名称" required>
          <a-input v-model:value="formData.fileName" placeholder="例如：2026战略合作协议" />
        </a-form-item>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="用印份数" required>
              <a-input-number v-model:value="formData.sealCopies" :min="1" :step="1" addon-after="份" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="盖章经办人">
              <a-input v-model:value="formData.agentName" placeholder="经办人姓名" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="用途与事由说明" required>
          <a-textarea v-model:value="formData.purpose" placeholder="说明用印背景及必要性" :rows="3" />
        </a-form-item>

        <div style="margin-top: 24px">
          <a-button type="primary" block size="large" :loading="submitting" @click="handleSubmit">
            立即提交用印申请
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
import { applySealApi } from '@/api/business';
import type { SealApplyDTO, SealType } from '@/types/business';

const router = useRouter();
const submitting = ref(false);

const formData = reactive<{
  sealType: SealType;
  fileName: string;
  sealCopies: number;
  agentName: string;
  purpose: string;
}>({
  sealType: 'COMMON',
  fileName: '',
  sealCopies: 2,
  agentName: '',
  purpose: ''
});

const handleSubmit = async () => {
  if (!formData.fileName.trim()) {
    message.warning('请输入用印文件名称');
    return;
  }
  if (!formData.purpose.trim()) {
    message.warning('请输入用印用途');
    return;
  }

  try {
    submitting.value = true;
    const payload: SealApplyDTO = {
      sealType: formData.sealType,
      fileName: formData.fileName,
      sealCopies: formData.sealCopies,
      agentName: formData.agentName,
      purpose: formData.purpose,
      isDraft: false
    };

    const res = await applySealApi(payload);
    if (res.code === 200) {
      message.success('用印申请已提交成功！');
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

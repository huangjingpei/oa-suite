<template>
  <div class="notify-config-container">
    <a-alert
      message="💡 企微 / 钉钉 / 飞书 审批待办机器人直通中枢"
      description="无需耗费巨资自研即时通讯底层，直接利用企业现有的企业微信、钉钉或飞书群机器人 Webhook。当有员工发起请假、用印、公文流转时，系统自动秒级推送高优审批卡片，员工手机立即收到震动提醒，点击直接免密审批。"
      type="info"
      show-icon
      style="margin-bottom: 16px; border-radius: 8px"
    />

    <a-row :gutter="16">
      <!-- 左侧：机器人配置列表与维护 -->
      <a-col :xs="24" :lg="16">
        <a-card :bordered="false" class="main-card">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
            <h3 style="margin: 0; font-size: 16px; font-weight: 600">已接入的机器人通道 ({{ configList.length }})</h3>
            <a-button type="primary" @click="openAddModal">
              <template #icon><PlusOutlined /></template>
              接入新机器人
            </a-button>
          </div>

          <a-table
            :columns="columns"
            :data-source="configList"
            :loading="loading"
            row-key="id"
            :pagination="false"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'platform'">
                <a-tag :color="getPlatformColor(record.platform)" style="font-weight: 500">
                  {{ getPlatformName(record.platform) }}
                </a-tag>
              </template>

              <template v-else-if="column.key === 'status'">
                <a-switch
                  :checked="record.status === 1"
                  checked-children="已激活"
                  un-checked-children="已停用"
                  @change="(checked: any) => handleToggleStatus(record, !!checked)"
                />
              </template>

              <template v-else-if="column.key === 'notifyEvents'">
                <a-space size="small" wrap>
                  <a-tag v-if="record.notifyEvents?.includes('TASK_ASSIGNED')" color="blue">待办到达</a-tag>
                  <a-tag v-if="record.notifyEvents?.includes('PROCESS_FINISHED')" color="green">流程办结</a-tag>
                  <a-tag v-if="record.notifyEvents?.includes('NOTICE_PUBLISHED')" color="purple">公文通告</a-tag>
                </a-space>
              </template>

              <template v-else-if="column.key === 'action'">
                <a-space size="small">
                  <a-button
                    type="link"
                    size="small"
                    style="color: #52c41a"
                    :loading="testingId === record.id"
                    @click="handleSendTest(record)"
                  >
                    <template #icon><SendOutlined /></template>
                    发送测试卡片
                  </a-button>
                  <a-button type="link" size="small" @click="openEditModal(record)">编辑</a-button>
                  <a-popconfirm
                    title="确定删除该消息机器人配置吗？"
                    @confirm="handleDelete(record.id)"
                  >
                    <a-button type="link" danger size="small">删除</a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <!-- 右侧：手机端消息效果模拟器 -->
      <a-col :xs="24" :lg="8">
        <a-card :bordered="false" class="main-card preview-card">
          <div class="phone-shell">
            <div class="phone-notch"></div>
            <div class="phone-header">
              <span>9:41</span>
              <span>📶 5G 100%</span>
            </div>
            <div class="phone-title">
              <span class="app-brand">{{ previewPlatformName }} 消息流</span>
            </div>
            <div class="phone-body">
              <div class="chat-bubble">
                <div class="bubble-header">
                  <div class="bot-avatar">🤖</div>
                  <div class="bot-info">
                    <div class="bot-name">{{ form.name || '协同办公提醒助手' }}</div>
                    <div class="bot-time">刚刚</div>
                  </div>
                </div>
                <div class="bubble-card">
                  <div class="card-title">🔔 协同办公平台 · 待办审批提醒</div>
                  <div class="card-divider"></div>
                  <div class="card-item"><span class="k">单据名称：</span><span class="v" style="color: #1677ff">张三提交的员工请假申请单</span></div>
                  <div class="card-item"><span class="k">流程类型：</span><span class="v">请假申报审批流程</span></div>
                  <div class="card-item"><span class="k">申请人员：</span><span class="v">张三 (技术研发中心)</span></div>
                  <div class="card-item"><span class="k">当前节点：</span><span class="v" style="color: #fa8c16; font-weight: bold">部门主管会签审批</span></div>
                  <div class="card-item"><span class="k">到达时间：</span><span class="v">2026-10-28 09:30:00</span></div>
                  <div class="card-footer-tip">请尽快登录协同办公系统或移动端完成办理。</div>
                  <div class="card-action-btn">点击免密进入移动端审批 &gt;</div>
                </div>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 新增/编辑配置弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      width="640px"
      :confirm-loading="saving"
      @ok="handleSave"
    >
      <a-form :model="form" layout="vertical" style="margin-top: 16px">
        <a-form-item label="所属协作平台" required>
          <a-radio-group v-model:value="form.platform" button-style="solid">
            <a-radio-button value="WECHAT_WORK">企业微信</a-radio-button>
            <a-radio-button value="DINGTALK">钉钉</a-radio-button>
            <a-radio-button value="FEISHU">飞书</a-radio-button>
            <a-radio-button value="CUSTOM">自定义</a-radio-button>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="机器人通道名称" required>
          <a-input v-model:value="form.name" placeholder="例如：总经办审批提醒群机器人" />
        </a-form-item>

        <a-form-item label="Webhook URL 地址" required extra="从企业微信群、钉钉群或飞书群右上角‘添加群机器人’后复制 Webhook 地址">
          <a-input v-model:value="form.webhookUrl" placeholder="https://qyapi.weixin.qq.com/... 或 https://oapi.dingtalk.com/..." />
        </a-form-item>

        <a-form-item
          v-if="form.platform === 'DINGTALK' || form.platform === 'FEISHU'"
          label="加签安全密钥 (Secret Key)"
          extra="若在钉钉或飞书机器人安全设置中勾选了‘加签’，请填入 SEC 开头的密钥，系统将自动进行 HMAC-SHA256 签名"
        >
          <a-input v-model:value="form.secretKey" placeholder="选填，加签密钥" />
        </a-form-item>

        <a-form-item label="订阅推送事件">
          <a-checkbox-group v-model:value="selectedEvents">
            <a-checkbox value="TASK_ASSIGNED">待办到达提醒（新审批到达相关经办人）</a-checkbox>
            <a-checkbox value="PROCESS_FINISHED">流程办结通知（审批通过或驳回通知）</a-checkbox>
            <a-checkbox value="NOTICE_PUBLISHED">公文通告广播（全员新通告发布）</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <a-form-item label="状态">
          <a-switch v-model:checked="formEnabled" checked-children="启用" un-checked-children="停用" />
        </a-form-item>

        <a-form-item label="备注说明">
          <a-input v-model:value="form.remark" placeholder="选填备注信息" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined, SendOutlined } from '@ant-design/icons-vue';
import {
  listNotifyConfigsApi,
  saveNotifyConfigApi,
  deleteNotifyConfigApi,
  sendTestNotifyMessageApi,
  type NotifyConfigVO,
  type NotifyConfigDTO
} from '@/api/system';

const loading = ref(false);
const configList = ref<NotifyConfigVO[]>([]);
const testingId = ref<number | null>(null);

const columns = [
  { title: '协作平台', key: 'platform', width: 120 },
  { title: '通道名称', dataIndex: 'name', key: 'name', width: 200 },
  { title: '订阅事件', key: 'notifyEvents', width: 240 },
  { title: '状态', key: 'status', width: 110, align: 'center' as const },
  { title: '操作', key: 'action', width: 220, align: 'center' as const }
];

const getPlatformName = (platform: string) => {
  switch (platform) {
    case 'WECHAT_WORK': return '企业微信';
    case 'DINGTALK': return '钉钉';
    case 'FEISHU': return '飞书';
    default: return '自定义';
  }
};

const getPlatformColor = (platform: string) => {
  switch (platform) {
    case 'WECHAT_WORK': return 'green';
    case 'DINGTALK': return 'blue';
    case 'FEISHU': return 'cyan';
    default: return 'default';
  }
};

const previewPlatformName = computed(() => getPlatformName(form.platform));

const loadData = async () => {
  loading.value = true;
  try {
    const res = await listNotifyConfigsApi();
    configList.value = res.data || [];
  } catch (err: any) {
    message.error(err.message || '加载配置列表失败');
  } finally {
    loading.value = false;
  }
};

// 状态切换开关
const handleToggleStatus = async (record: NotifyConfigVO, checked: boolean) => {
  try {
    await saveNotifyConfigApi({
      id: record.id,
      platform: record.platform,
      name: record.name,
      webhookUrl: record.webhookUrl,
      secretKey: record.secretKey,
      status: checked ? 1 : 0,
      notifyEvents: record.notifyEvents,
      remark: record.remark
    });
    record.status = checked ? 1 : 0;
    message.success(`${record.name} 已${checked ? '激活启用' : '暂停使用'}`);
  } catch (err: any) {
    message.error(err.message || '更新状态失败');
  }
};

// 发送测试消息
const handleSendTest = async (record: NotifyConfigVO) => {
  testingId.value = record.id;
  try {
    const res = await sendTestNotifyMessageApi(record.id);
    message.success(res.data || '测试消息卡片发送成功，请前往群内查看！');
  } catch (err: any) {
    message.error(err.message || '发送测试消息失败');
  } finally {
    testingId.value = null;
  }
};

// 删除配置
const handleDelete = async (id: number) => {
  try {
    await deleteNotifyConfigApi(id);
    message.success('机器人配置已删除');
    loadData();
  } catch (err: any) {
    message.error(err.message || '删除配置失败');
  }
};

// 表单弹窗
const modalVisible = ref(false);
const modalTitle = ref('接入新提醒机器人');
const saving = ref(false);
const selectedEvents = ref<string[]>(['TASK_ASSIGNED', 'PROCESS_FINISHED', 'NOTICE_PUBLISHED']);
const formEnabled = ref(true);

const form = reactive<NotifyConfigDTO>({
  platform: 'WECHAT_WORK',
  name: '',
  webhookUrl: '',
  secretKey: '',
  status: 1,
  notifyEvents: 'TASK_ASSIGNED,PROCESS_FINISHED,NOTICE_PUBLISHED',
  remark: ''
});

const openAddModal = () => {
  modalTitle.value = '接入新提醒机器人';
  form.id = undefined;
  form.platform = 'WECHAT_WORK';
  form.name = '';
  form.webhookUrl = '';
  form.secretKey = '';
  form.status = 1;
  formEnabled.value = true;
  selectedEvents.value = ['TASK_ASSIGNED', 'PROCESS_FINISHED', 'NOTICE_PUBLISHED'];
  form.remark = '';
  modalVisible.value = true;
};

const openEditModal = (record: NotifyConfigVO) => {
  modalTitle.value = '编辑提醒机器人配置';
  form.id = record.id;
  form.platform = record.platform;
  form.name = record.name;
  form.webhookUrl = record.webhookUrl;
  form.secretKey = record.secretKey || '';
  form.status = record.status;
  formEnabled.value = record.status === 1;
  selectedEvents.value = record.notifyEvents ? record.notifyEvents.split(',') : [];
  form.remark = record.remark || '';
  modalVisible.value = true;
};

const handleSave = async () => {
  if (!form.name?.trim()) {
    message.warning('请填写机器人通道名称');
    return;
  }
  if (!form.webhookUrl?.trim()) {
    message.warning('请填写 Webhook URL 地址');
    return;
  }
  form.status = formEnabled.value ? 1 : 0;
  form.notifyEvents = selectedEvents.value.join(',');
  saving.value = true;
  try {
    await saveNotifyConfigApi(form);
    message.success('配置已保存生效！');
    modalVisible.value = false;
    loadData();
  } catch (err: any) {
    message.error(err.message || '保存配置失败');
  } finally {
    saving.value = false;
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.notify-config-container {
  padding: 0;
}

.main-card {
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.preview-card {
  display: flex;
  justify-content: center;
  background: #f7f9fa;
}

.phone-shell {
  width: 310px;
  height: 580px;
  background: #111;
  border-radius: 40px;
  padding: 12px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
}

.phone-notch {
  width: 120px;
  height: 18px;
  background: #000;
  border-radius: 0 0 12px 12px;
  margin: 0 auto 6px auto;
}

.phone-header {
  display: flex;
  justify-content: space-between;
  padding: 0 12px 6px 12px;
  color: #fff;
  font-size: 11px;
}

.phone-title {
  text-align: center;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.phone-body {
  flex: 1;
  background: #ededed;
  border-radius: 24px;
  padding: 12px;
  overflow-y: auto;
}

.chat-bubble {
  margin-top: 8px;
}

.bubble-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.bot-avatar {
  width: 32px;
  height: 32px;
  background: #1677ff;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.bot-info {
  display: flex;
  flex-direction: column;
}

.bot-name {
  font-size: 12px;
  font-weight: 600;
  color: #333;
}

.bot-time {
  font-size: 10px;
  color: #999;
}

.bubble-card {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.card-title {
  font-size: 13px;
  font-weight: 700;
  color: #1f1f1f;
}

.card-divider {
  height: 1px;
  background: #f0f0f0;
  margin: 8px 0;
}

.card-item {
  font-size: 11px;
  line-height: 1.7;
  color: #595959;
}

.card-item .k {
  color: #8c8c8c;
}

.card-footer-tip {
  font-size: 10px;
  color: #bfbfbf;
  margin-top: 8px;
  padding-top: 6px;
  border-top: 1px dashed #f0f0f0;
}

.card-action-btn {
  margin-top: 8px;
  padding: 6px;
  background: #e6f4ff;
  color: #1677ff;
  font-size: 11px;
  font-weight: 600;
  text-align: center;
  border-radius: 4px;
}
</style>

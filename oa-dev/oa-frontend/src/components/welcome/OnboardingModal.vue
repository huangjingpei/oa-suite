<template>
  <a-modal
    v-model:open="visible"
    :footer="null"
    width="760px"
    destroy-on-close
    :body-style="{ padding: '0px', borderRadius: '16px', overflow: 'hidden' }"
    wrap-class-name="onboarding-modal-wrapper"
  >
    <!-- 顶部渐变视觉横幅 -->
    <div class="modal-banner">
      <div class="banner-badge">
        <span>⛵ 启航协同 · 新人入职赋能向导</span>
      </div>
      <h2 class="banner-title">欢迎加入，启航新伙伴！</h2>
      <p class="banner-subtitle">
        千帆竞发，勇立潮头。很高兴与您相遇在启航，让我们携手开启全新的协同篇章！
      </p>
    </div>

    <!-- 步骤条进度指示 -->
    <div class="steps-box">
      <a-steps :current="currentStep" size="small">
        <a-step title="启航相遇" />
        <a-step title="团队支持" />
        <a-step title="新人清单" />
        <a-step title="扬帆启程" />
      </a-steps>
    </div>

    <!-- 主体步骤内容容器 -->
    <div class="step-content-container">
      <!-- 步骤 1：启航相遇 (Welcome & Identity) -->
      <div v-if="currentStep === 0" class="step-pane fade-in">
        <div class="identity-card">
          <div class="avatar-box">
            <a-avatar :size="72" style="background: linear-gradient(135deg, #1677ff 0%, #36cfc9 100%)">
              {{ displayName.charAt(0) }}
            </a-avatar>
            <div class="badge-role">{{ userPost || '启航新员工' }}</div>
          </div>
          <div class="identity-info">
            <h3 class="user-greeting">
              您好，{{ displayName }}！
              <a-tag color="success" style="margin-left: 8px">入职已报到</a-tag>
            </h3>
            <div class="meta-grid">
              <div class="meta-item">
                <span class="label">所属部门：</span>
                <span class="val">{{ userDept || '启航智慧协同科技有限公司' }}</span>
              </div>
              <div class="meta-item">
                <span class="label">员工工号：</span>
                <span class="val">{{ userWorkNo || 'QH-2026' }}</span>
              </div>
              <div class="meta-item">
                <span class="label">系统账号：</span>
                <span class="val">{{ username }}</span>
              </div>
              <div class="meta-item">
                <span class="label">电子邮箱：</span>
                <span class="val">{{ userEmail || 'staff@graddu.com' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="vision-banner">
          <div class="vision-icon">💡</div>
          <div class="vision-text">
            <strong>启航核心价值观与使命</strong>
            <p>
              “以现代数字化协同重塑组织效率，让每一次决策流转都有迹可循，让每一位员工的价值都被看见。”
            </p>
          </div>
        </div>
      </div>

      <!-- 步骤 2：团队支持与办公指南 (Team & Support) -->
      <div v-if="currentStep === 1" class="step-pane fade-in">
        <h4 class="section-title">🏢 您的新人服务与支持后援团队</h4>
        <p class="section-desc">在您入职的第一周，遇到任何系统使用、办公网络或制度疑问，可随时联系以下支持伙伴：</p>

        <a-row :gutter="16">
          <a-col :span="12">
            <div class="support-card">
              <div class="support-icon">🤝</div>
              <div class="support-detail">
                <div class="name">带教导师 (Buddy)</div>
                <div class="duty">负责您的日常业务带教与项目环境融入</div>
                <div class="contact">可查看通讯录直接联系部门主管</div>
              </div>
            </div>
          </a-col>
          <a-col :span="12">
            <div class="support-card">
              <div class="support-icon">💻</div>
              <div class="support-detail">
                <div class="name">IT 技术服务台</div>
                <div class="duty">办公电脑、VPN、网络打印机、开发工具授权</div>
                <div class="contact">内线：8080 | 办公WiFi: Qihang-Staff-5G</div>
              </div>
            </div>
          </a-col>
          <a-col :span="12" style="margin-top: 12px">
            <div class="support-card">
              <div class="support-icon">📑</div>
              <div class="support-detail">
                <div class="name">HR 人事服务台</div>
                <div class="duty">入职档案签署、社保五险一金、劳动合同</div>
                <div class="contact">总经办人事组 (hr@graddu.com)</div>
              </div>
            </div>
          </a-col>
          <a-col :span="12" style="margin-top: 12px">
            <div class="support-card">
              <div class="support-icon">🏛️</div>
              <div class="support-detail">
                <div class="name">行政资产中心</div>
                <div class="duty">工卡配发、办公工位领用、公章与合同用印</div>
                <div class="contact">启航大厦A座 行政服务窗口</div>
              </div>
            </div>
          </a-col>
        </a-row>
      </div>

      <!-- 步骤 3：新人第一周 Checklist (Onboarding Checklist) -->
      <div v-if="currentStep === 2" class="step-pane fade-in">
        <div class="checklist-header">
          <div>
            <h4 class="section-title" style="margin: 0">📋 新员工入职第 1 周新手任务清单</h4>
            <span class="section-desc" style="margin: 0">完成以下关键任务，帮助您快速融入团队节奏：</span>
          </div>
          <div class="checklist-progress">
            <a-progress type="circle" :percent="completedPercent" :width="48" />
          </div>
        </div>

        <div class="checklist-items">
          <div
            v-for="(item, idx) in checklist"
            :key="idx"
            class="checklist-item"
            :class="{ checked: item.done }"
            @click="toggleItem(idx)"
          >
            <a-checkbox :checked="item.done" @click.stop="toggleItem(idx)" />
            <div class="item-text">
              <span class="title">{{ item.title }}</span>
              <span class="desc">{{ item.desc }}</span>
            </div>
            <a-tag :color="item.done ? 'success' : 'processing'">
              {{ item.done ? '已完成' : '待办理' }}
            </a-tag>
          </div>
        </div>
      </div>

      <!-- 步骤 4：扬帆起程 (Launch) -->
      <div v-if="currentStep === 3" class="step-pane fade-in">
        <div class="ready-hero">
          <div class="celebrate-emoji">🚀</div>
          <h3 class="ready-title">准备就绪，尽情探索启航协同工作台！</h3>
          <p class="ready-subtitle">系统为您预备了极简高效的办事通道，助您轻装上阵、高效协作：</p>
        </div>

        <div class="quick-nav-grid">
          <div class="nav-tile">
            <span class="tile-icon">⏰</span>
            <div class="tile-title">每日考勤打卡</div>
            <div class="tile-desc">支持 PC 浏览器与手机 H5 快速上下班打卡及外勤报备</div>
          </div>
          <div class="nav-tile">
            <span class="tile-icon">💼</span>
            <div class="tile-title">请假与休假申报</div>
            <div class="tile-desc">年假、事假、调休、病假在线流转，自动校验剩余工时</div>
          </div>
          <div class="nav-tile">
            <span class="tile-icon">🖨️</span>
            <div class="tile-title">行政用印与高保真套打</div>
            <div class="tile-desc">公章、合同章严密审批，并生成带防伪水印的纸质套打单</div>
          </div>
          <div class="nav-tile">
            <span class="tile-icon">📢</span>
            <div class="tile-title">公文公告通报</div>
            <div class="tile-desc">及时查阅红头公文决议，已读状态精准统计留痕</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部操作导航条 -->
    <div class="modal-footer">
      <a-checkbox v-if="currentStep === 3" v-model:checked="neverShowAgain">
        不再自动弹出新人向导
      </a-checkbox>
      <div v-else class="step-hint">
        步骤 {{ currentStep + 1 }} / 4
      </div>

      <a-space>
        <a-button v-if="currentStep > 0" @click="prevStep">
          上一步
        </a-button>
        <a-button v-if="currentStep < 3" type="primary" @click="nextStep">
          下一步
        </a-button>
        <a-button
          v-else
          type="primary"
          style="background: #52c41a; border-color: #52c41a; font-weight: 600"
          @click="finishOnboarding"
        >
          🎉 开启启航之旅 (进入工作台)
        </a-button>
      </a-space>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { message } from 'ant-design-vue';
import { useUserStore } from '@/store/user';

const props = defineProps<{
  open: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:open', val: boolean): void;
}>();

const userStore = useUserStore();

const visible = computed({
  get: () => props.open,
  set: (val: boolean) => emit('update:open', val)
});

const currentStep = ref(0);
const neverShowAgain = ref(true);

const displayName = computed(() => userStore.displayName || '启航新伙伴');
const username = computed(() => userStore.userInfo?.username || 'user');
const userDept = computed(() => userStore.userInfo?.deptName || '');
const userPost = computed(() => userStore.userInfo?.postName || '');
const userWorkNo = computed(() => userStore.userInfo?.workNo || '');
const userEmail = computed(() => userStore.userInfo?.email || '');

// 新人第一周任务清单
const checklist = ref([
  { title: '完成密码修改与个人资料核验', desc: '进入右上角个人设置，核验手机号码与邮箱信息', done: true },
  { title: '熟悉工作台与今日待办任务', desc: '了解现代待办中枢与传统全景服务大厅的切换使用', done: true },
  { title: '查阅《启航员工日常行为与考勤排班规范》', desc: '掌握考勤打卡时间（09:00 - 18:00）及请假外出审批要求', done: false },
  { title: '提交一寸入职电子照片与体检报告', desc: '向人事行政中心报备制作启航专属工卡与门禁权限', done: false },
  { title: '与直属领导开展入职第 1 周一对一面谈', desc: '明确本季度重点业务目标、带教导师与协作团队伙伴', done: false }
]);

const completedPercent = computed(() => {
  const total = checklist.value.length;
  const doneCount = checklist.value.filter((i) => i.done).length;
  return Math.round((doneCount / total) * 100);
});

const toggleItem = (idx: number) => {
  checklist.value[idx].done = !checklist.value[idx].done;
};

const nextStep = () => {
  if (currentStep.value < 3) {
    currentStep.value += 1;
  }
};

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value -= 1;
  }
};

const finishOnboarding = () => {
  if (neverShowAgain.value && username.value) {
    localStorage.setItem(`qihang_onboarding_shown_${username.value}`, '1');
  }
  visible.value = false;
  message.success('祝您在启航协同平台工作顺利，前程似锦、扬帆启航！');
};
</script>

<style scoped>
.modal-banner {
  background: linear-gradient(135deg, #1677ff 0%, #2f54eb 50%, #13c2c2 100%);
  color: #ffffff;
  padding: 24px 32px;
  position: relative;
}

.banner-badge {
  display: inline-block;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  margin-bottom: 8px;
  font-weight: 500;
}

.banner-title {
  color: #ffffff;
  font-size: 24px;
  font-weight: 700;
  margin: 4px 0 8px;
}

.banner-subtitle {
  color: rgba(255, 255, 255, 0.88);
  font-size: 14px;
  margin: 0;
  line-height: 1.5;
}

.steps-box {
  padding: 16px 32px 12px;
  background: #fdfdfd;
  border-bottom: 1px solid #f0f0f0;
}

.step-content-container {
  padding: 24px 32px;
  min-height: 290px;
}

.step-pane {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 步骤 1 样式 */
.identity-card {
  display: flex;
  align-items: center;
  gap: 24px;
  background: #f6f8fc;
  padding: 20px 24px;
  border-radius: 12px;
  border: 1px solid #e6ebf5;
}

.avatar-box {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.badge-role {
  margin-top: 8px;
  background: #e6f4ff;
  color: #1677ff;
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
}

.user-greeting {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #1f1f1f;
}

.meta-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 24px;
}

.meta-item {
  font-size: 13px;
}

.meta-item .label {
  color: #8c8c8c;
}

.meta-item .val {
  color: #262626;
  font-weight: 500;
}

.vision-banner {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin-top: 20px;
  background: #fffbe6;
  border: 1px solid #ffe58f;
  padding: 12px 16px;
  border-radius: 8px;
}

.vision-icon {
  font-size: 20px;
}

.vision-text strong {
  color: #d46b08;
  font-size: 13px;
}

.vision-text p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #873800;
  line-height: 1.5;
}

/* 步骤 2 样式 */
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f1f1f;
  margin-bottom: 4px;
}

.section-desc {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 16px;
}

.support-card {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  padding: 12px 16px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.support-card:hover {
  background: #f0f7ff;
  border-color: #adc6ff;
}

.support-icon {
  font-size: 22px;
}

.support-detail .name {
  font-weight: 600;
  font-size: 14px;
  color: #262626;
}

.support-detail .duty {
  font-size: 12px;
  color: #595959;
  margin-top: 2px;
}

.support-detail .contact {
  font-size: 11px;
  color: #1677ff;
  margin-top: 4px;
  font-weight: 500;
}

/* 步骤 3 样式 */
.checklist-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.checklist-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.checklist-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.checklist-item:hover {
  border-color: #1677ff;
  background: #f6faff;
}

.checklist-item.checked {
  background: #f6ffed;
  border-color: #b7eb8f;
}

.checklist-item.checked .title {
  text-decoration: line-through;
  color: #8c8c8c;
}

.item-text {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-text .title {
  font-size: 13px;
  font-weight: 500;
  color: #262626;
}

.item-text .desc {
  font-size: 11px;
  color: #8c8c8c;
  margin-top: 2px;
}

/* 步骤 4 样式 */
.ready-hero {
  text-align: center;
  margin-bottom: 20px;
}

.celebrate-emoji {
  font-size: 36px;
  margin-bottom: 6px;
}

.ready-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f1f1f;
  margin-bottom: 6px;
}

.ready-subtitle {
  font-size: 13px;
  color: #595959;
  margin: 0;
}

.quick-nav-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.nav-tile {
  background: #f8faff;
  border: 1px solid #e6f0ff;
  padding: 14px 16px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.tile-icon {
  font-size: 20px;
}

.tile-title {
  font-size: 13px;
  font-weight: 600;
  color: #1677ff;
}

.tile-desc {
  font-size: 12px;
  color: #595959;
  line-height: 1.4;
}

/* 底部操作条 */
.modal-footer {
  padding: 14px 32px 18px;
  background: #fafafa;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.step-hint {
  font-size: 13px;
  color: #8c8c8c;
}
</style>

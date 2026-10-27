<template>
  <div class="redhead-document">
    <!-- 1. 红头顶部企业名称 -->
    <div class="redhead-banner">
      盛泰智慧协同集团有限公司文件
    </div>

    <!-- 2. 文号与签发信息条 -->
    <div class="redhead-meta-row">
      <div class="doc-code">{{ doc.docNo || '盛泰发〔2026〕18号' }}</div>
      <div class="security-level">
        <span v-if="doc.securityLevel === 'CONFIDENTIAL'">★ 机 密</span>
        <span v-else-if="doc.securityLevel === 'SECRET'">★ 秘 密</span>
        <span v-else>普 件</span>
      </div>
      <div class="signer-info">签发人：<u>{{ doc.signerName || '王董事长' }}</u></div>
    </div>

    <!-- 3. 标准红双线 (上粗下细) -->
    <div class="red-double-line"></div>

    <!-- 4. 公文大标题 -->
    <h1 class="official-title">
      {{ doc.title || '关于印发 2026 年度科技创新与协同办公数字化转型实施纲要的通知' }}
    </h1>

    <!-- 5. 主送单位 -->
    <div class="target-zone">
      {{ doc.targetZone || '集团各中心、事业部、各直属分支机构、各项目部' }}：
    </div>

    <!-- 6. 公文正文 (标准仿宋排版，首行空两格) -->
    <div class="official-content">
      <div v-if="doc.content" class="content-text" v-html="formatContent(doc.content)"></div>
      <div v-else class="content-text">
        <p>为深入贯彻落实数字化转型战略布局，全面提升集团协同研发、规范化行政流转与智能敏捷办公水平，经集团董事会与总裁办公会联合研究决定，特制定《2026年度科技创新与协同办公数字化转型实施纲要》。</p>
        <p>现将有关工作要求通知如下：</p>
        <p>一、提高政治站位，筑牢协同底座。各业务中心应主动拥抱全新 Spring Boot 3 与 Vue 3 现代化协同平台，彻底消除遗留数据烟囱与信息孤岛，实现业务单据全生命周期可追溯、防篡改、高保真纸质留痕。</p>
        <p>二、压实主体责任，强化流程赋能。各部门第一责任人应严格审批权限，杜绝人情审批与越权审批，推动人事假勤、用印鉴真、公文签发等关键事务全面驶入合规高效的智能化协同快车道。</p>
        <p>请各部门接此通知后，组织全体员工认真学习领会，并结合部门岗位实际制定落地方案，确保各项数字化攻坚举措落到实处、见到实效。</p>
      </div>
    </div>

    <!-- 7. 附件清单 (若有) -->
    <div v-if="doc.files" class="attachment-section">
      <div class="attachment-label">附件：</div>
      <div class="attachment-list">
        <div>1. 2026年度数字化转型关键绩效考核指标分解表.pdf</div>
      </div>
    </div>

    <!-- 8. 发文机关署名与成文日期 (落款区与盖章) -->
    <div class="signature-section">
      <div class="signature-wrapper">
        <div class="issuer-name">{{ doc.issuerDept || '盛泰智慧协同集团有限公司' }}</div>
        <div class="publish-date">{{ formatChineseDate(doc.publishDate || doc.createTime) }}</div>

        <!-- 正规加盖企业公文红印章 (覆盖部分文字与日期) -->
        <div class="redhead-stamp">
          <ElectronicStamp
            company-name="盛泰智慧协同集团有限公司"
            stamp-text="发文专用章"
            :date="doc.publishDate ? doc.publishDate.substring(0, 10) : '2026-10-28'"
            :size="150"
          />
        </div>
      </div>
    </div>

    <!-- 9. 底部版记 (红单线分割、抄送、印发信息) -->
    <div class="edition-record">
      <div class="red-single-line"></div>
      <div class="record-row">
        <span class="record-label">抄送：</span>
        <span class="record-content">集团董事会成员、监事会，各外部战略合伙人单位。</span>
      </div>
      <div class="record-divider"></div>
      <div class="record-row record-footer">
        <span>盛泰智慧协同集团总裁办公室</span>
        <span>{{ doc.publishDate || '2026-10-28' }} 印发</span>
        <span>共印 35 份</span>
      </div>
      <div class="red-single-line" style="margin-top: 4px;"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { RedheadDocVO } from '@/types/business';
import ElectronicStamp from '@/components/print/ElectronicStamp.vue';

defineProps<{
  doc: Partial<RedheadDocVO>;
}>();

const formatContent = (content: string) => {
  if (!content) return '';
  return content.replace(/\n/g, '<br/>');
};

const formatChineseDate = (dateStr?: string) => {
  if (!dateStr) return '二〇二六年十月二十八日';
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return '二〇二六年十月二十八日';
  const digits = ['〇', '一', '二', '三', '四', '五', '六', '七', '八', '九'];
  const yearStr = d.getFullYear().toString().split('').map(char => digits[parseInt(char)]).join('');
  const month = d.getMonth() + 1;
  const monthStr = month <= 10 ? (month === 10 ? '十' : digits[month]) : `十${digits[month % 10]}`;
  const day = d.getDate();
  let dayStr = '';
  if (day <= 10) {
    dayStr = day === 10 ? '十' : digits[day];
  } else if (day < 20) {
    dayStr = `十${digits[day % 10]}`;
  } else if (day === 20) {
    dayStr = '二十';
  } else if (day < 30) {
    dayStr = `二十${digits[day % 10]}`;
  } else {
    dayStr = day === 30 ? '三十' : `三十${digits[day % 10]}`;
  }
  return `${yearStr}年${monthStr}月${dayStr}日`;
};
</script>

<style scoped>
.redhead-document {
  font-family: 'FangSong', 'STFangsong', '仿宋', serif;
  color: #000;
  line-height: 1.8;
  width: 100%;
}

/* 顶部红头大字 */
.redhead-banner {
  color: #e53935;
  font-family: 'SimSun', 'Songti SC', serif;
  font-size: 32px;
  font-weight: 900;
  text-align: center;
  letter-spacing: 5px;
  margin-top: 10px;
  margin-bottom: 24px;
}

/* 文号与密级栏 */
.redhead-meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-family: 'SimHei', 'Heiti SC', sans-serif;
  font-size: 15px;
  padding: 0 4px;
  margin-bottom: 8px;
}

.doc-code {
  font-weight: bold;
}

.security-level {
  color: #333;
}

.signer-info u {
  text-decoration: underline;
  padding: 0 4px;
}

/* 红双线 (上粗 3px，下细 1px) */
.red-double-line {
  border-top: 3px solid #e53935;
  border-bottom: 1px solid #e53935;
  height: 3px;
  margin-bottom: 28px;
}

/* 二号小标宋公文大标题 */
.official-title {
  text-align: center;
  font-family: 'SimSun', 'Songti SC', serif;
  font-size: 24px;
  font-weight: 900;
  line-height: 1.4;
  margin: 0 20px 28px 20px;
  letter-spacing: 1px;
}

/* 主送单位 */
.target-zone {
  font-size: 17px;
  font-weight: bold;
  margin-bottom: 12px;
}

/* 正文 */
.official-content {
  font-size: 16px;
  text-align: justify;
}

.content-text p {
  text-indent: 2.2em;
  margin: 0 0 10px 0;
  line-height: 1.8;
}

/* 附件说明 */
.attachment-section {
  display: flex;
  margin-top: 20px;
  font-size: 15px;
}

.attachment-label {
  width: 50px;
}

.attachment-list {
  flex: 1;
}

/* 发文落款与盖章 */
.signature-section {
  margin-top: 36px;
  display: flex;
  justify-content: flex-end;
  padding-right: 40px;
}

.signature-wrapper {
  position: relative;
  text-align: center;
  width: 260px;
}

.issuer-name {
  font-size: 17px;
  font-weight: bold;
  letter-spacing: 1px;
  margin-bottom: 6px;
}

.publish-date {
  font-size: 16px;
  letter-spacing: 1px;
}

.redhead-stamp {
  position: absolute;
  top: -35px;
  right: 25px;
  z-index: 10;
}

/* 底部版记 */
.edition-record {
  margin-top: 50px;
  font-size: 14px;
}

.red-single-line {
  border-top: 1.5px solid #e53935;
  margin: 8px 0;
}

.record-row {
  display: flex;
  padding: 4px 0;
}

.record-label {
  font-weight: bold;
  width: 50px;
}

.record-content {
  flex: 1;
}

.record-divider {
  border-top: 1px solid #ccc;
  margin: 4px 0;
}

.record-footer {
  display: flex;
  justify-content: space-between;
  color: #333;
}
</style>

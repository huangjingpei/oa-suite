<template>
  <div class="print-document">
    <div class="doc-header">
      <h1 class="company-name">云枢智慧协同科技有限公司</h1>
      <h2 class="doc-title">企 业 印 章 使 用 审 批 签 报 表</h2>
    </div>

    <div class="doc-meta">
      <span>用印流水号：<strong>{{ seal.applyNo || 'YZ202610280003' }}</strong></span>
      <span>申请日期：{{ seal.createTime ? seal.createTime.substring(0, 10) : '2026-10-28' }}</span>
    </div>

    <table class="grid-table">
      <tbody>
        <tr>
          <td class="col-label">申请人姓名</td>
          <td class="col-val">{{ seal.applicantName || '王商务' }}</td>
          <td class="col-label">申请部门</td>
          <td class="col-val">{{ seal.deptName || '市场营销部' }}</td>
          <td class="col-label">盖章经办人</td>
          <td class="col-val">{{ seal.agentName || seal.applicantName || '王商务' }}</td>
        </tr>

        <tr>
          <td class="col-label">印章类型</td>
          <td colspan="5" class="col-val">
            <span class="check-item"><span class="box">{{ seal.sealType === 'COMMON' ? '☑' : '☐' }}</span> 企业公章</span>
            <span class="check-item"><span class="box">{{ seal.sealType === 'CONTRACT' ? '☑' : '☐' }}</span> 合同专用章</span>
            <span class="check-item"><span class="box">{{ seal.sealType === 'LEGAL' ? '☑' : '☐' }}</span> 法人代表印章</span>
            <span class="check-item"><span class="box">{{ seal.sealType === 'FINANCE' ? '☑' : '☐' }}</span> 财务专用章</span>
            <span class="check-item"><span class="box">{{ seal.sealType === 'DECLARE' ? '☑' : '☐' }}</span> 资质申报章</span>
          </td>
        </tr>

        <tr>
          <td class="col-label">用印文件名称</td>
          <td colspan="3" class="col-val"><strong>{{ seal.fileName || '智能制造协同系统采购框架协议' }}</strong></td>
          <td class="col-label">用印份数</td>
          <td class="col-val">{{ seal.sealCopies || 2 }} 份</td>
        </tr>

        <tr v-if="seal.sealType === 'CONTRACT' || seal.contractAmount">
          <td class="col-label">合同金额</td>
          <td colspan="2" class="col-val">
            {{ seal.contractAmount ? `¥ ${Number(seal.contractAmount).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}` : '不涉及金额' }}
          </td>
          <td class="col-label">相对方单位</td>
          <td colspan="2" class="col-val">{{ seal.counterparty || '国投高新实业发展有限公司' }}</td>
        </tr>

        <tr>
          <td class="col-label">用途与事由</td>
          <td colspan="5" class="col-val reason-cell">
            <div class="reason-content">{{ seal.purpose || '与战略客户签订年度采购协议及保密附件，经法务审阅无重大合规风险，特申请加盖公章与合同章。' }}</div>
            <div v-if="seal.files" class="attachment-hint">【已上传正式盖章文本/合同终版扫描件】</div>
          </td>
        </tr>

        <!-- 申请部门负责人意见 -->
        <tr>
          <td class="col-label">申请部门<br/>负责人意见</td>
          <td colspan="5" class="col-val opinion-cell">
            <div class="opinion-body">
              {{ (seal.procStatus ?? 0) >= 2 ? '内容审核无误，确属正常业务需要，同意用印。' : '审核中...' }}
            </div>
            <div class="sign-block">
              <span>签字：<u>{{ (seal.procStatus ?? 0) >= 2 ? '钱总监' : '待签' }}</u></span>
              <span class="sign-date">日期：{{ seal.createTime ? seal.createTime.substring(0, 10) : '2026-10-28' }}</span>
            </div>
          </td>
        </tr>

        <!-- 法务合规部意见 -->
        <tr>
          <td class="col-label">法务合规部<br/>审核意见</td>
          <td colspan="5" class="col-val opinion-cell">
            <div class="opinion-body">
              {{ (seal.procStatus ?? 0) >= 2 ? '合同文本经法律合规审查，无权责倒挂或重大商业风险，准予盖印。' : '审核中...' }}
            </div>
            <div class="sign-block">
              <span>法务审核：<u>{{ (seal.procStatus ?? 0) >= 2 ? '周律师' : '待签' }}</u></span>
              <span class="sign-date">日期：{{ seal.createTime ? seal.createTime.substring(0, 10) : '2026-10-28' }}</span>
            </div>
          </td>
        </tr>

        <!-- 总经理批准与印章管理员盖戳留样 -->
        <tr>
          <td class="col-label">印章保管员<br/>核验盖章区</td>
          <td colspan="5" class="col-val opinion-cell stamp-container">
            <div class="opinion-body">
              {{ seal.procStatus === 2 ? '审批单据核对一致，骑缝章与落款章已规范加盖，原件一份已留底备查。' : '待领导审批后用印' }}
            </div>
            <div class="sign-block">
              <span>印章专管员：<u>{{ seal.procStatus === 2 ? '吴管理员' : '待办' }}</u></span>
              <span class="sign-date">日期：{{ seal.createTime ? seal.createTime.substring(0, 10) : '2026-10-28' }}</span>
            </div>
            <div v-if="seal.procStatus === 2" class="stamp-position">
              <ElectronicStamp
                company-name="云枢智慧协同科技有限公司"
                :stamp-text="seal.sealType === 'CONTRACT' ? '合同专用章' : '企业公章'"
                :date="seal.createTime ? seal.createTime.substring(0, 10) : '2026-10-28'"
                :size="140"
              />
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="doc-footer">
      <div class="notes">
        严正声明：印章加盖必须坚持“见字盖章、依批盖章”；严禁加盖空白纸张与未审文件；违者追究行政与法律责任。
      </div>
      <div class="verification-code">
        防伪鉴真号：{{ (seal.id || '66665555').toString(16).toUpperCase() }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { SealApplyVO } from '@/types/business';
import ElectronicStamp from '@/components/print/ElectronicStamp.vue';

defineProps<{
  seal: Partial<SealApplyVO>;
}>();
</script>

<style scoped>
.print-document {
  font-family: SimSun, 'Songti SC', 'STSong', serif;
  color: #000;
  line-height: 1.5;
  width: 100%;
}

.doc-header {
  text-align: center;
  margin-bottom: 12px;
}

.company-name {
  font-family: SimHei, 'Heiti SC', sans-serif;
  font-size: 20px;
  font-weight: bold;
  letter-spacing: 2px;
  margin: 0 0 6px 0;
}

.doc-title {
  font-family: SimHei, 'Heiti SC', sans-serif;
  font-size: 24px;
  font-weight: 900;
  letter-spacing: 4px;
  margin: 0;
  border-bottom: 2px solid #000;
  display: inline-block;
  padding-bottom: 4px;
}

.doc-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin: 12px 0 6px 0;
  font-family: 'FangSong', 'STFangsong', serif;
}

.grid-table {
  width: 100%;
  border-collapse: collapse;
  border: 2px solid #000;
  font-size: 14px;
}

.grid-table td {
  border: 1px solid #000;
  padding: 8px 10px;
}

.col-label {
  background-color: #fafafa;
  font-family: SimHei, 'Heiti SC', sans-serif;
  font-weight: bold;
  text-align: center;
  width: 95px;
  letter-spacing: 1px;
}

.col-val {
  font-family: 'FangSong', 'STFangsong', serif;
}

.check-item {
  margin-right: 14px;
  display: inline-block;
}

.box {
  font-size: 16px;
  margin-right: 2px;
}

.reason-cell {
  min-height: 80px;
  vertical-align: top;
  padding: 12px 10px;
}

.reason-content {
  min-height: 50px;
  white-space: pre-wrap;
  text-indent: 2em;
}

.attachment-hint {
  font-size: 12px;
  color: #555;
  margin-top: 6px;
}

.opinion-cell {
  height: 85px;
  position: relative;
  vertical-align: top;
  padding: 8px 10px;
}

.opinion-body {
  min-height: 40px;
  text-indent: 2em;
}

.sign-block {
  display: flex;
  justify-content: flex-end;
  gap: 24px;
  font-size: 13px;
  margin-top: 8px;
}

.stamp-container {
  position: relative;
}

.stamp-position {
  position: absolute;
  right: 60px;
  bottom: -15px;
  z-index: 10;
}

.doc-footer {
  margin-top: 14px;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #444;
  font-family: 'FangSong', 'STFangsong', serif;
}

.notes {
  flex: 1;
}

.verification-code {
  font-family: Consolas, monospace;
}
</style>

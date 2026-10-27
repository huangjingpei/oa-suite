<template>
  <div class="print-document">
    <div class="doc-header">
      <h1 class="company-name">云枢智慧协同科技有限公司</h1>
      <h2 class="doc-title">员 工 外 出 公 干 审 批 凭 证</h2>
    </div>

    <div class="doc-meta">
      <span>单据流水号：<strong>{{ egress.egressNo || 'WC202610280001' }}</strong></span>
      <span>报备日期：{{ egress.createTime ? egress.createTime.substring(0, 10) : '2026-10-28' }}</span>
    </div>

    <table class="grid-table">
      <tbody>
        <tr>
          <td class="col-label">外勤人员姓名</td>
          <td class="col-val">{{ egress.applicantName || '张三' }}</td>
          <td class="col-label">所属部门</td>
          <td class="col-val">{{ egress.deptName || '技术研发中心' }}</td>
          <td class="col-label">登录账号</td>
          <td class="col-val">{{ egress.applicantUsername || 'zhangsan' }}</td>
        </tr>

        <tr>
          <td class="col-label">外勤起止时段</td>
          <td colspan="3" class="col-val">
            自 <strong>{{ egress.startTime || '2026-10-28 14:00' }}</strong> 起 至
            <strong>{{ egress.endTime || '2026-10-28 17:30' }}</strong> 止
          </td>
          <td class="col-label">折算外勤工时</td>
          <td class="col-val highlight-val">
            <strong>{{ egress.totalHours || 3.5 }}</strong> 小时
          </td>
        </tr>

        <tr>
          <td class="col-label">外出目的地/单位</td>
          <td colspan="3" class="col-val">{{ egress.location || '市政务服务中心数字展厅' }}</td>
          <td class="col-label">同行人员</td>
          <td class="col-val">{{ egress.companions || '无（独立前往）' }}</td>
        </tr>

        <tr>
          <td class="col-label">外出工作具体事务</td>
          <td colspan="5" class="col-val reason-cell">
            <div class="reason-content">{{ egress.reason || '前往客户现场进行数字化政务协同系统需求调研与原型系统操作演示培训。' }}</div>
          </td>
        </tr>

        <tr>
          <td class="col-label">部门主管审核意见</td>
          <td colspan="5" class="col-val audit-cell">
            <div class="audit-opinion">
              <span class="decision-tag">同意公干外出</span>
              <p>业务公干属实，注意外出交通安全与项目材料保密。</p>
            </div>
            <div class="audit-signature">
              <span>主管签字：<span class="handwrite">王建国</span></span>
              <span>审核日期：{{ egress.createTime ? egress.createTime.substring(0, 10) : '2026-10-28' }}</span>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="doc-footer">
      <span>填单制表：OA 协同云中枢</span>
      <span>外勤审批状态：{{ getStatusText(egress.procStatus) }}</span>
      <span>打印时间：{{ currentDateTime }}</span>
    </div>

    <div class="action-bar no-print">
      <a-button type="primary" size="large" @click="handlePrint">
        <PrinterOutlined /> 立即打印外出凭证
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { PrinterOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import type { EgressVO } from '@/types/business';

defineProps<{
  egress: Partial<EgressVO>;
}>();

const currentDateTime = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'));

const getStatusText = (status?: number) => {
  if (status === 0) return '草稿待发';
  if (status === 1) return '流转审批中';
  if (status === 2) return '已终审通过';
  if (status === 3) return '已被驳回';
  return '审批办结归档';
};

const handlePrint = () => {
  window.print();
};
</script>

<style scoped>
.print-document {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px;
  background: #ffffff;
  color: #000000;
  font-family: 'SimSun', 'Songti SC', 'STSong', serif;
}

.doc-header {
  text-align: center;
  margin-bottom: 24px;
}

.company-name {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 8px 0;
  letter-spacing: 2px;
}

.doc-title {
  font-size: 24px;
  font-weight: 800;
  margin: 0;
  letter-spacing: 4px;
  text-decoration: underline;
  text-underline-offset: 6px;
}

.doc-meta {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  margin-bottom: 12px;
  padding: 0 2px;
}

.grid-table {
  width: 100%;
  border-collapse: collapse;
  border: 2px solid #000;
}

.grid-table td {
  border: 1px solid #000;
  padding: 10px 12px;
  font-size: 14px;
}

.col-label {
  background-color: #f7f7f7;
  font-weight: 700;
  width: 110px;
  text-align: center;
}

.col-val {
  min-width: 120px;
}

.highlight-val {
  color: #1677ff;
  font-size: 15px;
}

.reason-cell {
  height: 90px;
  vertical-align: top;
}

.reason-content {
  line-height: 1.6;
  white-space: pre-wrap;
}

.audit-cell {
  height: 100px;
  vertical-align: top;
  position: relative;
}

.decision-tag {
  display: inline-block;
  border: 1px solid #000;
  padding: 2px 8px;
  font-weight: 700;
  font-size: 13px;
  margin-bottom: 6px;
}

.audit-signature {
  position: absolute;
  right: 16px;
  bottom: 8px;
  display: flex;
  gap: 24px;
  font-size: 13px;
}

.handwrite {
  font-family: 'KaiTi', 'STKaiti', cursive;
  font-size: 16px;
  font-weight: 700;
  padding-left: 4px;
}

.doc-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
  margin-top: 14px;
  padding: 0 4px;
}

.action-bar {
  text-align: center;
  margin-top: 24px;
}

@media print {
  .no-print {
    display: none !important;
  }
  .print-document {
    padding: 0;
    max-width: 100%;
  }
}
</style>

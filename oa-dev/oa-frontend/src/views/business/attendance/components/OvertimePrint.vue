<template>
  <div class="print-document">
    <div class="doc-header">
      <h1 class="company-name">盛泰智慧协同科技有限公司</h1>
      <h2 class="doc-title">员 工 加 班 审 批 备 案 单</h2>
    </div>

    <div class="doc-meta">
      <span>单据流水号：<strong>{{ overtime.overtimeNo || 'JB202610280001' }}</strong></span>
      <span>申报日期：{{ overtime.createTime ? overtime.createTime.substring(0, 10) : '2026-10-28' }}</span>
    </div>

    <table class="grid-table">
      <tbody>
        <tr>
          <td class="col-label">申请人姓名</td>
          <td class="col-val">{{ overtime.applicantName || '张三' }}</td>
          <td class="col-label">所属部门</td>
          <td class="col-val">{{ overtime.deptName || '研发中心' }}</td>
          <td class="col-label">登录工号</td>
          <td class="col-val">{{ overtime.applicantUsername || 'zhangsan' }}</td>
        </tr>

        <tr>
          <td class="col-label">加班起止时段</td>
          <td colspan="3" class="col-val">
            自 <strong>{{ overtime.startTime || '2026-10-28 18:30' }}</strong> 起 至
            <strong>{{ overtime.endTime || '2026-10-28 22:30' }}</strong> 止
          </td>
          <td class="col-label">核算总工时</td>
          <td class="col-val highlight-val">
            <strong>{{ overtime.totalHours || 4 }}</strong> 小时
          </td>
        </tr>

        <tr>
          <td class="col-label">加班工作内容及交付说明</td>
          <td colspan="5" class="col-val reason-cell">
            <div class="reason-content">{{ overtime.reason || '因项目紧急版本升级上线，完成线上服务集群部署及回归验收测试。' }}</div>
          </td>
        </tr>

        <tr>
          <td class="col-label">部门主管审核意见</td>
          <td colspan="5" class="col-val audit-cell">
            <div class="audit-opinion">
              <span class="decision-tag">同意加班申报</span>
              <p>工作任务紧急，安排合理，同意按标准核算加班调休工时。</p>
            </div>
            <div class="audit-signature">
              <span>主管签字：<span class="handwrite">李明</span></span>
              <span>签署日期：{{ overtime.createTime ? overtime.createTime.substring(0, 10) : '2026-10-28' }}</span>
            </div>
          </td>
        </tr>

        <tr>
          <td class="col-label">人事行政复核与工时备案</td>
          <td colspan="5" class="col-val audit-cell">
            <div class="audit-opinion">
              <p>已核验出入打卡流水记录，工时计入员工调休池。</p>
            </div>
            <div class="audit-signature">
              <span>人事专员：<span class="handwrite">王晓华</span></span>
              <span>复核确认日期：{{ overtime.createTime ? overtime.createTime.substring(0, 10) : '2026-10-28' }}</span>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="doc-footer">
      <span>填单制表：OA 协同云中枢</span>
      <span>流程状态：{{ getStatusText(overtime.procStatus) }}</span>
      <span>打印时间：{{ currentDateTime }}</span>
    </div>

    <div class="action-bar no-print">
      <a-button type="primary" size="large" @click="handlePrint">
        <PrinterOutlined /> 立即打印加班单
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { PrinterOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import type { OvertimeVO } from '@/types/business';

defineProps<{
  overtime: Partial<OvertimeVO>;
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

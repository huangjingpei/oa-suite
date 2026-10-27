<template>
  <div class="print-document">
    <div class="doc-header">
      <h1 class="company-name">启航智慧协同科技有限公司</h1>
      <h2 class="doc-title">档 案 资 料 借 阅 审 批 单</h2>
    </div>

    <div class="doc-meta">
      <span>借阅单号：<strong>{{ borrow.borrowNo || 'DA202610280005' }}</strong></span>
      <span>申请日期：{{ borrow.createTime ? borrow.createTime.substring(0, 10) : '2026-10-28' }}</span>
    </div>

    <table class="grid-table">
      <tbody>
        <tr>
          <td class="col-label">借阅人姓名</td>
          <td class="col-val">{{ borrow.applicantName || '赵风控' }}</td>
          <td class="col-label">所属部门</td>
          <td class="col-val">{{ borrow.deptName || '审计合规部' }}</td>
          <td class="col-label">账号</td>
          <td class="col-val">{{ borrow.applicantUsername || 'zhaofengkong' }}</td>
        </tr>

        <tr>
          <td class="col-label">档案卷册名称</td>
          <td colspan="3" class="col-val"><strong>{{ borrow.recordTitle || '2025年度企业所得税汇算清缴审计底稿' }}</strong></td>
          <td class="col-label">借阅数量</td>
          <td class="col-val">{{ borrow.copies || 1 }} 卷/份</td>
        </tr>

        <tr>
          <td class="col-label">借阅方式</td>
          <td colspan="5" class="col-val">
            <span class="check-item"><span class="box">{{ borrow.borrowType === 'READ_ONLY' ? '☑' : '☐' }}</span> 现场查阅（严禁带出档案阅览室）</span>
            <span class="check-item"><span class="box">{{ borrow.borrowType === 'BORROW_PHYSICAL' ? '☑' : '☐' }}</span> 实体借出（须限期归还）</span>
            <span class="check-item"><span class="box">{{ borrow.borrowType === 'COPY' ? '☑' : '☐' }}</span> 复印/摘录</span>
          </td>
        </tr>

        <tr>
          <td class="col-label">借阅期限</td>
          <td colspan="5" class="col-val">
            自 <strong>{{ borrow.startTime || '2026-10-28 10:00' }}</strong> 起
            至 <strong>{{ borrow.expectedReturnTime || '2026-10-29 17:00' }}</strong> 止
          </td>
        </tr>

        <tr>
          <td class="col-label">借阅事由</td>
          <td colspan="5" class="col-val reason-cell">
            <div class="reason-content">{{ borrow.purpose || '迎接上级税务机关年度常规抽查，需核对原始凭证与审计底稿。已签署企业保密承诺书。' }}</div>
          </td>
        </tr>

        <!-- 申请部门负责人审批 -->
        <tr>
          <td class="col-label">申请部门<br/>主管意见</td>
          <td colspan="5" class="col-val opinion-cell">
            <div class="opinion-body">
              {{ (borrow.procStatus ?? 0) >= 2 ? '核实属实，符合查阅调卷程序，同意借阅。' : '审核中...' }}
            </div>
            <div class="sign-block">
              <span>主管签字：<u>{{ (borrow.procStatus ?? 0) >= 2 ? '郑总监' : '待签' }}</u></span>
              <span class="sign-date">日期：{{ borrow.createTime ? borrow.createTime.substring(0, 10) : '2026-10-28' }}</span>
            </div>
          </td>
        </tr>

        <!-- 档案室出库核验 -->
        <tr>
          <td class="col-label">综合档案室<br/>出库核准</td>
          <td colspan="5" class="col-val opinion-cell stamp-container">
            <div class="opinion-body">
              {{ borrow.procStatus === 2 ? '档案品相完好无损，已做条码出库登记，提醒按时返还。' : '待办结后调卷出库' }}
            </div>
            <div class="sign-block">
              <span>档案管理员：<u>{{ borrow.procStatus === 2 ? '周档案员' : '待办' }}</u></span>
              <span class="sign-date">日期：{{ borrow.createTime ? borrow.createTime.substring(0, 10) : '2026-10-28' }}</span>
            </div>
            <div v-if="borrow.procStatus === 2" class="stamp-position">
              <ElectronicStamp
                company-name="启航智慧协同科技有限公司"
                stamp-text="档案管理专用章"
                :date="borrow.createTime ? borrow.createTime.substring(0, 10) : '2026-10-28'"
                :size="130"
              />
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="doc-footer">
      <div class="notes">
        保密守则：档案资料严禁私自涂改、撕页、拍照外传或转借他人；归还时须经档案管理员现场清点验收。
      </div>
      <div class="verification-code">
        档案编码：{{ (borrow.id || '55554444').toString(16).toUpperCase() }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { RecordBorrowVO } from '@/types/business';
import ElectronicStamp from '@/components/print/ElectronicStamp.vue';

defineProps<{
  borrow: Partial<RecordBorrowVO>;
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

<template>
  <div class="print-document">
    <!-- 单据大标题 -->
    <div class="doc-header">
      <h1 class="company-name">盛泰智慧协同科技有限公司</h1>
      <h2 class="doc-title">员 工 请 假 审 批 单</h2>
    </div>

    <!-- 单据元数据条 -->
    <div class="doc-meta">
      <span>单据流水号：<strong>{{ leave.leaveNo || 'QJ202610280001' }}</strong></span>
      <span>申请填报日期：{{ leave.createTime ? leave.createTime.substring(0, 10) : '2026-10-28' }}</span>
    </div>

    <!-- 双黑线网格主体表格 -->
    <table class="grid-table">
      <tbody>
        <tr>
          <td class="col-label">申请人姓名</td>
          <td class="col-val">{{ leave.applicantName || '张三' }}</td>
          <td class="col-label">所在部门</td>
          <td class="col-val">{{ leave.deptName || '研发中心' }}</td>
          <td class="col-label">登录账号</td>
          <td class="col-val">{{ leave.applicantUsername || 'zhangsan' }}</td>
        </tr>

        <tr>
          <td class="col-label">请假类别</td>
          <td colspan="5" class="col-val">
            <span class="check-item"><span class="box">{{ leave.leaveType === 'PERSONAL' ? '☑' : '☐' }}</span> 事假</span>
            <span class="check-item"><span class="box">{{ leave.leaveType === 'SICK' ? '☑' : '☐' }}</span> 病假</span>
            <span class="check-item"><span class="box">{{ leave.leaveType === 'ANNUAL' ? '☑' : '☐' }}</span> 年假</span>
            <span class="check-item"><span class="box">{{ leave.leaveType === 'REST' ? '☑' : '☐' }}</span> 调休</span>
            <span class="check-item"><span class="box">{{ leave.leaveType === 'MARRIAGE' ? '☑' : '☐' }}</span> 婚假</span>
            <span class="check-item"><span class="box">{{ leave.leaveType === 'MATERNITY' ? '☑' : '☐' }}</span> 产假/陪产假</span>
            <span class="check-item"><span class="box">{{ leave.leaveType === 'FUNERAL' ? '☑' : '☐' }}</span> 丧假</span>
          </td>
        </tr>

        <tr>
          <td class="col-label">请假时间</td>
          <td colspan="5" class="col-val">
            自 <strong>{{ leave.startTime || '2026-10-28 09:00' }}</strong> 起
            至 <strong>{{ leave.endTime || '2026-10-29 18:00' }}</strong> 止，
            合计 <strong>{{ leave.totalDays || 2 }}</strong> 天
            （折算 <strong>{{ leave.totalHours || 16 }}</strong> 工作小时）
          </td>
        </tr>

        <tr>
          <td class="col-label">请假事由</td>
          <td colspan="5" class="col-val reason-cell">
            <div class="reason-content">{{ leave.reason || '家中有急事需外出处理，已妥善安排交接岗位工作。' }}</div>
            <div v-if="leave.files" class="attachment-hint">【附相关凭证材料已随附件归档】</div>
          </td>
        </tr>

        <!-- 部门主管意见 -->
        <tr>
          <td class="col-label">部门主管<br/>审核意见</td>
          <td colspan="5" class="col-val opinion-cell">
            <div class="opinion-body">
              {{ (leave.procStatus ?? 0) >= 2 ? '情况属实，工作已妥善交接，同意请假。' : '审核中...' }}
            </div>
            <div class="sign-block">
              <span>签字：<u>{{ (leave.procStatus ?? 0) >= 2 ? '李主管' : '待签' }}</u></span>
              <span class="sign-date">日期：{{ leave.createTime ? leave.createTime.substring(0, 10) : '2026-10-28' }}</span>
            </div>
          </td>
        </tr>

        <!-- 分管领导意见 -->
        <tr>
          <td class="col-label">分管领导<br/>审批意见</td>
          <td colspan="5" class="col-val opinion-cell">
            <div class="opinion-body">
              {{ (leave.procStatus ?? 0) >= 2 ? '同意报批。' : '审核中...' }}
            </div>
            <div class="sign-block">
              <span>签字：<u>{{ (leave.procStatus ?? 0) >= 2 ? '王总' : '待签' }}</u></span>
              <span class="sign-date">日期：{{ leave.createTime ? leave.createTime.substring(0, 10) : '2026-10-28' }}</span>
            </div>
          </td>
        </tr>

        <!-- 人力资源部备案归档 -->
        <tr>
          <td class="col-label">人力资源部<br/>复核归档</td>
          <td colspan="5" class="col-val opinion-cell stamp-container">
            <div class="opinion-body">
              {{ leave.procStatus === 2 ? '审批手续完备，假期台账已核销登记。' : '待全流程办结后归档' }}
            </div>
            <div class="sign-block">
              <span>经办人：<u>{{ leave.procStatus === 2 ? '赵人事' : '待办' }}</u></span>
              <span class="sign-date">日期：{{ leave.createTime ? leave.createTime.substring(0, 10) : '2026-10-28' }}</span>
            </div>
            <!-- 加盖电子印章 -->
            <div v-if="leave.procStatus === 2" class="stamp-position">
              <ElectronicStamp
                company-name="盛泰智慧协同科技有限公司"
                stamp-text="人力资源审批专用章"
                :date="leave.createTime ? leave.createTime.substring(0, 10) : '2026-10-28'"
                :size="130"
              />
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 页脚规范与防伪条 -->
    <div class="doc-footer">
      <div class="notes">
        注：1. 员工请假需严格按照 OA 协同流转规范预先申报；2. 请假期满返岗后须在 1 个工作日内核实销假。
      </div>
      <div class="verification-code">
        系统防伪验证码：{{ (leave.id || '9876543210').toString(16).toUpperCase() }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { LeaveVO } from '@/types/business';
import ElectronicStamp from '@/components/print/ElectronicStamp.vue';

defineProps<{
  leave: Partial<LeaveVO>;
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
  min-height: 60px;
  white-space: pre-wrap;
  text-indent: 2em;
}

.attachment-hint {
  font-size: 12px;
  color: #555;
  margin-top: 6px;
}

.opinion-cell {
  height: 90px;
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
  margin-top: 10px;
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

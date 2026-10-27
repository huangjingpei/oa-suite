<template>
  <div class="print-document">
    <div class="doc-header">
      <h1 class="company-name">盛泰智慧协同科技有限公司</h1>
      <h2 class="doc-title">考 勤 异 常 补 卡 审 批 表</h2>
    </div>

    <div class="doc-meta">
      <span>补卡单号：<strong>{{ repair.repairNo || 'BK202610280002' }}</strong></span>
      <span>申请日期：{{ repair.createTime ? repair.createTime.substring(0, 10) : '2026-10-28' }}</span>
    </div>

    <table class="grid-table">
      <tbody>
        <tr>
          <td class="col-label">申请人姓名</td>
          <td class="col-val">{{ repair.applicantName || '李四' }}</td>
          <td class="col-label">所在部门</td>
          <td class="col-val">{{ repair.deptName || '技术运维部' }}</td>
          <td class="col-label">补卡总次数</td>
          <td class="col-val">{{ repair.totalCount || 1 }} 次</td>
        </tr>

        <tr>
          <td class="col-label">总体事由说明</td>
          <td colspan="5" class="col-val">
            <div class="text-content">{{ repair.reason || '因外部网络保障外出公干，未能准时在公司打卡机指纹签到。' }}</div>
          </td>
        </tr>

        <!-- 漏打卡明细子表格 -->
        <tr>
          <td colspan="6" style="padding: 0; border: none;">
            <table class="inner-table">
              <thead>
                <tr>
                  <th style="width: 50px;">序号</th>
                  <th style="width: 120px;">漏卡日期</th>
                  <th style="width: 110px;">补卡班次</th>
                  <th style="width: 160px;">应补打卡时间</th>
                  <th>具体事由 / 证明人</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in (repair.details && repair.details.length ? repair.details : defaultDetails)" :key="index">
                  <td style="text-align: center;">{{ index + 1 }}</td>
                  <td style="text-align: center;">{{ item.repairDate }}</td>
                  <td style="text-align: center;">
                    {{ item.repairType === 'CHECK_IN' ? '上班卡' : item.repairType === 'CHECK_OUT' ? '下班卡' : '全天' }}
                  </td>
                  <td style="text-align: center;">{{ item.repairSignTime }}</td>
                  <td>{{ item.remark || '外出处理现场机房突发告警 / 证明人：王主管' }}</td>
                </tr>
              </tbody>
            </table>
          </td>
        </tr>

        <!-- 部门主管意见 -->
        <tr>
          <td class="col-label">直属主管<br/>审核意见</td>
          <td colspan="5" class="col-val opinion-cell">
            <div class="opinion-body">
              {{ (repair.procStatus ?? 0) >= 2 ? '核实属实，因公外出漏卡，同意补卡。' : '审核中...' }}
            </div>
            <div class="sign-block">
              <span>主管签字：<u>{{ (repair.procStatus ?? 0) >= 2 ? '赵主管' : '待签' }}</u></span>
              <span class="sign-date">日期：{{ repair.createTime ? repair.createTime.substring(0, 10) : '2026-10-28' }}</span>
            </div>
          </td>
        </tr>

        <!-- 人力资源部考勤专员 -->
        <tr>
          <td class="col-label">人力资源部<br/>考勤核准</td>
          <td colspan="5" class="col-val opinion-cell stamp-container">
            <div class="opinion-body">
              {{ repair.procStatus === 2 ? '考勤系统已重置为正常签到记录，本月补卡次数未超限。' : '流程办结后统一录入考勤系统' }}
            </div>
            <div class="sign-block">
              <span>考勤专员：<u>{{ repair.procStatus === 2 ? '孙专员' : '待办' }}</u></span>
              <span class="sign-date">日期：{{ repair.createTime ? repair.createTime.substring(0, 10) : '2026-10-28' }}</span>
            </div>
            <div v-if="repair.procStatus === 2" class="stamp-position">
              <ElectronicStamp
                company-name="盛泰智慧协同科技有限公司"
                stamp-text="考勤核准专用章"
                :date="repair.createTime ? repair.createTime.substring(0, 10) : '2026-10-28'"
                :size="130"
              />
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="doc-footer">
      <div class="notes">
        注：每位员工每月补卡申请原则上不得超过 3 次，补卡申请须附证明人签名或工单记录。
      </div>
      <div class="verification-code">
        单据校验码：{{ (repair.id || '88887777').toString(16).toUpperCase() }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { AttendanceRepairVO } from '@/types/business';
import ElectronicStamp from '@/components/print/ElectronicStamp.vue';

defineProps<{
  repair: Partial<AttendanceRepairVO>;
}>();

const defaultDetails = [
  {
    repairDate: '2026-10-28',
    repairType: 'CHECK_IN' as const,
    repairSignTime: '2026-10-28 09:00:00',
    remark: '外出驻场支持，未能准时指纹打卡 / 证明人：王主管'
  }
];
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

.inner-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  margin: -1px;
}

.inner-table th {
  border: 1px solid #000;
  background-color: #f5f5f5;
  padding: 6px 8px;
  font-family: SimHei, 'Heiti SC', sans-serif;
}

.inner-table td {
  border: 1px solid #000;
  padding: 6px 8px;
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

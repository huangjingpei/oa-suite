<template>
  <div class="business-page">
    <a-card :bordered="false" class="page-card">
      <div class="table-header">
        <div class="header-title">
          <DollarOutlined style="color: #52c41a; margin-right: 8px" />
          <span>员工月度工资条安全中心</span>
          <a-tag color="green" style="margin-left: 12px">安全二级防护</a-tag>
        </div>
        <div v-if="unlocked" class="header-actions">
          <a-space>
            <a-date-picker
              v-model:value="selectedMonth"
              picker="month"
              format="YYYY-MM"
              value-format="YYYY-MM"
              placeholder="按月份快速筛选"
              @change="loadData"
            />
            <a-button @click="loadData">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
            <a-button danger @click="handleLock">
              <template #icon><LockOutlined /></template>
              锁定隐私
            </a-button>
          </a-space>
        </div>
      </div>

      <!-- 状态 1：安全锁定状态 -->
      <div v-if="!unlocked" class="lock-wrapper">
        <div class="lock-box">
          <div class="lock-icon-wrap">
            <SafetyCertificateOutlined class="shield-icon" />
          </div>
          <h3 class="lock-title">薪资数据已安全加密锁定</h3>
          <p class="lock-desc">
            为保障您的个人薪酬隐私安全，查看明细前需通过安全身份核验（请输入当前账号登录密码或二级查询密码）。
          </p>

          <div class="lock-form">
            <a-input-password
              v-model:value="verifyPassword"
              size="large"
              placeholder="请输入安全验证密码"
              class="pwd-input"
              @press-enter="handleUnlock"
            />
            <a-button
              type="primary"
              size="large"
              :loading="verifying"
              style="margin-top: 16px; width: 100%"
              @click="handleUnlock"
            >
              验证身份并解密查阅
            </a-button>
          </div>
        </div>
      </div>

      <!-- 状态 2：解密查阅状态 -->
      <div v-else style="margin-top: 20px">
        <!-- 统计核心卡片 -->
        <a-row v-if="salaryList.length > 0" :gutter="16" style="margin-bottom: 24px">
          <a-col :span="6">
            <a-card :bordered="false" class="stat-card primary-stat">
              <div class="stat-label">最新月度实发工资 (Net Pay)</div>
              <div class="stat-amount net-amount">
                ¥ {{ formatMoney(salaryList[0].netSalary) }}
              </div>
              <div class="stat-footer">
                <span>月份：{{ salaryList[0].salaryMonth }}</span>
                <a-tag v-if="salaryList[0].isConfirmed === 1" color="success">已签收</a-tag>
                <a-tag v-else color="warning">待签收</a-tag>
              </div>
            </a-card>
          </a-col>

          <a-col :span="6">
            <a-card :bordered="false" class="stat-card">
              <div class="stat-label">当月应发总额 (Gross)</div>
              <div class="stat-amount gross-amount">
                ¥ {{ formatMoney(salaryList[0].grossSalary) }}
              </div>
              <div class="stat-footer">
                基本工资 ¥ {{ formatMoney(salaryList[0].baseSalary) }}
              </div>
            </a-card>
          </a-col>

          <a-col :span="6">
            <a-card :bordered="false" class="stat-card">
              <div class="stat-label">五险一金个人扣除</div>
              <div class="stat-amount deduct-amount">
                ¥ {{ formatMoney(salaryList[0].pensionFee + salaryList[0].housingFund) }}
              </div>
              <div class="stat-footer">
                社保 ¥{{ formatMoney(salaryList[0].pensionFee) }} / 公积金 ¥{{ formatMoney(salaryList[0].housingFund) }}
              </div>
            </a-card>
          </a-col>

          <a-col :span="6">
            <a-card :bordered="false" class="stat-card">
              <div class="stat-label">代扣个人所得税</div>
              <div class="stat-amount tax-amount">
                ¥ {{ formatMoney(salaryList[0].taxFee) }}
              </div>
              <div class="stat-footer">
                按综合所得税率依法预扣预缴
              </div>
            </a-card>
          </a-col>
        </a-row>

        <!-- 工资条记录台账表格 -->
        <a-table
          :columns="columns"
          :data-source="salaryList"
          :loading="loading"
          row-key="id"
          :pagination="{
            current: pageNum,
            pageSize: 10,
            total: total,
            onChange: (p: number) => { pageNum = p; loadData(); }
          }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'salaryMonth'">
              <a-tag color="blue">{{ record.salaryMonth }}</a-tag>
            </template>

            <template v-else-if="column.key === 'grossSalary'">
              <span>¥ {{ formatMoney(record.grossSalary) }}</span>
            </template>

            <template v-else-if="column.key === 'deductions'">
              <span style="color: #cf1322">
                - ¥ {{ formatMoney(record.pensionFee + record.housingFund + record.taxFee + record.otherDeduction) }}
              </span>
            </template>

            <template v-else-if="column.key === 'netSalary'">
              <span style="font-weight: 700; color: #52c41a; font-size: 15px">
                ¥ {{ formatMoney(record.netSalary) }}
              </span>
            </template>

            <template v-else-if="column.key === 'status'">
              <a-tag v-if="record.status === 1" color="success">已发放入账</a-tag>
              <a-tag v-else color="default">核算中</a-tag>
            </template>

            <template v-else-if="column.key === 'isConfirmed'">
              <a-tag v-if="record.isConfirmed === 1" color="green">已确认签收</a-tag>
              <a-tag v-else color="orange">待本人签收</a-tag>
            </template>

            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="openSlip(record)">
                  <EyeOutlined /> 查阅工资条
                </a-button>
                <a-popconfirm
                  v-if="record.isConfirmed !== 1"
                  title="确认该月份薪资及扣除项目无误并完成在线签收吗？"
                  @confirm="handleConfirm(record.id)"
                >
                  <a-button type="link" size="small">
                    <CheckOutlined /> 确认签收
                  </a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
    </a-card>

    <!-- 电子工资条弹窗 -->
    <a-modal
      v-model:open="slipModalVisible"
      title="电子工资条 (Electronic Pay Slip)"
      width="720px"
      :footer="null"
    >
      <div v-if="currentSalary" id="salary-slip" class="salary-slip-doc">
        <div class="slip-header">
          <h2 class="slip-title">云枢智慧协同科技有限公司</h2>
          <h3 class="slip-sub">【{{ currentSalary.salaryMonth }}】月度薪资明细单</h3>
        </div>

        <div class="slip-meta">
          <span>员工工号：{{ currentSalary.username }}</span>
          <span>员工姓名：{{ currentSalary.realName }}</span>
          <span>所属部门：{{ currentSalary.deptName || '未归属' }}</span>
          <span>职务岗位：{{ currentSalary.postName || '标准职级' }}</span>
        </div>

        <table class="slip-table">
          <thead>
            <tr>
              <th colspan="2" class="th-inc">【应发项目合计】</th>
              <th colspan="2" class="th-dec">【代扣代缴项目】</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td class="col-k">基本薪资</td>
              <td class="col-v">¥ {{ formatMoney(currentSalary.baseSalary) }}</td>
              <td class="col-k">个人社保五险</td>
              <td class="col-v">¥ {{ formatMoney(currentSalary.pensionFee) }}</td>
            </tr>
            <tr>
              <td class="col-k">岗位工资/津贴</td>
              <td class="col-v">¥ {{ formatMoney(currentSalary.postSalary) }}</td>
              <td class="col-k">住房公积金</td>
              <td class="col-v">¥ {{ formatMoney(currentSalary.housingFund) }}</td>
            </tr>
            <tr>
              <td class="col-k">月度绩效奖金</td>
              <td class="col-v">¥ {{ formatMoney(currentSalary.performanceSalary) }}</td>
              <td class="col-k">个人所得税</td>
              <td class="col-v">¥ {{ formatMoney(currentSalary.taxFee) }}</td>
            </tr>
            <tr>
              <td class="col-k">加班工时补贴</td>
              <td class="col-v">¥ {{ formatMoney(currentSalary.overtimeSalary) }}</td>
              <td class="col-k">其他考勤扣除</td>
              <td class="col-v">¥ {{ formatMoney(currentSalary.otherDeduction) }}</td>
            </tr>
            <tr>
              <td class="col-k">全勤与专项奖</td>
              <td class="col-v">¥ {{ formatMoney(currentSalary.bonus) }}</td>
              <td class="col-k">扣款小计</td>
              <td class="col-v" style="color: #cf1322">
                ¥ {{ formatMoney(currentSalary.pensionFee + currentSalary.housingFund + currentSalary.taxFee + currentSalary.otherDeduction) }}
              </td>
            </tr>
            <tr class="tr-total">
              <td class="col-k font-bold">应发总额</td>
              <td class="col-v font-bold">¥ {{ formatMoney(currentSalary.grossSalary) }}</td>
              <td class="col-k font-bold text-success">税后实发金额</td>
              <td class="col-v font-bold text-success font-large">¥ {{ formatMoney(currentSalary.netSalary) }}</td>
            </tr>
          </tbody>
        </table>

        <div v-if="currentSalary.remark" class="slip-remark">
          备注：{{ currentSalary.remark }}
        </div>

        <div class="slip-footer">
          <div>核算单位：云枢协同财务与薪酬中心</div>
          <div>签收状态：{{ currentSalary.isConfirmed === 1 ? '已在线核对签收' : '待本人签收' }}</div>
        </div>

        <div class="slip-action no-print">
          <a-space>
            <a-button type="primary" @click="handlePrintSlip">
              <PrinterOutlined /> 打印工资条
            </a-button>
            <a-button
              v-if="currentSalary.isConfirmed !== 1"
              type="default"
              @click="handleConfirm(currentSalary.id)"
            >
              <CheckOutlined /> 确认无误并签收
            </a-button>
          </a-space>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  DollarOutlined,
  ReloadOutlined,
  LockOutlined,
  SafetyCertificateOutlined,
  EyeOutlined,
  CheckOutlined,
  PrinterOutlined
} from '@ant-design/icons-vue';
import {
  verifySalaryPasswordApi,
  getMySalariesApi,
  confirmSalaryApi
} from '@/api/business';
import type { SalaryVO } from '@/types/business';

const unlocked = ref(false);
const verifying = ref(false);
const verifyPassword = ref('');

const loading = ref(false);
const salaryList = ref<SalaryVO[]>([]);
const total = ref(0);
const pageNum = ref(1);
const selectedMonth = ref<string | undefined>(undefined);

const slipModalVisible = ref(false);
const currentSalary = ref<SalaryVO | null>(null);

const columns = [
  { title: '薪酬月份', dataIndex: 'salaryMonth', key: 'salaryMonth', width: 130 },
  { title: '姓名', dataIndex: 'realName', key: 'realName', width: 120 },
  { title: '所属部门', dataIndex: 'deptName', key: 'deptName', width: 150 },
  { title: '应发薪资 (Gross)', dataIndex: 'grossSalary', key: 'grossSalary', width: 160 },
  { title: '五险一金及个税', key: 'deductions', width: 160 },
  { title: '实发金额 (Net)', dataIndex: 'netSalary', key: 'netSalary', width: 160 },
  { title: '发放状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '签收状态', dataIndex: 'isConfirmed', key: 'isConfirmed', width: 120 },
  { title: '操作', key: 'action', width: 180, align: 'center' as const }
];

const formatMoney = (val?: number) => {
  if (val === undefined || val === null) return '0.00';
  return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
};

const handleUnlock = async () => {
  if (!verifyPassword.value.trim()) {
    message.warning('请输入安全验证密码');
    return;
  }
  try {
    verifying.value = true;
    const res = await verifySalaryPasswordApi({ password: verifyPassword.value });
    if (res.code === 200) {
      unlocked.value = true;
      message.success('安全核验通过，薪酬数据已解密');
      loadData();
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  } finally {
    verifying.value = false;
  }
};

const handleLock = () => {
  unlocked.value = false;
  verifyPassword.value = '';
  salaryList.value = [];
  message.info('薪酬数据已重新安全锁定');
};

const loadData = async () => {
  if (!unlocked.value) return;
  loading.value = true;
  try {
    const res = await getMySalariesApi({
      month: selectedMonth.value,
      pageNum: pageNum.value,
      pageSize: 10
    });
    if (res.code === 200 && res.data) {
      salaryList.value = res.data.records || [];
      total.value = res.data.total || 0;
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  } finally {
    loading.value = false;
  }
};

const openSlip = (record: SalaryVO) => {
  currentSalary.value = record;
  slipModalVisible.value = true;
};

const handleConfirm = async (id: number) => {
  try {
    const res = await confirmSalaryApi(id);
    if (res.code === 200) {
      message.success('已确认核对并完成在线签收');
      if (currentSalary.value && currentSalary.value.id === id) {
        currentSalary.value.isConfirmed = 1;
      }
      loadData();
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  }
};

const handlePrintSlip = () => {
  window.print();
};

onMounted(() => {
  // 保持安全锁定，用户输入密码后加载
});
</script>

<style scoped>
.business-page {
  padding: 0;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f1f1f;
  display: flex;
  align-items: center;
}

.lock-wrapper {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.lock-box {
  width: 440px;
  text-align: center;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 36px 32px;
}

.lock-icon-wrap {
  margin-bottom: 16px;
}

.shield-icon {
  font-size: 48px;
  color: #1677ff;
}

.lock-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.lock-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 24px;
}

.lock-form {
  max-width: 320px;
  margin: 0 auto;
}

.stat-card {
  border-radius: 8px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
}

.primary-stat {
  background: linear-gradient(135deg, #f6ffed 0%, #e6f7ff 100%);
  border-color: #b7eb8f;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 6px;
}

.stat-amount {
  font-size: 24px;
  font-weight: 700;
}

.net-amount {
  color: #52c41a;
}

.gross-amount {
  color: #1677ff;
}

.deduct-amount {
  color: #cf1322;
}

.tax-amount {
  color: #d46b08;
}

.stat-footer {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 6px;
  display: flex;
  justify-content: space-between;
}

.salary-slip-doc {
  padding: 16px 20px;
  background: #ffffff;
}

.slip-header {
  text-align: center;
  margin-bottom: 16px;
}

.slip-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 6px 0;
}

.slip-sub {
  font-size: 15px;
  color: #1677ff;
  margin: 0;
}

.slip-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #475569;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #cbd5e1;
}

.slip-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #cbd5e1;
  font-size: 13px;
}

.slip-table th {
  padding: 8px;
  text-align: center;
}

.th-inc {
  background: #f0fdf4;
  color: #166534;
}

.th-dec {
  background: #fef2f2;
  color: #991b1b;
}

.slip-table td {
  border: 1px solid #e2e8f0;
  padding: 8px 12px;
}

.col-k {
  background: #f8fafc;
  width: 120px;
  color: #475569;
}

.col-v {
  width: 160px;
  text-align: right;
  font-family: monospace;
}

.tr-total td {
  background: #f1f5f9;
}

.font-bold {
  font-weight: 700;
}

.text-success {
  color: #15803d;
}

.font-large {
  font-size: 16px;
}

.slip-remark {
  font-size: 12px;
  color: #64748b;
  margin-top: 10px;
}

.slip-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #94a3b8;
  margin-top: 14px;
  padding-top: 8px;
  border-top: 1px dashed #cbd5e1;
}

.slip-action {
  text-align: center;
  margin-top: 20px;
}

@media print {
  .no-print {
    display: none !important;
  }
}
</style>

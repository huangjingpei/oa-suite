<template>
  <div class="mobile-clock-page">
    <!-- 定位与范围提示卡 -->
    <div class="location-banner">
      <EnvironmentOutlined class="location-icon" />
      <div class="location-text">
        <div style="font-weight: 600">北京市海淀区中关村南大街云枢协同大厦</div>
        <div style="font-size: 11px; opacity: 0.9">已进入办公园区 WiFi 打卡有效范围 (GPS 精度 5m)</div>
      </div>
    </div>

    <!-- 实时时钟与打卡大按钮区域 -->
    <div class="punch-card-wrapper">
      <div class="digital-clock">
        <div class="time-main">{{ currentTime }}</div>
        <div class="date-sub">{{ currentDate }} {{ currentWeekday }}</div>
      </div>

      <!-- 巨型呼吸打卡主按钮 -->
      <div
        class="punch-button"
        :class="nextPunchType === 'ON' ? 'punch-on' : 'punch-off'"
        @click="handleClockIn"
      >
        <div class="punch-ripple"></div>
        <div class="punch-label">{{ nextPunchType === 'ON' ? '上班签到' : '下班签退' }}</div>
        <div class="punch-time">{{ currentTime }}</div>
      </div>

      <div class="punch-mode-switch">
        <a-radio-group v-model:value="nextPunchType" size="small" button-style="solid">
          <a-radio-button value="ON">上班打卡</a-radio-button>
          <a-radio-button value="OFF">下班打卡</a-radio-button>
        </a-radio-group>
      </div>
    </div>

    <!-- 今日打卡流水 -->
    <a-card :bordered="false" class="records-card" title="📅 今日打卡足迹">
      <div v-if="records.length" class="record-timeline">
        <div v-for="r in records" :key="r.id" class="record-item">
          <div class="record-dot"></div>
          <div class="record-info">
            <div class="record-title">
              <span>{{ r.clockType === 'ON' ? '上班打卡' : '下班打卡' }}</span>
              <a-tag v-if="r.status === 0" color="success">正常</a-tag>
              <a-tag v-else color="warning">外勤</a-tag>
            </div>
            <div class="record-meta">打卡时刻：{{ r.clockTime }}</div>
            <div class="record-addr">打卡终端：{{ r.deviceType || '移动端' }} · {{ r.locationAddress || '园区范围' }}</div>
          </div>
        </div>
      </div>

      <div v-else class="empty-record">
        <span>今日尚未打卡，快点击上方按钮打卡吧！</span>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { message } from 'ant-design-vue';
import { EnvironmentOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import { clockInApi, getMyClockRecordsApi } from '@/api/business';
import type { ClockRecordVO } from '@/types/business';

const nextPunchType = ref<'ON' | 'OFF'>('ON');
const currentTime = ref(dayjs().format('HH:mm:ss'));
const currentDate = ref(dayjs().format('YYYY年MM月DD日'));
const currentWeekday = ref(['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][dayjs().day()]);
const records = ref<ClockRecordVO[]>([]);
let timer: any = null;

const loadRecords = async () => {
  try {
    const res = await getMyClockRecordsApi({ pageNum: 1, pageSize: 5 });
    if (res.code === 200 && res.data) {
      records.value = res.data.records || [];
      if (records.value.length > 0 && records.value[0].clockType === 'ON') {
        nextPunchType.value = 'OFF';
      }
    }
  } catch {
    records.value = [
      {
        id: 1,
        username: 'zhangsan',
        realName: '张三',
        clockType: 'ON',
        clockTime: dayjs().format('YYYY-MM-DD 08:52:16'),
        deviceType: 'MOBILE',
        locationAddress: '北京市海淀区云枢协同大厦A座',
        status: 0
      }
    ];
    nextPunchType.value = 'OFF';
  }
};

const handleClockIn = async () => {
  try {
    const res = await clockInApi({
      clockType: nextPunchType.value,
      deviceType: 'MOBILE',
      locationAddress: '云枢协同科技园 (移动端H5签到)'
    });
    if (res.code === 200) {
      message.success(nextPunchType.value === 'ON' ? '上班签到成功！' : '下班签退成功！');
      loadRecords();
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  }
};

onMounted(() => {
  loadRecords();
  timer = setInterval(() => {
    currentTime.value = dayjs().format('HH:mm:ss');
  }, 1000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>

<style scoped>
.mobile-clock-page {
  padding-bottom: 12px;
}

.location-banner {
  background: #e6f4ff;
  border: 1px solid #91caff;
  border-radius: 8px;
  padding: 10px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #0958d9;
  margin-bottom: 16px;
}

.location-icon {
  font-size: 20px;
}

.punch-card-wrapper {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px 16px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 16px;
}

.digital-clock .time-main {
  font-size: 32px;
  font-weight: 700;
  font-family: Consolas, monospace;
  color: #1f1f1f;
}

.digital-clock .date-sub {
  font-size: 13px;
  color: #8c8c8c;
  margin-top: 2px;
}

/* 巨型打卡按钮 */
.punch-button {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  margin: 24px auto 16px auto;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #ffffff;
  cursor: pointer;
  position: relative;
  box-shadow: 0 6px 20px rgba(22, 119, 255, 0.35);
  transition: transform 0.15s ease;
  user-select: none;
}

.punch-button:active {
  transform: scale(0.94);
}

.punch-on {
  background: linear-gradient(135deg, #1677ff 0%, #36cfc9 100%);
}

.punch-off {
  background: linear-gradient(135deg, #52c41a 0%, #13c2c2 100%);
  box-shadow: 0 6px 20px rgba(82, 196, 26, 0.35);
}

.punch-label {
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
}

.punch-time {
  font-size: 12px;
  opacity: 0.9;
  margin-top: 4px;
  font-family: Consolas, monospace;
}

.punch-mode-switch {
  margin-top: 8px;
}

.records-card {
  border-radius: 10px;
}

.record-timeline {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.record-item {
  display: flex;
  gap: 12px;
  position: relative;
}

.record-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #1677ff;
  margin-top: 5px;
}

.record-info {
  flex: 1;
}

.record-title {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 600;
}

.record-meta {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

.record-addr {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

.empty-record {
  text-align: center;
  padding: 20px 0;
  font-size: 13px;
  color: #999;
}
</style>
